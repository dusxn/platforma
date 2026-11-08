package org.raflab.platforma_server.controllers;

import lombok.RequiredArgsConstructor;
import org.raflab.platforma_server.controllers.request.BulkExamRegistrationRequest;
import org.raflab.platforma_server.controllers.request.CreateExamRegistrationRequest;
import org.raflab.platforma_server.controllers.request.CreateTaskRequest;
import org.raflab.platforma_server.controllers.response.ExamRegistrationResponse;
import org.raflab.platforma_server.model.ExamRegistration;
import org.raflab.platforma_server.model.Task;
import org.raflab.platforma_server.services.TeacherService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/teacher/tasks")
@RequiredArgsConstructor
@PreAuthorize("hasAuthority('ROLE_TEACHER')")
public class TeacherTaskController {
    private final TeacherService teacherService;

    //Create a new task
    @PostMapping
    public ResponseEntity<Task> createTask(@RequestBody CreateTaskRequest request) {
        Task createdTask = teacherService.createTask(request);
        return ResponseEntity.status(HttpStatus.CREATED).body(createdTask);
    }

    //Get all tasks for a specific group
    @GetMapping("/group/{groupId}")
    public ResponseEntity<List<Task>> getTasksByGroup(@PathVariable Long groupId) {
        return ResponseEntity.ok(teacherService.getTasksByGroupId(groupId));
    }

    //Get all exam registrations for a specific exam term
    @GetMapping("/registrations")
    public ResponseEntity<List<ExamRegistrationResponse>> getRegistrations(@RequestParam Long examTermId) {
        return ResponseEntity.ok(teacherService.getRegistrationsForExamTerm(examTermId));
    }

    // Bulk register students for an exam
    @PostMapping("/registrations")
    public ResponseEntity<List<ExamRegistrationResponse>> registerStudentsBulk(@RequestBody BulkExamRegistrationRequest request) {
        List<ExamRegistrationResponse> response = teacherService.registerStudentsBulk(request);
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }
}
