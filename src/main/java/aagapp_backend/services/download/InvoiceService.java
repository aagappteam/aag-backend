package aagapp_backend.services.download;


import aagapp_backend.dto.invoice.InvoiceDTO;
import com.lowagie.text.*;
import com.lowagie.text.pdf.*;
import org.springframework.stereotype.Service;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.math.BigDecimal;

@Service
public class InvoiceService {

    public ByteArrayInputStream generateInvoicePdf(InvoiceDTO dto) {
        Document document = new Document();
        ByteArrayOutputStream out = new ByteArrayOutputStream();

        try {
            PdfWriter.getInstance(document, out);
            document.open();

            Font header = FontFactory.getFont(FontFactory.HELVETICA_BOLD, 18);
            Paragraph title = new Paragraph("Withdrawal Invoice", header);
            title.setAlignment(Element.ALIGN_CENTER);
            document.add(title);
            document.add(Chunk.NEWLINE);

            Font font = FontFactory.getFont(FontFactory.HELVETICA, 12);
            document.add(new Paragraph("Invoice ID: " + dto.getInvoiceId(), font));
            document.add(new Paragraph("Date: " + dto.getDate(), font));
            document.add(new Paragraph("Influencer: " + dto.getInfluencerName() + " (ID: " + dto.getInfluencerId() + ")", font));
            document.add(new Paragraph("Month: " + dto.getMonth(), font));
            document.add(new Paragraph("Transaction ID: " + dto.getTransactionId(), font));
            document.add(Chunk.NEWLINE);

            PdfPTable table = new PdfPTable(2);
            table.setWidthPercentage(100);

            table.addCell("Description");
            table.addCell("Amount");

            table.addCell("Approved Withdrawal");
            table.addCell("Rs. " + dto.getApprovedAmount());

            BigDecimal cgst = dto.getApprovedAmount().multiply(BigDecimal.valueOf(0.09));
            BigDecimal sgst = dto.getApprovedAmount().multiply(BigDecimal.valueOf(0.09));
            BigDecimal total = dto.getApprovedAmount().add(cgst).add(sgst);

            table.addCell("CGST (9%)");
            table.addCell("Rs. " + cgst.setScale(2, BigDecimal.ROUND_HALF_UP));

            table.addCell("SGST (9%)");
            table.addCell("Rs. " + sgst.setScale(2, BigDecimal.ROUND_HALF_UP));

            table.addCell("Total Payable");
            table.addCell("Rs. " + total.setScale(2, BigDecimal.ROUND_HALF_UP));

            document.add(table);
            document.close();

        } catch (DocumentException ex) {
            ex.printStackTrace();
        }

        return new ByteArrayInputStream(out.toByteArray());
    }
}
