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

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class DepartmentTest {

    @InjectMocks
    private Department department;

    @Mock
    private Faculty faculty;

    private Validator validator;

    @BeforeEach
    void setUp() {
        ValidatorFactory factory = Validation.buildDefaultValidatorFactory();
        validator = factory.getValidator();
        department = new Department();
        department.setFaculty(faculty);
        department.setCreatedAt(LocalDateTime.now());
        department.setLastUpdate(LocalDateTime.now());
    }

    @Test
    void testValidDepartment() {
        department.setName("Computer Science");
        department.setFaculty(faculty);

        Set<ConstraintViolation<Department>> violations = validator.validate(department);
        assertTrue(violations.isEmpty(), "No violations should occur for a valid department");
    }

    @Test
    void testNullName() {
        department.setName(null);
        department.setFaculty(faculty);

        Set<ConstraintViolation<Department>> violations = validator.validate(department);
        assertEquals(1, violations.size());
        assertEquals("Department name must not be null", violations.iterator().next().getMessage());
    }

    @Test
    void testNullFaculty() {
        department.setName("Computer Science");
        department.setFaculty(null);

        Set<ConstraintViolation<Department>> violations = validator.validate(department);
        assertEquals(1, violations.size());
        assertEquals("Faculty must not be null", violations.iterator().next().getMessage());
    }

    @Test
    void testPrePersist() {
        department.onCreate();
        assertNotNull(department.getCreatedAt(), "Created at timestamp should not be null");
        assertNotNull(department.getLastUpdate(), "Last update timestamp should not be null");
    }

    @Test
    void testPreUpdate() {
        department.onCreate();
        LocalDateTime previousUpdate = department.getLastUpdate();
        department.onUpdate();
        assertNotEquals(previousUpdate, department.getLastUpdate(), "lastUpdate should be updated on update call");
    }

    @Test
    void testAddCourse() {
        Course course = mock(Course.class);
        department.getCourses().add(course);
        assertEquals(1, department.getCourses().size(), "Course list should contain the added course");
    }

    @Test
    void testEmptyCourses() {
        department.setCourses(new ArrayList<>());
        assertTrue(department.getCourses().isEmpty(), "Courses list should be empty initially");
    }

    @Test
    void testImmutableCreatedAt() {
        department.onCreate();
        LocalDateTime initialCreatedAt = department.getCreatedAt();
        assertThrows(UnsupportedOperationException.class, () -> {
            department.setCreatedAt(LocalDateTime.now()); // Simulate an attempt to modify createdAt
        }, "createdAt should not be modifiable after creation");
        assertEquals(initialCreatedAt, department.getCreatedAt(), "createdAt should remain unchanged");
    }

    @Test
    void testNonNullCourses() {
        List<Course> courses = department.getCourses();
        assertNotNull(courses, "Courses list should not be null after instantiation");
    }

    @Test
    void testNullFacultyWhenAddingCourse() {
        Course course = new Course();
        department.setFaculty(null); // Simulating faculty being null

        assertThrows(NullPointerException.class, () -> {
            department.getCourses().add(course); // Attempt to add course without a faculty
        }, "Should throw NullPointerException when adding a course without faculty");
    }
}
