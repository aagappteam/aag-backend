package aagapp_backend.controller.bank;

import aagapp_backend.entity.Bank.BankEntity;
import aagapp_backend.repository.bank.BankRepository;
import aagapp_backend.services.CommonBankService;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.boot.CommandLineRunner;
import org.springframework.core.io.ClassPathResource;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Map;

@Component
@RequiredArgsConstructor
public class BankSeeder implements CommandLineRunner {

    private final BankRepository bankRepository;

    @Override
    public void run(String... args) throws Exception {
        if (bankRepository.count() > 0) {
            System.out.println("✅ Banks already seeded.");
            return;
        }

        ObjectMapper mapper = new ObjectMapper();
        var file = new ClassPathResource("banks.json").getFile();

        Map<String, Object> data = mapper.readValue(file, new TypeReference<>() {});
        List<Map<String, Object>> bankList = (List<Map<String, Object>>) data.get("bank_list");

        List<BankEntity> banks = bankList.stream().map(item -> BankEntity.builder()
                .bankId(Long.valueOf(item.get("bank_id").toString()))
                .bankName(item.get("bank_name").toString())
                .ifscCode(item.get("ifsc_code") != null ? item.get("ifsc_code").toString().trim() : null)
                .build()).toList();

        bankRepository.saveAll(banks);
        System.out.println("✅ Seeded " + banks.size() + " banks into DB");
    }
}


