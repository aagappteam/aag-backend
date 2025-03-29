package aagapp_backend.repository;

import aagapp_backend.entity.notification.Notification;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import java.util.List;

@Repository
public interface NotificationRepository extends JpaRepository<Notification, Long> {
    // Find notifications by vendor ID and filter by non-null amount
    Page<Notification> findByVendorIdAndAmountIsNotNullOrderByCreatedDateDesc(Long vendorId, Pageable pageable);

    // Find notifications by customer ID and filter by non-null amount
    Page<Notification> findByCustomerIdAndAmountIsNotNullOrderByCreatedDateDesc(Long customerId, Pageable pageable);

    // Fetch notifications for Vendor where amount is null
    Page<Notification> findByVendorIdAndAmountIsNullOrderByCreatedDateDesc(Long vendorId, Pageable pageable);

    // Fetch notifications for Customer where amount is null
    Page<Notification> findByCustomerIdAndAmountIsNullOrderByCreatedDateDesc(Long customerId, Pageable pageable);

    // Original methods for regular fetching
    Page<Notification> findByVendorIdOrderByCreatedDateDesc(Long vendorId, Pageable pageable);
    Page<Notification> findByCustomerIdOrderByCreatedDateDesc(Long customerId, Pageable pageable);
}
