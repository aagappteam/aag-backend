package aagapp_backend.services;
import com.google.cloud.vision.v1.ImageAnnotatorClient;
import com.google.cloud.vision.v1.TextAnnotation;
import com.google.cloud.vision.v1.EntityAnnotation;
import com.google.cloud.vision.v1.AnnotateImageRequest;
import com.google.cloud.vision.v1.Image;
import com.google.protobuf.ByteString;
import org.springframework.stereotype.Service;

import java.io.File;
import java.nio.file.Files;
import java.util.ArrayList;
import java.util.List;

@Service
public class GoogleVisionOCRService {

    public String extractTextFromImage(String imagePath) throws Exception {
        // Read image file into byte array
        ByteString imgBytes = ByteString.copyFrom(Files.readAllBytes(new File(imagePath).toPath()));
        Image img = Image.newBuilder().setContent(imgBytes).build();

        List<AnnotateImageRequest> requests = new ArrayList<>();
        AnnotateImageRequest request = AnnotateImageRequest.newBuilder()
                .addFeatures(com.google.cloud.vision.v1.Feature.newBuilder().setType(com.google.cloud.vision.v1.Feature.Type.TEXT_DETECTION).build())
                .setImage(img)
                .build();
        requests.add(request);

        // Send request to Google Vision API
        try (ImageAnnotatorClient client = ImageAnnotatorClient.create()) {
            StringBuilder extractedText = new StringBuilder();

            // Iterate through the response list and append each text annotation
            client.batchAnnotateImages(requests).getResponsesList().forEach(response -> {
                List<EntityAnnotation> textAnnotations = response.getTextAnnotationsList();

                // If there are text annotations, print all of them
                for (EntityAnnotation annotation : textAnnotations) {
                    String text = annotation.getDescription();
                    System.out.println("Extracted Text: " + text);  // Printing each extracted text
                    extractedText.append(text).append("\n");  // Append each text found to the result
                }
            });

            return extractedText.toString();  // Return all extracted text
        }
    }
}
