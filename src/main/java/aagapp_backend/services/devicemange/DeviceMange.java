package aagapp_backend.services.devicemange;

import aagapp_backend.entity.CustomCustomer;
import aagapp_backend.entity.VendorEntity;
import aagapp_backend.entity.devices.UserDevice;
import aagapp_backend.entity.devices.VendorDevice;
import aagapp_backend.repository.CustomerDeviceMangeRepository;
import aagapp_backend.repository.VendorDeviceRepo;
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
//        vendorDevice.setVendorId(user.getService_provider_id());
        vendorDevice.setVendor(user);
        vendorDevice.setIpAddress(ipAddress);
        vendorDevice.setUserAgent(userAgent);
        vendorDevice.setLoginTime(LocalDateTime.now());
        vendorDeviceRepo.save(vendorDevice);
    }

    public List<UserDevice> getDeviceDetailsForUser(Long userId) {
        List<UserDevice> loginHistoryList = deviceMangeRepository.findByUserId(userId);

        List<UserDevice> devices = new ArrayList<UserDevice>();
        for (UserDevice device : loginHistoryList) {
            devices.add(device);

        }
        return devices;

    }

}
