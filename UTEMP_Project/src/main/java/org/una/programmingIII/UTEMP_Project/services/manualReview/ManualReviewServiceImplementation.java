package org.una.programmingIII.UTEMP_Project.services.manualReview;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.una.programmingIII.UTEMP_Project.models.Grade;
import org.una.programmingIII.UTEMP_Project.models.GradeState;
import org.una.programmingIII.UTEMP_Project.repositories.GradeRepository;
import org.una.programmingIII.UTEMP_Project.exceptions.InvalidDataException;
import org.una.programmingIII.UTEMP_Project.exceptions.ResourceNotFoundException;

import java.util.Optional;

@Service
public class ManualReviewServiceImplementation implements ManualReviewService {

    private static final Logger logger = LoggerFactory.getLogger(ManualReviewServiceImplementation.class);

    private final GradeRepository gradeRepository;

    @Autowired
    public ManualReviewServiceImplementation(
            GradeRepository gradeRepository) {

        this.gradeRepository = gradeRepository;
    }

    @Override
    public Optional<Grade> ManualReviewSubmission(Long submissionId, double gradeValue, String comments) {
        validateGradeValue(gradeValue);
        validateComments(comments);

        Optional<Grade> gradeOptional = gradeRepository.findBySubmissionId(submissionId);

        if (gradeOptional.isPresent()) {
            Grade grade = gradeOptional.get();
            grade.setGrade(gradeValue);
            grade.setComments(comments);
            grade.setReviewedByAi(false);
            grade.setState(GradeState.FINALIZED);
            gradeRepository.save(grade);

            logger.info("Submission reviewed successfully: Submission ID = {}, Grade = {}, Comments = {}", submissionId, gradeValue, comments);
            return Optional.of(grade);
        } else {
            logger.error("Grade not found for submission ID: {}", submissionId);
            throw new ResourceNotFoundException("No grade found for submission ID: " + submissionId, submissionId);
        }
    }

    private void validateGradeValue(double gradeValue) {
        if (gradeValue < 0 || gradeValue > 10) {
            throw new InvalidDataException("Grade value must be between 0 and 10.");
        }
    }

    private void validateComments(String comments) {
        if (comments == null || comments.trim().isEmpty()) {
            throw new InvalidDataException("Comments cannot be null or empty.");
        }
    }
}