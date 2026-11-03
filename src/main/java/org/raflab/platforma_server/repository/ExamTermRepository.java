package org.raflab.platforma_server.repository;

import org.raflab.platforma_server.model.ExamTerm;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

public interface ExamTermRepository extends JpaRepository<ExamTerm, Long>{
}
