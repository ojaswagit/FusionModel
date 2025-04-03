

package com.Final.Final.service;

import com.Final.Final.detection.YOLOv5ONNX;
import com.Final.Final.dto.FaceDetectionResponse;
import com.Final.Final.dto.FaceDetectionResult;

import ai.onnxruntime.OrtException;
import org.bytedeco.opencv.global.opencv_imgcodecs;
import org.bytedeco.opencv.opencv_core.Mat;
import org.bytedeco.opencv.opencv_core.Rect;
import org.bytedeco.opencv.global.opencv_imgproc;
import org.bytedeco.opencv.opencv_core.Point;
import org.bytedeco.opencv.opencv_core.Scalar;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

@Service
public class HeadCountService {
    private final YOLOv5ONNX detector;

    public HeadCountService() {
        try {
            this.detector = new YOLOv5ONNX("src/main/resources/yolov5l-face.onnx");
        } catch (Exception e) {
            throw new RuntimeException("Failed to load YOLO model", e);
        }
    }

    public FaceDetectionResponse detectFaces(MultipartFile file) {
        try {
            // Convert MultipartFile to OpenCV Mat
            byte[] bytes = file.getBytes();
            Mat image = opencv_imgcodecs.imdecode(new Mat(bytes), opencv_imgcodecs.IMREAD_COLOR);

            if (image.empty()) {
                System.err.println("Error: Loaded image is empty!");
                return new FaceDetectionResponse(0, new ArrayList<>());
            }

            // Run YOLO detection
            List<Rect> detectedFaces = detector.detectFaces(image);
            System.out.println("Number of detected faces: " + detectedFaces.size()); // Debugging

            // Convert detected faces to response format
            List<FaceDetectionResult> faceResults = new ArrayList<>();
            for (Rect face : detectedFaces) {
                faceResults.add(new FaceDetectionResult(face.x(), face.y(), face.width(), face.height()));

                // Draw bounding box with correct parameters
                opencv_imgproc.rectangle(image, 
                    new Point(face.x(), face.y()), 
                    new Point(face.x() + face.width(), face.y() + face.height()), 
                    new Scalar(0, 255, 0, 0), // Green color (B, G, R)
                    4 // Thickness
, 0, 0
                );
            }

            // Ensure output directory exists
            String outputDir = "src/main/resources/static/detected_faces/";
            new File(outputDir).mkdirs();

            // Save the processed image with detected faces
            String outputFilePath = outputDir + "detected_faces.jpg";
            boolean saved = opencv_imgcodecs.imwrite(outputFilePath, image);
            
            if (saved) {
                System.out.println("Processed image saved successfully at: " + outputFilePath);
            } else {
                System.err.println("Failed to save processed image.");
            }

            // Return response with face count and detected faces
            return new FaceDetectionResponse(faceResults.size(), faceResults);

        } catch (IOException e) {
            System.err.println("IOException: " + e.getMessage());
        } catch (OrtException e) {
            System.err.println("ONNX Runtime Exception: " + e.getMessage());
        } catch (Exception e) {
            e.printStackTrace();
        }

        // Return empty response in case of failure
        return new FaceDetectionResponse(0, new ArrayList<>());
    }
}
