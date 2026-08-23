package com.musicapp.service;

import com.musicapp.dto.response.PlaylistResponse;
import com.musicapp.dto.response.SongResponse;
import com.musicapp.entity.DeviceSync;
import com.musicapp.entity.User;
import com.musicapp.repository.DeviceSyncRepository;
import com.musicapp.repository.UserRepository;
import lombok.Builder;
import lombok.Data;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import java.time.LocalDateTime;
import java.util.List;

@Service
@RequiredArgsConstructor
public class SyncService {

    private final DeviceSyncRepository deviceSyncRepository;
    private final UserRepository userRepository;
    private final PlaylistService playlistService;
    private final FavouriteService favouriteService;

    /**
     * Called when a device (mobile app) reconnects — returns everything
     * that changed since its last known sync point.
     */
    @Transactional
    public SyncPullResponse pull(Long userId, String deviceId) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "User not found"));

        List<PlaylistResponse> playlists = playlistService.getUserPlaylists(userId);
        List<SongResponse> favourites = favouriteService.getUserFavourites(userId);

        DeviceSync deviceSync = deviceSyncRepository.findByUserIdAndDeviceId(userId, deviceId)
                .orElseGet(() -> {
                    DeviceSync ds = new DeviceSync();
                    ds.setUser(user);
                    ds.setDeviceId(deviceId);
                    return ds;
                });
        deviceSync.setLastSyncedAt(LocalDateTime.now());
        deviceSyncRepository.save(deviceSync);

        return SyncPullResponse.builder()
                .playlists(playlists)
                .favourites(favourites)
                .syncedAt(deviceSync.getLastSyncedAt())
                .build();
    }

    /**
     * Called when a device comes back online and needs to push
     * offline-queued actions (favourite toggles, playlist edits) to the server.
     * Each action is applied idempotently — safe to replay on retry.
     */
    @Transactional
    public void push(Long userId, List<OfflineAction> actions) {
        for (OfflineAction action : actions) {
            switch (action.getType()) {
                case "ADD_FAVOURITE" -> favouriteService.addFavourite(userId, action.getSongId());
                case "REMOVE_FAVOURITE" -> favouriteService.removeFavourite(userId, action.getSongId());
                default -> throw new ResponseStatusException(
                        HttpStatus.BAD_REQUEST, "Unknown offline action type: " + action.getType());
            }
        }
    }

    @Data
    @Builder
    public static class SyncPullResponse {
        private List<PlaylistResponse> playlists;
        private List<SongResponse> favourites;
        private LocalDateTime syncedAt;
    }

    @Data
    public static class OfflineAction {
        private String type;      // ADD_FAVOURITE | REMOVE_FAVOURITE | ...
        private Long songId;
        private Long playlistId;
        private LocalDateTime clientTimestamp;
    }
}
