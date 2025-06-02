package aagapp_backend.services.admin;

import aagapp_backend.entity.invoice.InvoiceAdmin;
import org.springframework.data.jpa.domain.Specification;

import java.math.BigDecimal;
import java.time.LocalDate;

public class InvoiceAdminSpecification {

    public static Specification<InvoiceAdmin> hasPan(String pan) {
        return (root, query, cb) -> pan == null ? null : cb.equal(root.get("pan"), pan);
    }

    public static Specification<InvoiceAdmin> hasGstn(String gstn) {
        return (root, query, cb) -> gstn == null ? null : cb.equal(root.get("gstn"), gstn);
    }

    public static Specification<InvoiceAdmin> hasRecipientState(String state) {
        return (root, query, cb) -> state == null ? null : cb.equal(root.get("recipientState"), state);
    }

    public static Specification<InvoiceAdmin> hasServiceType(String serviceType) {
        return (root, query, cb) -> serviceType == null ? null : cb.equal(root.get("serviceType"), serviceType);
    }

    public static Specification<InvoiceAdmin> hasMinTaxableValue(BigDecimal minValue) {
        return (root, query, cb) -> minValue == null ? null : cb.greaterThanOrEqualTo(root.get("taxableValue"), minValue);
    }

    public static Specification<InvoiceAdmin> hasMaxTaxableValue(BigDecimal maxValue) {
        return (root, query, cb) -> maxValue == null ? null : cb.lessThanOrEqualTo(root.get("taxableValue"), maxValue);
    }

    public static Specification<InvoiceAdmin> hasInvoiceNo(String invoiceNo) {
        return (root, query, cb) -> invoiceNo == null ? null : cb.equal(root.get("invoiceNo"), invoiceNo);
    }

    public static Specification<InvoiceAdmin> hasId(Long id) {
        return (root, query, cb) -> id == null ? null : cb.equal(root.get("id"), id);
    }

    public static Specification<InvoiceAdmin> hasName(String name) {
        return (root, query, cb) -> name == null ? null : cb.equal(root.get("name"), name);
    }

    public static Specification<InvoiceAdmin> hasEmail(String email) {
        return (root, query, cb) -> email == null ? null : cb.equal(root.get("email"), email);
    }

    public static Specification<InvoiceAdmin> hasMobile(String mobile) {
        return (root, query, cb) -> mobile == null ? null : cb.equal(root.get("mobile"), mobile);
    }

    public static Specification<InvoiceAdmin> hasPanTypeCheck(String panTypeCheck) {
        return (root, query, cb) -> panTypeCheck == null ? null : cb.equal(root.get("panTypeCheck"), panTypeCheck);
    }

    public static Specification<InvoiceAdmin> hasInvoiceDate(String invoiceDate) {
        return (root, query, cb) -> invoiceDate == null ? null : cb.equal(root.get("invoiceDate"), invoiceDate);
    }


    public static Specification<InvoiceAdmin> hasRecipientType(String recipientType) {
        return (root, query, cb) -> recipientType == null ? null : cb.equal(root.get("recipientType"), recipientType);
    }

    public static Specification<InvoiceAdmin> hasPlaceOfSupply(String placeOfSupply) {
        return (root, query, cb) -> placeOfSupply == null ? null : cb.equal(root.get("placeOfSupply"), placeOfSupply);
    }



    // Add more specs as needed
}
