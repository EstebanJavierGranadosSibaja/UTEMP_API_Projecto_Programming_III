package org.una.programmingIII.UTEMP_Project.models;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.time.LocalDateTime;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

class SubmissionTest {

    private Submission submission;

    @BeforeEach
    void setUp() {
        submission = Submission.builder()
                .assignment(new Assignment())
                .student(new User())
                .fileName("test_file.txt")
                .state(SubmissionState.SUBMITTED)
                .build();
    }

    @Test
    void testSubmissionInitialization() {
        assertThat(submission).isNotNull();
        assertThat(submission.getId()).isNull();
        assertThat(submission.getFileName()).isEqualTo("test_file.txt");
        assertThat(submission.getState()).isEqualTo(SubmissionState.SUBMITTED);
    }

    @Test
    void testFileNameConstraints() {
        submission.setFileName("ShortFileName");
        assertThat(submission.getFileName()).isEqualTo("ShortFileName");

        submission.setFileName("A".repeat(255));
        assertThat(submission.getFileName()).hasSize(255);

        submission.setFileName("");
        assertThat(submission.getFileName()).isBlank();

        submission.setFileName(null);
        assertThat(submission.getFileName()).isNull();
    }

    @Test
    void testCommentsConstraints() {
        submission.setComments("This is a test comment.");
        assertThat(submission.getComments()).isEqualTo("This is a test comment.");

        submission.setComments("A".repeat(500));
        assertThat(submission.getComments()).hasSize(500);

        submission.setComments("A".repeat(501));
        assertThat(submission.getComments()).hasSize(501);
    }

    @Test
    void testStateDefault() {
        Submission newSubmission = new Submission();
        assertThat(newSubmission.getState()).isNull();
    }

    @Test
    void testOnCreate() {
        submission.onCreate();
        assertThat(submission.getCreatedAt()).isNotNull();
        assertThat(submission.getLastUpdate()).isNotNull();
        assertThat(submission.getCreatedAt()).isEqualTo(submission.getLastUpdate());
    }

    @Test
    void testOnUpdate() {
        submission.onCreate();
        LocalDateTime initialLastUpdate = submission.getLastUpdate();


        submission.onUpdate();
        assertThat(submission.getLastUpdate()).isAfter(initialLastUpdate);
        assertThat(submission.getCreatedAt()).isNotNull();
    }

    @Test
    void testSetAndGetState() {
        submission.setState(SubmissionState.GRADED);
        assertThat(submission.getState()).isEqualTo(SubmissionState.GRADED);

        submission.setState(SubmissionState.REVISED);
        assertThat(submission.getState()).isEqualTo(SubmissionState.REVISED);
    }

    @Test
    void testSetAndGetAssignment() {
        Assignment assignment = new Assignment();
        submission.setAssignment(assignment);
        assertThat(submission.getAssignment()).isEqualTo(assignment);
    }

    @Test
    void testSetAndGetStudent() {
        User student = new User();
        submission.setStudent(student);
        assertThat(submission.getStudent()).isEqualTo(student);
    }

    @Test
    void testFileMetadata() {
        FileMetadatum fileMetadatum = new FileMetadatum();
        submission.getFileMetadata().add(fileMetadatum);
        assertThat(submission.getFileMetadata()).contains(fileMetadatum);
    }

    @Test
    void testGrades() {
        Grade grade = new Grade();
        submission.getGrades().add(grade);
        assertThat(submission.getGrades()).contains(grade);
    }

    @Test
    void testIdSetterAndGetter() {
        Long mockId = 1L;
        submission.setId(mockId);
        assertThat(submission.getId()).isEqualTo(mockId);
    }
}
