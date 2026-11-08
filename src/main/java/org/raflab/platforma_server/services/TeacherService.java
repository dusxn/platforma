package org.raflab.platforma_server.services;

import lombok.RequiredArgsConstructor;
import org.raflab.platforma_server.controllers.request.BulkExamRegistrationRequest;
import org.raflab.platforma_server.controllers.request.CreateExamRegistrationRequest;
import org.raflab.platforma_server.controllers.request.CreateTaskRequest;
import org.raflab.platforma_server.controllers.response.ExamRegistrationResponse;
import org.raflab.platforma_server.exceptions.ResourceNotFoundException;
import org.raflab.platforma_server.model.*;
import org.raflab.platforma_server.repository.*;
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
}
