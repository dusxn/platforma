package org.raflab.platforma_server.repository;

import org.raflab.platforma_server.model.ExamRegistration;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

public interface ExamRegistrationRepository extends JpaRepository<ExamRegistration, Long> {
}
