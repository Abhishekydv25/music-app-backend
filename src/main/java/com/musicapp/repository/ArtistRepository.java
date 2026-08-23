package com.musicapp.repository;

import com.musicapp.entity.Artist;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface ArtistRepository extends JpaRepository<Artist, Long> {

    List<Artist> findByNameContainingIgnoreCase(String name);
}
