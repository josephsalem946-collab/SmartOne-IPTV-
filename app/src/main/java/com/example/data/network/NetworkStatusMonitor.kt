package com.example.data.network

import android.content.Context
import android.net.ConnectivityManager
import android.net.Network
import android.net.NetworkCapabilities
import android.net.NetworkRequest
import android.os.Build
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

enum class ConnectionQuality {
    EXCELLENT, // High speed Wi-Fi or 5G
    MODERATE,  // Standard 4G
    WEAK,      // 2G, 3G, high packet loss, slow cellular
    OFFLINE    // No internet connection
}

data class NetworkStatus(
    val isConnected: Boolean = true,
    val isWeak: Boolean = false,
    val quality: ConnectionQuality = ConnectionQuality.EXCELLENT,
    val connectionType: String = "Wi-Fi Haut Débit",
    val isLowDataModeEnabled: Boolean = false,
    val isSimulatedMode: Boolean = false,
    val latencyMs: Long = 45L
)

/**
 * Real-time Network Monitor and Offline Resilience Manager.
 * Detects weak cellular, low bandwidth, and offline states.
 * Allows graceful degradation to low-bitrate and cached offline mode.
 */
object NetworkStatusMonitor {

    private val _networkStatus = MutableStateFlow(NetworkStatus())
    val networkStatus: StateFlow<NetworkStatus> = _networkStatus.asStateFlow()

    private var connectivityManager: ConnectivityManager? = null
    private val scope = CoroutineScope(Dispatchers.Default)

    fun initialize(context: Context) {
        val cm = context.getSystemService(Context.CONNECTIVITY_SERVICE) as? ConnectivityManager
        connectivityManager = cm

        if (cm != null) {
            val builder = NetworkRequest.Builder()
                .addCapability(NetworkCapabilities.NET_CAPABILITY_INTERNET)
            
            val callback = object : ConnectivityManager.NetworkCallback() {
                override fun onAvailable(network: Network) {
                    updateFromCapabilities(cm.getNetworkCapabilities(network))
                }

                override fun onLost(network: Network) {
                    _networkStatus.value = _networkStatus.value.copy(
                        isConnected = false,
                        isWeak = false,
                        quality = ConnectionQuality.OFFLINE,
                        connectionType = "Aucune Connexion (Hors-Ligne)",
                        latencyMs = 0L
                    )
                }

                override fun onCapabilitiesChanged(network: Network, capabilities: NetworkCapabilities) {
                    updateFromCapabilities(capabilities)
                }
            }

            try {
                cm.registerDefaultNetworkCallback(callback)
            } catch (e: Exception) {
                // Fallback for older or restricted environments
                try {
                    cm.registerNetworkCallback(builder.build(), callback)
                } catch (_: Exception) {}
            }

            // Initial check
            val activeNetwork = cm.activeNetwork
            val capabilities = cm.getNetworkCapabilities(activeNetwork)
            updateFromCapabilities(capabilities)
        }
    }

    private fun updateFromCapabilities(capabilities: NetworkCapabilities?) {
        if (_networkStatus.value.isSimulatedMode) {
            return // Don't override user manual test simulation
        }

        if (capabilities == null || !capabilities.hasCapability(NetworkCapabilities.NET_CAPABILITY_INTERNET)) {
            _networkStatus.value = _networkStatus.value.copy(
                isConnected = false,
                isWeak = false,
                quality = ConnectionQuality.OFFLINE,
                connectionType = "Hors-Ligne"
            )
            return
        }

        val isWifi = capabilities.hasTransport(NetworkCapabilities.TRANSPORT_WIFI)
        val isCellular = capabilities.hasTransport(NetworkCapabilities.TRANSPORT_CELLULAR)
        val downstreamBandwidthKbps = capabilities.linkDownstreamBandwidthKbps

        // Weak connection heuristic: cellular with low bandwidth (< 1500 kbps) or high congestion
        val isSlowCellular = isCellular && (downstreamBandwidthKbps in 1..2000)
        val isLowData = _networkStatus.value.isLowDataModeEnabled

        val quality = when {
            isLowData || isSlowCellular -> ConnectionQuality.WEAK
            isWifi -> ConnectionQuality.EXCELLENT
            isCellular -> ConnectionQuality.MODERATE
            else -> ConnectionQuality.MODERATE
        }

        val typeLabel = when {
            isWifi -> "Wi-Fi (Haut Débit)"
            isSlowCellular -> "Cellulaire Faible (2G/3G)"
            isCellular -> "Données Mobiles (4G/LTE)"
            else -> "Connexion Réseau"
        }

        _networkStatus.value = _networkStatus.value.copy(
            isConnected = true,
            isWeak = isSlowCellular || isLowData,
            quality = quality,
            connectionType = typeLabel,
            latencyMs = if (quality == ConnectionQuality.WEAK) 380L else 45L
        )
    }

    /**
     * Toggles low-data / weak-network saver mode.
     * When enabled, video streams default to 480p/360p and image prefetching is reduced.
     */
    fun toggleLowDataMode(enabled: Boolean) {
        val current = _networkStatus.value
        _networkStatus.value = current.copy(
            isLowDataModeEnabled = enabled,
            isWeak = enabled || current.quality == ConnectionQuality.WEAK,
            quality = if (enabled) ConnectionQuality.WEAK else (if (current.isConnected) ConnectionQuality.MODERATE else ConnectionQuality.OFFLINE)
        )
    }

    /**
     * Testing tool to simulate weak internet, offline mode, or high-speed connection.
     */
    fun setSimulationMode(quality: ConnectionQuality?) {
        if (quality == null) {
            _networkStatus.value = _networkStatus.value.copy(isSimulatedMode = false)
            val cm = connectivityManager
            updateFromCapabilities(cm?.getNetworkCapabilities(cm.activeNetwork))
            return
        }

        when (quality) {
            ConnectionQuality.OFFLINE -> {
                _networkStatus.value = NetworkStatus(
                    isConnected = false,
                    isWeak = false,
                    quality = ConnectionQuality.OFFLINE,
                    connectionType = "Simulation : Hors-Ligne",
                    isLowDataModeEnabled = false,
                    isSimulatedMode = true,
                    latencyMs = 0L
                )
            }
            ConnectionQuality.WEAK -> {
                _networkStatus.value = NetworkStatus(
                    isConnected = true,
                    isWeak = true,
                    quality = ConnectionQuality.WEAK,
                    connectionType = "Simulation : Internet Faible (3G/Haiti)",
                    isLowDataModeEnabled = true,
                    isSimulatedMode = true,
                    latencyMs = 450L
                )
            }
            ConnectionQuality.MODERATE, ConnectionQuality.EXCELLENT -> {
                _networkStatus.value = NetworkStatus(
                    isConnected = true,
                    isWeak = false,
                    quality = ConnectionQuality.EXCELLENT,
                    connectionType = "Simulation : Haut Débit (4G/Wi-Fi)",
                    isLowDataModeEnabled = false,
                    isSimulatedMode = true,
                    latencyMs = 35L
                )
            }
        }
    }
}
