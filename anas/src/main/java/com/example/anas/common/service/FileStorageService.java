package com.example.anas.common.service;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.util.UUID;

@Service
@Slf4j
public class FileStorageService {
    @Value("${file.upload-dir:uploads}")
    private String uploadDir;

    public String storeFile(MultipartFile file, String subdirectory) {
        try {
            Path uploadPath = Paths.get(uploadDir, subdirectory);
            if (!Files.exists(uploadPath)) {
                Files.createDirectories(uploadPath);
            }

            String originalFilename = file.getOriginalFilename();
            String extension = "";
            if (originalFilename != null && originalFilename.contains(".")) {
                extension = originalFilename.substring(originalFilename.lastIndexOf("."));
            }

            String filename = UUID.randomUUID().toString() + extension;
            Path filePath = uploadPath.resolve(filename);
            Files.copy(file.getInputStream(), filePath, StandardCopyOption.REPLACE_EXISTING);

            return "/files/" + subdirectory + "/" + filename;
        } catch (IOException e) {
            log.error("Ошибка при сохранении файла", e);
            throw new RuntimeException("Не удалось сохранить файл", e);
        }
    }

    public void deleteFile(String fileUrl) {
        try {
            if (fileUrl != null && fileUrl.startsWith("/files/")) {
                String relativePath = fileUrl.substring("/files/".length());
                Path filePath = Paths.get(uploadDir, relativePath);
                Files.deleteIfExists(filePath);
            }
        } catch (IOException e) {
            log.error("Ошибка при удалении файла", e);
        }
    }

    public Path getFilePath(String fileUrl) {
        if (fileUrl != null && fileUrl.startsWith("/files/")) {
            String relativePath = fileUrl.substring("/files/".length());
            return Paths.get(uploadDir, relativePath);
        }
        return null;
    }
}
