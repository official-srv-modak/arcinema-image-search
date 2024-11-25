package com.modakdev.arcinema_image_search.service.impl;

import com.modakdev.arcinema_image_search.client.UrlFetchClient;
import com.modakdev.arcinema_image_search.service.UrlFetchService;
import netscape.javascript.JSObject;
import org.json.simple.JSONObject;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.File;
import java.io.IOException;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.HashMap;
import java.util.Map;

@Service
public class UrlFetchServiceImpl implements UrlFetchService {
    @Value("${upload-path}")
    String UPLOAD_DIR;

    @Autowired
    UrlFetchClient client;

    @Override
    public ResponseEntity<Map<String, Object>> uploadFiles(MultipartFile imageFile) {
        Map<String, Object> response = new HashMap<>();

        if (imageFile.isEmpty()) {
            response.put("status", "error");
            response.put("message", "Please select a file!");
            response.put("name", imageFile.getOriginalFilename());
            return new ResponseEntity<>(response, HttpStatus.BAD_REQUEST);
        } else {
            try {
                // Ensure the upload directory exists
                uploadTransfers(imageFile, UPLOAD_DIR);

                response.put("status", "success");
                response.put("message", "File uploaded successfully");
                response.put("name", imageFile.getOriginalFilename());
                return new ResponseEntity<>(response, HttpStatus.OK);
            } catch (IOException e) {
                response.put("status", "error");
                response.put("message", "Failed to upload file: " + e.getMessage());
                response.put("name", null);
                return new ResponseEntity<>(response, HttpStatus.INTERNAL_SERVER_ERROR);
            }
        }
    }

    @Override
    public ResponseEntity<Map<String, Object>> getUrl(String imageFile) {
        JSONObject jsonObject = new JSONObject();
        jsonObject.put("image_name", imageFile);
        Object responseFromClient = client.getUrlResponse(jsonObject);
        if(responseFromClient instanceof Map)
            return new ResponseEntity<>((Map<String, Object>)responseFromClient, HttpStatus.OK);
        else {
            Map<String, Object> errorResponse = new HashMap<>();
            errorResponse.put("error", "Failed to get URL from client.");
            return new ResponseEntity<>(errorResponse, HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }


    private static void uploadTransfers(MultipartFile trainFile, String UPLOAD_DIR) throws IOException {
        Path path = Paths.get(UPLOAD_DIR);
        File uploadDir = path.toFile();

        if (!uploadDir.exists()) {
            uploadDir.mkdirs();
        }
        // Save the file locally
        String trainFilePath = UPLOAD_DIR + trainFile.getOriginalFilename();
        File dest = new File(trainFilePath);
        trainFile.transferTo(dest.toPath());
    }
}
