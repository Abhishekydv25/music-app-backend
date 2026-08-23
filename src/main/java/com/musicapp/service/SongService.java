package com.musicapp.service;

import com.musicapp.dto.response.SongResponse;
import com.musicapp.entity.Artist;
import com.musicapp.entity.Song;
import com.musicapp.repository.ArtistRepository;
import com.musicapp.repository.FavouriteRepository;
import com.musicapp.repository.SongRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.server.ResponseStatusException;

import java.util.List;

@Service
@RequiredArgsConstructor
public class SongService {

    private final SongRepository songRepository;
    private final FavouriteRepository favouriteRepository;
    private final ArtistRepository artistRepository;
    private final FileStorageService fileStorageService;
    private final SimpMessagingTemplate messagingTemplate;

    @Transactional
    public SongResponse uploadSong(
            String title,
            String artistName,
            String genre,
            Integer durationSec,
            MultipartFile audioFile,
            MultipartFile coverFile,
            Long currentUserId
    ) {
        if (audioFile == null || audioFile.isEmpty()) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Audio file is required");
        }

        // Find or create the artist by name (case-insensitive match on existing list)
        Artist artist = artistRepository.findByNameContainingIgnoreCase(artistName).stream()
                .filter(a -> a.getName().equalsIgnoreCase(artistName))
                .findFirst()
                .orElseGet(() -> {
                    Artist newArtist = new Artist();
                    newArtist.setName(artistName);
                    newArtist.setGenre(genre);
                    return artistRepository.save(newArtist);
                });

        String audioUrl = fileStorageService.storeFile(audioFile, "songs");
        String coverUrl = (coverFile != null && !coverFile.isEmpty())
                ? fileStorageService.storeFile(coverFile, "covers")
                : null;

        Song song = new Song();
        song.setTitle(title);
        song.setArtist(artist);
        song.setGenre(genre);
        song.setDurationSec(durationSec);
        song.setAudioUrl(audioUrl);
        song.setCoverImageUrl(coverUrl);

        Song saved = songRepository.save(song);
        SongResponse response = toResponse(saved, currentUserId);

        // Broadcast to every connected device (web + mobile) so their song
        // list auto-updates without a manual refresh — this is the core of
        // the "auto sync" behavior.
        messagingTemplate.convertAndSend("/topic/songs/added", response);

        return response;
    }

    public List<SongResponse> getAllSongs(Long currentUserId) {
        return songRepository.findAll().stream()
                .map(song -> toResponse(song, currentUserId))
                .toList();
    }

    public List<SongResponse> searchSongs(String query, Long currentUserId) {
        return songRepository.findByTitleContainingIgnoreCase(query).stream()
                .map(song -> toResponse(song, currentUserId))
                .toList();
    }

    public List<SongResponse> getSongsByGenre(String genre, Long currentUserId) {
        return songRepository.findByGenreIgnoreCase(genre).stream()
                .map(song -> toResponse(song, currentUserId))
                .toList();
    }

    public SongResponse getSongById(Long songId, Long currentUserId) {
        Song song = songRepository.findById(songId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Song not found"));
        return toResponse(song, currentUserId);
    }

    public Song getSongEntityOrThrow(Long songId) {
        return songRepository.findById(songId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Song not found"));
    }

    private SongResponse toResponse(Song song, Long currentUserId) {
        boolean isFavourite = currentUserId != null
                && favouriteRepository.existsByUserIdAndSongId(currentUserId, song.getId());

        return SongResponse.builder()
                .id(song.getId())
                .title(song.getTitle())
                .artistName(song.getArtist() != null ? song.getArtist().getName() : null)
                .albumTitle(song.getAlbum() != null ? song.getAlbum().getTitle() : null)
                .genre(song.getGenre())
                .durationSec(song.getDurationSec())
                .audioUrl(song.getAudioUrl())
                .coverImageUrl(song.getCoverImageUrl())
                .isFavourite(isFavourite)
                .build();
    }
}