package com.musicapp.controller;

import com.musicapp.dto.request.ChangePasswordRequest;
import com.musicapp.dto.response.UserResponse;
import com.musicapp.service.UserService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

@RestController
@RequestMapping("/api/users")
@RequiredArgsConstructor
public class UserController {

    private final UserService userService;

    @GetMapping("/me")
    public ResponseEntity<UserResponse> getMyProfile() {
        return ResponseEntity.ok(userService.getProfile(CurrentUser.id()));
    }

    @PatchMapping("/me/avatar")
    public ResponseEntity<UserResponse> updateAvatar(@RequestParam String avatarUrl) {
        return ResponseEntity.ok(userService.updateAvatar(CurrentUser.id(), avatarUrl));
    }

    @PostMapping(value = "/me/avatar", consumes = "multipart/form-data")
    public ResponseEntity<UserResponse> uploadAvatar(@RequestParam("file") MultipartFile file) {
        return ResponseEntity.ok(userService.uploadAvatar(CurrentUser.id(), file));
    }

    @PatchMapping("/me/username")
    public ResponseEntity<UserResponse> updateUsername(@RequestParam String username) {
        return ResponseEntity.ok(userService.updateUsername(CurrentUser.id(), username));
    }

    @PatchMapping("/me/email")
    public ResponseEntity<UserResponse> updateEmail(@RequestParam String email) {
        return ResponseEntity.ok(userService.updateEmail(CurrentUser.id(), email));
    }

    @PatchMapping("/me/password")
    public ResponseEntity<Void> changePassword(@Valid @RequestBody ChangePasswordRequest request) {
        userService.changePassword(CurrentUser.id(), request.getCurrentPassword(), request.getNewPassword());
        return ResponseEntity.noContent().build();
    }
}