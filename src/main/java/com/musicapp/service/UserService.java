package com.musicapp.service;

import com.musicapp.dto.response.UserResponse;
import com.musicapp.entity.User;
import com.musicapp.repository.UserRepository;
import jakarta.validation.constraints.Email;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.server.ResponseStatusException;

@Service
@RequiredArgsConstructor
public class UserService {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final FileStorageService fileStorageService;

    public UserResponse getProfile(Long userId) {
        User user = findUserOrThrow(userId);
        return toResponse(user);
    }

    @Transactional
    public UserResponse updateAvatar(Long userId, String avatarUrl) {
        User user = findUserOrThrow(userId);
        user.setAvatarUrl(avatarUrl);
        return toResponse(userRepository.save(user));
    }

    @Transactional
    public UserResponse uploadAvatar(Long userId, MultipartFile file) {
        String avatarUrl = fileStorageService.storeFile(file, "avatars");
        return updateAvatar(userId, avatarUrl);
    }

    @Transactional
    public UserResponse updateUsername(Long userId, String newUsername) {
        User user = findUserOrThrow(userId);

        if (!user.getUsername().equals(newUsername) && userRepository.existsByUsername(newUsername)) {
            throw new ResponseStatusException(HttpStatus.CONFLICT, "Username already taken");
        }

        user.setUsername(newUsername);
        return toResponse(userRepository.save(user));
    }

    @Transactional
    public UserResponse updateEmail(Long userId, String newEmail) {
        User user = findUserOrThrow(userId);

        if (!user.getEmail().equalsIgnoreCase(newEmail) && userRepository.existsByEmail(newEmail)) {
            throw new ResponseStatusException(HttpStatus.CONFLICT, "Email already in use");
        }

        user.setEmail(newEmail);
        return toResponse(userRepository.save(user));
    }

    @Transactional
    public void changePassword(Long userId, String currentPassword, String newPassword) {
        User user = findUserOrThrow(userId);

        if (!passwordEncoder.matches(currentPassword, user.getPassword())) {
            throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, "Current password is incorrect");
        }

        user.setPassword(passwordEncoder.encode(newPassword));
        userRepository.save(user);
    }

    private User findUserOrThrow(Long userId) {
        return userRepository.findById(userId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "User not found"));
    }

    private UserResponse toResponse(User user) {
        return UserResponse.builder()
                .id(user.getId())
                .username(user.getUsername())
                .email(user.getEmail())
                .avatarUrl(user.getAvatarUrl())
                .build();
    }
}