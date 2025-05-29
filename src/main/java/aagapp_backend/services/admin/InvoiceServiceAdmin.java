package aagapp_backend.services.admin;


import aagapp_backend.entity.VendorEntity;
import aagapp_backend.entity.invoice.InvoiceAdmin;
import aagapp_backend.repository.admin.InvoiceAdminRepository;
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

    public void createInvoice(Double paymentAmount, Long vendorId) {

        VendorEntity existingVendor = entityManager.find(VendorEntity.class, vendorId);
        if (existingVendor == null)
            throw new RuntimeException("Vendor not found");

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
        invoice.setPan("N/A");

        invoice.setTaxableValue(taxableValue);
        invoice.setGstRate(gstRate); // 18%
        invoice.setCgst(cgst);
        invoice.setSgst(sgst);
        invoice.setIgst(igst);
        invoice.setTotalInvoiceValue(taxableValue.add(gstAmount)); // total = amount + GST
        invoice.setServiceType(serviceType);

        invoice.setInvoiceNo("INV-" + System.currentTimeMillis());

        invoice.setInvoiceDate(LocalDate.now().format(DateTimeFormatter.ISO_LOCAL_DATE));

        //set unique invoice number
        invoice.setInvoiceNo(generateInvoiceNumber());


        // Save the invoice
        invoiceAdminRepository.save(invoice);
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
        String lastInvoiceNo = invoiceAdminRepository.findLastInvoiceNo(prefix);  // You need to implement this in the repo

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

}
