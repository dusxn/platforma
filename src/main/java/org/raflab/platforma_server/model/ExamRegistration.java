package org.raflab.platforma_server.model;

import jakarta.persistence.*;
import lombok.*;

@Entity
@Table(name = "exam_registrations", uniqueConstraints = {
        @UniqueConstraint(columnNames = {"student_id", "exam_term_id"})
})
@Data
@NoArgsConstructor
public class ExamRegistration {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "student_id", nullable = false)
    private Student student;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "exam_term_id", nullable = false)
    private ExamTerm examTerm;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "group_id", nullable = false)
    private Group group;
}
