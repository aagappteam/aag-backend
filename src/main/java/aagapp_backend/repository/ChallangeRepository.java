package aagapp_backend.repository;

import aagapp_backend.entity.Challenge;
import aagapp_backend.enums.ChallengeStatus;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface ChallangeRepository extends JpaRepository<Challenge,Long> {
    List<Challenge> findByChallengeStatus(ChallengeStatus challengeStatus);
}
