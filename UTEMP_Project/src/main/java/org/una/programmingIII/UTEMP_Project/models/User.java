package org.una.programmingIII.UTEMP_Project.models;

import jakarta.persistence.*;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.ColumnDefault;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.una.programmingIII.UTEMP_Project.transformers.converters.UserPermissionConverter;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Entity
@Table(name = "users")
public class User implements Identifiable {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id", nullable = false)
    private Long id;

    @NotBlank(message = "Name must not be blank")
    @Size(max = 100, message = "Name must be at most 100 characters long")
    @Column(name = "name", nullable = false, length = 100)
    private String name;

    @NotNull(message = "Email must not be null")
    @Email(message = "Email should be valid")
    @Size(max = 150, message = "Email must be at most 150 characters long")
    @Column(name = "email", nullable = false, length = 150)
    private String email;

    @NotNull(message = "Password must not be null")
    @Size(min = 8, max = 128, message = "Password must be between 8 and 128 characters long")
    @Column(name = "password", nullable = false, length = 128)
    private String password;

    @Size(max = 50, message = "Identification number must be at most 50 characters long")
    @Column(name = "identification_number", length = 50)
    private String identificationNumber;

    @OneToMany(mappedBy = "teacher")
    @Builder.Default
    private List<Course> coursesTeaching = new ArrayList<>();

    @OneToMany(mappedBy = "user")
    @Builder.Default
    private List<Notification> notifications = new ArrayList<>();

    @OneToMany(mappedBy = "student")
    @Builder.Default
    private List<Enrollment> userEnrollments = new ArrayList<>();

    @OneToMany(mappedBy = "student")
    @Builder.Default
    private List<Submission> submissions = new ArrayList<>();

    @Enumerated(EnumType.STRING)
    @Column(name = "state", nullable = false)
    @ColumnDefault("'ACTIVE'")
    @Builder.Default
    private UserState state = UserState.INACTIVE;

    @Enumerated(EnumType.STRING)
    @Column(name = "role", nullable = false)
    private UserRole role;

    @Convert(converter = UserPermissionConverter.class)
    @Column(name = "permissions", nullable = false, length = 800)
    @Builder.Default
    private List<UserPermission> permissions = new ArrayList<>();
    @Column(nullable = false, updatable = false)
    private LocalDateTime createdAt;
    @Column(nullable = false)
    private LocalDateTime lastUpdate;

    @Transient
    public Collection<? extends GrantedAuthority> getAuthorities() {
        List<GrantedAuthority> authorities = new ArrayList<>();

        if (role != null) {
            authorities.add(new SimpleGrantedAuthority(role.name()));
        }

        if (permissions != null) {
            for (UserPermission permission : permissions) {
                authorities.add(new SimpleGrantedAuthority(permission.name()));
            }
        }

        return authorities;
    }

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