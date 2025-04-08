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
import aagapp_backend.services.vendor.VenderService;
import aagapp_backend.services.vendor.VendorDeviceService;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.hibernate.validator.internal.util.stereotypes.Lazy;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Service
public class DeviceMange {

/*    @Autowired
    private CustomerDeviceMangeRepository deviceMangeRepository;

    @Autowired
    @Lazy
    private VendorDeviceService vendorDeviceService;  // Use VendorDeviceService

    @Autowired
    private CustomCustomerService customCustomerService;

    @Autowired
    private ExceptionHandlingImplement exceptionHandling;

    @Autowired
    private VendorDeviceRepo vendorDeviceRepo*/;

    private final CustomerDeviceMangeRepository deviceMangeRepository;
    private final VendorDeviceService vendorDeviceService;
    private final CustomCustomerService customCustomerService;
    private final ExceptionHandlingImplement exceptionHandling;
    private final VendorDeviceRepo vendorDeviceRepo;

    @Autowired
    public DeviceMange(CustomerDeviceMangeRepository deviceMangeRepository,
                        VendorDeviceService vendorDeviceService,
                       CustomCustomerService customCustomerService,
                       ExceptionHandlingImplement exceptionHandling,
                       VendorDeviceRepo vendorDeviceRepo) {
        this.deviceMangeRepository = deviceMangeRepository;
        this.vendorDeviceService = vendorDeviceService;
        this.customCustomerService = customCustomerService;
        this.exceptionHandling = exceptionHandling;
        this.vendorDeviceRepo = vendorDeviceRepo;
    }

    private final ObjectMapper objectMapper = new ObjectMapper();

    // Method to check if the user is logged in from a new device
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


    // Method to record customer login device
    public void recordLoginDevice(CustomCustomer user, String ipAddress, String userAgent, String jwtToken) {
        try {
            UserDevice userDevice = new UserDevice();
            userDevice.setUser(user);
            userDevice.setIpAddress(ipAddress);
            userDevice.setUserAgent(userAgent);
            userDevice.setLoginTime(LocalDateTime.now());
            deviceMangeRepository.save(userDevice);

            // Add the JWT token to the user's serialized token list
            List<String> tokens = getSerializedTokenForCustomer(user);
            tokens.add(jwtToken);
            saveSerializedTokensForCustomer(user, tokens);
        } catch (Exception e) {
            exceptionHandling.handleException(e);
        }
    }

    // Method to record vendor login device (delegated to VendorDeviceService)
    public void recordVendorLoginDevice(VendorEntity user, String ipAddress, String userAgent, String jwtToken) {
        try {
            vendorDeviceService.recordVendorLoginDevice(user, ipAddress, userAgent, jwtToken);  // Delegation
        } catch (Exception e) {
            exceptionHandling.handleException(e);
        }
    }

    // Method to get the login details of a customer (list of devices)
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

    // Method to get the login details of a vendor (list of devices)
    public List<VendorDeviceDTO> getVendorDeviceLoginDetails(Long userId) {
        try {
            return vendorDeviceService.getVendorDeviceLoginDetails(userId);  // Delegation
        } catch (Exception e) {
            exceptionHandling.handleException(e);
            return null;
        }
    }

    // Helper method to save the serialized JWT tokens list for a customer
    private void saveSerializedTokensForCustomer(CustomCustomer user, List<String> tokens) {
        try {
            String serializedTokens = objectMapper.writeValueAsString(tokens);
            user.setToken(serializedTokens); // Save serialized tokens to the user
            customCustomerService.save(user);
        } catch (Exception e) {
            exceptionHandling.handleException(e);
        }
    }

    // Helper method to retrieve the serialized JWT tokens list for a customer
    private List<String> getSerializedTokenForCustomer(CustomCustomer user) {
        try {
            if (user.getToken() == null || user.getToken().isEmpty()) {
                return new ArrayList<>();
            }
            return objectMapper.readValue(user.getToken(), new TypeReference<List<String>>() {});
        } catch (Exception e) {
            exceptionHandling.handleException(e);
            return new ArrayList<>();
        }
    }
}