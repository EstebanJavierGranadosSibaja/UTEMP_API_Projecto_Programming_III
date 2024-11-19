package org.una.programmingIII.UTEMP_Project.models;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.time.LocalDateTime;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

class FacultyTest {

    private Faculty faculty;

    @BeforeEach
    void setUp() {
        faculty = Faculty.builder()
                .name("Engineering Faculty")
                .university(new University())
                .departments(List.of(new Department(), new Department()))
                .build();
    }

    @Test
    void testFacultyInitialization() {
        assertThat(faculty).isNotNull();
        assertThat(faculty.getId()).isNull();
        assertThat(faculty.getName()).isEqualTo("Engineering Faculty");
        assertThat(faculty.getUniversity()).isNotNull();
        assertThat(faculty.getDepartments()).hasSize(2);
    }

    @Test
    void testFacultyNameConstraints() {
        faculty.setName("Valid Name");
        assertThat(faculty.getName()).isEqualTo("Valid Name");

        faculty.setName("A".repeat(50));
        assertThat(faculty.getName()).hasSize(50);

        faculty.setName(null);
        assertThat(faculty.getName()).isNull();
    }

    @Test
    void testFacultyUniversityNotNull() {
        faculty.setUniversity(null);
        assertThat(faculty.getUniversity()).isNull();
    }

    @Test
    void testDepartmentsList() {
        assertThat(faculty.getDepartments()).isNotNull();
        assertThat(faculty.getDepartments()).hasSize(2);

        faculty.getDepartments().add(new Department());
        assertThat(faculty.getDepartments()).hasSize(3);
    }

    @Test
    void testCreatedAtAndLastUpdateOnCreate() {
        faculty.onCreate();
        assertThat(faculty.getCreatedAt()).isNotNull();
        assertThat(faculty.getLastUpdate()).isNotNull();
        assertThat(faculty.getCreatedAt()).isEqualTo(faculty.getLastUpdate());
    }

    @Test
    void testLastUpdateOnUpdate() {
        faculty.onCreate();
        LocalDateTime initialLastUpdate = faculty.getLastUpdate();


        faculty.onUpdate();
        assertThat(faculty.getLastUpdate()).isAfter(initialLastUpdate);
        assertThat(faculty.getCreatedAt()).isNotNull();
    }

    @Test
    void testIdSetterAndGetter() {
        Long mockId = 1L;
        faculty.setId(mockId);
        assertThat(faculty.getId()).isEqualTo(mockId);
    }

    @Test
    void testEmptyDepartmentsListByDefault() {
        Faculty newFaculty = new Faculty();
        assertThat(newFaculty.getDepartments()).isNotNull();
        assertThat(newFaculty.getDepartments()).isEmpty();
    }
}
