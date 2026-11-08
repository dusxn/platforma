package org.raflab.platforma_server.controllers.response;

public record ExamRegistrationResponse (Long registrationId,
                                       Long studentId,
                                       String studentFirstName,
                                       String studentLastName,
                                       String studentIndexNumber,
                                       Long examTermId,
                                       Long groupId,
                                       String groupName){
    public static ExamRegistrationResponse fromEntity(org.raflab.platforma_server.model.ExamRegistration reg) {
        return new ExamRegistrationResponse(
                reg.getId(),
                reg.getStudent() != null ? reg.getStudent().getId() : null,
                reg.getStudent() != null ? reg.getStudent().getFirstName() : null,
                reg.getStudent() != null ? reg.getStudent().getLastName() : null,
                reg.getStudent() != null ? reg.getStudent().getIndexNumber() : null,
                reg.getExamTerm() != null ? reg.getExamTerm().getId() : null,
                reg.getGroup() != null ? reg.getGroup().getId() : null,
                reg.getGroup() != null ? reg.getGroup().getName() : null
        );
    }

}
