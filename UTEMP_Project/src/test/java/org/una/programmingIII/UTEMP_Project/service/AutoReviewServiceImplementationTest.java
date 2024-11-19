package org.una.programmingIII.UTEMP_Project.service;

import static org.mockito.Mockito.*;
import static org.junit.jupiter.api.Assertions.*;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.*;
import org.una.programmingIII.UTEMP_Project.exceptions.InvalidDataException;
import org.una.programmingIII.UTEMP_Project.exceptions.ResourceNotFoundException;
import org.una.programmingIII.UTEMP_Project.models.Submission;
import org.una.programmingIII.UTEMP_Project.repositories.SubmissionRepository;
import org.una.programmingIII.UTEMP_Project.services.autoReview.AutoReviewServiceImplementation;

import java.util.Optional;

public class AutoReviewServiceImplementationTest {

    @Mock
    private SubmissionRepository submissionRepository;

    @InjectMocks
    private AutoReviewServiceImplementation autoReviewService;

    private Submission submission;

    @BeforeEach
    public void setUp() {
        MockitoAnnotations.openMocks(this);

        submission = mock(Submission.class);
        when(submission.getId()).thenReturn(1L);
    }

    @Test
    public void testAutoReviewSubmission_ValidSubmission() {

        when(submissionRepository.findById(1L)).thenReturn(Optional.of(submission));

        autoReviewService.autoReviewSubmission(1L);

        verify(submissionRepository).findById(1L);
    }

    @Test
    public void testAutoReviewSubmission_InvalidSubmissionId() {

        when(submissionRepository.findById(1L)).thenReturn(Optional.empty());

        assertThrows(ResourceNotFoundException.class, () -> autoReviewService.autoReviewSubmission(1L));
    }

    @Test
    public void testGenerateGrade_ValidRange() {

        autoReviewService.autoReviewSubmission(1L);
    }

    @Test
    public void testGenerateComment_ValidGrade() {

        autoReviewService.autoReviewSubmission(1L);
    }

    @Test
    public void testGenerateComment_InvalidGrade() {

        assertThrows(InvalidDataException.class, () -> autoReviewService.autoReviewSubmission(-1L));
    }


    @Test
    public void testGenerateComment_MessageNotFound() {

        autoReviewService.autoReviewSubmission(11L);
    }
}
