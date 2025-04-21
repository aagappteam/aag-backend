package aagapp_backend.dto.leaderboard;

import lombok.AllArgsConstructor;
import lombok.Data;

import java.util.List;

@Data
@AllArgsConstructor
public class PageDTO<T> {
    private List<T> data;
    private int currentPage;
    private int totalPages;
    private long totalElements;
}
