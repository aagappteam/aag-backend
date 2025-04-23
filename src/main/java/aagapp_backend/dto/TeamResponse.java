package aagapp_backend.dto;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDateTime;


@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
public class TeamResponse {

    private Long id;
    private String teamName;
    private String profilePic;
    private Integer totalScore;
    private Boolean isWinner;
    private LocalDateTime createdAt;
}
