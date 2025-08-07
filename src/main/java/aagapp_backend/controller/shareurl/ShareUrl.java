package aagapp_backend.controller.shareurl;

import aagapp_backend.dto.GetGameResponseDTO;
import aagapp_backend.entity.VendorEntity;
import aagapp_backend.exception.GameNotFoundException;
import aagapp_backend.services.gameservice.GameService;
import aagapp_backend.services.vendor.VenderService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
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

    @GetMapping({
            "/{vendorId}/games/{gameId}",
            "/{vendorId}/leagues/{gameId}",
            "/{vendorId}/tournaments/{gameId}",
            "/{vendorId}/tournament/{gameId}"
    })
    public String shareGamePage(@PathVariable Long vendorId,
                                @PathVariable Long gameId,
                                Model model) {
        try {
            GetGameResponseDTO game = gameService.getGameById(gameId);
            VendorEntity vendor = vendorService.getServiceProviderById(vendorId);

            model.addAttribute("game", game);
            model.addAttribute("vendor", vendor);
            model.addAttribute("playLink", "https://aagapp.com");

            return "gameSharePage";
        } catch (Exception e) {

            return "error";
        }
    }
}
