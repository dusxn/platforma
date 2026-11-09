package org.raflab.platforma_server.repository;

import org.raflab.platforma_server.model.Student;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.Optional;

public interface StudentRepository extends JpaRepository<Student, Long> {
    Optional<Student> findByIndexNumber(String indexNumber);
    Optional<Student> findByUser_Username(String username);
}
