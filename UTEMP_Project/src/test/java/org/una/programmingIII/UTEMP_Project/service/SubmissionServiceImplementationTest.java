package org.una.programmingIII.UTEMP_Project.service;

import static org.mockito.Mockito.*;
import static org.junit.jupiter.api.Assertions.*;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.*;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.una.programmingIII.UTEMP_Project.dtos.FileMetadatumDTO;
import org.una.programmingIII.UTEMP_Project.dtos.GradeDTO;
import org.una.programmingIII.UTEMP_Project.dtos.SubmissionDTO;
import org.una.programmingIII.UTEMP_Project.exceptions.ResourceNotFoundException;
import org.una.programmingIII.UTEMP_Project.models.FileMetadatum;
import org.una.programmingIII.UTEMP_Project.models.Grade;
import org.una.programmingIII.UTEMP_Project.models.Submission;
import org.una.programmingIII.UTEMP_Project.repositories.*;
import org.una.programmingIII.UTEMP_Project.services.submission.SubmissionServiceImplementation;
import org.una.programmingIII.UTEMP_Project.transformers.mappers.GenericMapper;

import java.util.Optional;

public class SubmissionServiceImplementationTest {

    @InjectMocks
    private SubmissionServiceImplementation submissionService;

    @Mock
    private SubmissionRepository submissionRepository;

    @Mock
    private AssignmentRepository assignmentRepository;

    @Mock
    private FileMetadatumRepository fileMetadatumRepository;

    @Mock
    private GradeRepository gradeRepository;

    @Mock
    private GenericMapper<Submission, SubmissionDTO> submissionMapper;

    @Mock
    private GenericMapper<FileMetadatum, FileMetadatumDTO> fileMetadatumMapper;

    @Mock
    private GenericMapper<Grade, GradeDTO> gradeMapper;

    private Submission submission;
    private SubmissionDTO submissionDTO;
    private FileMetadatumDTO fileMetadatumDTO;
    private GradeDTO gradeDTO;

    @BeforeEach
    public void setUp() {
        MockitoAnnotations.openMocks(this);

        submission = new Submission();
        submission.setId(1L);
        submission.setFileName("testFile");

        submissionDTO = new SubmissionDTO();
        submissionDTO.setId(1L);
        submissionDTO.setFileName("testFile");

        fileMetadatumDTO = new FileMetadatumDTO();
        fileMetadatumDTO.setId(1L);

        gradeDTO = new GradeDTO();
        gradeDTO.setId(1L);
        gradeDTO.setGrade(90.0);
    }

    @Test
    public void testGetAllSubmissions() {
        Pageable pageable = PageRequest.of(0, 10);
        Page<Submission> submissionPage = mock(Page.class);
        when(submissionRepository.findAll(pageable)).thenReturn(submissionPage);
        when(submissionPage.map(submissionMapper::convertToDTO)).thenReturn(null);  // Mock map

        Page<SubmissionDTO> result = submissionService.getAllSubmissions(pageable);

        assertNotNull(result);
        verify(submissionRepository, times(1)).findAll(pageable);
    }

    @Test
    public void testGetSubmissionById() {
        when(submissionRepository.findById(1L)).thenReturn(Optional.of(submission));
        when(submissionMapper.convertToDTO(submission)).thenReturn(submissionDTO);

        Optional<SubmissionDTO> result = submissionService.getSubmissionById(1L);

        assertTrue(result.isPresent());
        assertEquals("testFile", result.get().getFileName());
    }

    @Test
    public void testGetSubmissionByIdNotFound() {
        when(submissionRepository.findById(1L)).thenReturn(Optional.empty());

        assertThrows(ResourceNotFoundException.class, () -> submissionService.getSubmissionById(1L));
    }

    @Test
    public void testCreateSubmission() {
        when(submissionMapper.convertToEntity(submissionDTO)).thenReturn(submission);
        when(assignmentRepository.existsById(1L)).thenReturn(true);
        when(submissionRepository.save(submission)).thenReturn(submission);
        when(submissionMapper.convertToDTO(submission)).thenReturn(submissionDTO);

        SubmissionDTO result = submissionService.createSubmission(submissionDTO);

        assertNotNull(result);
        verify(submissionRepository, times(1)).save(submission);
    }

    @Test
    public void testCreateSubmissionAssignmentNotFound() {
        when(assignmentRepository.existsById(1L)).thenReturn(false);

        assertThrows(ResourceNotFoundException.class, () -> submissionService.createSubmission(submissionDTO));
    }

    @Test
    public void testUpdateSubmission() {
        when(submissionRepository.findById(1L)).thenReturn(Optional.of(submission));
        when(submissionMapper.convertToEntity(submissionDTO)).thenReturn(submission);
        when(submissionRepository.save(submission)).thenReturn(submission);
        when(submissionMapper.convertToDTO(submission)).thenReturn(submissionDTO);

        Optional<SubmissionDTO> result = submissionService.updateSubmission(1L, submissionDTO);

        assertTrue(result.isPresent());
        assertEquals("testFile", result.get().getFileName());
        verify(submissionRepository, times(1)).save(submission);
    }

    @Test
    public void testUpdateSubmissionNotFound() {
        when(submissionRepository.findById(1L)).thenReturn(Optional.empty());

        assertThrows(ResourceNotFoundException.class, () -> submissionService.updateSubmission(1L, submissionDTO));
    }

    @Test
    public void testDeleteSubmission() {
        when(submissionRepository.findById(1L)).thenReturn(Optional.of(submission));

        submissionService.deleteSubmission(1L);

        verify(submissionRepository, times(1)).delete(submission);
    }

    @Test
    public void testDeleteSubmissionNotFound() {
        when(submissionRepository.findById(1L)).thenReturn(Optional.empty());

        assertThrows(ResourceNotFoundException.class, () -> submissionService.deleteSubmission(1L));
    }

    @Test
    public void testAddFileMetadatumToSubmission() {
        when(submissionRepository.findById(1L)).thenReturn(Optional.of(submission));
        when(fileMetadatumMapper.convertToEntity(fileMetadatumDTO)).thenReturn(new FileMetadatum());
        when(fileMetadatumRepository.save(any(FileMetadatum.class))).thenReturn(new FileMetadatum());
        when(fileMetadatumMapper.convertToDTO(any(FileMetadatum.class))).thenReturn(fileMetadatumDTO);

        FileMetadatumDTO result = submissionService.addFileMetadatumToSubmission(1L, fileMetadatumDTO);

        assertNotNull(result);
        verify(fileMetadatumRepository, times(1)).save(any(FileMetadatum.class));
    }

    @Test
    public void testAddGradeToSubmission() {
        when(submissionRepository.findById(1L)).thenReturn(Optional.of(submission));
        when(gradeMapper.convertToEntity(gradeDTO)).thenReturn(new Grade());
        when(gradeRepository.save(any(Grade.class))).thenReturn(new Grade());
        when(gradeMapper.convertToDTO(any(Grade.class))).thenReturn(gradeDTO);

        GradeDTO result = submissionService.addGradeToSubmission(1L, gradeDTO);

        assertNotNull(result);
        verify(gradeRepository, times(1)).save(any(Grade.class));
    }

    @Test
    public void testRemoveFileMetadatumFromSubmission() {
        when(submissionRepository.findById(1L)).thenReturn(Optional.of(submission));
        when(fileMetadatumRepository.findById(1L)).thenReturn(Optional.of(new FileMetadatum()));

        submissionService.removeFileMetadatumFromSubmission(1L, 1L);

        verify(fileMetadatumRepository, times(1)).delete(any(FileMetadatum.class));
    }

    @Test
    public void testRemoveGradeFromSubmission() {
        when(submissionRepository.findById(1L)).thenReturn(Optional.of(submission));
        when(gradeRepository.findById(1L)).thenReturn(Optional.of(new Grade()));

        submissionService.removeGradeFromSubmission(1L, 1L);

        verify(gradeRepository, times(1)).delete(any(Grade.class));
    }

    @Test
    public void testGetSubmissionsByAssignmentId() {
        Pageable pageable = PageRequest.of(0, 10);
        when(assignmentRepository.existsById(1L)).thenReturn(true);
        Page<Submission> submissionPage = mock(Page.class);
        when(submissionRepository.findByAssignmentId(1L, pageable)).thenReturn(submissionPage);

        submissionService.getSubmissionsByAssignmentId(1L, pageable);

        verify(submissionRepository, times(1)).findByAssignmentId(1L, pageable);
    }
}
