package aagapp_backend.repository.wallet;

import aagapp_backend.entity.CustomCustomer;
import aagapp_backend.entity.wallet.Wallet;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;


@Repository
public interface WalletRepository extends JpaRepository<Wallet, Long> {
    Optional<Wallet> findByCustomCustomerId(Long customerId);

    Wallet findByCustomCustomer(CustomCustomer customer);

    Wallet findByCustomCustomer_Id(Long customerId);
}
