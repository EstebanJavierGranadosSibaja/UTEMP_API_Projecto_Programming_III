package org.una.programmingIII.UTEMP_Project.controllers;

import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.ExampleObject;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.Valid;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import org.una.programmingIII.UTEMP_Project.dtos.FileMetadatumDTO;
import org.una.programmingIII.UTEMP_Project.dtos.GradeDTO;
import org.una.programmingIII.UTEMP_Project.dtos.SubmissionDTO;
import org.una.programmingIII.UTEMP_Project.exceptions.InvalidDataException;
import org.una.programmingIII.UTEMP_Project.exceptions.ResourceNotFoundException;
import org.una.programmingIII.UTEMP_Project.services.submission.SubmissionService;

import java.util.Optional;

@RestController
@RequestMapping("/utemp/submissions")
public class SubmissionController {

    private final SubmissionService submissionService;
    private final Logger logger = LoggerFactory.getLogger(SubmissionController.class);

    @Autowired
    public SubmissionController(SubmissionService submissionService) {
        this.submissionService = submissionService;
    }

    @Operation(
            summary = "Get all submissions",
            description = "Retrieve a paginated list of all submissions."
    )
    @ApiResponses(value = {
            @ApiResponse(
                    responseCode = "200",
                    description = "List of submissions retrieved successfully.",
                    content = @Content(
                            mediaType = "application/json",
                            schema = @Schema(implementation = Page.class)
                    )
            ),
            @ApiResponse(
                    responseCode = "500",
                    description = "Internal server error.",
                    content = @Content(
                            mediaType = "application/json",
                            examples = @ExampleObject(value = "{\"message\": \"Internal server error.\"}")
                    )
            )
    })
    @GetMapping
    @PreAuthorize("hasAuthority('GET_ALL_SUBMISSIONS')")
    public ResponseEntity<Page<SubmissionDTO>> getAllSubmissions(Pageable pageable) {
        try {
            Page<SubmissionDTO> submissions = submissionService.getAllSubmissions(pageable);
            return ResponseEntity.ok(submissions);
        } catch (Exception e) {
            logger.error("Error retrieving submissions", e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }

    @Operation(
            summary = "Get submission by ID",
            description = "Retrieve a submission by its ID."
    )
    @ApiResponses(value = {
            @ApiResponse(
                    responseCode = "200",
                    description = "Submission retrieved successfully.",
                    content = @Content(
                            mediaType = "application/json",
                            schema = @Schema(implementation = SubmissionDTO.class)
                    )
            ),
            @ApiResponse(
                    responseCode = "404",
                    description = "Submission not found.",
                    content = @Content(
                            mediaType = "application/json",
                            examples = @ExampleObject(value = "{\"message\": \"Submission not found.\"}")
                    )
            ),
            @ApiResponse(
                    responseCode = "500",
                    description = "Internal server error.",
                    content = @Content(
                            mediaType = "application/json",
                            examples = @ExampleObject(value = "{\"message\": \"Internal server error.\"}")
                    )
            )
    })
    @GetMapping("/{id}")
    @PreAuthorize("hasAuthority('GET_SUBMISSION_BY_ID')")
    public ResponseEntity<SubmissionDTO> getSubmissionById(
            @Parameter(description = "ID of the submission to retrieve", required = true) @PathVariable Long id) {
        try {
            Optional<SubmissionDTO> submissionDTO = submissionService.getSubmissionById(id);
            return submissionDTO.map(ResponseEntity::ok)
                    .orElseGet(() -> {
                        logger.warn("1Submission not found with id: {}", id);
                        return ResponseEntity.notFound().build();
                    });
        } catch (Exception e) {
            logger.error("Error retrieving submission with id: {}", id, e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }

    @Operation(
            summary = "Create a new submission",
            description = "Create a new submission."
    )
    @ApiResponses(value = {
            @ApiResponse(
                    responseCode = "201",
                    description = "Submission created successfully.",
                    content = @Content(
                            mediaType = "application/json",
                            schema = @Schema(implementation = SubmissionDTO.class)
                    )
            ),
            @ApiResponse(
                    responseCode = "400",
                    description = "Invalid data provided.",
                    content = @Content(
                            mediaType = "application/json",
                            examples = @ExampleObject(value = "{\"message\": \"Invalid data provided.\"}")
                    )
            ),
            @ApiResponse(
                    responseCode = "500",
                    description = "Internal server error.",
                    content = @Content(
                            mediaType = "application/json",
                            examples = @ExampleObject(value = "{\"message\": \"Internal server error.\"}")
                    )
            )
    })
    @PostMapping
    @PreAuthorize("hasAuthority('CREATE_SUBMISSION')")
    public ResponseEntity<SubmissionDTO> createSubmission(
            @Valid @RequestBody SubmissionDTO submissionDTO) {
        try {
            SubmissionDTO createdSubmission = submissionService.createSubmission(submissionDTO);
            return ResponseEntity.status(HttpStatus.CREATED).body(createdSubmission);
        } catch (InvalidDataException e) {
            logger.warn("Invalid data for submission creation: {}", submissionDTO, e);
            return ResponseEntity.badRequest().build();
        } catch (Exception e) {
            logger.error("Error creating submission", e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }

    @Operation(
            summary = "Update submission",
            description = "Update an existing submission by its ID."
    )
    @ApiResponses(value = {
            @ApiResponse(
                    responseCode = "200",
                    description = "Submission updated successfully.",
                    content = @Content(
                            mediaType = "application/json",
                            schema = @Schema(implementation = SubmissionDTO.class)
                    )
            ),
            @ApiResponse(
                    responseCode = "404",
                    description = "Submission not found.",
                    content = @Content(
                            mediaType = "application/json",
                            examples = @ExampleObject(value = "{\"message\": \"Submission not found.\"}")
                    )
            ),
            @ApiResponse(
                    responseCode = "400",
                    description = "Invalid data provided.",
                    content = @Content(
                            mediaType = "application/json",
                            examples = @ExampleObject(value = "{\"message\": \"Invalid data provided.\"}")
                    )
            ),
            @ApiResponse(
                    responseCode = "500",
                    description = "Internal server error.",
                    content = @Content(
                            mediaType = "application/json",
                            examples = @ExampleObject(value = "{\"message\": \"Internal server error.\"}")
                    )
            )
    })
    @PutMapping("/{id}")
    @PreAuthorize("hasAuthority('UPDATE_SUBMISSION')")
    public ResponseEntity<SubmissionDTO> updateSubmission(
            @Parameter(description = "ID of the submission to update", required = true) @PathVariable Long id,
            @Valid @RequestBody SubmissionDTO submissionDTO) {
        try {
            Optional<SubmissionDTO> updatedSubmission = submissionService.updateSubmission(id, submissionDTO);
            return updatedSubmission.map(ResponseEntity::ok)
                    .orElseGet(() -> {
                        logger.warn("Submission not found with id: {}", id);
                        return ResponseEntity.notFound().build();
                    });
        } catch (InvalidDataException e) {
            logger.warn("Invalid data for submission update: {}", submissionDTO, e);
            return ResponseEntity.badRequest().build();
        } catch (Exception e) {
            logger.error("Error updating submission with id: {}", id, e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }

    @Operation(
            summary = "Delete submission",
            description = "Delete a submission by its ID."
    )
    @ApiResponses(value = {
            @ApiResponse(
                    responseCode = "204",
                    description = "Submission deleted successfully."
            ),
            @ApiResponse(
                    responseCode = "404",
                    description = "Submission not found.",
                    content = @Content(
                            mediaType = "application/json",
                            examples = @ExampleObject(value = "{\"message\": \"Submission not found.\"}")
                    )
            ),
            @ApiResponse(
                    responseCode = "500",
                    description = "Internal server error.",
                    content = @Content(
                            mediaType = "application/json",
                            examples = @ExampleObject(value = "{\"message\": \"Internal server error.\"}")
                    )
            )
    })
    @DeleteMapping("/{id}")
    @PreAuthorize("hasAuthority('DELETE_SUBMISSION')")
    public ResponseEntity<Void> deleteSubmission(
            @Parameter(description = "ID of the submission to delete", required = true) @PathVariable Long id) {
        try {
            submissionService.deleteSubmission(id);
            return ResponseEntity.noContent().build();
        } catch (ResourceNotFoundException e) {
            logger.warn("Submission not found with id: {}", id, e);
            return ResponseEntity.notFound().build();
        } catch (Exception e) {
            logger.error("Error deleting submission with id: {}", id, e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }
    @Operation(
            summary = "Get submissions by assignment ID",
            description = "Retrieve a paginated list of submissions for a specific assignment."
    )
    @ApiResponses(value = {
            @ApiResponse(
                    responseCode = "200",
                    description = "List of submissions for assignment retrieved successfully."
            ),
            @ApiResponse(
                    responseCode = "500",
                    description = "Internal server error.",
                    content = @Content(
                            mediaType = "application/json",
                            examples = @ExampleObject(value = "{\"message\": \"Internal server error.\"}")
                    )
            )
    })
    @GetMapping("/assignment/{assignmentId}")
    @PreAuthorize("hasAuthority('GET_ALL_ASSIGNMENT_SUBMISSIONS')")
    public ResponseEntity<Page<SubmissionDTO>> getSubmissionsByAssignmentId(
            @Parameter(description = "ID of the assignment", required = true) @PathVariable Long assignmentId,
            Pageable pageable) {
        try {
            Page<SubmissionDTO> submissions = submissionService.getSubmissionsByAssignmentId(assignmentId, pageable);
            return ResponseEntity.ok(submissions);
        } catch (Exception e) {
            logger.error("Error retrieving submissions for assignment id: {}", assignmentId, e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }

    @Operation(
            summary = "Add file metadata to submission",
            description = "Add file metadata to an existing submission."
    )
    @ApiResponses(value = {
            @ApiResponse(
                    responseCode = "201",
                    description = "File metadata added successfully.",
                    content = @Content(
                        mediaType = "application/json",
                        schema = @Schema(implementation = FileMetadatumDTO.class)
            )
            ),
            @ApiResponse(
                    responseCode = "400",
                    description = "Invalid data provided.",
                    content = @Content(
                            mediaType = "application/json",
                            examples = @ExampleObject(value = "{\"message\": \"Invalid data provided.\"}")
                    )
            ),
            @ApiResponse(
                    responseCode = "404",
                    description = "Submission not found.",
                    content = @Content(
                            mediaType = "application/json",
                            examples = @ExampleObject(value = "{\"message\": \"Submission not found.\"}")
                    )
            ),
            @ApiResponse(
                    responseCode = "500",
                    description = "Internal server error.",
                    content = @Content(
                            mediaType = "application/json",
                            examples = @ExampleObject(value = "{\"message\": \"Internal server error.\"}")
                    )
            )
    })
    @PostMapping("/{submissionId}/file-metadata")
    @PreAuthorize("hasAuthority('ADD_FILE_TO_SUBMISSION')")
    public ResponseEntity<FileMetadatumDTO> addFileMetadatumToSubmission(
            @Parameter(description = "ID of the submission", required = true) @PathVariable Long submissionId,
            @Valid @RequestBody FileMetadatumDTO fileMetadatumDTO) {
        try {
            FileMetadatumDTO createdFileMetadatum = submissionService.addFileMetadatumToSubmission(submissionId, fileMetadatumDTO);
            return ResponseEntity.status(HttpStatus.CREATED).body(createdFileMetadatum);
        } catch (InvalidDataException e) {
            logger.warn("Invalid data for adding file metadata to submission id: {}", submissionId, e);
            return ResponseEntity.badRequest().build();
        } catch (ResourceNotFoundException e) {
            logger.warn("Submission not found with id: {}", submissionId, e);
            return ResponseEntity.notFound().build();
        } catch (Exception e) {
            logger.error("Error adding file metadata to submission id: {}", submissionId, e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }

    @Operation(
            summary = "Add grade to submission",
            description = "Add a grade to an existing submission."
    )
    @ApiResponses(value = {
            @ApiResponse(
                    responseCode = "201",
                    description = "Grade added successfully.",
                    content = @Content(
                        mediaType = "application/json",
                        schema = @Schema(implementation = GradeDTO.class)
            )
            ),
            @ApiResponse(
                    responseCode = "400",
                    description = "Invalid data provided.",
                    content = @Content(
                            mediaType = "application/json",
                            examples = @ExampleObject(value = "{\"message\": \"Invalid data provided.\"}")
                    )
            ),
            @ApiResponse(
                    responseCode = "404",
                    description = "Submission not found.",
                    content = @Content(
                            mediaType = "application/json",
                            examples = @ExampleObject(value = "{\"message\": \"Submission not found.\"}")
                    )
            ),
            @ApiResponse(
                    responseCode = "500",
                    description = "Internal server error.",
                    content = @Content(
                            mediaType = "application/json",
                            examples = @ExampleObject(value = "{\"message\": \"Internal server error.\"}")
                    )
            )
    })
    @PostMapping("/{submissionId}/grades")
    @PreAuthorize("hasAuthority('ADD_GRADE_TO_SUBMISSION')")
    public ResponseEntity<GradeDTO> addGradeToSubmission(
            @Parameter(description = "ID of the submission", required = true) @PathVariable Long submissionId,
            @Valid @RequestBody GradeDTO gradeDTO) {
        try {
            GradeDTO createdGrade = submissionService.addGradeToSubmission(submissionId, gradeDTO);
            return ResponseEntity.status(HttpStatus.CREATED).body(createdGrade);
        } catch (InvalidDataException e) {
            logger.warn("Invalid data for adding grade to submission id: {}", submissionId, e);
            return ResponseEntity.badRequest().build();
        } catch (ResourceNotFoundException e) {
            logger.warn("Submission not found with id: {}", submissionId, e);
            return ResponseEntity.notFound().build();
        } catch (Exception e) {
            logger.error("Error adding grade to submission id: {}", submissionId, e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }

    @Operation(
            summary = "Remove File Metadata from Submission",
            description = "This endpoint allows users to remove a specific file metadata entry from a designated submission. The IDs for both the submission and the file metadata to be removed are provided as path parameters. Upon successful execution, the specified file metadata will no longer be associated with the submission."
    )
    @ApiResponses(value = {
            @ApiResponse(
                    responseCode = "204",
                    description = "File metadata successfully removed from the submission. No content is returned as confirmation of the operation.",
                    content = @Content(
                            mediaType = "application/json",
                            examples = @ExampleObject(value = "{\"message\": \"File metadata successfully removed from the submission.\"}")
                    )
            ),
            @ApiResponse(
                    responseCode = "404",
                    description = "Submission or file metadata not found. This indicates that either the specified submission ID or file metadata ID does not exist.",
                    content = @Content(
                            mediaType = "application/json",
                            examples = @ExampleObject(value = "{\"message\": \"Submission or file metadata not found.\"}")
                    )
            ),
            @ApiResponse(
                    responseCode = "500",
                    description = "An error occurred while removing the file metadata from the submission. This indicates a server-side issue that prevented the operation from completing successfully.",
                    content = @Content(
                            mediaType = "application/json",
                            examples = @ExampleObject(value = "{\"message\": \"An error occurred while removing the file metadata from the submission.\"}")
                    )
            )
    })
    @DeleteMapping("/{submissionId}/file-metadata/{fileMetadatumId}")
    @PreAuthorize("hasAuthority('REMOVE_FILE_TO_SUBMISSION')")
    public ResponseEntity<Void> removeFileMetadatumFromSubmission(@PathVariable Long submissionId,
                                                                  @PathVariable Long fileMetadatumId) {
        try {
            submissionService.removeFileMetadatumFromSubmission(submissionId, fileMetadatumId);
            return ResponseEntity.noContent().build();
        } catch (ResourceNotFoundException e) {
            logger.warn("Failed to remove file metadata ID {} from submission ID {}: {}", fileMetadatumId, submissionId, e.getMessage());
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(null);
        } catch (Exception e) {
            logger.error("Error removing file metadata ID {} from submission ID {}: {}", fileMetadatumId, submissionId, e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(null);
        }
    }

    @Operation(
            summary = "Remove Grade from Submission",
            description = "This endpoint allows users to remove a specific grade entry from a designated submission. The IDs for both the submission and the grade to be removed are provided as path parameters. Upon successful execution, the specified grade will no longer be associated with the submission."
    )
    @ApiResponses(value = {
            @ApiResponse(
                    responseCode = "204",
                    description = "Grade successfully removed from the submission. No content is returned as confirmation of the operation.",
                    content = @Content(
                            mediaType = "application/json",
                            examples = @ExampleObject(value = "{\"message\": \"Grade successfully removed from the submission.\"}")
                    )
            ),
            @ApiResponse(
                    responseCode = "404",
                    description = "Submission or grade not found. This indicates that either the specified submission ID or grade ID does not exist.",
                    content = @Content(
                            mediaType = "application/json",
                            examples = @ExampleObject(value = "{\"message\": \"Submission or grade not found.\"}")
                    )
            ),
            @ApiResponse(
                    responseCode = "500",
                    description = "An error occurred while removing the grade from the submission. This indicates a server-side issue that prevented the operation from completing successfully.",
                    content = @Content(
                            mediaType = "application/json",
                            examples = @ExampleObject(value = "{\"message\": \"An error occurred while removing the grade from the submission.\"}")
                    )
            )
    })
    @DeleteMapping("/{submissionId}/grades/{gradeId}")
    @PreAuthorize("hasAuthority('REMOVE_GRADE_TO_SUBMISSION')")
    public ResponseEntity<Void> removeGradeFromSubmission(@PathVariable Long submissionId,
                                                          @PathVariable Long gradeId) {
        try {
            submissionService.removeGradeFromSubmission(submissionId, gradeId);
            return ResponseEntity.noContent().build(); // 204 No Content response on successful removal
        } catch (ResourceNotFoundException e) {
            logger.warn("Failed to remove grade ID {} from submission ID {}: {}", gradeId, submissionId, e.getMessage());
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(null);
        } catch (Exception e) {
            logger.error("Error removing grade ID {} from submission ID {}: {}", gradeId, submissionId, e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(null);
        }
    }
}