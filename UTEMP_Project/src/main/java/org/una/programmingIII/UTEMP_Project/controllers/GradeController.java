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
import org.una.programmingIII.UTEMP_Project.dtos.GradeDTO;
import org.una.programmingIII.UTEMP_Project.exceptions.InvalidDataException;
import org.una.programmingIII.UTEMP_Project.exceptions.ResourceNotFoundException;
import org.una.programmingIII.UTEMP_Project.services.grade.GradeService;
import org.una.programmingIII.UTEMP_Project.utils.PageConverter;
import org.una.programmingIII.UTEMP_Project.utils.PageDTO;

@RestController
@RequestMapping("/utemp/grades")
public class GradeController {

    private static final Logger logger = LoggerFactory.getLogger(GradeController.class);
    private final GradeService gradeService;

    @Autowired
    public GradeController(GradeService gradeService) {
        this.gradeService = gradeService;
    }

    @Operation(
            summary = "Get all grades",
            description = "Retrieve a paginated list of all grades associated with submissions."
    )
    @ApiResponses(value = {
            @ApiResponse(
                    responseCode = "200",
                    description = "Operation successful.",
                    content = @Content(mediaType = "application/json")
            ),
            @ApiResponse(
                    responseCode = "400",
                    description = "Invalid data.",
                    content = @Content(
                            mediaType = "application/json",
                            examples = @ExampleObject(value = "{\"message\": \"Invalid data.\"}")
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
    @PreAuthorize("hasAuthority('MANAGE_GRADES')")
    public ResponseEntity<PageDTO<GradeDTO>> getAllGrades(Pageable pageable) {
        try {
            Page<GradeDTO> gradesPage = gradeService.getAllGrades(pageable);
            PageDTO<GradeDTO> gradesDTOPage = PageConverter.convertPageToDTO(gradesPage, gradeDTO -> gradeDTO);
            logger.info("Fetched all grades successfully.");
            return ResponseEntity.ok(gradesDTOPage);
        } catch (InvalidDataException e) {
            logger.error("Invalid data while fetching grades: {}", e.getMessage());
            return ResponseEntity.badRequest().build();
        } catch (Exception e) {
            logger.error("Error while fetching grades: {}", e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }

    @Operation(
            summary = "Get grade by ID",
            description = "Retrieve a specific grade by its ID."
    )
    @ApiResponses(value = {
            @ApiResponse(
                    responseCode = "200",
                    description = "Operation successful.",
                    content = @Content(mediaType = "application/json")
            ),
            @ApiResponse(
                    responseCode = "404",
                    description = "Resource not found.",
                    content = @Content(
                            mediaType = "application/json",
                            examples = @ExampleObject(value = "{\"message\": \"Resource not found.\"}")
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
    @PreAuthorize("hasAuthority('MANAGE_GRADES')")
    public ResponseEntity<GradeDTO> getGradeById(
            @Parameter(description = "ID of the grade to retrieve", required = true) @PathVariable Long id) {
        try {
            return gradeService.getGradeById(id)
                    .map(gradeDTO -> {
                        logger.info("Fetched grade with ID: {}", id);
                        return ResponseEntity.ok(gradeDTO);
                    })
                    .orElseGet(() -> {
                        logger.warn("Grade with ID: {} not found.", id);
                        return ResponseEntity.status(HttpStatus.NOT_FOUND).build();
                    });
        } catch (Exception e) {
            logger.error("Error while fetching grade with ID {}: {}", id, e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }

    @Operation(
            summary = "Create a new grade",
            description = "Create a new grade record."
    )
    @ApiResponses(value = {
            @ApiResponse(
                    responseCode = "201",
                    description = "Grade created successfully.",
                    content = @Content(
                            mediaType = "application/json",
                            schema = @Schema(implementation = GradeDTO.class)
                    )
            ),
            @ApiResponse(
                    responseCode = "400",
                    description = "Invalid data.",
                    content = @Content(
                            mediaType = "application/json",
                            examples = @ExampleObject(value = "{\"message\": \"Invalid data.\"}")
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
    @PreAuthorize("hasAuthority('MANAGE_GRADES')")
    public ResponseEntity<GradeDTO> createGrade(
            @Parameter(description = "Grade data to create", required = true) @Valid @RequestBody GradeDTO gradeDTO) {
        try {
            GradeDTO createdGrade = gradeService.createGrade(gradeDTO);
            logger.info("Created new grade: {}", createdGrade);
            return ResponseEntity.status(HttpStatus.CREATED).body(createdGrade);
        } catch (InvalidDataException e) {
            logger.error("Invalid data while creating grade: {}", e.getMessage());
            return ResponseEntity.badRequest().build();
        } catch (Exception e) {
            logger.error("Error while creating grade: {}", e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }

    @Operation(
            summary = "Update a grade",
            description = "Update an existing grade by its ID."
    )
    @ApiResponses(value = {
            @ApiResponse(
                    responseCode = "200",
                    description = "Operation successful.",
                    content = @Content(mediaType = "application/json")
            ),
            @ApiResponse(
                    responseCode = "404",
                    description = "Resource not found.",
                    content = @Content(
                            mediaType = "application/json",
                            examples = @ExampleObject(value = "{\"message\": \"Resource not found.\"}")
                    )
            ),
            @ApiResponse(
                    responseCode = "400",
                    description = "Invalid data.",
                    content = @Content(
                            mediaType = "application/json",
                            examples = @ExampleObject(value = "{\"message\": \"Invalid data.\"}")
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
    @PreAuthorize("hasAuthority('MANAGE_GRADES')")
    public ResponseEntity<GradeDTO> updateGrade(
            @Parameter(description = "ID of the grade to update", required = true) @PathVariable Long id,
            @Valid @RequestBody GradeDTO gradeDTO) {
        try {
            return gradeService.updateGrade(id, gradeDTO)
                    .map(updatedGrade -> {
                        logger.info("Updated grade with ID: {}", id);
                        return ResponseEntity.ok(updatedGrade);
                    })
                    .orElseGet(() -> {
                        logger.warn("Grade with ID: {} not found for update.", id);
                        return ResponseEntity.status(HttpStatus.NOT_FOUND).build();
                    });
        } catch (InvalidDataException e) {
            logger.error("Invalid data while updating grade with ID {}: {}", id, e.getMessage());
            return ResponseEntity.badRequest().build();
        } catch (ResourceNotFoundException e) {
            logger.warn("Grade with ID: {} not found for update.", id);
            return ResponseEntity.status(HttpStatus.NOT_FOUND).build();
        } catch (Exception e) {
            logger.error("Error while updating grade with ID {}: {}", id, e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }

    @Operation(
            summary = "Delete a grade",
            description = "Delete a specific grade by its ID."
    )
    @ApiResponses(value = {
            @ApiResponse(
                    responseCode = "204",
                    description = "Grade deleted successfully."
            ),
            @ApiResponse(
                    responseCode = "404",
                    description = "Resource not found.",
                    content = @Content(
                            mediaType = "application/json",
                            examples = @ExampleObject(value = "{\"message\": \"Resource not found.\"}")
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
    @PreAuthorize("hasAuthority('MANAGE_GRADES')")
    public ResponseEntity<Void> deleteGrade(
            @Parameter(description = "ID of the grade to delete", required = true) @PathVariable Long id) {
        try {
            gradeService.deleteGrade(id);
            logger.info("Deleted grade with ID: {}", id);
            return ResponseEntity.noContent().build();
        } catch (ResourceNotFoundException e) {
            logger.warn("Grade with ID: {} not found for deletion.", id);
            return ResponseEntity.status(HttpStatus.NOT_FOUND).build();
        } catch (Exception e) {
            logger.error("Error while deleting grade with ID {}: {}", id, e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }

    @Operation(
            summary = "Get grades by submission ID",
            description = "Retrieve grades associated with a specific submission ID."
    )
    @ApiResponses(value = {
            @ApiResponse(
                    responseCode = "200",
                    description = "Operation successful.",
                    content = @Content(mediaType = "application/json")
            ),
            @ApiResponse(
                    responseCode = "404",
                    description = "Resource not found.",
                    content = @Content(
                            mediaType = "application/json",
                            examples = @ExampleObject(value = "{\"message\": \"Resource not found.\"}")
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
    @GetMapping("/submissions/{submissionId}/grades")
    @PreAuthorize("hasAuthority('GET_SUBMISSION_GRADES')")
    public ResponseEntity<PageDTO<GradeDTO>> getGradesBySubmissionId(
            @Parameter(description = "ID of the submission to retrieve grades for", required = true)
            @PathVariable Long submissionId, Pageable pageable) {
        try {
            Page<GradeDTO> gradesPage = gradeService.getGradesBySubmissionId(submissionId, pageable);
            PageDTO<GradeDTO> gradesDTOPage = PageConverter.convertPageToDTO(gradesPage, gradeDTO -> gradeDTO);
            logger.info("Fetched grades for submission ID: {}", submissionId);
            return ResponseEntity.ok(gradesDTOPage);

        } catch (ResourceNotFoundException e) {
            logger.warn("No grades found for submission ID: {}.", submissionId);
            return ResponseEntity.status(HttpStatus.NOT_FOUND).build();
        } catch (Exception e) {
            logger.error("Error while fetching grades for submission ID {}: {}", submissionId, e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }
}
