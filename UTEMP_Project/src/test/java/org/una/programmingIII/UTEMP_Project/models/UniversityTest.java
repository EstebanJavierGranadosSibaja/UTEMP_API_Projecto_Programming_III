package org.una.programmingIII.UTEMP_Project.models;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.time.LocalDateTime;

import static org.assertj.core.api.Assertions.assertThat;

class UniversityTest {

    private University university;

    @BeforeEach
    void setUp() {
        university = University.builder()
                .name("Test University")
                .location("Test Location")
                .build();
    }

    @Test
    void testUniversityInitialization() {
        assertThat(university).isNotNull();
        assertThat(university.getId()).isNull();
        assertThat(university.getName()).isEqualTo("Test University");
        assertThat(university.getLocation()).isEqualTo("Test Location");
    }

    @Test
    void testNameConstraints() {
        university.setName("Valid University Name");
        assertThat(university.getName()).isEqualTo("Valid University Name");


        university.setName("");
        assertThat(university.getName()).isBlank();

        university.setName("A".repeat(101));
        assertThat(university.getName()).hasSize(101);
    }

    @Test
    void testLocationConstraints() {
        university.setLocation("Valid Location");
        assertThat(university.getLocation()).isEqualTo("Valid Location");

        university.setLocation("A".repeat(200));
        assertThat(university.getLocation()).hasSize(200);

        university.setLocation("A".repeat(201));
        assertThat(university.getLocation()).hasSize(201);
    }

    @Test
    void testOnCreate() {
        university.onCreate();
        assertThat(university.getCreatedAt()).isNotNull();
        assertThat(university.getLastUpdate()).isNotNull();
        assertThat(university.getCreatedAt()).isEqualTo(university.getLastUpdate());
    }

    @Test
    void testOnUpdate() {
        university.onCreate();
        LocalDateTime initialLastUpdate = university.getLastUpdate();


        university.onUpdate();
        assertThat(university.getLastUpdate()).isAfter(initialLastUpdate);
        assertThat(university.getCreatedAt()).isNotNull();
    }

    @Test
    void testSetAndGetFaculties() {
        Faculty faculty = new Faculty();
        university.getFaculties().add(faculty);
        assertThat(university.getFaculties()).contains(faculty);
    }

    @Test
    void testIdSetterAndGetter() {
        Long mockId = 1L;
        university.setId(mockId);
        assertThat(university.getId()).isEqualTo(mockId);
    }
}
