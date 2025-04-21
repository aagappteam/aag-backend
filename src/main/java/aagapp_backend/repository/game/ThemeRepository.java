package aagapp_backend.repository.game;

import aagapp_backend.entity.ThemeEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;

public interface ThemeRepository extends JpaRepository<ThemeEntity, Long> {
    @Query("SELECT t FROM ThemeEntity t WHERE t.id IN :themeIds")
    List<ThemeEntity> findAllById(@Param("themeIds") List<Long> themeIds);
}
