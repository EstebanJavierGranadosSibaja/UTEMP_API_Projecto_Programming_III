package org.una.programmingIII.UTEMP_Project.controllers;

import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.una.programmingIII.UTEMP_Project.dtos.DepartmentDTO;
import org.una.programmingIII.UTEMP_Project.dtos.CourseDTO;
import org.una.programmingIII.UTEMP_Project.services.department.DepartmentService;
import org.una.programmingIII.UTEMP_Project.exceptions.ResourceNotFoundException;
import org.una.programmingIII.UTEMP_Project.exceptions.InvalidDataException;
import org.una.programmingIII.UTEMP_Project.exceptions.ResourceAlreadyExistsException;

import java.util.Optional;

@RestController
@RequestMapping("/utemp/departments")
public class DepartmentController {

    @Autowired
    private DepartmentService departmentService;

    @GetMapping
    public ResponseEntity<Page<DepartmentDTO>> getAllDepartments(Pageable pageable) {
        Page<DepartmentDTO> departments = departmentService.getAllDepartments(pageable);
        return new ResponseEntity<>(departments, HttpStatus.OK);
    }

    @GetMapping("/{id}")
    public ResponseEntity<DepartmentDTO> getDepartmentById(@PathVariable Long id) {
        try {
            Optional<DepartmentDTO> departmentDTO = departmentService.getDepartmentById(id);
            return departmentDTO.map(department -> new ResponseEntity<>(department, HttpStatus.OK))
                    .orElseThrow(() -> new ResourceNotFoundException("Department not found with id: " , id));
        } catch (ResourceNotFoundException e) {
            throw e;
        } catch (Exception e) {
            throw new InvalidDataException("Error retrieving department: " + e.getMessage());
        }
    }

    @PostMapping
    public ResponseEntity<DepartmentDTO> createDepartment(@Valid @RequestBody DepartmentDTO departmentDTO) {
        try {
            DepartmentDTO createdDepartment = departmentService.createDepartment(departmentDTO);
            return new ResponseEntity<>(createdDepartment, HttpStatus.CREATED);
        } catch (ResourceAlreadyExistsException e) {
            throw e;
        } catch (Exception e) {
            throw new InvalidDataException("Error creating department: " + e.getMessage());
        }
    }

    @PutMapping("/{id}")
    public ResponseEntity<DepartmentDTO> updateDepartment(@PathVariable Long id, @Valid @RequestBody DepartmentDTO departmentDTO) {
        try {
            Optional<DepartmentDTO> updatedDepartment = departmentService.updateDepartment(id, departmentDTO);
            return updatedDepartment.map(department -> new ResponseEntity<>(department, HttpStatus.OK))
                    .orElseThrow(() -> new ResourceNotFoundException("Department not found with id: " , id));
        } catch (ResourceNotFoundException e) {
            throw e;
        } catch (Exception e) {
            throw new InvalidDataException("Error updating department: " + e.getMessage());
        }
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteDepartment(@PathVariable Long id) {
        try {
            departmentService.deleteDepartment(id);
            return new ResponseEntity<>(HttpStatus.NO_CONTENT);
        } catch (ResourceNotFoundException e) {
            throw e;
        } catch (Exception e) {
            throw new InvalidDataException("Error deleting department: " + e.getMessage());
        }
    }

    @GetMapping("/{departmentId}/courses")
    public ResponseEntity<Page<CourseDTO>> getCoursesByDepartmentId(@PathVariable Long departmentId, Pageable pageable) {
        try {
            Page<CourseDTO> courses = departmentService.getCoursesByDepartmentId(departmentId, pageable);
            return new ResponseEntity<>(courses, HttpStatus.OK);
        } catch (Exception e) {
            throw new InvalidDataException("Error retrieving courses for department: " + e.getMessage());
        }
    }

    @PostMapping("/{departmentId}/courses")
    public ResponseEntity<Void> addCourseToDepartment(@PathVariable Long departmentId, @Valid @RequestBody CourseDTO courseDTO) {
        try {
            departmentService.addCourseToDepartment(departmentId, courseDTO);
            return new ResponseEntity<>(HttpStatus.CREATED);
        } catch (ResourceNotFoundException e) {
            throw e;
        } catch (Exception e) {
            throw new InvalidDataException("Error adding course to department: " + e.getMessage());
        }
    }

    @DeleteMapping("/{departmentId}/courses/{courseId}")
    public ResponseEntity<Void> removeCourseFromDepartment(@PathVariable Long departmentId, @PathVariable Long courseId) {
        try {
            departmentService.removeCourseFromDepartment(departmentId, courseId);
            return new ResponseEntity<>(HttpStatus.NO_CONTENT);
        } catch (ResourceNotFoundException e) {
            throw e;
        } catch (Exception e) {
            throw new InvalidDataException("Error removing course from department: " + e.getMessage());
        }
    }
}
