package aagapp_backend.services;

import aagapp_backend.dto.*;
import aagapp_backend.entity.game.AagAvailableGames;
import aagapp_backend.entity.ThemeEntity;
import aagapp_backend.entity.game.PriceEntity;
import aagapp_backend.exception.GameNotFoundException;
import aagapp_backend.repository.game.AagGameRepository;
import aagapp_backend.repository.game.PriceRepository;
import aagapp_backend.repository.game.ThemeRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
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

    @Autowired
    private PriceRepository priceRepository;

    // Create a new game
  /*  public GameResponseDTO createGame(GameRequestDTO gameRequestDTO) {
        // Create the game entity and set the game details
        AagAvailableGames newGame = new AagAvailableGames();
        newGame.setGameName(gameRequestDTO.getGameName());
        newGame.setGameImage(gameRequestDTO.getGameImage());
        newGame.setGameStatus(gameRequestDTO.getGameStatus());
        newGame.setMinRange(gameRequestDTO.getMinRange());
        newGame.setMaxRange(gameRequestDTO.getMaxRange());
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

        List<PriceEntity> prices = new ArrayList<>();
        for (PriceRequestDTO priceRequestDTO : gameRequestDTO.getPrices()) {
            PriceEntity newPrice = new PriceEntity();
            newPrice.setPriceValue(priceRequestDTO.getPriceValue());
            PriceEntity savedPrice = priceRepository.save(newPrice);
            prices.add(savedPrice);

            // Associate the price with the game (many-to-many relationship)
            savedPrice.getGames().add(newGame);
            priceRepository.save(savedPrice);
        }
        newGame.setPrice(prices);

        // Save the game entity to the database
        AagAvailableGames savedGame = gameRepository.save(newGame);

        // Return the saved game as a DTO response
        return new GameResponseDTO(savedGame);
    }*/
    public GameResponseDTO createGame(GameRequestDTO gameRequestDTO) {
        // Create the game entity and set the game details
        AagAvailableGames newGame = new AagAvailableGames();
        newGame.setGameName(gameRequestDTO.getGameName());
        newGame.setGameImage(gameRequestDTO.getGameImage());
        newGame.setGameStatus(gameRequestDTO.getGameStatus());
        newGame.setMinRange(gameRequestDTO.getMinRange());
        newGame.setMaxRange(gameRequestDTO.getMaxRange());

        // Handle themes
        List<ThemeEntity> themes = new ArrayList<>();
        for (ThemeRequestDTO themeRequestDTO : gameRequestDTO.getThemes()) {
            ThemeEntity newTheme = new ThemeEntity();
            newTheme.setName(themeRequestDTO.getName());
            newTheme.setImageUrl(themeRequestDTO.getImageUrl());
            themes.add(newTheme);  // Add theme to list instead of saving right away
        }

        // Batch save themes after constructing the entire list
        themeRepository.saveAll(themes);

        // Set the themes to the game entity
        newGame.setThemes(themes);

        // Handle prices
        List<PriceEntity> prices = new ArrayList<>();
        for (PriceRequestDTO priceRequestDTO : gameRequestDTO.getPrices()) {
            PriceEntity newPrice = new PriceEntity();
            newPrice.setPriceValue(priceRequestDTO.getPriceValue());

            // Associate the PriceEntity with the game (many-to-many relationship)
            newPrice.getGames().add(newGame);  // Add the game to the price entity
            prices.add(newPrice); // Add the price to the list
        }

        // Batch save prices after constructing the entire list
        priceRepository.saveAll(prices);  // Save all prices at once

        // Set the prices to the game entity (many-to-many relationship)
        newGame.setPrice(prices);

        // Save the game entity along with its related entities (themes and prices)
        AagAvailableGames savedGame = gameRepository.save(newGame);

        // Return the saved game as a DTO response
        return new GameResponseDTO(savedGame);
    }



    // Get all games with pagination
    public List<GameResponseDTO> getAllGames(int page, int size) {
//        List<AagAvailableGames> games = gameRepository.findAll(); // Pagination could be added here
        List<AagAvailableGames>  games =  gameRepository.findAllByOrderByIdAsc();

        return games.stream().map(GameResponseDTO::new).collect(Collectors.toList());
    }

    public List<GetGameResponseDashboardDTO> getAllGamesDashboard(int page, int size) {
        Pageable pageable = PageRequest.of(page, size);
        Page<AagAvailableGames> gamesPage = gameRepository.findAll(pageable);

        List<GetGameResponseDashboardDTO> gameResponseDTOs = gamesPage.stream()
                .map(game -> new GetGameResponseDashboardDTO(
                        game.getId(),
                        game.getGameName(),
                        game.getGameImage()

                ))
                .collect(Collectors.toList());

        return gameResponseDTOs;


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

