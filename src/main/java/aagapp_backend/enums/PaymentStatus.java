package aagapp_backend.enums;

public enum PaymentStatus {
    PENDING,    // Payment is being processed
    ACTIVE,     // Payment was successful, plan is now active
    FAILED,     // Payment failed
    CANCELLED,  // Payment was cancelled
    EXPIRED,    // Payment has expired
    REFUNDED,   // Payment was refunded
    DECLINED    // Payment was declined by the payment gateway
}
