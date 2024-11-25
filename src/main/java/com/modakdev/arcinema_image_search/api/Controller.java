package com.modakdev.arcinema_image_search.api;

import com.modakdev.arcinema_image_search.service.impl.UrlFetchServiceImpl;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

@RestController
public class Controller {
    @Autowired
    private UrlFetchServiceImpl urlService;

    @PostMapping("/upload-files")
    public ResponseEntity<Map<String,Object>> uploadFiles(@RequestParam("imageFile") MultipartFile imageFile) {
        return urlService.uploadFiles(imageFile);
    }

    @PostMapping("/get-url")
    public ResponseEntity<Map<String,Object>> getUrl(@RequestParam("imageFile") MultipartFile imageFile) {
        Map<String, Object> map = urlService.uploadFiles(imageFile).getBody();
        if(map != null)
        {
            ResponseEntity<Map<String, Object>> responseEntity = urlService.getUrl(String.valueOf(map.get("name")));
            return responseEntity;
        }
        else
        {
            Map<String, Object> map1 = new ConcurrentHashMap<>();
            map1.put("error", "some error");
            return new ResponseEntity<>(map1, HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }
}
