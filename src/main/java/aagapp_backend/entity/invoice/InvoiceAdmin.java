package aagapp_backend.entity.invoice;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.TimeZone;

@Entity
@Table(name = "invoice_admin")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class InvoiceAdmin {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String name;

    private String email;

    private String mobile;

    // GSTN (IN CASE OF INFLUENCER)
    private String gstn;

    // PAN
    private String pan;

    // PAN Type Check
    private String panTypeCheck;

    // Invoice Number
    private String invoiceNo;

    // Invoice Date
    private String invoiceDate;

    // Recipient State
    private String recipientState;

    // Recipient Type
    private String recipientType;

    // Place of Supply
    private String placeOfSupply;

    // Taxable Value (INR)
    @Column(precision = 15, scale = 2)
    private BigDecimal taxableValue;

    // GST Rate (%)
    @Column(precision = 5, scale = 2)
    private BigDecimal gstRate;

    // IGST (INR)
    @Column(precision = 15, scale = 2)
    private BigDecimal igst;

    // CGST (INR)
    @Column(precision = 15, scale = 2)
    private BigDecimal cgst;

    // SGST (INR)
    @Column(precision = 15, scale = 2)
    private BigDecimal sgst;

    // Total Invoice Value (INR)
    @Column(precision = 15, scale = 2)
    private BigDecimal totalInvoiceValue;

    // Service Type
    private String serviceType;
}