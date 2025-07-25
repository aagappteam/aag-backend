package aagapp_backend.services.bank;

import aagapp_backend.dto.bank.BankDTO;
import aagapp_backend.entity.Bank.BankEntity;
import aagapp_backend.repository.bank.BankRepository;
import aagapp_backend.services.BankSpecification;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class BankServiceImpl implements BankService {

    private final BankRepository bankRepository;

    private BankDTO mapToDTO(BankEntity entity) {
        return BankDTO.builder()
                .bankId(entity.getBankId())
                .bankName(entity.getBankName())
                .ifscCode(entity.getIfscCode())
                .build();
    }

    private BankEntity mapToEntity(BankDTO dto) {
        return BankEntity.builder()
                .bankId(dto.getBankId())
                .bankName(dto.getBankName())
                .ifscCode(dto.getIfscCode())
                .build();
    }

    @Override
    public BankDTO createBank(BankDTO dto) {
        BankEntity saved = bankRepository.save(mapToEntity(dto));
        return mapToDTO(saved);
    }

    @Override
    public List<BankDTO> getAllBanks() {
        return bankRepository.findAll().stream().map(this::mapToDTO).collect(Collectors.toList());
    }

    @Override
    public BankDTO getBankById(Long id) {
        BankEntity bank = bankRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Bank not found with ID: " + id));
        return mapToDTO(bank);
    }

    @Override
    public BankDTO updateBank(Long id, BankDTO dto) {
        BankEntity bank = bankRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Bank not found with ID: " + id));

        bank.setBankName(dto.getBankName());
        bank.setIfscCode(dto.getIfscCode());
        return mapToDTO(bankRepository.save(bank));
    }

    @Override
    public void deleteBank(Long id) {
        BankEntity bank = bankRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Bank not found with ID: " + id));
        bankRepository.delete(bank);
    }
    @Override
    public Page<BankDTO> getBanks(int page, int size, Long bankId, String bankName) {
        Pageable pageable = PageRequest.of(page, size, Sort.by("bankName").ascending());

        Specification<BankEntity> spec = Specification.where(BankSpecification.hasBankId(bankId))
                .and(BankSpecification.hasBankNameLike(bankName));

        return bankRepository.findAll(spec, pageable).map(this::mapToDTO);
    }

}

