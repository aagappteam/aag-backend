package aagapp_backend.services.vendor;

import aagapp_backend.entity.VendorEntity;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpSession;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.bind.annotation.RequestBody;

import java.util.List;
import java.util.Map;

@Service
public interface VenderService {
    VendorEntity saveServiceProvider(VendorEntity VendorEntity);
    ResponseEntity<?> updateServiceProvider(Long userId, @RequestBody Map<String, Object> updates) throws Exception;
    VendorEntity getServiceProviderById(Long userId);
    VendorEntity  findServiceProviderByPhone(String mobileNumber, String countryCode);
    ResponseEntity<?> loginWithPassword(Map<String, Object> loginDetails, HttpServletRequest request, HttpSession session);
    ResponseEntity<?> sendOtp(String mobileNumber, String countryCode, HttpSession session);
    VendorEntity saveOrUpdate(VendorEntity vendorEntity);
    List<VendorEntity> getTopInvitiesVendor();
    Map<String, Object> getTopInvitiesVendorWithAuth(Long authorizedVendorId);
}