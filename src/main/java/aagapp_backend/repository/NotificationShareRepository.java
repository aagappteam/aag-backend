package aagapp_backend.repository;

import aagapp_backend.entity.notification.Notification;
import aagapp_backend.entity.notification.NotificationShare;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;

public interface NotificationShareRepository  extends JpaRepository<NotificationShare, Long> , JpaSpecificationExecutor<NotificationShare> {
}
