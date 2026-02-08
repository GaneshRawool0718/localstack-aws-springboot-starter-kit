package com.example.localstackpoc.controller;

import com.example.localstackpoc.service.S3Service;
import org.springframework.http.HttpHeaders;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.List;
import java.util.Map;

/**
 * REST controller for S3 file operations.
 * Provides endpoints for uploading, downloading, and listing files in the S3 bucket.
 */
@RestController
@RequestMapping("/api/s3")
public class S3Controller {

    private final S3Service s3Service;

    public S3Controller(S3Service s3Service) {
        this.s3Service = s3Service;
    }

    /**
     * Upload a file to S3.
     * Example: curl -X POST http://localhost:8080/api/s3/upload -F "file=@myfile.txt"
     */
    @PostMapping("/upload")
    public ResponseEntity<Map<String, String>> uploadFile(
            @RequestParam("file") MultipartFile file) throws IOException {
        String key = file.getOriginalFilename();
        s3Service.uploadFile(key, file.getBytes(), file.getContentType());
        return ResponseEntity.ok(Map.of(
                "key", key,
                "status", "uploaded"
        ));
    }

    /**
     * Download a file from S3 by its key.
     * Example: curl http://localhost:8080/api/s3/download/myfile.txt -o myfile.txt
     */
    @GetMapping("/download/{key}")
    public ResponseEntity<byte[]> downloadFile(@PathVariable String key) {
        byte[] data = s3Service.downloadFile(key);
        return ResponseEntity.ok()
                .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=\"" + key + "\"")
                .body(data);
    }

    /**
     * List all files in the S3 bucket.
     * Example: curl http://localhost:8080/api/s3/files
     */
    @GetMapping("/files")
    public ResponseEntity<List<String>> listFiles() {
        return ResponseEntity.ok(s3Service.listFiles());
    }
}
