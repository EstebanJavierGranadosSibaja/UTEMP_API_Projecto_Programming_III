package org.una.programmingIII.UTEMP_Project.dtos;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.una.programmingIII.UTEMP_Project.models.NotificationStatus;

import java.time.LocalDateTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class NotificationDTO {

    @Schema(description = "Unique identifier for the notification", example = "1")
    private Long id;

    @Builder.Default
    @Schema(description = "User associated with this notification")
    private UserDTO user = new UserDTO();

    @NotBlank(message = "Message must not be blank")
    @Size(max = 500, message = "Message must be at most 500 characters long")
    @Schema(description = "Content of the notification message", example = "You have a new assignment due tomorrow.")
    private String message;

    @NotNull(message = "Status must not be null")
    @Schema(description = "Current status of the notification")
    private NotificationStatus status;

    @Schema(description = "Timestamp of when the notification was created")
    private LocalDateTime createdAt;

    @Schema(description = "Timestamp of the last update to the notification")
    private LocalDateTime lastUpdate;
}
