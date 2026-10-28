package com.example.data.model

data class SubscriptionPlan(
    val id: String,
    val label: String,
    val durationMonths: Int,
    val priceGds: Double
) {
    val priceUsd: Double get() = priceGds / 132.0
    val priceEur: Double get() = priceGds / 143.0
}

data class PaymentGateway(
    val id: String,
    val name: String,
    val status: String,
    val minHtg: Double,
    val maxHtg: Double,
    val type: String,
    val merchantMasked: String,
    val isBlocked: Boolean = false
)

data class UserAccount(
    val userId: String,
    val name: String,
    val email: String,
    val phone: String,
    val role: String = "client", // "client" or "admin"
    val status: String = "active", // "active", "suspended", "banned"
    val isOnline: Boolean = true,
    val isVisitor: Boolean = false,
    val isVerified: Boolean = false
)

data class ActiveSubscription(
    val subscriptionId: String,
    val userId: String,
    val planId: String,
    val label: String,
    val startedAt: Long,
    val expiresAt: Long,
    val maxDevices: Int = 2
)

data class ConnectedDevice(
    val deviceId: String,
    val deviceName: String,
    val deviceType: String,
    val ipAddress: String,
    val connectedAt: String,
    val isCurrentDevice: Boolean = false
)

data class TransactionRecord(
    val transactionId: String,
    val userId: String,
    val planId: String? = null,
    val description: String,
    val amount: Double,
    val currency: String = "HTG",
    val paymentMethod: String,
    val status: String = "SUCCESS",
    val createdAt: String
)

data class Channel(
    val id: String,
    val name: String,
    val category: String,
    val country: String,
    val currentProgram: String,
    val resolution: String = "1080p 60fps",
    val bitrate: String = "6.5 Mbps",
    val countryCode: String = "🌐"
)

data class AdminClient(
    val id: String,
    val name: String,
    val email: String,
    val phone: String,
    val planId: String,
    val planLabel: String,
    val expiresAt: String,
    val isOnline: Boolean,
    val isSuspended: Boolean,
    val canPayForOthers: Boolean = true
)

data class ThirdPartySubscription(
    val id: String,
    val payerId: String,
    val payerName: String,
    val recipientIdentifier: String, // email or phone
    val planLabel: String,
    val amountHtg: Double,
    val timestamp: String,
    val status: String = "ACTIVE"
)

data class DailyRevenue(
    val dayLabel: String,
    val amount: Double
)

data class UserComment(
    val id: String,
    val author: String,
    val phone: String,
    val rating: Int = 5,
    val content: String,
    val date: String,
    val badge: String = "Client Vérifié"
)
