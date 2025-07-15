package aagapp_backend.repository.ticket;

import aagapp_backend.entity.ticket.TicketMessage;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface TicketMessageRepository extends JpaRepository<TicketMessage, Long> {
    // Get all messages for a specific ticket, ordered by creation time
    List<TicketMessage> findByTicketIdOrderByCreatedAtAsc(Long ticketId);
}
