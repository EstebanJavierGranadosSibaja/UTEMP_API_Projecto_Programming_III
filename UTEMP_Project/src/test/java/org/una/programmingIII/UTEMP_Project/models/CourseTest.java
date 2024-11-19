package org.una.programmingIII.UTEMP_Project.models;

import static org.junit.jupiter.api.Assertions.*;

import jakarta.validation.ConstraintViolation;
import jakarta.validation.Validation;
import jakarta.validation.Validator;
import jakarta.validation.ValidatorFactory;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDateTime;
import java.util.Set;

@ExtendWith(MockitoExtension.class)
public class CourseTest {

    @InjectMocks
    private Course course;

    private Validator validator;

    @Mock
    private User teacher; // Mock de User, ya que Course tiene una relación con User

    @Mock
    private Department department; // Mock de Department, ya que Course tiene una relación con Department

    @BeforeEach
    public void setUp() {
        ValidatorFactory factory = Validation.buildDefaultValidatorFactory();
        validator = factory.getValidator();
        course = Course.builder()
                .name("Test Course")
                .description("This is a test course.")
                .teacher(teacher)
                .department(department)
                .state(CourseState.ACTIVE)
                .createdAt(LocalDateTime.now())
                .lastUpdate(LocalDateTime.now())
                .build();
        course.onCreate(); // Simula la creación al iniciar
    }

    @Test
    void testCreateCourse() {
        assertNotNull(course);
        assertEquals("Test Course", course.getName());
        assertEquals("This is a test course.", course.getDescription());
        assertNotNull(course.getCreatedAt());
        assertNotNull(course.getLastUpdate());
        assertEquals(CourseState.ACTIVE, course.getState());
        assertTrue(course.getAssignments().isEmpty()); // Verifica que la lista de asignaciones esté vacía al inicio
        assertTrue(course.getEnrollments().isEmpty()); // Verifica que la lista de inscripciones esté vacía al inicio
    }

    @Test
    void testPrePersist() {
        assertNotNull(course.getCreatedAt());
        assertNotNull(course.getLastUpdate());
    }

    @Test
    void testPreUpdate() {
        LocalDateTime previousUpdate = course.getLastUpdate();

        try {
            Thread.sleep(10);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }

        course.onUpdate();
        assertNotEquals(previousUpdate, course.getLastUpdate(), "lastUpdate no se actualizó correctamente");
    }


    @Test
    void testValidationNameNotNull() {
        course.setName(null);
        Set<ConstraintViolation<Course>> violations = validator.validate(course);
        assertEquals(1, violations.size());
        assertEquals("Course name must not be null", violations.iterator().next().getMessage());
    }

    @Test
    void testValidationDescriptionNotNull() {
        course.setDescription(null);
        Set<ConstraintViolation<Course>> violations = validator.validate(course);
        assertEquals(1, violations.size());
        assertEquals("Description must not be null", violations.iterator().next().getMessage());
    }

    @Test
    void testValidationTeacherNotNull() {
        course.setTeacher(null);
        Set<ConstraintViolation<Course>> violations = validator.validate(course);
        assertEquals(1, violations.size());
        assertEquals("Teacher must not be null", violations.iterator().next().getMessage());
    }

    @Test
    void testValidationDepartmentNotNull() {
        course.setDepartment(null);
        Set<ConstraintViolation<Course>> violations = validator.validate(course);
        assertEquals(1, violations.size());
        assertEquals("Department must not be null", violations.iterator().next().getMessage());
    }

    @Test
    void testAddAssignment() {
        Assignment assignment = new Assignment();
        assignment.setCourse(course); // Relacionar el Assignment con el Course
        course.getAssignments().add(assignment);
        assertEquals(1, course.getAssignments().size());
        assertTrue(course.getAssignments().contains(assignment));
    }

    @Test
    void testAddEnrollment() {
        Enrollment enrollment = new Enrollment();
        enrollment.setCourse(course); // Relacionar la Enrollment con el Course
        course.getEnrollments().add(enrollment);
        assertEquals(1, course.getEnrollments().size());
        assertTrue(course.getEnrollments().contains(enrollment));
    }

    @Test
    void testStateChange() {
        course.setState(CourseState.INACTIVE);
        assertEquals(CourseState.INACTIVE, course.getState());
    }

    @Test
    void testUpdateFields() {
        String newName = "Updated Course Name";
        String newDescription = "Updated Course Description";
        course.setName(newName);
        course.setDescription(newDescription);
        course.onUpdate(); // Simula la actualización
        assertEquals(newName, course.getName());
        assertEquals(newDescription, course.getDescription());
        assertNotNull(course.getLastUpdate());
    }

    @Test
    void testBuilder() {
        Course builtCourse = Course.builder()
                .name("Built Course")
                .description("Description for built course")
                .teacher(teacher)
                .department(department)
                .state(CourseState.ACTIVE)
                .build();

        assertNotNull(builtCourse);
        assertEquals("Built Course", builtCourse.getName());
        assertEquals("Description for built course", builtCourse.getDescription());
        assertEquals(CourseState.ACTIVE, builtCourse.getState());
    }

    @Test
    void testNullValuesOnBuilder() {
        assertThrows(NullPointerException.class, () -> {
            Course.builder()
                    .name(null) // Nombre nulo
                    .description("Valid Description")
                    .teacher(teacher)
                    .department(department)
                    .state(CourseState.ACTIVE)
                    .build();
        });

        assertThrows(NullPointerException.class, () -> {
            Course.builder()
                    .name("Valid Name")
                    .description(null) // Descripción nula
                    .teacher(teacher)
                    .department(department)
                    .state(CourseState.ACTIVE)
                    .build();
        });

        assertThrows(NullPointerException.class, () -> {
            Course.builder()
                    .name("Valid Name")
                    .description("Valid Description")
                    .teacher(null)
                    .department(department)
                    .state(CourseState.ACTIVE)
                    .build();
        });

        assertThrows(NullPointerException.class, () -> {
            Course.builder()
                    .name("Valid Name")
                    .description("Valid Description")
                    .teacher(teacher)
                    .department(null)
                    .state(CourseState.ACTIVE)
                    .build();
        });

        assertThrows(NullPointerException.class, () -> {
            Course.builder()
                    .name("Valid Name")
                    .description("Valid Description")
                    .teacher(teacher)
                    .department(department)
                    .state(null)
                    .build();
        });
    }


    @Test
    void testExtremeDescription() {
        String extremeDescription = "D".repeat(500); // Probar con la longitud máxima permitida
        course.setDescription(extremeDescription);
        Set<ConstraintViolation<Course>> violations = validator.validate(course);
        assertTrue(violations.isEmpty(), "La descripción no debería tener violaciones");
    }

    @Test
    void testEmptyState() {
        course.setState(null);
        Set<ConstraintViolation<Course>> violations = validator.validate(course);
        assertEquals(1, violations.size());
        assertEquals("State must not be null", violations.iterator().next().getMessage());
    }
}
