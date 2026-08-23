package com.musicapp.controller;

import com.musicapp.dto.response.SongResponse;
import com.musicapp.service.FavouriteService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/favourites")
@RequiredArgsConstructor
public class FavouriteController {

    private final FavouriteService favouriteService;

    @GetMapping("/me")
    public ResponseEntity<List<SongResponse>> myFavourites() {
        return ResponseEntity.ok(favouriteService.getUserFavourites(CurrentUser.id()));
    }

    @PostMapping("/{songId}")
    public ResponseEntity<Void> add(@PathVariable Long songId) {
        favouriteService.addFavourite(CurrentUser.id(), songId);
        return ResponseEntity.noContent().build();
    }

    @DeleteMapping("/{songId}")
    public ResponseEntity<Void> remove(@PathVariable Long songId) {
        favouriteService.removeFavourite(CurrentUser.id(), songId);
        return ResponseEntity.noContent().build();
    }
}
