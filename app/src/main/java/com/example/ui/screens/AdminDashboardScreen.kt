package com.example.ui.screens

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
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
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.model.AdminClient
import com.example.data.model.DailyRevenue
import com.example.data.model.PaymentGateway
import com.example.data.model.SubscriptionPlan
import com.example.data.model.ThirdPartySubscription
import com.example.data.model.UserComment
import com.example.data.repository.SmartOneRepository
import com.example.ui.components.*
import com.example.ui.theme.*
import java.util.Locale

@Composable
fun AdminDashboardScreen(
    adminClients: List<AdminClient>,
    dailyRevenues: List<DailyRevenue>,
    recentComments: List<UserComment>,
    paymentGateways: List<PaymentGateway> = emptyList(),
    thirdPartySubscriptions: List<ThirdPartySubscription> = emptyList(),
    onUpdateDailyRevenue: (String, Double) -> Unit,
    onIncreaseDailyRevenue: (String, Double) -> Unit,
    onDeleteComment: (String) -> Unit,
    onToggleSanction: (String) -> Unit,
    onResetDevices: (String) -> Unit = {},
    onToggleGatewayStatus: (String) -> Unit = {},
    onPayForClient: (String, SubscriptionPlan) -> Unit = { _, _ -> },
    onToggleClientPayForOthers: (String) -> Unit = {},
    modifier: Modifier = Modifier
) {
    // Dynamic Revenue Calculations: Daily, Monthly (x30), Annual (x365)
    val totalRevenue7Days = remember(dailyRevenues) {
        dailyRevenues.sumOf { it.amount }
    }
    val avgDailyRevenue = remember(dailyRevenues) {
        if (dailyRevenues.isNotEmpty()) dailyRevenues.sumOf { it.amount } / dailyRevenues.size else 0.0
    }
    val todayRevenue = remember(dailyRevenues) {
        dailyRevenues.find { it.dayLabel.contains("Aujourd'hui", ignoreCase = true) }?.amount
            ?: dailyRevenues.lastOrNull()?.amount ?: 4500.0
    }
    val maxRevenue = remember(dailyRevenues) {
        dailyRevenues.maxOfOrNull { it.amount }?.coerceAtLeast(1000.0) ?: 10000.0
    }

    // Dynamic Monthly Projection (x30 days based on average daily revenue)
    val projectedMonthlyRevenue = remember(avgDailyRevenue) {
        avgDailyRevenue * 30.0
    }

    // Dynamic Annual Projection (x365 days / 12 months)
    val projectedAnnualRevenue = remember(projectedMonthlyRevenue) {
        projectedMonthlyRevenue * 12.0
    }

    // Modal state for custom revenue adjustment
    var showCustomRevenueDialog by remember { mutableStateOf(false) }
    var selectedDayForEdit by remember { mutableStateOf("Aujourd'hui") }
    var inputAmountText by remember { mutableStateOf("") }

    // Admin Payment & Third Party Subscription states
    var recipientInput by remember { mutableStateOf("") }
    var selectedPlanIndex by remember { mutableIntStateOf(3) } // Default 1 An
    var adminPayNoticeMessage by remember { mutableStateOf<String?>(null) }
    var recipientInputError by remember { mutableStateOf<String?>(null) }

    LazyColumn(
        modifier = modifier.fillMaxSize(),
        contentPadding = PaddingValues(16.dp),
        verticalArrangement = Arrangement.spacedBy(14.dp)
    ) {
        // Admin Header
        item {
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .testTag("admin_header_card"),
                colors = CardDefaults.cardColors(containerColor = DarkSurface),
                border = CardDefaults.outlinedCardBorder().copy(
                    brush = androidx.compose.ui.graphics.SolidColor(BrandCyan.copy(alpha = 0.5f))
                )
            ) {
                Row(
                    modifier = Modifier.padding(16.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    androidx.compose.foundation.Image(
                        painter = androidx.compose.ui.res.painterResource(id = com.example.R.drawable.ic_smartone_emblem),
                        contentDescription = "Emblème SmartOne IPTV",
                        modifier = Modifier
                            .size(46.dp)
                            .clip(CircleShape)
                    )

                    Spacer(modifier = Modifier.width(12.dp))

                    Column {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Text(
                                text = "Smart",
                                color = LogoSapphireBlue,
                                fontWeight = FontWeight.Bold,
                                fontSize = 17.sp
                            )
                            Text(
                                text = "One ",
                                color = LogoTealCyan,
                                fontWeight = FontWeight.ExtraBold,
                                fontSize = 17.sp
                            )
                            Surface(
                                shape = RoundedCornerShape(4.dp),
                                color = LogoCopperGold.copy(alpha = 0.18f),
                                border = CardDefaults.outlinedCardBorder().copy(
                                    brush = androidx.compose.ui.graphics.SolidColor(LogoCopperGold)
                                )
                            ) {
                                Text(
                                    text = "IPTV",
                                    color = LogoCopperGold,
                                    fontWeight = FontWeight.Black,
                                    fontSize = 13.sp,
                                    modifier = Modifier.padding(horizontal = 4.dp, vertical = 1.dp)
                                )
                            }
                            Spacer(modifier = Modifier.width(6.dp))
                            Text(
                                text = "• Admin",
                                color = DarkTextPrimary,
                                fontWeight = FontWeight.Bold,
                                fontSize = 15.sp
                            )
                        }
                        Text(
                            text = "Supervision & Contrôle Global 🌐",
                            color = DarkTextSecondary,
                            fontSize = 12.sp
                        )
                    }
                }
            }
        }

        // Section 1: Dynamic Revenue Reports (Daily, Monthly, Annual)
        item {
            Text(
                text = "Rapports des Revenus & Projections",
                color = DarkTextPrimary,
                fontSize = 16.sp,
                fontWeight = FontWeight.Bold
            )
        }

        // Row 1: Revenus Aujourd'hui & Rapport Mensuel
        item {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                MetricCard(
                    title = "Revenu Aujourd'hui",
                    value = "${String.format(Locale.US, "%,.0f", todayRevenue)} HTG",
                    subtitle = "≈ $${String.format(Locale.US, "%.0f", todayRevenue / SmartOneRepository.USD_RATE)} USD",
                    icon = Icons.AutoMirrored.Filled.TrendingUp,
                    accentColor = StatusGreen,
                    modifier = Modifier.weight(1f)
                )

                MetricCard(
                    title = "Rapport Mensuel (x30j)",
                    value = "${String.format(Locale.US, "%,.0f", projectedMonthlyRevenue)} HTG",
                    subtitle = "≈ $${String.format(Locale.US, "%.0f", projectedMonthlyRevenue / SmartOneRepository.USD_RATE)} USD",
                    icon = Icons.Default.CalendarMonth,
                    accentColor = BrandCyan,
                    modifier = Modifier.weight(1f)
                )
            }
        }

        // Row 2: Rapport Annuel & Moyenne Journalière
        item {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                MetricCard(
                    title = "Rapport Annuel (x365j)",
                    value = "${String.format(Locale.US, "%,.0f", projectedAnnualRevenue)} HTG",
                    subtitle = "≈ $${String.format(Locale.US, "%.0f", projectedAnnualRevenue / SmartOneRepository.USD_RATE)} USD",
                    icon = Icons.Default.Assessment,
                    accentColor = BrandGold,
                    modifier = Modifier.weight(1f)
                )

                MetricCard(
                    title = "Moyenne Journalière",
                    value = "${String.format(Locale.US, "%,.0f", avgDailyRevenue)} HTG/j",
                    subtitle = "Semaine: ${String.format(Locale.US, "%,.0f", totalRevenue7Days)} HTG",
                    icon = Icons.Default.Equalizer,
                    accentColor = BrandBluePrimary,
                    modifier = Modifier.weight(1f)
                )
            }
        }

        // Section 2: Interactive Daily Revenue Booster & Custom Editor
        item {
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .testTag("revenue_adjustment_card"),
                colors = CardDefaults.cardColors(containerColor = DarkSurface),
                border = CardDefaults.outlinedCardBorder().copy(
                    brush = androidx.compose.ui.graphics.SolidColor(StatusGreen.copy(alpha = 0.5f))
                ),
                shape = RoundedCornerShape(12.dp)
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(
                                imageVector = Icons.Default.Tune,
                                contentDescription = null,
                                tint = StatusGreen,
                                modifier = Modifier.size(20.dp)
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(
                                text = "Ajustement du Revenu Journalier",
                                color = DarkTextPrimary,
                                fontWeight = FontWeight.Bold,
                                fontSize = 15.sp
                            )
                        }

                        Surface(
                            shape = RoundedCornerShape(4.dp),
                            color = StatusGreen.copy(alpha = 0.15f)
                        ) {
                            Text(
                                text = "+Projections",
                                color = StatusGreen,
                                fontSize = 10.sp,
                                fontWeight = FontWeight.Bold,
                                modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(4.dp))

                    Text(
                        text = "Augmentez ou modifiez le montant journalier pour recalculer et élever immédiatement les rapports mensuel et annuel.",
                        color = DarkTextSecondary,
                        fontSize = 11.sp,
                        lineHeight = 15.sp
                    )

                    Spacer(modifier = Modifier.height(12.dp))

                    Text(
                        text = "Booster rapide du jour (Aujourd'hui) :",
                        color = DarkTextMuted,
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Medium
                    )

                    Spacer(modifier = Modifier.height(6.dp))

                    // Quick Booster Chips (+500, +1 000, +2 500, +5 000 HTG)
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        val boostAmounts = listOf(500.0, 1000.0, 2500.0, 5000.0)
                        for (amt in boostAmounts) {
                            Button(
                                onClick = { onIncreaseDailyRevenue("Aujourd'hui", amt) },
                                colors = ButtonDefaults.buttonColors(
                                    containerColor = StatusGreen.copy(alpha = 0.2f),
                                    contentColor = StatusGreen
                                ),
                                contentPadding = PaddingValues(horizontal = 4.dp, vertical = 6.dp),
                                shape = RoundedCornerShape(8.dp),
                                modifier = Modifier.weight(1f)
                            ) {
                                Text(
                                    text = "+${String.format(Locale.US, "%,.0f", amt)}",
                                    fontSize = 11.sp,
                                    fontWeight = FontWeight.Bold
                                )
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(10.dp))

                    // Button to open full custom day editor
                    OutlinedButton(
                        onClick = {
                            selectedDayForEdit = "Aujourd'hui"
                            inputAmountText = todayRevenue.toInt().toString()
                            showCustomRevenueDialog = true
                        },
                        modifier = Modifier
                            .fillMaxWidth()
                            .testTag("open_custom_revenue_button"),
                        shape = RoundedCornerShape(8.dp),
                        colors = ButtonDefaults.outlinedButtonColors(contentColor = BrandCyan)
                    ) {
                        Icon(imageVector = Icons.Default.Edit, contentDescription = null, modifier = Modifier.size(16.dp))
                        Spacer(modifier = Modifier.width(6.dp))
                        Text("Modifier un jour précis ou fixer un montant", fontSize = 12.sp)
                    }
                }
            }
        }

        // Daily Revenue Bar Chart with live interactive values
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
                        Text(
                            text = "Détail des 7 derniers jours",
                            color = DarkTextPrimary,
                            fontWeight = FontWeight.Bold,
                            fontSize = 15.sp
                        )
                        Text(
                            text = "Semaine : ${String.format(Locale.US, "%,.0f", totalRevenue7Days)} HTG",
                            color = BrandCyan,
                            fontSize = 12.sp,
                            fontWeight = FontWeight.SemiBold
                        )
                    }

                    Spacer(modifier = Modifier.height(14.dp))

                    for (day in dailyRevenues) {
                        val percentage = (day.amount / maxRevenue).toFloat().coerceIn(0.06f, 1f)

                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(vertical = 4.dp)
                                .clickable {
                                    selectedDayForEdit = day.dayLabel
                                    inputAmountText = day.amount.toInt().toString()
                                    showCustomRevenueDialog = true
                                },
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = day.dayLabel,
                                color = if (day.dayLabel == "Aujourd'hui") BrandCyan else DarkTextSecondary,
                                fontSize = 12.sp,
                                fontWeight = if (day.dayLabel == "Aujourd'hui") FontWeight.Bold else FontWeight.Normal,
                                modifier = Modifier.width(72.dp)
                            )

                            Box(
                                modifier = Modifier
                                    .weight(1f)
                                    .height(18.dp)
                                    .clip(RoundedCornerShape(4.dp))
                                    .background(DarkSurfaceVariant)
                            ) {
                                Box(
                                    modifier = Modifier
                                        .fillMaxHeight()
                                        .fillMaxWidth(percentage)
                                        .clip(RoundedCornerShape(4.dp))
                                        .background(
                                            if (day.dayLabel == "Aujourd'hui") StatusGreen else BrandBluePrimary
                                        )
                                )
                            }

                            Text(
                                text = "${String.format(Locale.US, "%,.0f", day.amount)} HTG",
                                color = DarkTextPrimary,
                                fontSize = 12.sp,
                                fontWeight = FontWeight.Bold,
                                modifier = Modifier
                                    .width(86.dp)
                                    .padding(start = 8.dp)
                            )

                            Icon(
                                imageVector = Icons.Default.Edit,
                                contentDescription = "Modifier",
                                tint = DarkTextMuted,
                                modifier = Modifier.size(14.dp)
                            )
                        }
                    }
                }
            }
        }

        // Section: Kontwòl Passerelles de Paiement (Bloke / Debloke Aksè)
        item {
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .testTag("admin_payment_gateways_control"),
                colors = CardDefaults.cardColors(containerColor = DarkSurface),
                border = CardDefaults.outlinedCardBorder().copy(
                    brush = androidx.compose.ui.graphics.SolidColor(BrandCyan.copy(alpha = 0.5f))
                ),
                shape = RoundedCornerShape(14.dp)
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(
                                imageVector = Icons.Default.Payment,
                                contentDescription = null,
                                tint = BrandCyan,
                                modifier = Modifier.size(20.dp)
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(
                                text = "Kontwòl Passerelles de Paiement",
                                color = DarkTextPrimary,
                                fontSize = 16.sp,
                                fontWeight = FontWeight.Bold
                            )
                        }

                        Surface(
                            shape = RoundedCornerShape(12.dp),
                            color = BrandCyan.copy(alpha = 0.15f)
                        ) {
                            Text(
                                text = "Bloke / Debloke",
                                color = BrandCyan,
                                fontSize = 11.sp,
                                fontWeight = FontWeight.Bold,
                                modifier = Modifier.padding(horizontal = 8.dp, vertical = 3.dp)
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(6.dp))

                    Text(
                        text = "Ou ka bloke oswa debloke nenpòt mòd pèyman an tan reyèl. Remak : Pèyman pa kat (Stripe / Google Play) bloke pa defo.",
                        color = DarkTextSecondary,
                        fontSize = 12.sp,
                        lineHeight = 16.sp
                    )

                    Spacer(modifier = Modifier.height(14.dp))

                    Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                        for (gw in paymentGateways) {
                            val isBlocked = gw.isBlocked
                            Card(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .testTag("gateway_item_${gw.id}"),
                                colors = CardDefaults.cardColors(
                                    containerColor = if (isBlocked) DarkSurfaceVariant.copy(alpha = 0.7f) else DarkSurfaceVariant
                                ),
                                border = CardDefaults.outlinedCardBorder().copy(
                                    brush = androidx.compose.ui.graphics.SolidColor(
                                        if (isBlocked) StatusRed.copy(alpha = 0.6f) else StatusGreen.copy(alpha = 0.4f)
                                    )
                                ),
                                shape = RoundedCornerShape(10.dp)
                            ) {
                                Row(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .padding(12.dp),
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Row(modifier = Modifier.weight(1f), verticalAlignment = Alignment.CenterVertically) {
                                        val logoResId = when (gw.id) {
                                            "moncash" -> R.drawable.ic_moncash_logo
                                            "natcash" -> R.drawable.ic_natcash_logo
                                            "crypto" -> R.drawable.ic_crypto_logo
                                            else -> null
                                        }
                                        if (logoResId != null) {
                                            Surface(
                                                modifier = Modifier
                                                    .size(34.dp)
                                                    .clip(RoundedCornerShape(8.dp)),
                                                color = Color.White
                                            ) {
                                                Image(
                                                    painter = painterResource(id = logoResId),
                                                    contentDescription = gw.name,
                                                    modifier = Modifier
                                                        .fillMaxSize()
                                                        .padding(2.dp),
                                                    contentScale = ContentScale.Fit
                                                )
                                            }
                                            Spacer(modifier = Modifier.width(10.dp))
                                        }

                                        Column {
                                            Row(verticalAlignment = Alignment.CenterVertically) {
                                                Text(
                                                    text = gw.name,
                                                    color = if (isBlocked) DarkTextSecondary else DarkTextPrimary,
                                                    fontWeight = FontWeight.Bold,
                                                    fontSize = 14.sp
                                                )
                                                Spacer(modifier = Modifier.width(6.dp))
                                                Surface(
                                                    shape = RoundedCornerShape(4.dp),
                                                    color = if (isBlocked) StatusRed.copy(alpha = 0.2f) else StatusGreen.copy(alpha = 0.2f)
                                                ) {
                                                    Text(
                                                        text = if (isBlocked) "BLOQUÉ" else "ACTIF",
                                                        color = if (isBlocked) StatusRed else StatusGreen,
                                                        fontSize = 9.sp,
                                                        fontWeight = FontWeight.Bold,
                                                        modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                                                    )
                                                }
                                            }

                                        Spacer(modifier = Modifier.height(2.dp))

                                        Text(
                                            text = if (isBlocked) "Aksè bloke pou tout itilizatè yo." else "${gw.type} • Limite : ${gw.minHtg.toInt()} - ${gw.maxHtg.toInt()} HTG",
                                            color = if (isBlocked) StatusRed else DarkTextMuted,
                                            fontSize = 11.sp
                                        )
                                    }
                                }

                                Spacer(modifier = Modifier.width(8.dp))

                                    Button(
                                        onClick = { onToggleGatewayStatus(gw.id) },
                                        colors = ButtonDefaults.buttonColors(
                                            containerColor = if (isBlocked) StatusGreen else StatusRed
                                        ),
                                        shape = RoundedCornerShape(8.dp),
                                        contentPadding = PaddingValues(horizontal = 10.dp, vertical = 6.dp),
                                        modifier = Modifier.testTag("toggle_gateway_${gw.id}")
                                    ) {
                                        Icon(
                                            imageVector = if (isBlocked) Icons.Default.LockOpen else Icons.Default.Lock,
                                            contentDescription = null,
                                            tint = Color.White,
                                            modifier = Modifier.size(14.dp)
                                        )
                                        Spacer(modifier = Modifier.width(4.dp))
                                        Text(
                                            text = if (isBlocked) "Debloke" else "Bloke",
                                            fontSize = 11.sp,
                                            fontWeight = FontWeight.Bold,
                                            color = Color.White
                                        )
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }

        // Section: Peye / Aktive Abònman pou yon Kliyan (pa Email oubyen Telefòn)
        item {
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .testTag("admin_pay_for_client_card"),
                colors = CardDefaults.cardColors(containerColor = DarkSurface),
                border = CardDefaults.outlinedCardBorder().copy(
                    brush = androidx.compose.ui.graphics.SolidColor(BrandGold.copy(alpha = 0.6f))
                ),
                shape = RoundedCornerShape(14.dp)
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(
                                imageVector = Icons.Default.PersonAdd,
                                contentDescription = null,
                                tint = BrandGold,
                                modifier = Modifier.size(20.dp)
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(
                                text = "Peye Abònman pou yon Kliyan",
                                color = DarkTextPrimary,
                                fontSize = 16.sp,
                                fontWeight = FontWeight.Bold
                            )
                        }

                        Surface(
                            shape = RoundedCornerShape(12.dp),
                            color = BrandGold.copy(alpha = 0.15f)
                        ) {
                            Text(
                                text = "Aksè Admin",
                                color = BrandGold,
                                fontSize = 11.sp,
                                fontWeight = FontWeight.Bold,
                                modifier = Modifier.padding(horizontal = 8.dp, vertical = 3.dp)
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(6.dp))

                    Text(
                        text = "Antre email oubyen nimewo telefòn kliyan an (+509...) pou valide epi aktive abònman IPTV a dirèkteman sou kont li.",
                        color = DarkTextSecondary,
                        fontSize = 12.sp,
                        lineHeight = 16.sp
                    )

                    Spacer(modifier = Modifier.height(12.dp))

                    OutlinedTextField(
                        value = recipientInput,
                        onValueChange = {
                            recipientInput = it
                            if (recipientInputError != null) recipientInputError = null
                        },
                        label = { Text("Email oubyen Nimewo Telefòn Kliyan an") },
                        placeholder = { Text("Ex: +509 3215 3281 oswa marie.c@gmail.com", color = DarkTextMuted) },
                        singleLine = true,
                        isError = recipientInputError != null,
                        modifier = Modifier
                            .fillMaxWidth()
                            .testTag("admin_recipient_input"),
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = BrandGold,
                            unfocusedBorderColor = DarkBorder,
                            focusedTextColor = Color.White,
                            unfocusedTextColor = Color.White
                        )
                    )

                    if (recipientInputError != null) {
                        Text(
                            text = recipientInputError ?: "",
                            color = StatusRed,
                            fontSize = 11.sp,
                            modifier = Modifier.padding(start = 4.dp, top = 4.dp)
                        )
                    }

                    Spacer(modifier = Modifier.height(10.dp))

                    Text(
                        text = "Chwazi plan pou kliyan an :",
                        color = DarkTextMuted,
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Medium
                    )

                    Spacer(modifier = Modifier.height(6.dp))

                    // Plan selector chips
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        val plans = SmartOneRepository.subscriptionPlans
                        for ((index, plan) in plans.withIndex()) {
                            FilterChip(
                                selected = selectedPlanIndex == index,
                                onClick = { selectedPlanIndex = index },
                                label = { Text(plan.label, fontSize = 11.sp) },
                                colors = FilterChipDefaults.filterChipColors(
                                    selectedContainerColor = BrandGold,
                                    selectedLabelColor = Color.Black,
                                    containerColor = DarkSurfaceVariant,
                                    labelColor = DarkTextPrimary
                                ),
                                modifier = Modifier.weight(1f)
                            )
                        }
                    }

                    val chosenPlan = SmartOneRepository.subscriptionPlans.getOrNull(selectedPlanIndex) ?: SmartOneRepository.subscriptionPlans.last()

                    Spacer(modifier = Modifier.height(12.dp))

                    Button(
                        onClick = {
                            if (recipientInput.isBlank()) {
                                recipientInputError = "Tanpri antre yon email oswa yon nimewo telefòn valid."
                                return@Button
                            }
                            onPayForClient(recipientInput.trim(), chosenPlan)
                            adminPayNoticeMessage = "Abònman ${chosenPlan.label} aktive avèk siksè pou ${recipientInput.trim()} !"
                            recipientInput = ""
                            recipientInputError = null
                        },
                        colors = ButtonDefaults.buttonColors(containerColor = BrandGoldDark),
                        shape = RoundedCornerShape(8.dp),
                        modifier = Modifier
                            .fillMaxWidth()
                            .testTag("admin_submit_pay_for_client_button")
                    ) {
                        Icon(Icons.Default.Check, contentDescription = null, tint = Color.Black, modifier = Modifier.size(16.dp))
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(
                            text = "Aktive Abònman ${chosenPlan.label} (${String.format(Locale.US, "%,.0f", chosenPlan.priceGds)} HTG)",
                            color = Color.Black,
                            fontWeight = FontWeight.Bold,
                            fontSize = 13.sp
                        )
                    }

                    if (adminPayNoticeMessage != null) {
                        Spacer(modifier = Modifier.height(8.dp))
                        Surface(
                            shape = RoundedCornerShape(6.dp),
                            color = StatusGreen.copy(alpha = 0.15f),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Row(
                                modifier = Modifier.padding(10.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Icon(Icons.Default.CheckCircle, contentDescription = null, tint = StatusGreen, modifier = Modifier.size(16.dp))
                                Spacer(modifier = Modifier.width(6.dp))
                                Text(
                                    text = adminPayNoticeMessage ?: "",
                                    color = StatusGreen,
                                    fontSize = 12.sp,
                                    fontWeight = FontWeight.SemiBold
                                )
                            }
                        }
                    }

                    if (thirdPartySubscriptions.isNotEmpty()) {
                        Spacer(modifier = Modifier.height(14.dp))
                        HorizontalDivider(color = DarkBorder)
                        Spacer(modifier = Modifier.height(10.dp))
                        Text(
                            text = "Dènye abònman peye pou kliyan/tiers :",
                            color = DarkTextMuted,
                            fontSize = 12.sp,
                            fontWeight = FontWeight.SemiBold
                        )
                        Spacer(modifier = Modifier.height(6.dp))
                        for (item in thirdPartySubscriptions.take(3)) {
                            Surface(
                                shape = RoundedCornerShape(6.dp),
                                color = DarkSurfaceVariant,
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(vertical = 3.dp)
                            ) {
                                Row(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .padding(8.dp),
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Column {
                                        Text(
                                            text = "Kliyan : ${item.recipientIdentifier}",
                                            color = DarkTextPrimary,
                                            fontWeight = FontWeight.Bold,
                                            fontSize = 12.sp
                                        )
                                        Text(
                                            text = "Peye pa : ${item.payerName} • ${item.timestamp}",
                                            color = DarkTextMuted,
                                            fontSize = 10.sp
                                        )
                                    }
                                    Surface(
                                        shape = RoundedCornerShape(4.dp),
                                        color = BrandCyan.copy(alpha = 0.2f)
                                    ) {
                                        Text(
                                            text = "${item.planLabel} (${String.format(Locale.US, "%,.0f", item.amountHtg)} G)",
                                            color = BrandCyan,
                                            fontSize = 10.sp,
                                            fontWeight = FontWeight.Bold,
                                            modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                                        )
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }

        // Section 3: 10 Derniers Commentaires Récents
        item {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(top = 8.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        imageVector = Icons.Default.Forum,
                        contentDescription = null,
                        tint = BrandCyan,
                        modifier = Modifier.size(20.dp)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = "10 Derniers Commentaires Récents",
                        color = DarkTextPrimary,
                        fontSize = 16.sp,
                        fontWeight = FontWeight.Bold
                    )
                }

                Surface(
                    shape = RoundedCornerShape(12.dp),
                    color = BrandGold.copy(alpha = 0.15f)
                ) {
                    Text(
                        text = "Note: 4.8 / 5 ★",
                        color = BrandGold,
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold,
                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 3.dp)
                    )
                }
            }
        }

        // Listing the 10 most recent comments
        items(recentComments.take(10), key = { it.id }) { comment ->
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .testTag("admin_comment_${comment.id}"),
                colors = CardDefaults.cardColors(containerColor = DarkSurface),
                border = CardDefaults.outlinedCardBorder().copy(
                    brush = androidx.compose.ui.graphics.SolidColor(DarkBorder)
                ),
                shape = RoundedCornerShape(12.dp)
            ) {
                Column(modifier = Modifier.padding(14.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Box(
                                modifier = Modifier
                                    .size(34.dp)
                                    .clip(CircleShape)
                                    .background(BrandCyan.copy(alpha = 0.2f)),
                                contentAlignment = Alignment.Center
                            ) {
                                Text(
                                    text = comment.author.take(1).uppercase(),
                                    color = BrandCyan,
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 14.sp
                                )
                            }
                            Spacer(modifier = Modifier.width(10.dp))
                            Column {
                                Text(
                                    text = comment.author,
                                    color = DarkTextPrimary,
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 13.sp
                                )
                                Text(
                                    text = "${comment.phone} • ${comment.date}",
                                    color = DarkTextMuted,
                                    fontSize = 11.sp
                                )
                            }
                        }

                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Surface(
                                shape = RoundedCornerShape(4.dp),
                                color = StatusGreen.copy(alpha = 0.15f)
                            ) {
                                Text(
                                    text = comment.badge,
                                    color = StatusGreen,
                                    fontSize = 10.sp,
                                    fontWeight = FontWeight.SemiBold,
                                    modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                                )
                            }
                            Spacer(modifier = Modifier.width(4.dp))
                            IconButton(
                                onClick = { onDeleteComment(comment.id) },
                                modifier = Modifier.size(28.dp)
                            ) {
                                Icon(
                                    imageVector = Icons.Default.DeleteOutline,
                                    contentDescription = "Supprimer",
                                    tint = DarkTextMuted,
                                    modifier = Modifier.size(16.dp)
                                )
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(8.dp))

                    // Rating Stars
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        for (i in 1..5) {
                            Icon(
                                imageVector = Icons.Default.Star,
                                contentDescription = null,
                                tint = if (i <= comment.rating) BrandGold else DarkBorder,
                                modifier = Modifier.size(14.dp)
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(6.dp))

                    Text(
                        text = comment.content,
                        color = DarkTextPrimary,
                        fontSize = 13.sp,
                        lineHeight = 18.sp
                    )
                }
            }
        }

        // Section 4: Active Subscribers & Client Management Table
        item {
            Text(
                text = "Gestion des abonnés et sanctions",
                color = DarkTextPrimary,
                fontSize = 16.sp,
                fontWeight = FontWeight.Bold,
                modifier = Modifier.padding(top = 8.dp)
            )
        }

        items(adminClients, key = { it.id }) { client ->
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .testTag("admin_client_${client.id}"),
                colors = CardDefaults.cardColors(containerColor = DarkSurface),
                border = CardDefaults.outlinedCardBorder().copy(
                    brush = androidx.compose.ui.graphics.SolidColor(
                        if (client.isSuspended) StatusRed.copy(alpha = 0.5f) else DarkBorder
                    )
                ),
                shape = RoundedCornerShape(12.dp)
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(14.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Box(
                        modifier = Modifier
                            .size(10.dp)
                            .clip(CircleShape)
                            .background(
                                if (client.isSuspended) StatusRed else if (client.isOnline) StatusGreen else DarkTextMuted
                            )
                    )

                    Spacer(modifier = Modifier.width(10.dp))

                    Column(modifier = Modifier.weight(1f)) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Text(
                                text = client.name,
                                color = DarkTextPrimary,
                                fontWeight = FontWeight.Bold,
                                fontSize = 14.sp
                            )
                            Spacer(modifier = Modifier.width(6.dp))
                            Surface(
                                shape = RoundedCornerShape(4.dp),
                                color = BrandCyan.copy(alpha = 0.15f)
                            ) {
                                Text(
                                    text = client.planLabel,
                                    color = BrandCyan,
                                    fontSize = 10.sp,
                                    fontWeight = FontWeight.SemiBold,
                                    modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                                )
                            }
                        }

                        Spacer(modifier = Modifier.height(2.dp))

                        Text(
                            text = "Réf: ${client.id.uppercase()} • ${client.phone} • ${client.email}",
                            color = DarkTextMuted,
                            fontSize = 11.sp
                        )

                        Text(
                            text = if (client.isSuspended) "Statut : SUSPENDU PAR ADMIN" else "Expire le ${client.expiresAt}",
                            color = if (client.isSuspended) StatusRed else DarkTextSecondary,
                            fontSize = 11.sp,
                            fontWeight = if (client.isSuspended) FontWeight.Bold else FontWeight.Normal
                        )

                        Spacer(modifier = Modifier.height(2.dp))

                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(
                                imageVector = Icons.Default.Devices,
                                contentDescription = null,
                                tint = BrandCyan,
                                modifier = Modifier.size(12.dp)
                            )
                            Spacer(modifier = Modifier.width(4.dp))
                            Text(
                                text = "Connexions : 2/2 appareils simultanés max",
                                color = BrandCyan,
                                fontSize = 10.sp,
                                fontWeight = FontWeight.SemiBold
                            )
                        }

                        Spacer(modifier = Modifier.height(6.dp))

                        // Third-party Payment Access status & toggle
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(6.dp)
                        ) {
                            Surface(
                                shape = RoundedCornerShape(4.dp),
                                color = if (client.canPayForOthers) StatusGreen.copy(alpha = 0.15f) else StatusRed.copy(alpha = 0.15f)
                            ) {
                                Text(
                                    text = if (client.canPayForOthers) "Peye pou lòt : Otorize" else "Peye pou lòt : Bloke",
                                    color = if (client.canPayForOthers) StatusGreen else StatusRed,
                                    fontSize = 10.sp,
                                    fontWeight = FontWeight.Bold,
                                    modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                                )
                            }

                            TextButton(
                                onClick = { onToggleClientPayForOthers(client.id) },
                                contentPadding = PaddingValues(horizontal = 6.dp, vertical = 0.dp),
                                modifier = Modifier.height(26.dp)
                            ) {
                                Text(
                                    text = if (client.canPayForOthers) "Bloke aksè a" else "Bay aksè a",
                                    fontSize = 10.sp,
                                    color = if (client.canPayForOthers) StatusRed else StatusGreen,
                                    fontWeight = FontWeight.Bold
                                )
                            }

                            TextButton(
                                onClick = {
                                    recipientInput = client.phone.ifBlank { client.email }
                                    adminPayNoticeMessage = "Nimewo/email ${recipientInput} chwazi pou renouvèlman."
                                },
                                contentPadding = PaddingValues(horizontal = 6.dp, vertical = 0.dp),
                                modifier = Modifier.height(26.dp)
                            ) {
                                Text(
                                    text = "Peye pou li",
                                    fontSize = 10.sp,
                                    color = BrandGold,
                                    fontWeight = FontWeight.Bold
                                )
                            }
                        }
                    }

                    Button(
                        onClick = { onToggleSanction(client.id) },
                        colors = ButtonDefaults.buttonColors(
                            containerColor = if (client.isSuspended) StatusGreen else StatusRed
                        ),
                        shape = RoundedCornerShape(6.dp),
                        contentPadding = PaddingValues(horizontal = 10.dp, vertical = 6.dp),
                        modifier = Modifier.testTag("sanction_button_${client.id}")
                    ) {
                        Text(
                            text = if (client.isSuspended) "Rétablir" else "Sanctionner",
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Bold
                        )
                    }
                }
            }
        }
    }

    // Dialog for Custom Day Revenue Modification
    if (showCustomRevenueDialog) {
        AlertDialog(
            onDismissRequest = { showCustomRevenueDialog = false },
            title = {
                Text(
                    text = "Modifier le Revenu : $selectedDayForEdit",
                    fontWeight = FontWeight.Bold,
                    fontSize = 16.sp
                )
            },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                    Text(
                        text = "Choisissez le jour et saisissez le montant exact en HTG. Les rapports mensuel et annuel seront recalculés automatiquement.",
                        color = DarkTextSecondary,
                        fontSize = 12.sp
                    )

                    // Day selector chips
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(4.dp)
                    ) {
                        val days = listOf("Lun", "Mar", "Mer", "Jeu", "Ven", "Sam", "Aujourd'hui")
                        for (d in days) {
                            FilterChip(
                                selected = selectedDayForEdit == d,
                                onClick = {
                                    selectedDayForEdit = d
                                    val currentVal = dailyRevenues.find { it.dayLabel == d }?.amount ?: 4500.0
                                    inputAmountText = currentVal.toInt().toString()
                                },
                                label = { Text(if (d == "Aujourd'hui") "Auj." else d, fontSize = 10.sp) },
                                colors = FilterChipDefaults.filterChipColors(
                                    selectedContainerColor = BrandCyan,
                                    selectedLabelColor = Color.Black,
                                    containerColor = DarkSurfaceVariant,
                                    labelColor = DarkTextPrimary
                                )
                            )
                        }
                    }

                    OutlinedTextField(
                        value = inputAmountText,
                        onValueChange = { inputAmountText = it.filter { ch -> ch.isDigit() } },
                        label = { Text("Montant Journalier (HTG)") },
                        singleLine = true,
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                        modifier = Modifier.fillMaxWidth(),
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = BrandCyan,
                            unfocusedBorderColor = DarkBorder,
                            focusedTextColor = Color.White,
                            unfocusedTextColor = Color.White
                        )
                    )
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        val parsed = inputAmountText.toDoubleOrNull() ?: 0.0
                        onUpdateDailyRevenue(selectedDayForEdit, parsed)
                        showCustomRevenueDialog = false
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = StatusGreen)
                ) {
                    Text("Enregistrer & Recalculer", color = Color.White, fontWeight = FontWeight.Bold)
                }
            },
            dismissButton = {
                TextButton(onClick = { showCustomRevenueDialog = false }) {
                    Text("Annuler", color = DarkTextSecondary)
                }
            },
            containerColor = DarkSurface,
            titleContentColor = DarkTextPrimary
        )
    }
}

@Composable
fun MetricCard(
    title: String,
    value: String,
    subtitle: String,
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    accentColor: Color,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier,
        colors = CardDefaults.cardColors(containerColor = DarkSurface),
        border = CardDefaults.outlinedCardBorder().copy(
            brush = androidx.compose.ui.graphics.SolidColor(DarkBorder)
        ),
        shape = RoundedCornerShape(12.dp)
    ) {
        Column(modifier = Modifier.padding(12.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = title,
                    color = DarkTextMuted,
                    fontSize = 11.sp
                )
                Icon(
                    imageVector = icon,
                    contentDescription = null,
                    tint = accentColor,
                    modifier = Modifier.size(16.dp)
                )
            }

            Spacer(modifier = Modifier.height(6.dp))

            Text(
                text = value,
                color = DarkTextPrimary,
                fontWeight = FontWeight.Bold,
                fontSize = 15.sp
            )

            Text(
                text = subtitle,
                color = DarkTextSecondary,
                fontSize = 11.sp
            )
        }
    }
}
