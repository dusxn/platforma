package org.raflab.platforma_server.controllers.request;

public record CreateExamRegistrationRequest(Long studentId,Long examTermId, Long groupId) {
}
