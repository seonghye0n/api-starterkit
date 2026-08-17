package com.example.apistarterkit.global.file;

import org.springframework.core.io.Resource;
import org.springframework.web.multipart.MultipartFile;

public interface FileStorageService {

    String store(MultipartFile file);

    Resource loadAsResource(String storedFileName);
}
