package com.musicapp.service;

import com.musicapp.dto.request.LoginRequest;
import com.musicapp.dto.request.RegisterRequest;
import com.musicapp.dto.response.AuthResponse;
import com.musicapp.dto.response.UserResponse;
import com.musicapp.entity.RefreshToken;
import com.musicapp.entity.User;
import com.musicapp.repository.RefreshTokenRepository;
import com.musicapp.repository.UserRepository;
import com.musicapp.security.JwtUtil;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import java.time.LocalDateTime;

@Service
@RequiredArgsConstructor
public class AuthService {

    private final UserRepository userRepository;
    private final RefreshTokenRepository refreshTokenRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtUtil jwtUtil;

    @Transactional
    public AuthResponse register(RegisterRequest request) {
        if (userRepository.existsByEmail(request.getEmail())) {
            throw new ResponseStatusException(HttpStatus.CONFLICT, "Email already registered");
        }
        if (userRepository.existsByUsername(request.getUsername())) {
            throw new ResponseStatusException(HttpStatus.CONFLICT, "Username already taken");
        }

        User user = new User();
        user.setUsername(request.getUsername());
        user.setEmail(request.getEmail());
        user.setPassword(passwordEncoder.encode(request.getPassword()));

        User savedUser = userRepository.save(user);

        return buildAuthResponse(savedUser);
    }

    @Transactional
    public AuthResponse login(LoginRequest request) {
        User user = userRepository.findByEmail(request.getEmail())
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.UNAUTHORIZED, "Invalid email or password"));

        if (!passwordEncoder.matches(request.getPassword(), user.getPassword())) {
            throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, "Invalid email or password");
        }

        return buildAuthResponse(user);
    }

    @Transactional
    public AuthResponse refreshToken(String refreshTokenValue) {
        RefreshToken storedToken = refreshTokenRepository.findByToken(refreshTokenValue)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.UNAUTHORIZED, "Invalid refresh token"));

        if (storedToken.isExpired()) {
            refreshTokenRepository.delete(storedToken);
            throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, "Refresh token expired, please log in again");
        }

        User user = storedToken.getUser();
        String newAccessToken = jwtUtil.generateAccessToken(user.getId(), user.getEmail());

        return AuthResponse.builder()
                .accessToken(newAccessToken)
                .refreshToken(refreshTokenValue) // reuse existing valid refresh token
                .user(toUserResponse(user))
                .build();
    }

    @Transactional
    public void logout(String refreshTokenValue) {
        refreshTokenRepository.findByToken(refreshTokenValue)
                .ifPresent(refreshTokenRepository::delete);
    }

    private AuthResponse buildAuthResponse(User user) {
        String accessToken = jwtUtil.generateAccessToken(user.getId(), user.getEmail());
        String refreshTokenValue = jwtUtil.generateRefreshToken(user.getId(), user.getEmail());

        RefreshToken refreshToken = new RefreshToken();
        refreshToken.setUser(user);
        refreshToken.setToken(refreshTokenValue);
        refreshToken.setExpiresAt(LocalDateTime.now().plusSeconds(jwtUtil.getRefreshTokenExpiryMs() / 1000));
        refreshTokenRepository.save(refreshToken);

        return AuthResponse.builder()
                .accessToken(accessToken)
                .refreshToken(refreshTokenValue)
                .user(toUserResponse(user))
                .build();
    }

    private UserResponse toUserResponse(User user) {
        return UserResponse.builder()
                .id(user.getId())
                .username(user.getUsername())
                .email(user.getEmail())
                .avatarUrl(user.getAvatarUrl())
                .build();
    }
}
