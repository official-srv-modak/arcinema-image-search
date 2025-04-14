package com.modakdev.arcinema_image_search.api;

import com.modakdev.arcinema_image_search.service.impl.UrlFetchServiceImpl;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.FileSystemResource;
import org.springframework.core.io.Resource;
import org.springframework.http.*;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.*;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

@RestController
@RequestMapping("/youtube-api")
public class YouTubeStreamController {

    private static final String VIDEO_DIR = "videos/";

    @Autowired
    private UrlFetchServiceImpl urlService;

    @Value("${server.base.ip}")
    private String baseIp;

    @Value("${server.port}")
    private String port;

    @Value("${server.servlet.context-path}")
    private String serverContextPath;

    @PostMapping("/get-url")
    public ResponseEntity<Map<String,Object>> getUrl(@RequestParam("imageFile") MultipartFile imageFile) {
        Map<String, Object> map = urlService.uploadFiles(imageFile).getBody();
        if(map != null) {
            ResponseEntity<Map<String, Object>> responseEntity = urlService.getUrl(String.valueOf(map.get("name")), 0);
            if (responseEntity.getStatusCodeValue() == 200) {
                String youtubeUrl = responseEntity.getBody().get("url").toString();
                ResponseEntity<String> fileNameEntity = (ResponseEntity<String>) downloadVideo(youtubeUrl);
                if (fileNameEntity.getStatusCodeValue() == 200) {
                    Map<String, Object> map2 = new ConcurrentHashMap<>();
                    map2.put("movie_name", responseEntity.getBody().get("movie_name").toString());
                    map2.put("url", "http://" + baseIp + ":" + port + serverContextPath + "/youtube-api/play?fileName=" + fileNameEntity.getBody());
                    return new ResponseEntity<>(map2, HttpStatus.OK);
                } else {
                    Map<String, Object> map1 = new ConcurrentHashMap<>();
                    map1.put("error", "some error");
                    return new ResponseEntity<>(map1, HttpStatus.INTERNAL_SERVER_ERROR);
                }

            }
            else
            {
                Map<String, Object> map1 = new ConcurrentHashMap<>();
                map1.put("error", "some error");
                return new ResponseEntity<>(map1, HttpStatus.INTERNAL_SERVER_ERROR);
            }
        }
        else
        {
            Map<String, Object> map1 = new ConcurrentHashMap<>();
            map1.put("error", "some error");
            return new ResponseEntity<>(map1, HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    // Endpoint 1: Download video (or return if already downloaded)
    @GetMapping("/download")
    public ResponseEntity<?> downloadVideo(@RequestParam String url) {
        try {
            new File(VIDEO_DIR).mkdirs();

            String hashedFileName = getSha256Hash(url) + ".mp4";
            File targetFile = new File(VIDEO_DIR + hashedFileName);

            // If already downloaded, just return the filename
            if (targetFile.exists() && targetFile.length() > 100 * 1024) {
                return ResponseEntity.ok(hashedFileName);
            }

            ProcessBuilder pb = new ProcessBuilder(
                    "yt-dlp",
                    "-o", targetFile.getAbsolutePath(),
                    "-f", "mp4",
                    url
            );
            pb.redirectErrorStream(true);
            Process process = pb.start();

            BufferedReader reader = new BufferedReader(new InputStreamReader(process.getInputStream()));
            String line;
            while ((line = reader.readLine()) != null) {
                System.out.println(line);
            }

            int exitCode = process.waitFor();
            if (exitCode != 0 || !targetFile.exists() || targetFile.length() < 100 * 1024) {
                return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("Download failed.");
            }

            return ResponseEntity.ok(hashedFileName);

        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body("Exception: " + e.getMessage());
        }
    }

    // Endpoint 2: Serve video
    @GetMapping("/play")
    public ResponseEntity<Resource> playVideo(@RequestParam String fileName) {
        try {
            File file = new File(VIDEO_DIR + fileName);
            if (!file.exists()) {
                return ResponseEntity.status(HttpStatus.NOT_FOUND).build();
            }

            Resource resource = new FileSystemResource(file);
            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.parseMediaType("video/mp4"));

            return ResponseEntity.ok()
                    .headers(headers)
                    .contentLength(file.length())
                    .body(resource);

        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }

    // Helper: Generate SHA-256 hash of URL to use as filename
    private String getSha256Hash(String input) throws NoSuchAlgorithmException {
        MessageDigest digest = MessageDigest.getInstance("SHA-256");
        byte[] hash = digest.digest(input.getBytes(StandardCharsets.UTF_8));

        // Convert to hex
        StringBuilder hexString = new StringBuilder();
        for (byte b : hash) {
            hexString.append(String.format("%02x", b));
        }
        return hexString.toString();
    }
}
