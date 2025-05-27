package aagapp_backend.services.download;


import aagapp_backend.dto.invoice.InvoiceDTO;
import com.lowagie.text.*;
import com.lowagie.text.Font;
import com.lowagie.text.Image;
import com.lowagie.text.Rectangle;
import com.lowagie.text.pdf.*;
import com.lowagie.text.pdf.draw.LineSeparator;
import org.springframework.stereotype.Service;

import java.awt.*;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.format.DateTimeFormatter;
import java.util.stream.Stream;

@Service
public class InvoiceService {

    public ByteArrayInputStream generateInvoicePdf(InvoiceDTO dto) {
        Document document = new Document(PageSize.A4, 36, 36, 36, 36);
        ByteArrayOutputStream out = new ByteArrayOutputStream();

        try {
            PdfWriter.getInstance(document, out);
            document.open();

            DateTimeFormatter formatter = DateTimeFormatter.ofPattern("dd MMMM yyyy");

            Font titleFont = FontFactory.getFont(FontFactory.HELVETICA_BOLD, 20, new Color(0, 102, 204));
            Font headerFont = FontFactory.getFont(FontFactory.HELVETICA_BOLD, 14);
            Font normalFont = FontFactory.getFont(FontFactory.HELVETICA, 12);
            Font tableHeaderFont = FontFactory.getFont(FontFactory.HELVETICA_BOLD, 12, Color.WHITE);

            // --- Header Table (Logo + Title)
            PdfPTable headerTable = new PdfPTable(2);
            headerTable.setWidthPercentage(100);
            headerTable.setWidths(new float[]{6, 1});

            PdfPCell titleCell = new PdfPCell(new Phrase("TAX INVOICE", titleFont));
            titleCell.setBorder(Rectangle.NO_BORDER);
            titleCell.setVerticalAlignment(Element.ALIGN_MIDDLE);
            titleCell.setPaddingBottom(10);
            headerTable.addCell(titleCell);

            Image logo = Image.getInstance("https://aag-data.s3.ap-south-1.amazonaws.com/default-data/final+logo+of+AAG.png");
            logo.scaleToFit(80, 80);
            PdfPCell logoCell = new PdfPCell(logo, false);
            logoCell.setHorizontalAlignment(Element.ALIGN_RIGHT);
            logoCell.setBorder(Rectangle.NO_BORDER);
            headerTable.addCell(logoCell);

            document.add(headerTable);
            document.add(new LineSeparator());
            document.add(Chunk.NEWLINE);

            // --- Invoice Info
            PdfPTable invoiceInfo = new PdfPTable(2);
            invoiceInfo.setWidthPercentage(100);
            invoiceInfo.setSpacingAfter(10);
            invoiceInfo.setWidths(new float[]{1, 2});
            invoiceInfo.addCell(getLabelCell("Invoice ID:", headerFont));
            invoiceInfo.addCell(getValueCell(dto.getInvoiceId(), normalFont));
            invoiceInfo.addCell(getLabelCell("Invoice Date:", headerFont));
            invoiceInfo.addCell(getValueCell(dto.getDate().format(formatter), normalFont));
            invoiceInfo.addCell(getLabelCell("Place of Supply:", headerFont));
            invoiceInfo.addCell(getValueCell(dto.getState(), normalFont));
            invoiceInfo.addCell(getLabelCell("Reverse Charge:", headerFont));
            invoiceInfo.addCell(getValueCell("No", normalFont));
            document.add(invoiceInfo);

            // --- Seller Info
            document.add(new Paragraph("Company Details", headerFont));
            document.add(getCompanyParagraph(normalFont));
            document.add(Chunk.NEWLINE);

            // --- Buyer Info
            document.add(new Paragraph("Customer Details", headerFont));
            PdfPTable customerInfo = new PdfPTable(2);
            customerInfo.setWidthPercentage(100);
            customerInfo.setWidths(new float[]{1, 2});
            customerInfo.setSpacingAfter(10);
            customerInfo.addCell(getLabelCell("Name:", headerFont));
            customerInfo.addCell(getValueCell(dto.getInfluencerName(), normalFont));
            customerInfo.addCell(getLabelCell("Username:", headerFont));
            customerInfo.addCell(getValueCell("influencer_" + dto.getInfluencerId(), normalFont));
            customerInfo.addCell(getLabelCell("Email:", headerFont));
            customerInfo.addCell(getValueCell("influencer@example.com", normalFont));
            customerInfo.addCell(getLabelCell("Billing Address:", headerFont));
            customerInfo.addCell(getValueCell("Not Provided", normalFont));
            customerInfo.addCell(getLabelCell("GSTIN:", headerFont));
            customerInfo.addCell(getValueCell("-", normalFont));
            document.add(customerInfo);

            // --- GST Calculation
            BigDecimal cgst = BigDecimal.ZERO;
            BigDecimal sgst = BigDecimal.ZERO;
            BigDecimal igst = BigDecimal.ZERO;
            BigDecimal gstRate;

            boolean isUP = dto.getState().trim().equalsIgnoreCase("Uttar Pradesh");
            String userType = dto.getUserType().toLowerCase();

            if (userType.equals("customer")) {
                gstRate = new BigDecimal("0.28");
                if (isUP) {
                    cgst = dto.getApprovedAmount().multiply(new BigDecimal("0.14"));
                    sgst = dto.getApprovedAmount().multiply(new BigDecimal("0.14"));
                } else {
                    igst = dto.getApprovedAmount().multiply(gstRate);
                }
            } else if (userType.equals("vendor")) {
                gstRate = new BigDecimal("0.18");
                if (isUP) {
                    cgst = dto.getApprovedAmount().multiply(new BigDecimal("0.09"));
                    sgst = dto.getApprovedAmount().multiply(new BigDecimal("0.09"));
                } else {
                    igst = dto.getApprovedAmount().multiply(gstRate);
                }
            }

            // --- Details of Supply Table
            PdfPTable table = (igst.compareTo(BigDecimal.ZERO) > 0) ? new PdfPTable(6) : new PdfPTable(7);
            table.setWidthPercentage(100);
            table.setSpacingBefore(10f);
            table.setSpacingAfter(10f);

            String[] headers = (igst.compareTo(BigDecimal.ZERO) > 0)
                    ? new String[]{"S. No.", "Description", "HSN/SAC", "Unit Price", "Total", "IGST"}
                    : new String[]{"S. No.", "Description", "HSN/SAC", "Unit Price", "Total", "CGST", "SGST"};

            for (String h : headers) {
                PdfPCell cell = new PdfPCell(new Phrase(h, tableHeaderFont));
                cell.setBackgroundColor(new Color(0, 102, 204));
                cell.setHorizontalAlignment(Element.ALIGN_CENTER);
                cell.setPadding(5);
                table.addCell(cell);
            }

            // Add row
            table.addCell("1");
            table.addCell("Game Wallet Recharge");
            table.addCell("998439");
            table.addCell(dto.getApprovedAmount().toString());
            table.addCell(dto.getApprovedAmount().toString());
            if (igst.compareTo(BigDecimal.ZERO) > 0) {
                table.addCell(igst.setScale(2, RoundingMode.HALF_UP).toString());
            } else {
                table.addCell(cgst.setScale(2, RoundingMode.HALF_UP).toString());
                table.addCell(sgst.setScale(2, RoundingMode.HALF_UP).toString());
            }

            document.add(table);

            // --- Total Summary
            BigDecimal total = dto.getApprovedAmount().add(cgst).add(sgst).add(igst);
            document.add(new Paragraph("Total Summary", headerFont));
            document.add(new Paragraph("Subtotal: Rs. " + dto.getApprovedAmount(), normalFont));
            if (igst.compareTo(BigDecimal.ZERO) > 0) {
                document.add(new Paragraph("IGST: Rs. " + igst.setScale(2, RoundingMode.HALF_UP), normalFont));
            } else {
                document.add(new Paragraph("CGST: Rs. " + cgst.setScale(2, RoundingMode.HALF_UP), normalFont));
                document.add(new Paragraph("SGST: Rs. " + sgst.setScale(2, RoundingMode.HALF_UP), normalFont));
            }
            document.add(new Paragraph("Total Invoice Amount: Rs. " + total.setScale(2, RoundingMode.HALF_UP), headerFont));
            document.add(Chunk.NEWLINE);

            // --- Payment Info
            document.add(new Paragraph("Payment Details", headerFont));
            document.add(new Paragraph("Payment Mode: UPI / Credit Card / Wallet", normalFont));
            document.add(new Paragraph("Transaction ID: " + dto.getTransactionId(), normalFont));

            document.close();
        } catch (Exception e) {
            e.printStackTrace();
        }

        return new ByteArrayInputStream(out.toByteArray());
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

    private Paragraph getCompanyParagraph(Font font) {
        Paragraph p = new Paragraph();
        p.setFont(font);
        p.add("Name     : Celestialit Verse Private Limited\n");
        p.add("Address  : 1/d-6 Sector 1 Vistar Ply Mart Lucknow, Uttar Pradesh - 226010\n");
        p.add("GSTIN    : 09AALCC9415D1ZK\n");
        p.add("PAN      : AALCC9415D\n");
        p.add("Email    : contact@celestialitverse.com\n");
        p.add("Phone    : +91-9628577197\n");
        p.add("Website  : www.celestialitverse.com");
        return p;
    }


}
