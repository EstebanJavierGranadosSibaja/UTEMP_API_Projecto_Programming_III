//package org.una.programmingIII.UTEMP_Project.controllers;
//
//import org.junit.jupiter.api.BeforeEach;
//import org.junit.jupiter.api.Test;
//import org.springframework.http.ResponseEntity;
//import org.una.programmingIII.UTEMP_Project.dtos.FileMetadatumDTO;
//import org.una.programmingIII.UTEMP_Project.services.file.FileService;
//
//import java.io.IOException;
//import java.util.Collections;
//import java.util.List;
//import java.util.Optional;
//
//import static org.junit.jupiter.api.Assertions.*;
//import static org.mockito.ArgumentMatchers.any;
//import static org.mockito.ArgumentMatchers.eq;
//import static org.mockito.Mockito.*;
//
//class FileControllerTest {
//
//    private FileService fileService;
//    private FileController fileController;
//
//    @BeforeEach
//    void setUp() {
//        fileService = mock(FileService.class);
//        fileController = new FileController(fileService);
//    }
//
//    @Test
//    void testGetFileMetadata_Success() throws IOException {
//        Long id = 1L;
//        FileMetadatumDTO mockMetadata = new FileMetadatumDTO();
//        mockMetadata.setId(id);
//
//        when(fileService.getFileMetadatumById(id)).thenReturn(Optional.of(mockMetadata));
//
//        ResponseEntity<FileMetadatumDTO> response = fileController.getFileMetadata(id);
//
//        assertEquals(200, response.getStatusCodeValue());
//        assertNotNull(response.getBody());
//        assertEquals(id, response.getBody().getId());
//    }
//
//    @Test
//    void testGetFileMetadata_NotFound() throws IOException {
//        Long id = 1L;
//
//        when(fileService.getFileMetadatumById(id)).thenReturn(Optional.empty());
//
//        ResponseEntity<FileMetadatumDTO> response = fileController.getFileMetadata(id);
//
//        assertEquals(404, response.getStatusCodeValue());
//        assertNull(response.getBody());
//    }
//
//    @Test
//    void testUploadFileChunk_Success() throws IOException {
//        FileMetadatumDTO mockChunk = new FileMetadatumDTO();
//
//        doNothing().when(fileService).receiveFileChunk(any(FileMetadatumDTO.class));
//
//        ResponseEntity<String> response = fileController.uploadFileChunk(mockChunk);
//
//        assertEquals(200, response.getStatusCodeValue());
//        assertEquals("File chunk uploaded successfully.", response.getBody());
//    }
//
//    @Test
//    void testUploadFileChunk_FileUploadException() throws IOException {
//        FileMetadatumDTO mockChunk = new FileMetadatumDTO();
//
//        doThrow(new IOException("Simulated error")).when(fileService).receiveFileChunk(any(FileMetadatumDTO.class));
//
//        ResponseEntity<String> response = fileController.uploadFileChunk(mockChunk);
//
//        assertEquals(500, response.getStatusCodeValue());
//        assertTrue(response.getBody().contains("IO error"));
//    }
//
//    @Test
//    void testDownloadFile_Success() throws IOException {
//        Long id = 1L;
//        FileMetadatumDTO mockChunk = new FileMetadatumDTO();
//        mockChunk.setId(id);
//
//        when(fileService.downloadFileInChunks(id)).thenReturn(Collections.singletonList(mockChunk));
//
//        ResponseEntity<List<FileMetadatumDTO>> response = fileController.downloadFile(id);
//
//        assertEquals(200, response.getStatusCodeValue());
//        assertNotNull(response.getBody());
//        assertEquals(1, response.getBody().size());
//        assertEquals(id, response.getBody().get(0).getId());
//    }
//
//    @Test
//    void testUpdateFile_Success() throws IOException {
//        Long id = 1L;
//        FileMetadatumDTO mockMetadata = new FileMetadatumDTO();
//        mockMetadata.setId(id);
//
//        when(fileService.updateFileMetadatum(eq(id), any(FileMetadatumDTO.class))).thenReturn(mockMetadata);
//
//        ResponseEntity<FileMetadatumDTO> response = fileController.updateFile(mockMetadata);
//
//        assertEquals(200, response.getStatusCodeValue());
//        assertNotNull(response.getBody());
//        assertEquals(id, response.getBody().getId());
//    }
//
//    @Test
//    void testUpdateFile_InvalidArgument() throws IOException {
//        FileMetadatumDTO mockMetadata = new FileMetadatumDTO();
//
//        doThrow(new IllegalArgumentException("Invalid ID")).when(fileService)
//                .updateFileMetadatum(anyLong(), any(FileMetadatumDTO.class));
//
//        ResponseEntity<FileMetadatumDTO> response = fileController.updateFile(mockMetadata);
//
//        assertEquals(400, response.getStatusCodeValue());
//        assertNull(response.getBody());
//    }
//
//    @Test
//    void testDeleteFile_Success() throws IOException {
//        Long id = 1L;
//
//        doNothing().when(fileService).deleteFileMetadatum(id);
//
//        ResponseEntity<String> response = fileController.deleteFile(id);
//
//        assertEquals(200, response.getStatusCodeValue());
//        assertEquals("File deleted successfully.", response.getBody());
//    }
//
//    @Test
//    void testDeleteFile_InvalidArgument() throws IOException {
//        Long id = 1L;
//
//        doThrow(new IllegalArgumentException("Invalid ID")).when(fileService).deleteFileMetadatum(id);
//
//        ResponseEntity<String> response = fileController.deleteFile(id);
//
//        assertEquals(400, response.getStatusCodeValue());
//        assertTrue(response.getBody().contains("Invalid argument"));
//    }
//}
