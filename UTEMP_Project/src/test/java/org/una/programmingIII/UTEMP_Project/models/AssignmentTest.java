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

import java.time.Instant;
import java.time.LocalDateTime;
import java.util.Set;

@ExtendWith(MockitoExtension.class)
public class AssignmentTest {

    @InjectMocks
    private Assignment assignment;

    private Validator validator;

    @Mock
    private Course course; // Mock de Course, ya que Assignment tiene una relación con Course

    @BeforeEach
    public void setUp() {
        ValidatorFactory factory = Validation.buildDefaultValidatorFactory();
        validator = factory.getValidator();
        assignment = Assignment.builder()
                .title("Test Title")
                .description("Test Description")
                .course(course)
                .createdAt(LocalDateTime.now())
                .lastUpdate(LocalDateTime.now())
                .build();
        assignment.onCreate(); // Simula la creación al iniciar
    }

    @Test
    void testAssignmentInitialization() {
        assertNotNull(assignment);
        assertEquals("Test Title", assignment.getTitle());
        assertEquals("Test Description", assignment.getDescription());
        assertNotNull(assignment.getCreatedAt());
        assertNotNull(assignment.getLastUpdate());
        assertEquals(AssignmentState.PENDING, assignment.getState()); // Asegúrate de que el estado por defecto sea PENDING
    }


    @Test
    void testAssignmentStateChange() {
        assignment.setState(AssignmentState.ONGOING);
        assertEquals(AssignmentState.ONGOING, assignment.getState());

        assignment.setState(AssignmentState.PENDING);
        assertEquals(AssignmentState.PENDING, assignment.getState());
    }

    @Test
    void testCreateAssignment() {
        assertNotNull(assignment);
        assertEquals("Test Title", assignment.getTitle());
        assertEquals("Test Description", assignment.getDescription());
        assertNotNull(assignment.getCreatedAt()); // Verifica que createdAt no sea null
        assertNotNull(assignment.getLastUpdate()); // Verifica que lastUpdate no sea null
        assertNotNull(assignment.getDeadline()); // Verifica que deadline no sea null
    }

    @Test
    void testSetDeadline() {
        Instant deadline = Instant.ofEpochMilli(7);
        assignment.setDeadline(deadline);
        assertEquals(deadline, assignment.getDeadline());
    }

    @Test
    void testUpdateAssignmentFields() {
        String newTitle = "Updated Title";
        assignment.setTitle(newTitle);
        assignment.onUpdate(); // Llama al método de actualización

        assertEquals(newTitle, assignment.getTitle());
        assertNotNull(assignment.getLastUpdate());
    }

    @Test
    void testPrePersist() {
        // Ya se inicializa en setUp(), así que este test puede ser opcional
        assertNotNull(assignment.getCreatedAt());
        assertNotNull(assignment.getLastUpdate());
        assertNotNull(assignment.getDeadline());
    }

    @Test
    void testPreUpdate() {
        assignment.onCreate();
        LocalDateTime previousUpdate = assignment.getLastUpdate();
        try {
            Thread.sleep(10);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }
        assignment.onUpdate();
        assertNotEquals(previousUpdate, assignment.getLastUpdate(), "lastUpdate no se actualizó correctamente");
    }


    @Test
    void testValidationTitleNotNull() {
        assignment.setTitle(null);
        assignment.setDescription("Valid Description");
        assignment.setCourse(course);

        Set<ConstraintViolation<Assignment>> violations = validator.validate(assignment);
        assertEquals(1, violations.size());
        assertEquals("Title must not be null", violations.iterator().next().getMessage());
    }

    @Test
    void testValidationDescriptionNotNull() {
        assignment.setDescription(null);
        assignment.setTitle("Valid Title");
        assignment.setCourse(course);

        Set<ConstraintViolation<Assignment>> violations = validator.validate(assignment);
        assertEquals(1, violations.size());
        assertEquals("Description must not be null", violations.iterator().next().getMessage());
    }

    @Test
    void testValidationCourseNotNull() {
        assignment.setCourse(null);
        assignment.setTitle("Valid Title");
        assignment.setDescription("Valid Description");

        Set<ConstraintViolation<Assignment>> violations = validator.validate(assignment);
        assertEquals(1, violations.size());
        assertEquals("Course must not be null", violations.iterator().next().getMessage());
    }

    @Test
    void testStateDefault() {
        assertEquals(AssignmentState.PENDING, assignment.getState());
    }
}