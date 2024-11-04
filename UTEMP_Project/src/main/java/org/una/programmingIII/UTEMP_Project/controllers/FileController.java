package org.una.programmingIII.UTEMP_Project.controllers;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.ExampleObject;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
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

    @Operation(
            summary = "Get file metadata",
            description = "Retrieve metadata for a file by its ID. This endpoint fetches the details of a file including its name, size, type, storage path, and associated submission and student information.",
            parameters = {
                    @Parameter(name = "id", description = "Unique identifier for the file metadata", required = true, schema = @Schema(type = "integer", example = "1"))
            }
    )
    @ApiResponses(value = {
            @ApiResponse(
                    responseCode = "200",
                    description = "File metadata retrieved successfully",
                    content = @Content(
                            mediaType = "application/json",
                            schema = @Schema(implementation = FileMetadatumDTO.class)
                    )
            ),
            @ApiResponse(
                    responseCode = "404",
                    description = "File not found",
                    content = @Content(
                            mediaType = "application/json",
                            examples = @ExampleObject(
                                    value = """
                                            {
                                              "message": "File not found",
                                              "details": "No file exists with the provided ID."
                                            }
                                            """
                            )
                    )
            ),
            @ApiResponse(
                    responseCode = "500",
                    description = "Internal server error",
                    content = @Content(
                            mediaType = "application/json",
                            examples = @ExampleObject(
                                    value = "{\"message\": \"Internal server error\"}"
                            )
                    )
            )
    })
    @GetMapping("/{id}")
    public ResponseEntity<FileMetadatumDTO> getFileMetadata(@PathVariable Long id) {
        try {
            Optional<FileMetadatumDTO> fileMetadata = fileService.getFileMetadatumById(id);
            return fileMetadata.map(ResponseEntity::ok)
                    .orElseGet(() -> ResponseEntity.status(HttpStatus.NOT_FOUND).build());
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(null);
        }
    }

    @Operation(
            summary = "Upload a file chunk",
            description = "Upload a chunk of a file. This endpoint allows clients to send a part of a file, facilitating the upload of large files in smaller segments to optimize memory usage and ensure successful uploads."
    )
    @ApiResponses(value = {
            @ApiResponse(
                    responseCode = "200",
                    description = "File chunk uploaded successfully",
                    content = @Content(
                            mediaType = "application/json",
                            schema = @Schema(implementation = FileMetadatumDTO.class),
                            examples = @ExampleObject(value = "File chunk uploaded successfully.")
                    )
            ),
            @ApiResponse(
                    responseCode = "400",
                    description = "Invalid argument",
                    content = @Content(
                            mediaType = "application/json",
                            examples = @ExampleObject(value = "{\"message\": \"Invalid argument\"}")
                    )
            ),
            @ApiResponse(
                    responseCode = "500",
                    description = "Internal server error",
                    content = @Content(
                            mediaType = "application/json",
                            examples = @ExampleObject(value = "{\"message\": \"Internal server error\"}")
                    )
            )
    })
    @PostMapping("/upload")
    public ResponseEntity<String> uploadFileChunk(@RequestBody FileMetadatumDTO fileChunkDTO) {
        try {
            fileService.receiveFileChunk(fileChunkDTO);
            return ResponseEntity.ok("File chunk uploaded successfully.");
        } catch (FileUploadException e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("File upload error: " + e.getMessage());
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().body("Invalid argument: " + e.getMessage());
        } catch (IOException e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("IO error: " + e.getMessage());
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("Unexpected error: " + e.getMessage());
        }
    }

    @Operation(
            summary = "Download file in chunks",
            description = "Retrieve a file in chunks by its ID. This endpoint allows for downloading large files in manageable segments."
    )
    @ApiResponses(value = {
            @ApiResponse(
                    responseCode = "200",
                    description = "File chunks retrieved successfully",
                    content = @Content(
                            mediaType = "application/json",
                            schema = @Schema(implementation = FileMetadatumDTO.class),
                            examples = @ExampleObject(value = """
                                        [
                                            {
                                                "id": 1,
                                                "submission": {},
                                                "student": {},
                                                "fileName": "assignment_submission.pdf",
                                                "fileSize": 102400,
                                                "fileType": "application/pdf",
                                                "storagePath": "/uploads/assignments/",
                                                "createdAt": "2024-01-01T12:00:00",
                                                "lastUpdate": "2024-01-01T12:00:00",
                                                "fileChunk": [byte data],
                                                "chunkIndex": 0,
                                                "totalChunks": 5
                                            }
                                        ]
                                    """)
                    )
            ),
            @ApiResponse(
                    responseCode = "500",
                    description = "Internal server error",
                    content = @Content(
                            mediaType = "application/json",
                            examples = @ExampleObject(value = "{\"message\": \"Internal server error\"}")
                    )
            )
    })
    @GetMapping("/download/{id}")
    public ResponseEntity<List<FileMetadatumDTO>> downloadFile(@PathVariable Long id) {
        try {
            List<FileMetadatumDTO> fileChunks = fileService.downloadFileInChunks(id);
            return ResponseEntity.ok(fileChunks);
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(null);
        }
    }

    @Operation(
            summary = "Update file metadata",
            description = "Update metadata of an existing file. This endpoint allows clients to modify the metadata associated with a specific file using its unique identifier."
    )
    @ApiResponses(value = {
            @ApiResponse(
                    responseCode = "200",
                    description = "File metadata updated successfully",
                    content = @Content(
                            mediaType = "application/json",
                            schema = @Schema(implementation = FileMetadatumDTO.class)
                    )
            ),
            @ApiResponse(
                    responseCode = "400",
                    description = "Invalid argument",
                    content = @Content(
                            mediaType = "application/json",
                            examples = @ExampleObject(value = "{\"message\": \"Invalid argument\"}")
                    )
            ),
            @ApiResponse(
                    responseCode = "500",
                    description = "Internal server error",
                    content = @Content(
                            mediaType = "application/json",
                            examples = @ExampleObject(value = "{\"message\": \"Internal server error\"}")
                    )
            )
    })
    @PutMapping
    public ResponseEntity<FileMetadatumDTO> updateFile(@RequestBody FileMetadatumDTO fileChunkDTO) {
        try {
            FileMetadatumDTO updatedFile = fileService.updateFileMetadatum(fileChunkDTO.getId(), fileChunkDTO);
            return ResponseEntity.ok(updatedFile);
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().body(null);
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(null);
        }
    }

    @Operation(
            summary = "Delete a file",
            description = "Delete a file by its ID. This endpoint allows clients to remove a specific file from the system based on its unique identifier.",
            parameters = {
                    @Parameter(name = "id", description = "Unique identifier of the file to be deleted", required = true,
                            schema = @Schema(type = "integer", example = "1"))
            }
    )
    @ApiResponses(value = {
            @ApiResponse(
                    responseCode = "200",
                    description = "File deleted successfully",
                    content = @Content(
                            mediaType = "text/plain",
                            examples = @ExampleObject(value = "File deleted successfully.")
                    )
            ),
            @ApiResponse(
                    responseCode = "400",
                    description = "Invalid argument",
                    content = @Content(
                            mediaType = "application/json",
                            examples = @ExampleObject(value = "{\"message\": \"Invalid argument\"}")
                    )
            ),
            @ApiResponse(
                    responseCode = "500",
                    description = "Internal server error",
                    content = @Content(
                            mediaType = "application/json",
                            examples = @ExampleObject(value = "{\"message\": \"Internal server error\"}")
                    )
            )
    })
    @DeleteMapping("/{id}")
    public ResponseEntity<String> deleteFile(@PathVariable Long id) {
        try {
            fileService.deleteFileMetadatum(id);
            return ResponseEntity.ok("File deleted successfully.");
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().body("Invalid argument: " + e.getMessage());
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("Error deleting file: " + e.getMessage());
        }
    }
}