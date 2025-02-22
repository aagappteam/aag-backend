package aagapp_backend.controller;

import aagapp_backend.services.GoogleVisionOCRService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/ocr")
public class OCRController {

    @Autowired
    private GoogleVisionOCRService googleVisionOCRService;

    @GetMapping("/extract-text")
    public String extractText(@RequestParam("imagePath") String imagePath) throws Exception {
        return googleVisionOCRService.extractTextFromImage(imagePath);
    }
}
