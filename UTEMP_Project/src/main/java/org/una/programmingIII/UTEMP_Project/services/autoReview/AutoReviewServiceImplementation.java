package org.una.programmingIII.UTEMP_Project.services.autoReview;

import jakarta.mail.MessagingException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;
import org.una.programmingIII.UTEMP_Project.models.Grade;
import org.una.programmingIII.UTEMP_Project.models.GradeState;
import org.una.programmingIII.UTEMP_Project.models.Submission;
import org.una.programmingIII.UTEMP_Project.exceptions.InvalidDataException;
import org.una.programmingIII.UTEMP_Project.exceptions.ResourceNotFoundException;
import io.krakens.grok.api.Grok;
import io.krakens.grok.api.GrokCompiler;
import io.krakens.grok.api.Match;
import org.una.programmingIII.UTEMP_Project.observers.Subject;
import org.una.programmingIII.UTEMP_Project.repositories.SubmissionRepository;
import org.una.programmingIII.UTEMP_Project.services.EmailNotificationObserver;

import java.util.HashMap;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ConcurrentHashMap;

@Service
public class AutoReviewServiceImplementation extends Subject<EmailNotificationObserver> implements AutoReviewService  {

    private static final Logger logger = LoggerFactory.getLogger(AutoReviewServiceImplementation.class);

    private static final String COMMENT_PATTERN = "%{NUMBER:grade} : %{GREEDYDATA:message}";
    private static final double MAX_GRADE = 10.0;
    private static final double MIN_GRADE = 0.0;

    private final Map<String, Double> fileGrades = new ConcurrentHashMap<>();
    private final Map<String, String> fileComments = new ConcurrentHashMap<>();

    private static final Map<Double, String> commentsMap = new HashMap<>() {{
        put(0.0, "You have much to improve in your work.");
        put(1.0, "You need to try harder.");
        put(2.0, "The work is below expectations.");
        put(3.0, "You need to improve several aspects.");
        put(4.0, "Acceptable work but with a lot of room for improvement.");
        put(5.0, "Average work.");
        put(6.0, "Good work, but there are areas to improve.");
        put(7.0, "Good work, almost excellent.");
        put(8.0, "Very good work.");
        put(9.0, "Excellent work.");
        put(10.0, "Excellent, very good work.");
    }};

    private final SubmissionRepository submissionRepository;
    private final Grok grok;

    public AutoReviewServiceImplementation(
            SubmissionRepository submissionRepository) {

        this.submissionRepository = submissionRepository;
        this.grok = initializeGrok();
    }

    private Grok initializeGrok() {
        GrokCompiler grokCompiler = GrokCompiler.newInstance();
        grokCompiler.register("NUMBER", "(\\d+(\\.\\d+)?)");
        grokCompiler.register("GREEDYDATA", "(.*)");
        return grokCompiler.compile(COMMENT_PATTERN);
    }

    @Async("taskExecutor")
    public CompletableFuture<Grade> autoReviewSubmission(Long submissionId) {
        return CompletableFuture.supplyAsync(() -> {
            try {
                Submission submission = validateSubmission(submissionId);
                String fileId = submission.getFileMetadata().getFirst().getId().toString();

                double grade = fileGrades.computeIfAbsent(fileId, this::generateGrade);
                String comment = fileComments.computeIfAbsent(fileId, id -> generateComment(grade));

                return createGradeEntity(submission, grade, comment);
            } catch (InvalidDataException e) {
                logger.error("Invalid data error: {}", e.getMessage());
                throw e;
            } catch (ResourceNotFoundException e) {
                logger.error("Resource not found: {}", e.getMessage());
                throw e;
            } catch (Exception e) {
                logger.error("Error processing auto review: {}", e.getMessage(), e);
                throw new RuntimeException("Failed to process auto review due to an unexpected error.");
            }
        });
    }

    private double generateGrade(String fileId) {
        return Math.random() * (MAX_GRADE - MIN_GRADE) + MIN_GRADE;
    }

    private String generateComment(double grade) {
        if (grade < 0) {
            throw new InvalidDataException("Grade cannot be negative.");
        }

        String message = commentsMap.getOrDefault(grade, "Comment not found.");
        String logMessage = grade + " : " + message;

        if ("Comment not found.".equals(message)) {
            logger.warn("No comment found for grade: {}", grade);
        }
        return extractMessageWithGrok(logMessage);
    }

    private String extractMessageWithGrok(String logMessage) {
        Match match = grok.match(logMessage);

        if (match != null && !match.capture().isEmpty()) {
            return (String) match.capture().getOrDefault("message", "Message not captured.");
        } else {
            logger.warn("No match found for log message: {}", logMessage);
            return "Message not captured.";
        }
    }

    private Grade createGradeEntity(Submission submission, double grade, String comment) {
        Grade gradeEntity = new Grade();
        gradeEntity.setSubmission(submission);
        gradeEntity.setGrade(grade);
        gradeEntity.setComments(comment);
        gradeEntity.setReviewedByAi(true);
        gradeEntity.setState(GradeState.PENDING_REVIEW);
        return gradeEntity;
    }

    private Submission validateSubmission(Long submissionId) {
        if (submissionId == null || submissionId <= 0) {
            throw new InvalidDataException("Submission ID must be a positive number.");
        }

        return submissionRepository.findById(submissionId)
                .orElseThrow(() -> new ResourceNotFoundException("Submission with ID " + submissionId + " not found.", submissionId));
    }


    @Async("taskExecutor")
    protected void sendNotificationForGrade(Grade grade, Submission submission) {
        String message = "The grade of the assignment '" +
                submission.getAssignment().getTitle() + "' was " + grade.getGrade();
        try {
            notifyObservers("SUBMISSION_GRADED", message, submission.getStudent().getEmail());
            // notificationService.sendNotificationToUser(submission.getStudent(), message);

        } catch (Exception e) {
            logger.error("Error notifying student {}: {}", submission.getStudent().getEmail(), e.getMessage(), e);
        }
    }

}