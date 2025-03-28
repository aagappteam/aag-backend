package aagapp_backend.repository;

import aagapp_backend.entity.cache.TopVendorCache;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface TopVendorCacheRepository extends JpaRepository<TopVendorCache, Long> {
    List<TopVendorCache> findTop3ByOrderByRankAsc();
}
