package org.una.programmingIII.UTEMP_Project.service;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.*;
import org.una.programmingIII.UTEMP_Project.dtos.FacultyDTO;
import org.una.programmingIII.UTEMP_Project.dtos.UniversityDTO;
import org.una.programmingIII.UTEMP_Project.exceptions.ResourceNotFoundException;
import org.una.programmingIII.UTEMP_Project.models.Faculty;
import org.una.programmingIII.UTEMP_Project.models.University;
import org.una.programmingIII.UTEMP_Project.repositories.FacultyRepository;
import org.una.programmingIII.UTEMP_Project.repositories.UniversityRepository;
import org.una.programmingIII.UTEMP_Project.services.university.UniversityServiceImplementation;
import org.una.programmingIII.UTEMP_Project.transformers.mappers.GenericMapper;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.time.LocalDateTime;
import java.util.Optional;

import static org.mockito.Mockito.*;
import static org.junit.jupiter.api.Assertions.*;

class UniversityServiceImplementationTest {

    @Mock
    private UniversityRepository universityRepository;

    @Mock
    private FacultyRepository facultyRepository;

    @Mock
    private GenericMapper<University, UniversityDTO> universityMapper;

    @Mock
    private GenericMapper<Faculty, FacultyDTO> facultyMapper;

    @Mock
    private Pageable pageable;

    @InjectMocks
    private UniversityServiceImplementation universityService;

    private University university;
    private UniversityDTO universityDTO;
    private Faculty faculty;
    private FacultyDTO facultyDTO;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
        university = new University();
        university.setId(1L);
        university.setName("Test University");
        university.setLocation("Test Location");
        university.setLastUpdate(LocalDateTime.now());

        universityDTO = new UniversityDTO();
        universityDTO.setId(1L);
        universityDTO.setName("Test University");
        universityDTO.setLocation("Test Location");

        faculty = new Faculty();
        faculty.setId(1L);
        faculty.setName("Test Faculty");

        facultyDTO = new FacultyDTO();
        facultyDTO.setId(1L);
        facultyDTO.setName("Test Faculty");
    }

    @Test
    void testCreateUniversity() {
        when(universityMapper.convertToEntity(universityDTO)).thenReturn(university);
        when(universityRepository.save(university)).thenReturn(university);
        when(universityMapper.convertToDTO(university)).thenReturn(universityDTO);

        UniversityDTO result = universityService.createUniversity(universityDTO);

        assertNotNull(result);
        assertEquals("Test University", result.getName());
        verify(universityRepository, times(1)).save(university);
    }

    @Test
    void testGetUniversityById() {
        when(universityRepository.findById(1L)).thenReturn(Optional.of(university));
        when(universityMapper.convertToDTO(university)).thenReturn(universityDTO);

        Optional<UniversityDTO> result = universityService.getUniversityById(1L);

        assertTrue(result.isPresent());
        assertEquals("Test University", result.get().getName());
        verify(universityRepository, times(1)).findById(1L);
    }

    @Test
    void testGetUniversityById_NotFound() {
        when(universityRepository.findById(1L)).thenReturn(Optional.empty());

        assertThrows(ResourceNotFoundException.class, () -> universityService.getUniversityById(1L));
    }

    @Test
    void testGetAllUniversities() {
        Page<University> universityPage = mock(Page.class);
        when(universityRepository.findAll(pageable)).thenReturn(universityPage);
        when(universityPage.map(universityMapper::convertToDTO)).thenReturn(mock(Page.class));

        Page<UniversityDTO> result = universityService.getAllUniversities(pageable);

        assertNotNull(result);
        verify(universityRepository, times(1)).findAll(pageable);
    }

    @Test
    void testAddFacultyToUniversity() {
        when(universityRepository.findById(1L)).thenReturn(Optional.of(university));
        when(facultyMapper.convertToEntity(facultyDTO)).thenReturn(faculty);

        universityService.addFacultyToUniversity(1L, facultyDTO);

        assertTrue(university.getFaculties().contains(faculty));
        verify(facultyRepository, times(1)).save(faculty);
    }

    @Test
    void testAddFacultyToUniversity_UniversityNotFound() {
        when(universityRepository.findById(1L)).thenReturn(Optional.empty());

        assertThrows(ResourceNotFoundException.class, () -> universityService.addFacultyToUniversity(1L, facultyDTO));
    }

    @Test
    void testUpdateUniversity() {
        when(universityRepository.findById(1L)).thenReturn(Optional.of(university));
        when(universityMapper.convertToEntity(universityDTO)).thenReturn(university);
        when(universityRepository.save(university)).thenReturn(university);
        when(universityMapper.convertToDTO(university)).thenReturn(universityDTO);

        Optional<UniversityDTO> result = universityService.updateUniversity(1L, universityDTO);

        assertTrue(result.isPresent());
        assertEquals("Test University", result.get().getName());
        verify(universityRepository, times(1)).save(university);
    }

    @Test
    void testUpdateUniversity_NotFound() {
        when(universityRepository.findById(1L)).thenReturn(Optional.empty());

        assertThrows(ResourceNotFoundException.class, () -> universityService.updateUniversity(1L, universityDTO));
    }

    @Test
    void testDeleteUniversity() {
        when(universityRepository.findById(1L)).thenReturn(Optional.of(university));

        universityService.deleteUniversity(1L);

        verify(universityRepository, times(1)).delete(university);
    }

    @Test
    void testDeleteUniversity_NotFound() {
        when(universityRepository.findById(1L)).thenReturn(Optional.empty());

        assertThrows(ResourceNotFoundException.class, () -> universityService.deleteUniversity(1L));
    }

    @Test
    void testRemoveFacultyFromUniversity() {
        when(universityRepository.findById(1L)).thenReturn(Optional.of(university));
        when(facultyRepository.findById(1L)).thenReturn(Optional.of(faculty));

        university.getFaculties().add(faculty);
        universityService.removeFacultyFromUniversity(1L, 1L);

        assertFalse(university.getFaculties().contains(faculty));
        verify(facultyRepository, times(1)).delete(faculty);
    }

    @Test
    void testRemoveFacultyFromUniversity_NotFound() {
        when(universityRepository.findById(1L)).thenReturn(Optional.empty());

        assertThrows(ResourceNotFoundException.class, () -> universityService.removeFacultyFromUniversity(1L, 1L));
    }

    @Test
    void testRemoveFacultyFromUniversity_FacultyNotFound() {
        when(universityRepository.findById(1L)).thenReturn(Optional.of(university));
        when(facultyRepository.findById(1L)).thenReturn(Optional.empty());

        assertThrows(ResourceNotFoundException.class, () -> universityService.removeFacultyFromUniversity(1L, 1L));
    }
}
