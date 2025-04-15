package aagapp_backend.repository.ticket;

import aagapp_backend.entity.CustomCustomer;
import aagapp_backend.entity.VendorEntity;
import aagapp_backend.entity.ticket.Ticket;
import aagapp_backend.enums.TicketEnum;
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

    @Query("SELECT t FROM Ticket t WHERE t.customerOrVendorId = :id AND t.role = :role")
    List<Ticket> findByCustomerOrVendorIdAndRole(@Param("id") Long id, @Param("role") String role);

//    @Query("SELECT t FROM Ticket t WHERE t.customerOrVendorId = :id AND LOWER(t.role) = LOWER(:role) AND (:status IS NULL OR t.status = :status)")
//    List<Ticket> findTicketsByRoleAndIdAndStatus(@Param("id") Long id, @Param("role") String role, @Param("status") String status);

    @Query("SELECT t FROM Ticket t WHERE LOWER(t.role) = LOWER(:role) AND t.customerOrVendorId = :id AND (:status IS NULL OR t.status = :status)")
    List<Ticket> findByRoleAndCustomerOrVendorIdAndStatus(
            @Param("role") String role,
            @Param("id") Long id,
            @Param("status") TicketEnum status
    );

    Page<Ticket> findByStatus(TicketEnum status, Pageable pageable);

    Page<Ticket> findByRole(String role, Pageable pageable);

    Page<Ticket> findByStatusAndRole(TicketEnum status, String role, Pageable pageable);




}
