package aagapp_backend.dto;

import aagapp_backend.enums.GameStatus;
import aagapp_backend.enums.GameStatus;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@AllArgsConstructor
public class GetGameResponseDashboardDTO {

    private Long id;
    private String name;

    private String gameIcon;


}
