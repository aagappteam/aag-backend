package aagapp_backend.repository.ticket;

import aagapp_backend.entity.CustomCustomer;
import aagapp_backend.entity.VendorEntity;
import aagapp_backend.entity.ticket.Ticket;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface TicketRepository extends JpaRepository<Ticket, Long> {
    List<Ticket> findAll();

//    List<Ticket> findByVendorId(Long vendorId);

    // Query to find tickets by customerId
//    List<Ticket> findByCustomerId(Long customerId);

    @Query("SELECT t FROM Ticket t WHERE t.vendorId = :id ORDER BY t.createdDate DESC")
    List<Ticket> findByVendorId(@Param("id") Long vendorId);



    @Query("SELECT t FROM Ticket t WHERE t.customerId = :id ORDER BY t.createdDate DESC")
    List<Ticket> findByCustomerId(@Param("id") Long customerId);

    Page<Ticket> findByStatus(String status, Pageable pageable);

    Page<Ticket> findByRole(String role, Pageable pageable);

    Page<Ticket> findByStatusAndRole(String status, String role, Pageable pageable);
}
