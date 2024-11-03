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
import org.una.programmingIII.UTEMP_Project.dtos.CourseDTO;
import org.una.programmingIII.UTEMP_Project.exceptions.InvalidDataException;
import org.una.programmingIII.UTEMP_Project.exceptions.ResourceNotFoundException;
import org.una.programmingIII.UTEMP_Project.services.course.CourseService;

import java.util.Optional;

@RestController
@RequestMapping("/utemp/courses")
public class CourseController {

    private static final Logger logger = LoggerFactory.getLogger(CourseController.class);
    private final CourseService courseService;

    @Autowired
    public CourseController(CourseService courseService) {
        this.courseService = courseService;
    }

    @Operation(
            summary = "Get all courses",
            description = """
                    Retrieve a paginated list of all courses.
                    This endpoint provides a way to access all courses
                    available in the system, supporting pagination to
                    handle large datasets. The client can specify
                    the page number and size in the request to
                    retrieve a subset of the complete list.
                    """
    )
    @ApiResponses(value = {
            @ApiResponse(
                    responseCode = "200",
                    description = "Successfully retrieved courses",
                    content = @Content(
                            mediaType = "application/json",
                            schema = @Schema(implementation = CourseDTO.class)
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
                                              "details": "An unexpected error occurred while fetching courses"
                                            }
                                            """
                            )
                    )
            )
    })
    @GetMapping
    @PreAuthorize("hasAuthority('GET_ALL_COURSES')")
    public ResponseEntity<Page<CourseDTO>> getAllCourses(Pageable pageable) {
        try {
            Page<CourseDTO> courses = courseService.getAllCourses(pageable);
            return ResponseEntity.ok(courses);
        } catch (Exception e) {
            logger.error("Error fetching all courses: {}", e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(null);
        }
    }

    @Operation(
            summary = "Get course by ID",
            description = """
                    Retrieve a course by its ID.
                    This endpoint allows clients to request details of a
                    specific course using its unique identifier. If the course
                    is found, a 200 OK response will be returned along with
                    the course details. If the course does not exist, a
                    404 Not Found error will be returned.
                    Any unexpected errors will result in a 500 Internal
                    Server Error response.
                    """
    )
    @ApiResponses(value = {
            @ApiResponse(
                    responseCode = "200",
                    description = "Successfully retrieved course",
                    content = @Content(
                            mediaType = "application/json",
                            schema = @Schema(implementation = CourseDTO.class)
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
                                              "details": "No course exists with ID 123"
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
                                              "details": "An unexpected error occurred while trying to retrieve the course"
                                            }
                                            """
                            )
                    )
            )
    })
    @GetMapping("/{id}")
    @PreAuthorize("hasAuthority('GET_COURSE_BY_ID')")
    public ResponseEntity<CourseDTO> getCourseById(@Parameter(description = "ID of the course to be retrieved") @PathVariable Long id) {
        try {
            Optional<CourseDTO> courseDTO = courseService.getCourseById(id);
            return courseDTO
                    .map(ResponseEntity::ok)
                    .orElseThrow(() -> new ResourceNotFoundException("Course", id));
        } catch (ResourceNotFoundException e) {
            logger.error("1Course not found with ID {}: {}", id, e.getMessage());
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(null);
        } catch (Exception e) {
            logger.error("Unexpected error retrieving course with ID {}: {}", id, e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(null);
        }
    }

    @Operation(
            summary = "Create course",
            description = """
                    Create a new course.
                    This endpoint allows clients to submit details for a
                    new course to be added to the system. The required
                    course information must be provided in the request body.
                    Upon successful creation, a 201 Created response will be
                    returned along with the created course data. If the data
                    provided is invalid, a 400 Bad Request error will be returned.
                    Additionally, for any unexpected errors, a 500 Internal
                    Server Error response will be issued.
                    """
    )
    @ApiResponses(value = {
            @ApiResponse(
                    responseCode = "201",
                    description = "Course created successfully",
                    content = @Content(
                            mediaType = "application/json",
                            schema = @Schema(implementation = CourseDTO.class)
                    )
            ),
            @ApiResponse(
                    responseCode = "400",
                    description = "Invalid data for creating course",
                    content = @Content(
                            mediaType = "application/json",
                            examples = @ExampleObject(
                                    value = """
                                            {
                                              "message": "Invalid course data",
                                              "details": "Course title is required"
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
                                              "details": "An unexpected error occurred while trying to create the course"
                                            }
                                            """
                            )
                    )
            )
    })
    @PostMapping
    @PreAuthorize("hasAuthority('CREATE_COURSE')")
    public ResponseEntity<CourseDTO> createCourse(@Valid @RequestBody CourseDTO courseDTO) {
        try {
            CourseDTO createdCourse = courseService.createCourse(courseDTO);
            return ResponseEntity.status(HttpStatus.CREATED).body(createdCourse);
        } catch (InvalidDataException e) {
            logger.error("Invalid data for creating course: {}", e.getMessage());
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(null);
        } catch (Exception e) {
            logger.error("Unexpected error creating course: {}", e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(null);
        }
    }

    @Operation(
            summary = "Update course",
            description = """
                    Update an existing course by ID.
                    This endpoint allows clients to modify the details of a
                    specific course using its unique identifier. The updated
                    course information must be provided in the request body.
                    On success, a 200 OK response will be returned along with
                    the updated course data. If the course does not exist, a
                    404 Not Found error will be issued. Additionally, if the
                    provided data is invalid, a 400 Bad Request error will be returned,
                    and for any unexpected errors, a 500 Internal Server Error response
                    will be issued.
                    """
    )
    @ApiResponses(value = {
            @ApiResponse(
                    responseCode = "200",
                    description = "Course updated successfully",
                    content = @Content(
                            mediaType = "application/json",
                            schema = @Schema(implementation = CourseDTO.class)
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
                                              "details": "No course found with ID 10"
                                            }
                                            """
                            )
                    )
            ),
            @ApiResponse(
                    responseCode = "400",
                    description = "Invalid data for updating course",
                    content = @Content(
                            mediaType = "application/json",
                            examples = @ExampleObject(
                                    value = """
                                            {
                                              "message": "Invalid course data",
                                              "details": "Title cannot be empty"
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
                                              "details": "An unexpected error occurred while trying to update the course"
                                            }
                                            """
                            )
                    )
            )
    })
    @PutMapping("/{id}")
    @PreAuthorize("hasAuthority('UPDATE_COURSE')")
    public ResponseEntity<CourseDTO> updateCourse(
            @Parameter(
                    description = "ID of the course to be updated",
                    required = true,
                    example = "10"
            )
            @PathVariable Long id,
            @Valid @RequestBody CourseDTO courseDTO) {
        try {
            Optional<CourseDTO> updatedCourse = courseService.updateCourse(id, courseDTO);
            return updatedCourse
                    .map(ResponseEntity::ok)
                    .orElseThrow(() -> new ResourceNotFoundException("Course", id));
        } catch (ResourceNotFoundException e) {
            logger.error("Course not found with ID {}: {}", id, e.getMessage());
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(null);
        } catch (InvalidDataException e) {
            logger.error("Invalid data for updating course ID {}: {}", id, e.getMessage());
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(null);
        } catch (Exception e) {
            logger.error("Unexpected error updating course ID {}: {}", id, e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(null);
        }
    }

    @Operation(
            summary = "Delete course",
            description = """
                    Delete a course by ID.
                    This endpoint allows clients to remove a course from the system
                    using its unique identifier. If the specified course is successfully
                    deleted, a 204 No Content response will be returned.
                    If the course does not exist, a 404 Not Found error will be issued.
                    Additionally, if there is an unexpected error during the deletion process,
                    a 500 Internal Server Error response will be provided.
                    """
    )
    @ApiResponses(value = {
            @ApiResponse(
                    responseCode = "204",
                    description = "Course deleted successfully"
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
                                              "details": "No course found with ID 100"
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
                                              "details": "An unexpected error occurred while trying to delete the course"
                                            }
                                            """
                            )
                    )
            )
    })
    @DeleteMapping("/{id}")
    @PreAuthorize("hasAuthority('DELETE_COURSE')")
    public ResponseEntity<Void> deleteCourse(
            @Parameter(
                    description = "ID of the course to be deleted",
                    required = true,
                    example = "10"
            )
            @PathVariable Long id) {
        try {
            courseService.deleteCourse(id);
            return ResponseEntity.noContent().build();
        } catch (ResourceNotFoundException e) {
            logger.error("Error deleting course with ID {}: {}", id, e.getMessage());
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(null);
        } catch (Exception e) {
            logger.error("Unexpected error deleting course with ID {}: {}", id, e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(null);
        }
    }


    @Operation(
            summary = "Get courses by teacher ID",
            description = """
                    Retrieve a paginated list of courses taught by a specific teacher.
                    This endpoint allows clients to fetch all courses associated with
                    a given teacher ID. The response will include pagination details
                    to facilitate navigation through potentially large datasets.
                    If the specified teacher does not exist, a 404 Not Found error
                    will be returned. Additionally, if the request contains invalid
                    teacher ID data, appropriate error responses will be issued.
                    """
    )
    @ApiResponses(value = {
            @ApiResponse(
                    responseCode = "200",
                    description = "Successfully retrieved courses",
                    content = @Content(
                            mediaType = "application/json",
                            schema = @Schema(implementation = Page.class), // Cambia a Page si es un Page<CourseDTO>
                            examples = @ExampleObject(
                                    value = """
                                            {
                                              "content": [
                                                {
                                                  "id": 1,
                                                  "title": "Course Title 1",
                                                  "description": "Description of Course 1.",
                                                  "teacherId": 5,
                                                  "credits": 3
                                                },
                                                {
                                                  "id": 2,
                                                  "title": "Course Title 2",
                                                  "description": "Description of Course 2.",
                                                  "teacherId": 5,
                                                  "credits": 4
                                                }
                                              ],
                                              "pageable": {
                                                "sort": {
                                                  "sorted": true,
                                                  "unsorted": false,
                                                  "empty": false
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
                    responseCode = "404",
                    description = "Teacher not found",
                    content = @Content(
                            mediaType = "application/json",
                            examples = @ExampleObject(
                                    value = """
                                            {
                                              "message": "Teacher not found",
                                              "details": "No teacher found with ID 100"
                                            }
                                            """
                            )
                    )
            ),
            @ApiResponse(
                    responseCode = "400",
                    description = "Invalid data for teacher ID",
                    content = @Content(
                            mediaType = "application/json",
                            examples = @ExampleObject(
                                    value = """
                                            {
                                              "message": "Invalid teacher ID",
                                              "details": "Teacher ID must be a positive integer"
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
                                              "details": "An unexpected error occurred"
                                            }
                                            """
                            )
                    )
            )
    })
    @GetMapping("/teacher/{teacherId}")
    @PreAuthorize("hasAuthority('GET_ALL_COURSES_OF_TEACHER')")
    public ResponseEntity<Page<CourseDTO>> getCoursesByTeacherId(
            @Parameter(
                    description = "ID of the teacher",
                    required = true,
                    example = "5"
            )
            @PathVariable Long teacherId,
            Pageable pageable) {
        try {
            Page<CourseDTO> courses = courseService.getCoursesByTeacherId(teacherId, pageable);
            return ResponseEntity.ok(courses);
        } catch (ResourceNotFoundException e) {
            logger.error("Teacher not found with ID {}: {}", teacherId, e.getMessage());
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(null);
        } catch (InvalidDataException e) {
            logger.error("Invalid data for teacher ID {}: {}", teacherId, e.getMessage());
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(null);
        } catch (Exception e) {
            logger.error("Unexpected error fetching courses for teacher ID {}: {}", teacherId, e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(null);
        }
    }

    @Operation(
            summary = "Get courses by department ID",
            description = """
                    Retrieve a paginated list of courses in a specific department.
                    This endpoint allows clients to fetch all courses associated with
                    a given department ID. The response will include pagination details
                    to help clients navigate through potentially large datasets.
                    If the specified department does not exist, a 404 Not Found error
                    will be returned. In case of invalid department ID format or
                    other internal errors, appropriate error responses will be issued.
                    """
    )
    @ApiResponses(value = {
            @ApiResponse(
                    responseCode = "200",
                    description = "Successfully retrieved courses",
                    content = @Content(
                            mediaType = "application/json",
                            schema = @Schema(implementation = Page.class), // Cambia a Page si es un Page<CourseDTO>
                            examples = @ExampleObject(
                                    value = """
                                            {
                                              "content": [
                                                {
                                                  "id": 1,
                                                  "title": "Course Title 1",
                                                  "description": "Description of Course 1.",
                                                  "departmentId": 10,
                                                  "credits": 3
                                                },
                                                {
                                                  "id": 2,
                                                  "title": "Course Title 2",
                                                  "description": "Description of Course 2.",
                                                  "departmentId": 10,
                                                  "credits": 4
                                                }
                                              ],
                                              "pageable": {
                                                "sort": {
                                                  "sorted": true,
                                                  "unsorted": false,
                                                  "empty": false
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
                    responseCode = "404",
                    description = "Department not found",
                    content = @Content(
                            mediaType = "application/json",
                            examples = @ExampleObject(
                                    value = """
                                            {
                                              "message": "Department not found",
                                              "details": "No department found with ID 100"
                                            }
                                            """
                            )
                    )
            ),
            @ApiResponse(
                    responseCode = "400",
                    description = "Invalid data for department ID",
                    content = @Content(
                            mediaType = "application/json",
                            examples = @ExampleObject(
                                    value = """
                                            {
                                              "message": "Invalid department ID",
                                              "details": "Department ID must be a positive integer"
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
                                              "details": "An unexpected error occurred"
                                            }
                                            """
                            )
                    )
            )
    })
    @GetMapping("/department/{departmentId}")
    @PreAuthorize("hasAuthority('GET_ALL_DEPARTMENT_COURSES')")
    public ResponseEntity<Page<CourseDTO>> getCoursesByDepartmentId(
            @Parameter(
                    description = "ID of the department",
                    required = true,
                    example = "10"
            )
            @PathVariable Long departmentId,
            Pageable pageable) {
        try {
            Page<CourseDTO> courses = courseService.getCoursesByDepartmentId(departmentId, pageable);
            return ResponseEntity.ok(courses);
        } catch (ResourceNotFoundException e) {
            logger.error("Department not found with ID {}: {}", departmentId, e.getMessage());
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(null);
        } catch (InvalidDataException e) {
            logger.error("Invalid data for department ID {}: {}", departmentId, e.getMessage());
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(null);
        } catch (Exception e) {
            logger.error("Unexpected error fetching courses for department ID {}: {}", departmentId, e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(null);
        }
    }

    @Operation(
            summary = "Add Assignment to Course",
            description = "This endpoint allows users to add a new assignment to a specific course. The course ID is provided as a path parameter, while the details of the assignment are sent in the request body as an AssignmentDTO. Successful execution of this operation will result in the assignment being linked to the specified course."
    )
    @ApiResponses(value = {
            @ApiResponse(
                    responseCode = "201",
                    description = "Assignment successfully added to the course.",
                    content = @Content(
                            mediaType = "application/json",
                            examples = @ExampleObject(value = "{\"message\": \"Assignment successfully added to the course.\"}")
                    )
            ),
            @ApiResponse(
                    responseCode = "404",
                    description = "Course not found. The specified course ID does not correspond to any existing course.",
                    content = @Content(
                            mediaType = "application/json",
                            examples = @ExampleObject(value = "{\"message\": \"Course not found.\"}")
                    )
            ),
            @ApiResponse(
                    responseCode = "400",
                    description = "Invalid data provided for the assignment. This could include missing fields or incorrect data types.",
                    content = @Content(
                            mediaType = "application/json",
                            examples = @ExampleObject(value = "{\"message\": \"Invalid data provided for the assignment.\"}")
                    )
            ),
            @ApiResponse(
                    responseCode = "500",
                    description = "An error occurred while adding the assignment to the course, indicating a server-side issue.",
                    content = @Content(
                            mediaType = "application/json",
                            examples = @ExampleObject(value = "{\"message\": \"An error occurred while adding the assignment to the course.\"}")
                    )
            )
    })
    @PostMapping("/{courseId}/assignments")
    @PreAuthorize("hasAuthority('ADD_ASSIGNMENT_TO_COURSE')")
    public ResponseEntity<Void> addAssignmentToCourse(@PathVariable Long courseId,
                                                      @RequestBody AssignmentDTO assignmentDTO) {
        try {
            courseService.addAssignmentToCourse(courseId, assignmentDTO);
            return ResponseEntity.status(HttpStatus.CREATED).build(); // 201 Created response on successful addition
        } catch (ResourceNotFoundException e) {
            logger.warn("Failed to add assignment to course ID {}: {}", courseId, e.getMessage());
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(null);
        } catch (InvalidDataException e) {
            logger.error("Invalid data for assignment when adding to course ID {}: {}", courseId, e.getMessage());
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(null);
        } catch (Exception e) {
            logger.error("Error adding assignment to course ID {}: {}", courseId, e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(null);
        }
    }

    @Operation(
            summary = "Remove Assignment from Course",
            description = "This endpoint allows users to remove a specific assignment from a designated course. The IDs for both the course and the assignment to be removed are provided as path parameters. Upon successful execution, the assignment will no longer be associated with the course."
    )
    @ApiResponses(value = {
            @ApiResponse(
                    responseCode = "204",
                    description = "Assignment successfully removed from the course. No content is returned as confirmation.",
                    content = @Content(
                            mediaType = "application/json",
                            examples = @ExampleObject(value = "{\"message\": \"Assignment successfully removed from the course.\"}")
                    )
            ),
            @ApiResponse(
                    responseCode = "404",
                    description = "Course or assignment not found. This indicates that either the specified course ID or assignment ID does not exist.",
                    content = @Content(
                            mediaType = "application/json",
                            examples = @ExampleObject(value = "{\"message\": \"Course or assignment not found.\"}")
                    )
            ),
            @ApiResponse(
                    responseCode = "500",
                    description = "An error occurred while removing the assignment from the course. This indicates a server-side issue that prevented the operation from completing successfully.",
                    content = @Content(
                            mediaType = "application/json",
                            examples = @ExampleObject(value = "{\"message\": \"An error occurred while removing the assignment from the course.\"}")
                    )
            )
    })
    @DeleteMapping("/{courseId}/assignments/{assignmentId}")
    @PreAuthorize("hasAuthority('REMOVE_ASSIGNMENT_TO_COURSE')")
    public ResponseEntity<Void> removeAssignmentFromCourse(@PathVariable Long courseId,
                                                           @PathVariable Long assignmentId) {
        try {
            courseService.removeAssignmentFromCourse(courseId, assignmentId);
            return ResponseEntity.noContent().build();
        } catch (ResourceNotFoundException e) {
            logger.warn("Failed to remove assignment ID {} from course ID {}: {}", assignmentId, courseId, e.getMessage());
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(null);
        } catch (Exception e) {
            logger.error("Error removing assignment ID {} from course ID {}: {}", assignmentId, courseId, e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(null);
        }
    }
}