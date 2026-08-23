package com.musicapp.repository;

import com.musicapp.entity.PlaylistSong;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface PlaylistSongRepository extends JpaRepository<PlaylistSong, Long> {

    List<PlaylistSong> findByPlaylistIdOrderByPositionAsc(Long playlistId);

    Optional<PlaylistSong> findByPlaylistIdAndSongId(Long playlistId, Long songId);

    void deleteByPlaylistIdAndSongId(Long playlistId, Long songId);

    long countByPlaylistId(Long playlistId);
}
