package aagapp_backend.services.admin;

import aagapp_backend.entity.CustomCustomer;
import aagapp_backend.entity.VendorEntity;
import aagapp_backend.entity.invoice.InvoiceAdmin;
import aagapp_backend.entity.kyc.KycEntity;
import aagapp_backend.repository.admin.InvoiceAdminRepository;
import aagapp_backend.repository.kycRepository.KycRepository;
import aagapp_backend.repository.vendor.VendorRepository;
import com.lowagie.text.*;
import com.lowagie.text.Font;
import com.lowagie.text.Image;
import com.lowagie.text.Rectangle;
import com.lowagie.text.pdf.PdfPCell;
import com.lowagie.text.pdf.PdfPTable;
import com.lowagie.text.pdf.PdfWriter;
import com.lowagie.text.pdf.draw.LineSeparator;
import org.apache.poi.ss.usermodel.*;
import jakarta.persistence.EntityManager;
import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;

import java.awt.*;
import java.awt.Color;
import java.io.*;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.net.URL;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.time.format.TextStyle;
import java.util.List;
import java.util.Locale;
import java.util.Optional;

import static aagapp_backend.services.admin.InvoiceAdminSpecification.*;
import static org.apache.poi.ss.util.CellUtil.createCell;

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

    public void createInvoiceForVendor(Double paymentAmount, Long vendorId) {
        try {
            VendorEntity existingVendor = entityManager.find(VendorEntity.class, vendorId);

            String vendorState = "N/A";
            if (existingVendor != null && existingVendor.getState() != null) {
                vendorState = existingVendor.getState();
            }

            KycEntity kycEntity = null;
            try {
                kycEntity = kycRepository.findByUserOrVendorIdAndRole(vendorId, "vendor");
            } catch (Exception e) {
                // log if needed
            }

            InvoiceAdmin invoice = new InvoiceAdmin();

            boolean isFromUP = "Uttar Pradesh".equalsIgnoreCase(vendorState);

            // GST INCLUDED: GST rate = 18%
            BigDecimal gstRate = new BigDecimal("18.00");
            BigDecimal totalAmount = BigDecimal.valueOf(paymentAmount);
            BigDecimal hundred = new BigDecimal("100");
            BigDecimal divisor = hundred.add(gstRate);


            // Calculate Taxable and GST from total amount (inclusive)
            BigDecimal taxableValue = totalAmount.multiply(hundred).divide(divisor, 2, RoundingMode.HALF_UP);
            BigDecimal gstAmount = totalAmount.subtract(taxableValue);

            BigDecimal cgst = BigDecimal.ZERO;
            BigDecimal sgst = BigDecimal.ZERO;
            BigDecimal igst = BigDecimal.ZERO;
            String serviceType;

            if (isFromUP) {
                cgst = gstAmount.divide(new BigDecimal("2"));
                sgst = gstAmount.divide(new BigDecimal("2"));
                igst = BigDecimal.ZERO;
                serviceType = "INFLUENCER WITHIN STATE";
            } else {
                igst = gstAmount;
                serviceType = "INFLUENCER OUTSIDE STATE";
            }

            // Set fields with null-safety and "N/A" fallback
            invoice.setRecipientState("Uttar Pradesh"); // Base state of your company
            invoice.setRecipientType("Registered");

            invoice.setPlaceOfSupply(vendorState != null ? vendorState : "N/A");
            invoice.setGstn("N/A"); // Assume GSTN not available initially

            invoice.setTaxableValue(taxableValue);
            invoice.setGstRate(gstRate);
            invoice.setCgst(cgst);
            invoice.setSgst(sgst);
            invoice.setIgst(igst);
            invoice.setTotalInvoiceValue(taxableValue.add(gstAmount));
            invoice.setServiceType(serviceType);
            invoice.setName(existingVendor != null && existingVendor.getName() != null ? existingVendor.getName() : "N/A");
            invoice.setEmail(existingVendor != null && existingVendor.getPrimary_email() != null ? existingVendor.getPrimary_email() : "N/A");
            invoice.setMobile(existingVendor != null && existingVendor.getMobileNumber() != null ? existingVendor.getMobileNumber() : "N/A");

            invoice.setInvoiceDate(LocalDate.now().format(DateTimeFormatter.ISO_LOCAL_DATE));

            try {
                invoice.setPan(kycEntity != null && kycEntity.getPanNo() != null ? kycEntity.getPanNo() : "N/A");
                invoice.setPanTypeCheck(kycEntity != null && kycEntity.getPanNo() != null ? checkPanType(kycEntity.getPanNo()) : "N/A");
            } catch (Exception e) {
                invoice.setPan("N/A");
                invoice.setPanTypeCheck("N/A");
            }

            invoice.setInvoiceNo(generateInvoiceNumber());

            invoiceAdminRepository.save(invoice);

        } catch (Exception e) {
            e.printStackTrace();
            throw new RuntimeException("Error while creating invoice: " + e.getMessage(), e);
        }
    }


    public void createInvoiceForCustomer(Double paymentAmount, Long customerId) {
        try {
            CustomCustomer customCustomer = entityManager.find(CustomCustomer.class, customerId);

            String userState = "N/A";
            if (customCustomer != null && customCustomer.getState() != null) {
                userState = customCustomer.getState();
            }

            KycEntity kycEntity = null;
            try {
                kycEntity = kycRepository.findByUserOrVendorIdAndRole(customerId, "user");
            } catch (Exception e) {
                // log if needed
            }

            InvoiceAdmin invoice = new InvoiceAdmin();

            boolean isFromUP = "Uttar Pradesh".equalsIgnoreCase(userState);

            // GST INCLUDED: GST rate = 28%
            BigDecimal gstRate = new BigDecimal("28.00");
            BigDecimal totalAmount = BigDecimal.valueOf(paymentAmount);
            BigDecimal hundred = new BigDecimal("100");
            BigDecimal divisor = hundred.add(gstRate);


            // Calculate Taxable and GST from total amount (inclusive)
            BigDecimal taxableValue = totalAmount.multiply(hundred).divide(divisor, 2, RoundingMode.HALF_UP);
            BigDecimal gstAmount = totalAmount.subtract(taxableValue);

            BigDecimal cgst = BigDecimal.ZERO;
            BigDecimal sgst = BigDecimal.ZERO;
            BigDecimal igst = BigDecimal.ZERO;
            String serviceType;

            if (isFromUP) {
                cgst = gstAmount.divide(new BigDecimal("2"));
                sgst = gstAmount.divide(new BigDecimal("2"));
                igst = BigDecimal.ZERO;
                serviceType = "ONLINE GAMING WITHIN STATE";
            } else {
                igst = gstAmount;
                serviceType = "ONLINE GAMING OUTSIDE STATE";
            }

            // Set fields with null-safety and "N/A" fallback
            invoice.setRecipientState("Uttar Pradesh"); // Base state of your company
            invoice.setRecipientType("Unregistered");

            invoice.setPlaceOfSupply(userState != null ? userState : "N/A");
            invoice.setGstn("N/A"); // Assume GSTN not available initially

            invoice.setTaxableValue(taxableValue);
            invoice.setGstRate(gstRate);
            invoice.setCgst(cgst);
            invoice.setSgst(sgst);
            invoice.setIgst(igst);
            invoice.setTotalInvoiceValue(taxableValue.add(gstAmount));
            invoice.setServiceType(serviceType);
            invoice.setName(customCustomer != null && customCustomer.getName() != null ? customCustomer.getName() : "N/A");
            invoice.setEmail(customCustomer != null && customCustomer.getEmail() != null ? customCustomer.getEmail() : "N/A");
            invoice.setMobile(customCustomer != null && customCustomer.getMobileNumber() != null ? customCustomer.getMobileNumber() : "N/A");

            invoice.setInvoiceDate(LocalDate.now().format(DateTimeFormatter.ISO_LOCAL_DATE));

            try {
                invoice.setPan(kycEntity != null && kycEntity.getPanNo() != null ? kycEntity.getPanNo() : "N/A");
                invoice.setPanTypeCheck(kycEntity != null && kycEntity.getPanNo() != null ? checkPanType(kycEntity.getPanNo()) : "N/A");
            } catch (Exception e) {
                invoice.setPan("N/A");
                invoice.setPanTypeCheck("N/A");
            }

            invoice.setInvoiceNo(generateInvoiceNumber());

            invoiceAdminRepository.save(invoice);

        } catch (Exception e) {
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

        String lastInvoiceNo= invoiceAdminRepository.findLastInvoiceNo(prefix);


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



    public Page<InvoiceAdmin> getInvoices(
            Long id,
            String name,
            String email,
            String mobile,
            String gstn,
            String pan,
            String panTypeCheck,
            String invoiceNo,
            String invoiceDate,
            String recipientState,
            String recipientType,
            String placeOfSupply,
            String serviceType,
            Pageable pageable
    ) {
        Specification<InvoiceAdmin> spec = Specification
                .where(InvoiceAdminSpecification.hasId(id))
                .and(InvoiceAdminSpecification.hasName(name))
                .and(InvoiceAdminSpecification.hasEmail(email))
                .and(InvoiceAdminSpecification.hasMobile(mobile))
                .and(InvoiceAdminSpecification.hasGstn(gstn))
                .and(InvoiceAdminSpecification.hasPan(pan))
                .and(InvoiceAdminSpecification.hasPanTypeCheck(panTypeCheck))
                .and(InvoiceAdminSpecification.hasInvoiceNo(invoiceNo))
                .and(InvoiceAdminSpecification.hasInvoiceDate(invoiceDate))
                .and(InvoiceAdminSpecification.hasRecipientState(recipientState))
                .and(InvoiceAdminSpecification.hasRecipientType(recipientType))
                .and(InvoiceAdminSpecification.hasPlaceOfSupply(placeOfSupply))
                .and(InvoiceAdminSpecification.hasServiceType(serviceType));

        return invoiceAdminRepository.findAll(spec, pageable);
    }



    public byte[] generateInvoicePdf(Long id) throws Exception {
        Optional<InvoiceAdmin> optionalInvoice = invoiceAdminRepository.findById(id);
        if (optionalInvoice.isEmpty()) {
            throw new Exception("Invoice not found with ID: " + id);
        }

        InvoiceAdmin invoice = optionalInvoice.get();

        Document document = new Document(PageSize.A4, 36, 36, 36, 36);
        ByteArrayOutputStream out = new ByteArrayOutputStream();
        PdfWriter.getInstance(document, out);
        document.open();

        try {
            // Fonts and colors
            Font titleFont = FontFactory.getFont(FontFactory.HELVETICA_BOLD, 20, new Color(0, 102, 204));
            Font headerFont = FontFactory.getFont(FontFactory.HELVETICA_BOLD, 14);
            Font normalFont = FontFactory.getFont(FontFactory.HELVETICA, 12);
            Font tableHeaderFont = FontFactory.getFont(FontFactory.HELVETICA_BOLD, 12, Color.WHITE);
            Font labelFont = FontFactory.getFont(FontFactory.HELVETICA, 12);
            Font valueFont = FontFactory.getFont(FontFactory.HELVETICA, 12);
            Font totalValueFont = FontFactory.getFont(FontFactory.HELVETICA_BOLD, 12);


            // Header table
            PdfPTable headerTable = new PdfPTable(2);
            headerTable.setWidthPercentage(100);
            headerTable.setWidths(new float[]{6, 1});

            PdfPCell titleCell = new PdfPCell(new Phrase("TAX INVOICE", titleFont));
            titleCell.setBorder(Rectangle.NO_BORDER);
            titleCell.setVerticalAlignment(Element.ALIGN_MIDDLE);
            titleCell.setPaddingBottom(10);
            headerTable.addCell(titleCell);

//            Image logo = Image.getInstance("https://aag-data.s3.ap-south-1.amazonaws.com/default-data/final+logo+of+AAG.png");
            Image logo = loadLogo();
            logo.scaleToFit(60, 60);

//            logo.scaleToFit(80, 80);
            PdfPCell logoCell = new PdfPCell(logo, false);
            logoCell.setHorizontalAlignment(Element.ALIGN_RIGHT);
            logoCell.setBorder(Rectangle.NO_BORDER);
            headerTable.addCell(logoCell);

            document.add(headerTable);
            document.add(new LineSeparator());
            document.add(Chunk.NEWLINE);

            // Invoice Info
            PdfPTable invoiceInfo = new PdfPTable(2);
            invoiceInfo.setWidthPercentage(100);
            invoiceInfo.setSpacingAfter(10);
            invoiceInfo.setWidths(new float[]{1, 2});
            document.add(new Paragraph("Invoice Details", headerFont));
            invoiceInfo.addCell(getLabelCell("Invoice No", normalFont));
            invoiceInfo.addCell(getValueCell(invoice.getInvoiceNo(), normalFont));
            invoiceInfo.addCell(getLabelCell("Place of Supply", normalFont));
            invoiceInfo.addCell(getValueCell(invoice.getPlaceOfSupply(), normalFont));
//            invoiceInfo.addCell(getLabelCell("Service Type:", headerFont));
//            invoiceInfo.addCell(getValueCell(invoice.getServiceType(), normalFont));
            invoiceInfo.addCell(getLabelCell("Reverse Charge", normalFont));
            invoiceInfo.addCell(getValueCell("No", normalFont));
            document.add(invoiceInfo);

            // Company Info
            document.add(new Paragraph("Company Details", headerFont));
            document.add(getCompanyInfoTable(labelFont, valueFont));
            document.add(Chunk.NEWLINE);

            // Customer Info (can be static for now or updated later)
            document.add(new Paragraph("Customer Details", headerFont));
            PdfPTable customerInfo = new PdfPTable(2);
            customerInfo.setWidthPercentage(100);
            customerInfo.setWidths(new float[]{1, 2});
            customerInfo.setSpacingAfter(10);
            customerInfo.addCell(getLabelCell("Name", normalFont));
            customerInfo.addCell(getValueCell(invoice.getName(), normalFont));
            customerInfo.addCell(getLabelCell("Email", normalFont));
            customerInfo.addCell(getValueCell(invoice.getEmail(), normalFont));
            customerInfo.addCell(getLabelCell("Mobile No", normalFont));
            customerInfo.addCell(getValueCell(invoice.getMobile(), normalFont));
            customerInfo.addCell(getLabelCell("GSTIN", normalFont));
            customerInfo.addCell(getValueCell(invoice.getGstn(), normalFont));
            document.add(customerInfo);

            // Details of Supply
            PdfPTable table = new PdfPTable(8);
            table.setWidthPercentage(100);
            table.setSpacingBefore(10f);
            table.setSpacingAfter(10f);
            String[] headers = {"S.No.", "Descri\nption", "HSN/SAC", "Unit\nPrice", "CGST", "SGST","IGST", "Total\nAmount"};

            for (String h : headers) {
                PdfPCell cell = new PdfPCell(new Phrase(h, tableHeaderFont));
                cell.setBackgroundColor(new Color(0, 102, 204));
                cell.setHorizontalAlignment(Element.ALIGN_CENTER);
                cell.setPadding(1);
                table.addCell(cell);
            }

            // Data row
            table.addCell("1");
            table.addCell(invoice.getServiceType());
            table.addCell("998361");
            table.addCell(invoice.getTaxableValue().toString());
            table.addCell(invoice.getCgst().toString());
            table.addCell(invoice.getSgst().toString());
            table.addCell(invoice.getIgst().toString());
            table.addCell(invoice.getTotalInvoiceValue().toString());
            document.add(table);


            //total summary
            document.add(new Paragraph("Total Summary", headerFont));

            PdfPTable summaryTable = new PdfPTable(2);
            summaryTable.setWidthPercentage(100);
            summaryTable.setSpacingBefore(5f);
            summaryTable.setSpacingAfter(10f);
            summaryTable.setWidths(new float[]{2, 3});

            summaryTable.addCell(createCell("Taxable Value (Rs.):", labelFont));
            summaryTable.addCell(createCell(invoice.getTaxableValue().toPlainString(), valueFont));

            summaryTable.addCell(createCell("CGST (Rs.):", labelFont));
            summaryTable.addCell(createCell(invoice.getCgst().toPlainString(), valueFont));

            summaryTable.addCell(createCell("SGST (Rs.):", labelFont));
            summaryTable.addCell(createCell(invoice.getSgst().toPlainString(), valueFont));

            summaryTable.addCell(createCell("IGST (Rs.):", labelFont));
            summaryTable.addCell(createCell(invoice.getIgst().toPlainString(), valueFont));

            summaryTable.addCell(createCell("Total Invoice Amount (Rs.):", labelFont));
            summaryTable.addCell(createCell(invoice.getTotalInvoiceValue().toPlainString(), totalValueFont));

            document.add(summaryTable);


            // --- Payment Info
            document.add(new Paragraph("Payment Details", headerFont));
            document.add(new Paragraph("Payment Mode: UPI / Credit Card / Wallet", normalFont));
//            document.add(new Paragraph("Transaction ID: " + dto.getTransactionId(), normalFont));

            document.close();
        } catch (Exception e) {
            e.printStackTrace();
            throw new Exception("Error generating invoice PDF");
        }

        return out.toByteArray();
    }

    private Image loadLogo() throws IOException, BadElementException {
        URL logoUrl = getClass().getClassLoader().getResource("images/aag_logo.png");
        if (logoUrl == null) {
            throw new FileNotFoundException("Logo image not found in resources");
        }

        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        try (InputStream is = logoUrl.openStream()) {
            byte[] buffer = new byte[8192];
            int bytesRead;
            while ((bytesRead = is.read(buffer)) != -1) {
                baos.write(buffer, 0, bytesRead);
            }
        }

        return Image.getInstance(baos.toByteArray());
    }



    private PdfPCell getLabelCell(String text, Font font) {
        PdfPCell cell = new PdfPCell(new Phrase(text, font));
        cell.setBorder(Rectangle.NO_BORDER);
        return cell;
    }

    private PdfPCell getValueCell(String text, Font font) {
        PdfPCell cell = new PdfPCell(new Phrase(text, font));
        cell.setBorder(Rectangle.NO_BORDER);
        return cell;
    }

    private PdfPTable getCompanyInfoTable(Font labelFont, Font valueFont) {
        PdfPTable table = new PdfPTable(2); // 2 columns: Label and Value
        table.setWidthPercentage(100);
//        table.setSpacingBefore(10f);
        table.setWidths(new float[]{1, 2});
        table.setSpacingAfter(10);

        table.addCell(createCell("Name", labelFont));
        table.addCell(createCell("Celestialit Verse Private Limited", valueFont));

        table.addCell(createCell("Address", labelFont));
        table.addCell(createCell("1/d-6 Sector 1 Vistar Ply Mart Lucknow, Uttar Pradesh - 226010", valueFont));

        table.addCell(createCell("GSTIN", labelFont));
        table.addCell(createCell("09AALCC9415D1ZK", valueFont));

        table.addCell(createCell("PAN", labelFont));
        table.addCell(createCell("AALCC9415D", valueFont));

        table.addCell(createCell("Email", labelFont));
        table.addCell(createCell("contact@celestialitverse.com", valueFont));

//    table.addCell(createCell("Phone", labelFont));
//    table.addCell(createCell("+91-9628577197", valueFont));

        table.addCell(createCell("Website", labelFont));
        table.addCell(createCell("https://celestialitverse.com", valueFont));

        return table;
    }

    private PdfPCell createCell(String text, Font font) {
        PdfPCell cell = new PdfPCell(new Phrase(text, font));
        cell.setBorder(Rectangle.NO_BORDER);  // cleaner look without borders
        cell.setPadding(4f);
        return cell;
    }


    public byte[] generateInvoicesExcel(String startDate, String endDate) throws Exception {
        List<InvoiceAdmin> invoices;

        if (startDate != null && endDate != null) {
            invoices = invoiceAdminRepository.findByInvoiceDateBetween(startDate, endDate);
        } else if (startDate != null) {
            invoices = invoiceAdminRepository.findByInvoiceDateGreaterThanEqual(startDate);
        } else if (endDate != null) {
            invoices = invoiceAdminRepository.findByInvoiceDateLessThanEqual(endDate);
        } else {
            invoices = invoiceAdminRepository.findAll(); // export all
        }

        if (invoices.isEmpty()) {
            throw new Exception("No invoices found.");
        }

        Workbook workbook = new XSSFWorkbook();
        Sheet sheet = workbook.createSheet("Invoices");

        Row headerRow = sheet.createRow(0);
        String[] columns = {"ID", "GSTN (IN CASE OF INFLUENCER)", "PAN", "PAN Type Check", "Invoice No", "Invoice Date", "Recipient State", "Recipient Type", "Place of Supply", "Taxable Value (INR)", "GST Rate (%)", "IGST (INR)", "CGST (INR)", "SGST (INR)", "Total Invoice Value (INR)","Service Type"};
        for (int i = 0; i < columns.length; i++) {
            headerRow.createCell(i).setCellValue(columns[i]);
        }

        int rowNum = 1;
        for (InvoiceAdmin invoice : invoices) {
            Row row = sheet.createRow(rowNum++);
            row.createCell(0).setCellValue(invoice.getId());
            row.createCell(1).setCellValue(invoice.getGstn());
            row.createCell(2).setCellValue(invoice.getPan());
            row.createCell(3).setCellValue(invoice.getPanTypeCheck());
            row.createCell(4).setCellValue(invoice.getInvoiceNo());
            row.createCell(5).setCellValue(invoice.getInvoiceDate());
            row.createCell(6).setCellValue(invoice.getRecipientState());
            row.createCell(7).setCellValue(invoice.getRecipientType());
            row.createCell(8).setCellValue(invoice.getPlaceOfSupply());
            row.createCell(9).setCellValue(invoice.getTaxableValue().doubleValue());
            row.createCell(10).setCellValue(invoice.getGstRate().doubleValue());
            row.createCell(11).setCellValue(invoice.getIgst().doubleValue());
            row.createCell(12).setCellValue(invoice.getCgst().doubleValue());
            row.createCell(13).setCellValue(invoice.getSgst().doubleValue());
            row.createCell(14).setCellValue(invoice.getTotalInvoiceValue().doubleValue());
            row.createCell(15).setCellValue(invoice.getServiceType());

        }

        ByteArrayOutputStream out = new ByteArrayOutputStream();
        workbook.write(out);
        workbook.close();

        return out.toByteArray();
    }


    public String generateInvoicesExcelAsHtml(String startDate, String endDate) throws Exception {
        byte[] excelData = generateInvoicesExcel(startDate, endDate);
        ByteArrayInputStream excelInputStream = new ByteArrayInputStream(excelData);
        Workbook workbook = new XSSFWorkbook(excelInputStream);
        Sheet sheet = workbook.getSheetAt(0);

        StringBuilder html = new StringBuilder();
        html.append("<html><head><style>")
                .append("body { font-family: Calibri, sans-serif; background-color: #fff; }")
                .append("table { border-collapse: collapse; width: 100%; font-size: 14px; }")
                .append("th, td { border: 1px solid #d0d7de; padding: 8px; text-align: left; vertical-align: top; white-space: nowrap; }")
                .append("th { background-color: #f3f3f3; font-weight: bold; color: #000; }")
                .append("tr:nth-child(even) td { background-color: #fafafa; }")
                .append("</style></head><body>");

        html.append("<table>");

        for (Row row : sheet) {
            html.append("<tr>");
            for (Cell cell : row) {
                String tag = row.getRowNum() == 0 ? "th" : "td";
                html.append("<").append(tag).append(">")
                        .append(getCellValueAsString(cell))
                        .append("</").append(tag).append(">");
            }
            html.append("</tr>");
        }

        html.append("</table></body></html>");
        workbook.close();
        return html.toString();
    }

    private String getCellValueAsString(Cell cell) {
        if (cell == null) return "";
        switch (cell.getCellType()) {
            case STRING:
                return cell.getStringCellValue();
            case NUMERIC:
                if (DateUtil.isCellDateFormatted(cell)) {
                    return cell.getDateCellValue().toString();
                } else {
                    return String.valueOf(cell.getNumericCellValue());
                }
            case BOOLEAN:
                return String.valueOf(cell.getBooleanCellValue());
            case FORMULA:
                return cell.getCellFormula();
            case BLANK:
                return "";
            default:
                return "";
        }
    }

}
