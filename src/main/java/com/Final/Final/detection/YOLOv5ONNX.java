package com.Final.Final.detection;

import ai.onnxruntime.*;
//import org.bytedeco.opencv.global.opencv_core;
import org.bytedeco.opencv.global.opencv_imgproc;
import org.bytedeco.opencv.opencv_core.*;

import java.nio.FloatBuffer;
import java.util.*;

public class YOLOv5ONNX {
    private final OrtEnvironment env;
    private final OrtSession session;
    private final String inputName;

    public YOLOv5ONNX(String modelPath) throws OrtException {
        env = OrtEnvironment.getEnvironment();
        session = env.createSession(modelPath, new OrtSession.SessionOptions());

        // Auto-fetch the input name from ONNX model
        this.inputName = session.getInputNames().iterator().next();
        System.out.println("✅ ONNX Model Loaded - Input Name: " + inputName);
    }

    public List<Rect> detectFaces(Mat image) throws OrtException {
        List<Rect> detectedFaces = new ArrayList<>();
        int width = 640, height = 640;

        // Resize image to 640x640
        Mat resized = new Mat();
        opencv_imgproc.resize(image, resized, new Size(width, height));

        // Convert image to float & normalize (HWC → CHW)
        float[] imgData = preprocessImage(resized, width, height);

        // Create ONNX Tensor
        try (OnnxTensor inputTensor = OnnxTensor.createTensor(env, FloatBuffer.wrap(imgData),
                new long[]{1, 3, height, width})) {

            // Run inference
            OrtSession.Result output = session.run(Collections.singletonMap(inputName, inputTensor));

            // Process detections
            processOutput(output, detectedFaces);
        }

        return detectedFaces;
    }

    private float[] preprocessImage(Mat image, int width, int height) {
        float[] imgData = new float[3 * width * height];
        int index = 0;

        for (int c = 0; c < 3; c++) {  // BGR to CHW format
            for (int y = 0; y < height; y++) {
                for (int x = 0; x < width; x++) {
                    imgData[index++] = (float) (image.ptr(y, x).get(c) & 0xFF) / 255.0f; // Normalize
                }
            }
        }
        return imgData;
    }

    private void processOutput(OrtSession.Result output, List<Rect> detectedFaces) throws OrtException {
        OnnxValue result = output.get(0);

        if (result instanceof OnnxTensor) {
            OnnxTensor tensor = (OnnxTensor) result;
            float[][][] outputArray = (float[][][]) tensor.getValue(); // 🔥 FIX: Handle 3D output correctly
            extractBoundingBoxes(outputArray, detectedFaces);
        }
    }

    private void extractBoundingBoxes(float[][][] outputArray, List<Rect> detectedFaces) {
        float CONFIDENCE_THRESHOLD = 0.5f;
        float IOU_THRESHOLD = 0.4f;
        List<Rect> boxes = new ArrayList<>();
        List<Float> scores = new ArrayList<>();

        for (float[][] batch : outputArray) { // Iterate over batch
            for (float[] detection : batch) { // Iterate over detected faces
                if (detection.length < 6) continue;

                float x = detection[0] * 640;
                float y = detection[1] * 640;
                float w = detection[2] * 640;
                float h = detection[3] * 640;
                float confidence = detection[4];

                if (confidence > CONFIDENCE_THRESHOLD) {
                    boxes.add(new Rect((int) x, (int) y, (int) w, (int) h));
                    scores.add(confidence);
                }
            }
        }

        // Apply Non-Maximum Suppression (NMS)
        List<Rect> finalDetections = nonMaxSuppression(boxes, scores, IOU_THRESHOLD);
        detectedFaces.addAll(finalDetections);
    }

    private List<Rect> nonMaxSuppression(List<Rect> boxes, List<Float> scores, float threshold) {
        List<Rect> result = new ArrayList<>();
        boolean[] removed = new boolean[boxes.size()];

        for (int i = 0; i < boxes.size(); i++) {
            if (removed[i]) continue;
            Rect boxA = boxes.get(i);
            result.add(boxA);

            for (int j = i + 1; j < boxes.size(); j++) {
                if (removed[j]) continue;
                Rect boxB = boxes.get(j);

                if (iou(boxA, boxB) > threshold) {
                    removed[j] = true;
                }
            }
        }
        return result;
    }

    private float iou(Rect boxA, Rect boxB) {
        int xA = Math.max(boxA.x(), boxB.x());
        int yA = Math.max(boxA.y(), boxB.y());
        int xB = Math.min(boxA.x() + boxA.width(), boxB.x() + boxB.width());
        int yB = Math.min(boxA.y() + boxA.height(), boxB.y() + boxB.height());

        int interArea = Math.max(0, xB - xA) * Math.max(0, yB - yA);
        int boxAArea = boxA.width() * boxA.height();
        int boxBArea = boxB.width() * boxB.height();

        return (float) interArea / (boxAArea + boxBArea - interArea);
    }
}
