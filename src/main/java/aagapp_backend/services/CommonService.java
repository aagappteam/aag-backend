package aagapp_backend.services;

import aagapp_backend.entity.ThemeEntity;
import aagapp_backend.entity.game.AagAvailableGames;
import aagapp_backend.services.exception.BusinessException;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;

import java.util.Objects;
import java.util.Optional;

@Service
public class CommonService {


    public <T> T findOrThrow(Optional<T> opt, String entityName, Object id) {
        return opt.orElseThrow(() -> new BusinessException(entityName + " not found with ID: " + id, HttpStatus.BAD_REQUEST));
    }


    public String resolveGameImageUrl(AagAvailableGames game, Long themeId) {
        return game.getThemes().stream()
                .filter(theme -> theme.getId().equals(themeId))
                .map(ThemeEntity::getGameimageUrl)
                .filter(Objects::nonNull)
                .findFirst()
                .orElse(game.getGameImage());
    }


}
