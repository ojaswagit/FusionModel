package com.Final.Final.dto;

import java.util.List;

public class FaceDetectionResponse {
    private int faceCount;
    private List<FaceDetectionResult> faces;

    // Constructor accepting List<FaceDetectionResult>
    public FaceDetectionResponse(int faceCount, List<FaceDetectionResult> faces) {
        this.faceCount = faceCount;
        this.faces = faces;
    }

    public int getFaceCount() {
        return faceCount;
    }

    public List<FaceDetectionResult> getFaces() {
        return faces;
    }

    public void setFaceCount(int faceCount) {
        this.faceCount = faceCount;
    }

    public void setFaces(List<FaceDetectionResult> faces) {
        this.faces = faces;
    }
}
