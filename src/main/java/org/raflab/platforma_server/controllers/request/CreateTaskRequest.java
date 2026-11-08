package org.raflab.platforma_server.controllers.request;

public record CreateTaskRequest(String title, String archivePath,Long groupId, Long courseId) {
}
