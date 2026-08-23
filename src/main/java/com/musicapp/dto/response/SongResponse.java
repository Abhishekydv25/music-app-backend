package com.musicapp.dto.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class SongResponse {

    private Long id;
    private String title;
    private String artistName;
    private String albumTitle;
    private String genre;
    private Integer durationSec;
    private String audioUrl;
    private String coverImageUrl;
    private Boolean isFavourite; // populated per-request based on logged-in user
}
