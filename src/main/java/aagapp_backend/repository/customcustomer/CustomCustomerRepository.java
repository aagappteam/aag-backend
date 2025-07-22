package aagapp_backend.repository.customcustomer;

import aagapp_backend.entity.CustomCustomer;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.Date;
import java.util.List;
import java.util.Optional;

@Repository
public interface CustomCustomerRepository extends JpaRepository<CustomCustomer, Long>, JpaSpecificationExecutor<CustomCustomer> {
    Optional<CustomCustomer> findByMobileNumber(String mobileNumber);

    Optional<CustomCustomer> findByEmail(String email);

    boolean existsByEmail(String email);

    boolean existsByMobileNumber(String mobileNumber);

    Optional<CustomCustomer> findByToken(String token);


    List<CustomCustomer> findByCountryCode(String countryCode);

    List<CustomCustomer> findByState(String state);


    @Query("SELECT COUNT(v) FROM CustomCustomer v WHERE v.lastActiveAt >= :activeSince")
    Long countActiveVendors(Date activeSince);

    @Query("SELECT COUNT(v) FROM CustomCustomer v WHERE v.lastActiveAt < :activeSince OR v.lastActiveAt IS NULL")
    Long countInactiveVendors(Date activeSince);

    Optional<Object> findByReferralCode(String referralCode);
}
