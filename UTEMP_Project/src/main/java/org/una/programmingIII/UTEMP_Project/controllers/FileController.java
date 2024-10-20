package org.una.programmingIII.UTEMP_Project.controllers;

import org.apache.tomcat.util.http.fileupload.FileUploadException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.una.programmingIII.UTEMP_Project.dtos.FileMetadatumDTO;
import org.una.programmingIII.UTEMP_Project.services.FileMetadatumServices.FileMetadatumService;

@RestController
@RequestMapping("/api/files")
public class FileController {

    private final FileMetadatumService fileMetadatumService;

    public FileController(FileMetadatumService fileMetadatumService) {
        this.fileMetadatumService = fileMetadatumService;
    }

    @PostMapping("/upload")
    @PreAuthorize("hasAuthority('LOAD_FILE')")
    public ResponseEntity<String> uploadFileChunk(@RequestBody FileMetadatumDTO fileChunkDTO) {
        try {
            fileMetadatumService.receiveFileChunk(fileChunkDTO);
            return ResponseEntity.ok().build();
        } catch (FileUploadException e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(e.getMessage());
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }
}