package ru.istok.backend.user.service;

import java.util.UUID;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;
import ru.istok.backend.common.exception.LoginAlreadyExistsException;
import ru.istok.backend.common.exception.UserNotFoundException;
import ru.istok.backend.user.dto.UserCreateRequest;
import ru.istok.backend.user.dto.UserResponse;
import ru.istok.backend.user.dto.UserUpdateRequest;
import ru.istok.backend.user.entity.User;
import ru.istok.backend.user.entity.UserStatus;
import ru.istok.backend.user.mapper.UserMapper;
import ru.istok.backend.user.repository.UserRepository;

@Service
@RequiredArgsConstructor
public class UserService {

    private final UserRepository userRepository;
    private final UserMapper userMapper;
    private final PasswordEncoder passwordEncoder;

    @Transactional(readOnly = true)
    public List<UserResponse> findAll() {
        return userRepository.findAll()
                .stream()
                .map(userMapper::toResponse)
                .toList();
    }

    @Transactional(readOnly = true)
    public UserResponse findById(UUID id) {
        return userMapper.toResponse(getUser(id));
    }

    @Transactional
    public UserResponse create(UserCreateRequest request) {
        validateLoginIsFree(request.getLogin());

        User user = new User();
        user.setName(request.getName());
        user.setLogin(request.getLogin());
        user.setPasswordHash(passwordEncoder.encode(request.getPassword()));
        user.setRole(request.getRole());
        user.setStatus(UserStatus.ACTIVE);

        return userMapper.toResponse(userRepository.save(user));
    }

    @Transactional
    public UserResponse update(UUID id, UserUpdateRequest request) {
        User user = getUser(id);
        validateLoginIsFreeForUpdate(request.getLogin(), id);

        user.setName(request.getName());
        user.setLogin(request.getLogin());
        user.setRole(request.getRole());
        user.setStatus(request.getStatus());

        if (StringUtils.hasText(request.getPassword())) {
            user.setPasswordHash(passwordEncoder.encode(request.getPassword()));
        }

        return userMapper.toResponse(user);
    }

    @Transactional
    public void archive(UUID id) {
        User user = getUser(id);
        user.setStatus(UserStatus.ARCHIVED);
    }

    private User getUser(UUID id) {
        return userRepository.findById(id)
                .orElseThrow(UserNotFoundException::new);
    }

    private void validateLoginIsFree(String login) {
        if (userRepository.existsByLogin(login)) {
            throw new LoginAlreadyExistsException();
        }
    }

    private void validateLoginIsFreeForUpdate(String login, UUID userId) {
        if (userRepository.existsByLoginAndIdNot(login, userId)) {
            throw new LoginAlreadyExistsException();
        }
    }
}
