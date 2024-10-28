package org.una.programmingIII.UTEMP_Project.controllers;

import jakarta.validation.Valid;
import org.apache.tomcat.util.http.fileupload.FileUploadException;
import org.springframework.data.domain.Page;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import org.una.programmingIII.UTEMP_Project.dtos.FileMetadatumDTO;
import org.una.programmingIII.UTEMP_Project.services.file.FileService;

import java.util.Optional;

@RestController
@RequestMapping("/api/files")
public class FileController {

    private final FileService fileService;

    public FileController(FileService fileService) {
        this.fileService = fileService;
    }

    @PostMapping("/upload")
    @PreAuthorize("hasAuthority('LOAD_FILE')")
    public ResponseEntity<String> uploadFileChunk(@RequestBody FileMetadatumDTO fileChunkDTO) {
        try {
            fileService.receiveFileChunk(fileChunkDTO);
            return ResponseEntity.ok().build();
        } catch (FileUploadException e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(e.getMessage());
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }

    @PostMapping("/finalize-upload/{fileId}")
    public ResponseEntity<String> finalizeUpload(@PathVariable Long fileId, @RequestBody FileMetadatumDTO fileDTO) {
        try {
            fileService.finalizeFileUpload(fileId, fileDTO);
            return ResponseEntity.ok("File upload finalized successfully.");
        } catch (FileUploadException e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body("Error finalizing file upload: " + e.getMessage());
        }
    }

    @GetMapping
    public ResponseEntity<Page<FileMetadatumDTO>> getAllFileMetadata(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size) {
        Page<FileMetadatumDTO> fileMetadata = fileService.getAllFileMetadata(page, size);
        return ResponseEntity.ok(fileMetadata);
    }

    @GetMapping("/{id}")
    public ResponseEntity<FileMetadatumDTO> getFileMetadatumById(@PathVariable Long id) {
        Optional<FileMetadatumDTO> fileMetadatumDTO = fileService.getFileMetadatumById(id);
        return fileMetadatumDTO.map(ResponseEntity::ok)
                .orElseGet(() -> ResponseEntity.notFound().build());
    }

    @PostMapping
    public ResponseEntity<FileMetadatumDTO> createFileMetadatum(@Valid @RequestBody FileMetadatumDTO fileMetadatumDTO) {
        FileMetadatumDTO createdFileMetadatum = fileService.createFileMetadatum(fileMetadatumDTO);
        return ResponseEntity.status(HttpStatus.CREATED).body(createdFileMetadatum);
    }

    @PutMapping("/{id}")
    public ResponseEntity<FileMetadatumDTO> updateFileMetadatum(
            @PathVariable Long id,
            @Valid @RequestBody FileMetadatumDTO fileMetadatumDTO) {
        Optional<FileMetadatumDTO> updatedFileMetadatum = fileService.updateFileMetadatum(id, fileMetadatumDTO);
        return updatedFileMetadatum.map(ResponseEntity::ok)
                .orElseGet(() -> ResponseEntity.notFound().build());
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteFileMetadatum(@PathVariable Long id) {
        fileService.deleteFileMetadatum(id);
        return ResponseEntity.noContent().build();
    }
}