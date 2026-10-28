package com.example.data.repository

import android.content.Context
import android.content.SharedPreferences
import com.example.data.model.Channel
import com.example.data.model.SubscriptionPlan
import com.example.data.payment.PaymentOrder
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import org.json.JSONArray
import org.json.JSONObject

data class OfflineDepositRequest(
    val id: String,
    val gatewayName: String,
    val amountHtg: Double,
    val transactionRef: String,
    val senderPhone: String,
    val timestamp: Long,
    val status: String = "EN_ATTENTE_SYNCHRONISATION"
)

/**
 * Manages persistent on-device cache and offline queues.
 * Allows the app to start, display channels, browse categories,
 * and submit deposits even when network is completely offline or very weak.
 */
object OfflineCacheManager {

    private const val PREFS_NAME = "smartone_offline_cache"
    private const val KEY_CACHED_BALANCE = "cached_balance"
    private const val KEY_CACHED_SUBSCRIPTION_PLAN = "cached_subscription_plan"
    private const val KEY_CACHED_CHANNELS_COUNT = "cached_channels_count"
    private const val KEY_OFFLINE_DEPOSITS_QUEUE = "offline_deposits_queue"
    private const val KEY_LAST_SYNC_TIME = "last_sync_time"

    private var prefs: SharedPreferences? = null

    private val _queuedOfflineDeposits = MutableStateFlow<List<OfflineDepositRequest>>(emptyList())
    val queuedOfflineDeposits: StateFlow<List<OfflineDepositRequest>> = _queuedOfflineDeposits.asStateFlow()

    private val _lastSyncTimestamp = MutableStateFlow<Long>(System.currentTimeMillis())
    val lastSyncTimestamp: StateFlow<Long> = _lastSyncTimestamp.asStateFlow()

    fun initialize(context: Context) {
        prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
        loadQueuedDeposits()
    }

    fun saveBalance(balance: Double) {
        prefs?.edit()?.putFloat(KEY_CACHED_BALANCE, balance.toFloat())?.apply()
    }

    fun getCachedBalance(default: Double = 0.0): Double {
        return prefs?.getFloat(KEY_CACHED_BALANCE, default.toFloat())?.toDouble() ?: default
    }

    fun saveActiveSubscription(planLabel: String?, expiryDate: String?) {
        prefs?.edit()
            ?.putString("cached_sub_plan", planLabel)
            ?.putString("cached_sub_expiry", expiryDate)
            ?.apply()
    }

    fun getCachedSubscription(): Pair<String?, String?> {
        val plan = prefs?.getString("cached_sub_plan", null)
        val expiry = prefs?.getString("cached_sub_expiry", null)
        return Pair(plan, expiry)
    }

    /**
     * Queues an offline deposit request when internet is weak or disconnected.
     * Guaranteed to persist and sync when connectivity returns.
     */
    fun enqueueOfflineDeposit(
        gatewayName: String,
        amountHtg: Double,
        transactionRef: String,
        senderPhone: String
    ): OfflineDepositRequest {
        val request = OfflineDepositRequest(
            id = "OFFLINE_${System.currentTimeMillis()}_${(1000..9999).random()}",
            gatewayName = gatewayName,
            amountHtg = amountHtg,
            transactionRef = transactionRef,
            senderPhone = senderPhone,
            timestamp = System.currentTimeMillis()
        )

        val updated = _queuedOfflineDeposits.value + request
        _queuedOfflineDeposits.value = updated
        persistQueuedDeposits(updated)
        return request
    }

    fun removeOrSyncDeposit(id: String) {
        val updated = _queuedOfflineDeposits.value.filterNot { it.id == id }
        _queuedOfflineDeposits.value = updated
        persistQueuedDeposits(updated)
    }

    private fun persistQueuedDeposits(list: List<OfflineDepositRequest>) {
        val jsonArray = JSONArray()
        list.forEach { item ->
            val obj = JSONObject().apply {
                put("id", item.id)
                put("gatewayName", item.gatewayName)
                put("amountHtg", item.amountHtg)
                put("transactionRef", item.transactionRef)
                put("senderPhone", item.senderPhone)
                put("timestamp", item.timestamp)
                put("status", item.status)
            }
            jsonArray.put(obj)
        }
        prefs?.edit()?.putString(KEY_OFFLINE_DEPOSITS_QUEUE, jsonArray.toString())?.apply()
    }

    private fun loadQueuedDeposits() {
        val raw = prefs?.getString(KEY_OFFLINE_DEPOSITS_QUEUE, null) ?: return
        try {
            val array = JSONArray(raw)
            val list = mutableListOf<OfflineDepositRequest>()
            for (i in 0 until array.length()) {
                val obj = array.getJSONObject(i)
                list.add(
                    OfflineDepositRequest(
                        id = obj.getString("id"),
                        gatewayName = obj.getString("gatewayName"),
                        amountHtg = obj.getDouble("amountHtg"),
                        transactionRef = obj.getString("transactionRef"),
                        senderPhone = obj.getString("senderPhone"),
                        timestamp = obj.getLong("timestamp"),
                        status = obj.optString("status", "EN_ATTENTE_SYNCHRONISATION")
                    )
                )
            }
            _queuedOfflineDeposits.value = list
        } catch (_: Exception) {}
    }

    fun updateLastSyncTime() {
        val now = System.currentTimeMillis()
        _lastSyncTimestamp.value = now
        prefs?.edit()?.putLong(KEY_LAST_SYNC_TIME, now)?.apply()
    }
}
