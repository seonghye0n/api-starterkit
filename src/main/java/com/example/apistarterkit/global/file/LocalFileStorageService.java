package com.example.apistarterkit.global.file;

import com.example.apistarterkit.global.exception.CustomException;
import com.example.apistarterkit.global.exception.ErrorCode;
import java.io.IOException;
import java.io.InputStream;
import java.net.MalformedURLException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.util.UUID;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.Resource;
import org.springframework.core.io.UrlResource;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

/**
 * 로컬 디스크 기반 파일 저장 구현체(프로필 이미지 등 범용 업로드 예시).
 * S3 등 외부 스토리지로 교체하려면 {@link FileStorageService}를 구현한 클래스로 바꿔 끼우면 된다.
 */
@Slf4j
@Service
public class LocalFileStorageService implements FileStorageService {

    private final Path rootLocation;

    public LocalFileStorageService(@Value("${app.file.upload-dir}") String uploadDir) {
        this.rootLocation = Paths.get(uploadDir).toAbsolutePath().normalize();
        try {
            Files.createDirectories(rootLocation);
        } catch (IOException e) {
            throw new CustomException(ErrorCode.FILE_UPLOAD_FAILED);
        }
    }

    @Override
    public String store(MultipartFile file) {
        String originalName = file.getOriginalFilename();
        String storedFileName = UUID.randomUUID() + extractExtension(originalName);

        try (InputStream inputStream = file.getInputStream()) {
            Path destination = rootLocation.resolve(storedFileName).normalize();
            Files.copy(inputStream, destination, StandardCopyOption.REPLACE_EXISTING);
            return storedFileName;
        } catch (IOException e) {
            log.error("파일 저장 실패: {}", originalName, e);
            throw new CustomException(ErrorCode.FILE_UPLOAD_FAILED);
        }
    }

    @Override
    public Resource loadAsResource(String storedFileName) {
        try {
            Path file = rootLocation.resolve(storedFileName).normalize();
            Resource resource = new UrlResource(file.toUri());
            if (!resource.exists() || !resource.isReadable()) {
                throw new CustomException(ErrorCode.FILE_NOT_FOUND);
            }
            return resource;
        } catch (MalformedURLException e) {
            throw new CustomException(ErrorCode.FILE_NOT_FOUND);
        }
    }

    private String extractExtension(String originalName) {
        if (originalName == null || !originalName.contains(".")) {
            return "";
        }
        return originalName.substring(originalName.lastIndexOf('.'));
    }
}
