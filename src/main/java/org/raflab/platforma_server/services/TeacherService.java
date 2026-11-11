package org.raflab.platforma_server.services;

import java.io.IOException;
import java.nio.file.InvalidPathException;
import java.nio.file.Files;
import java.nio.file.Path;
import lombok.RequiredArgsConstructor;
import org.raflab.platforma_server.controllers.request.BulkExamRegistrationRequest;
import org.raflab.platforma_server.controllers.request.CreateExamRegistrationRequest;
import org.raflab.platforma_server.controllers.request.CreateCommentRequest;
import org.raflab.platforma_server.controllers.request.CreateTaskRequest;
import org.raflab.platforma_server.controllers.request.SetSolutionPointsRequest;
import org.raflab.platforma_server.controllers.response.CommentResponse;
import org.raflab.platforma_server.controllers.response.ExamRegistrationResponse;
import org.raflab.platforma_server.controllers.response.TaskSubmissionOverviewResponse;
import org.raflab.platforma_server.exceptions.ResourceNotFoundException;
import org.raflab.platforma_server.model.*;
import org.raflab.platforma_server.repository.*;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.FileSystemResource;
import org.springframework.core.io.Resource;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
public class TeacherService {
    private final TaskRepository taskRepository;
    private final GroupRepository groupRepository;
    private final StudentRepository studentRepository;
    private final ExamTermRepository examTermRepository;
    private final ExamRegistrationRepository examRegistrationRepository;
    private final SolutionRepository solutionRepository;
    private final CommentRepository commentRepository;

    @Value("${app.solutions.storage-dir:task-solutions}")
    private String solutionStorageDirectory;

    public record SolutionFile(Resource resource, String fileName) {
    }

    @Transactional
    public Task createTask(CreateTaskRequest request) {
        Group group = groupRepository.findByIdWithExamTerm(request.groupId())
                .orElseThrow(() -> new ResourceNotFoundException("Group not found with id: " + request.groupId()));
        Task task = new Task();
        task.setGroup(group);
        task.setTitle(request.title());
        task.setArchivePath(request.archivePath());

        return taskRepository.save(task);
    }

    @Transactional
    public ExamRegistrationResponse registerStudentForExam(CreateExamRegistrationRequest request) {
        Student student = studentRepository.findById(request.studentId())
                .orElseThrow(() -> new ResourceNotFoundException("Student not found!"));

        ExamTerm examTerm = examTermRepository.findById(request.examTermId())
                .orElseThrow(() -> new ResourceNotFoundException("Term not found!"));

        Group group = groupRepository.findById(request.groupId())
                .orElseThrow(() -> new ResourceNotFoundException("Group not found!"));

        ExamRegistration registration = new ExamRegistration();
        registration.setStudent(student);
        registration.setExamTerm(examTerm);
        registration.setGroup(group);

        ExamRegistration saved = examRegistrationRepository.save(registration);
        return ExamRegistrationResponse.fromEntity(saved);
    }

    @Transactional(readOnly = true)
    public List<Task> getTasksByGroupId(Long groupId) {
        if (!groupRepository.existsById(groupId)) {
            throw new ResourceNotFoundException("Group not found with id: " + groupId);
        }

        return taskRepository.findByGroupId(groupId);
    }

    @Transactional
    public List<ExamRegistrationResponse> registerStudentsBulk(BulkExamRegistrationRequest request) {
        ExamTerm examTerm = examTermRepository.findById(request.examTermId())
                .orElseThrow(() -> new ResourceNotFoundException("Exam term not found with id: " + request.examTermId()));

        Group group = groupRepository.findById(request.groupId())
                .orElseThrow(() -> new ResourceNotFoundException("Group not found with id: " + request.groupId()));

        List<Student> students = studentRepository.findAllById(request.studentIds());
        if (students.isEmpty()) {
            throw new ResourceNotFoundException("No valid students found for given IDs");
        }

        List<Student> newStudentsToRegister = students.stream()
                .filter(student -> !examRegistrationRepository.existsByStudentIdAndExamTermId(student.getId(), request.examTermId()))
                .toList();

        if (!newStudentsToRegister.isEmpty()) {
            List<ExamRegistration> registrations = newStudentsToRegister.stream().map(student -> {
                ExamRegistration reg = new ExamRegistration();
                reg.setStudent(student);
                reg.setExamTerm(examTerm);
                reg.setGroup(group);
                return reg;
            }).toList();

            examRegistrationRepository.saveAll(registrations);
            examRegistrationRepository.flush();
        }

        return getRegistrationsForExamTerm(request.examTermId());
    }

    @Transactional(readOnly = true)
    public List<ExamRegistrationResponse> getRegistrationsForExamTerm(Long examTermId) {
        if (!examTermRepository.existsById(examTermId)) {
            throw new ResourceNotFoundException("Exam term not found with id: " + examTermId);
        }

        return examRegistrationRepository.findByExamTermId(examTermId)
                .stream()
                .map(ExamRegistrationResponse::fromEntity)
                .toList();
    }

    @Transactional(readOnly = true)
    public List<TaskSubmissionOverviewResponse> getSubmissionsForTask(Long taskId) {
        return solutionRepository.findByTaskId(taskId).stream()
                .map(solution -> new TaskSubmissionOverviewResponse(
                        solution.getStudent().getId(),
                        solution.getStudent().getFirstName(),
                        solution.getStudent().getLastName(),
                        solution.getStudent().getIndexNumber(),
                        solution.getTask().getId(),
                        solution.getTask().getTitle(),
                        solution.getDownloadTime(),
                        solution.getSubmissionTime(),
                        solution.getSolutionPath()
                ))
                .toList();
    }

    @Transactional(readOnly = true)
    public SolutionFile getSolutionFile(Long solutionId) {
        Solution solution = findSolution(solutionId);
        String storedPath = solution.getSolutionPath();
        if (storedPath == null || storedPath.isBlank()) {
            throw new ResourceNotFoundException("Solution file not found");
        }

        try {
            Path root = Path.of(solutionStorageDirectory).toRealPath();
            Path candidate = root.resolve(storedPath).normalize();
            if (!candidate.startsWith(root)) {
                throw new ResourceNotFoundException("Solution file not found");
            }

            Path file = candidate.toRealPath();
            if (!file.startsWith(root) || !Files.isRegularFile(file) || !Files.isReadable(file)) {
                throw new ResourceNotFoundException("Solution file not found");
            }
            return new SolutionFile(new FileSystemResource(file), file.getFileName().toString());
        } catch (InvalidPathException | IOException exception) {
            throw new ResourceNotFoundException("Solution file not found");
        }
    }

    @Transactional
    public CommentResponse addComment(Long solutionId, CreateCommentRequest request) {
        Solution solution = findSolution(solutionId);
        Comment comment = new Comment();
        comment.setSolution(solution);
        comment.setFilePath(request.filePath().trim());
        comment.setLineNumber(request.lineNumber());
        comment.setText(request.text().trim());
        return CommentResponse.fromEntity(commentRepository.save(comment));
    }

    @Transactional(readOnly = true)
    public List<CommentResponse> getComments(Long solutionId) {
        findSolution(solutionId);
        return commentRepository.findBySolutionIdOrderByLineNumberAscIdAsc(solutionId).stream()
                .map(CommentResponse::fromEntity)
                .toList();
    }

    @Transactional
    public void setSolutionPoints(Long solutionId, SetSolutionPointsRequest request) {
        Solution solution = findSolution(solutionId);
        solution.setPoints(request.points());
        solutionRepository.save(solution);
    }

    private Solution findSolution(Long solutionId) {
        return solutionRepository.findById(solutionId)
                .orElseThrow(() -> new ResourceNotFoundException("Solution not found with id: " + solutionId));
    }
}
