package aagapp_backend.repository;

import aagapp_backend.entity.notification.Notification;
import aagapp_backend.entity.notification.UserNotification;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.stereotype.Repository;

@Repository
public interface UserNotificationRepository extends JpaRepository<UserNotification, Long>, JpaSpecificationExecutor<UserNotification> {
    // Find notifications by vendor ID and filter by non-null amount
    Page<UserNotification> findByVendorIdAndAmountIsNotNullOrderByCreatedDateDesc(Long vendorId, Pageable pageable);

    // Find notifications by customer ID and filter by non-null amount
    Page<UserNotification> findByCustomerIdAndAmountIsNotNullOrderByCreatedDateDesc(Long customerId, Pageable pageable);

    // Fetch notifications for Vendor where amount is null
    Page<UserNotification> findByVendorIdAndAmountIsNullOrderByCreatedDateDesc(Long vendorId, Pageable pageable);

    // Fetch notifications for Customer where amount is null
    Page<UserNotification> findByCustomerIdAndAmountIsNullOrderByCreatedDateDesc(Long customerId, Pageable pageable);

    // Original methods for regular fetching
    Page<UserNotification> findByVendorIdOrderByCreatedDateDesc(Long vendorId, Pageable pageable);
    Page<UserNotification> findByCustomerIdOrderByCreatedDateDesc(Long customerId, Pageable pageable);


}
