package aagapp_backend.controller;


import org.springframework.core.io.ClassPathResource;
import org.springframework.core.io.Resource;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;

import java.io.IOException;

@Controller
public class AssetLinksController {

    @GetMapping("/.well-known/assetlinks.json")
    public ResponseEntity<Resource> serveAssetLinks() throws IOException {
        Resource resource = new ClassPathResource("public/.well-known/assetlinks.json");
        if (!resource.exists()) {
            return ResponseEntity.notFound().build();
        }
        return ResponseEntity.ok()
                .contentType(MediaType.APPLICATION_JSON)
                .body(resource);
    }
}
