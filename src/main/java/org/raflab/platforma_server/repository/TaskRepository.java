package org.raflab.platforma_server.repository;

import org.raflab.platforma_server.model.Task;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

public interface TaskRepository extends JpaRepository<Task, Long> {
    List<Task> findByGroupId(Long groupId);
}
