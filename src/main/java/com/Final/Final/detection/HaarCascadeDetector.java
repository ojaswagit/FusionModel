package com.Final.Final.detection;

import org.springframework.stereotype.Component;

@Component
public class HaarCascadeDetector {
    private static final String MODEL_PATH = "src/main/resources/models/haarcascade_frontalface_default.xml";

    public HaarCascadeDetector() {
        // Model file exists but we are not using it
        System.out.println("HaarCascade model!");
    }
}
