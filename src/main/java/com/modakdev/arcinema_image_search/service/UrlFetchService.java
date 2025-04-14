package com.modakdev.arcinema_image_search.service;

import org.springframework.http.ResponseEntity;
import org.springframework.web.multipart.MultipartFile;

import java.util.Map;

public interface UrlFetchService {
    public ResponseEntity<Map<String, Object>> uploadFiles(MultipartFile imageFile);
    public ResponseEntity<Map<String, Object>> getUrl(String imageFile, int flag);
}
