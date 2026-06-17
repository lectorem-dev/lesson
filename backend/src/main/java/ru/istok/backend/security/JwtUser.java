package ru.istok.backend.security;

import java.util.UUID;
import ru.istok.backend.user.entity.UserRole;

public record JwtUser(UUID userId, String login, UserRole role) {
}
