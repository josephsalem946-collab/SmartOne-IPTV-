package com.example.data.network

import com.example.data.payment.CreatePaymentResponse
import com.example.data.payment.PaymentGatewayType
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.RequestBody.Companion.toRequestBody
import org.json.JSONObject
import java.util.concurrent.TimeUnit

/**
 * Secure Backend Proxy Client for Mobile App.
 *
 * Communicates exclusively with our backend server:
 * - Injects Firebase ID token in Authorization header
 * - Never includes or requests merchant credentials
 * - Zero sensitive keys exposed in the mobile app
 */
class SecurePaymentProxyClient(
    private val backendBaseUrl: String = "https://api.smartone-iptv.com"
) {
    private val client = OkHttpClient.Builder()
        .connectTimeout(30, TimeUnit.SECONDS)
        .readTimeout(35, TimeUnit.SECONDS)
        .writeTimeout(35, TimeUnit.SECONDS)
        .retryOnConnectionFailure(true)
        .build()

    private val jsonMediaType = "application/json; charset=utf-8".toMediaType()

    suspend fun createPayment(
        gateway: PaymentGatewayType,
        amount: Double,
        planId: String? = null,
        firebaseIdToken: String
    ): Result<CreatePaymentResponse> = withContext(Dispatchers.IO) {
        val endpoint = if (gateway == PaymentGatewayType.MONCASH) {
            "$backendBaseUrl/api/payment/moncash/create"
        } else {
            "$backendBaseUrl/api/payment/natcash/create"
        }

        val payload = JSONObject().apply {
            put("amount", amount)
            if (planId != null) {
                put("planId", planId)
            }
            put("returnUrl", "app://smartone-iptv/payment/success")
            put("cancelUrl", "app://smartone-iptv/payment/cancel")
        }

        val request = Request.Builder()
            .url(endpoint)
            .addHeader("Authorization", "Bearer $firebaseIdToken")
            .addHeader("Content-Type", "application/json")
            .post(payload.toString().toRequestBody(jsonMediaType))
            .build()

        try {
            client.newCall(request).execute().use { response ->
                val bodyString = response.body?.string().orEmpty()
                if (response.isSuccessful) {
                    val json = JSONObject(bodyString)
                    Result.success(
                        CreatePaymentResponse(
                            success = json.optBoolean("success", true),
                            orderId = json.getString("orderId"),
                            checkoutUrl = json.getString("checkoutUrl"),
                            paymentToken = json.optString("paymentToken", ""),
                            message = if (json.has("message")) json.getString("message") else null
                        )
                    )
                } else {
                    val errJson = runCatching { JSONObject(bodyString) }.getOrNull()
                    val msg = errJson?.optString("message") ?: "Erreur de paiement (${response.code})"
                    Result.failure(Exception(msg))
                }
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
}
