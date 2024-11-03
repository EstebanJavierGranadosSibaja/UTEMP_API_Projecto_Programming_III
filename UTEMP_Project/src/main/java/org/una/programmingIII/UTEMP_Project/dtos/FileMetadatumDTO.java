package org.una.programmingIII.UTEMP_Project.dtos;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class FileMetadatumDTO {

    @Schema(description = "Unique identifier for the file metadata", example = "1")
    private Long id;

    @Builder.Default
    @Schema(description = "Submission associated with this file metadata")
    private SubmissionDTO submission = new SubmissionDTO();

    @Builder.Default
    @Schema(description = "Student who uploaded the file")
    private UserDTO student = new UserDTO();

    @NotNull(message = "File name must not be null")
    @Size(max = 255, message = "File name must be at most 255 characters long")
    @Schema(description = "Name of the file", example = "assignment_submission.pdf")
    private String fileName;

    @NotNull(message = "File size must not be null")
    @Schema(description = "Size of the file in bytes", example = "102400")
    private Long fileSize;

    @Size(max = 100, message = "File type must be at most 100 characters long")
    @Schema(description = "MIME type of the file", example = "application/pdf")
    private String fileType;

    @Size(max = 500, message = "Storage path must be at most 500 characters long")
    @Schema(description = "Path where the file is stored", example = "/uploads/assignments/")
    private String storagePath;

    @Schema(description = "Timestamp of when the file metadata was created")
    private LocalDateTime createdAt;

    @Schema(description = "Timestamp of the last update to the file metadata")
    private LocalDateTime lastUpdate;

    // Nuevos campos para manejo de fragmentos
    @Schema(description = "Fragment of the file being uploaded", example = "[byte data]")
    private byte[] fileChunk; // Fragmento del archivo

    @Schema(description = "Index of the file chunk", example = "0")
    private int chunkIndex; // Índice del fragmento

    @Schema(description = "Total number of file chunks", example = "5")
    private int totalChunks; // Número total de fragmentos

}
