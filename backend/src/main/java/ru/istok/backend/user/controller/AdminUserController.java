package ru.istok.backend.user.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.parameters.RequestBody;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import java.util.List;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;
import ru.istok.backend.user.dto.UserCreateRequest;
import ru.istok.backend.user.dto.UserResponse;
import ru.istok.backend.user.dto.UserUpdateRequest;
import ru.istok.backend.user.service.UserService;

@RestController
@RequestMapping("/api/admin/users")
@RequiredArgsConstructor
@Tag(name = "Пользователи", description = "Управление пользователями администратором")
public class AdminUserController {

    private final UserService userService;

    @GetMapping
    @Operation(summary = "Получение списка пользователей")
    @ApiResponse(responseCode = "200", description = "Список пользователей")
    public List<UserResponse> getUsers() {
        return userService.findAll();
    }

    @GetMapping("/{id}")
    @Operation(summary = "Получение пользователя по идентификатору")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Данные пользователя"),
            @ApiResponse(responseCode = "404", description = "Пользователь не найден")
    })
    public UserResponse getUser(
            @Parameter(description = "Идентификатор пользователя", required = true)
            @PathVariable UUID id
    ) {
        return userService.findById(id);
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    @Operation(summary = "Создание пользователя администратором")
    @ApiResponses({
            @ApiResponse(responseCode = "201", description = "Пользователь создан"),
            @ApiResponse(responseCode = "400", description = "Некорректные данные пользователя"),
            @ApiResponse(responseCode = "409", description = "Логин уже занят")
    })
    public UserResponse createUser(
            @Valid
            @RequestBody(description = "Данные нового пользователя", required = true)
            @org.springframework.web.bind.annotation.RequestBody UserCreateRequest request
    ) {
        return userService.create(request);
    }

    @PutMapping("/{id}")
    @Operation(summary = "Обновление пользователя")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Пользователь обновлен"),
            @ApiResponse(responseCode = "400", description = "Некорректные данные пользователя"),
            @ApiResponse(responseCode = "404", description = "Пользователь не найден"),
            @ApiResponse(responseCode = "409", description = "Логин уже занят")
    })
    public UserResponse updateUser(
            @Parameter(description = "Идентификатор пользователя", required = true)
            @PathVariable UUID id,
            @Valid
            @RequestBody(description = "Новые данные пользователя", required = true)
            @org.springframework.web.bind.annotation.RequestBody UserUpdateRequest request
    ) {
        return userService.update(id, request);
    }

    @DeleteMapping("/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    @Operation(summary = "Архивирование пользователя")
    @ApiResponses({
            @ApiResponse(responseCode = "204", description = "Пользователь переведен в архив"),
            @ApiResponse(responseCode = "404", description = "Пользователь не найден")
    })
    public void archiveUser(
            @Parameter(description = "Идентификатор пользователя", required = true)
            @PathVariable UUID id
    ) {
        userService.archive(id);
    }
}
