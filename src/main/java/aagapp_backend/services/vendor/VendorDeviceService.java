package aagapp_backend.services.vendor;

import aagapp_backend.dto.VendorDeviceDTO;
import aagapp_backend.entity.VendorEntity;
import aagapp_backend.entity.devices.VendorDevice;
import aagapp_backend.repository.vendor.VendorDeviceRepo;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.hibernate.validator.internal.util.stereotypes.Lazy;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Service
public class VendorDeviceService {

    @Autowired
    private VendorDeviceRepo vendorDeviceRepo;

    @Autowired
    private ObjectMapper objectMapper;

    @Autowired
    @Lazy
    private VenderService venderService;

    // Method to record vendor login details
    public void recordVendorLoginDevice(VendorEntity user, String ipAddress, String userAgent, String jwtToken) {
        try {
            // Creating a new VendorDevice instance and setting the fields
            VendorDevice vendorDevice = new VendorDevice();
            vendorDevice.setVendor(user);
            vendorDevice.setIpAddress(ipAddress);
            vendorDevice.setUserAgent(userAgent);
            vendorDevice.setLoginTime(LocalDateTime.now());

            // Saving the vendor device details
            vendorDeviceRepo.save(vendorDevice);

            // Add the JWT token to the vendor's serialized token list
            List<String> tokens = getSerializedTokenForVendor(user);
            tokens.add(jwtToken);
            saveSerializedTokensForVendor(user, tokens);
        } catch (Exception e) {
            // Handle exception (could be logged or passed to an exception handler)
            e.printStackTrace();
        }
    }

    // Helper method to retrieve the serialized JWT tokens list for a vendor
    private List<String> getSerializedTokenForVendor(VendorEntity user) {
        try {
            if (user.getToken() == null || user.getToken().isEmpty()) {
                return new ArrayList<>();
            }
            List<String> tokens = new ArrayList<>();
            tokens.add(user.getToken());  // Just add the JWT token as it is
            return tokens;
        } catch (Exception e) {
            e.printStackTrace();
            return new ArrayList<>();
        }
    }

    // Helper method to save the serialized JWT tokens list for a vendor
    private void saveSerializedTokensForVendor(VendorEntity user, List<String> tokens) {
        try {
            String serializedTokens = objectMapper.writeValueAsString(tokens);
            user.setToken(serializedTokens); // Save serialized tokens to the vendor
            venderService.saveOrUpdate(user);  // Assuming venderService saves the vendor entity
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    // Method to get vendor login device details
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
            e.printStackTrace();
            return new ArrayList<>();
        }
    }
}
