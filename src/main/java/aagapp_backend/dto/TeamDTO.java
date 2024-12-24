package aagapp_backend.dto;


import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;

@AllArgsConstructor
@NoArgsConstructor
@Setter
@Getter
@Component
public class TeamDTO {
    private Long id;
    private String name;
    private Long leagueId;
    private String leagueName;
    private LocalDateTime createdate;

}

