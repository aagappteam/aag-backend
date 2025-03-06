package aagapp_backend.services;

import aagapp_backend.dto.ThemeRequestDTO;
import aagapp_backend.entity.game.AagAvailableGames;
import aagapp_backend.entity.ThemeEntity;
import aagapp_backend.dto.GameRequestDTO;
import aagapp_backend.dto.GameResponseDTO;
import aagapp_backend.exception.GameNotFoundException;
import aagapp_backend.repository.game.AagGameRepository;
import aagapp_backend.repository.game.ThemeRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
public class AagGameService {

    @Autowired
    private AagGameRepository gameRepository;

    @Autowired
    private ThemeRepository themeRepository;

    // Create a new game
    public GameResponseDTO createGame(GameRequestDTO gameRequestDTO) {
        // Create the game entity and set the game details
        AagAvailableGames newGame = new AagAvailableGames();
        newGame.setGameName(gameRequestDTO.getGameName());
        newGame.setGameImage(gameRequestDTO.getGameImage());
        newGame.setGameStatus(gameRequestDTO.getGameStatus());

        // Handle themes
        List<ThemeEntity> themes = new ArrayList<>();

        for (int i = 0; i < gameRequestDTO.getThemes().size(); i++) {
            // Get the theme ID and image URL from the request
            ThemeRequestDTO themeRequestDTO = gameRequestDTO.getThemes().get(i);
            String themeImageUrl = themeRequestDTO.getImageUrl();
            String themename = themeRequestDTO.getName();


                // If theme does not exist, create a new one with the provided image URL
                ThemeEntity newTheme = new ThemeEntity();
                newTheme.setName(themename);
                newTheme.setImageUrl(themeImageUrl);  // Set the image URL from the request
                // Save the new theme
                ThemeEntity savedTheme = themeRepository.save(newTheme);
                themes.add(savedTheme);  // Add the new theme to the list

        }

        // Set the themes to the game entity
        newGame.setThemes(themes);

        // Save the game entity to the database
        AagAvailableGames savedGame = gameRepository.save(newGame);

        // Return the saved game as a DTO response
        return new GameResponseDTO(savedGame);
    }


    // Get all games with pagination
    public List<GameResponseDTO> getAllGames(int page, int size) {
        List<AagAvailableGames> games = gameRepository.findAll(); // Pagination could be added here
        return games.stream().map(GameResponseDTO::new).collect(Collectors.toList());
    }

    // Get game by ID
    public GameResponseDTO getGameById(Long gameId) throws GameNotFoundException {
        AagAvailableGames game = gameRepository.findById(gameId)
                .orElseThrow(() -> new GameNotFoundException("Game not found with ID: " + gameId));
        return new GameResponseDTO(game);
    }

    // Update a game
    public GameResponseDTO updateGame(Long gameId, GameRequestDTO gameRequestDTO) throws GameNotFoundException {
        // Find the game by its ID or throw an exception if not found
        AagAvailableGames game = gameRepository.findById(gameId)
                .orElseThrow(() -> new GameNotFoundException("Game not found with ID: " + gameId));

        // Update basic game fields
        game.setGameName(gameRequestDTO.getGameName());
        game.setGameImage(gameRequestDTO.getGameImage());
        game.setGameStatus(gameRequestDTO.getGameStatus());

        // Handle themes update
        List<ThemeEntity> themes = new ArrayList<>();

        for (ThemeRequestDTO themeRequestDTO : gameRequestDTO.getThemes()) {
            String themeImageUrl = themeRequestDTO.getImageUrl();

            // Check if the theme exists by ID

                // If the theme does not exist, create a new theme with the provided image URL
                ThemeEntity newTheme = new ThemeEntity();
                newTheme.setName(themeRequestDTO.getName());  // Set the name from the request
                newTheme.setImageUrl(themeImageUrl);
                // Save the new theme
                ThemeEntity savedTheme = themeRepository.save(newTheme);
                themes.add(savedTheme);

        }

        // Set the updated themes to the game entity
        game.setThemes(themes);

        // Save the updated game to the database
        AagAvailableGames updatedGame = gameRepository.save(game);

        // Return the updated game as a DTO response
        return new GameResponseDTO(updatedGame);
    }


    // Delete a game
    public void deleteGame(Long gameId) throws GameNotFoundException {
        AagAvailableGames game = gameRepository.findById(gameId)
                .orElseThrow(() -> new GameNotFoundException("Game not found with ID: " + gameId));

        gameRepository.delete(game);
    }
}

