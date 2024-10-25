package org.una.programmingIII.UTEMP_Project.controllers;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;
import org.una.programmingIII.UTEMP_Project.dtos.FacultyDTO;
import org.una.programmingIII.UTEMP_Project.dtos.DepartmentDTO;
import org.una.programmingIII.UTEMP_Project.services.faculty.FacultyService;
import java.util.List;
import java.util.Optional;

@RestController
@RequestMapping("/api/faculties")
@Validated
public class FacultyController {

    @Autowired
    private FacultyService facultyService;

    @GetMapping
    public ResponseEntity<List<FacultyDTO>> getAllFaculties() {
        List<FacultyDTO> faculties = facultyService.getAllFaculties();
        return ResponseEntity.ok(faculties);
    }

    @GetMapping("/{id}")
    public ResponseEntity<FacultyDTO> getFacultyById(@PathVariable Long id) {
        Optional<FacultyDTO> faculty = facultyService.getFacultyById(id);
        return faculty.map(ResponseEntity::ok).orElseGet(() -> ResponseEntity.notFound().build());
    }

    @PostMapping
    public ResponseEntity<FacultyDTO> createFaculty(@RequestBody FacultyDTO facultyDTO) {
        FacultyDTO newFaculty = facultyService.createFaculty(facultyDTO);
        return ResponseEntity.ok(newFaculty);
    }

    @PutMapping("/{id}")
    public ResponseEntity<FacultyDTO> updateFaculty(@PathVariable Long id, @RequestBody FacultyDTO facultyDTO) {
        Optional<FacultyDTO> updatedFaculty = facultyService.updateFaculty(id, facultyDTO);
        return updatedFaculty.map(ResponseEntity::ok).orElseGet(() -> ResponseEntity.notFound().build());
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteFaculty(@PathVariable Long id) {
        facultyService.deleteFaculty(id);
        return ResponseEntity.noContent().build();
    }

    @GetMapping("/{facultyId}/departments")
    public ResponseEntity<List<DepartmentDTO>> getDepartmentsByFacultyId(@PathVariable Long facultyId) {
        List<DepartmentDTO> departments = facultyService.getDepartmentsByFacultyId(facultyId);
        return ResponseEntity.ok(departments);
    }

    @PostMapping("/{facultyId}/departments")
    public ResponseEntity<Void> addDepartmentToFaculty(@PathVariable Long facultyId, @RequestBody DepartmentDTO departmentDTO) {
        facultyService.addDepartmentToFaculty(facultyId, departmentDTO);
        return ResponseEntity.ok().build();
    }

    @DeleteMapping("/{facultyId}/departments/{departmentId}")
    public ResponseEntity<Void> removeDepartmentFromFaculty(@PathVariable Long facultyId, @PathVariable Long departmentId) {
        facultyService.removeDepartmentFromFaculty(facultyId, departmentId);
        return ResponseEntity.noContent().build();
    }
}
