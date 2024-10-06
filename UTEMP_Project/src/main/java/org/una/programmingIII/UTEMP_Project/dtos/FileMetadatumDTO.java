package org.una.programmingIII.UTEMP_Project.dtos;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.*;
import java.time.LocalDateTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class FileMetadatumDTO {

    private Long id;

    @Builder.Default
    private AssignmentDTO assignment = new AssignmentDTO();

    @Builder.Default
    private UserDTO student = new UserDTO();

    @NotNull(message = "File name must not be null")
    @Size(max = 255, message = "File name must be at most 255 characters long")
    private String fileName;

    @NotNull(message = "File size must not be null")
    private Long fileSize;

    @Size(max = 100, message = "File type must be at most 100 characters long")
    private String fileType;

    @Size(max = 500, message = "Storage path must be at most 500 characters long")
    private String storagePath;

    private LocalDateTime createdAt;

    private LocalDateTime lastUpdate;
}
