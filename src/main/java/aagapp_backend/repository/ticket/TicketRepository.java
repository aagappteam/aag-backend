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

    // Get tickets by customer ID
    List<Ticket> findByCustomer(CustomCustomer customer);

    // Get tickets by vendor ID
    List<Ticket> findByVendor(VendorEntity vendor);

    List<Ticket> findByStatus(String status);
}
