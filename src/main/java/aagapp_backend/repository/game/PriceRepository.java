package aagapp_backend.repository.game;
import aagapp_backend.entity.VendorEntity;
import aagapp_backend.entity.game.AagAvailableGames;
import aagapp_backend.entity.game.Game;
import aagapp_backend.entity.game.PriceEntity;
import aagapp_backend.enums.GameStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;


import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.util.List;
import java.util.Optional;

@Repository
public interface PriceRepository extends JpaRepository<PriceEntity, Long> {
    Page<PriceEntity> findAll(Pageable pageable);

}


