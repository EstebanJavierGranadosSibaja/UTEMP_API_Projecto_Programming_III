package org.una.programmingIII.UTEMP_Project.controllers;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.una.programmingIII.UTEMP_Project.dtos.DepartmentDTO;
import org.una.programmingIII.UTEMP_Project.dtos.CourseDTO;
import org.una.programmingIII.UTEMP_Project.services.department.DepartmentService;
import org.una.programmingIII.UTEMP_Project.exceptions.InvalidDataException;
import org.una.programmingIII.UTEMP_Project.exceptions.ResourceNotFoundException;

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

    @GetMapping
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

    @GetMapping("/{id}")
    public ResponseEntity<DepartmentDTO> getDepartmentById(@PathVariable Long id) {
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

    @PostMapping
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

    @PutMapping("/{id}")
    public ResponseEntity<DepartmentDTO> updateDepartment(@PathVariable Long id,
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

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteDepartment(@PathVariable Long id) {
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

    @GetMapping("/faculty/{facultyId}")
    public ResponseEntity<Page<DepartmentDTO>> getDepartmentsByFacultyId(@PathVariable Long facultyId,
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

    @PostMapping("/{departmentId}/courses")
    public ResponseEntity<Void> addCourseToDepartment(@PathVariable Long departmentId,
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
            logger.error("Unexpected error while adding course to department: {}", e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }

    @DeleteMapping("/{departmentId}/courses/{courseId}")
    public ResponseEntity<Void> removeCourseFromDepartment(@PathVariable Long departmentId,
                                                           @PathVariable Long courseId) {
        try {
            departmentService.removeCourseFromDepartment(departmentId, courseId);
            logger.info("Course ID {} removed from department ID {} successfully.", courseId, departmentId);
            return ResponseEntity.noContent().build();
        } catch (ResourceNotFoundException e) {
            logger.error("Course not found for removal from department: {}", e.getMessage());
            return ResponseEntity.status(HttpStatus.NOT_FOUND).build();
        } catch (Exception e) {
            logger.error("Unexpected error while removing course from department: {}", e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }
}
