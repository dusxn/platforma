package org.raflab.platforma_server.model;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import jakarta.persistence.*;
import lombok.*;
import java.time.LocalDateTime;

@Entity
@Table(name = "solutions")
@Data
@NoArgsConstructor
@JsonIgnoreProperties({"hibernateLazyInitializer", "handler"})
public class Solution {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    // Timestamps when student downloaded the task
    @Column(name = "download_time")
    private LocalDateTime downloadTime;

    // Timestamps when student submitted the task
    @Column(name = "submission_time")
    private LocalDateTime submissionTime;

    @Column(name = "solution_path")
    private String solutionPath;

    @Column(name = "points")
    private Double points;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "student_id", nullable = false)
    private Student student;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "task_id", nullable = false)
    @JsonIgnore
    private Task task;
}
