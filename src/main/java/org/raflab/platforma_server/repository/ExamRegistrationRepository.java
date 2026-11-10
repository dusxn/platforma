package org.raflab.platforma_server.repository;

import org.raflab.platforma_server.model.ExamRegistration;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

public interface ExamRegistrationRepository extends JpaRepository<ExamRegistration, Long> {
    List<ExamRegistration> findByExamTermId(Long examTermId);
    List<ExamRegistration> findByStudentId(Long studentId);
    List<ExamRegistration> findByGroupId(Long groupId);
    boolean existsByStudentIdAndExamTermId(Long studentId, Long examTermId);
    boolean existsByStudentIdAndExamTermIdAndGroupId(Long studentId, Long examTermId, Long groupId);
}
