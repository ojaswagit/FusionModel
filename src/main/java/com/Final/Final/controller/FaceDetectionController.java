package com.Final.Final.controller;

import com.Final.Final.service.HeadCountService;
import com.Final.Final.dto.FaceDetectionResponse;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

@RestController
@RequestMapping("/api")
public class FaceDetectionController {
    private final HeadCountService headCountService;

    public FaceDetectionController(HeadCountService headCountService) {
        this.headCountService = headCountService;
    }

    @PostMapping("/detect-faces")
    public ResponseEntity<FaceDetectionResponse> detectFaces(@RequestParam("file") MultipartFile file) {
        FaceDetectionResponse response = headCountService.detectFaces(file);
        return ResponseEntity.ok(response);
    }
}
