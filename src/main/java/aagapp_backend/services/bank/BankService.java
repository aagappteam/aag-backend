package aagapp_backend.services.bank;

import aagapp_backend.dto.bank.BankDTO;
import org.springframework.data.domain.Page;

import java.util.List;

public interface BankService {
    BankDTO createBank(BankDTO dto);
    List<BankDTO> getAllBanks();
    BankDTO getBankById(Long id);
    BankDTO updateBank(Long id, BankDTO dto);
    void deleteBank(Long id);

    Page<BankDTO> getBanks(int page, int size, Long bankId, String bankName);
}

