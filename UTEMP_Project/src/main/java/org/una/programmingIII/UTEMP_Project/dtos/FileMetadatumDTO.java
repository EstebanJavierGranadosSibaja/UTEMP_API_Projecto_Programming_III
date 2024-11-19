package org.una.programmingIII.UTEMP_Project.dtos;

import com.fasterxml.jackson.annotation.JsonBackReference;
import com.fasterxml.jackson.annotation.JsonFormat;
import jakarta.validation.constraints.Min;
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

    private Long id;

    @JsonBackReference("submission-fileMetadata")
    @Builder.Default
    @NotNull(message = "submission is null")
    private SubmissionDTO submission = new SubmissionDTO();

    @Builder.Default
    @NotNull(message = "student is null")
    private UserDTO student = new UserDTO();

    @Size(max = 255, message = "File name must be at most 255 characters long")
    @NotNull(message = "File name must not be null")
    private String fileName;

    @NotNull(message = "File size must not be null")
    @Min(value = 1, message = "El tamaño del archivo debe ser mayor que 0.")
    private Long fileSize;

    @Size(max = 100, message = "File type must be at most 100 characters long")
    private String fileType;

    @Size(max = 500, message = "Storage path must be at most 500 characters long")
    private String storagePath;

    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss.SSSSSS")
    private LocalDateTime createdAt;

    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss.SSSSSS")
    private LocalDateTime lastUpdate;

    // Nuevos campos para manejo de fragmentos
    private byte[] fileChunk; // Fragmento del archivo
    private int chunkIndex; // Índice del fragmento
    private int totalChunks; // Número total de fragmentos

    @Override
    public String toString() {
        return "CourseDTO{" +
                "id=" + id +
                ", name='" + fileSize + '\'' +
                ", description='" + fileName + '\'' +
                ", state=" + fileType +
                ", createdAt=" + createdAt +
                ", lastUpdate=" + lastUpdate +
                ", teacher=" + storagePath +
                ", department=" + (student != null ? student.getId() : "null") +
                '}';
    }
}
