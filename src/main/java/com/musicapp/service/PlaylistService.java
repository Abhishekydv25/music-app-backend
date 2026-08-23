package com.musicapp.service;

import com.musicapp.dto.request.PlaylistRequest;
import com.musicapp.dto.response.PlaylistResponse;
import com.musicapp.dto.response.SongResponse;
import com.musicapp.entity.*;
import com.musicapp.repository.PlaylistRepository;
import com.musicapp.repository.PlaylistSongRepository;
import com.musicapp.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import java.util.List;

@Service
@RequiredArgsConstructor
public class PlaylistService {

    private final PlaylistRepository playlistRepository;
    private final PlaylistSongRepository playlistSongRepository;
    private final UserRepository userRepository;
    private final SongService songService;

    @Transactional
    public PlaylistResponse createPlaylist(Long userId, PlaylistRequest request) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "User not found"));

        Playlist playlist = new Playlist();
        playlist.setName(request.getName());
        playlist.setIsPublic(Boolean.TRUE.equals(request.getIsPublic()));
        playlist.setCoverImageUrl(request.getCoverImageUrl());
        playlist.setUser(user);

        Playlist saved = playlistRepository.save(playlist);
        return toResponse(saved);
    }

    public List<PlaylistResponse> getUserPlaylists(Long userId) {
        return playlistRepository.findByUserId(userId).stream()
                .map(this::toResponse)
                .toList();
    }

    public PlaylistResponse getPlaylistById(Long playlistId, Long requestingUserId) {
        Playlist playlist = findPlaylistOrThrow(playlistId);
        assertOwnerOrPublic(playlist, requestingUserId);
        return toResponse(playlist);
    }

    @Transactional
    public PlaylistResponse updatePlaylist(Long playlistId, Long userId, PlaylistRequest request) {
        Playlist playlist = findPlaylistOrThrow(playlistId);
        assertOwner(playlist, userId);

        playlist.setName(request.getName());
        playlist.setIsPublic(Boolean.TRUE.equals(request.getIsPublic()));
        if (request.getCoverImageUrl() != null) {
            playlist.setCoverImageUrl(request.getCoverImageUrl());
        }

        return toResponse(playlistRepository.save(playlist));
    }

    @Transactional
    public void deletePlaylist(Long playlistId, Long userId) {
        Playlist playlist = findPlaylistOrThrow(playlistId);
        assertOwner(playlist, userId);
        playlistRepository.delete(playlist);
    }

    @Transactional
    public PlaylistResponse addSongToPlaylist(Long playlistId, Long songId, Long userId) {
        Playlist playlist = findPlaylistOrThrow(playlistId);
        assertOwner(playlist, userId);

        if (playlistSongRepository.findByPlaylistIdAndSongId(playlistId, songId).isPresent()) {
            throw new ResponseStatusException(HttpStatus.CONFLICT, "Song already in playlist");
        }

        Song song = songService.getSongEntityOrThrow(songId);

        PlaylistSong playlistSong = new PlaylistSong();
        playlistSong.setPlaylist(playlist);
        playlistSong.setSong(song);
        playlistSong.setPosition((int) playlistSongRepository.countByPlaylistId(playlistId));
        playlistSongRepository.save(playlistSong);

        return toResponse(playlistRepository.findById(playlistId).orElseThrow());
    }

    @Transactional
    public void removeSongFromPlaylist(Long playlistId, Long songId, Long userId) {
        Playlist playlist = findPlaylistOrThrow(playlistId);
        assertOwner(playlist, userId);
        playlistSongRepository.deleteByPlaylistIdAndSongId(playlistId, songId);
    }

    private Playlist findPlaylistOrThrow(Long playlistId) {
        return playlistRepository.findById(playlistId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Playlist not found"));
    }

    private void assertOwner(Playlist playlist, Long userId) {
        if (!playlist.getUser().getId().equals(userId)) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, "You don't own this playlist");
        }
    }

    private void assertOwnerOrPublic(Playlist playlist, Long requestingUserId) {
        boolean isOwner = playlist.getUser().getId().equals(requestingUserId);
        if (!isOwner && !Boolean.TRUE.equals(playlist.getIsPublic())) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, "This playlist is private");
        }
    }

    private PlaylistResponse toResponse(Playlist playlist) {
        List<SongResponse> songs = playlistSongRepository.findByPlaylistIdOrderByPositionAsc(playlist.getId())
                .stream()
                .map(ps -> songService.getSongById(ps.getSong().getId(), playlist.getUser().getId()))
                .toList();

        return PlaylistResponse.builder()
                .id(playlist.getId())
                .name(playlist.getName())
                .isPublic(playlist.getIsPublic())
                .coverImageUrl(playlist.getCoverImageUrl())
                .createdAt(playlist.getCreatedAt())
                .songs(songs)
                .songCount(songs.size())
                .build();
    }
}
