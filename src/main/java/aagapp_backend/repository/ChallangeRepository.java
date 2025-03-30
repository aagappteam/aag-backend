package aagapp_backend.repository;

import aagapp_backend.entity.Challenge;
import aagapp_backend.enums.ChallengeStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;

public interface ChallangeRepository extends JpaRepository<Challenge,Long> {
    @Query("SELECT c FROM Challenge c WHERE c.challengeStatus = :status")
    List<Challenge> findByChallengeStatus(@Param("status") Challenge.ChallengeStatus status);


}
