package org.raflab.platforma_server.controllers;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.raflab.platforma_server.controllers.request.CreateCommentRequest;
import org.raflab.platforma_server.controllers.request.SetSolutionPointsRequest;
import org.raflab.platforma_server.controllers.response.CommentResponse;
import org.raflab.platforma_server.services.TeacherService;
import org.springframework.core.io.Resource;
import org.springframework.http.ContentDisposition;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.nio.charset.StandardCharsets;
import java.util.List;

@RestController
@RequestMapping("/api/teacher/solutions")
@RequiredArgsConstructor
@PreAuthorize("hasAuthority('ROLE_TEACHER')")
public class TeacherSolutionController {
    private final TeacherService teacherService;

    @GetMapping("/{solutionId}/file")
    public ResponseEntity<Resource> downloadSolutionFile(@PathVariable Long solutionId) {
        TeacherService.SolutionFile solutionFile = teacherService.getSolutionFile(solutionId);
        ContentDisposition disposition = ContentDisposition.attachment()
                .filename(solutionFile.fileName(), StandardCharsets.UTF_8)
                .build();
        return ResponseEntity.ok()
                .header(HttpHeaders.CONTENT_DISPOSITION, disposition.toString())
                .contentType(MediaType.APPLICATION_OCTET_STREAM)
                .body(solutionFile.resource());
    }

    @PostMapping("/{solutionId}/comments")
    public ResponseEntity<CommentResponse> addComment(
            @PathVariable Long solutionId,
            @Valid @RequestBody CreateCommentRequest request
    ) {
        return ResponseEntity.status(HttpStatus.CREATED).body(teacherService.addComment(solutionId, request));
    }

    @GetMapping("/{solutionId}/comments")
    public ResponseEntity<List<CommentResponse>> getComments(@PathVariable Long solutionId) {
        return ResponseEntity.ok(teacherService.getComments(solutionId));
    }

    @PutMapping("/{solutionId}/points")
    public ResponseEntity<Void> setPoints(
            @PathVariable Long solutionId,
            @Valid @RequestBody SetSolutionPointsRequest request
    ) {
        teacherService.setSolutionPoints(solutionId, request);
        return ResponseEntity.noContent().build();
    }
}
