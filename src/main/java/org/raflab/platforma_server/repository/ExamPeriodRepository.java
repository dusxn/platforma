package org.raflab.platforma_server.repository;

import org.raflab.platforma_server.model.ExamPeriod;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

public interface ExamPeriodRepository extends JpaRepository<ExamPeriod, Long>{
}
