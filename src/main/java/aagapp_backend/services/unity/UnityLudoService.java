package aagapp_backend.services.unity;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;

@Service
public class UnityLudoService {
    private static final String UNITY_EXECUTABLE = "E:\\Projects\\ProdBuild\\AAGLudoServer.exe";
    private final RestTemplate restTemplate = new RestTemplate();

    public static Process startUnityBackend() {
        Process process = null;
        try {
            ProcessBuilder processBuilder = new ProcessBuilder(UNITY_EXECUTABLE);
            process = processBuilder.start();

            // Read Unity Logs
            Process finalProcess = process;
            new Thread(() -> {
                try (BufferedReader reader = new BufferedReader(new InputStreamReader(finalProcess.getInputStream()))) {
                    String line;
                    while ((line = reader.readLine()) != null) {
                        System.out.println("[Unity]: " + line);
                    }
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }).start();

        } catch (IOException e) {
            e.printStackTrace();
        }
        return process;
    }


    public String getGamestate() {
        String url = "http://127.0.0.1:8081/gamestate"; // Replace with actual Unity API URL
        return restTemplate.getForObject(url, String.class);
    }

}
