package aagapp_backend.services;

import aagapp_backend.entity.CustomCustomer;
import aagapp_backend.entity.VendorEntity;
import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import jakarta.transaction.Transactional;
import org.springframework.stereotype.Service;

import java.util.Date;

@Service
public class VendorActivityService {
    @PersistenceContext
    private EntityManager entityManager;

    @Transactional
    public void updateVendorLastActive(Long vendorId) {
        VendorEntity vendor = entityManager.find(VendorEntity.class, vendorId);
        if (vendor != null) {
            vendor.setLastActiveAt(new Date());
            entityManager.merge(vendor);
        }
    }

    @Transactional
    public void updateCustomerLastActive(Long customerId) {
        CustomCustomer customer = entityManager.find(CustomCustomer.class, customerId);
        if (customer != null) {
            customer.setLastActiveAt(new Date());
            entityManager.merge(customer);
        }
    }
}

