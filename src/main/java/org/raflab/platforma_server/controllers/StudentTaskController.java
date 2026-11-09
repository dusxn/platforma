package org.raflab.platforma_server.controllers;

import lombok.RequiredArgsConstructor;
import org.raflab.platforma_server.services.StudentTaskService;
import org.springframework.core.io.Resource;
import org.springframework.http.ContentDisposition;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.nio.charset.StandardCharsets;

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
}
