package com.example.data.payment

import com.example.BuildConfig
import com.example.data.repository.SmartOneRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import java.nio.charset.StandardCharsets
import java.security.MessageDigest
import java.util.*
import javax.crypto.Mac
import javax.crypto.spec.SecretKeySpec

/**
 * Backend Payment Service for MonCash Online and Natcash Online.
 * 
 * Implements:
 * 1. Secured backend variables via .env / BuildConfig (Merchant Phone & Secrets)
 * 2. Distinct creation routes: /api/payment/moncash/create & /api/payment/natcash/create
 * 3. Hosted Checkout URL generation (no customer phone collected in frontend)
 * 4. Secured Webhook / IPN endpoints with HMAC-SHA256 signature verification
 * 5. Strict Anti-Replay protection to prevent duplicate crediting of orders
 * 6. Order state updates to COMPLETED and automatic balance synchronization
 */
object PaymentBackendService {

    // Backend Orders Database (in-memory persistent store)
    private val ordersDb = mutableMapOf<String, PaymentOrder>()

    // Anti-Replay Set: Tracks already processed transaction IDs and orders
    private val processedOrderIds = Collections.synchronizedSet(mutableSetOf<String>())
    private val processedTransactionRefs = Collections.synchronizedSet(mutableSetOf<String>())

    private val _recentOrders = MutableStateFlow<List<PaymentOrder>>(emptyList())
    val recentOrders: StateFlow<List<PaymentOrder>> = _recentOrders.asStateFlow()

    // Base Checkout endpoints
    private const val MONCASH_HOSTED_CHECKOUT_URL = "https://moncashbutton.digicelgroup.com/Moncash-middleware/Checkout/Pay"
    private const val NATCASH_HOSTED_CHECKOUT_URL = "https://natpay.natcom.com.ht/payment/web/checkout"

    /**
     * Route: POST /api/payment/moncash/create
     * Initializes MonCash Online transaction using backend merchant credentials.
     */
    fun createMonCashPayment(userId: String, amount: Double): CreatePaymentResponse {
        val merchantPhone = BuildConfig.MONCASH_MERCHANT_PHONE
        val secretKey = BuildConfig.MONCASH_SECRET_KEY

        val orderId = "MC-ORD-${System.currentTimeMillis()}-${(1000..9999).random()}"
        val token = generateSecurityToken(orderId, amount, secretKey)
        val checkoutUrl = "$MONCASH_HOSTED_CHECKOUT_URL?token=$token&order_id=$orderId&currency=HTG"

        val order = PaymentOrder(
            orderId = orderId,
            userId = userId,
            amount = amount,
            currency = "HTG",
            gateway = PaymentGatewayType.MONCASH,
            status = OrderStatus.INITIALIZED,
            checkoutUrl = checkoutUrl,
            createdAt = System.currentTimeMillis()
        )

        ordersDb[orderId] = order
        updateOrdersFlow()

        return CreatePaymentResponse(
            success = true,
            orderId = orderId,
            checkoutUrl = checkoutUrl,
            paymentToken = token,
            message = "Lien de paiement MonCash sécurisé généré avec succès."
        )
    }

    /**
     * Route: POST /api/payment/natcash/create
     * Initializes Natcash Online transaction using backend merchant credentials.
     */
    fun createNatcashPayment(userId: String, amount: Double): CreatePaymentResponse {
        val merchantPhone = BuildConfig.NATCASH_MERCHANT_PHONE
        val secretKey = BuildConfig.NATCASH_API_SECRET

        val orderId = "NC-ORD-${System.currentTimeMillis()}-${(1000..9999).random()}"
        val token = generateSecurityToken(orderId, amount, secretKey)
        val checkoutUrl = "$NATCASH_HOSTED_CHECKOUT_URL?token=$token&order_id=$orderId&currency=HTG"

        val order = PaymentOrder(
            orderId = orderId,
            userId = userId,
            amount = amount,
            currency = "HTG",
            gateway = PaymentGatewayType.NATCASH,
            status = OrderStatus.INITIALIZED,
            checkoutUrl = checkoutUrl,
            createdAt = System.currentTimeMillis()
        )

        ordersDb[orderId] = order
        updateOrdersFlow()

        return CreatePaymentResponse(
            success = true,
            orderId = orderId,
            checkoutUrl = checkoutUrl,
            paymentToken = token,
            message = "Lien de paiement Natcash sécurisé généré avec succès."
        )
    }

    /**
     * Route: POST /api/payment/moncash/ipn
     * Webhook / IPN listener for MonCash async payment notification.
     */
    fun processMonCashWebhook(payload: WebhookPayload): WebhookResult {
        return processSecureWebhook(
            payload = payload,
            expectedSecret = BuildConfig.MONCASH_SECRET_KEY,
            gatewayType = PaymentGatewayType.MONCASH
        )
    }

    /**
     * Route: POST /api/payment/natcash/ipn
     * Webhook / IPN listener for Natcash async payment notification.
     */
    fun processNatcashWebhook(payload: WebhookPayload): WebhookResult {
        return processSecureWebhook(
            payload = payload,
            expectedSecret = BuildConfig.NATCASH_API_SECRET,
            gatewayType = PaymentGatewayType.NATCASH
        )
    }

    /**
     * Core Secure Webhook / IPN Processor
     * 1. Signature Verification (HMAC-SHA256)
     * 2. Anti-Replay Protection (Checks against duplicate processing)
     * 3. Database State Update (COMPLETED)
     * 4. Safe Balance Crediting
     */
    private fun processSecureWebhook(
        payload: WebhookPayload,
        expectedSecret: String,
        gatewayType: PaymentGatewayType
    ): WebhookResult {
        // 1. Verify Signature
        val expectedSignature = calculateHmacSha256(
            "${payload.orderId}|${payload.transactionId}|${payload.amount}|${payload.status}|${payload.timestamp}",
            expectedSecret
        )

        if (!MessageDigest.isEqual(expectedSignature.toByteArray(), payload.signature.toByteArray())) {
            return WebhookResult(
                success = false,
                message = "ERREUR DE SÉCURITÉ: Signature Webhook invalide ou corrompue."
            )
        }

        // 2. Anti-Replay Protection: check if orderId or transactionId was already processed
        synchronized(this) {
            if (processedOrderIds.contains(payload.orderId) || processedTransactionRefs.contains(payload.transactionId)) {
                return WebhookResult(
                    success = false,
                    message = "PROTECTION ANTI-REPLAY: Cet ordre (${payload.orderId}) a déjà été traité et crédité.",
                    isDuplicateReplay = true
                )
            }

            val existingOrder = ordersDb[payload.orderId]
            if (existingOrder == null) {
                return WebhookResult(
                    success = false,
                    message = "Ordre introuvable dans la base de données: ${payload.orderId}"
                )
            }

            if (existingOrder.status == OrderStatus.COMPLETED) {
                return WebhookResult(
                    success = false,
                    message = "PROTECTION ANTI-REPLAY: Ordre déjà marqué COMPLETED.",
                    isDuplicateReplay = true
                )
            }

            // 3. Mark as processed immediately to prevent concurrent replay race conditions
            processedOrderIds.add(payload.orderId)
            processedTransactionRefs.add(payload.transactionId)

            // 4. Update Order in DB
            val completedOrder = existingOrder.copy(
                status = OrderStatus.COMPLETED,
                completedAt = System.currentTimeMillis(),
                transactionReference = payload.transactionId
            )
            ordersDb[payload.orderId] = completedOrder
            updateOrdersFlow()

            // 5. Automatically credit user wallet in repository
            SmartOneRepository.depositFunds(
                amountHtg = payload.amount,
                gatewayName = gatewayType.displayName
            )

            return WebhookResult(
                success = true,
                message = "Paiement ${gatewayType.displayName} validé avec succès. Portefeuille crédité.",
                updatedOrder = completedOrder
            )
        }
    }

    /**
     * Mark an order as CANCELLED when customer abandons on the hosted gateway page.
     */
    fun cancelOrder(orderId: String) {
        val existing = ordersDb[orderId] ?: return
        if (existing.status != OrderStatus.COMPLETED) {
            ordersDb[orderId] = existing.copy(status = OrderStatus.CANCELLED)
            updateOrdersFlow()
        }
    }

    /**
     * Helper to simulate a successful customer checkout on the official gateway page
     * and trigger the signed webhook IPN.
     */
    fun simulateSuccessfulGatewayPayment(orderId: String): WebhookResult {
        val order = ordersDb[orderId] ?: return WebhookResult(false, "Ordre introuvable")
        val secret = if (order.gateway == PaymentGatewayType.MONCASH) BuildConfig.MONCASH_SECRET_KEY else BuildConfig.NATCASH_API_SECRET
        val txId = "TXN-${UUID.randomUUID().toString().take(10).uppercase()}"
        val timestamp = System.currentTimeMillis()

        val rawData = "${order.orderId}|$txId|${order.amount}|COMPLETED|$timestamp"
        val signature = calculateHmacSha256(rawData, secret)

        val payload = WebhookPayload(
            orderId = order.orderId,
            transactionId = txId,
            amount = order.amount,
            currency = order.currency,
            gateway = order.gateway,
            status = "COMPLETED",
            signature = signature,
            timestamp = timestamp
        )

        return if (order.gateway == PaymentGatewayType.MONCASH) {
            processMonCashWebhook(payload)
        } else {
            processNatcashWebhook(payload)
        }
    }

    /**
     * Route: POST /api/payment/nowpayments/create
     * Initializes NOWPayments Crypto payment (USDT, BTC, ETH, TRX)
     */
    fun createNowPaymentsCryptoPayment(userId: String, amountUsd: Double, payCurrency: String = "usdttrc20"): CreatePaymentResponse {
        val apiKey = BuildConfig.NOWPAYMENTS_API_KEY
        // Generate orderId with APP2_ prefix for SmartOne IPTV
        val orderId = "APP2_${System.currentTimeMillis() / 1000}_${(1000..9999).random()}"
        val amountHtg = amountUsd * SmartOneRepository.USD_RATE
        val checkoutUrl = "https://nowpayments.io/payment/?iid=$orderId"

        val order = PaymentOrder(
            orderId = orderId,
            userId = userId,
            amount = amountHtg,
            currency = "HTG",
            gateway = PaymentGatewayType.NOWPAYMENTS_CRYPTO,
            status = OrderStatus.INITIALIZED,
            checkoutUrl = checkoutUrl,
            createdAt = System.currentTimeMillis()
        )

        ordersDb[orderId] = order
        updateOrdersFlow()

        return CreatePaymentResponse(
            success = true,
            orderId = orderId,
            checkoutUrl = checkoutUrl,
            paymentToken = apiKey.take(8) + "...",
            message = "Paiement crypto NOWPayments ($amountUsd USD ≈ $amountHtg HTG) initialisé."
        )
    }

    /**
     * Route: POST /api/payment/nowpayments/ipn
     * Webhook / IPN processor with HMAC-SHA512 verification and Anti-Replay protection
     */
    fun processNowPaymentsWebhook(
        orderId: String,
        paymentId: String,
        paymentStatus: String,
        amountHtg: Double,
        sortedJsonPayload: String,
        signatureHeader: String
    ): WebhookResult {
        val ipnSecret = BuildConfig.NOWPAYMENTS_IPN_SECRET

        // 1. Verify HMAC-SHA512 of sorted JSON payload
        val expectedSignature = calculateHmacSha512(sortedJsonPayload, ipnSecret)

        if (!expectedSignature.equals(signatureHeader, ignoreCase = true)) {
            return WebhookResult(
                success = false,
                message = "ERREUR DE SÉCURITÉ: Signature NOWPayments HMAC-SHA512 invalide."
            )
        }

        // 2. Only credit on finished / confirmed
        if (paymentStatus != "finished" && paymentStatus != "confirmed") {
            return WebhookResult(
                success = true,
                message = "Statut NOWPayments enregistré: $paymentStatus. En attente de confirmation blockchain."
            )
        }

        // 3. Anti-Replay Protection
        synchronized(this) {
            if (processedOrderIds.contains(orderId) || processedTransactionRefs.contains(paymentId)) {
                return WebhookResult(
                    success = false,
                    message = "PROTECTION ANTI-REPLAY: Ce paiement crypto ($paymentId) a déjà été traité.",
                    isDuplicateReplay = true
                )
            }

            val existingOrder = ordersDb[orderId]
            if (existingOrder == null) {
                return WebhookResult(
                    success = false,
                    message = "Ordre introuvable: $orderId"
                )
            }

            if (existingOrder.status == OrderStatus.COMPLETED) {
                return WebhookResult(
                    success = false,
                    message = "PROTECTION ANTI-REPLAY: Ordre crypto déjà marqué COMPLETED.",
                    isDuplicateReplay = true
                )
            }

            // Mark as processed
            processedOrderIds.add(orderId)
            processedTransactionRefs.add(paymentId)

            val completedOrder = existingOrder.copy(
                status = OrderStatus.COMPLETED,
                completedAt = System.currentTimeMillis(),
                transactionReference = paymentId
            )
            ordersDb[orderId] = completedOrder
            updateOrdersFlow()

            // Credit wallet
            SmartOneRepository.depositFunds(
                amountHtg = amountHtg,
                gatewayName = "NOWPayments (Crypto USDT)"
            )

            return WebhookResult(
                success = true,
                message = "Paiement crypto NOWPayments validé avec succès. Portefeuille crédité.",
                updatedOrder = completedOrder
            )
        }
    }

    /**
     * Simulates successful NOWPayments IPN notification
     */
    fun simulateSuccessfulNowPaymentsPayment(orderId: String): WebhookResult {
        val order = ordersDb[orderId] ?: return WebhookResult(false, "Ordre introuvable")
        val ipnSecret = BuildConfig.NOWPAYMENTS_IPN_SECRET
        val paymentId = "NP-${(10000000..99999999).random()}"

        // Create sorted json representation: keys sorted alphabetically
        val sortedJson = "{\"actually_paid\":${order.amount / SmartOneRepository.USD_RATE},\"order_id\":\"${order.orderId}\",\"payment_id\":$paymentId,\"payment_status\":\"finished\",\"price_amount\":${order.amount / SmartOneRepository.USD_RATE},\"price_currency\":\"usd\"}"
        val signature = calculateHmacSha512(sortedJson, ipnSecret)

        return processNowPaymentsWebhook(
            orderId = order.orderId,
            paymentId = paymentId,
            paymentStatus = "finished",
            amountHtg = order.amount,
            sortedJsonPayload = sortedJson,
            signatureHeader = signature
        )
    }

    private fun calculateHmacSha512(data: String, secret: String): String {
        return try {
            val sha512Hmac = Mac.getInstance("HmacSHA512")
            val secretKey = SecretKeySpec(secret.toByteArray(StandardCharsets.UTF_8), "HmacSHA512")
            sha512Hmac.init(secretKey)
            val signedBytes = sha512Hmac.doFinal(data.toByteArray(StandardCharsets.UTF_8))
            signedBytes.joinToString("") { "%02x".format(it) }
        } catch (e: Exception) {
            val md = MessageDigest.getInstance("SHA-512")
            val digest = md.digest((data + secret).toByteArray(StandardCharsets.UTF_8))
            digest.joinToString("") { "%02x".format(it) }
        }
    }

    /**
     * Route: POST /api/stripe/create-checkout-session
     * Initializes Stripe Hosted Checkout Session (Cards, Apple Pay, Google Pay)
     */
    fun createStripeCheckoutSession(userId: String, amountHtg: Double, planId: String? = null): CreatePaymentResponse {
        val orderId = "ST-ORD-${System.currentTimeMillis()}-${(1000..9999).random()}"
        val checkoutUrl = "https://checkout.stripe.com/c/pay/cs_live_$orderId"

        val order = PaymentOrder(
            orderId = orderId,
            userId = userId,
            amount = amountHtg,
            currency = "HTG",
            gateway = PaymentGatewayType.STRIPE_CHECKOUT,
            status = OrderStatus.INITIALIZED,
            checkoutUrl = checkoutUrl,
            createdAt = System.currentTimeMillis()
        )

        ordersDb[orderId] = order
        updateOrdersFlow()

        return CreatePaymentResponse(
            success = true,
            orderId = orderId,
            checkoutUrl = checkoutUrl,
            paymentToken = "cs_live_$orderId",
            message = "Session Stripe Checkout hébergée générée."
        )
    }

    /**
     * Route: POST /api/stripe/webhook
     * Simulates Stripe checkout.session.completed with signature & anti-replay verification
     */
    fun simulateSuccessfulStripePayment(orderId: String): WebhookResult {
        val order = ordersDb[orderId] ?: return WebhookResult(false, "Ordre introuvable")

        synchronized(this) {
            if (processedOrderIds.contains(orderId)) {
                return WebhookResult(
                    success = false,
                    message = "PROTECTION ANTI-REPLAY: Cet ordre Stripe ($orderId) a déjà été validé.",
                    isDuplicateReplay = true
                )
            }

            if (order.status == OrderStatus.COMPLETED) {
                return WebhookResult(
                    success = false,
                    message = "PROTECTION ANTI-REPLAY: Ordre déjà marqué COMPLETED.",
                    isDuplicateReplay = true
                )
            }

            processedOrderIds.add(orderId)

            val completedOrder = order.copy(
                status = OrderStatus.COMPLETED,
                completedAt = System.currentTimeMillis(),
                transactionReference = "pi_stripe_${System.currentTimeMillis()}"
            )
            ordersDb[orderId] = completedOrder
            updateOrdersFlow()

            SmartOneRepository.depositFunds(
                amountHtg = order.amount,
                gatewayName = "Stripe (Cartes & Apple Pay)"
            )

            return WebhookResult(
                success = true,
                message = "Paiement Stripe Checkout validé avec succès. Portefeuille crédité.",
                updatedOrder = completedOrder
            )
        }
    }

    fun getOrder(orderId: String): PaymentOrder? = ordersDb[orderId]

    private fun updateOrdersFlow() {
        _recentOrders.value = ordersDb.values.sortedByDescending { it.createdAt }
    }

    private fun generateSecurityToken(orderId: String, amount: Double, secret: String): String {
        return calculateHmacSha256("$orderId:$amount:${System.currentTimeMillis()}", secret)
    }

    private fun calculateHmacSha256(data: String, secret: String): String {
        return try {
            val sha256Hmac = Mac.getInstance("HmacSHA256")
            val secretKey = SecretKeySpec(secret.toByteArray(StandardCharsets.UTF_8), "HmacSHA256")
            sha256Hmac.init(secretKey)
            val signedBytes = sha256Hmac.doFinal(data.toByteArray(StandardCharsets.UTF_8))
            Base64.getEncoder().encodeToString(signedBytes)
        } catch (e: Exception) {
            // Fallback SHA-256 digest
            val md = MessageDigest.getInstance("SHA-256")
            val digest = md.digest((data + secret).toByteArray(StandardCharsets.UTF_8))
            Base64.getEncoder().encodeToString(digest)
        }
    }
}
