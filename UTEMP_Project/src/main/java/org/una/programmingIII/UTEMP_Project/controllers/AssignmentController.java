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
import org.una.programmingIII.UTEMP_Project.dtos.AssignmentDTO;
import org.una.programmingIII.UTEMP_Project.dtos.SubmissionDTO;
import org.una.programmingIII.UTEMP_Project.exceptions.InvalidDataException;
import org.una.programmingIII.UTEMP_Project.exceptions.ResourceNotFoundException;
import org.una.programmingIII.UTEMP_Project.services.assignment.AssignmentService;

import java.util.Optional;

@RestController
@RequestMapping("/utemp/assignments")
public class AssignmentController {

    private static final Logger logger = LoggerFactory.getLogger(AssignmentController.class);
    private final AssignmentService assignmentService;

    @Autowired
    public AssignmentController(AssignmentService assignmentService) {
        this.assignmentService = assignmentService;
    }

    @Operation(
            summary = "Retrieve all assignments",
            description = """
                    Fetches a paginated list of all assignments in the system.
                    This endpoint allows clients to retrieve a comprehensive
                    overview of all assignments available, with support for
                    pagination. The client can specify page size and number
                    through the Pageable parameter. A successful response will
                    return a 200 OK status along with a Page object containing
                    a list of AssignmentDTO items. In case of invalid data or
                    errors, appropriate error responses will be returned.
                    """
    )
    @ApiResponses(value = {
            @ApiResponse(
                    responseCode = "200",
                    description = "Successfully retrieved assignments",
                    content = @Content(
                            mediaType = "application/json",
                            schema = @Schema(implementation = Page.class),
                            examples = @ExampleObject(
                                    value = """
                                            {
                                              "content": [
                                                {
                                                  "id": 1,
                                                  "title": "First Assignment",
                                                  "description": "Description of the first assignment.",
                                                  "deadline": "2024-12-15T23:59:59Z",
                                                  "courseId": 10
                                                },
                                                {
                                                  "id": 2,
                                                  "title": "Second Assignment",
                                                  "description": "Description of the second assignment.",
                                                  "deadline": "2024-12-20T23:59:59Z",
                                                  "courseId": 10
                                                }
                                              ],
                                              "pageable": {
                                                "sort": {
                                                  "sorted": false,
                                                  "unsorted": true,
                                                  "empty": true
                                                },
                                                "offset": 0,
                                                "pageNumber": 0,
                                                "pageSize": 10,
                                                "unpaged": false,
                                                "paged": true
                                              },
                                              "totalElements": 2,
                                              "totalPages": 1,
                                              "size": 10,
                                              "number": 0,
                                              "numberOfElements": 2,
                                              "first": true,
                                              "last": true,
                                              "empty": false
                                            }
                                            """
                            )
                    )
            ),
            @ApiResponse(
                    responseCode = "400",
                    description = "Invalid data provided for fetching assignments",
                    content = @Content(
                            mediaType = "application/json",
                            examples = @ExampleObject(
                                    value = """
                                            {
                                              "message": "Invalid pagination parameters",
                                              "details": "Page size must be greater than zero"
                                            }
                                            """
                            )
                    )
            ),
            @ApiResponse(
                    responseCode = "500",
                    description = "Internal server error while fetching assignments",
                    content = @Content
            )
    })
    @GetMapping
    @PreAuthorize("hasAuthority('MANAGE_ASSIGNMENTS')")
    public ResponseEntity<Page<AssignmentDTO>> getAllAssignments(Pageable pageable) {
        try {
            Page<AssignmentDTO> assignments = assignmentService.getAllAssignments(pageable);
            return ResponseEntity.ok(assignments);
        } catch (InvalidDataException e) {
            logger.error("Invalid data fetching assignments: {}", e.getMessage());
            return ResponseEntity.badRequest().body(null);
        } catch (Exception e) {
            logger.error("Error fetching all assignments: {}", e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(null);
        }
    }

    @Operation(
            summary = "Retrieve assignment by ID",
            description = """
                    Fetches an assignment record from the system using its unique ID.
                    This endpoint is useful for retrieving detailed information about
                    a specific assignment. A valid assignment ID must be provided
                    as a path variable. If the assignment exists, the server responds
                    with a 200 OK status and the corresponding AssignmentDTO object.
                    If no assignment is found with the specified ID, a 404 Not Found
                    response is returned, along with an error message. In case of
                    unexpected errors, a 500 Internal Server Error response is sent.
                    """
    )
    @ApiResponses(value = {
            @ApiResponse(
                    responseCode = "200",
                    description = "Assignment successfully retrieved",
                    content = @Content(
                            mediaType = "application/json",
                            schema = @Schema(implementation = AssignmentDTO.class),
                            examples = @ExampleObject(
                                    value = """
                                            {
                                              "id": 1,
                                              "title": "Assignment Title",
                                              "description": "This is a detailed description of the assignment.",
                                              "deadline": "2024-12-15T23:59:59Z",
                                              "courseId": 10
                                            }
                                            """
                            )
                    )
            ),
            @ApiResponse(
                    responseCode = "404",
                    description = "Assignment not found with the specified ID",
                    content = @Content(
                            mediaType = "application/json",
                            examples = @ExampleObject(
                                    value = """
                                            {
                                              "message": "Assignment not found",
                                              "details": "No assignment found with ID 1"
                                            }
                                            """
                            )
                    )
            ),
            @ApiResponse(
                    responseCode = "500",
                    description = "Unexpected error while retrieving the assignment",
                    content = @Content
            )
    })
    @GetMapping("/{id}")
    @PreAuthorize("hasAuthority('MANAGE_ASSIGNMENTS')")
    public ResponseEntity<AssignmentDTO> getAssignmentById(
            @Parameter(
                    description = "ID of the assignment to retrieve",
                    required = true
            )
            @PathVariable Long id) {
        try {
            Optional<AssignmentDTO> assignmentDTO = assignmentService.getAssignmentById(id);
            if (assignmentDTO.isPresent()) {
                return ResponseEntity.ok(assignmentDTO.get());
            } else {
                logger.error("Assignment not found with ID {}.", id);
                return ResponseEntity.status(HttpStatus.NOT_FOUND)
                        .body(new AssignmentDTO(null, "Assignment not found", null, null, null, null, null, null, null)); // Aquí puedes proporcionar un cuerpo más informativo
            }
        } catch (Exception e) {
            logger.error("Unexpected error retrieving assignment with ID {}: {}", id, e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(new AssignmentDTO(null, "Error retrieving assignment", null, null, null, null, null, null, null)); // Cuerpo de error claro
        }
    }

    @Operation(
            summary = "Create a new assignment",
            description = """
                    This endpoint allows users to create a new assignment record in the system.
                    The request must include an AssignmentDTO object in the request body,
                    which contains the assignment details such as title, description,
                    deadline, and associated course ID. Upon successful creation,
                    the server responds with a 201 Created status, along with the
                    created AssignmentDTO object in the response body.
                    If the provided data is invalid, a 400 Bad Request response is returned,
                    detailing the validation errors.
                    """
    )
    @ApiResponses(value = {
            @ApiResponse(
                    responseCode = "201",
                    description = "Assignment successfully created",
                    content = @Content(
                            mediaType = "application/json",
                            schema = @Schema(implementation = AssignmentDTO.class),
                            examples = @ExampleObject(
                                    value = """
                                            {
                                              "id": 1,
                                              "title": "New Assignment Title",
                                              "description": "This is a detailed description of the new assignment.",
                                              "deadline": "2024-12-15T23:59:59Z",
                                              "courseId": 10
                                            }
                                            """
                            )
                    )
            ),
            @ApiResponse(
                    responseCode = "400",
                    description = "Invalid assignment data provided",
                    content = @Content(
                            mediaType = "application/json",
                            examples = @ExampleObject(
                                    value = """
                                            {
                                              "message": "Invalid data provided",
                                              "details": [
                                                "Title must not be empty",
                                                "Deadline must be a future date"
                                              ]
                                            }
                                            """
                            )
                    )
            ),
            @ApiResponse(
                    responseCode = "500",
                    description = "Internal server error while creating the assignment",
                    content = @Content
            )
    })
    @PostMapping
    @PreAuthorize("hasAuthority('MANAGE_ASSIGNMENTS')")
    public ResponseEntity<AssignmentDTO> createAssignment(
            @Parameter(
                    description = "Details of the assignment to be created",
                    required = true
            )
            @Valid @RequestBody AssignmentDTO assignmentDTO) {
        try {
            AssignmentDTO createdAssignment = assignmentService.createAssignment(assignmentDTO);
            return ResponseEntity.status(HttpStatus.CREATED).body(createdAssignment);
        } catch (InvalidDataException e) {
            logger.error("Invalid data for creating assignment: {}", e.getMessage());
            return ResponseEntity.badRequest().body(null);
        } catch (Exception e) {
            logger.error("Unexpected error creating assignment: {}", e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(null);
        }
    }

    @Operation(
            summary = "Update an existing assignment by ID",
            description = """
                    Updates the details of a specific assignment identified by its ID.
                    This endpoint allows clients to modify the assignment's properties by sending an
                    updated AssignmentDTO object in the request body.
                    If the assignment is successfully updated, the response includes the updated
                    AssignmentDTO. If the assignment is not found, a 404 Not Found response is returned.
                    If the provided data is invalid, a 400 Bad Request response is returned.
                    """
    )
    @ApiResponses(value = {
            @ApiResponse(
                    responseCode = "200",
                    description = "Assignment successfully updated",
                    content = @Content(
                            mediaType = "application/json",
                            schema = @Schema(implementation = AssignmentDTO.class),
                            examples = @ExampleObject(
                                    value = """
                                            {
                                              "id": 1,
                                              "title": "Updated Assignment Title",
                                              "description": "Updated description of the assignment.",
                                              "deadline": "2024-12-01T23:59:59Z",
                                              "courseId": 10
                                            }
                                            """
                            )
                    )
            ),
            @ApiResponse(
                    responseCode = "404",
                    description = "Assignment not found with the specified ID",
                    content = @Content
            ),
            @ApiResponse(
                    responseCode = "400",
                    description = "Invalid data provided for updating the assignment",
                    content = @Content
            ),
            @ApiResponse(
                    responseCode = "500",
                    description = "Unexpected error while attempting to update the assignment",
                    content = @Content
            )
    })
    @PutMapping("/{id}")
    @PreAuthorize("hasAuthority('MANAGE_ASSIGNMENTS')")
    public ResponseEntity<AssignmentDTO> updateAssignment(
            @Parameter(
                    description = "Unique identifier of the assignment to be updated",
                    required = true,
                    example = "5"
            )
            @PathVariable Long id,
            @Valid @RequestBody AssignmentDTO assignmentDTO) {
        try {
            Optional<AssignmentDTO> updatedAssignment = assignmentService.updateAssignment(id, assignmentDTO);
            return updatedAssignment
                    .map(ResponseEntity::ok)
                    .orElseGet(() -> ResponseEntity.status(HttpStatus.NOT_FOUND).build());
        } catch (InvalidDataException e) {
            logger.error("Invalid data for updating assignment ID {}: {}", id, e.getMessage());
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).build(); // Cambiado para mayor consistencia
        } catch (Exception e) {
            logger.error("Unexpected error updating assignment ID {}: {}", id, e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build(); // Cambiado para mayor consistencia
        }
    }

    @Operation(
            summary = "Delete an assignment by ID",
            description = """
                    Deletes a specific assignment identified by its ID.
                    This endpoint permanently removes the assignment from the system.
                    If the assignment is successfully deleted, a 204 No Content response is returned.
                    If the specified assignment does not exist, a 404 Not Found response is returned.
                    """
    )
    @ApiResponses(value = {
            @ApiResponse(
                    responseCode = "204",
                    description = "Assignment successfully deleted"
            ),
            @ApiResponse(
                    responseCode = "404",
                    description = "Assignment not found with the specified ID",
                    content = @Content
            ),
            @ApiResponse(
                    responseCode = "500",
                    description = "Unexpected error while attempting to delete the assignment",
                    content = @Content
            )
    })
    @DeleteMapping("/{id}")
    @PreAuthorize("hasAuthority('MANAGE_ASSIGNMENTS')")
    public ResponseEntity<Void> deleteAssignment(
            @Parameter(
                    description = "Unique identifier of the assignment to be deleted",
                    required = true,
                    example = "5"
            )
            @PathVariable Long id) {
        try {
            assignmentService.deleteAssignment(id);
            return ResponseEntity.noContent().build();
        } catch (ResourceNotFoundException e) {
            logger.error("Error deleting assignment with ID {}: {}", id, e.getMessage());
            return ResponseEntity.status(HttpStatus.NOT_FOUND).build(); // Cambiado a .build()
        } catch (Exception e) {
            logger.error("Unexpected error deleting assignment with ID {}: {}", id, e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build(); // Cambiado a .build()
        }
    }

    @Operation(
            summary = "Retrieve assignments by course ID",
            description = """
                    Retrieves a paginated list of assignments associated with a specified course.
                    This endpoint is useful for fetching assignments in manageable pages by specifying the course ID.
                    The response includes AssignmentDTO objects containing assignment details.
                    """
    )
    @ApiResponses(value = {
            @ApiResponse(
                    responseCode = "200",
                    description = "Assignments successfully retrieved",
                    content = @Content(
                            mediaType = "application/json",
                            schema = @Schema(implementation = Page.class),
                            examples = @ExampleObject(
                                    value = """
                                            {
                                              "content": [
                                                {
                                                  "id": 1,
                                                  "title": "Assignment 1",
                                                  "description": "Complete chapter 3 exercises",
                                                  "deadline": "2024-11-01T23:59:59Z",
                                                  "course": {
                                                    "id": 10,
                                                    "name": "Mathematics"
                                                  },
                                                  "state": "ACTIVE"
                                                },
                                                {
                                                  "id": 2,
                                                  "title": "Assignment 2",
                                                  "description": "Write a summary of chapter 4",
                                                  "deadline": "2024-11-10T23:59:59Z",
                                                  "course": {
                                                    "id": 10,
                                                    "name": "Mathematics"
                                                  },
                                                  "state": "ACTIVE"
                                                }
                                              ],
                                              "pageable": {
                                                "pageNumber": 0,
                                                "pageSize": 10,
                                                "sort": {
                                                  "sorted": true,
                                                  "unsorted": false,
                                                  "empty": false
                                                }
                                              },
                                              "totalPages": 1,
                                              "totalElements": 2
                                            }
                                            """
                            )
                    )
            ),
            @ApiResponse(
                    responseCode = "400",
                    description = "Invalid course ID or pagination parameters",
                    content = @Content
            ),
            @ApiResponse(
                    responseCode = "500",
                    description = "Unexpected error retrieving assignments for the specified course",
                    content = @Content
            )
    })
    @GetMapping("/course/{courseId}")
    @PreAuthorize("hasAuthority('GET_COURSE_ASSIGNMENTS')")
    public ResponseEntity<Page<AssignmentDTO>> getAssignmentsByCourseId(
            @Parameter(
                    description = "Unique identifier of the course to fetch assignments for",
                    required = true,
                    example = "10"
            )
            @PathVariable Long courseId,
            Pageable pageable) {
        try {
            Page<AssignmentDTO> assignments = assignmentService.getAssignmentsByCourseId(courseId, pageable);
            return ResponseEntity.ok(assignments);
        } catch (InvalidDataException e) {
            logger.error("Invalid data fetching assignments for course ID {}: {}", courseId, e.getMessage());
            return ResponseEntity.badRequest().body(null);
        } catch (Exception e) {
            logger.error("Error fetching assignments for course ID {}: {}", courseId, e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(null);
        }
    }

    @Operation(
            summary = "Add a submission to an assignment",
            description = """
                    This endpoint allows students to submit their work for a specific assignment.
                    It requires the ID of the assignment and the submission details.
                    Upon successful creation of the submission, a 201 Created response is returned
                    along with the created SubmissionDTO object.
                    If the assignment is not found or if the provided data is invalid, appropriate error responses will be returned.
                    """
    )
    @ApiResponses(value = {
            @ApiResponse(
                    responseCode = "201",
                    description = "Submission successfully added to the assignment",
                    content = @Content(
                            mediaType = "application/json",
                            schema = @Schema(implementation = SubmissionDTO.class)
                    )
            ),
            @ApiResponse(
                    responseCode = "404",
                    description = "Assignment not found with the provided ID",
                    content = @Content
            ),
            @ApiResponse(
                    responseCode = "400",
                    description = "Invalid submission data",
                    content = @Content(
                            mediaType = "application/json",
                            examples = @ExampleObject(
                                    value = """
                                            {
                                              "message": "Invalid file name",
                                              "details": "File name must not be blank"
                                            }
                                            """
                            )
                    )
            ),
            @ApiResponse(
                    responseCode = "500",
                    description = "Internal server error while adding submission",
                    content = @Content
            )
    })
    @PostMapping("/{assignmentId}/submissions")
    @PreAuthorize("hasAuthority('ADD_ASSIGNMENT_SUBMISSION')")
    public ResponseEntity<SubmissionDTO> addSubmissionToAssignment(
            @Parameter(
                    description = "Unique identifier of the assignment to which the submission will be added",
                    required = true,
                    example = "20"
            )
            @PathVariable Long assignmentId,
            @Valid @RequestBody SubmissionDTO submissionDTO) {
        try {
            SubmissionDTO createdSubmission = assignmentService.addSubmissionToAssignment(assignmentId, submissionDTO);
            return ResponseEntity.status(HttpStatus.CREATED).body(createdSubmission);
        } catch (ResourceNotFoundException e) {
            logger.error("Assignment not found with ID {}: {}", assignmentId, e.getMessage());
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(null);
        } catch (InvalidDataException e) {
            logger.error("Invalid data for submission: {}", e.getMessage());
            return ResponseEntity.badRequest().body(null);
        } catch (Exception e) {
            logger.error("Unexpected error adding submission to assignment ID {}: {}", assignmentId, e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(null);
        }
    }

    @Operation(
            summary = "Remove Submission from Assignment",
            description = "This endpoint allows users to remove a specific submission from a designated assignment. The IDs for both the assignment and the submission to be removed are provided as path parameters. Once successfully executed, the specified submission will no longer be associated with the assignment."
    )
    @ApiResponses(value = {
            @ApiResponse(
                    responseCode = "204",
                    description = "Submission successfully removed from the assignment. No content is returned as confirmation of the operation.",
                    content = @Content(
                            mediaType = "application/json",
                            examples = @ExampleObject(value = "{\"message\": \"Submission successfully removed from the assignment.\"}")
                    )
            ),
            @ApiResponse(
                    responseCode = "404",
                    description = "Assignment or submission not found. This indicates that either the specified assignment ID or submission ID does not exist.",
                    content = @Content(
                            mediaType = "application/json",
                            examples = @ExampleObject(value = "{\"message\": \"Assignment or submission not found.\"}")
                    )
            ),
            @ApiResponse(
                    responseCode = "500",
                    description = "An error occurred while removing the submission from the assignment. This indicates a server-side issue that prevented the operation from completing successfully.",
                    content = @Content(
                            mediaType = "application/json",
                            examples = @ExampleObject(value = "{\"message\": \"An error occurred while removing the submission from the assignment.\"}")
                    )
            )
    })
    @DeleteMapping("/{assignmentId}/submissions/{submissionId}")
    @PreAuthorize("hasAuthority('REMOVE_ASSIGNMENT_SUBMISSION')")
    public ResponseEntity<Void> removeSubmissionFromAssignment(@PathVariable Long assignmentId,
                                                               @PathVariable Long submissionId) {
        try {
            assignmentService.removeSubmissionFromAssignment(assignmentId, submissionId);
            return ResponseEntity.noContent().build();
        } catch (ResourceNotFoundException e) {
            logger.warn("Failed to remove submission ID {} from assignment ID {}: {}", submissionId, assignmentId, e.getMessage());
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(null);
        } catch (Exception e) {
            logger.error("Error removing submission ID {} from assignment ID {}: {}", submissionId, assignmentId, e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(null);
        }
    }
}