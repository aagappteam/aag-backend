package aagapp_backend.services.download;

import aagapp_backend.dto.invoice.InvoiceDTO;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.NoSuchElementException;

@Service
public class InfluencerEarningsService {

    public InvoiceDTO getInvoiceDataByInfluencerId(Long influencerId) {
/*        InfluencerEarnings earnings = earningsRepository
                .findTopByInfluencerIdOrderByDateDesc(influencerId)
                .orElseThrow(() -> new NoSuchElementException("No earnings found for influencer ID: " + influencerId));*/

        LocalDate localDate = LocalDate.now();
        InvoiceDTO dto = new InvoiceDTO();
        dto.setInvoiceId("INV-" + System.currentTimeMillis());
        dto.setDate(localDate);
        dto.setInfluencerId(influencerId);
        dto.setInfluencerName("Anil");
        dto.setMonth("May");
        dto.setTransactionId("TXN-" + System.currentTimeMillis());
        dto.setApprovedAmount(BigDecimal.valueOf(1000.00));

        return dto;
    }
}

