package aagapp_backend.repository;


import aagapp_backend.entity.Challenge;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;

public interface ChallangeRepository extends JpaRepository<Challenge, Long> {
    @Query("SELECT c FROM Challenge c WHERE c.challengeStatus = :status")
    List<Challenge> findByChallengeStatus(@Param("status") Challenge.ChallengeStatus status);

    // Pagination supported version of findByVendorIdIn
    @Query("SELECT c FROM Challenge c WHERE c.vendorId IN :vendorId AND c.challengeStatus = :status")
    Page<Challenge> findByVendorIdInAndStatus(@Param("vendorId") List<Long> vendorId,
                                              @Param("status") Challenge.ChallengeStatus status,
                                              Pageable pageable);

    // Pagination supported version of findByVendorIdIn
    @Query("SELECT c FROM Challenge c WHERE c.vendorId IN :vendorId")
    Page<Challenge> findByVendorIdIn(@Param("vendorId") List<Long> vendorId, Pageable pageable);


    @Query("SELECT c FROM Challenge c WHERE c.opponentVendorId IN :opponentVendorId")
    Page<Challenge> findByOpponentVendorIdIn(@Param("opponentVendorId") List<Long> opponentVendorId, Pageable pageable);

    @Query("SELECT c FROM Challenge c WHERE c.opponentVendorId IN :opponentVendorId AND c.challengeStatus = :status")
    Page<Challenge> findByOpponentVendorIdInAndStatus(@Param("opponentVendorId") List<Long> opponentVendorId,
                                                      @Param("status") Challenge.ChallengeStatus status,
                                                      Pageable pageable);


}
