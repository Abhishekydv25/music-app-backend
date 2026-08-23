package com.musicapp.repository;

import com.musicapp.entity.Song;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface SongRepository extends JpaRepository<Song, Long> {

    List<Song> findByTitleContainingIgnoreCase(String title);

    List<Song> findByGenreIgnoreCase(String genre);

    List<Song> findByArtistId(Long artistId);

    List<Song> findByAlbumId(Long albumId);
}
