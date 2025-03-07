package aagapp_backend.dto;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class ThemeRequestDTO {
    private String name;  // Theme name
    private String imageUrl;  // Theme image URL
}