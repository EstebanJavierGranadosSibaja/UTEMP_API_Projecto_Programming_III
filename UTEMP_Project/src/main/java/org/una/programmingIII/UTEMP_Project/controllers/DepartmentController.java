package org.una.programmingIII.UTEMP_Project.controllers;

import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.ExampleObject;
import io.swagger.v3.oas.annotations.media.Schema;
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
import org.una.programmingIII.UTEMP_Project.dtos.CourseDTO;
import org.una.programmingIII.UTEMP_Project.services.department.DepartmentService;
import org.una.programmingIII.UTEMP_Project.exceptions.InvalidDataException;
import org.una.programmingIII.UTEMP_Project.exceptions.ResourceNotFoundException;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.Parameter;

import jakarta.validation.Valid;

@RestController
@RequestMapping("/utemp/departments")
public class DepartmentController {

    private static final Logger logger = LoggerFactory.getLogger(DepartmentController.class);
    private final DepartmentService departmentService;

    @Autowired
    public DepartmentController(DepartmentService departmentService) {
        this.departmentService = departmentService;
    }

    @Operation(
            summary = "Fetch all departments",
            description = """
        Returns a paginated list of all departments.
        This endpoint retrieves a list of departments in a paginated format,
        allowing clients to specify pagination parameters.
        """
    )
    @ApiResponses(value = {
            @ApiResponse(
                    responseCode = "200",
                    description = "Departments fetched successfully",
                    content = @Content(
                            mediaType = "application/json",
                            schema = @Schema(implementation = DepartmentDTO.class)
                    )
            ),
            @ApiResponse(
                    responseCode = "400",
                    description = "Invalid data for pagination",
                    content = @Content(
                            mediaType = "application/json",
                            examples = @ExampleObject(
                                    value = """
                                {
                                  "message": "Invalid pagination parameters.",
                                  "details": "Page size must be a positive number."
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
    @GetMapping
    @PreAuthorize("hasAuthority('GET_ALL_DEPARTMENTS')")
    public ResponseEntity<Page<DepartmentDTO>> getAllDepartments(Pageable pageable) {
        try {
            Page<DepartmentDTO> departments = departmentService.getAllDepartments(pageable);
            logger.info("Fetched all departments successfully.");
            return ResponseEntity.ok(departments);
        } catch (InvalidDataException e) {
            logger.error("Invalid data while fetching departments: {}", e.getMessage());
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(null);
        } catch (Exception e) {
            logger.error("Unexpected error while fetching departments: {}", e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(null);
        }
    }

    @Operation(
            summary = "Fetch department by ID",
            description = """
        Returns a department by its ID.
        This endpoint retrieves the details of a department specified
        by the provided ID. If the department does not exist, a
        corresponding error response will be returned.
        """
    )
    @ApiResponses(value = {
            @ApiResponse(
                    responseCode = "200",
                    description = "Department found",
                    content = @Content(
                            mediaType = "application/json",
                            schema = @Schema(implementation = DepartmentDTO.class)
                    )
            ),
            @ApiResponse(
                    responseCode = "404",
                    description = "Department not found",
                    content = @Content(
                            mediaType = "application/json",
                            examples = @ExampleObject(
                                    value = """
                                {
                                  "message": "Department not found.",
                                  "details": "No department found with ID 123."
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
    @PreAuthorize("hasAuthority('GET_DEPARTMENT_BY_ID')")
    public ResponseEntity<DepartmentDTO> getDepartmentById(
            @Parameter(description = "ID of the department to be fetched") @PathVariable Long id) {
        try {
            DepartmentDTO departmentDTO = departmentService.getDepartmentById(id)
                    .orElseThrow(() -> new ResourceNotFoundException("Department", id));
            logger.info("Fetched department with ID {} successfully.", id);
            return ResponseEntity.ok(departmentDTO);
        } catch (ResourceNotFoundException e) {
            logger.error("Department not found: {}", e.getMessage());
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(null);
        } catch (Exception e) {
            logger.error("Unexpected error while fetching department by ID {}: {}", id, e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(null);
        }
    }

    @Operation(
            summary = "Create a new department",
            description = """
        Creates a new department and returns the created department.
        This endpoint allows the creation of a new department within the
        system. The provided department data must be valid and complete
        according to the required fields.
        """
    )
    @ApiResponses(value = {
            @ApiResponse(
                    responseCode = "201",
                    description = "Department created successfully",
                    content = @Content(
                            mediaType = "application/json",
                            schema = @Schema(implementation = DepartmentDTO.class)
                    )
            ),
            @ApiResponse(
                    responseCode = "400",
                    description = "Invalid data for creating department",
                    content = @Content(
                            mediaType = "application/json",
                            examples = @ExampleObject(
                                    value = """
                                {
                                  "message": "Invalid data for creating department.",
                                  "details": "The name field is required."
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
    @PreAuthorize("hasAuthority('CREATE_DEPARTMENT')")
    public ResponseEntity<DepartmentDTO> createDepartment(@Valid @RequestBody DepartmentDTO departmentDTO) {
        try {
            DepartmentDTO createdDepartment = departmentService.createDepartment(departmentDTO);
            logger.info("Department created successfully: {}", createdDepartment);
            return ResponseEntity.status(HttpStatus.CREATED).body(createdDepartment);
        } catch (InvalidDataException e) {
            logger.error("Invalid data for creating department: {}", e.getMessage());
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(null);
        } catch (Exception e) {
            logger.error("Unexpected error while creating department: {}", e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(null);
        }
    }

    @Operation(
            summary = "Update an existing department",
            description = """
        Updates an existing department by its ID.
        This endpoint allows for modifying the details of a specific
        department. Ensure that the department ID exists before attempting
        to update its information.
        """
    )
    @ApiResponses(value = {
            @ApiResponse(
                    responseCode = "200",
                    description = "Department updated successfully",
                    content = @Content(
                            mediaType = "application/json",
                            schema = @Schema(implementation = DepartmentDTO.class)
                    )
            ),
            @ApiResponse(
                    responseCode = "404",
                    description = "Department not found",
                    content = @Content(
                            mediaType = "application/json",
                            examples = @ExampleObject(
                                    value = """
                                {
                                  "message": "Department not found.",
                                  "details": "No department exists with the provided ID."
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
    @PreAuthorize("hasAuthority('UPDATE_DEPARTMENT')")
    public ResponseEntity<DepartmentDTO> updateDepartment(@Parameter(description = "ID of the department to be updated") @PathVariable Long id,
                                                          @Valid @RequestBody DepartmentDTO departmentDTO) {
        try {
            DepartmentDTO updatedDepartment = departmentService.updateDepartment(id, departmentDTO)
                    .orElseThrow(() -> new ResourceNotFoundException("Department", id));
            logger.info("Department with ID {} updated successfully.", id);
            return ResponseEntity.ok(updatedDepartment);
        } catch (ResourceNotFoundException e) {
            logger.error("Department not found for update: {}", e.getMessage());
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(null);
        } catch (Exception e) {
            logger.error("Unexpected error while updating department ID {}: {}", id, e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(null);
        }
    }

    @Operation(
            summary = "Delete a department",
            description = """
        Deletes a department by its ID.
        This endpoint allows for the removal of a specific department
        from the system using its unique identifier. Ensure that the
        department ID exists before attempting to delete it.
        """
    )
    @ApiResponses(value = {
            @ApiResponse(
                    responseCode = "204",
                    description = "Department deleted successfully"
            ),
            @ApiResponse(
                    responseCode = "404",
                    description = "Department not found",
                    content = @Content(
                            mediaType = "application/json",
                            examples = @ExampleObject(
                                    value = """
                                {
                                  "message": "Department not found.",
                                  "details": "No department exists with the provided ID."
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
    @PreAuthorize("hasAuthority('DELETE_DEPARTMENT')")
    public ResponseEntity<Void> deleteDepartment(@Parameter(description = "ID of the department to be deleted") @PathVariable Long id) {
        try {
            departmentService.deleteDepartment(id);
            logger.info("Department with ID {} deleted successfully.", id);
            return ResponseEntity.noContent().build();
        } catch (ResourceNotFoundException e) {
            logger.error("Department not found for deletion: {}", e.getMessage());
            return ResponseEntity.status(HttpStatus.NOT_FOUND).build();
        } catch (Exception e) {
            logger.error("Unexpected error while deleting department ID {}: {}", id, e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }

    @Operation(
            summary = "Fetch departments by faculty ID",
            description = """
        Returns a paginated list of departments for a given faculty.
        This endpoint retrieves all departments associated with a specific
        faculty ID and supports pagination to manage large datasets.
        Ensure that the provided faculty ID is valid to receive a
        successful response.
        """
    )
    @ApiResponses(value = {
            @ApiResponse(
                    responseCode = "200",
                    description = "Departments for faculty found",
                    content = @Content(mediaType = "application/json", schema = @Schema(implementation = DepartmentDTO.class))
            ),
            @ApiResponse(
                    responseCode = "400",
                    description = "Invalid data for faculty ID",
                    content = @Content(
                            mediaType = "application/json",
                            examples = @ExampleObject(
                                    value = """
                                {
                                  "message": "Invalid faculty ID provided.",
                                  "details": "Faculty ID must be a positive integer."
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
                                  "details": "An unexpected error occurred."
                                }
                                """
                            )
                    )
            )
    })
    @GetMapping("/faculty/{facultyId}")
    @PreAuthorize("hasAuthority('GET_ALL_FACULTY_DEPARTMENT')")
    public ResponseEntity<Page<DepartmentDTO>> getDepartmentsByFacultyId(
            @Parameter(description = "ID of the faculty to retrieve departments from") @PathVariable Long facultyId,
            Pageable pageable) {
        try {
            Page<DepartmentDTO> departments = departmentService.getDepartmentsByFacultyId(facultyId, pageable);
            logger.info("Fetched departments for faculty ID {} successfully.", facultyId);
            return ResponseEntity.ok(departments);
        } catch (InvalidDataException e) {
            logger.error("Invalid data while fetching departments for faculty ID {}: {}", facultyId, e.getMessage());
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(null);
        } catch (Exception e) {
            logger.error("Unexpected error while fetching departments for faculty ID {}: {}", facultyId, e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(null);
        }
    }

    @Operation(
            summary = "Add a course to a department",
            description = """
        Adds a course to the specified department.
        This endpoint allows for the addition of a new course
        associated with a given department by providing the
        department ID and the course details.
        It is important to ensure that the provided
        course data is valid and that the department exists
        before attempting to add the course.
        """
    )
    @ApiResponses(value = {
            @ApiResponse(
                    responseCode = "201",
                    description = "Course added to department successfully"
            ),
            @ApiResponse(
                    responseCode = "400",
                    description = "Invalid data while adding course",
                    content = @Content(
                            mediaType = "application/json",
                            examples = @ExampleObject(
                                    value = """
                                {
                                  "message": "Invalid data for course creation",
                                  "details": "Course name cannot be empty."
                                }
                                """
                            )
                    )
            ),
            @ApiResponse(
                    responseCode = "404",
                    description = "Department not found for adding course",
                    content = @Content(
                            mediaType = "application/json",
                            examples = @ExampleObject(
                                    value = """
                                {
                                  "message": "Department not found",
                                  "details": "No department exists with ID 123."
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
                                  "details": "An unexpected error occurred."
                                }
                                """
                            )
                    )
            )
    })
    @PostMapping("/{departmentId}/courses")
    @PreAuthorize("hasAuthority('ADD_COURSE_TO_DEPARTMENT')")
    public ResponseEntity<Void> addCourseToDepartment(
            @Parameter(description = "ID of the department to which the course will be added") @PathVariable Long departmentId,
            @Valid @RequestBody CourseDTO courseDTO) {
        try {
            departmentService.addCourseToDepartment(departmentId, courseDTO);
            logger.info("Course added to department ID {} successfully.", departmentId);
            return ResponseEntity.status(HttpStatus.CREATED).build();
        } catch (InvalidDataException e) {
            logger.error("Invalid data while adding course to department: {}", e.getMessage());
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).build();
        } catch (ResourceNotFoundException e) {
            logger.error("Department not found for adding course: {}", e.getMessage());
            return ResponseEntity.status(HttpStatus.NOT_FOUND).build();
        } catch (Exception e) {
            logger.error("Unexpected error while adding course to department ID {}: {}", departmentId, e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }

    @Operation(
            summary = "Remove Course from Department",
            description = "This endpoint removes a specific course from a designated department. It requires the department ID and the course ID as path parameters. Successful execution of this operation will result in the course being disassociated from the department, effectively removing it from the department's course offerings. This action is typically restricted to users with managerial privileges over departments."
    )
    @ApiResponses(value = {
            @ApiResponse(
                    responseCode = "204",
                    description = "Course successfully removed from the department.",
                    content = @Content(
                            mediaType = "application/json",
                            examples = @ExampleObject(value = "{\"message\": \"Course successfully removed from the department.\"}")
                    )
            ),
            @ApiResponse(
                    responseCode = "404",
                    description = "Department or course not found.",
                    content = @Content(
                            mediaType = "application/json",
                            examples = @ExampleObject(value = "{\"message\": \"Department or course not found.\"}")
                    )
            ),
            @ApiResponse(
                    responseCode = "500",
                    description = "An error occurred while attempting to remove the course from the department.",
                    content = @Content(
                            mediaType = "application/json",
                            examples = @ExampleObject(value = "{\"message\": \"An error occurred while attempting to remove the course from the department.\"}")
                    )
            )
    })
    @DeleteMapping("/{departmentId}/courses/{courseId}")
    @PreAuthorize("hasAuthority('REMOVE_COURSE_TO_DEPARTMENT')")
    public ResponseEntity<Void> removeCourseFromDepartment(@PathVariable Long departmentId,
                                                           @PathVariable Long courseId) {
        try {
            departmentService.removeCourseFromDepartment(departmentId, courseId);
            return ResponseEntity.noContent().build();
        } catch (ResourceNotFoundException e) {
            logger.warn("Failed to remove course ID {} from department ID {}: {}", courseId, departmentId, e.getMessage());
            return ResponseEntity.status(HttpStatus.NOT_FOUND).build();
        } catch (Exception e) {
            logger.error("Error removing course ID {} from department ID {}: {}", courseId, departmentId, e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }
}