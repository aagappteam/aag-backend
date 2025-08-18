package aagapp_backend.services;

import aagapp_backend.entity.Bank.BankEntity;
import aagapp_backend.repository.bank.BankRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class CommonBankService {

    @Autowired
    private BankRepository bankRepository;

    public void saveAllBanks(List<BankEntity> bankList) {
        bankRepository.saveAll(bankList);
    }
}

