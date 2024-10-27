package org.una.programmingIII.UTEMP_Project.services.manualReviewServices;

import org.una.programmingIII.UTEMP_Project.models.Grade;
import java.util.Optional;

public interface ManualReviewService {
    Optional<Grade> ManualReviewSubmission(Long submissionId, double gradeValue, String comments);
}
