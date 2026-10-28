package com.example.ui.screens

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import com.example.R
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.*
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.network.NetworkStatusMonitor
import com.example.data.payment.PaymentBackendService
import com.example.data.payment.PaymentGatewayType
import com.example.data.payment.PaymentOrder
import com.example.data.repository.OfflineCacheManager
import com.example.data.repository.SmartOneRepository
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.ui.components.*
import com.example.ui.theme.*
import java.util.Locale

@Composable
fun DepositScreen(
    balance: Double,
    onDepositSuccess: (Double, String) -> Unit,
    onNavigateToHistory: () -> Unit,
    modifier: Modifier = Modifier
) {
    var amountText by remember { mutableStateOf("1000") }
    var activeOrderForCheckout by remember { mutableStateOf<PaymentOrder?>(null) }
    var errorMessage by remember { mutableStateOf("") }
    var isLoading by remember { mutableStateOf(false) }

    // Return status banners
    var successReturnOrder by remember { mutableStateOf<PaymentOrder?>(null) }
    var cancelReturnOrderId by remember { mutableStateOf<String?>(null) }

    val networkStatus by NetworkStatusMonitor.networkStatus.collectAsStateWithLifecycle()
    val queuedOfflineDeposits by OfflineCacheManager.queuedOfflineDeposits.collectAsStateWithLifecycle()

    val paymentGateways by SmartOneRepository.paymentGateways.collectAsStateWithLifecycle()
    val isMonCashBlocked = paymentGateways.find { it.id == "moncash" }?.isBlocked ?: false
    val isNatcashBlocked = paymentGateways.find { it.id == "natcash" }?.isBlocked ?: false
    val isCardBlocked = paymentGateways.find { it.id == "google_play_stripe" }?.isBlocked ?: true
    val isCryptoBlocked = paymentGateways.find { it.id == "crypto" }?.isBlocked ?: false

    val amountValue = amountText.toDoubleOrNull() ?: 0.0
    val amountUsd = amountValue / SmartOneRepository.USD_RATE
    val amountEur = amountValue / SmartOneRepository.EUR_RATE

    LazyColumn(
        modifier = modifier.fillMaxSize(),
        contentPadding = PaddingValues(16.dp),
        verticalArrangement = Arrangement.spacedBy(14.dp)
    ) {
        // Balance Header & Tabs
        item {
            Card(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(containerColor = DarkSurface),
                border = CardDefaults.outlinedCardBorder().copy(
                    brush = androidx.compose.ui.graphics.SolidColor(DarkBorder)
                )
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        SmartOneBrandLogo(size = SmartOneLogoSize.SMALL)
                        Surface(
                            shape = RoundedCornerShape(20.dp),
                            color = StatusGreen.copy(alpha = 0.15f)
                        ) {
                            Text(
                                text = "Passerelles 24/7",
                                color = StatusGreen,
                                fontSize = 10.sp,
                                fontWeight = FontWeight.Bold,
                                modifier = Modifier.padding(horizontal = 8.dp, vertical = 2.dp)
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(10.dp))

                    Text(
                        text = "Solde du portefeuille SmartOne",
                        color = DarkTextMuted,
                        fontSize = 12.sp
                    )
                    Text(
                        text = "${String.format(Locale.US, "%,.2f", balance)} HTG",
                        color = BrandCyan,
                        fontSize = 22.sp,
                        fontWeight = FontWeight.Bold
                    )
                    Text(
                        text = "≈ $${String.format(Locale.US, "%.2f", balance / SmartOneRepository.USD_RATE)} · €${String.format(Locale.US, "%.2f", balance / SmartOneRepository.EUR_RATE)}",
                        color = DarkTextSecondary,
                        fontSize = 12.sp
                    )

                    Spacer(modifier = Modifier.height(12.dp))

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Button(
                            onClick = { /* already on deposit */ },
                            colors = ButtonDefaults.buttonColors(containerColor = BrandBluePrimary),
                            modifier = Modifier.weight(1f),
                            shape = RoundedCornerShape(8.dp)
                        ) {
                            Text("Dépôt")
                        }

                        OutlinedButton(
                            onClick = onNavigateToHistory,
                            colors = ButtonDefaults.outlinedButtonColors(contentColor = DarkTextPrimary),
                            modifier = Modifier.weight(1f),
                            shape = RoundedCornerShape(8.dp)
                        ) {
                            Text("Historique")
                        }
                    }
                }
            }
        }

        // Offline Queued Deposits Banner
        if (queuedOfflineDeposits.isNotEmpty()) {
            item {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    colors = CardDefaults.cardColors(containerColor = Color(0xFF1E293B)),
                    border = CardDefaults.outlinedCardBorder().copy(
                        brush = androidx.compose.ui.graphics.SolidColor(StatusAmber)
                    )
                ) {
                    Column(modifier = Modifier.padding(14.dp)) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(Icons.Default.CloudSync, contentDescription = null, tint = StatusAmber, modifier = Modifier.size(20.dp))
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(
                                text = "File d'attente hors-ligne (${queuedOfflineDeposits.size})",
                                color = StatusAmber,
                                fontWeight = FontWeight.Bold,
                                fontSize = 14.sp
                            )
                        }
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(
                            text = "Vos demandes de recharge enregistrées en mode basse connexion seront automatiquement synchronisées avec le serveur dès que le réseau sera rétabli.",
                            color = DarkTextSecondary,
                            fontSize = 11.sp
                        )
                        Spacer(modifier = Modifier.height(8.dp))
                        queuedOfflineDeposits.forEach { deposit ->
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .background(DarkSurfaceVariant, RoundedCornerShape(6.dp))
                                    .padding(horizontal = 8.dp, vertical = 6.dp),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Column {
                                    Text("${deposit.gatewayName} • ${deposit.amountHtg.toInt()} HTG", color = Color.White, fontWeight = FontWeight.Bold, fontSize = 12.sp)
                                    Text("Réf: ${deposit.transactionRef}", color = DarkTextMuted, fontSize = 10.sp)
                                }
                                Surface(shape = RoundedCornerShape(4.dp), color = StatusAmber.copy(alpha = 0.2f)) {
                                    Text("EN ATTENTE SYNC", color = StatusAmber, fontSize = 9.sp, fontWeight = FontWeight.Bold, modifier = Modifier.padding(horizontal = 4.dp, vertical = 1.dp))
                                }
                            }
                            Spacer(modifier = Modifier.height(4.dp))
                        }
                    }
                }
            }
        }

        // Return Page 1: Success Return Card
        if (successReturnOrder != null) {
            val order = successReturnOrder!!
            item {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    colors = CardDefaults.cardColors(containerColor = DarkSurface),
                    border = CardDefaults.outlinedCardBorder().copy(
                        brush = androidx.compose.ui.graphics.SolidColor(StatusGreen)
                    )
                ) {
                    Column(
                        modifier = Modifier.padding(16.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(imageVector = Icons.Default.CheckCircle, contentDescription = null, tint = StatusGreen, modifier = Modifier.size(24.dp))
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(
                                text = "Paiement ${order.gateway.displayName} Validé !",
                                color = StatusGreen,
                                fontWeight = FontWeight.Bold,
                                fontSize = 16.sp
                            )
                        }

                        Spacer(modifier = Modifier.height(8.dp))

                        Text(
                            text = "Votre transaction a été confirmée avec succès via Webhook IPN sécurisé.\nMontant crédité : ${String.format(Locale.US, "%,.2f", order.amount)} HTG\nRéférence : ${order.orderId}",
                            color = DarkTextPrimary,
                            fontSize = 13.sp,
                            lineHeight = 18.sp
                        )

                        Spacer(modifier = Modifier.height(12.dp))

                        Button(
                            onClick = { successReturnOrder = null },
                            colors = ButtonDefaults.buttonColors(containerColor = StatusGreen),
                            modifier = Modifier.fillMaxWidth(),
                            shape = RoundedCornerShape(8.dp)
                        ) {
                            Text("Continuer mes achats", color = Color.White, fontWeight = FontWeight.Bold)
                        }
                    }
                }
            }
        }

        // Return Page 2: Cancel Return Card
        if (cancelReturnOrderId != null) {
            item {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    colors = CardDefaults.cardColors(containerColor = DarkSurface),
                    border = CardDefaults.outlinedCardBorder().copy(
                        brush = androidx.compose.ui.graphics.SolidColor(StatusAmber)
                    )
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(14.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Row(modifier = Modifier.weight(1f), verticalAlignment = Alignment.CenterVertically) {
                            Icon(imageVector = Icons.Default.Cancel, contentDescription = null, tint = StatusAmber, modifier = Modifier.size(22.dp))
                            Spacer(modifier = Modifier.width(8.dp))
                            Column {
                                Text(
                                    text = "Transaction Annulée",
                                    color = StatusAmber,
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 14.sp
                                )
                                Text(
                                    text = "La session de paiement a été fermée. Aucun montant n'a été prélevé.",
                                    color = DarkTextSecondary,
                                    fontSize = 11.sp
                                )
                            }
                        }

                        IconButton(onClick = { cancelReturnOrderId = null }) {
                            Icon(imageVector = Icons.Default.Close, contentDescription = "Fermer", tint = DarkTextMuted)
                        }
                    }
                }
            }
        }

        // Amount Selection & Preset Chips
        item {
            Card(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(containerColor = DarkSurface),
                border = CardDefaults.outlinedCardBorder().copy(
                    brush = androidx.compose.ui.graphics.SolidColor(DarkBorder)
                )
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Text(
                        text = "1. Choisissez le montant à recharger",
                        color = DarkTextPrimary,
                        fontSize = 15.sp,
                        fontWeight = FontWeight.Bold
                    )

                    Spacer(modifier = Modifier.height(10.dp))

                    OutlinedTextField(
                        value = amountText,
                        onValueChange = {
                            amountText = it.filter { ch -> ch.isDigit() }
                            errorMessage = ""
                        },
                        singleLine = true,
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                        modifier = Modifier
                            .fillMaxWidth()
                            .testTag("deposit_amount_field"),
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = BrandCyan,
                            unfocusedBorderColor = DarkBorder,
                            focusedTextColor = Color.White,
                            unfocusedTextColor = Color.White
                        ),
                        trailingIcon = {
                            Text(
                                text = "HTG",
                                color = BrandCyan,
                                fontWeight = FontWeight.Bold,
                                modifier = Modifier.padding(end = 12.dp)
                            )
                        }
                    )

                    Spacer(modifier = Modifier.height(8.dp))

                    Text(
                        text = "≈ ${String.format(Locale.US, "%,.2f", amountValue)} HTG · $${String.format(Locale.US, "%.2f", amountUsd)} USD · €${String.format(Locale.US, "%.2f", amountEur)} EUR",
                        color = BrandGold,
                        fontSize = 13.sp,
                        fontWeight = FontWeight.Medium
                    )

                    Spacer(modifier = Modifier.height(12.dp))

                    Text(
                        text = "Raccourcis abonnements :",
                        color = DarkTextMuted,
                        fontSize = 12.sp
                    )

                    Spacer(modifier = Modifier.height(6.dp))

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        val quickAmounts = listOf(250, 750, 1500, 2500)
                        for (a in quickAmounts) {
                            FilterChip(
                                selected = amountText == a.toString(),
                                onClick = { amountText = a.toString() },
                                modifier = Modifier
                                    .weight(1f)
                                    .height(30.dp),
                                label = {
                                    Box(
                                        modifier = Modifier.fillMaxWidth(),
                                        contentAlignment = Alignment.Center
                                    ) {
                                        Text(
                                            text = "$a GDS",
                                            fontSize = 9.sp,
                                            fontWeight = FontWeight.SemiBold,
                                            maxLines = 1
                                        )
                                    }
                                },
                                colors = FilterChipDefaults.filterChipColors(
                                    containerColor = DarkSurfaceVariant,
                                    labelColor = DarkTextPrimary,
                                    selectedContainerColor = BrandBluePrimary,
                                    selectedLabelColor = Color.White
                                ),
                                border = null
                            )
                        }
                    }
                }
            }
        }

        // Security Notice Box
        item {
            Card(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(containerColor = DarkSurfaceVariant)
            ) {
                Row(
                    modifier = Modifier.padding(12.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(
                        imageVector = Icons.Default.Shield,
                        contentDescription = null,
                        tint = BrandCyan,
                        modifier = Modifier.size(22.dp)
                    )
                    Spacer(modifier = Modifier.width(10.dp))
                    Text(
                        text = "Sécurité Backend : Zéro numéro client collecté sur l'application. Vous êtes redirigé directement vers le portail officiel (MonCash ou Natcash) avec validation par Webhook IPN anti-replay.",
                        color = DarkTextSecondary,
                        fontSize = 11.sp,
                        lineHeight = 15.sp
                    )
                }
            }
        }

        if (errorMessage.isNotEmpty()) {
            item {
                Text(
                    text = errorMessage,
                    color = StatusRed,
                    fontSize = 13.sp,
                    fontWeight = FontWeight.Medium
                )
            }
        }

        // 4 Direct Official Payment Gateway Buttons
        item {
            Text(
                text = "2. Choisissez votre passerelle de paiement officielle",
                color = DarkTextPrimary,
                fontSize = 15.sp,
                fontWeight = FontWeight.Bold,
                modifier = Modifier.padding(top = 4.dp)
            )
        }

        // Button 1: Payer avec MonCash
        item {
            Button(
                onClick = {
                    if (isMonCashBlocked) {
                        errorMessage = "Pèyman MonCash bloke pa administratè a (Passerelle MonCash désactivée par l'admin)."
                        return@Button
                    }
                    if (amountValue < 250.0 || amountValue > 10000.0) {
                        errorMessage = "Le montant MonCash doit être compris entre 250 et 10 000 HTG."
                        return@Button
                    }
                    errorMessage = ""
                    isLoading = true
                    // Backend call: /api/payment/moncash/create
                    val response = PaymentBackendService.createMonCashPayment("usr_current", amountValue)
                    isLoading = false
                    if (response.success) {
                        activeOrderForCheckout = PaymentBackendService.getOrder(response.orderId)
                    }
                },
                colors = ButtonDefaults.buttonColors(
                    containerColor = if (isMonCashBlocked) DarkSurfaceVariant else Color(0xFFD81E05),
                    contentColor = if (isMonCashBlocked) DarkTextMuted else Color.White
                ),
                modifier = Modifier
                    .fillMaxWidth()
                    .height(54.dp)
                    .testTag("pay_with_moncash_button"),
                shape = RoundedCornerShape(12.dp)
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Surface(
                            modifier = Modifier
                                .size(36.dp)
                                .clip(RoundedCornerShape(8.dp)),
                            color = if (isMonCashBlocked) DarkBorder else Color.White
                        ) {
                            Image(
                                painter = painterResource(id = R.drawable.ic_moncash_logo),
                                contentDescription = "Logo MonCash",
                                modifier = Modifier
                                    .fillMaxSize()
                                    .padding(2.dp),
                                contentScale = ContentScale.Fit
                            )
                        }
                        Spacer(modifier = Modifier.width(12.dp))
                        Column {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Text("Payer avec MonCash", color = if (isMonCashBlocked) DarkTextMuted else Color.White, fontWeight = FontWeight.Bold, fontSize = 15.sp)
                                if (isMonCashBlocked) {
                                    Spacer(modifier = Modifier.width(6.dp))
                                    Surface(shape = RoundedCornerShape(4.dp), color = StatusRed.copy(alpha = 0.2f)) {
                                        Text("BLOQUÉ", color = StatusRed, fontSize = 10.sp, fontWeight = FontWeight.Bold, modifier = Modifier.padding(horizontal = 4.dp, vertical = 1.dp))
                                    }
                                }
                            }
                            Text(
                                text = if (isMonCashBlocked) "Passerelle temporairement désactivée" else "Redirection officielle Digicel Online",
                                color = if (isMonCashBlocked) StatusRed else Color.White.copy(alpha = 0.8f),
                                fontSize = 11.sp
                            )
                        }
                    }
                    Icon(
                        imageVector = if (isMonCashBlocked) Icons.Default.Lock else Icons.AutoMirrored.Filled.ArrowForward,
                        contentDescription = null,
                        tint = if (isMonCashBlocked) DarkTextMuted else Color.White
                    )
                }
            }
        }

        // Button 2: Payer avec Natcash
        item {
            Button(
                onClick = {
                    if (isNatcashBlocked) {
                        errorMessage = "Pèyman Natcash bloke pa administratè a (Passerelle Natcash désactivée par l'admin)."
                        return@Button
                    }
                    if (amountValue < 250.0 || amountValue > 10000.0) {
                        errorMessage = "Le montant Natcash doit être compris entre 250 et 10 000 HTG."
                        return@Button
                    }
                    errorMessage = ""
                    isLoading = true
                    // Backend call: /api/payment/natcash/create
                    val response = PaymentBackendService.createNatcashPayment("usr_current", amountValue)
                    isLoading = false
                    if (response.success) {
                        activeOrderForCheckout = PaymentBackendService.getOrder(response.orderId)
                    }
                },
                colors = ButtonDefaults.buttonColors(
                    containerColor = if (isNatcashBlocked) DarkSurfaceVariant else Color(0xFFFF6600),
                    contentColor = if (isNatcashBlocked) DarkTextMuted else Color.White
                ),
                modifier = Modifier
                    .fillMaxWidth()
                    .height(54.dp)
                    .testTag("pay_with_natcash_button"),
                shape = RoundedCornerShape(12.dp)
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Surface(
                            modifier = Modifier
                                .size(36.dp)
                                .clip(RoundedCornerShape(8.dp)),
                            color = if (isNatcashBlocked) DarkBorder else Color.White
                        ) {
                            Image(
                                painter = painterResource(id = R.drawable.ic_natcash_logo),
                                contentDescription = "Logo Natcash",
                                modifier = Modifier
                                    .fillMaxSize()
                                    .padding(2.dp),
                                contentScale = ContentScale.Fit
                            )
                        }
                        Spacer(modifier = Modifier.width(12.dp))
                        Column {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Text("Payer avec Natcash", color = if (isNatcashBlocked) DarkTextMuted else Color.White, fontWeight = FontWeight.Bold, fontSize = 15.sp)
                                if (isNatcashBlocked) {
                                    Spacer(modifier = Modifier.width(6.dp))
                                    Surface(shape = RoundedCornerShape(4.dp), color = StatusRed.copy(alpha = 0.2f)) {
                                        Text("BLOQUÉ", color = StatusRed, fontSize = 10.sp, fontWeight = FontWeight.Bold, modifier = Modifier.padding(horizontal = 4.dp, vertical = 1.dp))
                                    }
                                }
                            }
                            Text(
                                text = if (isNatcashBlocked) "Passerelle temporairement désactivée" else "Redirection officielle Natcom Online",
                                color = if (isNatcashBlocked) StatusRed else Color.White.copy(alpha = 0.8f),
                                fontSize = 11.sp
                            )
                        }
                    }
                    Icon(
                        imageVector = if (isNatcashBlocked) Icons.Default.Lock else Icons.AutoMirrored.Filled.ArrowForward,
                        contentDescription = null,
                        tint = if (isNatcashBlocked) DarkTextMuted else Color.White
                    )
                }
            }
        }

        // Button 3: Payer par Carte / Stripe (Bloqué par défaut comme demandé)
        item {
            Button(
                onClick = {
                    if (isCardBlocked) {
                        errorMessage = "Pèyman pa kat bloke pa administratè a (Paiement par carte bancaire bloqué par l'administrateur)."
                        return@Button
                    }
                    if (amountValue < 250.0) {
                        errorMessage = "Le montant minimum pour Stripe / Carte est de 250 HTG."
                        return@Button
                    }
                    errorMessage = ""
                    isLoading = true
                    val amount = amountValue.coerceAtLeast(250.0)
                    val resp = PaymentBackendService.createStripeCheckoutSession("usr_current", amount)
                    isLoading = false
                    if (resp.success) {
                        activeOrderForCheckout = PaymentBackendService.getOrder(resp.orderId)
                    }
                },
                colors = ButtonDefaults.buttonColors(
                    containerColor = if (isCardBlocked) DarkSurfaceVariant else Color(0xFF0066FF),
                    contentColor = if (isCardBlocked) DarkTextMuted else Color.White
                ),
                modifier = Modifier
                    .fillMaxWidth()
                    .height(54.dp)
                    .testTag("pay_with_stripe_button"),
                shape = RoundedCornerShape(12.dp)
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Box(
                            modifier = Modifier
                                .size(32.dp)
                                .clip(CircleShape)
                                .background(if (isCardBlocked) DarkBorder else Color.White),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                imageVector = if (isCardBlocked) Icons.Default.Lock else Icons.Default.CreditCard,
                                contentDescription = null,
                                tint = if (isCardBlocked) DarkTextMuted else Color(0xFF0066FF),
                                modifier = Modifier.size(18.dp)
                            )
                        }
                        Spacer(modifier = Modifier.width(12.dp))
                        Column {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Text(
                                    text = "Payer par Carte / Stripe",
                                    color = if (isCardBlocked) DarkTextMuted else Color.White,
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 15.sp
                                )
                                Spacer(modifier = Modifier.width(6.dp))
                                Surface(
                                    shape = RoundedCornerShape(4.dp),
                                    color = if (isCardBlocked) StatusRed.copy(alpha = 0.2f) else StatusGreen.copy(alpha = 0.15f)
                                ) {
                                    Text(
                                        text = if (isCardBlocked) "BLOQUÉ PAR L'ADMIN" else "DISPONIBLE",
                                        color = if (isCardBlocked) StatusRed else StatusGreen,
                                        fontSize = 10.sp,
                                        fontWeight = FontWeight.Bold,
                                        modifier = Modifier.padding(horizontal = 4.dp, vertical = 1.dp)
                                    )
                                }
                            }
                            Text(
                                text = if (isCardBlocked) "Pèyman pa kat bloke pa administratè a" else "Visa, Mastercard, Apple Pay & Google Pay",
                                color = if (isCardBlocked) StatusRed else Color.White.copy(alpha = 0.85f),
                                fontSize = 11.sp
                            )
                        }
                    }
                    Icon(
                        imageVector = if (isCardBlocked) Icons.Default.Lock else Icons.AutoMirrored.Filled.ArrowForward,
                        contentDescription = null,
                        tint = if (isCardBlocked) DarkTextMuted else Color.White
                    )
                }
            }
        }

        // Button 4: Crypto NOWPayments (Green background, White text, MonCash shape)
        item {
            Button(
                onClick = {
                    if (isCryptoBlocked) {
                        errorMessage = "Pèyman Crypto bloke pa administratè a (Passerelle Crypto désactivée par l'admin)."
                        return@Button
                    }
                    val amountUsd = (amountValue / SmartOneRepository.USD_RATE)
                    if (amountUsd < 5.0) {
                        errorMessage = "Le montant minimum Crypto est de $5.00 USD (env. 660 HTG)."
                        return@Button
                    }
                    errorMessage = ""
                    isLoading = true
                    val resp = PaymentBackendService.createNowPaymentsCryptoPayment("usr_current", amountUsd)
                    isLoading = false
                    if (resp.success) {
                        activeOrderForCheckout = PaymentBackendService.getOrder(resp.orderId)
                    }
                },
                colors = ButtonDefaults.buttonColors(
                    containerColor = if (isCryptoBlocked) DarkSurfaceVariant else Color(0xFF059669),
                    contentColor = if (isCryptoBlocked) DarkTextMuted else Color.White
                ),
                modifier = Modifier
                    .fillMaxWidth()
                    .height(54.dp)
                    .testTag("pay_with_crypto_button"),
                shape = RoundedCornerShape(12.dp)
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Surface(
                            modifier = Modifier
                                .size(36.dp)
                                .clip(RoundedCornerShape(8.dp)),
                            color = if (isCryptoBlocked) DarkBorder else Color(0xFF181A20)
                        ) {
                            Image(
                                painter = painterResource(id = R.drawable.ic_crypto_logo),
                                contentDescription = "Logo Crypto",
                                modifier = Modifier
                                    .fillMaxSize()
                                    .padding(2.dp),
                                contentScale = ContentScale.Fit
                            )
                        }
                        Spacer(modifier = Modifier.width(12.dp))
                        Column {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Text(
                                    text = "Payer par Crypto",
                                    color = if (isCryptoBlocked) DarkTextMuted else Color.White,
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 15.sp
                                )
                                if (isCryptoBlocked) {
                                    Spacer(modifier = Modifier.width(6.dp))
                                    Surface(shape = RoundedCornerShape(4.dp), color = StatusRed.copy(alpha = 0.2f)) {
                                        Text("BLOQUÉ", color = StatusRed, fontSize = 10.sp, fontWeight = FontWeight.Bold, modifier = Modifier.padding(horizontal = 4.dp, vertical = 1.dp))
                                    }
                                }
                            }
                            Text(
                                text = if (isCryptoBlocked) "Passerelle temporairement désactivée" else "USDT, Bitcoin, Ethereum, TRX",
                                color = if (isCryptoBlocked) StatusRed else Color.White.copy(alpha = 0.85f),
                                fontSize = 11.sp
                            )
                        }
                    }
                    Icon(
                        imageVector = if (isCryptoBlocked) Icons.Default.Lock else Icons.AutoMirrored.Filled.ArrowForward,
                        contentDescription = null,
                        tint = if (isCryptoBlocked) DarkTextMuted else Color.White
                    )
                }
            }
        }
    }

    // Hosted Checkout Dialog or Crypto Checkout Dialog with Live Polling
    if (activeOrderForCheckout != null) {
        val order = activeOrderForCheckout!!
        if (order.gateway == PaymentGatewayType.NOWPAYMENTS_CRYPTO) {
            com.example.ui.components.CryptoCheckoutDialog(
                order = order,
                onSuccess = { amountHtg ->
                    activeOrderForCheckout = null
                    val updated = PaymentBackendService.getOrder(order.orderId)
                    successReturnOrder = updated ?: order
                    cancelReturnOrderId = null
                    onDepositSuccess(amountHtg, "NOWPayments Crypto (USDT)")
                },
                onDismiss = {
                    activeOrderForCheckout = null
                }
            )
        } else {
            HostedCheckoutDialog(
                order = order,
                onComplete = { success, message ->
                    activeOrderForCheckout = null
                    if (success) {
                        val updated = PaymentBackendService.getOrder(order.orderId)
                        successReturnOrder = updated ?: order
                        cancelReturnOrderId = null
                        onDepositSuccess(order.amount, order.gateway.displayName)
                    } else {
                        errorMessage = message
                    }
                },
                onCancel = {
                    PaymentBackendService.cancelOrder(order.orderId)
                    cancelReturnOrderId = order.orderId
                    activeOrderForCheckout = null
                }
            )
        }
    }
}
