package org.una.programmingIII.UTEMP_Project.controllers;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.ExampleObject;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import jakarta.validation.Valid;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import org.una.programmingIII.UTEMP_Project.dtos.EnrollmentDTO;
import org.una.programmingIII.UTEMP_Project.exceptions.InvalidDataException;
import org.una.programmingIII.UTEMP_Project.exceptions.ResourceNotFoundException;
import org.una.programmingIII.UTEMP_Project.services.enrollment.EnrollmentService;

import java.util.Optional;

@RestController
@RequestMapping("/utemp/enrollments")
public class EnrollmentController {

    private static final Logger logger = LoggerFactory.getLogger(EnrollmentController.class);
    private final EnrollmentService enrollmentService;

    @Autowired
    public EnrollmentController(EnrollmentService enrollmentService) {
        this.enrollmentService = enrollmentService;
    }

    @Operation(
            summary = "Fetch all enrollments",
            description = """
                    Returns a paginated list of all enrollments.
                    This endpoint retrieves all enrollment records available in the system, supporting pagination
                    for efficient data management and display. The response includes metadata about the pagination
                    such as total pages, current page, and number of items per page.
                    """
    )
    @ApiResponses(value = {
            @ApiResponse(
                    responseCode = "200",
                    description = "Enrollments fetched successfully",
                    content = @Content(
                            mediaType = "application/json",
                            schema = @Schema(implementation = EnrollmentDTO.class)
                    )
            ),
            @ApiResponse(
                    responseCode = "500",
                    description = "Internal server error",
                    content = @Content(
                            mediaType = "application/json",
                            examples = @ExampleObject(
                                    value = """
                                            {
                                              "message": "Internal server error",
                                              "details": "An unexpected error occurred while processing the request."
                                            }
                                            """
                            )
                    )
            )
    })
    @GetMapping
    @PreAuthorize("hasAuthority('MANAGE_ENROLLMENTS')")
    public ResponseEntity<Page<EnrollmentDTO>> getAllEnrollments(Pageable pageable) {
        try {
            Page<EnrollmentDTO> enrollments = enrollmentService.getAllEnrollments(pageable);
            return ResponseEntity.ok(enrollments);
        } catch (Exception e) {
            logger.error("Error retrieving enrollments: {}", e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(null);
        }
    }

    @Operation(
            summary = "Fetch enrollments by course ID",
            description = """
                    Returns a paginated list of enrollments for a specific course.
                    This endpoint retrieves all enrollment records associated with a specific course identified by its ID.
                    The results are returned in a paginated format, allowing for efficient data handling and display.
                    """
    )
    @ApiResponses(value = {
            @ApiResponse(
                    responseCode = "200",
                    description = "Enrollments for course fetched successfully",
                    content = @Content(
                            mediaType = "application/json",
                            schema = @Schema(implementation = Page.class) // Ajusta el esquema para que sea una lista paginada de EnrollmentDTO
                    )
            ),
            @ApiResponse(
                    responseCode = "404",
                    description = "Course not found",
                    content = @Content(
                            mediaType = "application/json",
                            examples = @ExampleObject(
                                    value = """
                                            {
                                              "message": "Course not found",
                                              "details": "No course record exists with the provided ID."
                                            }
                                            """
                            )
                    )
            ),
            @ApiResponse(
                    responseCode = "500",
                    description = "Internal server error",
                    content = @Content(
                            mediaType = "application/json",
                            examples = @ExampleObject(
                                    value = """
                                            {
                                              "message": "Internal server error",
                                              "details": "An unexpected error occurred while processing the request."
                                            }
                                            """
                            )
                    )
            )
    })
    @GetMapping("/course/{courseId}")
    @PreAuthorize("hasAuthority('MANAGE_ENROLLMENTS')")
    public ResponseEntity<Page<EnrollmentDTO>> getEnrollmentsByCourseId(
            @Parameter(description = "ID of the course to retrieve enrollments for") @PathVariable Long courseId,
            Pageable pageable) {
        try {
            Page<EnrollmentDTO> enrollments = enrollmentService.getEnrollmentsByCourseId(courseId, pageable);
            return ResponseEntity.ok(enrollments);
        } catch (ResourceNotFoundException e) {
            logger.warn("Course not found with ID: {}", courseId);
            return ResponseEntity.status(HttpStatus.NOT_FOUND).build();
        } catch (Exception e) {
            logger.error("Error retrieving enrollments for course {}: {}", courseId, e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(null);
        }
    }

    @Operation(
            summary = "Fetch enrollments by student ID",
            description = """
                    Returns a paginated list of enrollments for a specific student.
                    This endpoint allows you to retrieve all the enrollment records associated with a specific student identified by their ID.
                    The results are returned in a paginated format to facilitate efficient data retrieval.
                    """
    )
    @ApiResponses(value = {
            @ApiResponse(
                    responseCode = "200",
                    description = "Enrollments for student fetched successfully",
                    content = @Content(
                            mediaType = "application/json",
                            schema = @Schema(implementation = Page.class) // Define que se devuelve una página de EnrollmentDTO
                    )
            ),
            @ApiResponse(
                    responseCode = "404",
                    description = "Student not found",
                    content = @Content(
                            mediaType = "application/json",
                            examples = @ExampleObject(
                                    value = """
                                            {
                                              "message": "Student not found",
                                              "details": "No student record exists with the provided ID."
                                            }
                                            """
                            )
                    )
            ),
            @ApiResponse(
                    responseCode = "500",
                    description = "Internal server error",
                    content = @Content(
                            mediaType = "application/json",
                            examples = @ExampleObject(
                                    value = """
                                            {
                                              "message": "Internal server error",
                                              "details": "An unexpected error occurred while processing the request."
                                            }
                                            """
                            )
                    )
            )
    })
    @GetMapping("/student/{studentId}")
    @PreAuthorize("hasAuthority('GET_STUDENT_ENROLLMENTS')")
    public ResponseEntity<Page<EnrollmentDTO>> getEnrollmentsByStudentId(
            @Parameter(description = "ID of the student to retrieve enrollments for") @PathVariable Long studentId,
            Pageable pageable) {
        try {
            Page<EnrollmentDTO> enrollments = enrollmentService.getEnrollmentsByStudentId(studentId, pageable);
            return ResponseEntity.ok(enrollments);
        } catch (ResourceNotFoundException e) {
            logger.warn("Student not found with ID: {}", studentId);
            return ResponseEntity.status(HttpStatus.NOT_FOUND).build();
        } catch (Exception e) {
            logger.error("Error retrieving enrollments for student {}: {}", studentId, e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(null);
        }
    }

    @Operation(
            summary = "Fetch enrollment by ID",
            description = """
                    Returns an enrollment by its ID.
                    This endpoint retrieves the details of a specific enrollment record based on the provided ID.
                    If the enrollment exists, it returns the enrollment data; otherwise, it returns a not found status.
                    """
    )
    @ApiResponses(value = {
            @ApiResponse(
                    responseCode = "200",
                    description = "Enrollment found",
                    content = @Content(
                            mediaType = "application/json",
                            schema = @Schema(implementation = EnrollmentDTO.class) // Define que el objeto de retorno es un EnrollmentDTO
                    )
            ),
            @ApiResponse(
                    responseCode = "404",
                    description = "Enrollment not found",
                    content = @Content(
                            mediaType = "application/json",
                            examples = @ExampleObject(
                                    value = """
                                            {
                                              "message": "Enrollment not found",
                                              "details": "No enrollment record exists with the provided ID."
                                            }
                                            """
                            )
                    )
            ),
            @ApiResponse(
                    responseCode = "500",
                    description = "Internal server error",
                    content = @Content(
                            mediaType = "application/json",
                            examples = @ExampleObject(
                                    value = """
                                            {
                                              "message": "Internal server error",
                                              "details": "An unexpected error occurred while processing the request."
                                            }
                                            """
                            )
                    )
            )
    })
    @GetMapping("/{id}")
    @PreAuthorize("hasAuthority('MANAGE_ENROLLMENTS')")
    public ResponseEntity<EnrollmentDTO> getEnrollmentById(
            @Parameter(description = "ID of the enrollment to be fetched") @PathVariable Long id) {
        try {
            Optional<EnrollmentDTO> enrollmentDTO = enrollmentService.getEnrollmentById(id);
            return enrollmentDTO.map(ResponseEntity::ok)
                    .orElseGet(() -> {
                        logger.warn("Enrollment not found with id: {}", id);
                        return ResponseEntity.status(HttpStatus.NOT_FOUND).build();
                    });
        } catch (Exception e) {
            logger.error("Error retrieving enrollment with id {}: {}", id, e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }

    @Operation(
            summary = "Create a new enrollment",
            description = """
                    Creates a new enrollment and returns the created enrollment.
                    This endpoint allows clients to create a new enrollment record in the system.
                    Upon successful creation, the endpoint returns the details of the newly created enrollment in the response body.
                    """
    )
    @ApiResponses(value = {
            @ApiResponse(
                    responseCode = "201",
                    description = "Enrollment created successfully",
                    content = @Content(
                            mediaType = "application/json",
                            schema = @Schema(implementation = EnrollmentDTO.class) // Retorna el EnrollmentDTO recién creado
                    )
            ),
            @ApiResponse(
                    responseCode = "400",
                    description = "Invalid data for creating enrollment",
                    content = @Content(
                            mediaType = "application/json",
                            examples = @ExampleObject(
                                    value = """
                                            {
                                              "message": "Invalid data",
                                              "details": "The provided enrollment data is not valid."
                                            }
                                            """
                            )
                    )
            ),
            @ApiResponse(
                    responseCode = "500",
                    description = "Internal server error",
                    content = @Content(
                            mediaType = "application/json",
                            examples = @ExampleObject(
                                    value = """
                                            {
                                              "message": "Internal server error",
                                              "details": "An unexpected error occurred while processing the request."
                                            }
                                            """
                            )
                    )
            )
    })
    @PostMapping
    @PreAuthorize("hasAuthority('MANAGE_ENROLLMENTS')")
    public ResponseEntity<EnrollmentDTO> createEnrollment(@Valid @RequestBody EnrollmentDTO enrollmentDTO) {
        try {
            EnrollmentDTO createdEnrollment = enrollmentService.createEnrollment(enrollmentDTO);
            return ResponseEntity.status(HttpStatus.CREATED).body(createdEnrollment);
        } catch (InvalidDataException e) {
            logger.error("Invalid data while creating enrollment: {}", e.getMessage());
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).build();
        } catch (Exception e) {
            logger.error("Error creating enrollment: {}", e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }

    @Operation(
            summary = "Update an existing enrollment",
            description = """
                    Updates an existing enrollment by its ID.
                    This endpoint allows clients to modify the details of a specific enrollment record.
                    A successful update will return the updated enrollment information in the response body.
                    If the enrollment with the specified ID is not found, a 404 Not Found response is returned.
                    """
    )
    @ApiResponses(value = {
            @ApiResponse(
                    responseCode = "200",
                    description = "Enrollment updated successfully",
                    content = @Content(
                            mediaType = "application/json",
                            schema = @Schema(implementation = EnrollmentDTO.class) // Retorna el objeto actualizado
                    )
            ),
            @ApiResponse(
                    responseCode = "404",
                    description = "Enrollment not found",
                    content = @Content(
                            mediaType = "application/json",
                            examples = @ExampleObject(
                                    value = """
                                            {
                                              "message": "Enrollment not found",
                                              "details": "No enrollment record exists with the specified ID."
                                            }
                                            """
                            )
                    )
            ),
            @ApiResponse(
                    responseCode = "400",
                    description = "Invalid data for updating enrollment",
                    content = @Content(
                            mediaType = "application/json",
                            examples = @ExampleObject(
                                    value = """
                                            {
                                              "message": "Invalid data",
                                              "details": "The provided enrollment data is not valid."
                                            }
                                            """
                            )
                    )
            ),
            @ApiResponse(
                    responseCode = "500",
                    description = "Internal server error",
                    content = @Content(
                            mediaType = "application/json",
                            examples = @ExampleObject(
                                    value = """
                                            {
                                              "message": "Internal server error",
                                              "details": "An unexpected error occurred while processing the request."
                                            }
                                            """
                            )
                    )
            )
    })
    @PutMapping("/{id}")
    @PreAuthorize("hasAuthority('MANAGE_ENROLLMENTS')")
    public ResponseEntity<EnrollmentDTO> updateEnrollment(
            @Parameter(description = "ID of the enrollment to update") @PathVariable Long id,
            @Valid @RequestBody EnrollmentDTO enrollmentDTO) {
        try {
            Optional<EnrollmentDTO> updatedEnrollment = enrollmentService.updateEnrollment(id, enrollmentDTO);
            return updatedEnrollment.map(ResponseEntity::ok)
                    .orElseGet(() -> {
                        logger.warn("Enrollment not found for update with id: {}", id);
                        return ResponseEntity.status(HttpStatus.NOT_FOUND).build();
                    });
        } catch (InvalidDataException e) {
            logger.error("Invalid data while updating enrollment: {}", e.getMessage());
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).build();
        } catch (Exception e) {
            logger.error("Error updating enrollment with id {}: {}", id, e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }

    @Operation(
            summary = "Delete an enrollment",
            description = """
                    Deletes an enrollment by its ID.
                    This endpoint allows clients to delete a specific enrollment record from the system.
                    Upon successful deletion, a 204 No Content response is returned, indicating that the operation
                    was successful and no further information is returned in the response body.
                    """
    )
    @ApiResponses(value = {
            @ApiResponse(
                    responseCode = "204",
                    description = "Enrollment deleted successfully",
                    content = @Content(
                            mediaType = "application/json"
                    )
            ),
            @ApiResponse(
                    responseCode = "404",
                    description = "Enrollment not found",
                    content = @Content(
                            mediaType = "application/json",
                            examples = @ExampleObject(
                                    value = """
                                            {
                                              "message": "Enrollment not found",
                                              "details": "No enrollment record exists with the specified ID."
                                            }
                                            """
                            )
                    )
            ),
            @ApiResponse(
                    responseCode = "500",
                    description = "Internal server error",
                    content = @Content(
                            mediaType = "application/json",
                            examples = @ExampleObject(
                                    value = """
                                            {
                                              "message": "Internal server error",
                                              "details": "An unexpected error occurred while processing the request."
                                            }
                                            """
                            )
                    )
            )
    })
    @DeleteMapping("/{id}")
    @PreAuthorize("hasAuthority('MANAGE_ENROLLMENTS')")
    public ResponseEntity<Void> deleteEnrollment(
            @Parameter(description = "ID of the enrollment to delete") @PathVariable Long id) {
        try {
            enrollmentService.deleteEnrollment(id);
            return ResponseEntity.noContent().build();
        } catch (ResourceNotFoundException e) {
            logger.warn("Enrollment not found for deletion with id: {}", id);
            return ResponseEntity.status(HttpStatus.NOT_FOUND).build();
        } catch (Exception e) {
            logger.error("Error deleting enrollment with id {}: {}", id, e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }
}