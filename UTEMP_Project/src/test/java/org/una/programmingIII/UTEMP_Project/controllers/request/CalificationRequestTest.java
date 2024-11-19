package org.una.programmingIII.UTEMP_Project.controllers.request;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.una.programmingIII.UTEMP_Project.controllers.request.CalificationRequest;

import static org.junit.jupiter.api.Assertions.*;

class CalificationRequestTest {

    private CalificationRequest calificationRequest;

    @BeforeEach
    void setUp() {
        calificationRequest = new CalificationRequest();
    }

    @Test
    void testSubmissionIdGetterAndSetter() {
        Long testSubmissionId = 123L;

        calificationRequest.setSubmissionId(testSubmissionId);

        assertEquals(testSubmissionId, calificationRequest.getSubmissionId(), "Submission ID should be set and retrieved correctly.");
    }

    @Test
    void testGradeValueGetterAndSetter() {
        Double testGradeValue = 85.5;

        calificationRequest.setGradeValue(testGradeValue);

        assertEquals(testGradeValue, calificationRequest.getGradeValue(), "Grade value should be set and retrieved correctly.");
    }

    @Test
    void testCommentsGetterAndSetter() {
        String testComments = "Good work, well done!";

        calificationRequest.setComments(testComments);

        assertEquals(testComments, calificationRequest.getComments(), "Comments should be set and retrieved correctly.");
    }

    @Test
    void testNullValues() {
        calificationRequest.setSubmissionId(null);
        calificationRequest.setGradeValue(null);
        calificationRequest.setComments(null);

        assertNull(calificationRequest.getSubmissionId(), "Submission ID should be null.");
        assertNull(calificationRequest.getGradeValue(), "Grade value should be null.");
        assertNull(calificationRequest.getComments(), "Comments should be null.");
    }
}
