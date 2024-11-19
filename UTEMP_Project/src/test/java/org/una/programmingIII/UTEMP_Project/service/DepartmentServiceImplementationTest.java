package org.una.programmingIII.UTEMP_Project.service;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.*;
import org.springframework.data.domain.*;
import org.una.programmingIII.UTEMP_Project.dtos.CourseDTO;
import org.una.programmingIII.UTEMP_Project.dtos.DepartmentDTO;
import org.una.programmingIII.UTEMP_Project.exceptions.ResourceNotFoundException;
import org.una.programmingIII.UTEMP_Project.models.Course;
import org.una.programmingIII.UTEMP_Project.models.Department;
import org.una.programmingIII.UTEMP_Project.repositories.CourseRepository;
import org.una.programmingIII.UTEMP_Project.repositories.DepartmentRepository;
import org.una.programmingIII.UTEMP_Project.repositories.FacultyRepository;
import org.una.programmingIII.UTEMP_Project.services.department.DepartmentServiceImplementation;
import org.una.programmingIII.UTEMP_Project.transformers.mappers.GenericMapper;

import java.util.List;
import java.util.Optional;

import static org.mockito.Mockito.*;
import static org.junit.jupiter.api.Assertions.*;

public class DepartmentServiceImplementationTest {

    @Mock
    private DepartmentRepository departmentRepository;

    @Mock
    private FacultyRepository facultyRepository;

    @Mock
    private CourseRepository courseRepository;

    @Mock
    private GenericMapper<Department, DepartmentDTO> departmentMapper;

    @Mock
    private GenericMapper<Course, CourseDTO> courseMapper;

    @InjectMocks
    private DepartmentServiceImplementation departmentService;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
    }

    @Test
    void testGetAllDepartments() {
        Pageable pageable = PageRequest.of(0, 10);
        Department department = new Department();
        department.setId(1L);
        department.setName("Computer Science");

        Page<Department> departmentPage = new PageImpl<>(List.of(department));

        when(departmentRepository.findAll(pageable)).thenReturn(departmentPage);
        when(departmentMapper.convertToDTO(department)).thenReturn(new DepartmentDTO());

        Page<DepartmentDTO> result = departmentService.getAllDepartments(pageable);

        assertNotNull(result);
        assertEquals(1, result.getTotalElements());
        verify(departmentRepository, times(1)).findAll(pageable);
    }

    @Test
    void testGetDepartmentById() {
        Department department = new Department();
        department.setId(1L);
        department.setName("Computer Science");

        when(departmentRepository.findById(1L)).thenReturn(Optional.of(department));
        when(departmentMapper.convertToDTO(department)).thenReturn(new DepartmentDTO());

        Optional<DepartmentDTO> result = departmentService.getDepartmentById(1L);

        assertTrue(result.isPresent());
        assertEquals(1L, result.get().getId());
        verify(departmentRepository, times(1)).findById(1L);
    }

    @Test
    void testGetDepartmentByIdNotFound() {
        when(departmentRepository.findById(1L)).thenReturn(Optional.empty());

        ResourceNotFoundException exception = assertThrows(ResourceNotFoundException.class, () -> departmentService.getDepartmentById(1L));

        assertEquals("Department not found", exception.getMessage());
    }

    @Test
    void testCreateDepartment() {
        DepartmentDTO departmentDTO = new DepartmentDTO();
        departmentDTO.setName("Computer Science");

        Department department = new Department();
        department.setId(1L);
        department.setName("Computer Science");

        when(departmentMapper.convertToEntity(departmentDTO)).thenReturn(department);
        when(departmentRepository.save(department)).thenReturn(department);
        when(departmentMapper.convertToDTO(department)).thenReturn(departmentDTO);

        DepartmentDTO result = departmentService.createDepartment(departmentDTO);

        assertNotNull(result);
        assertEquals("Computer Science", result.getName());
        verify(departmentRepository, times(1)).save(department);
    }

    @Test
    void testCreateDepartmentInvalidData() {
        DepartmentDTO departmentDTO = new DepartmentDTO();
        departmentDTO.setName("Computer Science");

        when(departmentMapper.convertToEntity(departmentDTO)).thenReturn(new Department());
        when(facultyRepository.findById(1L)).thenReturn(Optional.empty());

        ResourceNotFoundException exception = assertThrows(ResourceNotFoundException.class, () -> departmentService.createDepartment(departmentDTO));

        assertEquals("Faculty not found", exception.getMessage());
    }

    @Test
    void testUpdateDepartment() {
        DepartmentDTO departmentDTO = new DepartmentDTO();
        departmentDTO.setName("Updated Department");

        Department existingDepartment = new Department();
        existingDepartment.setId(1L);
        existingDepartment.setName("Old Department");

        when(departmentRepository.findById(1L)).thenReturn(Optional.of(existingDepartment));
        when(departmentRepository.save(existingDepartment)).thenReturn(existingDepartment);
        when(departmentMapper.convertToDTO(existingDepartment)).thenReturn(departmentDTO);

        Optional<DepartmentDTO> result = departmentService.updateDepartment(1L, departmentDTO);

        assertTrue(result.isPresent());
        assertEquals("Updated Department", result.get().getName());
        verify(departmentRepository, times(1)).save(existingDepartment);
    }

    @Test
    void testDeleteDepartment() {
        Department department = new Department();
        department.setId(1L);
        department.setName("Computer Science");

        when(departmentRepository.findById(1L)).thenReturn(Optional.of(department));

        departmentService.deleteDepartment(1L);

        verify(departmentRepository, times(1)).delete(department);
    }

    @Test
    void testDeleteDepartmentNotFound() {
        when(departmentRepository.findById(1L)).thenReturn(Optional.empty());

        ResourceNotFoundException exception = assertThrows(ResourceNotFoundException.class, () -> departmentService.deleteDepartment(1L));

        assertEquals("Department not found", exception.getMessage());
    }

    @Test
    void testAddCourseToDepartment() {
        CourseDTO courseDTO = new CourseDTO();
        Department department = new Department();
        department.setId(1L);
        Course course = new Course();
        course.setId(1L);

        when(departmentRepository.findById(1L)).thenReturn(Optional.of(department));
        when(courseMapper.convertToEntity(courseDTO)).thenReturn(course);

        departmentService.addCourseToDepartment(1L, courseDTO);

        verify(courseRepository, times(1)).save(course);
    }

    @Test
    void testRemoveCourseFromDepartment() {
        Department department = new Department();
        department.setId(1L);
        Course course = new Course();
        course.setId(1L);

        when(departmentRepository.findById(1L)).thenReturn(Optional.of(department));
        when(courseRepository.findById(1L)).thenReturn(Optional.of(course));

        departmentService.removeCourseFromDepartment(1L, 1L);

        verify(courseRepository, times(1)).delete(course);
    }

    @Test
    void testRemoveCourseFromDepartmentNotFound() {
        when(departmentRepository.findById(1L)).thenReturn(Optional.empty());

        ResourceNotFoundException exception = assertThrows(ResourceNotFoundException.class, () -> departmentService.removeCourseFromDepartment(1L, 1L));

        assertEquals("Department not found", exception.getMessage());
    }
}
