package aagapp_backend.dto;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class ThemeRequestDTO {
    private Long id;  // Theme ID
    private String imageUrl;  // Theme image URL
}