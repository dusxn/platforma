package org.raflab.platforma_server.repository;

import org.raflab.platforma_server.model.Comment;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;


public interface CommentRepository extends JpaRepository<Comment, Long> {
}
