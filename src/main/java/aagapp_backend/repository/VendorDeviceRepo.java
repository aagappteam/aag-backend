package aagapp_backend.repository;

import aagapp_backend.entity.VendorEntity;
import aagapp_backend.entity.devices.VendorDevice;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface VendorDeviceRepo extends JpaRepository<VendorDevice, Long> {
    List<VendorDevice> findByVendorId(Long vendorId);
}
