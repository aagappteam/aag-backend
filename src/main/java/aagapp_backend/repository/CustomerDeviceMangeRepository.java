package aagapp_backend.repository;

import aagapp_backend.entity.devices.UserDevice;
import aagapp_backend.entity.devices.VendorDevice;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface CustomerDeviceMangeRepository extends JpaRepository<UserDevice, Long> {
    List<UserDevice> findByUserId(Long userId);
}
