package com.musicapp.controller;

import com.musicapp.dto.response.SongResponse;
import com.musicapp.entity.Song;
import com.musicapp.service.SongService;
import lombok.RequiredArgsConstructor;
import org.springframework.core.io.FileSystemResource;
import org.springframework.core.io.Resource;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpRange;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.server.ResponseStatusException;

import java.io.File;
import java.util.List;

@RestController
@RequestMapping("/api/songs")
@RequiredArgsConstructor
public class SongController {

    private final SongService songService;

    @GetMapping
    public ResponseEntity<List<SongResponse>> getAllSongs() {
        return ResponseEntity.ok(songService.getAllSongs(CurrentUser.id()));
    }

    /**
     * Upload a new song: multipart form with metadata fields + the audio
     * file (required) + an optional cover image.
     */
    @PostMapping(consumes = "multipart/form-data")
    public ResponseEntity<SongResponse> uploadSong(
            @RequestParam String title,
            @RequestParam String artistName,
            @RequestParam(required = false) String genre,
            @RequestParam Integer durationSec,
            @RequestParam("audioFile") MultipartFile audioFile,
            @RequestParam(value = "coverFile", required = false) MultipartFile coverFile
    ) {
        SongResponse response = songService.uploadSong(
                title, artistName, genre, durationSec, audioFile, coverFile, CurrentUser.id());
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    @GetMapping("/search")
    public ResponseEntity<List<SongResponse>> search(@RequestParam String query) {
        return ResponseEntity.ok(songService.searchSongs(query, CurrentUser.id()));
    }

    @GetMapping("/genre/{genre}")
    public ResponseEntity<List<SongResponse>> byGenre(@PathVariable String genre) {
        return ResponseEntity.ok(songService.getSongsByGenre(genre, CurrentUser.id()));
    }

    @GetMapping("/{id}")
    public ResponseEntity<SongResponse> getSong(@PathVariable Long id) {
        return ResponseEntity.ok(songService.getSongById(id, CurrentUser.id()));
    }

    /**
     * Range-request audio streaming so the player can seek/scrub.
     * Assumes local file storage; for S3, swap FileSystemResource
     * for a pre-signed URL redirect instead.
     */
    @GetMapping("/{id}/stream")
    public ResponseEntity<Resource> stream(@PathVariable Long id,
                                           @RequestHeader HttpHeaders headers) {
        Song song = songService.getSongEntityOrThrow(id);
        File audioFile = new File("." + song.getAudioUrl()); // audioUrl e.g. /uploads/songs/xyz.mp3

        if (!audioFile.exists()) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, "Audio file missing on disk");
        }

        long fileLength = audioFile.length();
        List<HttpRange> ranges = headers.getRange();

        if (ranges.isEmpty()) {
            return ResponseEntity.ok()
                    .contentType(MediaType.parseMediaType("audio/mpeg"))
                    .contentLength(fileLength)
                    .header(HttpHeaders.ACCEPT_RANGES, "bytes")
                    .body(new FileSystemResource(audioFile));
        }

        HttpRange range = ranges.get(0);
        long start = range.getRangeStart(fileLength);
        long end = range.getRangeEnd(fileLength);
        long rangeLength = end - start + 1;

        return ResponseEntity.status(HttpStatus.PARTIAL_CONTENT)
                .contentType(MediaType.parseMediaType("audio/mpeg"))
                .header(HttpHeaders.ACCEPT_RANGES, "bytes")
                .header(HttpHeaders.CONTENT_RANGE, "bytes " + start + "-" + end + "/" + fileLength)
                .contentLength(rangeLength)
                .body(new FileSystemResource(audioFile));
    }
}