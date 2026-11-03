package org.raflab.platforma_server.model;

import jakarta.persistence.*;
import lombok.*;

@Entity
@Table(name = "exam_periods")
@Data
@NoArgsConstructor
public class ExamPeriod {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private String name;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "course_id")
    private Course course;
}
