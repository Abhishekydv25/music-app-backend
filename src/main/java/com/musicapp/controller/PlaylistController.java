package com.musicapp.controller;

import com.musicapp.dto.request.PlaylistRequest;
import com.musicapp.dto.response.PlaylistResponse;
import com.musicapp.service.PlaylistService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/playlists")
@RequiredArgsConstructor
public class PlaylistController {

    private final PlaylistService playlistService;

    @PostMapping
    public ResponseEntity<PlaylistResponse> create(@Valid @RequestBody PlaylistRequest request) {
        PlaylistResponse response = playlistService.createPlaylist(CurrentUser.id(), request);
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    @GetMapping("/me")
    public ResponseEntity<List<PlaylistResponse>> myPlaylists() {
        return ResponseEntity.ok(playlistService.getUserPlaylists(CurrentUser.id()));
    }

    @GetMapping("/{id}")
    public ResponseEntity<PlaylistResponse> getById(@PathVariable Long id) {
        return ResponseEntity.ok(playlistService.getPlaylistById(id, CurrentUser.id()));
    }

    @PutMapping("/{id}")
    public ResponseEntity<PlaylistResponse> update(@PathVariable Long id,
                                                     @Valid @RequestBody PlaylistRequest request) {
        return ResponseEntity.ok(playlistService.updatePlaylist(id, CurrentUser.id(), request));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> delete(@PathVariable Long id) {
        playlistService.deletePlaylist(id, CurrentUser.id());
        return ResponseEntity.noContent().build();
    }

    @PostMapping("/{id}/songs/{songId}")
    public ResponseEntity<PlaylistResponse> addSong(@PathVariable Long id, @PathVariable Long songId) {
        return ResponseEntity.ok(playlistService.addSongToPlaylist(id, songId, CurrentUser.id()));
    }

    @DeleteMapping("/{id}/songs/{songId}")
    public ResponseEntity<Void> removeSong(@PathVariable Long id, @PathVariable Long songId) {
        playlistService.removeSongFromPlaylist(id, songId, CurrentUser.id());
        return ResponseEntity.noContent().build();
    }
}
