package org.raflab.platforma_server.services;

import java.io.IOException;
import java.nio.file.InvalidPathException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.LocalDateTime;

import lombok.RequiredArgsConstructor;
import org.raflab.platforma_server.exceptions.ForbiddenException;
import org.raflab.platforma_server.exceptions.ResourceNotFoundException;
import org.raflab.platforma_server.model.ExamTerm;
import org.raflab.platforma_server.model.Solution;
import org.raflab.platforma_server.model.Student;
import org.raflab.platforma_server.model.Task;
import org.raflab.platforma_server.repository.ExamRegistrationRepository;
import org.raflab.platforma_server.repository.SolutionRepository;
import org.raflab.platforma_server.repository.StudentRepository;
import org.raflab.platforma_server.repository.TaskRepository;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.FileSystemResource;
import org.springframework.core.io.Resource;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class StudentTaskService {
    private final StudentRepository studentRepository;
    private final TaskRepository taskRepository;
    private final ExamRegistrationRepository examRegistrationRepository;
    private final SolutionRepository solutionRepository;

    @Value("${app.tasks.storage-dir:task-archives}")
    private String storageDirectory;

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
}
