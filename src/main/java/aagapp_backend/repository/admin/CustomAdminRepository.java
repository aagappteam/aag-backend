package aagapp_backend.repository.admin;

import aagapp_backend.entity.CustomAdmin;
import aagapp_backend.entity.invoice.InvoiceAdmin;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface CustomAdminRepository extends JpaRepository<CustomAdmin, Long> {
    Optional<CustomAdmin> findByMobileNumber(String mobile);
}
