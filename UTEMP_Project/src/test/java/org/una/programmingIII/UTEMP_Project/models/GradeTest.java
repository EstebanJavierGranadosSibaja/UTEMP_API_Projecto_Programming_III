package org.una.programmingIII.UTEMP_Project.models;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.time.LocalDateTime;

import static org.assertj.core.api.Assertions.assertThat;

class GradeTest {

    private Grade grade;

    @BeforeEach
    void setUp() {
        grade = Grade.builder()
                .submission(new Submission())
                .grade(85.0)
                .comments("Well done!")
                .reviewedByAi(false)
                .state(GradeState.FINALIZED)
                .build();
    }

    @Test
    void testGradeInitialization() {
        assertThat(grade).isNotNull();
        assertThat(grade.getId()).isNull();
        assertThat(grade.getSubmission()).isNotNull();
        assertThat(grade.getGrade()).isEqualTo(85.0);
        assertThat(grade.getComments()).isEqualTo("Well done!");
        assertThat(grade.getReviewedByAi()).isFalse();
        assertThat(grade.getState()).isEqualTo(GradeState.FINALIZED);
    }

    @Test
    void testCommentsConstraints() {
        grade.setComments("This is a valid comment.");
        assertThat(grade.getComments()).isEqualTo("This is a valid comment.");

        grade.setComments("A".repeat(1000));
        assertThat(grade.getComments()).hasSize(1000);

        grade.setComments(null);
        assertThat(grade.getComments()).isNull();
    }

    @Test
    void testReviewedByAiDefaults() {
        Grade newGrade = new Grade();
        assertThat(newGrade.getReviewedByAi()).isNull();
    }

    @Test
    void testStateDefault() {
        Grade newGrade = new Grade();
        assertThat(newGrade.getState()).isNull();
    }

    @Test
    void testGradeValue() {
        grade.setGrade(95.5);
        assertThat(grade.getGrade()).isEqualTo(95.5);

        grade.setGrade(null);
        assertThat(grade.getGrade()).isNull();
    }

    @Test
    void testOnCreate() {
        grade.onCreate();
        assertThat(grade.getCreatedAt()).isNotNull();
        assertThat(grade.getLastUpdate()).isNotNull();
        assertThat(grade.getCreatedAt()).isEqualTo(grade.getLastUpdate());
    }

    @Test
    void testOnUpdate() {
        grade.onCreate();
        LocalDateTime initialLastUpdate = grade.getLastUpdate();


        grade.onUpdate();
        assertThat(grade.getLastUpdate()).isAfter(initialLastUpdate);
        assertThat(grade.getCreatedAt()).isNotNull();
    }

    @Test
    void testSetAndGetState() {
        grade.setState(GradeState.PENDING_REVIEW);
        assertThat(grade.getState()).isEqualTo(GradeState.PENDING_REVIEW);

        grade.setState(GradeState.FINALIZED);
        assertThat(grade.getState()).isEqualTo(GradeState.FINALIZED);
    }

    @Test
    void testSetAndGetSubmission() {
        Submission submission = new Submission();
        grade.setSubmission(submission);
        assertThat(grade.getSubmission()).isEqualTo(submission);
    }

    @Test
    void testIdSetterAndGetter() {
        Long mockId = 1L;
        grade.setId(mockId);
        assertThat(grade.getId()).isEqualTo(mockId);
    }
}
