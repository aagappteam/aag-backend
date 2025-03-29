package aagapp_backend.controller;

import aagapp_backend.services.unity.UnityLudoService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;


@RestController
@RequestMapping("/unity")

public class Unity {
    // TODO: Implement UnityController
    // Implement methods for managing unity entities

    @Autowired
    private UnityLudoService unityService;  // Inject UnityService for interacting with unity entities


    @GetMapping("/test")
    public Process testUnity() {
        return unityService.startUnityBackend();

    }

    @GetMapping("/state")
    public String getGameState() {
        return unityService.getGamestate();
    }

}
