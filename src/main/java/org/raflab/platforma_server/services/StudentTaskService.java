package org.raflab.platforma_server.services;

import java.io.IOException;
import java.nio.file.InvalidPathException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

import lombok.RequiredArgsConstructor;
import org.raflab.platforma_server.controllers.response.CommentResponse;
import org.raflab.platforma_server.controllers.response.SolutionResponse;
import org.raflab.platforma_server.controllers.response.TaskResponse;
import org.raflab.platforma_server.exceptions.ForbiddenException;
import org.raflab.platforma_server.exceptions.ResourceNotFoundException;
import org.raflab.platforma_server.model.*;
import org.raflab.platforma_server.repository.*;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.FileSystemResource;
import org.springframework.core.io.Resource;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;
import org.springframework.web.multipart.MultipartFile;

@Service
@RequiredArgsConstructor
public class StudentTaskService {
    private final StudentRepository studentRepository;
    private final TaskRepository taskRepository;
    private final ExamRegistrationRepository examRegistrationRepository;
    private final SolutionRepository solutionRepository;
    private final CommentRepository commentRepository;

    @Value("${app.tasks.storage-dir:task-archives}")
    private String storageDirectory;

    @Value("${app.solutions.storage-dir:task-solutions}")
    private String solutionStorageDirectory;

    public record TaskArchive(Resource resource, String fileName) {
    }


    @Transactional
    public TaskArchive downloadTask(String username, Long taskId) {

        Student student = studentRepository.findByUser_Username(username)
                .orElseThrow(() -> new ForbiddenException("The logged-in user is not a student"));

        Task task = taskRepository.findByIdWithGroupAndExamTerm(taskId)
                .orElseThrow(() -> new ResourceNotFoundException("Task not found with id: " + taskId));

        ExamTerm examTerm = task.getGroup().getExamTerm();
        if (!examRegistrationRepository.existsByStudentIdAndExamTermIdAndGroupId(
                student.getId(), examTerm.getId(), task.getGroup().getId())) {
            throw new ForbiddenException("You are not registered for this task's group and exam term");
        }

        LocalDateTime now = LocalDateTime.now();
        System.out.println("Current time: " + now);

        if (now.isBefore(examTerm.getStartTime()) || !now.isBefore(examTerm.getEndTime())) {
            throw new ForbiddenException("The task can only be downloaded during the exam term");
        }

        Path archivePath = resolveArchivePath(task.getArchivePath());
        Solution solution = solutionRepository.findByStudentIdAndTaskId(student.getId(), taskId)
                .orElseGet(() -> {
                    Solution newSolution = new Solution();
                    newSolution.setStudent(student);
                    newSolution.setTask(task);
                    return newSolution;
                });
        if (solution.getDownloadTime() == null) {
            solution.setDownloadTime(now);
        }
        solutionRepository.save(solution);

        return new TaskArchive(new FileSystemResource(archivePath), archivePath.getFileName().toString());
    }

    private Path resolveArchivePath(String relativePath) {
        try {
            if (relativePath == null || relativePath.isBlank()) {
                throw new ResourceNotFoundException("Task archive not found");
            }

            Path root = Path.of(storageDirectory).toRealPath();
            Path archive = root.resolve(relativePath).normalize();
            if (!archive.startsWith(root)) {
                throw new ResourceNotFoundException("Task archive not found");
            }

            Path realArchive = archive.toRealPath();
            if (!realArchive.startsWith(root) || !Files.isRegularFile(realArchive) || !Files.isReadable(realArchive)) {
                throw new ResourceNotFoundException("Task archive not found");
            }
            return realArchive;
        } catch (InvalidPathException | IOException exception) {
            throw new ResourceNotFoundException("Task archive not found");
        }
    }

    @Transactional
    public SolutionResponse submitSolution(Long taskId, MultipartFile file, String currentUsername) throws IOException {
        if (taskId == null) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Task id is required");
        }
        if (file == null || file.isEmpty()) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Solution file must not be empty");
        }

        //Find the student based on the current logged-in user
        Student student = studentRepository.findByUser_Username(currentUsername)
                .orElseThrow(() -> new ResourceNotFoundException("Student profile not found for user: " + currentUsername));

        //Find the task based on the provided taskId
        Task task = taskRepository.findByIdWithGroupAndExamTerm(taskId)
                .orElseThrow(() -> new ResourceNotFoundException("Task not found with id: " + taskId));

        //Check if the student is registered for the exam term associated with the task's group
        ExamTerm examTerm = task.getGroup().getExamTerm();
        boolean isRegistered = examRegistrationRepository.existsByStudentIdAndExamTermIdAndGroupId(student.getId(), examTerm.getId(), task.getGroup().getId());
        if (!isRegistered) {
            throw new ForbiddenException("Student is not registered for this exam term!");
        }

        //Check if the current time is within the allowed submission window
        LocalDateTime now = LocalDateTime.now();
        if (now.isBefore(examTerm.getStartTime())) {
            throw new ForbiddenException("Exam term has not started yet! Submission is not allowed.");
        }
        if (now.isAfter(examTerm.getEndTime())) {
            throw new ForbiddenException("Exam term has expired! Submission is no longer allowed.");
        }

        //Create or update the solution entity for the student and task
        Solution solution = solutionRepository.findByStudentIdAndTaskId(student.getId(), task.getId())
                .orElseGet(Solution::new);

        Path storageRoot = Path.of(solutionStorageDirectory).toAbsolutePath().normalize();
        Path submissionDirectory = storageRoot.resolve(student.getId().toString())
                .resolve(task.getId().toString()).normalize();
        if (!submissionDirectory.startsWith(storageRoot)) {
            throw new ResponseStatusException(HttpStatus.INTERNAL_SERVER_ERROR, "Invalid solution storage path");
        }
        Files.createDirectories(submissionDirectory);

        String extension = getSafeExtension(file.getOriginalFilename());
        String fileName = UUID.randomUUID() + extension;
        Path destination = submissionDirectory.resolve(fileName);
        file.transferTo(destination);
        String relativePath = storageRoot.relativize(destination).toString().replace('\\', '/');

        solution.setTask(task);
        solution.setStudent(student);
        solution.setSolutionPath(relativePath);
        solution.setSubmissionTime(now);

        Solution saved = solutionRepository.save(solution);
        return toSolutionResponse(saved);
    }

    private String getSafeExtension(String originalFilename) {
        if (originalFilename == null) {
            return "";
        }
        int extensionIndex = originalFilename.lastIndexOf('.');
        if (extensionIndex < 0 || extensionIndex == originalFilename.length() - 1) {
            return "";
        }
        String extension = originalFilename.substring(extensionIndex + 1);
        return extension.matches("[A-Za-z0-9]{1,10}") ? "." + extension : "";
    }

    @Transactional(readOnly = true)
    public List<SolutionResponse> getStudentSolutions(String currentUsername) {
        Student student = studentRepository.findByUser_Username(currentUsername)
                .orElseThrow(() -> new ResourceNotFoundException("Student profile not found for user: " + currentUsername));

        return solutionRepository.findByStudentId(student.getId()).stream()
                .map(this::toSolutionResponse)
                .toList();
    }

    private SolutionResponse toSolutionResponse(Solution solution) {
        List<CommentResponse> comments = commentRepository
                .findBySolutionIdOrderByLineNumberAscIdAsc(solution.getId())
                .stream()
                .map(CommentResponse::fromEntity)
                .toList();
        return SolutionResponse.fromEntity(solution, comments);
    }

    @Transactional(readOnly = true)
    public List<TaskResponse> getStudentTasks(String username) {
        Student student = studentRepository.findByUser_Username(username)
                .orElseThrow(() -> new ForbiddenException("The logged-in user is not a student"));

        List<ExamRegistration> registrations = examRegistrationRepository.findByStudentId(student.getId());
        List<Long> groupIds = registrations.stream()
                .map(reg -> reg.getGroup().getId())
                .toList();

        return taskRepository.findByGroupIdIn(groupIds).stream()
                .map(TaskResponse::fromEntity)
                .toList();
    }
}
