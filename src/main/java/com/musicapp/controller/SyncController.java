package com.musicapp.controller;

import com.musicapp.service.SyncService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/sync")
@RequiredArgsConstructor
public class SyncController {

    private final SyncService syncService;

    /**
     * Mobile app calls this on reconnect/foreground to pull the latest
     * playlists + favourites (server is source of truth for reads).
     */
    @GetMapping("/pull")
    public ResponseEntity<SyncService.SyncPullResponse> pull(@RequestParam String deviceId) {
        return ResponseEntity.ok(syncService.pull(CurrentUser.id(), deviceId));
    }

    /**
     * Mobile app calls this to flush its offline action queue
     * (favourite toggles made while offline, etc.) once back online.
     */
    @PostMapping("/push")
    public ResponseEntity<Void> push(@RequestBody List<SyncService.OfflineAction> actions) {
        syncService.push(CurrentUser.id(), actions);
        return ResponseEntity.noContent().build();
    }
}
