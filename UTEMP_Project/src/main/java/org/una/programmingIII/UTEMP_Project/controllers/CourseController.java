package org.una.programmingIII.UTEMP_Project.controllers;

import jakarta.validation.Valid;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
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

    @GetMapping
    public ResponseEntity<Page<CourseDTO>> getAllCourses(Pageable pageable) {
        try {
            Page<CourseDTO> courses = courseService.getAllCourses(pageable);
            return ResponseEntity.ok(courses);
        } catch (Exception e) {
            logger.error("Error fetching all courses: {}", e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(null);
        }
    }

    @GetMapping("/{id}")
    public ResponseEntity<CourseDTO> getCourseById(@PathVariable Long id) {
        try {
            Optional<CourseDTO> courseDTO = courseService.getCourseById(id);
            return courseDTO
                    .map(ResponseEntity::ok)
                    .orElseThrow(() -> new ResourceNotFoundException("Course", id));
        } catch (ResourceNotFoundException e) {
            logger.error("Course not found with ID {}: {}", id, e.getMessage());
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(null);
        } catch (Exception e) {
            logger.error("Unexpected error retrieving course with ID {}: {}", id, e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(null);
        }
    }

    @PostMapping
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

    @PutMapping("/{id}")
    public ResponseEntity<CourseDTO> updateCourse(@PathVariable Long id,
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

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteCourse(@PathVariable Long id) {
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

    @GetMapping("/teacher/{teacherId}")
    public ResponseEntity<Page<CourseDTO>> getCoursesByTeacherId(@PathVariable Long teacherId,
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

    @GetMapping("/department/{departmentId}")
    public ResponseEntity<Page<CourseDTO>> getCoursesByDepartmentId(@PathVariable Long departmentId,
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
            logger.error("Unexpected error while fetching courses for department ID {}: {}", departmentId, e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(null);
        }
    }

    @PostMapping("/{courseId}/assignments")
    public ResponseEntity<Void> addAssignmentToCourse(@PathVariable Long courseId,
                                                      @Valid @RequestBody AssignmentDTO assignmentDTO) {
        try {
            courseService.addAssignmentToCourse(courseId, assignmentDTO);
            return ResponseEntity.status(HttpStatus.CREATED).build();
        } catch (ResourceNotFoundException e) {
            logger.error("Course not found with ID {}: {}", courseId, e.getMessage());
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(null);
        } catch (InvalidDataException e) {
            logger.error("Invalid data for assignment in course ID {}: {}", courseId, e.getMessage());
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(null);
        } catch (Exception e) {
            logger.error("Unexpected error while adding assignment to course ID {}: {}", courseId, e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(null);
        }
    }

    @DeleteMapping("/{courseId}/assignments/{assignmentId}")
    public ResponseEntity<Void> removeAssignmentFromCourse(@PathVariable Long courseId,
                                                           @PathVariable Long assignmentId) {
        try {
            courseService.removeAssignmentFromCourse(courseId, assignmentId);
            return ResponseEntity.noContent().build();
        } catch (ResourceNotFoundException e) {
            logger.error("Course or assignment not found with IDs {} and {}: {}", courseId, assignmentId, e.getMessage());
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(null);
        } catch (InvalidDataException e) {
            logger.error("Invalid data for removing assignment with ID {} from course ID {}: {}", assignmentId, courseId, e.getMessage());
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(null);
        } catch (Exception e) {
            logger.error("Unexpected error while removing assignment with ID {} from course ID {}: {}", assignmentId, courseId, e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(null);
        }
    }
}
