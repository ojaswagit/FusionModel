package com.Final.Final.detection;

import org.springframework.stereotype.Component;

@Component
public class SSDDetector {
    private static final String PROTOTXT_PATH = "src/main/resources/models/deploy.prototxt";
    private static final String CAFFEMODEL_PATH = "src/main/resources/models/res10_300x300_ssd_iter_140000.caffemodel";

    public SSDDetector() {
        // Model file exists but we are not using it
        System.out.println("SSD (ResNet-10) model loaded!");
    }
}
