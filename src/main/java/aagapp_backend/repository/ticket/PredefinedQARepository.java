package aagapp_backend.repository.ticket;

import aagapp_backend.entity.ticket.PredefinedQA;
import aagapp_backend.enums.TicketUserType;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;


public interface PredefinedQARepository extends JpaRepository<PredefinedQA, Long> {

    Page<PredefinedQA> findByUserType(TicketUserType userType, Pageable pageable);

    @Query("SELECT qa FROM PredefinedQA qa WHERE qa.userType = :userType AND " +
            "(LOWER(qa.question) LIKE LOWER(CONCAT('%', :keyword, '%')) OR " +
            "LOWER(qa.answer) LIKE LOWER(CONCAT('%', :keyword, '%')))")
    Page<PredefinedQA> findByUserTypeAndKeyword(@Param("userType") TicketUserType userType,
                                                @Param("keyword") String keyword,
                                                Pageable pageable);
}

