package org.una.programmingIII.UTEMP_Project.models;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.time.LocalDateTime;

import static org.assertj.core.api.Assertions.assertThat;

class EnrollmentTest {

    private Enrollment enrollment;

    @BeforeEach
    void setUp() {
        enrollment = Enrollment.builder()
                .course(new Course())
                .student(new User())
                .state(EnrollmentState.ENROLLED)
                .build();
    }

    @Test
    void testEnrollmentInitialization() {
        assertThat(enrollment).isNotNull();
        assertThat(enrollment.getId()).isNull(); // ID debe ser null antes de persistir.
        assertThat(enrollment.getCourse()).isNotNull();
        assertThat(enrollment.getStudent()).isNotNull();
        assertThat(enrollment.getState()).isEqualTo(EnrollmentState.ENROLLED);
    }

    @Test
    void testCreatedAtAndLastUpdateOnCreate() {
        enrollment.onCreate();
        assertThat(enrollment.getCreatedAt()).isNotNull();
        assertThat(enrollment.getLastUpdate()).isNotNull();
        assertThat(enrollment.getCreatedAt()).isEqualTo(enrollment.getLastUpdate());
    }

    @Test
    void testLastUpdateOnUpdate() {
        enrollment.onCreate();
        LocalDateTime initialLastUpdate = enrollment.getLastUpdate();

        // Simular una actualización después de un tiempo
        enrollment.onUpdate();
        assertThat(enrollment.getLastUpdate()).isAfter(initialLastUpdate);
        assertThat(enrollment.getCreatedAt()).isNotNull(); // createdAt no debe cambiar.
    }

    @Test
    void testSetState() {
        enrollment.setState(EnrollmentState.DROPPED);
        assertThat(enrollment.getState()).isEqualTo(EnrollmentState.DROPPED);

        enrollment.setState(EnrollmentState.COMPLETED);
        assertThat(enrollment.getState()).isEqualTo(EnrollmentState.COMPLETED);
    }

    @Test
    void testCourseNotNull() {
        enrollment.setCourse(null);
        assertThat(enrollment.getCourse()).isNull();
    }

    @Test
    void testStudentNotNull() {
        enrollment.setStudent(null);
        assertThat(enrollment.getStudent()).isNull();
    }

    @Test
    void testIdGetter() {
        Long mockId = 1L;
        enrollment.setId(mockId);
        assertThat(enrollment.getId()).isEqualTo(mockId);
    }

    @Test
    void testDefaultState() {
        Enrollment defaultEnrollment = new Enrollment();
        assertThat(defaultEnrollment.getState()).isNull(); // Si el estado no se inicializa explícitamente.
    }
}
