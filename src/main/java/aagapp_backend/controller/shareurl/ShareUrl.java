package aagapp_backend.controller.shareurl;

import aagapp_backend.dto.GetGameResponseDTO;
import aagapp_backend.entity.VendorEntity;
import aagapp_backend.entity.game.Game;
import aagapp_backend.exception.GameNotFoundException;
import aagapp_backend.services.gameservice.GameService;
import aagapp_backend.services.vendor.VenderService;
import org.springframework.ui.Model;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;

@Controller
@RequestMapping("/vendor")
public class ShareUrl {


    private final GameService gameService;
    private final VenderService vendorService;

    @Autowired
    public ShareUrl(GameService gameService, VenderService vendorService) {
        this.gameService = gameService;
        this.vendorService = vendorService;
    }

    @GetMapping("/{vendorId}/games/{gameId}")
    public String shareGamePage(@PathVariable Long vendorId,
                                @PathVariable Long gameId,
                                Model model) throws GameNotFoundException {
        GetGameResponseDTO game = gameService.getGameById(gameId);
        VendorEntity vendor = vendorService.getServiceProviderById(vendorId);

        model.addAttribute("game", game);
        model.addAttribute("vendor", vendor);

        return "gameSharePage";
    }


    @GetMapping("/{vendorId}/leagues/{gameId}")
    public String shareGamePageLeague(@PathVariable Long vendorId,
                                @PathVariable Long gameId,
                                Model model) throws GameNotFoundException {
        GetGameResponseDTO game = gameService.getGameById(gameId);
        VendorEntity vendor = vendorService.getServiceProviderById(vendorId);

        model.addAttribute("game", game);
        model.addAttribute("vendor", vendor);

        return "gameSharePage";
    }

    @GetMapping("/{vendorId}/tournament/{gameId}")
    public String shareGamePageLeagueTournment(@PathVariable Long vendorId,
                                      @PathVariable Long gameId,
                                      Model model) throws GameNotFoundException {
        GetGameResponseDTO game = gameService.getGameById(gameId);
        VendorEntity vendor = vendorService.getServiceProviderById(vendorId);

        model.addAttribute("game", game);
        model.addAttribute("vendor", vendor);

        return "gameSharePage";
    }



}
