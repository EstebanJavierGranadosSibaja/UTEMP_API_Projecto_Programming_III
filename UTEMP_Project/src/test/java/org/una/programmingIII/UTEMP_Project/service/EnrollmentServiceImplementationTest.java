package org.una.programmingIII.UTEMP_Project.service;

import static org.mockito.Mockito.*;
import static org.junit.jupiter.api.Assertions.*;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.*;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.una.programmingIII.UTEMP_Project.dtos.EnrollmentDTO;
import org.una.programmingIII.UTEMP_Project.exceptions.ResourceNotFoundException;
import org.una.programmingIII.UTEMP_Project.models.Course;
import org.una.programmingIII.UTEMP_Project.models.Enrollment;
import org.una.programmingIII.UTEMP_Project.models.User;
import org.una.programmingIII.UTEMP_Project.repositories.EnrollmentRepository;
import org.una.programmingIII.UTEMP_Project.repositories.CourseRepository;
import org.una.programmingIII.UTEMP_Project.repositories.UserRepository;
import org.una.programmingIII.UTEMP_Project.services.enrollment.EnrollmentServiceImplementation;
import org.una.programmingIII.UTEMP_Project.transformers.mappers.GenericMapper;

import java.util.List;
import java.util.Optional;

class EnrollmentServiceImplementationTest {

    @Mock
    private EnrollmentRepository enrollmentRepository;

    @Mock
    private CourseRepository courseRepository;

    @Mock
    private UserRepository userRepository;

    @Mock
    private GenericMapper<Enrollment, EnrollmentDTO> enrollmentMapper;

    @InjectMocks
    private EnrollmentServiceImplementation enrollmentService;

    private EnrollmentDTO enrollmentDTO;
    private Enrollment enrollment;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);

        enrollmentDTO = new EnrollmentDTO();
        enrollmentDTO.setId(1L);

        enrollment = new Enrollment();
        enrollment.setId(1L);
    }

    @Test
    void testGetAllEnrollments() {
        Pageable pageable = mock(Pageable.class);
        Page<Enrollment> enrollmentPage = new PageImpl<>(List.of(enrollment));
        when(enrollmentRepository.findAll(pageable)).thenReturn(enrollmentPage);
        when(enrollmentMapper.convertToDTO(any(Enrollment.class))).thenReturn(enrollmentDTO);

        Page<EnrollmentDTO> result = enrollmentService.getAllEnrollments(pageable);

        assertNotNull(result);
        assertEquals(1, result.getContent().size());
        verify(enrollmentRepository, times(1)).findAll(pageable);
    }

    @Test
    void testGetEnrollmentsByCourseId() {
        Pageable pageable = mock(Pageable.class);
        Long courseId = 1L;
        Page<Enrollment> enrollmentPage = new PageImpl<>(List.of(enrollment));
        when(enrollmentRepository.findByCourseId(courseId, pageable)).thenReturn(enrollmentPage);
        when(enrollmentMapper.convertToDTO(any(Enrollment.class))).thenReturn(enrollmentDTO);

        Page<EnrollmentDTO> result = enrollmentService.getEnrollmentsByCourseId(courseId, pageable);

        assertNotNull(result);
        assertEquals(1, result.getContent().size());
        verify(enrollmentRepository, times(1)).findByCourseId(courseId, pageable);
    }

    @Test
    void testGetEnrollmentByIdFound() {
        Long enrollmentId = 1L;
        when(enrollmentRepository.findById(enrollmentId)).thenReturn(Optional.of(enrollment));
        when(enrollmentMapper.convertToDTO(any(Enrollment.class))).thenReturn(enrollmentDTO);

        Optional<EnrollmentDTO> result = enrollmentService.getEnrollmentById(enrollmentId);

        assertTrue(result.isPresent());
        verify(enrollmentRepository, times(1)).findById(enrollmentId);
    }

    @Test
    void testGetEnrollmentByIdNotFound() {
        Long enrollmentId = 1L;
        when(enrollmentRepository.findById(enrollmentId)).thenReturn(Optional.empty());

        assertThrows(ResourceNotFoundException.class, () -> enrollmentService.getEnrollmentById(enrollmentId));
        verify(enrollmentRepository, times(1)).findById(enrollmentId);
    }

    @Test
    void testCreateEnrollment() {
        when(enrollmentMapper.convertToEntity(any(EnrollmentDTO.class))).thenReturn(enrollment);
        when(courseRepository.findById(anyLong())).thenReturn(Optional.of(mock(Course.class)));
        when(userRepository.findById(anyLong())).thenReturn(Optional.of(mock(User.class)));
        when(enrollmentRepository.save(any(Enrollment.class))).thenReturn(enrollment);
        when(enrollmentMapper.convertToDTO(any(Enrollment.class))).thenReturn(enrollmentDTO);

        EnrollmentDTO result = enrollmentService.createEnrollment(enrollmentDTO);

        assertNotNull(result);
        verify(enrollmentRepository, times(1)).save(any(Enrollment.class));
    }

    @Test
    void testUpdateEnrollment() {
        Long enrollmentId = 1L;
        when(enrollmentRepository.findById(enrollmentId)).thenReturn(Optional.of(enrollment));
        when(enrollmentMapper.convertToDTO(any(Enrollment.class))).thenReturn(enrollmentDTO);

        Optional<EnrollmentDTO> result = enrollmentService.updateEnrollment(enrollmentId, enrollmentDTO);

        assertTrue(result.isPresent());
        verify(enrollmentRepository, times(1)).findById(enrollmentId);
        verify(enrollmentRepository, times(1)).save(any(Enrollment.class));
    }

    @Test
    void testUpdateEnrollmentNotFound() {
        Long enrollmentId = 1L;
        when(enrollmentRepository.findById(enrollmentId)).thenReturn(Optional.empty());

        assertThrows(ResourceNotFoundException.class, () -> enrollmentService.updateEnrollment(enrollmentId, enrollmentDTO));
        verify(enrollmentRepository, times(1)).findById(enrollmentId);
    }

    @Test
    void testDeleteEnrollment() {
        Long enrollmentId = 1L;
        when(enrollmentRepository.findById(enrollmentId)).thenReturn(Optional.of(enrollment));

        enrollmentService.deleteEnrollment(enrollmentId);

        verify(enrollmentRepository, times(1)).delete(any(Enrollment.class));
    }

    @Test
    void testDeleteEnrollmentNotFound() {
        Long enrollmentId = 1L;
        when(enrollmentRepository.findById(enrollmentId)).thenReturn(Optional.empty());

        assertThrows(ResourceNotFoundException.class, () -> enrollmentService.deleteEnrollment(enrollmentId));
        verify(enrollmentRepository, times(1)).findById(enrollmentId);
    }
}
