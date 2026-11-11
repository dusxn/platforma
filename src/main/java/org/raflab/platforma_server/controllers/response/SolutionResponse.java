package org.raflab.platforma_server.controllers.response;

import java.time.LocalDateTime;
import java.util.List;

public record SolutionResponse(
        Long solutionId,
        Long taskId,
        String taskTitle,
        Long studentId,
        String solutionPath,
        LocalDateTime submittedAt,
        Double points,
        List<CommentResponse> comments
) {
    public static SolutionResponse fromEntity(
            org.raflab.platforma_server.model.Solution solution,
            List<CommentResponse> comments
    ) {
        return new SolutionResponse(
                solution.getId(),
                solution.getTask() != null ? solution.getTask().getId() : null,
                solution.getTask() != null ? solution.getTask().getTitle() : null,
                solution.getStudent() != null ? solution.getStudent().getId() : null,
                solution.getSolutionPath(),
                solution.getSubmissionTime(),
                solution.getPoints(),
                comments
        );
    }
}
