package aagapp_backend.repository.admin;

import aagapp_backend.entity.invoice.InvoiceAdmin;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

@Repository
public interface InvoiceAdminRepository extends JpaRepository<InvoiceAdmin, Long> {
    @Query("SELECT i.invoiceNo FROM InvoiceAdmin i WHERE i.invoiceNo LIKE :prefix% ORDER BY i.invoiceNo DESC LIMIT 1")
    String findLastInvoiceNo(@Param("prefix") String prefix);

}