package aagapp_backend.dto;

import aagapp_backend.entity.ThemeEntity;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class ThemeResponseDTO {
    private Long themeId;
    private String themeImage;

    public ThemeResponseDTO(ThemeEntity theme) {
        this.themeId = theme.getId();
        this.themeImage = theme.getImageUrl();
    }
}
