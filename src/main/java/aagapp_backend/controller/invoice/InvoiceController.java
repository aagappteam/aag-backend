package aagapp_backend.controller.invoice;

import aagapp_backend.dto.invoice.InvoiceDTO;
import aagapp_backend.services.download.InfluencerEarningsService;
import aagapp_backend.services.download.InvoiceService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.*;
import org.springframework.web.bind.annotation.*;

import java.io.ByteArrayInputStream;
import java.util.NoSuchElementException;

@RestController
@RequestMapping("/invoice")
public class InvoiceController {

    @Autowired
    private InvoiceService invoiceService;

    @Autowired
    private InfluencerEarningsService earningsService;

    @PostMapping("/download")
    public ResponseEntity<byte[]> downloadInvoice(@RequestBody InvoiceDTO invoiceDTO) {
        ByteArrayInputStream bis = invoiceService.generateInvoicePdf(invoiceDTO);

        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_PDF);
        headers.setContentDisposition(ContentDisposition.builder("attachment")
                .filename("Invoice_" + invoiceDTO.getInvoiceId() + ".pdf")
                .build());

        byte[] pdfBytes = bis.readAllBytes();
        return new ResponseEntity<>(pdfBytes, headers, HttpStatus.OK);
    }


/*    @GetMapping("/download")
    public ResponseEntity<byte[]> downloadInvoice(@RequestParam Long influencerId) {
        try {
            InvoiceDTO dto = earningsService.getInvoiceDataByInfluencerId(influencerId);
            ByteArrayInputStream bis = invoiceService.generateInvoicePdf(dto);

            HttpHeaders headers = new HttpHeaders();
            headers.add("Content-Disposition", "inline; filename=invoice.pdf");

            return ResponseEntity.ok()
                    .headers(headers)
                    .contentType(MediaType.APPLICATION_PDF)
                    .body(bis.readAllBytes());

        } catch (NoSuchElementException e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND)
                    .body(("Invoice data not found for influencerId: " + influencerId).getBytes());
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(("Error generating invoice: " + e.getMessage()).getBytes());
        }
    }*/
}
