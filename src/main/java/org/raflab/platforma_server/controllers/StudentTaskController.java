package org.raflab.platforma_server.controllers;

import lombok.RequiredArgsConstructor;
import org.raflab.platforma_server.controllers.response.SolutionResponse;
import org.raflab.platforma_server.controllers.response.TaskResponse;
import org.raflab.platforma_server.services.StudentTaskService;
import org.springframework.core.io.Resource;
import org.springframework.http.*;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.bind.annotation.*;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.List;

@RestController
@RequestMapping("/api/student/tasks")
@RequiredArgsConstructor
@PreAuthorize("hasRole('STUDENT')")
public class StudentTaskController {
    private final StudentTaskService studentTaskService;

    @GetMapping("/{taskId}/download")
    public ResponseEntity<Resource> downloadTask(@PathVariable Long taskId,
                                                  @AuthenticationPrincipal Jwt jwt) {
        StudentTaskService.TaskArchive archive = studentTaskService.downloadTask(jwt.getSubject(), taskId);
        ContentDisposition disposition = ContentDisposition.attachment()
                .filename(archive.fileName(), StandardCharsets.UTF_8)
                .build();

        return ResponseEntity.ok()
                .header(HttpHeaders.CONTENT_DISPOSITION, disposition.toString())
                .contentType(MediaType.APPLICATION_OCTET_STREAM)
                .body(archive.resource());
    }

    @GetMapping
    public ResponseEntity<List<TaskResponse>> getMyTasks(@AuthenticationPrincipal Jwt jwt) {
        String currentUsername = jwt.getSubject();
        List<TaskResponse> tasks = studentTaskService.getStudentTasks(currentUsername);
        return ResponseEntity.ok(tasks);
    }

    @GetMapping("/solutions")
    public ResponseEntity<List<SolutionResponse>> getMySolutions(@AuthenticationPrincipal Jwt jwt) {
        String currentUsername = jwt.getSubject();
        List<SolutionResponse> solutions = studentTaskService.getStudentSolutions(currentUsername);
        return ResponseEntity.ok(solutions);
    }

    @PostMapping(value = "/submit", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<SolutionResponse> submitSolution(@RequestParam("taskId") Long taskId,
                                                           @RequestParam("file") MultipartFile file,
                                                           @AuthenticationPrincipal Jwt jwt) throws IOException {
        String currentUsername = jwt.getSubject();
        SolutionResponse response = studentTaskService.submitSolution(taskId, file, currentUsername);
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }
}
