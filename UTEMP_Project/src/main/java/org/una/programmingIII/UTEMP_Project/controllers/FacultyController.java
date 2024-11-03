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
import org.una.programmingIII.UTEMP_Project.dtos.DepartmentDTO;
import org.una.programmingIII.UTEMP_Project.dtos.FacultyDTO;
import org.una.programmingIII.UTEMP_Project.exceptions.InvalidDataException;
import org.una.programmingIII.UTEMP_Project.exceptions.ResourceNotFoundException;
import org.una.programmingIII.UTEMP_Project.services.faculty.FacultyService;

@RestController
@RequestMapping("/utemp/faculties")
public class FacultyController {

    private static final Logger logger = LoggerFactory.getLogger(FacultyController.class);
    private final FacultyService facultyService;

    @Autowired
    public FacultyController(FacultyService facultyService) {
        this.facultyService = facultyService;
    }

    @Operation(
            summary = "Get all faculties",
            description = """
                    Retrieve a paginated list of faculties.
                    This endpoint allows clients to obtain a comprehensive list of all faculties available in the system,
                    along with their associated universities and departments,
                    and supports pagination to manage large datasets effectively.
                    """
    )
    @ApiResponses(value = {
            @ApiResponse(
                    responseCode = "200",
                    description = "Faculties retrieved successfully",
                    content = @Content(
                            mediaType = "application/json",
                            schema = @Schema(implementation = FacultyDTO.class)
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
    @PreAuthorize("hasAuthority('GET_ALL_FACULTIES')")
    public ResponseEntity<Page<FacultyDTO>> getAllFaculties(Pageable pageable) {
        try {
            Page<FacultyDTO> faculties = facultyService.getAllFaculties(pageable);
            return ResponseEntity.ok(faculties);
        } catch (Exception e) {
            logger.error("Error retrieving faculties: {}", e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }

    @Operation(
            summary = "Get faculty by ID",
            description = """
                    Retrieve a faculty by its unique identifier.
                    This endpoint allows clients to obtain detailed information about a specific faculty,
                    including its name, associated university, and departments.
                    If the faculty is not found, a 404 Not Found response is returned.
                    """
    )
    @ApiResponses(value = {
            @ApiResponse(
                    responseCode = "200",
                    description = "Faculty retrieved successfully",
                    content = @Content(
                            mediaType = "application/json",
                            schema = @Schema(implementation = FacultyDTO.class)
                    )
            ),
            @ApiResponse(
                    responseCode = "404",
                    description = "Faculty not found",
                    content = @Content(
                            mediaType = "application/json",
                            examples = @ExampleObject(
                                    value = """
                                            {
                                              "message": "Faculty not found",
                                              "details": "No faculty record exists with the specified ID."
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
    @PreAuthorize("hasAuthority('GET_FACULTY_BY_ID')")
    public ResponseEntity<FacultyDTO> getFacultyById(@Parameter(description = "ID of the faculty to retrieve") @PathVariable Long id) {
        try {
            return facultyService.getFacultyById(id)
                    .map(ResponseEntity::ok)
                    .orElseGet(() -> {
                        logger.warn("Faculty not found with id: {}", id);
                        return ResponseEntity.status(HttpStatus.NOT_FOUND).build();
                    });
        } catch (Exception e) {
            logger.error("Error retrieving faculty with id {}: {}", id, e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }

    @Operation(
            summary = "Create a new faculty",
            description = """
                    Create a new faculty with the provided details.
                    This endpoint allows for adding a new faculty to the system.
                    Ensure that all required fields are included in the request body to avoid validation errors.
                    """
    )
    @ApiResponses(value = {
            @ApiResponse(
                    responseCode = "201",
                    description = "Faculty created successfully",
                    content = @Content(
                            mediaType = "application/json",
                            schema = @Schema(implementation = FacultyDTO.class)
                    )
            ),
            @ApiResponse(
                    responseCode = "400",
                    description = "Invalid data provided",
                    content = @Content(
                            mediaType = "application/json",
                            examples = @ExampleObject(
                                    value = """
                                            {
                                              "message": "Invalid data",
                                              "details": "The faculty information provided is not valid."
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
    @PreAuthorize("hasAuthority('CREATE_FACULTY')")
    public ResponseEntity<FacultyDTO> createFaculty(@Valid @RequestBody FacultyDTO facultyDTO) {
        try {
            FacultyDTO createdFaculty = facultyService.createFaculty(facultyDTO);
            return ResponseEntity.status(HttpStatus.CREATED).body(createdFaculty);
        } catch (InvalidDataException e) {
            logger.error("Invalid data while creating faculty: {}", e.getMessage());
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).build();
        } catch (Exception e) {
            logger.error("Error creating faculty: {}", e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }

    @Operation(
            summary = "Update an existing faculty",
            description = """
                    Update the details of an existing faculty.
                    This endpoint allows for modifying faculty information by its ID.
                    Ensure that the faculty ID is valid and that the request body contains all necessary details for the update.
                    """
    )
    @ApiResponses(value = {
            @ApiResponse(
                    responseCode = "200",
                    description = "Faculty updated successfully",
                    content = @Content(
                            mediaType = "application/json",
                            schema = @Schema(implementation = FacultyDTO.class)
                    )
            ),
            @ApiResponse(
                    responseCode = "400",
                    description = "Invalid data for updating faculty",
                    content = @Content(
                            mediaType = "application/json",
                            examples = @ExampleObject(
                                    value = """
                                            {
                                              "message": "Invalid data",
                                              "details": "The faculty information provided is not valid."
                                            }
                                            """
                            )
                    )
            ),
            @ApiResponse(
                    responseCode = "404",
                    description = "Faculty not found",
                    content = @Content(
                            mediaType = "application/json",
                            examples = @ExampleObject(
                                    value = """
                                            {
                                              "message": "Resource not found",
                                              "details": "Faculty with id {id} does not exist."
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
    @PreAuthorize("hasAuthority('UPDATE_FACULTY')")
    public ResponseEntity<FacultyDTO> updateFaculty(@PathVariable Long id,
                                                    @Valid @RequestBody FacultyDTO facultyDTO) {
        try {
            return facultyService.updateFaculty(id, facultyDTO)
                    .map(ResponseEntity::ok)
                    .orElseGet(() -> {
                        logger.warn("Faculty not found for update with id: {}", id);
                        return ResponseEntity.status(HttpStatus.NOT_FOUND).build();
                    });
        } catch (InvalidDataException e) {
            logger.error("Invalid data while updating faculty: {}", e.getMessage());
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).build();
        } catch (Exception e) {
            logger.error("Error updating faculty with id {}: {}", id, e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }

    @Operation(
            summary = "Delete a faculty",
            description = """
                    Delete a faculty by its ID.
                    This endpoint will remove a faculty from the database.
                    Make sure to provide a valid faculty ID.
                    If the faculty does not exist, a 404 status will be returned.
                    """
    )
    @ApiResponses(value = {
            @ApiResponse(
                    responseCode = "204",
                    description = "Faculty deleted successfully"
            ),
            @ApiResponse(
                    responseCode = "404",
                    description = "Faculty not found",
                    content = @Content(
                            mediaType = "application/json",
                            examples = @ExampleObject(
                                    value = """
                                            {
                                              "message": "Resource not found",
                                              "details": "Faculty with id {id} does not exist."
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
    @PreAuthorize("hasAuthority('DELETE_FACULTY')")
    public ResponseEntity<Void> deleteFaculty(@PathVariable Long id) {
        try {
            facultyService.deleteFaculty(id);
            return ResponseEntity.noContent().build();
        } catch (ResourceNotFoundException e) {
            logger.warn("Faculty not found for deletion with id: {}", id);
            return ResponseEntity.status(HttpStatus.NOT_FOUND).build();
        } catch (Exception e) {
            logger.error("Error deleting faculty with id {}: {}", id, e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }

    @Operation(
            summary = "Get faculties by university ID",
            description = """
                    Retrieve a paginated list of faculties associated with a university.
                    This endpoint allows fetching faculties by specifying the university ID.
                    """
    )
    @ApiResponses(value = {
            @ApiResponse(
                    responseCode = "200",
                    description = "List of faculties retrieved successfully",
                    content = @Content(
                            mediaType = "application/json",
                            schema = @Schema(implementation = FacultyDTO.class)
                    )
            ),
            @ApiResponse(
                    responseCode = "404",
                    description = "University not found",
                    content = @Content(
                            mediaType = "application/json",
                            examples = @ExampleObject(
                                    value = """
                                            {
                                              "message": "University not found",
                                              "details": "No university exists with the provided ID."
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
    @GetMapping("/university/{universityId}")
    @PreAuthorize("hasAuthority('GET_ALL_UNIVERSITY_FACILITIES')")
    public ResponseEntity<Page<FacultyDTO>> getFacultiesByUniversityId(@PathVariable Long universityId,
                                                                       Pageable pageable) {
        try {
            Page<FacultyDTO> faculties = facultyService.getFacultiesByUniversityId(universityId, pageable);
            return ResponseEntity.ok(faculties);
        } catch (Exception e) {
            logger.error("Error retrieving faculties for university {}: {}", universityId, e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(null);
        }
    }

    @Operation(
            summary = "Add a department to a faculty",
            description = """
                    Associate a department with a faculty.
                    This endpoint allows adding a new department to a specific faculty by its ID.
                    If the department is successfully added, a 201 Created response is returned.
                    In case of invalid data, a 400 Bad Request response is returned.
                    In case of an internal error, a 500 Internal Server Error response is returned.
                    """
    )
    @ApiResponses(value = {
            @ApiResponse(
                    responseCode = "201",
                    description = "Department added successfully to the faculty"
            ),
            @ApiResponse(
                    responseCode = "400",
                    description = "Invalid data for adding department",
                    content = @Content(
                            mediaType = "application/json",
                            examples = @ExampleObject(
                                    value = """
                                            {
                                              "message": "Invalid data",
                                              "details": "The department information provided is not valid."
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
    @PostMapping("/{facultyId}/departments")
    @PreAuthorize("hasAuthority('ADD_DEPARTMENT_TO_FACULTY')")
    public ResponseEntity<Void> addDepartmentToFaculty(@PathVariable Long facultyId,
                                                       @Valid @RequestBody DepartmentDTO departmentDTO) {
        try {
            facultyService.addDepartmentToFaculty(facultyId, departmentDTO);
            return ResponseEntity.status(HttpStatus.CREATED).build();
        } catch (InvalidDataException e) {
            logger.error("Invalid data while adding department to faculty: {}", e.getMessage());
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).build();
        } catch (Exception e) {
            logger.error("Error adding department to faculty with id {}: {}", facultyId, e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }

    @Operation(
            summary = "Remove a department from a faculty",
            description = """
                    Dissociate a department from a faculty.
                    This endpoint allows the removal of a department from a specific faculty by its IDs
                    If the department is successfully removed, a 204 No Content response is returned.
                    In case the faculty or department is not found, a 404 Not Found response is returned.
                    """
    )
    @ApiResponses(value = {
            @ApiResponse(
                    responseCode = "204",
                    description = "Department removed successfully from the faculty"
            ),
            @ApiResponse(
                    responseCode = "404",
                    description = "Faculty or department not found",
                    content = @Content(
                            mediaType = "application/json",
                            examples = @ExampleObject(
                                    value = """
                                            {
                                              "message": "Resource not found",
                                              "details": "Department with id {departmentId} does not exist in faculty with id {facultyId}."
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
    @DeleteMapping("/{facultyId}/departments/{departmentId}")
    @PreAuthorize("hasAuthority('REMOVE_DEPARTMENT_TO_FACULTY')")
    public ResponseEntity<Void> removeDepartmentFromFaculty(@PathVariable Long facultyId,
                                                            @PathVariable Long departmentId) {
        try {
            facultyService.removeDepartmentFromFaculty(facultyId, departmentId);
            return ResponseEntity.noContent().build();
        } catch (ResourceNotFoundException e) {
            logger.warn("Department not found for removal from faculty with id: {} and departmentId: {}", facultyId, departmentId);
            return ResponseEntity.status(HttpStatus.NOT_FOUND).build();
        } catch (Exception e) {
            logger.error("Error removing department from faculty with id {}: {}", facultyId, e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }
}