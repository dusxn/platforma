package org.raflab.platforma_server.repository;

import org.raflab.platforma_server.model.Group;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.*;
import org.springframework.data.repository.query.*;
import org.springframework.stereotype.Repository;

import java.util.Optional;

public interface GroupRepository extends JpaRepository<Group, Long>{
    @Query("SELECT g FROM Group g JOIN FETCH g.examTerm WHERE g.id = :id")
    Optional<Group> findByIdWithExamTerm(@Param("id") Long id);
}
