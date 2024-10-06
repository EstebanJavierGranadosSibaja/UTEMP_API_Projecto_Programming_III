package org.una.programmingIII.UTEMP_Project.models;

import jakarta.persistence.*;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.*;
import org.hibernate.annotations.ColumnDefault;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Entity
@Table(name = "submissions")
public class Submission implements Identifiable{
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id", nullable = false)
    private Long id;

    @NotNull(message = "Assignment must not be null")
    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "assignment_id", nullable = false)
    @Builder.Default
    private Assignment assignment = new Assignment();

    @NotNull(message = "Student must not be null")
    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "student_id", nullable = false)
    @Builder.Default
    private User student = new User();

    @NotBlank(message = "File name must not be blank")
    @Size(max = 255, message = "File name must be at most 255 characters long")
    @Column(name = "file_name", nullable = false, length = 255)
    private String fileName;

    @Column(name = "grade")
    private Double grade;

    @Size(max = 500, message = "Comments must be at most 500 characters long")
    @Column(name = "comments", length = 500)
    private String comments;

    @OneToMany(mappedBy = "submission")
    @Builder.Default
    private List<Grade> grades = new ArrayList<>();

    @Enumerated(EnumType.STRING)
    @Column(name = "state", nullable = false)
    @ColumnDefault("'SUBMITTED'")
    private SubmissionState state;

    @Column(nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @Column(nullable = false)
    private LocalDateTime lastUpdate;

    @PrePersist
    protected void onCreate() {
        this.createdAt = LocalDateTime.now();
        this.lastUpdate = LocalDateTime.now();
    }

    @PreUpdate
    protected void onUpdate() {
        this.lastUpdate = LocalDateTime.now();
    }

    @Transient
    @Override
    public Long getId() {
        return this.id;
    }
}
