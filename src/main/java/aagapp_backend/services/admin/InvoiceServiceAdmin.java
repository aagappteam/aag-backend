package aagapp_backend.services.admin;

import aagapp_backend.entity.VendorEntity;
import aagapp_backend.entity.invoice.InvoiceAdmin;
import aagapp_backend.entity.kyc.KycEntity;
import aagapp_backend.repository.admin.InvoiceAdminRepository;
import aagapp_backend.repository.kycRepository.KycRepository;
import aagapp_backend.repository.vendor.VendorRepository;
import jakarta.persistence.EntityManager;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.time.format.TextStyle;
import java.util.Locale;

@Service
public class InvoiceServiceAdmin {

    @Autowired
    private InvoiceAdminRepository invoiceAdminRepository;

    @Autowired
    private VendorRepository vendorRepository;

    @Autowired
    private EntityManager entityManager;

    @Autowired
    private KycRepository kycRepository;

    public void createInvoice(Double paymentAmount, Long vendorId) {
        try {
            VendorEntity existingVendor = entityManager.find(VendorEntity.class, vendorId);
            if (existingVendor == null)
                throw new RuntimeException("Vendor not found");

            KycEntity kycEntity = kycRepository.findByUserOrVendorIdAndRole(vendorId, "vendor");

            InvoiceAdmin invoice = new InvoiceAdmin();

            // Vendor ki state
            String vendorState = existingVendor.getState();
            boolean isFromUP = "Uttar Pradesh".equalsIgnoreCase(vendorState);

            // Static GST Rate
            BigDecimal gstRate = new BigDecimal("18.00");

            // Payment amount as taxable value
            BigDecimal taxableValue = BigDecimal.valueOf(paymentAmount);
            BigDecimal gstAmount = taxableValue.multiply(gstRate).divide(new BigDecimal("100"));

            BigDecimal cgst = BigDecimal.ZERO;
            BigDecimal sgst = BigDecimal.ZERO;
            BigDecimal igst = BigDecimal.ZERO;

            String serviceType;

            if (isFromUP) {
                // Intra-state supply
                cgst = gstAmount.divide(new BigDecimal("2"));
                sgst = gstAmount.divide(new BigDecimal("2"));
                serviceType = "INFLUENCER WITHIN STATE";
                igst = BigDecimal.ZERO;
            } else {
                // Inter-state supply
                igst = gstAmount;
                serviceType = "INFLUENCER OUTSIDE STATE";
            }

            // Set all invoice values
            invoice.setRecipientState("Uttar Pradesh"); // Your business base state
            invoice.setRecipientType("Registered");
            invoice.setPlaceOfSupply(vendorState); // Vendor's state
            invoice.setGstn("-"); // Placeholder until KYC is done

            invoice.setTaxableValue(taxableValue);
            invoice.setGstRate(gstRate); // 18%
            invoice.setCgst(cgst);
            invoice.setSgst(sgst);
            invoice.setIgst(igst);
            invoice.setTotalInvoiceValue(taxableValue.add(gstAmount));
            invoice.setServiceType(serviceType);

            invoice.setInvoiceDate(LocalDate.now().format(DateTimeFormatter.ISO_LOCAL_DATE));
            invoice.setPan(kycEntity.getPanNo());
            invoice.setPanTypeCheck(checkPanType(kycEntity.getPanNo()));

            // Set unique invoice number
            invoice.setInvoiceNo(generateInvoiceNumber());

            // Save the invoice
            invoiceAdminRepository.save(invoice);
        } catch (Exception e) {
            // Log the error or rethrow if needed
            e.printStackTrace();
            throw new RuntimeException("Error while creating invoice: " + e.getMessage(), e);
        }
    }

    public String generateInvoiceNumber() {
        LocalDate today = LocalDate.now();
        int year = today.getYear();
        int month = today.getMonthValue();

        // Financial year (e.g., 25-26)
        String startYear, endYear;
        if (month >= 4) {
            startYear = String.format("%02d", year % 100);
            endYear = String.format("%02d", (year + 1) % 100);
        } else {
            startYear = String.format("%02d", (year - 1) % 100);
            endYear = String.format("%02d", year % 100);
        }
        String financialYear = startYear + "-" + endYear;

        // Current month in short form (APR, MAY, etc.)
        String monthStr = today.getMonth().getDisplayName(TextStyle.SHORT, Locale.ENGLISH).toUpperCase();

        // Prefix to match previous invoices
        String prefix = "AAG/" + financialYear + "/" + monthStr + "/";

        // Get the last invoice number with the same prefix
        String lastInvoiceNo = invoiceAdminRepository.findLastInvoiceNo(prefix);

        int nextSerial = 1;
        if (lastInvoiceNo != null && lastInvoiceNo.startsWith(prefix)) {
            try {
                String[] parts = lastInvoiceNo.split("/");
                nextSerial = Integer.parseInt(parts[3]) + 1;
            } catch (Exception ignored) {}
        }

        String serialFormatted = String.format("%04d", nextSerial);
        return prefix + serialFormatted;
    }

    public String checkPanType(String pan) {
        if (pan == null || pan.length() != 10) {
            return "Invalid PAN format";
        }

        char typeChar = Character.toUpperCase(pan.charAt(3));

        switch (typeChar) {
            case 'P': return "Individual";
            case 'C': return "Company";
            case 'H': return "HUF";
            case 'A': return "AOP";
            case 'B': return "BOI";
            case 'G': return "Government Agency";
            case 'J': return "Artificial Juridical Person";
            case 'L': return "Local Authority";
            case 'F': return "Firm/Partnership";
            case 'T': return "Trust";
            default: return "Unknown PAN type";
        }
    }
}
