package org.raflab.platforma_server.repository;

import org.raflab.platforma_server.model.Group;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

public interface GroupRepository extends JpaRepository<Group, Long>{
}
