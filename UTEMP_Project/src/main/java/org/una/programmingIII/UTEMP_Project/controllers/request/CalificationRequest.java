package org.una.programmingIII.UTEMP_Project.controllers.request;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class CalificationRequest {
    Long submissionId;
    Double gradeValue;
    String comments;
}
