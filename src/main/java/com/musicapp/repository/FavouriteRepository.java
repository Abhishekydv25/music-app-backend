package com.musicapp.repository;

import com.musicapp.entity.Favourite;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface FavouriteRepository extends JpaRepository<Favourite, Long> {

    List<Favourite> findByUserIdOrderByAddedAtDesc(Long userId);

    Optional<Favourite> findByUserIdAndSongId(Long userId, Long songId);

    boolean existsByUserIdAndSongId(Long userId, Long songId);

    void deleteByUserIdAndSongId(Long userId, Long songId);
}
