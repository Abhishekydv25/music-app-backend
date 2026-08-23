package com.musicapp.dto.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class AlbumResponse {

    private Long id;
    private String title;
    private String artistName;
    private String coverImageUrl;
    private Integer releaseYear;
}
