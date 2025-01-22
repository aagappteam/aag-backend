package aagapp_backend.repository.vendor;

import aagapp_backend.entity.devices.VendorDevice;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface VendorDeviceRepo extends JpaRepository<VendorDevice, Long> {
    List<VendorDevice> findByVendorId(Long vendorId);
}
