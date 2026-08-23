package com.musicapp.repository;

import com.musicapp.entity.Playlist;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface PlaylistRepository extends JpaRepository<Playlist, Long> {

    List<Playlist> findByUserId(Long userId);

    List<Playlist> findByUserIdAndIsPublicTrue(Long userId);
}
