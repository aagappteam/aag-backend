package aagapp_backend.services.devicemange;

import aagapp_backend.dto.CustomerDeviceDTO;
import aagapp_backend.dto.VendorDeviceDTO;
import aagapp_backend.entity.CustomCustomer;
import aagapp_backend.entity.VendorEntity;
import aagapp_backend.entity.devices.UserDevice;
import aagapp_backend.entity.devices.VendorDevice;
import aagapp_backend.repository.customcustomer.CustomerDeviceMangeRepository;
import aagapp_backend.repository.vendor.VendorDeviceRepo;
import aagapp_backend.services.CustomCustomerService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Service
public class DeviceMange {

    @Autowired
    private CustomerDeviceMangeRepository deviceMangeRepository;

    @Autowired
    private VendorDeviceRepo vendorDeviceRepo;

    @Autowired
    private CustomCustomerService customCustomerService;

    public boolean isLoggedInFromNewDevice(Long user, String ipAddress, String userAgent) {
        if (ipAddress == null || userAgent == null) {
            throw new IllegalArgumentException("ipAddress and userAgent must not be null");
        }

        List<UserDevice> userDevices = deviceMangeRepository.findByUserId(user);

        // Iterate through the list of devices already associated with the user
        for (UserDevice device : userDevices) {
            // If a device with the same IP address and user agent exists, it's not a new device
            if (device.getIpAddress().equals(ipAddress) && device.getUserAgent().equals(userAgent)) {
                return false; // The user is not logged in from a new device
            }
        }

        // If no matching device is found, it's a new device
        return true; // The user is logged in from a new device
    }

    public boolean isVendorLoginFromNewDevice(Long user, String ipAddress, String userAgent) {
        if (ipAddress == null || userAgent == null) {
            throw new IllegalArgumentException("ipAddress and userAgent must not be null");
        }

        List<VendorDevice> vendorDevices = vendorDeviceRepo.findByVendorId(user);

        // Iterate through the list of devices already associated with the user
        for (VendorDevice device : vendorDevices) {
            // If a device with the same IP address and user agent exists, it's not a new device
            if (device.getIpAddress().equals(ipAddress) && device.getUserAgent().equals(userAgent)) {
                return false; // The user is not logged in from a new device
            }
        }

        // If no matching device is found, it's a new device
        return true; // The user is logged in from a new device
    }


    public void recordLoginDevice(CustomCustomer user, String ipAddress, String userAgent) {
        UserDevice userDevice = new UserDevice();
        userDevice.setUser(user);
        userDevice.setIpAddress(ipAddress);
        userDevice.setUserAgent(userAgent);
        userDevice.setLoginTime(LocalDateTime.now());
        deviceMangeRepository.save(userDevice);
    }

    public void recordVendorLoginDevice(VendorEntity user, String ipAddress, String userAgent) {
        VendorDevice vendorDevice = new VendorDevice();
        vendorDevice.setVendor(user);
        vendorDevice.setIpAddress(ipAddress);
        vendorDevice.setUserAgent(userAgent);
        vendorDevice.setLoginTime(LocalDateTime.now());
        vendorDeviceRepo.save(vendorDevice);
    }

    public List<CustomerDeviceDTO> getCustomerDeviceLoginDetails(Long userId) {
        List<UserDevice> loginHistoryList = deviceMangeRepository.findByUserId(userId);

        List<CustomerDeviceDTO> devices = new ArrayList<>();
        for (UserDevice device : loginHistoryList) {
            CustomerDeviceDTO dto = new CustomerDeviceDTO(
                    device.getUser().getId(),
                    device.getIpAddress(),
                    device.getUserAgent(),
                    device.getLoginTime()
            );
            devices.add(dto);

        }
        return devices;

    }

    public List<VendorDeviceDTO> getVendorDeviceLoginDetails(Long userId) {
        List<VendorDevice> loginHistoryList = vendorDeviceRepo.findByVendorId(userId);

        List<VendorDeviceDTO> deviceDetails = new ArrayList<>();
        for (VendorDevice device : loginHistoryList) {
            VendorDeviceDTO dto = new VendorDeviceDTO(
                    device.getVendor().getService_provider_id(),
                    device.getIpAddress(),
                    device.getUserAgent(),
                    device.getLoginTime()
            );
            deviceDetails.add(dto);
        }
        return deviceDetails;
    }

}
