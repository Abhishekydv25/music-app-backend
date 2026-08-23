package com.musicapp.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Entity
@Table(name = "playlist_songs", uniqueConstraints = {
        @UniqueConstraint(columnNames = {"playlist_id", "song_id"})
})
@Data
@NoArgsConstructor
@AllArgsConstructor
public class PlaylistSong {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "playlist_id", nullable = false)
    private Playlist playlist;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "song_id", nullable = false)
    private Song song;

    @Column(nullable = false)
    private Integer position = 0;

    @Column(name = "added_at", updatable = false)
    private LocalDateTime addedAt;

    @PrePersist
    protected void onCreate() {
        this.addedAt = LocalDateTime.now();
    }
}
