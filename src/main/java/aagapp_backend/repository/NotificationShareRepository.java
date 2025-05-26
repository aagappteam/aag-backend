package aagapp_backend.repository;

import aagapp_backend.entity.notification.Notification;
import aagapp_backend.entity.notification.NotificationShare;
import org.springframework.data.jpa.repository.JpaRepository;

public interface NotificationShareRepository  extends JpaRepository<NotificationShare, Long> {
}
