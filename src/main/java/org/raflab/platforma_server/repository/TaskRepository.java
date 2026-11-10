package org.raflab.platforma_server.repository;

import org.raflab.platforma_server.model.Task;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

public interface TaskRepository extends JpaRepository<Task, Long> {
    List<Task> findByGroupId(Long groupId);
    List<Task> findByGroupIdIn(List<Long> groupIds);

    @Query("SELECT t FROM Task t JOIN FETCH t.group g JOIN FETCH g.examTerm WHERE t.id = :taskId")
    Optional<Task> findByIdWithGroupAndExamTerm(@Param("taskId") Long taskId);
}
