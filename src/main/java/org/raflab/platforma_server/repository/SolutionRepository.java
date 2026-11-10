package org.raflab.platforma_server.repository;

import org.raflab.platforma_server.model.Solution;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

public interface SolutionRepository extends JpaRepository<Solution, Long> {
    List<Solution> findByStudentId(Long studentId);
    List<Solution> findByTaskId(Long taskId);
    Optional<Solution> findByStudentIdAndTaskId(Long studentId, Long taskId);
}
