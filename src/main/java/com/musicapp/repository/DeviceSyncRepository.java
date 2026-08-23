package com.musicapp.repository;

import com.musicapp.entity.DeviceSync;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface DeviceSyncRepository extends JpaRepository<DeviceSync, Long> {

    Optional<DeviceSync> findByUserIdAndDeviceId(Long userId, String deviceId);
}
