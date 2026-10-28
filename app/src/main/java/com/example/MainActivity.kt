package com.example

import android.os.Bundle
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.compose.BackHandler
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.*
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutVertically
import androidx.compose.ui.input.pointer.PointerEventPass
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.data.model.Channel
import com.example.data.network.ConnectionQuality
import com.example.data.network.NetworkStatusMonitor
import com.example.data.repository.OfflineCacheManager
import com.example.data.repository.SmartOneRepository
import com.example.ui.components.*
import com.example.ui.dialogs.*
import com.example.ui.screens.*
import com.example.ui.theme.*
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import java.util.Locale

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            MyApplicationTheme {
                SmartOneMainApp()
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SmartOneMainApp() {
    val context = LocalContext.current
    val coroutineScope = rememberCoroutineScope()
    val drawerState = rememberDrawerState(initialValue = DrawerValue.Closed)

    // Repository states
    val channels by SmartOneRepository.channels.collectAsStateWithLifecycle()
    val currentPlaying by SmartOneRepository.currentPlaying.collectAsStateWithLifecycle()
    val isPlaying by SmartOneRepository.isPlaying.collectAsStateWithLifecycle()
    val currentUser by SmartOneRepository.currentUser.collectAsStateWithLifecycle()
    val balance by SmartOneRepository.balance.collectAsStateWithLifecycle()
    val activeSubscription by SmartOneRepository.activeSubscription.collectAsStateWithLifecycle()
    val favorites by SmartOneRepository.favorites.collectAsStateWithLifecycle()
    val freeSecondsLeft by SmartOneRepository.freeTimeLeftSeconds.collectAsStateWithLifecycle()
    val transactions by SmartOneRepository.transactions.collectAsStateWithLifecycle()
    val adminClients by SmartOneRepository.adminClients.collectAsStateWithLifecycle()
    val dailyRevenues by SmartOneRepository.dailyRevenues.collectAsStateWithLifecycle()
    val recentComments by SmartOneRepository.recentComments.collectAsStateWithLifecycle()
    val connectedDevices by SmartOneRepository.connectedDevices.collectAsStateWithLifecycle()
    val deviceLimitExceeded by SmartOneRepository.deviceLimitExceeded.collectAsStateWithLifecycle()
    val selectedLanguage by SmartOneRepository.selectedLanguage.collectAsStateWithLifecycle()
    val paymentGateways by SmartOneRepository.paymentGateways.collectAsStateWithLifecycle()
    val thirdPartySubscriptions by SmartOneRepository.thirdPartySubscriptions.collectAsStateWithLifecycle()
    val networkStatus by NetworkStatusMonitor.networkStatus.collectAsStateWithLifecycle()

    // Initialize Network and Offline Cache
    LaunchedEffect(Unit) {
        NetworkStatusMonitor.initialize(context)
        OfflineCacheManager.initialize(context)
    }

    val canCurrentUserPayForOthers = remember(adminClients, currentUser) {
        adminClients.find { 
            (currentUser?.email?.isNotBlank() == true && it.email.equals(currentUser?.email, true)) ||
            (currentUser?.phone?.isNotBlank() == true && it.phone.equals(currentUser?.phone, true))
        }?.canPayForOthers ?: true
    }

    // UI screen & dialog navigation states
    var currentScreen by remember { mutableStateOf("home") }
    var selectedCategory by remember { mutableStateOf("Toutes") }
    var searchQuery by remember { mutableStateOf("") }

    var showAuthDialog by remember { mutableStateOf(false) }
    var showProfileDialog by remember { mutableStateOf(false) }
    var showSearchDialog by remember { mutableStateOf(false) }
    var showLanguageDialog by remember { mutableStateOf(false) }
    var showAboutDialog by remember { mutableStateOf(false) }
    var showFaqDialog by remember { mutableStateOf(false) }
    var showPrivacyDialog by remember { mutableStateOf(false) }
    var showFeedbackDialog by remember { mutableStateOf(false) }
    var showFreeQuotaExpiredDialog by remember { mutableStateOf(false) }
    var showDownloadDialog by remember { mutableStateOf(false) }

    // Bottom Navigation Bar Inactivity Auto-hide (1 minute = 60 seconds)
    var isBottomBarVisible by remember { mutableStateOf(true) }
    var lastInteractionTime by remember { mutableLongStateOf(System.currentTimeMillis()) }

    // Inactivity countdown: hides bottom bar after 1 minute of no user action
    LaunchedEffect(lastInteractionTime) {
        delay(60_000L)
        isBottomBarVisible = false
    }

    // 1-hour free daily countdown timer loop
    LaunchedEffect(isPlaying, activeSubscription, currentPlaying) {
        while (true) {
            delay(1000L)
            if (activeSubscription == null && isPlaying && currentPlaying != null) {
                val hasRemaining = SmartOneRepository.decrementFreeTime()
                if (!hasRemaining) {
                    showFreeQuotaExpiredDialog = true
                }
            }
        }
    }

    // Back button handling: return to home screen if on sub-screen
    BackHandler(enabled = currentScreen != "home") {
        currentScreen = "home"
    }

    ModalNavigationDrawer(
        drawerState = drawerState,
        drawerContent = {
            AppDrawer(
                currentUser = currentUser,
                selectedNav = currentScreen,
                onNavigate = { route ->
                    currentScreen = route
                    coroutineScope.launch { drawerState.close() }
                },
                onSelectCategory = { cat ->
                    selectedCategory = cat
                    currentScreen = "home"
                    coroutineScope.launch { drawerState.close() }
                },
                onOpenProfile = {
                    coroutineScope.launch { drawerState.close() }
                    if (currentUser != null) showProfileDialog = true else showAuthDialog = true
                },
                onOpenLanguagePicker = {
                    coroutineScope.launch { drawerState.close() }
                    showLanguageDialog = true
                },
                onOpenAdmin = {
                    currentScreen = "admin"
                    coroutineScope.launch { drawerState.close() }
                },
                onOpenAbout = {
                    coroutineScope.launch { drawerState.close() }
                    showAboutDialog = true
                },
                onOpenDownloadApp = {
                    coroutineScope.launch { drawerState.close() }
                    showDownloadDialog = true
                },
                onOpenFaq = {
                    coroutineScope.launch { drawerState.close() }
                    showFaqDialog = true
                },
                onOpenPrivacy = {
                    coroutineScope.launch { drawerState.close() }
                    showPrivacyDialog = true
                },
                onOpenFeedback = {
                    coroutineScope.launch { drawerState.close() }
                    showFeedbackDialog = true
                }
            )
        }
    ) {
        Scaffold(
            modifier = Modifier
                .fillMaxSize()
                .pointerInput(Unit) {
                    awaitPointerEventScope {
                        while (true) {
                            val event = awaitPointerEvent(PointerEventPass.Initial)
                            if (event.changes.isNotEmpty()) {
                                lastInteractionTime = System.currentTimeMillis()
                                if (!isBottomBarVisible) {
                                    isBottomBarVisible = true
                                }
                            }
                        }
                    }
                },
            topBar = {
                TopAppBar(
                    title = {
                        SmartOneBrandLogo(size = SmartOneLogoSize.HEADER_COMPACT)
                    },
                    navigationIcon = {
                        IconButton(
                            onClick = {
                                lastInteractionTime = System.currentTimeMillis()
                                isBottomBarVisible = true
                                coroutineScope.launch { drawerState.open() }
                            },
                            modifier = Modifier.testTag("app_menu_button")
                        ) {
                            Icon(
                                imageVector = Icons.Default.Menu,
                                contentDescription = "Menu Principal",
                                tint = DarkTextPrimary
                            )
                        }
                    },
                    actions = {
                        // Random Channel action (🎲)
                        IconButton(
                            onClick = {
                                lastInteractionTime = System.currentTimeMillis()
                                isBottomBarVisible = true
                                SmartOneRepository.playRandomChannel()
                                currentScreen = "home"
                                Toast.makeText(context, "🎲 Chaîne aléatoire lancée !", Toast.LENGTH_SHORT).show()
                            },
                            modifier = Modifier.testTag("random_channel_button")
                        ) {
                            Icon(
                                imageVector = Icons.Default.Casino,
                                contentDescription = "Chaîne Aléatoire",
                                tint = BrandGold
                            )
                        }

                        // Search action (🔍)
                        IconButton(
                            onClick = {
                                lastInteractionTime = System.currentTimeMillis()
                                isBottomBarVisible = true
                                showSearchDialog = true
                            },
                            modifier = Modifier.testTag("app_search_button")
                        ) {
                            Icon(
                                imageVector = Icons.Default.Search,
                                contentDescription = "Rechercher",
                                tint = DarkTextPrimary
                            )
                        }

                        // Profile / User action (👤)
                        IconButton(
                            onClick = {
                                lastInteractionTime = System.currentTimeMillis()
                                isBottomBarVisible = true
                                if (currentUser != null) {
                                    showProfileDialog = true
                                } else {
                                    showAuthDialog = true
                                }
                            },
                            modifier = Modifier.testTag("app_user_button")
                        ) {
                            Icon(
                                imageVector = if (currentUser != null) Icons.Default.AccountCircle else Icons.Default.PersonOutline,
                                contentDescription = "Compte",
                                tint = if (currentUser != null) BrandCyan else DarkTextPrimary
                            )
                        }
                    },
                    colors = TopAppBarDefaults.topAppBarColors(
                        containerColor = DarkSurface,
                        titleContentColor = DarkTextPrimary
                    )
                )
            },
            bottomBar = {
                AnimatedVisibility(
                    visible = isBottomBarVisible,
                    enter = slideInVertically(initialOffsetY = { it }) + fadeIn(),
                    exit = slideOutVertically(targetOffsetY = { it }) + fadeOut()
                ) {
                    NavigationBar(
                        containerColor = DarkSurface,
                        contentColor = DarkTextPrimary
                    ) {
                    NavigationBarItem(
                        selected = currentScreen == "home",
                        onClick = { currentScreen = "home" },
                        icon = { Icon(Icons.Default.Tv, contentDescription = "Accueil") },
                        label = { Text("Chaînes", fontSize = 11.sp) },
                        colors = NavigationBarItemDefaults.colors(
                            selectedIconColor = BrandCyan,
                            selectedTextColor = BrandCyan,
                            indicatorColor = BrandBluePrimary.copy(alpha = 0.2f),
                            unselectedIconColor = DarkTextMuted,
                            unselectedTextColor = DarkTextMuted
                        )
                    )

                    NavigationBarItem(
                        selected = currentScreen == "plans",
                        onClick = { currentScreen = "plans" },
                        icon = { Icon(Icons.Default.CreditCard, contentDescription = "Abonnements") },
                        label = { Text("Forfaits", fontSize = 11.sp) },
                        colors = NavigationBarItemDefaults.colors(
                            selectedIconColor = BrandCyan,
                            selectedTextColor = BrandCyan,
                            indicatorColor = BrandBluePrimary.copy(alpha = 0.2f),
                            unselectedIconColor = DarkTextMuted,
                            unselectedTextColor = DarkTextMuted
                        )
                    )

                    NavigationBarItem(
                        selected = currentScreen == "deposit",
                        onClick = { currentScreen = "deposit" },
                        icon = { Icon(Icons.Default.AccountBalanceWallet, contentDescription = "Dépôt") },
                        label = { Text("Dépôt", fontSize = 11.sp) },
                        colors = NavigationBarItemDefaults.colors(
                            selectedIconColor = BrandCyan,
                            selectedTextColor = BrandCyan,
                            indicatorColor = BrandBluePrimary.copy(alpha = 0.2f),
                            unselectedIconColor = DarkTextMuted,
                            unselectedTextColor = DarkTextMuted
                        )
                    )

                    NavigationBarItem(
                        selected = currentScreen == "fav",
                        onClick = { currentScreen = "fav" },
                        icon = { Icon(Icons.Default.Star, contentDescription = "Favoris") },
                        label = { Text("Favoris", fontSize = 11.sp) },
                        colors = NavigationBarItemDefaults.colors(
                            selectedIconColor = BrandCyan,
                            selectedTextColor = BrandCyan,
                            indicatorColor = BrandBluePrimary.copy(alpha = 0.2f),
                            unselectedIconColor = DarkTextMuted,
                            unselectedTextColor = DarkTextMuted
                        )
                    )

                    NavigationBarItem(
                        selected = currentScreen == "history",
                        onClick = { currentScreen = "history" },
                        icon = { Icon(Icons.AutoMirrored.Filled.ReceiptLong, contentDescription = "Historique") },
                        label = { Text("Historique", fontSize = 11.sp) },
                        colors = NavigationBarItemDefaults.colors(
                            selectedIconColor = BrandCyan,
                            selectedTextColor = BrandCyan,
                            indicatorColor = BrandBluePrimary.copy(alpha = 0.2f),
                            unselectedIconColor = DarkTextMuted,
                            unselectedTextColor = DarkTextMuted
                        )
                    )
                }
            }
        },
        containerColor = DarkBackground
        ) { paddingValues ->
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(paddingValues)
            ) {
                // 1. Offline Mode Banner
                AnimatedVisibility(visible = !networkStatus.isConnected) {
                    Surface(
                        modifier = Modifier.fillMaxWidth(),
                        color = StatusRed.copy(alpha = 0.95f)
                    ) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(horizontal = 14.dp, vertical = 7.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.weight(1f)) {
                                Icon(Icons.Default.WifiOff, contentDescription = null, tint = Color.White, modifier = Modifier.size(17.dp))
                                Spacer(modifier = Modifier.width(8.dp))
                                Text(
                                    text = "Mode Hors-Ligne • Données & chaînes en cache local accessibles",
                                    color = Color.White,
                                    fontSize = 12.sp,
                                    fontWeight = FontWeight.Medium
                                )
                            }
                            TextButton(
                                onClick = {
                                    NetworkStatusMonitor.initialize(context)
                                    Toast.makeText(context, "Tentative de reconnexion...", Toast.LENGTH_SHORT).show()
                                },
                                contentPadding = PaddingValues(horizontal = 6.dp, vertical = 2.dp)
                            ) {
                                Text("Réessayer", color = Color.White, fontWeight = FontWeight.Bold, fontSize = 11.sp)
                            }
                        }
                    }
                }

                // 2. Weak Internet / Low Bandwidth Banner
                AnimatedVisibility(visible = networkStatus.isConnected && networkStatus.isWeak) {
                    Surface(
                        modifier = Modifier.fillMaxWidth(),
                        color = Color(0xFFB45309) // Deep Warm Amber
                    ) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(horizontal = 14.dp, vertical = 6.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.weight(1f)) {
                                Icon(Icons.Default.Speed, contentDescription = null, tint = Color.White, modifier = Modifier.size(16.dp))
                                Spacer(modifier = Modifier.width(8.dp))
                                Text(
                                    text = "Connexion Faible • Mode Bas Débit actif (Streaming & requêtes optimisés)",
                                    color = Color.White,
                                    fontSize = 11.sp,
                                    fontWeight = FontWeight.Medium
                                )
                            }
                            Surface(
                                shape = RoundedCornerShape(4.dp),
                                color = Color(0x33000000)
                            ) {
                                Text(
                                    text = "FLUIDE",
                                    color = Color.White,
                                    fontSize = 9.sp,
                                    fontWeight = FontWeight.Bold,
                                    modifier = Modifier.padding(horizontal = 5.dp, vertical = 2.dp)
                                )
                            }
                        }
                    }
                }

                Box(modifier = Modifier.weight(1f).fillMaxWidth()) {
                    when (currentScreen) {
                    "home" -> HomeScreen(
                        channels = channels,
                        currentPlaying = currentPlaying,
                        isPlaying = isPlaying,
                        favorites = favorites,
                        activeSubscription = activeSubscription,
                        freeSecondsLeft = freeSecondsLeft,
                        selectedCategory = selectedCategory,
                        searchQuery = searchQuery,
                        onSelectChannel = { channel -> SmartOneRepository.playChannel(channel) },
                        onTogglePlayPause = { SmartOneRepository.togglePlayPause() },
                        onStop = { SmartOneRepository.stopPlayback() },
                        onToggleFavorite = { channelId -> SmartOneRepository.toggleFavorite(channelId) },
                        onSelectCategory = { cat -> selectedCategory = cat },
                        onClearSearch = { searchQuery = "" },
                        onOpenPlans = { currentScreen = "plans" }
                    )

                    "plans" -> PlansScreen(
                        balance = balance,
                        activeSubscription = activeSubscription,
                        freeSecondsLeft = freeSecondsLeft,
                        onActivatePlan = { plan ->
                            val success = SmartOneRepository.activateSubscription(plan)
                            if (success) {
                                Toast.makeText(context, "Abonnement ${plan.label} activé avec succès !", Toast.LENGTH_LONG).show()
                                currentScreen = "home"
                            } else {
                                Toast.makeText(context, "Solde insuffisant pour activer ce plan.", Toast.LENGTH_SHORT).show()
                            }
                        },
                        onNavigateToDeposit = { currentScreen = "deposit" },
                        canPayForOthers = canCurrentUserPayForOthers,
                        onPayForOther = { recipient, plan ->
                            val success = SmartOneRepository.paySubscriptionForThirdParty(
                                recipientIdentifier = recipient,
                                plan = plan,
                                payerName = currentUser?.name ?: "Client SmartOne",
                                deductFromBalance = true
                            )
                            if (success) {
                                Toast.makeText(context, "Abònman ${plan.label} aktive pou $recipient avèk siksè !", Toast.LENGTH_LONG).show()
                            } else {
                                Toast.makeText(context, "Solde ensifizan pou peye abònman sa a.", Toast.LENGTH_SHORT).show()
                            }
                            success
                        }
                    )

                    "deposit" -> DepositScreen(
                        balance = balance,
                        onDepositSuccess = { amount, gw ->
                            Toast.makeText(context, "+$amount HTG ajoutés à votre portefeuille !", Toast.LENGTH_SHORT).show()
                        },
                        onNavigateToHistory = { currentScreen = "history" }
                    )

                    "history" -> HistoryScreen(
                        transactions = transactions
                    )

                    "fav" -> FavoritesScreen(
                        channels = channels,
                        favorites = favorites,
                        currentPlaying = currentPlaying,
                        isPlaying = isPlaying,
                        onSelectChannel = { channel ->
                            SmartOneRepository.playChannel(channel)
                            currentScreen = "home"
                        },
                        onToggleFavorite = { channelId -> SmartOneRepository.toggleFavorite(channelId) }
                    )

                    "admin" -> AdminDashboardScreen(
                        adminClients = adminClients,
                        dailyRevenues = dailyRevenues,
                        recentComments = recentComments,
                        paymentGateways = paymentGateways,
                        thirdPartySubscriptions = thirdPartySubscriptions,
                        onUpdateDailyRevenue = { dayLabel, newAmount ->
                            SmartOneRepository.updateDailyRevenue(dayLabel, newAmount)
                            Toast.makeText(context, "Revenu de $dayLabel mis à jour : ${String.format(Locale.US, "%,.0f", newAmount)} HTG", Toast.LENGTH_SHORT).show()
                        },
                        onIncreaseDailyRevenue = { dayLabel, amountToAdd ->
                            SmartOneRepository.increaseDailyRevenue(dayLabel, amountToAdd)
                            Toast.makeText(context, "+${String.format(Locale.US, "%,.0f", amountToAdd)} HTG ajouté au revenu journalier", Toast.LENGTH_SHORT).show()
                        },
                        onDeleteComment = { commentId ->
                            SmartOneRepository.deleteComment(commentId)
                            Toast.makeText(context, "Commentaire supprimé", Toast.LENGTH_SHORT).show()
                        },
                        onToggleSanction = { clientId ->
                            SmartOneRepository.toggleClientSuspension(clientId)
                            Toast.makeText(context, "Statut du client mis à jour", Toast.LENGTH_SHORT).show()
                        },
                        onResetDevices = { clientId ->
                            SmartOneRepository.resetConnectedDevicesForClient(clientId)
                            Toast.makeText(context, "Sessions réinitialisées (2 appareils max)", Toast.LENGTH_SHORT).show()
                        },
                        onToggleGatewayStatus = { gatewayId ->
                            SmartOneRepository.togglePaymentGatewayStatus(gatewayId)
                            val isBlocked = SmartOneRepository.isGatewayBlocked(gatewayId)
                            Toast.makeText(
                                context,
                                if (isBlocked) "Passerelle bloquée avec succès" else "Passerelle débloquée avec succès",
                                Toast.LENGTH_SHORT
                            ).show()
                        },
                        onPayForClient = { recipient, plan ->
                            SmartOneRepository.paySubscriptionForThirdParty(
                                recipientIdentifier = recipient,
                                plan = plan,
                                payerName = "Administrateur (Direct)",
                                deductFromBalance = false
                            )
                            Toast.makeText(context, "Abònman ${plan.label} aktive pou $recipient avèk siksè !", Toast.LENGTH_LONG).show()
                        },
                        onToggleClientPayForOthers = { clientId ->
                            SmartOneRepository.toggleClientPayForOthers(clientId)
                            Toast.makeText(context, "Aksè peye pou lòt kliyan modifye avèk siksè", Toast.LENGTH_SHORT).show()
                        }
                    )
                }
            }
        }
    }
}

    // Auth Dialog
    if (showAuthDialog) {
        AuthDialog(
            onDismiss = { showAuthDialog = false },
            onLoginSuccess = { name, emailOrPhone, isGoogle ->
                SmartOneRepository.login(name, emailOrPhone, isGoogle)
                showAuthDialog = false
                Toast.makeText(context, "Bienvenue, $name !", Toast.LENGTH_SHORT).show()
            }
        )
    }

    // Profile Dialog
    if (showProfileDialog) {
        ProfileDialog(
            user = currentUser,
            balance = balance,
            activeSubscription = activeSubscription,
            connectedDevices = connectedDevices,
            onDisconnectDevice = { deviceId ->
                SmartOneRepository.disconnectDevice(deviceId)
                Toast.makeText(context, "Appareil déconnecté", Toast.LENGTH_SHORT).show()
            },
            onDismiss = { showProfileDialog = false },
            onNavigateToDeposit = {
                showProfileDialog = false
                currentScreen = "deposit"
            },
            onLogout = {
                SmartOneRepository.logout()
                Toast.makeText(context, "Déconnecté", Toast.LENGTH_SHORT).show()
            },
            onOpenAdmin = {
                currentScreen = "admin"
            }
        )
    }

    // Device Limit Exceeded Dialog (Max 2 appareils en même temps)
    if (deviceLimitExceeded) {
        DeviceLimitExceededDialog(
            connectedDevices = connectedDevices,
            onDisconnectDevice = { deviceId ->
                SmartOneRepository.disconnectDevice(deviceId)
                Toast.makeText(context, "Appareil déconnecté. Accès libéré !", Toast.LENGTH_SHORT).show()
            },
            onDismiss = {
                SmartOneRepository.dismissDeviceLimitError()
            }
        )
    }

    // Search Dialog
    if (showSearchDialog) {
        SearchDialog(
            channels = channels,
            onDismiss = { showSearchDialog = false },
            onSelectChannel = { channel ->
                SmartOneRepository.playChannel(channel)
                currentScreen = "home"
            },
            onFilterCategory = { cat ->
                selectedCategory = cat
                currentScreen = "home"
            }
        )
    }

    // Language Dialog
    if (showLanguageDialog) {
        LanguageDialog(
            selectedLanguage = selectedLanguage,
            onDismiss = { showLanguageDialog = false },
            onSelectLanguage = { lang ->
                SmartOneRepository.setLanguage(lang)
                Toast.makeText(context, "Langue : $lang", Toast.LENGTH_SHORT).show()
            }
        )
    }

    // About Dialog
    if (showAboutDialog) {
        InfoContentDialog(
            title = "À propos de SmartOne IPTV",
            content = {
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    SmartOneFullBrandCard(height = 140.dp)
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(
                        text = "SmartOne IPTV propose une expérience de streaming global haute définition avec accès aux flux d'actualités, de sport, de cinéma et de divertissement mondial.",
                        color = DarkTextPrimary,
                        fontSize = 13.sp
                    )
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(text = "Éditeur : SmartOne Global Media & Administration", color = DarkTextSecondary, fontSize = 12.sp)
                    Text(text = "Support Disponible : ${SmartOneRepository.SUPPORT_EMAIL}", color = BrandGold, fontSize = 12.sp)
                }
            },
            onDismiss = { showAboutDialog = false }
        )
    }

    // FAQ Dialog
    if (showFaqDialog) {
        InfoContentDialog(
            title = "Foire Aux Questions (FAQ)",
            content = {
                Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                    Column {
                        Text("• Combien de temps gratuit par jour ?", fontWeight = FontWeight.Bold, color = DarkTextPrimary, fontSize = 13.sp)
                        Text("Chaque utilisateur ou visiteur bénéficie d'une heure (60 minutes) de streaming gratuit chaque jour.", color = DarkTextSecondary, fontSize = 12.sp)
                    }
                    Column {
                        Text("• Un nouvel abonnement remplace-t-il l'ancien ?", fontWeight = FontWeight.Bold, color = DarkTextPrimary, fontSize = 13.sp)
                        Text("Oui, l'activation d'un nouveau forfait annule automatiquement le plan précédent.", color = DarkTextSecondary, fontSize = 12.sp)
                    }
                    Column {
                        Text("• Comment recharger mon solde ?", fontWeight = FontWeight.Bold, color = DarkTextPrimary, fontSize = 13.sp)
                        Text("Rendez-vous dans la section Dépôt et choisissez MonCash, NatCash, Carte ou Crypto.", color = DarkTextSecondary, fontSize = 12.sp)
                    }
                }
            },
            onDismiss = { showFaqDialog = false }
        )
    }

    // Privacy Dialog
    if (showPrivacyDialog) {
        InfoContentDialog(
            title = "Politique de Confidentialité",
            content = {
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    Text(
                        text = "Vos informations personnelles et identifiants de paiement sont protégés et chiffrés. Les transactions sont validées côté serveur sécurisé.",
                        color = DarkTextPrimary,
                        fontSize = 13.sp
                    )
                    Text(
                        text = "Conformité Google Play et vérification de majorité (+18 ans) obligatoire.",
                        color = DarkTextSecondary,
                        fontSize = 12.sp
                    )
                }
            },
            onDismiss = { showPrivacyDialog = false }
        )
    }

    // Feedback Dialog
    if (showFeedbackDialog) {
        var feedbackText by remember { mutableStateOf("") }
        AlertDialog(
            onDismissRequest = { showFeedbackDialog = false },
            title = { Text("Commentaires & Avis", fontWeight = FontWeight.Bold) },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    Text("Donnez-nous votre avis pour améliorer SmartOne IPTV :", color = DarkTextSecondary, fontSize = 12.sp)
                    OutlinedTextField(
                        value = feedbackText,
                        onValueChange = { feedbackText = it },
                        modifier = Modifier.fillMaxWidth(),
                        minLines = 3,
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = BrandCyan,
                            unfocusedBorderColor = DarkBorder
                        )
                    )
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        if (feedbackText.isNotBlank()) {
                            SmartOneRepository.addComment(
                                author = currentUser?.name ?: "Client SmartOne",
                                comment = feedbackText
                            )
                        }
                        showFeedbackDialog = false
                        Toast.makeText(context, "Merci pour votre commentaire !", Toast.LENGTH_SHORT).show()
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = BrandBluePrimary)
                ) {
                    Text("Envoyer")
                }
            },
            dismissButton = {
                TextButton(onClick = { showFeedbackDialog = false }) {
                    Text("Annuler", color = DarkTextSecondary)
                }
            },
            containerColor = DarkSurface,
            titleContentColor = DarkTextPrimary,
            textContentColor = DarkTextPrimary
        )
    }

    // Free Quota Expired Dialog
    if (showFreeQuotaExpiredDialog) {
        AlertDialog(
            onDismissRequest = { showFreeQuotaExpiredDialog = false },
            title = {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(imageVector = Icons.Default.TimerOff, contentDescription = null, tint = StatusAmber)
                    Spacer(modifier = Modifier.width(8.dp))
                    Text("Temps Gratuit Épuisé")
                }
            },
            text = {
                Text(
                    text = "Votre heure de streaming gratuit quotidienne (60 minutes) est terminée.\n\nSouscrivez à un forfait VIP à partir de 250 HTG pour profiter d'un streaming illimité 24/7 en Ultra HD !",
                    color = DarkTextPrimary
                )
            },
            confirmButton = {
                Button(
                    onClick = {
                        showFreeQuotaExpiredDialog = false
                        currentScreen = "plans"
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = BrandBluePrimary)
                ) {
                    Text("Voir les abonnements")
                }
            },
            dismissButton = {
                TextButton(onClick = { showFreeQuotaExpiredDialog = false }) {
                    Text("Fermer", color = DarkTextSecondary)
                }
            },
            containerColor = DarkSurface,
            titleContentColor = DarkTextPrimary,
            textContentColor = DarkTextPrimary
        )
    }

    // Download App Dialog
    if (showDownloadDialog) {
        DownloadAppDialog(onDismiss = { showDownloadDialog = false })
    }
}
