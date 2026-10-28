package com.example.data.payment

enum class PaymentGatewayType(val displayName: String, val brandColorHex: Long) {
    MONCASH("MonCash Online", 0xFFE11922),
    NATCASH("Natcash Online", 0xFFFF6600),
    NOWPAYMENTS_CRYPTO("NOWPayments Crypto", 0xFF059669),
    STRIPE_CHECKOUT("Stripe (Cartes & Apple Pay)", 0xFF0066FF)
}

enum class OrderStatus {
    INITIALIZED,
    REDIRECTED,
    PENDING_VALIDATION,
    COMPLETED,
    CANCELLED,
    FAILED
}

data class PaymentOrder(
    val orderId: String,
    val userId: String,
    val amount: Double,
    val currency: String = "HTG",
    val gateway: PaymentGatewayType,
    val status: OrderStatus,
    val checkoutUrl: String,
    val createdAt: Long = System.currentTimeMillis(),
    val completedAt: Long? = null,
    val transactionReference: String? = null
)

data class CreatePaymentRequest(
    val orderId: String,
    val userId: String,
    val amount: Double,
    val currency: String = "HTG",
    val gateway: PaymentGatewayType,
    val returnUrl: String = "app://smartone-iptv/payment/success",
    val cancelUrl: String = "app://smartone-iptv/payment/cancel"
)

data class CreatePaymentResponse(
    val success: Boolean,
    val orderId: String,
    val checkoutUrl: String,
    val paymentToken: String,
    val message: String? = null
)

data class WebhookPayload(
    val orderId: String,
    val transactionId: String,
    val amount: Double,
    val currency: String,
    val gateway: PaymentGatewayType,
    val status: String, // "COMPLETED"
    val signature: String,
    val timestamp: Long
)

data class WebhookResult(
    val success: Boolean,
    val message: String,
    val isDuplicateReplay: Boolean = false,
    val updatedOrder: PaymentOrder? = null
)
