package org.una.programmingIII.UTEMP_Project.services.autoReviewServices;

import org.una.programmingIII.UTEMP_Project.models.Grade;
import java.util.concurrent.CompletableFuture;

public interface AutoReviewService {
    CompletableFuture<Grade> autoReview(Long submissionId);
}
