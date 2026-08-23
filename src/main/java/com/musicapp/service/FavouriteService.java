package com.musicapp.service;

import com.musicapp.dto.response.SongResponse;
import com.musicapp.entity.Favourite;
import com.musicapp.entity.Song;
import com.musicapp.entity.User;
import com.musicapp.repository.FavouriteRepository;
import com.musicapp.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import java.util.List;
import java.util.Map;

@Service
@RequiredArgsConstructor
public class FavouriteService {

    private final FavouriteRepository favouriteRepository;
    private final UserRepository userRepository;
    private final SongService songService;
    private final SimpMessagingTemplate messagingTemplate;

    @Transactional
    public void addFavourite(Long userId, Long songId) {
        if (favouriteRepository.existsByUserIdAndSongId(userId, songId)) {
            return; // already favourited — idempotent
        }

        User user = userRepository.findById(userId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "User not found"));
        Song song = songService.getSongEntityOrThrow(songId);

        Favourite favourite = new Favourite();
        favourite.setUser(user);
        favourite.setSong(song);
        favouriteRepository.save(favourite);

        broadcastFavouriteChange(userId, songId, "ADDED");
    }

    @Transactional
    public void removeFavourite(Long userId, Long songId) {
        favouriteRepository.deleteByUserIdAndSongId(userId, songId);
        broadcastFavouriteChange(userId, songId, "REMOVED");
    }

    public List<SongResponse> getUserFavourites(Long userId) {
        return favouriteRepository.findByUserIdOrderByAddedAtDesc(userId).stream()
                .map(fav -> songService.getSongById(fav.getSong().getId(), userId))
                .toList();
    }

    // Only this user's other logged-in devices receive the update —
    // one person's phone shouldn't see another person's favourites change.
    private void broadcastFavouriteChange(Long userId, Long songId, String action) {
        messagingTemplate.convertAndSend(
                "/topic/user/" + userId + "/favourites",
                Map.of("songId", songId, "action", action));
    }
}