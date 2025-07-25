package aagapp_backend.controller.bank;

import aagapp_backend.dto.bank.BankDTO;
import aagapp_backend.services.bank.BankService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/banks")
@RequiredArgsConstructor
public class BankController {

    private final BankService bankService;

    @PostMapping
    public ResponseEntity<BankDTO> createBank(@Valid @RequestBody BankDTO dto) {
        return ResponseEntity.ok(bankService.createBank(dto));
    }

    @GetMapping
    public ResponseEntity<Page<BankDTO>> getBanks(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size,
            @RequestParam(required = false) Long bankId,
            @RequestParam(required = false) String bankName
    ) {
        return ResponseEntity.ok(bankService.getBanks(page, size, bankId, bankName));
    }


    @GetMapping("/{id}")
    public ResponseEntity<BankDTO> getBankById(@PathVariable Long id) {
        return ResponseEntity.ok(bankService.getBankById(id));
    }

    @PutMapping("/{id}")
    public ResponseEntity<BankDTO> updateBank(@PathVariable Long id, @Valid @RequestBody BankDTO dto) {
        return ResponseEntity.ok(bankService.updateBank(id, dto));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteBank(@PathVariable Long id) {
        bankService.deleteBank(id);
        return ResponseEntity.noContent().build();
    }
}

