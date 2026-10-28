package com.example.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.CreditCard
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.PersonAdd
import androidx.compose.material.icons.filled.Share
import androidx.compose.material.icons.filled.Star
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.model.ActiveSubscription
import com.example.data.model.SubscriptionPlan
import com.example.data.repository.SmartOneRepository
import com.example.ui.components.*
import com.example.ui.theme.*
import java.util.Locale

@Composable
fun PlansScreen(
    balance: Double,
    activeSubscription: ActiveSubscription?,
    freeSecondsLeft: Int,
    onActivatePlan: (SubscriptionPlan) -> Unit,
    onNavigateToDeposit: () -> Unit,
    onPayForOther: (String, SubscriptionPlan) -> Boolean = { _, _ -> false },
    canPayForOthers: Boolean = true,
    modifier: Modifier = Modifier
) {
    var selectedPlanToConfirm by remember { mutableStateOf<SubscriptionPlan?>(null) }
    var isPayingForOther by remember { mutableStateOf(false) }
    var thirdPartyRecipient by remember { mutableStateOf("") }
    var recipientError by remember { mutableStateOf<String?>(null) }

    LazyColumn(
        modifier = modifier.fillMaxSize(),
        contentPadding = PaddingValues(16.dp),
        verticalArrangement = Arrangement.spacedBy(14.dp)
    ) {
        item {
            // Free Timer / Status banner
            FreeTimerBanner(
                activeSubscription = activeSubscription,
                freeSecondsLeft = freeSecondsLeft,
                onOpenPlans = {}
            )
        }

        // Official SmartOne IPTV Full Brand Card
        item {
            SmartOneFullBrandCard(height = 120.dp)
        }

        // Wallet Balance Card
        item {
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .testTag("plans_wallet_card"),
                colors = CardDefaults.cardColors(containerColor = DarkSurface),
                border = CardDefaults.outlinedCardBorder().copy(
                    brush = androidx.compose.ui.graphics.SolidColor(DarkBorder)
                )
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(14.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column {
                        Text(
                            text = "Solde disponible",
                            color = DarkTextMuted,
                            fontSize = 12.sp
                        )
                        Text(
                            text = "${String.format(Locale.US, "%,.2f", balance)} HTG",
                            color = BrandCyan,
                            fontSize = 20.sp,
                            fontWeight = FontWeight.Bold
                        )
                        Text(
                            text = "≈ $${String.format(Locale.US, "%.2f", balance / SmartOneRepository.USD_RATE)} · €${String.format(Locale.US, "%.2f", balance / SmartOneRepository.EUR_RATE)}",
                            color = DarkTextSecondary,
                            fontSize = 12.sp
                        )
                    }

                    Button(
                        onClick = onNavigateToDeposit,
                        colors = ButtonDefaults.buttonColors(containerColor = BrandBluePrimary),
                        shape = RoundedCornerShape(8.dp),
                        modifier = Modifier.testTag("plans_deposit_shortcut")
                    ) {
                        Text("Recharger")
                    }
                }
            }
        }

        // Beneficiary Mode Selector (Pou mwen menm vs Peye pou yon lòt moun)
        item {
            Card(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(containerColor = DarkSurface),
                border = CardDefaults.outlinedCardBorder().copy(
                    brush = androidx.compose.ui.graphics.SolidColor(
                        if (isPayingForOther) BrandCyan.copy(alpha = 0.6f) else DarkBorder
                    )
                )
            ) {
                Column(modifier = Modifier.padding(14.dp)) {
                    Text(
                        text = "Benefisyè Abònman an",
                        color = DarkTextPrimary,
                        fontSize = 14.sp,
                        fontWeight = FontWeight.Bold
                    )
                    Spacer(modifier = Modifier.height(10.dp))
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        FilterChip(
                            selected = !isPayingForOther,
                            onClick = { isPayingForOther = false; recipientError = null },
                            label = { Text("Pou mwen menm", fontSize = 12.sp) },
                            leadingIcon = {
                                Icon(Icons.Default.Person, contentDescription = null, modifier = Modifier.size(16.dp))
                            },
                            colors = FilterChipDefaults.filterChipColors(
                                selectedContainerColor = BrandCyan,
                                selectedLabelColor = Color.Black,
                                containerColor = DarkSurfaceVariant,
                                labelColor = DarkTextPrimary
                            ),
                            modifier = Modifier.weight(1f)
                        )

                        FilterChip(
                            selected = isPayingForOther,
                            onClick = { isPayingForOther = true },
                            label = { Text("Peye pou yon lòt", fontSize = 12.sp) },
                            leadingIcon = {
                                Icon(Icons.Default.PersonAdd, contentDescription = null, modifier = Modifier.size(16.dp))
                            },
                            colors = FilterChipDefaults.filterChipColors(
                                selectedContainerColor = BrandGold,
                                selectedLabelColor = Color.Black,
                                containerColor = DarkSurfaceVariant,
                                labelColor = DarkTextPrimary
                            ),
                            modifier = Modifier.weight(1f)
                        )
                    }

                    if (isPayingForOther) {
                        Spacer(modifier = Modifier.height(12.dp))
                        if (!canPayForOthers) {
                            Surface(
                                shape = RoundedCornerShape(8.dp),
                                color = StatusRed.copy(alpha = 0.15f),
                                modifier = Modifier.fillMaxWidth()
                            ) {
                                Row(
                                    modifier = Modifier.padding(10.dp),
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Icon(Icons.Default.Lock, contentDescription = null, tint = StatusRed, modifier = Modifier.size(18.dp))
                                    Spacer(modifier = Modifier.width(8.dp))
                                    Text(
                                        text = "Aksè pou peye pou yon lòt kliyan bloke pa Administratè a pou kont sa a. Kontakte sipò a.",
                                        color = StatusRed,
                                        fontSize = 12.sp
                                    )
                                }
                            }
                        } else {
                            Text(
                                text = "Antre email oubyen nimewo telefòn kont lòt moun lan pou aktive abònman an dirèkteman sou kont li :",
                                color = DarkTextSecondary,
                                fontSize = 12.sp
                            )
                            Spacer(modifier = Modifier.height(8.dp))
                            OutlinedTextField(
                                value = thirdPartyRecipient,
                                onValueChange = {
                                    thirdPartyRecipient = it
                                    if (recipientError != null) recipientError = null
                                },
                                label = { Text("Email oswa Nimewo Telefòn (+509...)") },
                                placeholder = { Text("+509 3215 3281 oswa kliyan@gmail.com", color = DarkTextMuted) },
                                isError = recipientError != null,
                                singleLine = true,
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .testTag("third_party_recipient_input"),
                                colors = OutlinedTextFieldDefaults.colors(
                                    focusedBorderColor = BrandGold,
                                    unfocusedBorderColor = DarkBorder,
                                    focusedTextColor = Color.White,
                                    unfocusedTextColor = Color.White,
                                    errorBorderColor = StatusRed
                                )
                            )
                            if (recipientError != null) {
                                Text(
                                    text = recipientError ?: "",
                                    color = StatusRed,
                                    fontSize = 11.sp,
                                    modifier = Modifier.padding(start = 4.dp, top = 4.dp)
                                )
                            }
                        }
                    }
                }
            }
        }

        // Info notice about auto-cancellation
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
                        imageVector = Icons.Default.Info,
                        contentDescription = null,
                        tint = BrandGold,
                        modifier = Modifier.size(20.dp)
                    )
                    Spacer(modifier = Modifier.width(10.dp))
                    Text(
                        text = if (isPayingForOther) {
                            "Lè w peye pou yon lòt moun, abònman an ap aktive imedyatman sou kont li (2 aparèy an menm tan max)."
                        } else {
                            "Activer un nouvel abonnement remplace et annule automatiquement le plan précédent en cours."
                        },
                        color = DarkTextSecondary,
                        fontSize = 12.sp,
                        lineHeight = 16.sp
                    )
                }
            }
        }

        item {
            Text(
                text = if (isPayingForOther) "Chwazi plan pou lòt moun lan" else "Choisissez votre formule VIP",
                color = DarkTextPrimary,
                fontSize = 18.sp,
                fontWeight = FontWeight.Bold,
                modifier = Modifier.padding(top = 4.dp)
            )
        }

        // List of 4 Subscription Plans
        items(SmartOneRepository.subscriptionPlans, key = { it.id }) { plan ->
            val isCurrentPlan = activeSubscription?.planId == plan.id
            val isPopular = plan.id == "plan_6m" || plan.id == "plan_1y"

            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(16.dp))
                    .testTag("plan_card_${plan.id}"),
                colors = CardDefaults.cardColors(containerColor = DarkSurface),
                border = CardDefaults.outlinedCardBorder().copy(
                    brush = androidx.compose.ui.graphics.SolidColor(
                        if (isCurrentPlan) StatusGreen else if (isPopular) BrandGold else DarkBorder
                    )
                )
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Text(
                                text = plan.label,
                                color = DarkTextPrimary,
                                fontSize = 18.sp,
                                fontWeight = FontWeight.Bold
                            )
                            if (isPopular) {
                                Spacer(modifier = Modifier.width(8.dp))
                                Surface(
                                    shape = RoundedCornerShape(12.dp),
                                    color = BrandGold.copy(alpha = 0.2f)
                                ) {
                                    Text(
                                        text = if (plan.id == "plan_1y") "MEILLEURE OFFRE" else "POPULAIRE",
                                        color = BrandGold,
                                        fontSize = 10.sp,
                                        fontWeight = FontWeight.Bold,
                                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 2.dp)
                                    )
                                }
                            }
                        }

                        if (isCurrentPlan) {
                            Surface(
                                shape = RoundedCornerShape(12.dp),
                                color = StatusGreen.copy(alpha = 0.2f)
                            ) {
                                Text(
                                    text = "ACTIF",
                                    color = StatusGreen,
                                    fontSize = 11.sp,
                                    fontWeight = FontWeight.Bold,
                                    modifier = Modifier.padding(horizontal = 8.dp, vertical = 3.dp)
                                )
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(8.dp))

                    // Price display
                    Row(verticalAlignment = Alignment.Bottom) {
                        Text(
                            text = "${String.format(Locale.US, "%,.0f", plan.priceGds)} HTG",
                            color = BrandCyan,
                            fontSize = 24.sp,
                            fontWeight = FontWeight.ExtraBold
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = "≈ $${String.format(Locale.US, "%.2f", plan.priceUsd)} · €${String.format(Locale.US, "%.2f", plan.priceEur)}",
                            color = DarkTextMuted,
                            fontSize = 13.sp,
                            modifier = Modifier.padding(bottom = 2.dp)
                        )
                    }

                    Spacer(modifier = Modifier.height(12.dp))

                    // Feature highlights
                    val features = listOf(
                        "Connexion sur 2 appareils en même temps",
                        "Accès illimité 24/7 à toutes les chaînes",
                        "Qualité 4K Ultra HD & 1080p 60fps",
                        "Zéro publicité & streaming fluide",
                        "Support prioritaire dédié"
                    )

                    for (f in features) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            modifier = Modifier.padding(vertical = 2.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Default.Check,
                                contentDescription = null,
                                tint = StatusGreen,
                                modifier = Modifier.size(16.dp)
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(
                                text = f,
                                color = DarkTextSecondary,
                                fontSize = 12.sp
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(14.dp))

                    Button(
                        onClick = {
                            if (isPayingForOther) {
                                if (thirdPartyRecipient.isBlank()) {
                                    recipientError = "Tanpri antre email oubyen nimewo telefòn benefisyè a anvan."
                                    return@Button
                                }
                                if (!canPayForOthers) {
                                    recipientError = "Aksè sa a bloke pa administratè a."
                                    return@Button
                                }
                            }
                            selectedPlanToConfirm = plan
                        },
                        colors = ButtonDefaults.buttonColors(
                            containerColor = if (isPayingForOther) BrandGoldDark else if (isCurrentPlan) DarkSurfaceVariant else if (isPopular) BrandGoldDark else BrandBluePrimary
                        ),
                        modifier = Modifier
                            .fillMaxWidth()
                            .testTag("activate_plan_button_${plan.id}"),
                        shape = RoundedCornerShape(10.dp)
                    ) {
                        Text(
                            text = if (isPayingForOther) {
                                "Peye pou moun sa a (${String.format(Locale.US, "%,.0f", plan.priceGds)} HTG)"
                            } else if (isCurrentPlan) {
                                "Renouveler ce plan"
                            } else {
                                "Activer (${String.format(Locale.US, "%,.0f", plan.priceGds)} HTG)"
                            },
                            color = if (isPayingForOther || isPopular) Color.Black else Color.White,
                            fontWeight = FontWeight.Bold
                        )
                    }
                }
            }
        }
    }

    // Confirmation Dialog
    if (selectedPlanToConfirm != null) {
        val plan = selectedPlanToConfirm!!
        val hasEnoughFunds = balance >= plan.priceGds

        AlertDialog(
            onDismissRequest = { selectedPlanToConfirm = null },
            title = {
                Text(
                    text = if (isPayingForOther) "Konfime Pèyman pou yon Lòt Kliyan" else "Confirmer l'abonnement",
                    fontWeight = FontWeight.Bold
                )
            },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    if (isPayingForOther) {
                        Text(
                            text = "Èske w vle peye abònman ${plan.label} (${String.format(Locale.US, "%,.0f", plan.priceGds)} HTG) pou :",
                            color = DarkTextPrimary
                        )
                        Surface(
                            shape = RoundedCornerShape(6.dp),
                            color = BrandCyan.copy(alpha = 0.15f),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Text(
                                text = thirdPartyRecipient,
                                color = BrandCyan,
                                fontWeight = FontWeight.Bold,
                                fontSize = 14.sp,
                                modifier = Modifier.padding(8.dp)
                            )
                        }
                        Text(
                            text = "Montant sa a ap dedwi dirèkteman sou solde ou epi abònman an ap aktive imedyatman pou kont kliyan sa a (max 2 aparèy konekte an menm tan).",
                            color = DarkTextSecondary,
                            fontSize = 12.sp
                        )
                    } else {
                        Text(
                            text = "Voulez-vous souscrire au plan ${plan.label} pour ${String.format(Locale.US, "%,.0f", plan.priceGds)} HTG ?",
                            color = DarkTextPrimary
                        )
                        if (activeSubscription != null) {
                            Text(
                                text = "Attention : Votre plan actuel (${activeSubscription.label}) sera annulé automatiquement et remplacé par ce nouveau plan.",
                                color = BrandGold,
                                fontSize = 12.sp
                            )
                        }
                    }

                    if (!hasEnoughFunds) {
                        Text(
                            text = "Solde insuffisant ! Votre solde est de ${String.format(Locale.US, "%,.2f", balance)} HTG. Veuillez effectuer un dépôt d'abord.",
                            color = StatusRed,
                            fontSize = 13.sp,
                            fontWeight = FontWeight.SemiBold
                        )
                    }
                }
            },
            confirmButton = {
                if (hasEnoughFunds) {
                    Button(
                        onClick = {
                            if (isPayingForOther) {
                                val ok = onPayForOther(thirdPartyRecipient, plan)
                                if (ok) {
                                    thirdPartyRecipient = ""
                                    isPayingForOther = false
                                }
                            } else {
                                onActivatePlan(plan)
                            }
                            selectedPlanToConfirm = null
                        },
                        colors = ButtonDefaults.buttonColors(
                            containerColor = if (isPayingForOther) BrandGoldDark else BrandBluePrimary
                        )
                    ) {
                        Text(
                            text = if (isPayingForOther) "Valide & Peye Kounye a" else "Confirmer l'activation",
                            color = if (isPayingForOther) Color.Black else Color.White,
                            fontWeight = FontWeight.Bold
                        )
                    }
                } else {
                    Button(
                        onClick = {
                            selectedPlanToConfirm = null
                            onNavigateToDeposit()
                        },
                        colors = ButtonDefaults.buttonColors(containerColor = BrandBluePrimary)
                    ) {
                        Text("Faire un dépôt")
                    }
                }
            },
            dismissButton = {
                TextButton(onClick = { selectedPlanToConfirm = null }) {
                    Text("Annuler", color = DarkTextSecondary)
                }
            },
            containerColor = DarkSurface,
            titleContentColor = DarkTextPrimary,
            textContentColor = DarkTextPrimary
        )
    }
}
