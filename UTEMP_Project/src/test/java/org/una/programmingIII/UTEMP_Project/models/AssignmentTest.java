package org.una.programmingIII.UTEMP_Project.models;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import jakarta.validation.ConstraintViolation;
import jakarta.validation.Validation;
import jakarta.validation.Validator;
import jakarta.validation.ValidatorFactory;

import java.time.Instant;
import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class AssignmentTest {

    @InjectMocks
    private Assignment assignment;

    @Mock
    private Course course;

    private Validator validator;

    @BeforeEach
    void setUp() {
        ValidatorFactory factory = Validation.buildDefaultValidatorFactory();
        validator = factory.getValidator();
        assignment = Assignment.builder()
                .title("Sample Title")
                .description("Sample Description")
                .course(course)
                .build();
    }

    @Test
    void testValidAssignment() {
        Set<ConstraintViolation<Assignment>> violations = validator.validate(assignment);
        assertTrue(violations.isEmpty(), "No violations should occur for a valid assignment");
    }

    @Test
    void testNullTitle() {
        assignment.setTitle(null);
        Set<ConstraintViolation<Assignment>> violations = validator.validate(assignment);
        assertEquals(1, violations.size());
        assertEquals("Title must not be null", violations.iterator().next().getMessage());
    }

    @Test
    void testNullDescription() {
        assignment.setDescription(null);
        Set<ConstraintViolation<Assignment>> violations = validator.validate(assignment);
        assertEquals(1, violations.size());
        assertEquals("Description must not be null", violations.iterator().next().getMessage());
    }

    @Test
    void testNullCourse() {
        assignment.setCourse(null);
        Set<ConstraintViolation<Assignment>> violations = validator.validate(assignment);
        assertEquals(1, violations.size());
        assertEquals("Course must not be null", violations.iterator().next().getMessage());
    }

    @Test
    void testPrePersist() {
        assignment.onCreate();
        assertNotNull(assignment.getCreatedAt(), "createdAt should not be null after creation");
        assertNotNull(assignment.getLastUpdate(), "lastUpdate should not be null after creation");
        assertNotNull(assignment.getDeadline(), "Deadline should be set after creation");
        assertTrue(assignment.getDeadline().isAfter(Instant.now()), "Deadline should be a future date");
    }

    @Test
    void testPreUpdate() throws InterruptedException {
        assignment.onCreate();
        LocalDateTime previousUpdate = assignment.getLastUpdate();

        Thread.sleep(10);

        assignment.onUpdate();
        assertNotEquals(previousUpdate, assignment.getLastUpdate(), "lastUpdate should be updated on update call");
    }


    @Test
    void testAddSubmission() {
        Submission submission = mock(Submission.class);
        assignment.getSubmissions().add(submission);
        assertEquals(1, assignment.getSubmissions().size(), "Submissions list should contain the added submission");
    }

    @Test
    void testEmptySubmissions() {
        assignment.setSubmissions(new ArrayList<>());
        assertTrue(assignment.getSubmissions().isEmpty(), "Submissions list should be empty initially");
    }

//    @Test
//    void testImmutableCreatedAt() {
//        assignment.onCreate();
//        LocalDateTime initialCreatedAt = assignment.getCreatedAt();
//        assertEquals(initialCreatedAt, assignment.getCreatedAt(), "createdAt should remain unchanged after creation");
//
//        assertThrows(UnsupportedOperationException.class, () -> {
//        }, "createdAt should not be modifiable after creation");
//    }

    @Test
    void testDeadlineInitialization() {
        assignment.onCreate();
        assertNotNull(assignment.getDeadline(), "Deadline should be initialized on creation");
        Instant expectedDeadline = Instant.now().plus(7, ChronoUnit.DAYS);
        assertTrue(
                assignment.getDeadline().isAfter(expectedDeadline.minus(1, ChronoUnit.SECONDS)) &&
                        assignment.getDeadline().isBefore(expectedDeadline.plus(1, ChronoUnit.SECONDS)),
                "Deadline should be set to one week from now"
        );
    }

    @Test
    void testAssignmentState() {
        assertEquals(AssignmentState.PENDING, assignment.getState(), "Default state should be PENDING");
        assignment.setState(AssignmentState.COMPLETED);
        assertEquals(AssignmentState.COMPLETED, assignment.getState(), "State should be updatable");
    }

    @Test
    void testNullDeadline() {
        assignment.setDeadline(null);
        assertNull(assignment.getDeadline(), "Deadline should be allowed to be null");
    }

    @Test
    void testNonNullSubmissions() {
        List<Submission> submissions = assignment.getSubmissions();
        assertNotNull(submissions, "Submissions list should not be null after instantiation");
    }

    @Test
    void testAssignmentRelationships() {
        Course mockCourse = mock(Course.class);
        assignment.setCourse(mockCourse);
        assertEquals(mockCourse, assignment.getCourse(), "The course relationship should be set correctly");
    }
}