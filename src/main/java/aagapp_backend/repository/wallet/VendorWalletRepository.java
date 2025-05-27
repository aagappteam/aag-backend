package aagapp_backend.repository.wallet;

import aagapp_backend.entity.wallet.VendorWallet;
import org.springframework.data.jpa.repository.JpaRepository;

public interface VendorWalletRepository extends JpaRepository<VendorWallet, Long> {
}
