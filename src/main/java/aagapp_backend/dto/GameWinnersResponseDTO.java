package aagapp_backend.dto;

import lombok.Getter;
import lombok.Setter;

import java.util.List;

@Getter
@Setter
public class GameWinnersResponseDTO {
    private String gameName;
    private String gameLogoUrl;
    private List<RoomWinnersDTO> allRoomWinners;

    // Pagination details
    private int currentPage;
    private int pageSize;
    private int totalPages;
    private long totalElements;

    // Constructor, Getters, Setters

    public GameWinnersResponseDTO(String gameName, String gameLogoUrl, List<RoomWinnersDTO> allRoomWinners, int currentPage, int pageSize, int totalPages, long totalElements) {
        this.gameName = gameName;
        this.gameLogoUrl = gameLogoUrl;
        this.allRoomWinners = allRoomWinners;
        this.currentPage = currentPage;
        this.pageSize = pageSize;
        this.totalPages = totalPages;
        this.totalElements = totalElements;
    }
}

