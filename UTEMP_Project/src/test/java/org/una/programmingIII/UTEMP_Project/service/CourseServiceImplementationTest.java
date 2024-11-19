package org.una.programmingIII.UTEMP_Project.service;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.*;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.una.programmingIII.UTEMP_Project.dtos.CourseDTO;
import org.una.programmingIII.UTEMP_Project.exceptions.InvalidDataException;
import org.una.programmingIII.UTEMP_Project.exceptions.ResourceNotFoundException;
import org.una.programmingIII.UTEMP_Project.models.Course;
import org.una.programmingIII.UTEMP_Project.models.CourseState;
import org.una.programmingIII.UTEMP_Project.repositories.CourseRepository;
import org.una.programmingIII.UTEMP_Project.services.course.CourseServiceImplementation;
import org.una.programmingIII.UTEMP_Project.transformers.mappers.GenericMapper;

import java.util.List;
import java.util.Optional;

import static org.mockito.Mockito.*;
import static org.junit.jupiter.api.Assertions.*;

@ExtendWith(MockitoExtension.class)
public class CourseServiceImplementationTest {

    @Mock
    private CourseRepository courseRepository;
    @Mock
    private GenericMapper<Course, CourseDTO> courseMapper;

    @InjectMocks
    private CourseServiceImplementation courseService;

    private Course course;
    private CourseDTO courseDTO;

    @BeforeEach
    public void setUp() {
        course = new Course();
        course.setId(1L);
        course.setName("Test Course");
        course.setState(CourseState.ACTIVE);

        courseDTO = new CourseDTO();
        courseDTO.setId(1L);
        courseDTO.setName("Test Course");
        courseDTO.setState(CourseState.ACTIVE);
    }

    @Test
    public void testGetAllCourses_Success() {
        Pageable pageable = Pageable.unpaged();
        Page<Course> coursePage = new PageImpl<>(List.of(course));
        when(courseRepository.findAll(pageable)).thenReturn(coursePage);
        when(courseMapper.convertToDTO(any(Course.class))).thenReturn(courseDTO);

        Page<CourseDTO> result = courseService.getAllCourses(pageable);

        assertNotNull(result);
        assertEquals(1, result.getTotalElements());
        assertEquals("Test Course", result.getContent().getFirst().getName());
    }

    @Test
    public void testGetCourseById_Success() {
        when(courseRepository.findById(1L)).thenReturn(Optional.of(course));
        when(courseMapper.convertToDTO(any(Course.class))).thenReturn(courseDTO);

        Optional<CourseDTO> result = courseService.getCourseById(1L);

        assertTrue(result.isPresent());
        assertEquals("Test Course", result.get().getName());
    }

    @Test
    public void testGetCourseById_NotFound() {
        when(courseRepository.findById(1L)).thenReturn(Optional.empty());

        Optional<CourseDTO> result = courseService.getCourseById(1L);

        assertFalse(result.isPresent());
    }

    @Test
    public void testCreateCourse_Success() {
        when(courseMapper.convertToEntity(courseDTO)).thenReturn(course);
        when(courseRepository.save(any(Course.class))).thenReturn(course);
        when(courseMapper.convertToDTO(any(Course.class))).thenReturn(courseDTO);

        CourseDTO result = courseService.createCourse(courseDTO);

        assertNotNull(result);
        assertEquals("Test Course", result.getName());
    }

    @Test
    public void testCreateCourse_InvalidData() {
        when(courseMapper.convertToEntity(courseDTO)).thenReturn(course);
        when(courseRepository.save(any(Course.class))).thenThrow(new InvalidDataException("Invalid course data"));

        assertThrows(InvalidDataException.class, () -> courseService.createCourse(courseDTO));
    }

    @Test
    public void testUpdateCourse_Success() {
        when(courseRepository.findById(1L)).thenReturn(Optional.of(course));
        when(courseMapper.convertToDTO(any(Course.class))).thenReturn(courseDTO);

        Optional<CourseDTO> result = courseService.updateCourse(1L, courseDTO);

        assertTrue(result.isPresent());
        assertEquals("Test Course", result.get().getName());
    }

    @Test
    public void testUpdateCourse_NotFound() {
        when(courseRepository.findById(1L)).thenReturn(Optional.empty());

        assertThrows(ResourceNotFoundException.class, () -> courseService.updateCourse(1L, courseDTO));
    }

    @Test
    public void testDeleteCourse_Success() {
        when(courseRepository.findById(1L)).thenReturn(Optional.of(course));

        courseService.deleteCourse(1L);

        verify(courseRepository, times(1)).delete(course);
    }

    @Test
    public void testDeleteCourse_NotFound() {
        when(courseRepository.findById(1L)).thenReturn(Optional.empty());

        assertThrows(ResourceNotFoundException.class, () -> courseService.deleteCourse(1L));
    }

}
