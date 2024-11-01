package org.una.programmingIII.UTEMP_Project.controllers;

import org.apache.tomcat.util.http.fileupload.FileUploadException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import org.una.programmingIII.UTEMP_Project.dtos.FileMetadatumDTO;
import org.una.programmingIII.UTEMP_Project.services.file.FileService;

import java.io.IOException;
import java.util.List;
import java.util.Optional;

@RestController
@RequestMapping("/utemp/files")
public class FileController {

    private final FileService fileService;

    public FileController(FileService fileService) {
        this.fileService = fileService;
    }

    @GetMapping("/download/{id}")
    public ResponseEntity<List<FileMetadatumDTO>> downloadFile(@PathVariable Long id) {
        try {
            List<FileMetadatumDTO> fileChunks = fileService.downloadFileInChunks(id);
            return ResponseEntity.ok(fileChunks);
        } catch (IOException e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(null);
        }
    }

    @GetMapping("/{id}")
    public ResponseEntity<FileMetadatumDTO> getFileMetadata(@PathVariable Long id) {
        Optional<FileMetadatumDTO> fileMetadata = fileService.getFileMetadatumById(id);
        return fileMetadata.map(ResponseEntity::ok)
                .orElseGet(() -> ResponseEntity.status(HttpStatus.NOT_FOUND).build());
    }

    @PostMapping("/upload")
    @PreAuthorize("hasAuthority('LOAD_FILE')")
    public ResponseEntity<String> uploadFileChunk(@RequestBody FileMetadatumDTO fileChunkDTO) {
        try {
            fileService.receiveFileChunk(fileChunkDTO);
            return ResponseEntity.ok("File chunk uploaded successfully.");
        } catch (FileUploadException e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(e.getMessage());
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("hasAuthority('DELETE_FILE')")
    public ResponseEntity<String> deleteFile(@PathVariable Long id) {
        try {
            fileService.deleteFileMetadatum(id);
            return ResponseEntity.ok("File deleted successfully.");
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }

    @PutMapping
    @PreAuthorize("hasAuthority('UPDATE_FILE')")
    public ResponseEntity<FileMetadatumDTO> updateFile(@RequestBody FileMetadatumDTO fileChunkDTO) {
        try {
            FileMetadatumDTO updatedFile = fileService.updateFileMetadatum(fileChunkDTO.getId(), fileChunkDTO);
            return ResponseEntity.ok(updatedFile);
        } catch (IOException e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(null);
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().body(null);
        }
    }
}
