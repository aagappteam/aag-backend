package aagapp_backend.repository.admin;

import aagapp_backend.entity.admin.AdminLogs;
import aagapp_backend.entity.invoice.InvoiceAdmin;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;

public interface AdminLogsInterface  extends JpaRepository<InvoiceAdmin, Long>, JpaSpecificationExecutor<AdminLogs>  {


}
