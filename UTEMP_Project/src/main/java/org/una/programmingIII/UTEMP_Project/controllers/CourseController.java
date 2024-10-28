package org.una.programmingIII.UTEMP_Project.controllers;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.una.programmingIII.UTEMP_Project.dtos.AssignmentDTO;
import org.una.programmingIII.UTEMP_Project.dtos.CourseDTO;
import org.una.programmingIII.UTEMP_Project.services.course.CourseService;
import org.una.programmingIII.UTEMP_Project.exceptions.ResourceNotFoundException;
import org.una.programmingIII.UTEMP_Project.exceptions.InvalidDataException;
import org.una.programmingIII.UTEMP_Project.exceptions.ResourceAlreadyExistsException;

import jakarta.validation.Valid;
import java.util.Optional;

@RestController
@RequestMapping("/utemp/courses")
public class CourseController {

    @Autowired
    private CourseService courseService;

    @GetMapping
    public ResponseEntity<Page<CourseDTO>> getAllCourses(Pageable pageable) {
        Page<CourseDTO> courses = courseService.getAllCourses(pageable);
        return new ResponseEntity<>(courses, HttpStatus.OK);
    }

    @GetMapping("/{id}")
    public ResponseEntity<CourseDTO> getCourseById(@PathVariable Long id) {
        try {
            Optional<CourseDTO> course = courseService.getCourseById(id);
            return course.map(ResponseEntity::ok)
                    .orElseThrow(() -> new ResourceNotFoundException("Course not found with id: " , id));
        } catch (ResourceNotFoundException e) {
            throw e;
        } catch (Exception e) {
            throw new InvalidDataException("Error retrieving course: " + e.getMessage());
        }
    }

    @PostMapping
    public ResponseEntity<CourseDTO> createCourse(@Valid @RequestBody CourseDTO courseDTO) {
        try {
            CourseDTO createdCourse = courseService.createCourse(courseDTO);
            return new ResponseEntity<>(createdCourse, HttpStatus.CREATED);
        } catch (ResourceAlreadyExistsException e) {
            throw e;
        } catch (Exception e) {
            throw new InvalidDataException("Error creating course: " + e.getMessage());
        }
    }

    @PutMapping("/{id}")
    public ResponseEntity<CourseDTO> updateCourse(@PathVariable Long id, @Valid @RequestBody CourseDTO courseDTO) {
        try {
            Optional<CourseDTO> updatedCourse = courseService.updateCourse(id, courseDTO);
            return updatedCourse.map(ResponseEntity::ok)
                    .orElseThrow(() -> new ResourceNotFoundException("Course not found with id: " , id));
        } catch (ResourceNotFoundException e) {
            throw e;
        } catch (Exception e) {
            throw new InvalidDataException("Error updating course: " + e.getMessage());
        }
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteCourse(@PathVariable Long id) {
        try {
            courseService.deleteCourse(id);
            return ResponseEntity.noContent().build();
        } catch (ResourceNotFoundException e) {
            throw e;
        } catch (Exception e) {
            throw new InvalidDataException("Error deleting course: " + e.getMessage());
        }
    }

    @GetMapping("/teacher/{teacherId}")
    public ResponseEntity<Page<CourseDTO>> getCoursesByTeacherId(@PathVariable Long teacherId, Pageable pageable) {
        Page<CourseDTO> courses = courseService.getCoursesByTeacherId(teacherId, pageable);
        return new ResponseEntity<>(courses, HttpStatus.OK);
    }

    @GetMapping("/department/{departmentId}")
    public ResponseEntity<Page<CourseDTO>> getCoursesByDepartmentId(@PathVariable Long departmentId, Pageable pageable) {
        Page<CourseDTO> courses = courseService.getCoursesByDepartmentId(departmentId, pageable);
        return new ResponseEntity<>(courses, HttpStatus.OK);
    }

    @PostMapping("/{courseId}/assignments")
    public ResponseEntity<Void> addAssignmentToCourse(@PathVariable Long courseId, @Valid @RequestBody AssignmentDTO assignmentDTO) {
        try {
            courseService.addAssignmentToCourse(courseId, assignmentDTO);
            return ResponseEntity.status(HttpStatus.CREATED).build();
        } catch (ResourceNotFoundException e) {
            throw e;
        } catch (Exception e) {
            throw new InvalidDataException("Error adding assignment to course: " + e.getMessage());
        }
    }

    @DeleteMapping("/{courseId}/assignments/{assignmentId}")
    public ResponseEntity<Void> removeAssignmentFromCourse(@PathVariable Long courseId, @PathVariable Long assignmentId) {
        try {
            courseService.removeAssignmentFromCourse(courseId, assignmentId);
            return ResponseEntity.noContent().build();
        } catch (ResourceNotFoundException e) {
            throw e;
        } catch (Exception e) {
            throw new InvalidDataException("Error removing assignment from course: " + e.getMessage());
        }
    }
}