package com.musicapp.dto.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Data;

@Data
public class PlaylistRequest {

    @NotBlank(message = "Playlist name is required")
    @Size(max = 150, message = "Playlist name must be under 150 characters")
    private String name;

    private Boolean isPublic = false;

    private String coverImageUrl;
}
