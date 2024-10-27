package org.una.programmingIII.UTEMP_Project.services.autoReview;

import org.una.programmingIII.UTEMP_Project.models.Grade;
import java.util.concurrent.CompletableFuture;

public interface AutoReviewService {
    CompletableFuture<Grade> autoReviewSubmission(Long submissionId);
}
