package org.una.programmingIII.UTEMP_Project.service;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.una.programmingIII.UTEMP_Project.exceptions.InvalidDataException;
import org.una.programmingIII.UTEMP_Project.exceptions.ResourceNotFoundException;
import org.una.programmingIII.UTEMP_Project.models.Grade;
import org.una.programmingIII.UTEMP_Project.models.GradeState;
import org.una.programmingIII.UTEMP_Project.repositories.GradeRepository;
import org.una.programmingIII.UTEMP_Project.services.submission.ManualReviewServiceImplementation;

import java.util.Optional;

import static org.mockito.Mockito.*;
import static org.junit.jupiter.api.Assertions.*;

public class ManualReviewServiceImplementationTest {

    @Mock
    private GradeRepository gradeRepository;

    @InjectMocks
    private ManualReviewServiceImplementation manualReviewService;

    private Grade grade;

    @BeforeEach
    public void setUp() {
        MockitoAnnotations.openMocks(this);
        grade = new Grade();
        grade.setId(1L);
        grade.setGrade(8.0);
        grade.setComments("Good work");
        grade.setState(GradeState.PENDING_REVIEW);
    }

    @Test
    public void testManualReviewSubmissionSuccess() {
        Long submissionId = 1L;
        double gradeValue = 9.0;
        String comments = "Well done";

        when(gradeRepository.findBySubmissionId(submissionId)).thenReturn(Optional.of(grade));
        when(gradeRepository.save(any(Grade.class))).thenReturn(grade);

        Optional<Grade> result = manualReviewService.ManualReviewSubmission(submissionId, gradeValue, comments);

        assertTrue(result.isPresent());
        Grade updatedGrade = result.get();
        assertEquals(gradeValue, updatedGrade.getGrade());
        assertEquals(comments, updatedGrade.getComments());
        assertEquals(GradeState.FINALIZED, updatedGrade.getState());

        verify(gradeRepository, times(1)).findBySubmissionId(submissionId);
        verify(gradeRepository, times(1)).save(updatedGrade);
    }

    @Test
    public void testManualReviewSubmissionGradeNotFound() {

        Long submissionId = 2L;
        double gradeValue = 9.0;
        String comments = "Well done";

        when(gradeRepository.findBySubmissionId(submissionId)).thenReturn(Optional.empty());

        ResourceNotFoundException thrown = assertThrows(ResourceNotFoundException.class, () -> manualReviewService.ManualReviewSubmission(submissionId, gradeValue, comments));

        assertEquals("No grade found for submission ID: " + submissionId, thrown.getMessage());

        verify(gradeRepository, times(1)).findBySubmissionId(submissionId);
        verify(gradeRepository, times(0)).save(any(Grade.class));
    }

    @Test
    public void testManualReviewSubmissionInvalidGradeValue() {

        Long submissionId = 1L;
        double gradeValue = 15.0;
        String comments = "Well done";

        InvalidDataException thrown = assertThrows(InvalidDataException.class, () -> manualReviewService.ManualReviewSubmission(submissionId, gradeValue, comments));

        assertEquals("Grade value must be between 0 and 10.", thrown.getMessage());

        verify(gradeRepository, times(0)).findBySubmissionId(submissionId);
        verify(gradeRepository, times(0)).save(any(Grade.class));
    }

    @Test
    public void testManualReviewSubmissionInvalidComments() {

        Long submissionId = 1L;
        double gradeValue = 8.0;
        String comments = "";
        InvalidDataException thrown = assertThrows(InvalidDataException.class, () -> manualReviewService.ManualReviewSubmission(submissionId, gradeValue, comments));

        assertEquals("Comments cannot be null or empty.", thrown.getMessage());

        verify(gradeRepository, times(0)).findBySubmissionId(submissionId);
        verify(gradeRepository, times(0)).save(any(Grade.class));
    }
}
