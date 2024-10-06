package org.una.programmingIII.UTEMP_Project.models;

import jakarta.persistence.*;
import jakarta.validation.constraints.NotNull;
import lombok.*;
import org.hibernate.annotations.ColumnDefault;

import java.time.Instant;
import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;
import java.util.*;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Entity
@Table(name = "assignments")
public class Assignment {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id", nullable = false)
    private Long id;

    @NotNull(message = "Title must not be null")
    @Column(name = "title", nullable = false, length = 100)
    private String title;

    @NotNull(message = "Description must not be null")
    @Column(name = "description", nullable = false, length = 1000)
    private String description;

    @Column(name = "deadline")
    private Instant deadline;

    @NotNull(message = "Course must not be null")
    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "course_id", nullable = false)
    private Course course;

    @OneToMany(mappedBy = "assignment")
    @Builder.Default
    private List<FileMetadatum> fileMetadata = new ArrayList<>();

    @OneToMany(mappedBy = "assignment")
    @Builder.Default
    private List<Submission> submissions = new ArrayList<>();

    @Enumerated(EnumType.STRING)
    @Column(name = "state", nullable = false)
    @ColumnDefault("'PENDING'")
    private AssignmentState state;

    @Column(nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @Column(nullable = false)
    private LocalDateTime lastUpdate;

    @PrePersist
    protected void onCreate() {
        this.createdAt = LocalDateTime.now();
        this.lastUpdate = LocalDateTime.now();
        this.deadline = Instant.now().plus(7, ChronoUnit.DAYS);
    }

    @PreUpdate
    protected void onUpdate() {
        this.lastUpdate = LocalDateTime.now();
    }
}