package org.una.programmingIII.UTEMP_Project.dtos;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.*;
import org.una.programmingIII.UTEMP_Project.models.NotificationStatus;

import java.time.LocalDateTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class NotificationDTO {

    private Long id;

    @Builder.Default
    private UserDTO user = new UserDTO();

    @NotBlank(message = "Message must not be blank")
    @Size(max = 500, message = "Message must be at most 500 characters long")
    private String message;

    @NotNull(message = "Status must not be null")
    private NotificationStatus status;

    private LocalDateTime createdAt;

    private LocalDateTime lastUpdate;
}
