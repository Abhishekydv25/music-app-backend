package com.musicapp.dto.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class PlaylistResponse {

    private Long id;
    private String name;
    private Boolean isPublic;
    private String coverImageUrl;
    private LocalDateTime createdAt;
    private List<SongResponse> songs;
    private Integer songCount;
}
