package aagapp_backend.dto;

import aagapp_backend.entity.ThemeEntity;
import aagapp_backend.entity.game.PriceEntity;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class PriceResponseDTO {
/*
    private Long themeId;
*/
    private Double price;

    public PriceResponseDTO(PriceEntity price) {
/*
        this.themeId = theme.getId();
*/
        this.price = price.getPriceValue();
    }
}
