package aagapp_backend.repository.customcustomer;

import aagapp_backend.entity.CustomCustomer;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface CustomCustomerRepository extends JpaRepository<CustomCustomer, Long> {
    Optional<CustomCustomer> findByMobileNumber(String mobileNumber);

    Optional<CustomCustomer> findByEmail(String email);

    boolean existsByEmail(String email);

    boolean existsByMobileNumber(String mobileNumber);

    Optional<CustomCustomer> findByToken(String token);


    List<CustomCustomer> findByCountryCode(String countryCode);

    List<CustomCustomer> findByState(String state);
}
