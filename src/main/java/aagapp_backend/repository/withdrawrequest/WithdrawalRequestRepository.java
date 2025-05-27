package aagapp_backend.repository.withdrawrequest;

import aagapp_backend.entity.withdrawrequest.WithdrawalRequest;
import org.springframework.data.jpa.repository.JpaRepository;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import java.util.List;

public interface WithdrawalRequestRepository extends JpaRepository<WithdrawalRequest, Long> {
    List<WithdrawalRequest> findByMonthYear(String monthYear);
    List<WithdrawalRequest> findByInfluencerIdAndMonthYear(Long influencerId, String monthYear);
    Page<WithdrawalRequest> findAll(Pageable pageable);  // Method to fetch paginated results

    Page<WithdrawalRequest> findByInfluencerId(Long influencerId, Pageable pageable);
    Page<WithdrawalRequest> findByInfluencerIdAndMonthYear(Long influencerId, String monthYear, Pageable pageable);

    Page<WithdrawalRequest> findByStatus(String status, Pageable pageable);

    Page<WithdrawalRequest> findByInfluencerIdAndStatus(Long influencerId, String upperCase, Pageable pageable);
}

