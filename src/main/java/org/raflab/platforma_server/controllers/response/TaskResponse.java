package org.raflab.platforma_server.controllers.response;

import org.raflab.platforma_server.model.Task;

public record TaskResponse(
        Long taskId,
        String title,
        String archivePath,
        Long groupId,
        String groupName,
        Long examTermId
) {
    public static TaskResponse fromEntity(Task task) {
        return new TaskResponse(
                task.getId(),
                task.getTitle(),
                task.getArchivePath(),
                task.getGroup() != null ? task.getGroup().getId() : null,
                task.getGroup() != null ? task.getGroup().getName() : null,
                task.getGroup() != null && task.getGroup().getExamTerm() != null
                        ? task.getGroup().getExamTerm().getId() : null
        );
    }
}