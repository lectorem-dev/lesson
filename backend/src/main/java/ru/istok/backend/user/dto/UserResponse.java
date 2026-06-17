package ru.istok.backend.user.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import java.time.LocalDateTime;
import java.util.UUID;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import ru.istok.backend.user.entity.UserRole;
import ru.istok.backend.user.entity.UserStatus;

@Getter
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "Данные пользователя")
public class UserResponse {

    @Schema(description = "Идентификатор пользователя", example = "550e8400-e29b-41d4-a716-446655440000")
    private UUID id;

    @Schema(description = "Имя пользователя", example = "Иван Иванов")
    private String name;

    @Schema(description = "Логин пользователя", example = "student")
    private String login;

    @Schema(description = "Роль пользователя: ADMIN или STUDENT", example = "STUDENT")
    private UserRole role;

    @Schema(description = "Статус пользователя: ACTIVE или ARCHIVED", example = "ACTIVE")
    private UserStatus status;

    @Schema(description = "Дата создания пользователя")
    private LocalDateTime createdAt;

    @Schema(description = "Дата последнего обновления пользователя")
    private LocalDateTime updatedAt;
}
