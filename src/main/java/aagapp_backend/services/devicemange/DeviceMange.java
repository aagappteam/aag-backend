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
import aagapp_backend.services.exception.ExceptionHandlingImplement;
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

    @Autowired
    private ExceptionHandlingImplement exceptionHandling;

    public boolean isLoggedInFromNewDevice(Long user, String ipAddress, String userAgent) {
        try {
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
            return true;
        } catch (Exception e) {
            exceptionHandling.handleException(e);
            return false;
        }
    }

    public boolean isVendorLoginFromNewDevice(Long user, String ipAddress, String userAgent) {
        try {
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
            return true;
        } catch (Exception e) {
            exceptionHandling.handleException(e);
            return false;
        }
    }


    public void recordLoginDevice(CustomCustomer user, String ipAddress, String userAgent) {
        try {
            UserDevice userDevice = new UserDevice();
            userDevice.setUser(user);
            userDevice.setIpAddress(ipAddress);
            userDevice.setUserAgent(userAgent);
            userDevice.setLoginTime(LocalDateTime.now());
            deviceMangeRepository.save(userDevice);
        } catch (Exception e) {
            exceptionHandling.handleException(e);
        }
    }

    public void recordVendorLoginDevice(VendorEntity user, String ipAddress, String userAgent) {
        try {
            VendorDevice vendorDevice = new VendorDevice();
            vendorDevice.setVendor(user);
            vendorDevice.setIpAddress(ipAddress);
            vendorDevice.setUserAgent(userAgent);
            vendorDevice.setLoginTime(LocalDateTime.now());
            vendorDeviceRepo.save(vendorDevice);
        } catch (Exception e) {
            exceptionHandling.handleException(e);
        }
    }

    public List<CustomerDeviceDTO> getCustomerDeviceLoginDetails(Long userId) {
        try {
            List<UserDevice> loginHistoryList = deviceMangeRepository.findByUserId(userId);

            List<CustomerDeviceDTO> devices = new ArrayList<>();
            for (UserDevice device : loginHistoryList) {
                CustomerDeviceDTO dto = new CustomerDeviceDTO(device.getUser().getId(), device.getIpAddress(), device.getUserAgent(), device.getLoginTime());
                devices.add(dto);

            }
            return devices;
        } catch (Exception e) {
            exceptionHandling.handleException(e);
            return null;
        }

    }

    public List<VendorDeviceDTO> getVendorDeviceLoginDetails(Long userId) {
        try {
            List<VendorDevice> loginHistoryList = vendorDeviceRepo.findByVendorId(userId);

            List<VendorDeviceDTO> deviceDetails = new ArrayList<>();
            for (VendorDevice device : loginHistoryList) {
                VendorDeviceDTO dto = new VendorDeviceDTO(device.getVendor().getService_provider_id(), device.getIpAddress(), device.getUserAgent(), device.getLoginTime());
                deviceDetails.add(dto);
            }
            return deviceDetails;
        } catch (Exception e) {
            exceptionHandling.handleException(e);
            return null;
        }
    }

}
