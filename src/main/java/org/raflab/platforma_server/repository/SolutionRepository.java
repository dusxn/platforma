package org.raflab.platforma_server.repository;

import org.raflab.platforma_server.model.Solution;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

public interface SolutionRepository extends JpaRepository<Solution, Long> {
}
