package org.raflab.platforma_server.model;

import jakarta.persistence.*;
import lombok.*;

@Entity
@Table(name = "students")
@Data
@NoArgsConstructor
public class Student {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private String firstName;

    @Column(nullable = false)
    private String lastName;

    @Column(name = "index_number", unique = true, nullable = false)
    private String indexNumber;

    @OneToOne
    @JoinColumn(name = "user_id")
    private User user;
}
