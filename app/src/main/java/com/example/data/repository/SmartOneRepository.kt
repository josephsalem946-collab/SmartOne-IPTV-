package com.example.data.repository

import com.example.data.model.*
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import java.text.SimpleDateFormat
import java.util.*

object SmartOneRepository {

    const val SUPPORT_EMAIL = "supportdisponible@gmail.com"
    const val ADMIN_EMAIL = "supportdisponible@gmail.com"
    const val USD_RATE = 132.0
    const val EUR_RATE = 143.0
    const val DAILY_FREE_SECONDS = 3600 // 1 hour per day

    val subscriptionPlans = listOf(
        SubscriptionPlan(id = "plan_1m", label = "1 Mois", durationMonths = 1, priceGds = 250.0),
        SubscriptionPlan(id = "plan_3m", label = "3 Mois", durationMonths = 3, priceGds = 750.0),
        SubscriptionPlan(id = "plan_6m", label = "6 Mois", durationMonths = 6, priceGds = 1500.0),
        SubscriptionPlan(id = "plan_1y", label = "1 An", durationMonths = 12, priceGds = 2500.0)
    )

    private val _paymentGateways = MutableStateFlow(listOf(
        PaymentGateway(
            id = "natcash",
            name = "NatCash - Online",
            status = "Disponible 24/7",
            minHtg = 250.0,
            maxHtg = 10000.0,
            type = "Mobile Money",
            merchantMasked = "+509XXXXX281",
            isBlocked = false
        ),
        PaymentGateway(
            id = "moncash",
            name = "MonCash - Online",
            status = "Actif",
            minHtg = 250.0,
            maxHtg = 10000.0,
            type = "Mobile Money",
            merchantMasked = "+509XXXXX695",
            isBlocked = false
        ),
        PaymentGateway(
            id = "crypto",
            name = "Crypto (USDT, BTC, ETH, TRX)",
            status = "NOWPayments",
            minHtg = 2000.0,
            maxHtg = 10000.0,
            type = "Crypto",
            merchantMasked = "Multi-Coin",
            isBlocked = false
        ),
        PaymentGateway(
            id = "google_play_stripe",
            name = "Google Play + Stripe (Cartes)",
            status = "Bloqué par l'Admin",
            minHtg = 1000.0,
            maxHtg = 10000.0,
            type = "Card / In-App",
            merchantMasked = "In-App Billing",
            isBlocked = true // Bloqué par défaut selon la demande
        )
    ))
    val paymentGateways: StateFlow<List<PaymentGateway>> = _paymentGateways.asStateFlow()

    val categories = listOf(
        "Toutes",
        "Top News",
        "News",
        "Sports",
        "Movies",
        "Series",
        "Music",
        "Kids",
        "Animation",
        "Documentary",
        "Science",
        "Technology",
        "Gaming",
        "Culture",
        "Entertainment",
        "Family",
        "General",
        "Cooking",
        "Lifestyle",
        "Auto",
        "Business",
        "Classic",
        "Comedy",
        "Education",
        "Legislative",
        "Outdoor",
        "Relax",
        "Religious",
        "Shop",
        "Travel",
        "Weather"
    )

    val languages = listOf(
        "Français",
        "Créole Haïtien",
        "English",
        "Español",
        "Deutsch",
        "Italiano",
        "Português",
        "Nederlands",
        "العربية (Arabe)",
        "Русский (Russe)",
        "Türkçe",
        "Polski",
        "Română",
        "Українська",
        "Hindi",
        "Bengali",
        "Bahasa Indonesia",
        "Tiếng Việt",
        "日本語 (Japonais)",
        "한국어 (Coréen)",
        "中文 (Mandarin)"
    )

    private val allChannelsList = listOf(
        Channel("c1", "SmartOne HD 1", "General", "Global", "Le Grand Journal du Monde", "4K UHD 60fps", "12 Mbps", "🌐"),
        Channel("c2", "France 24 HD", "News", "France", "Journal International & Débats", "1080p 60fps", "6.5 Mbps", "🇫🇷"),
        Channel("c3", "Canal+ Sport", "Sports", "France", "UEFA Champions League Live", "1080p 60fps", "8.0 Mbps", "⚽"),
        Channel("c4", "Tele Ginen Live", "News", "Haïti", "Ginen Nouvèl & Analiz", "1080p 30fps", "4.8 Mbps", "🇭🇹"),
        Channel("c5", "CNN International", "Top News", "USA", "The Situation Room with Wolf Blitzer", "1080p 60fps", "6.0 Mbps", "🇺🇸"),
        Channel("c6", "Eurosport 1 HD", "Sports", "Europe", "Grand Slam Tennis Championship", "1080p 60fps", "7.5 Mbps", "🎾"),
        Channel("c7", "HBO Cinemax", "Movies", "USA", "Dune: Deuxième Partie (Film 2024)", "4K HDR", "14 Mbps", "🍿"),
        Channel("c8", "National Geographic", "Documentary", "USA", "Secrets of the Blue Planet", "1080p 60fps", "7.0 Mbps", "🌍"),
        Channel("c9", "MTV Live Hits", "Music", "Global", "Top 40 World Charts Countdown", "1080p 60fps", "6.0 Mbps", "🎵"),
        Channel("c10", "Cartoon Network", "Kids", "USA", "Adventure Time & Gumball", "1080p 60fps", "5.5 Mbps", "🧸"),
        Channel("c11", "Bloomberg TV", "Business", "Global", "Surveillance Markets & Forex", "1080p 60fps", "5.8 Mbps", "📈"),
        Channel("c12", "Discovery Science", "Science", "USA", "How The Universe Works", "1080p 60fps", "6.2 Mbps", "🔭"),
        Channel("c13", "Tech Zone 24", "Technology", "Global", "AI Innovations & Robotics", "1080p 60fps", "6.0 Mbps", "🤖"),
        Channel("c14", "IGN Gaming TV", "Gaming", "Global", "Pro Esports Live Finals", "1080p 60fps", "8.5 Mbps", "🎮"),
        Channel("c15", "Disney Channel", "Animation", "Global", "Miraculous: Les Aventures de Ladybug", "1080p 60fps", "5.5 Mbps", "✨"),
        Channel("c16", "Top Gear Channel", "Auto", "UK", "Supercars Extreme Speed Review", "1080p 60fps", "7.0 Mbps", "🏎️"),
        Channel("c17", "Cuisine & Saveurs", "Cooking", "France", "Les Secrets de la Haute Gastronomie", "1080p 60fps", "5.2 Mbps", "🍳"),
        Channel("c18", "Comedy Central", "Comedy", "USA", "The Daily Show & Stand-Up Gala", "1080p 60fps", "5.0 Mbps", "😂"),
        Channel("c19", "RTBF La Une", "General", "Belgique", "Soir Première & Magazine", "1080p 60fps", "6.0 Mbps", "🇧🇪"),
        Channel("c20", "RTS Un", "Culture", "Suisse", "Temps Présent & Grands Reportages", "1080p 60fps", "6.2 Mbps", "🇨🇭"),
        Channel("c21", "Météo 24/7", "Weather", "Global", "Prévisions Radar Caraïbes & Monde", "1080p 30fps", "4.5 Mbps", "☀️"),
        Channel("c22", "Tele Métropole", "News", "Haïti", "Le Point avec Wendell Théodore", "1080p 30fps", "4.8 Mbps", "🇭🇹"),
        Channel("c23", "beIN SPORTS 1", "Sports", "France", "Ligue des Champions & Liga Española", "1080p 60fps", "8.5 Mbps", "🏆"),
        Channel("c24", "Cinema Classic", "Classic", "Global", "Les Chefs-d'œuvre du 7e Art", "1080p 30fps", "5.0 Mbps", "📽️"),
        Channel("c25", "Trace Urban", "Music", "France", "Urban Afro-Caribbean & Hip-Hop", "1080p 60fps", "6.2 Mbps", "🎧"),
        Channel("c26", "Voyage & Nature", "Travel", "Global", "Exploration des Îles des Caraïbes", "4K UHD", "11 Mbps", "🏝️"),
        Channel("c27", "Relax Nature Spa", "Relax", "Global", "Sons d'Océan & Cascade 4K", "4K UHD", "10 Mbps", "🧘"),
        Channel("c28", "Histoire TV", "Education", "France", "Civilisations Antiques & Mystères", "1080p 60fps", "5.8 Mbps", "🏛️")
    )

    // Observable states
    private val _channels = MutableStateFlow(allChannelsList)
    val channels: StateFlow<List<Channel>> = _channels.asStateFlow()

    private val _currentPlaying = MutableStateFlow<Channel?>(allChannelsList[0])
    val currentPlaying: StateFlow<Channel?> = _currentPlaying.asStateFlow()

    private val _isPlaying = MutableStateFlow(true)
    val isPlaying: StateFlow<Boolean> = _isPlaying.asStateFlow()

    private val _currentUser = MutableStateFlow<UserAccount?>(null)
    val currentUser: StateFlow<UserAccount?> = _currentUser.asStateFlow()

    private val _balance = MutableStateFlow(1250.0) // initial demo balance in HTG
    val balance: StateFlow<Double> = _balance.asStateFlow()

    private val _activeSubscription = MutableStateFlow<ActiveSubscription?>(null)
    val activeSubscription: StateFlow<ActiveSubscription?> = _activeSubscription.asStateFlow()

    const val MAX_CONCURRENT_DEVICES = 2
    const val CURRENT_DEVICE_ID = "dev_this_phone"

    private val _connectedDevices = MutableStateFlow(listOf(
        ConnectedDevice(
            deviceId = CURRENT_DEVICE_ID,
            deviceName = "Cet Appareil (Smartphone Android)",
            deviceType = "MOBILE",
            ipAddress = "190.115.18.42 (Haïti)",
            connectedAt = "Aujourd'hui à 11:20",
            isCurrentDevice = true
        ),
        ConnectedDevice(
            deviceId = "dev_smart_tv",
            deviceName = "Smart TV Salon (Samsung Tizen)",
            deviceType = "TV",
            ipAddress = "190.115.18.42 (Haïti)",
            connectedAt = "Hier à 20:15",
            isCurrentDevice = false
        )
    ))
    val connectedDevices: StateFlow<List<ConnectedDevice>> = _connectedDevices.asStateFlow()

    private val _deviceLimitExceeded = MutableStateFlow(false)
    val deviceLimitExceeded: StateFlow<Boolean> = _deviceLimitExceeded.asStateFlow()

    private val _favorites = MutableStateFlow<Set<String>>(setOf("c1", "c3", "c4"))
    val favorites: StateFlow<Set<String>> = _favorites.asStateFlow()

    private val _freeTimeLeftSeconds = MutableStateFlow(3600) // 60 min countdown
    val freeTimeLeftSeconds: StateFlow<Int> = _freeTimeLeftSeconds.asStateFlow()

    private val _transactions = MutableStateFlow<List<TransactionRecord>>(listOf(
        TransactionRecord(
            transactionId = "TX-89211",
            userId = "usr_initial",
            description = "Dépôt initial",
            amount = 1250.0,
            currency = "HTG",
            paymentMethod = "NatCash - Online",
            status = "SUCCESS",
            createdAt = "17 Oct 2026 08:30"
        )
    ))
    val transactions: StateFlow<List<TransactionRecord>> = _transactions.asStateFlow()

    private val _adminClients = MutableStateFlow(listOf(
        AdminClient("u1", "Marie Claire", "marie.c@gmail.com", "+509 3123 4567", "plan_1y", "1 An", "2027-10-17", isOnline = true, isSuspended = false, canPayForOthers = true),
        AdminClient("u2", "Jean Paul", "jean.p@gmail.com", "+509 4234 5678", "plan_3m", "3 Mois", "2027-01-15", isOnline = true, isSuspended = false, canPayForOthers = true),
        AdminClient("u3", "Nadia Baptiste", "nadia.b@gmail.com", "+509 3456 7890", "plan_1m", "1 Mois", "2026-11-17", isOnline = false, isSuspended = false, canPayForOthers = false),
        AdminClient("u4", "Luc Derival", "luc.d@gmail.com", "+509 4890 1234", "plan_6m", "6 Mois", "2027-04-17", isOnline = false, isSuspended = true, canPayForOthers = true)
    ))
    val adminClients: StateFlow<List<AdminClient>> = _adminClients.asStateFlow()

    private val _thirdPartySubscriptions = MutableStateFlow(listOf(
        ThirdPartySubscription(
            id = "tps_1",
            payerId = "u1",
            payerName = "Marie Claire",
            recipientIdentifier = "+509 3215 3281",
            planLabel = "3 Mois",
            amountHtg = 750.0,
            timestamp = "Hier à 16:30",
            status = "ACTIVE"
        ),
        ThirdPartySubscription(
            id = "tps_2",
            payerId = "u2",
            payerName = "Jean Paul",
            recipientIdentifier = "josephsalem946@gmail.com",
            planLabel = "1 An",
            amountHtg = 2500.0,
            timestamp = "Aujourd'hui à 09:15",
            status = "ACTIVE"
        )
    ))
    val thirdPartySubscriptions: StateFlow<List<ThirdPartySubscription>> = _thirdPartySubscriptions.asStateFlow()

    private val _dailyRevenues = MutableStateFlow(listOf(
        DailyRevenue("Lun", 2500.0),
        DailyRevenue("Mar", 4250.0),
        DailyRevenue("Mer", 1750.0),
        DailyRevenue("Jeu", 5000.0),
        DailyRevenue("Ven", 3750.0),
        DailyRevenue("Sam", 6500.0),
        DailyRevenue("Aujourd'hui", 4500.0)
    ))
    val dailyRevenues: StateFlow<List<DailyRevenue>> = _dailyRevenues.asStateFlow()

    private val _recentComments = MutableStateFlow(listOf(
        UserComment("c1", "Jean-Baptiste Moïse", "+509 3782-9011", 5, "Service impeccable ! MonCash validé en moins de 10 secondes et les chaînes sportives ne coupent jamais.", "Il y a 15 min", "Abonné 1 An"),
        UserComment("c2", "Daphnée Saint-Louis", "+509 4621-0045", 5, "Très bonne qualité 1080p 60fps sur ma smart TV. Natcash fonctionne parfaitement.", "Il y a 45 min", "Abonné 6 Mois"),
        UserComment("c3", "Fritzner Pierre", "+509 3109-8874", 4, "L'interface est super rapide, les tirages et chaînes sont à l'heure. Très satisfait.", "Il y a 2h", "Client Vérifié"),
        UserComment("c4", "Marie Yolande Casimir", "+509 4812-3390", 5, "Bravo à l'équipe de Joseph Salem ! Le support est très réactif et le paiement par carte passe direct.", "Il y a 3h", "Abonné 1 An"),
        UserComment("c5", "Rodolphe Étienne", "+509 3450-7122", 5, "Excellente sélection de chaînes françaises et américaines. Très fluide.", "Il y a 5h", "Abonné 3 Mois"),
        UserComment("c6", "Ketsia Paul", "+509 4118-2900", 4, "Recharge de solde instantanée. Le lecteur vidéo est stable et sans pub.", "Il y a 8h", "Client Vérifié"),
        UserComment("c7", "Claudy Dorce", "+509 3899-1234", 5, "Application au top, surtout les 4 boutons de paiement bien clairs et faciles.", "Il y a 12h", "Abonné 1 Mois"),
        UserComment("c8", "Sonia Chery", "+509 4301-9988", 5, "J'adore la fluidité des flux HD. Aucun lag pendant les matchs en direct.", "Hier 21:40", "Abonné 1 An"),
        UserComment("c9", "Marc-Antoine Jeudy", "+509 3662-4411", 4, "Bon rapport qualité/prix en gourdes haïtiennes. Continuez ainsi !", "Hier 18:15", "Abonné 3 Mois"),
        UserComment("c10", "Wadson Thermidor", "+509 4700-5566", 5, "Le meilleur service IPTV en Haïti. 10/10 pour la qualité et le son surround.", "Hier 14:02", "Client VIP")
    ))
    val recentComments: StateFlow<List<UserComment>> = _recentComments.asStateFlow()

    private val _selectedLanguage = MutableStateFlow("Français")
    val selectedLanguage: StateFlow<String> = _selectedLanguage.asStateFlow()

    // Actions
    fun playChannel(channel: Channel) {
        _currentPlaying.value = channel
        _isPlaying.value = true
    }

    fun togglePlayPause() {
        _isPlaying.value = !_isPlaying.value
    }

    fun stopPlayback() {
        _isPlaying.value = false
        _currentPlaying.value = null
    }

    fun playRandomChannel() {
        val rand = allChannelsList.random()
        playChannel(rand)
    }

    fun toggleFavorite(channelId: String) {
        val current = _favorites.value.toMutableSet()
        if (current.contains(channelId)) {
            current.remove(channelId)
        } else {
            current.add(channelId)
        }
        _favorites.value = current
    }

    fun decrementFreeTime(): Boolean {
        // Only decrement if user has NO active subscription and is currently playing
        if (_activeSubscription.value == null && _isPlaying.value && _currentPlaying.value != null) {
            val current = _freeTimeLeftSeconds.value
            if (current > 0) {
                _freeTimeLeftSeconds.value = current - 1
                return true
            } else {
                _isPlaying.value = false
                return false
            }
        }
        return true
    }

    fun login(name: String, emailOrPhone: String, isGoogle: Boolean = false): UserAccount {
        val isAdm = emailOrPhone.equals(ADMIN_EMAIL, ignoreCase = true) ||
                emailOrPhone.contains("admin", ignoreCase = true) ||
                emailOrPhone.equals("supportdisponible@gmail.com", ignoreCase = true) ||
                emailOrPhone.startsWith("admin", ignoreCase = true)
        val maskedEmail = if (emailOrPhone.contains("@")) {
            if (isAdm) SUPPORT_EMAIL else emailOrPhone
        } else ""
        val user = UserAccount(
            userId = if (isGoogle) "firebase_google_${System.currentTimeMillis()}" else "usr_${System.currentTimeMillis()}",
            name = name.ifBlank { if (isAdm) "Administrateur (SmartOne)" else "Client" },
            email = maskedEmail,
            phone = if (!emailOrPhone.contains("@")) emailOrPhone else "",
            role = if (isAdm) "admin" else "client",
            status = "active",
            isOnline = true,
            isVisitor = false,
            isVerified = true
        )
        _currentUser.value = user
        return user
    }

    fun logout() {
        _currentUser.value = null
    }

    fun depositFunds(amountHtg: Double, gatewayName: String): Boolean {
        if (amountHtg <= 0) return false
        _balance.value += amountHtg
        val dateFormat = SimpleDateFormat("dd MMM yyyy HH:mm", Locale.FRENCH)
        val newTx = TransactionRecord(
            transactionId = "TX-${(10000..99999).random()}",
            userId = _currentUser.value?.userId ?: "guest",
            description = "Dépôt validé",
            amount = amountHtg,
            currency = "HTG",
            paymentMethod = gatewayName,
            status = "SUCCESS",
            createdAt = dateFormat.format(Date())
        )
        _transactions.value = listOf(newTx) + _transactions.value
        return true
    }

    fun activateSubscription(plan: SubscriptionPlan): Boolean {
        val currentBal = _balance.value
        if (currentBal < plan.priceGds) {
            return false
        }
        // Deduct balance
        _balance.value = currentBal - plan.priceGds

        // Auto-cancel previous plan and create new subscription
        val now = System.currentTimeMillis()
        val expiry = now + (plan.durationMonths * 30L * 24L * 60L * 60L * 1000L)
        val newSub = ActiveSubscription(
            subscriptionId = "SUB-${(1000..9999).random()}",
            userId = _currentUser.value?.userId ?: "usr_current",
            planId = plan.id,
            label = plan.label,
            startedAt = now,
            expiresAt = expiry
        )
        _activeSubscription.value = newSub

        val dateFormat = SimpleDateFormat("dd MMM yyyy HH:mm", Locale.FRENCH)
        val newTx = TransactionRecord(
            transactionId = "SUB-TX-${(10000..99999).random()}",
            userId = _currentUser.value?.userId ?: "guest",
            planId = plan.id,
            description = "Abonnement ${plan.label} (auto-annulation du précédent)",
            amount = plan.priceGds,
            currency = "HTG",
            paymentMethod = "Portefeuille SmartOne",
            status = "SUCCESS",
            createdAt = dateFormat.format(Date())
        )
        _transactions.value = listOf(newTx) + _transactions.value
        return true
    }

    fun toggleClientSuspension(clientId: String) {
        _adminClients.value = _adminClients.value.map { client ->
            if (client.id == clientId) {
                client.copy(isSuspended = !client.isSuspended)
            } else client
        }
    }

    fun updateDailyRevenue(dayLabel: String, newAmount: Double) {
        _dailyRevenues.value = _dailyRevenues.value.map {
            if (it.dayLabel.equals(dayLabel, ignoreCase = true)) {
                it.copy(amount = newAmount.coerceAtLeast(0.0))
            } else it
        }
    }

    fun increaseDailyRevenue(dayLabel: String = "Aujourd'hui", amountToAdd: Double) {
        _dailyRevenues.value = _dailyRevenues.value.map {
            if (it.dayLabel.equals(dayLabel, ignoreCase = true)) {
                it.copy(amount = (it.amount + amountToAdd).coerceAtLeast(0.0))
            } else it
        }
    }

    fun addComment(author: String, comment: String, rating: Int = 5) {
        val newC = UserComment(
            id = "c_${System.currentTimeMillis()}",
            author = author.ifBlank { "Client SmartOne" },
            phone = "+509 3${(100..999).random()}-${(1000..9999).random()}",
            rating = rating.coerceIn(1, 5),
            content = comment,
            date = "À l'instant",
            badge = "Nouveau Commentaire"
        )
        _recentComments.value = listOf(newC) + _recentComments.value
    }

    fun deleteComment(commentId: String) {
        _recentComments.value = _recentComments.value.filter { it.id != commentId }
    }

    fun canConnectNewDevice(): Boolean {
        return _connectedDevices.value.size < MAX_CONCURRENT_DEVICES
    }

    fun connectNewDevice(name: String, type: String = "TV"): Boolean {
        if (_connectedDevices.value.size >= MAX_CONCURRENT_DEVICES) {
            _deviceLimitExceeded.value = true
            return false
        }
        val newDev = ConnectedDevice(
            deviceId = "dev_${System.currentTimeMillis()}",
            deviceName = name,
            deviceType = type,
            ipAddress = "190.115.18.${(10..99).random()} (Haïti)",
            connectedAt = "À l'instant",
            isCurrentDevice = false
        )
        _connectedDevices.value = _connectedDevices.value + newDev
        _deviceLimitExceeded.value = false
        return true
    }

    fun disconnectDevice(deviceId: String) {
        _connectedDevices.value = _connectedDevices.value.filter { it.deviceId != deviceId }
        _deviceLimitExceeded.value = false
    }

    fun dismissDeviceLimitError() {
        _deviceLimitExceeded.value = false
    }

    fun resetConnectedDevicesForClient(clientId: String) {
        _connectedDevices.value = _connectedDevices.value.filter { it.isCurrentDevice }
        _deviceLimitExceeded.value = false
    }

    fun togglePaymentGatewayStatus(gatewayId: String) {
        _paymentGateways.value = _paymentGateways.value.map { gw ->
            if (gw.id == gatewayId) {
                val newBlocked = !gw.isBlocked
                gw.copy(
                    isBlocked = newBlocked,
                    status = if (newBlocked) "Bloqué par l'Admin" else "Disponible 24/7"
                )
            } else gw
        }
    }

    fun isGatewayBlocked(gatewayId: String): Boolean {
        return _paymentGateways.value.find { it.id == gatewayId }?.isBlocked ?: false
    }

    fun toggleClientPayForOthers(clientId: String) {
        _adminClients.value = _adminClients.value.map { cl ->
            if (cl.id == clientId) {
                cl.copy(canPayForOthers = !cl.canPayForOthers)
            } else cl
        }
    }

    fun paySubscriptionForThirdParty(
        recipientIdentifier: String,
        plan: SubscriptionPlan,
        payerName: String = "Client SmartOne",
        deductFromBalance: Boolean = true
    ): Boolean {
        if (recipientIdentifier.isBlank()) return false
        val cost = plan.priceGds

        if (deductFromBalance) {
            if (_balance.value < cost) return false
            _balance.value = _balance.value - cost
        }

        val dateFormat = SimpleDateFormat("dd MMM yyyy HH:mm", Locale.FRENCH)
        val nowStr = dateFormat.format(Date())

        val record = ThirdPartySubscription(
            id = "tps_${System.currentTimeMillis()}",
            payerId = _currentUser.value?.userId ?: "admin_direct",
            payerName = payerName,
            recipientIdentifier = recipientIdentifier.trim(),
            planLabel = plan.label,
            amountHtg = cost,
            timestamp = nowStr,
            status = "ACTIVE"
        )
        _thirdPartySubscriptions.value = listOf(record) + _thirdPartySubscriptions.value

        val newTx = TransactionRecord(
            transactionId = "TIERS-TX-${(10000..99999).random()}",
            userId = _currentUser.value?.userId ?: "admin",
            planId = plan.id,
            description = "Abonnement ${plan.label} payé pour tiers ($recipientIdentifier)",
            amount = cost,
            currency = "HTG",
            paymentMethod = if (deductFromBalance) "Portefeuille SmartOne" else "Paiement Direct Admin",
            status = "SUCCESS",
            createdAt = nowStr
        )
        _transactions.value = listOf(newTx) + _transactions.value

        // Check if recipient matches one in admin clients, if so update their plan & expiry
        _adminClients.value = _adminClients.value.map { client ->
            val cleanRecipient = recipientIdentifier.trim().lowercase().replace(" ", "")
            val cleanEmail = client.email.trim().lowercase().replace(" ", "")
            val cleanPhone = client.phone.trim().lowercase().replace(" ", "")
            if (cleanRecipient == cleanEmail || cleanRecipient == cleanPhone || cleanPhone.contains(cleanRecipient)) {
                client.copy(
                    planId = plan.id,
                    planLabel = plan.label,
                    expiresAt = "2027-10-22"
                )
            } else client
        }

        return true
    }

    fun setLanguage(lang: String) {
        _selectedLanguage.value = lang
    }
}
