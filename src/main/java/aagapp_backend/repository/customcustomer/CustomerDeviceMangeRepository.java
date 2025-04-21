package aagapp_backend.repository.customcustomer;

import aagapp_backend.entity.devices.UserDevice;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface CustomerDeviceMangeRepository extends JpaRepository<UserDevice, Long> {
    List<UserDevice> findByUserId(Long userId);
}
