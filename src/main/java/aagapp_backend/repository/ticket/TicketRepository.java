package aagapp_backend.repository.ticket;

import aagapp_backend.entity.CustomCustomer;
import aagapp_backend.entity.VendorEntity;
import aagapp_backend.entity.ticket.Ticket;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface TicketRepository extends JpaRepository<Ticket, Long> {
    List<Ticket> findAll();

    List<Ticket> findByVendorId(Long vendorId);

    // Query to find tickets by customerId
    List<Ticket> findByCustomerId(Long customerId);

    List<Ticket> findByStatus(String status);
}
