//package org.una.programmingIII.UTEMP_Project.service;
//
//import static org.mockito.Mockito.*;
//import static org.junit.jupiter.api.Assertions.*;
//
//import org.junit.jupiter.api.BeforeEach;
//import org.junit.jupiter.api.Test;
//import org.mockito.*;
//import org.una.programmingIII.UTEMP_Project.dtos.FileMetadatumDTO;
//import org.una.programmingIII.UTEMP_Project.exceptions.FileNotFoundDataBaseException;
//import org.una.programmingIII.UTEMP_Project.models.FileMetadatum;
//import org.una.programmingIII.UTEMP_Project.models.Submission;
//import org.una.programmingIII.UTEMP_Project.models.User;
//import org.una.programmingIII.UTEMP_Project.repositories.FileMetadatumRepository;
//import org.una.programmingIII.UTEMP_Project.repositories.SubmissionRepository;
//import org.una.programmingIII.UTEMP_Project.repositories.UserRepository;
//import org.una.programmingIII.UTEMP_Project.services.file.FileServiceImplementation;
//
//import java.io.IOException;
//import java.util.List;
//import java.util.Optional;
//
//public class FileServiceImplementationTest {
//
//    @InjectMocks
//    private FileServiceImplementation fileService;
//
//    @Mock
//    private FileMetadatumRepository fileMetadatumRepository;
//
//    @Mock
//    private SubmissionRepository submissionRepository;
//
//    @Mock
//    private UserRepository userRepository;
//
//    @Mock
//    private User user;
//
//    @Mock
//    private FileMetadatum fileMetadatum;
//
//    @BeforeEach
//    public void setup() {
//        MockitoAnnotations.openMocks(this);
//    }
//
//    @Test
//    public void testGetFileMetadatumById_FileFound() {
//        Long fileId = 1L;
//        FileMetadatum file = new FileMetadatum();
//        file.setId(fileId);
//        file.setFileName("file.txt");
//        file.setFileType("txt");
//        file.setFileSize(1024L);
//        file.setStoragePath("path/to/file.txt");
//
//        when(fileMetadatumRepository.findById(fileId)).thenReturn(Optional.of(file));
//
//        Optional<FileMetadatumDTO> result = fileService.getFileMetadatumById(fileId);
//
//        assertTrue(result.isPresent());
//        assertEquals(fileId, result.get().getId());
//        assertEquals("file.txt", result.get().getFileName());
//    }
//
//    @Test
//    public void testGetFileMetadatumById_FileNotFound() {
//
//        Long fileId = 1L;
//
//        when(fileMetadatumRepository.findById(fileId)).thenReturn(Optional.empty());
//
//        assertThrows(FileNotFoundDataBaseException.class, () -> fileService.getFileMetadatumById(fileId));
//    }
//
//    @Test
//    public void testReceiveFileChunk() throws IOException {
//
//        FileMetadatumDTO fileChunkDTO = FileMetadatumDTO.builder()
//                .id(1L)
//                .fileName("file.txt")
//                .fileType("txt")
//                .fileSize(1024L)
//                .storagePath("path/to/file.txt")
//                .chunkIndex(0)
//                .totalChunks(2)
//                .build();
//
//        fileService.receiveFileChunk(fileChunkDTO);
//
//        verify(fileService, times(1)).receiveFileChunk(fileChunkDTO);
//    }
//
//    @Test
//    public void testFinalizeFileUpload() throws IOException {
//
//        Long fileId = 1L;
//        FileMetadatumDTO fileChunkDTO = FileMetadatumDTO.builder()
//                .id(fileId)
//                .fileName("file.txt")
//                .fileType("txt")
//                .storagePath("path/to/file.txt")
//                .chunkIndex(0)
//                .totalChunks(1)
//                .build();
//
//        when(fileMetadatumRepository.findById(fileId)).thenReturn(Optional.of(fileMetadatum));
//        when(userRepository.findById(anyLong())).thenReturn(Optional.of(user));
//        when(submissionRepository.findById(anyLong())).thenReturn(Optional.of(mock(Submission.class)));
//
//        fileService.finalizeFileUpload(fileId, fileChunkDTO);
//
//        verify(fileMetadatumRepository, times(1)).save(any(FileMetadatum.class));
//    }
//
//    @Test
//    public void testDeleteFileMetadatum() {
//
//        Long fileId = 1L;
//        FileMetadatum file = new FileMetadatum();
//        file.setStoragePath("path/to/file.txt");
//
//        when(fileMetadatumRepository.findById(fileId)).thenReturn(Optional.of(file));
//
//        fileService.deleteFileMetadatum(fileId);
//
//        verify(fileMetadatumRepository, times(1)).deleteById(fileId);
//    }
//
//    @Test
//    public void testDownloadFileInChunks() throws IOException {
//
//        Long fileId = 1L;
//        FileMetadatum file = new FileMetadatum();
//        file.setStoragePath("path/to/file.txt");
//        when(fileMetadatumRepository.findById(fileId)).thenReturn(Optional.of(file));
//
//        List<FileMetadatumDTO> chunks = fileService.downloadFileInChunks(fileId);
//
//        assertNotNull(chunks);
//        assertFalse(chunks.isEmpty());
//    }
//}
