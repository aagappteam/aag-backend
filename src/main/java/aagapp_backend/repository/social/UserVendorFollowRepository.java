package aagapp_backend.repository.social;

import aagapp_backend.dto.TopVendorDto;
import aagapp_backend.entity.social.UserVendorFollow;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;

public interface UserVendorFollowRepository extends JpaRepository<UserVendorFollow, Long> {

    @Query(value = "SELECT * FROM user_vendor_follow WHERE vendor_id = :vendorId", nativeQuery = true)
    List<UserVendorFollow> findByVendorId(Long vendorId);

    @Query(value = "SELECT * FROM user_vendor_follow WHERE user_id = :userId", nativeQuery = true)
    List<UserVendorFollow> findByUserId(Long userId);

    @Query(value = "SELECT * FROM user_vendor_follow WHERE user_id = :userId AND vendor_id = :vendorId LIMIT 1", nativeQuery = true)
    Optional<UserVendorFollow> findByUserIdAndVendorId(Long userId, Long vendorId);

    @Query(value = "SELECT COUNT(*) FROM user_vendor_follow WHERE vendor_id = :vendorId", nativeQuery = true)
    Long countByVendorId(Long vendorId);

    @Query(value = "SELECT CASE WHEN COUNT(*) > 0 THEN true ELSE false END FROM user_vendor_follow WHERE user_id = :userId AND vendor_id = :vendorId", nativeQuery = true)
    boolean existsByUserIdAndVendorId(Long userId, Long vendorId);

    @Query(value = "SELECT new aagapp_backend.dto.TopVendorDto(v.service_provider_id, v.first_name, COUNT(f.id)) " +
            "FROM VendorEntity v LEFT JOIN UserVendorFollow f ON f.vendor.service_provider_id = v.service_provider_id " +
            "GROUP BY v.service_provider_id, v.first_name ORDER BY COUNT(f.id) DESC")
    List<TopVendorDto> findTopVendorsWithFollowerCount();

    @Query("SELECT COUNT(f.id) " +
            "FROM UserVendorFollow f " +
            "WHERE f.vendor.service_provider_id = :vendorId")
    Long countFollowersByVendorId(@Param("vendorId") Long vendorId);

}


