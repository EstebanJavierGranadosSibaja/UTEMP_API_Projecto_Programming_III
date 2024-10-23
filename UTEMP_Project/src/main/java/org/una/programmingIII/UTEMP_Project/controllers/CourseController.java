package org.una.programmingIII.UTEMP_Project.controllers;

import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.una.programmingIII.UTEMP_Project.dtos.CourseDTO;
import org.una.programmingIII.UTEMP_Project.services.CourseServices.CourseService;

import java.util.List;
import java.util.Optional;

@RestController
@RequestMapping("/api/courses")
public class CourseController {

    @Autowired
    private CourseService courseService;

    @GetMapping
    public ResponseEntity<List<CourseDTO>> getAllCourses() {
        List<CourseDTO> courses = courseService.getAllCourses();
        return new ResponseEntity<>(courses, HttpStatus.OK);
    }

    @GetMapping("/{id}")
    public ResponseEntity<CourseDTO> getCourseById(@PathVariable Long id) {
        Optional<CourseDTO> course = courseService.getCourseById(id);
        return course.map(ResponseEntity::ok).orElseGet(() -> new ResponseEntity<>(HttpStatus.NOT_FOUND));
    }

    @PostMapping
    public ResponseEntity<CourseDTO> createCourse(@Valid @RequestBody CourseDTO courseDTO) {
        CourseDTO createdCourse = courseService.createCourse(courseDTO);
        return new ResponseEntity<>(createdCourse, HttpStatus.CREATED);
    }

    @PutMapping("/{id}")
    public ResponseEntity<CourseDTO> updateCourse(@PathVariable Long id, @Valid @RequestBody CourseDTO courseDTO) {
        Optional<CourseDTO> updatedCourse = courseService.updateCourse(id, courseDTO);
        return updatedCourse.map(ResponseEntity::ok).orElseGet(() -> new ResponseEntity<>(HttpStatus.NOT_FOUND));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteCourse(@PathVariable Long id) {
        courseService.deleteCourse(id);
        return new ResponseEntity<>(HttpStatus.NO_CONTENT);
    }

    @GetMapping("/teacher/{teacherId}")
    public ResponseEntity<List<CourseDTO>> getCoursesByTeacherId(@PathVariable Long teacherId) {
        List<CourseDTO> courses = courseService.getCoursesByTeacherId(teacherId);
        return new ResponseEntity<>(courses, HttpStatus.OK);
    }

    @GetMapping("/department/{departmentId}")
    public ResponseEntity<List<CourseDTO>> getCoursesByDepartmentId(@PathVariable Long departmentId) {
        List<CourseDTO> courses = courseService.getCoursesByDepartmentId(departmentId);
        return new ResponseEntity<>(courses, HttpStatus.OK);
    }
}
