package com.musicapp.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Entity
@Table(name = "device_sync", uniqueConstraints = {
        @UniqueConstraint(columnNames = {"user_id", "device_id"})
})
@Data
@NoArgsConstructor
@AllArgsConstructor
public class DeviceSync {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    @Column(name = "device_id", nullable = false, length = 150)
    private String deviceId;

    @Column(name = "last_synced_at", nullable = false)
    private LocalDateTime lastSyncedAt;

    @Column(name = "last_sync_version", length = 100)
    private String lastSyncVersion;

    @PrePersist
    protected void onCreate() {
        this.lastSyncedAt = LocalDateTime.now();
    }
}
