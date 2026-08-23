package com.musicapp.service;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.server.ResponseStatusException;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.UUID;

/**
 * Local disk storage for dev. Swap the internals for an S3Service
 * implementing the same method signatures when moving to production
 * (see app.storage.provider in application-prod.yml).
 */
@Service
public class FileStorageService {

    @Value("${app.storage.local-path}")
    private String storagePath;

    public String storeFile(MultipartFile file, String subfolder) {
        if (file.isEmpty()) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "File is empty");
        }

        try {
            Path folderPath = Paths.get(storagePath, subfolder);
            Files.createDirectories(folderPath);

            String originalName = StringUtils.cleanPath(file.getOriginalFilename() != null
                    ? file.getOriginalFilename() : "file");
            String extension = originalName.contains(".")
                    ? originalName.substring(originalName.lastIndexOf("."))
                    : "";
            String storedFilename = UUID.randomUUID() + extension;

            Path targetPath = folderPath.resolve(storedFilename);
            Files.copy(file.getInputStream(), targetPath);

            return "/uploads/" + subfolder + "/" + storedFilename;
        } catch (IOException e) {
            throw new ResponseStatusException(HttpStatus.INTERNAL_SERVER_ERROR, "Failed to store file: " + e.getMessage());
        }
    }

    public void deleteFile(String fileUrl) {
        try {
            String relativePath = fileUrl.replaceFirst("^/uploads/", "");
            Path targetPath = Paths.get(storagePath, relativePath);
            Files.deleteIfExists(targetPath);
        } catch (IOException e) {
            // Log and continue — a failed cleanup shouldn't break the calling operation
        }
    }
}
