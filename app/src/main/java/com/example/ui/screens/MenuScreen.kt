package com.example.ui.screens

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.*
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.model.ActiveSubscription
import com.example.data.model.UserAccount
import com.example.data.repository.SmartOneRepository
import com.example.ui.components.ColorARTRed
import com.example.ui.components.SmartOneBrandLogo
import com.example.ui.components.SmartOneFullBrandCard
import com.example.ui.components.SmartOneLogoSize
import com.example.ui.theme.*
import java.util.Locale

@Composable
fun MenuScreen(
    currentUser: UserAccount?,
    balance: Double,
    activeSubscription: ActiveSubscription?,
    notificationsCount: Int,
    selectedLanguage: String,
    onNavigate: (String) -> Unit,
    onSelectCategory: (String) -> Unit,
    onOpenProfile: () -> Unit,
    onOpenLanguagePicker: () -> Unit,
    onOpenNotifications: () -> Unit,
    onOpenDownloadApp: () -> Unit,
    onOpenFaq: () -> Unit,
    onOpenPrivacy: () -> Unit,
    onOpenFeedback: () -> Unit,
    onOpenAbout: () -> Unit,
    onOpenVideoSettings: () -> Unit = {},
    onLogout: () -> Unit,
    modifier: Modifier = Modifier
) {
    LazyColumn(
        modifier = modifier
            .fillMaxSize()
            .testTag("menu_screen"),
        contentPadding = PaddingValues(16.dp),
        verticalArrangement = Arrangement.spacedBy(14.dp)
    ) {
        // 0. Official SmartOne IPTV Full Brand Header
        item {
            SmartOneFullBrandCard(height = 110.dp)
        }

        // 1. Profile / Account Card
        item {
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(16.dp))
                    .clickable { onOpenProfile() }
                    .testTag("menu_profile_card"),
                colors = CardDefaults.cardColors(containerColor = LightSurface),
                border = CardDefaults.outlinedCardBorder().copy(
                    brush = androidx.compose.ui.graphics.SolidColor(LightBorder)
                ),
                elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Surface(
                            shape = CircleShape,
                            color = if (currentUser != null) BrandBluePrimary.copy(alpha = 0.15f) else LightSurfaceVariant,
                            border = BorderStroke(1.5.dp, if (currentUser != null) BrandBluePrimary else LightBorder),
                            modifier = Modifier.size(52.dp)
                        ) {
                            Box(contentAlignment = Alignment.Center) {
                                Icon(
                                    imageVector = if (currentUser != null) Icons.Default.Person else Icons.Default.PersonOutline,
                                    contentDescription = null,
                                    tint = if (currentUser != null) BrandBluePrimary else LightTextMuted,
                                    modifier = Modifier.size(28.dp)
                                )
                            }
                        }

                        Spacer(modifier = Modifier.width(14.dp))

                        Column(modifier = Modifier.weight(1f)) {
                            Text(
                                text = currentUser?.name ?: "Visiteur (Non connecté)",
                                color = LightTextPrimary,
                                fontWeight = FontWeight.Bold,
                                fontSize = 16.sp
                            )
                            Spacer(modifier = Modifier.height(2.dp))
                            Text(
                                text = if (currentUser != null) "Session sécurisée active" else "Touchez pour vous connecter",
                                color = if (currentUser != null) StatusGreen else LightTextMuted,
                                fontSize = 12.sp
                            )
                        }

                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.KeyboardArrowRight,
                            contentDescription = "Ouvrir",
                            tint = LightTextMuted,
                            modifier = Modifier.size(20.dp)
                        )
                    }

                    Spacer(modifier = Modifier.height(12.dp))
                    HorizontalDivider(thickness = 0.8.dp, color = LightBorder)
                    Spacer(modifier = Modifier.height(10.dp))

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column {
                            Text(
                                text = "Solde portefeuille",
                                color = LightTextMuted,
                                fontSize = 11.sp
                            )
                            Text(
                                text = "${String.format(Locale.US, "%,.2f", balance)} HTG",
                                color = BrandCyan,
                                fontWeight = FontWeight.Bold,
                                fontSize = 15.sp
                            )
                        }

                        Surface(
                            shape = RoundedCornerShape(20.dp),
                            color = if (activeSubscription != null) StatusGreen.copy(alpha = 0.12f) else BrandGoldLight,
                            border = BorderStroke(
                                1.dp,
                                if (activeSubscription != null) StatusGreen.copy(alpha = 0.4f) else BrandGold.copy(alpha = 0.4f)
                            )
                        ) {
                            Text(
                                text = if (activeSubscription != null) "VIP ACTIF" else "MODE GRATUIT",
                                color = if (activeSubscription != null) StatusGreen else BrandGoldDark,
                                fontSize = 10.sp,
                                fontWeight = FontWeight.Bold,
                                modifier = Modifier.padding(horizontal = 9.dp, vertical = 3.dp)
                            )
                        }
                    }
                }
            }
        }

        // 2. Main Hub Options
        item {
            Text(
                text = "Services Principaux",
                color = LightTextPrimary,
                fontSize = 14.sp,
                fontWeight = FontWeight.Bold,
                modifier = Modifier.padding(start = 4.dp, top = 2.dp)
            )
        }

        item {
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(14.dp),
                colors = CardDefaults.cardColors(containerColor = LightSurface),
                border = CardDefaults.outlinedCardBorder().copy(
                    brush = androidx.compose.ui.graphics.SolidColor(LightBorder)
                ),
                elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
            ) {
                Column {
                    MenuItemRow(
                        icon = Icons.AutoMirrored.Filled.ReceiptLong,
                        iconTint = BrandBluePrimary,
                        title = "Historique des Transactions",
                        subtitle = "Recharges, abonnements & preuves",
                        onClick = { onNavigate("history") }
                    )
                    HorizontalDivider(thickness = 0.5.dp, color = LightBorder, modifier = Modifier.padding(start = 56.dp))
                    MenuItemRow(
                        icon = Icons.Default.Notifications,
                        iconTint = StatusAmber,
                        title = "Notifications",
                        subtitle = "Alertes, diffusions & messages admin",
                        badge = if (notificationsCount > 0) "$notificationsCount" else null,
                        badgeColor = StatusRed,
                        onClick = onOpenNotifications
                    )
                    HorizontalDivider(thickness = 0.5.dp, color = LightBorder, modifier = Modifier.padding(start = 56.dp))
                    MenuItemRow(
                        icon = Icons.Default.Download,
                        iconTint = StatusGreen,
                        title = "Télécharger l'Application (APK)",
                        subtitle = "Installez la dernière version Android",
                        onClick = onOpenDownloadApp
                    )
                    HorizontalDivider(thickness = 0.5.dp, color = LightBorder, modifier = Modifier.padding(start = 56.dp))
                    MenuItemRow(
                        icon = Icons.Default.Tune,
                        iconTint = BrandBluePrimary,
                        title = "Luminosité & Contraste du Lecteur",
                        subtitle = "Régler la clarté et le contraste de l'écran",
                        onClick = onOpenVideoSettings
                    )
                    HorizontalDivider(thickness = 0.5.dp, color = LightBorder, modifier = Modifier.padding(start = 56.dp))
                    MenuItemRow(
                        icon = Icons.Default.Translate,
                        iconTint = BrandCyan,
                        title = "Langue de l'Application",
                        subtitle = "Actuel : $selectedLanguage",
                        onClick = onOpenLanguagePicker
                    )
                }
            }
        }

        // 3. Category Explorer Section
        item {
            Text(
                text = "Explorer par Catégorie",
                color = LightTextPrimary,
                fontSize = 14.sp,
                fontWeight = FontWeight.Bold,
                modifier = Modifier.padding(start = 4.dp, top = 4.dp)
            )
        }

        item {
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(14.dp),
                colors = CardDefaults.cardColors(containerColor = LightSurface),
                border = CardDefaults.outlinedCardBorder().copy(
                    brush = androidx.compose.ui.graphics.SolidColor(LightBorder)
                ),
                elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
            ) {
                Column(modifier = Modifier.padding(12.dp)) {
                    val categories = listOf(
                        "Toutes" to "📺",
                        "Sport" to "⚽",
                        "Cinéma" to "🎬",
                        "Infos" to "📰",
                        "Musique" to "🎵",
                        "Jeunesse" to "🧸",
                        "Haïti" to "🇭🇹",
                        "Monde" to "🌍"
                    )

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        categories.take(4).forEach { (cat, emoji) ->
                            Surface(
                                shape = RoundedCornerShape(10.dp),
                                color = LightSurfaceVariant,
                                border = BorderStroke(1.dp, LightBorder),
                                modifier = Modifier
                                    .weight(1f)
                                    .clickable {
                                        onSelectCategory(cat)
                                        onNavigate("home")
                                    }
                            ) {
                                Column(
                                    modifier = Modifier.padding(vertical = 10.dp, horizontal = 4.dp),
                                    horizontalAlignment = Alignment.CenterHorizontally
                                ) {
                                    Text(text = emoji, fontSize = 18.sp)
                                    Spacer(modifier = Modifier.height(3.dp))
                                    Text(
                                        text = cat,
                                        fontSize = 11.sp,
                                        fontWeight = FontWeight.SemiBold,
                                        color = LightTextPrimary,
                                        maxLines = 1
                                    )
                                }
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(8.dp))

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        categories.drop(4).forEach { (cat, emoji) ->
                            Surface(
                                shape = RoundedCornerShape(10.dp),
                                color = LightSurfaceVariant,
                                border = BorderStroke(1.dp, LightBorder),
                                modifier = Modifier
                                    .weight(1f)
                                    .clickable {
                                        onSelectCategory(cat)
                                        onNavigate("home")
                                    }
                            ) {
                                Column(
                                    modifier = Modifier.padding(vertical = 10.dp, horizontal = 4.dp),
                                    horizontalAlignment = Alignment.CenterHorizontally
                                ) {
                                    Text(text = emoji, fontSize = 18.sp)
                                    Spacer(modifier = Modifier.height(3.dp))
                                    Text(
                                        text = cat,
                                        fontSize = 11.sp,
                                        fontWeight = FontWeight.SemiBold,
                                        color = LightTextPrimary,
                                        maxLines = 1
                                    )
                                }
                            }
                        }
                    }
                }
            }
        }

        // 4. Administration Section (Visible ONLY to verified Admin accounts)
        if (currentUser?.isAdmin == true) {
            item {
                Text(
                    text = "Gestion & Contrôle",
                    color = LightTextPrimary,
                    fontSize = 14.sp,
                    fontWeight = FontWeight.Bold,
                    modifier = Modifier.padding(start = 4.dp, top = 4.dp)
                )
            }

            item {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(14.dp),
                    colors = CardDefaults.cardColors(containerColor = LightSurface),
                    border = CardDefaults.outlinedCardBorder().copy(
                        brush = androidx.compose.ui.graphics.SolidColor(LightBorder)
                    ),
                    elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
                ) {
                    val isBlocked = com.example.data.repository.SmartOneRepository.isAdminPanelBlocked.value
                    MenuItemRow(
                        icon = if (isBlocked) Icons.Default.Lock else Icons.Default.AdminPanelSettings,
                        iconTint = ColorARTRed,
                        title = if (isBlocked) "Panneau d'Administration (Bloke)" else "Panneau d'Administration",
                        subtitle = if (isBlocked) "Aksè a bloke pou rezon sekirite pa Pwopriyetè a" else "Clients, revenus, validation des paiements & sanctions",
                        badge = if (isBlocked) "BLOKÉ" else "ADMIN",
                        badgeColor = ColorARTRed,
                        onClick = { onNavigate("admin") }
                    )
                }
            }
        }


        // 5. Support & Legal Section
        item {
            Text(
                text = "Assistance & Informations",
                color = LightTextPrimary,
                fontSize = 14.sp,
                fontWeight = FontWeight.Bold,
                modifier = Modifier.padding(start = 4.dp, top = 4.dp)
            )
        }

        item {
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(14.dp),
                colors = CardDefaults.cardColors(containerColor = LightSurface),
                border = CardDefaults.outlinedCardBorder().copy(
                    brush = androidx.compose.ui.graphics.SolidColor(LightBorder)
                ),
                elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
            ) {
                Column {
                    MenuItemRow(
                        icon = Icons.Default.HelpOutline,
                        iconTint = BrandBluePrimary,
                        title = "Questions Fréquentes (FAQ)",
                        subtitle = "Aide à l'utilisation, flux et abonnements",
                        onClick = onOpenFaq
                    )
                    HorizontalDivider(thickness = 0.5.dp, color = LightBorder, modifier = Modifier.padding(start = 56.dp))
                    MenuItemRow(
                        icon = Icons.Default.Security,
                        iconTint = StatusGreen,
                        title = "Confidentialité & Sécurité",
                        subtitle = "Protection des données et conditions",
                        onClick = onOpenPrivacy
                    )
                    HorizontalDivider(thickness = 0.5.dp, color = LightBorder, modifier = Modifier.padding(start = 56.dp))
                    MenuItemRow(
                        icon = Icons.Default.Feedback,
                        iconTint = StatusAmber,
                        title = "Signaler un Problème / Avis",
                        subtitle = "Envoyez votre retour directement à l'équipe",
                        onClick = onOpenFeedback
                    )
                    HorizontalDivider(thickness = 0.5.dp, color = LightBorder, modifier = Modifier.padding(start = 56.dp))
                    MenuItemRow(
                        icon = Icons.Default.Info,
                        iconTint = BrandCyan,
                        title = "À Propos de SmartOne IPTV",
                        subtitle = "Version, licence et mentions légales",
                        onClick = onOpenAbout
                    )
                }
            }
        }

        // 6. Sign Out Button if Logged In
        if (currentUser != null) {
            item {
                OutlinedButton(
                    onClick = onLogout,
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(top = 4.dp),
                    shape = RoundedCornerShape(12.dp),
                    colors = ButtonDefaults.outlinedButtonColors(contentColor = StatusRed),
                    border = BorderStroke(1.dp, StatusRed.copy(alpha = 0.5f))
                ) {
                    Icon(
                        imageVector = Icons.AutoMirrored.Filled.Logout,
                        contentDescription = "Déconnexion",
                        tint = StatusRed,
                        modifier = Modifier.size(18.dp)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = "Se Déconnecter du Compte",
                        fontSize = 13.sp,
                        fontWeight = FontWeight.Bold,
                        color = StatusRed
                    )
                }
            }
        }

        // 7. Footer Branding
        item {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 12.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                SmartOneBrandLogo(size = SmartOneLogoSize.SMALL)
                Spacer(modifier = Modifier.height(6.dp))
                Text(
                    text = "SmartOne IPTV v23.0 • Plateforme Officielle",
                    fontSize = 11.sp,
                    color = LightTextMuted,
                    fontWeight = FontWeight.Medium
                )
            }
        }
    }
}

@Composable
private fun MenuItemRow(
    icon: ImageVector,
    iconTint: Color,
    title: String,
    subtitle: String,
    badge: String? = null,
    badgeColor: Color = BrandBluePrimary,
    onClick: () -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick)
            .padding(horizontal = 14.dp, vertical = 12.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Surface(
            shape = RoundedCornerShape(10.dp),
            color = iconTint.copy(alpha = 0.12f),
            modifier = Modifier.size(38.dp)
        ) {
            Box(contentAlignment = Alignment.Center) {
                Icon(
                    imageVector = icon,
                    contentDescription = null,
                    tint = iconTint,
                    modifier = Modifier.size(20.dp)
                )
            }
        }

        Spacer(modifier = Modifier.width(14.dp))

        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = title,
                fontSize = 14.sp,
                fontWeight = FontWeight.SemiBold,
                color = LightTextPrimary
            )
            Text(
                text = subtitle,
                fontSize = 11.5.sp,
                color = LightTextMuted,
                maxLines = 1
            )
        }

        if (badge != null) {
            Surface(
                shape = RoundedCornerShape(12.dp),
                color = badgeColor
            ) {
                Text(
                    text = badge,
                    color = Color.White,
                    fontSize = 10.sp,
                    fontWeight = FontWeight.Bold,
                    modifier = Modifier.padding(horizontal = 7.dp, vertical = 2.dp)
                )
            }
            Spacer(modifier = Modifier.width(8.dp))
        }

        Icon(
            imageVector = Icons.AutoMirrored.Filled.KeyboardArrowRight,
            contentDescription = null,
            tint = LightTextMuted,
            modifier = Modifier.size(18.dp)
        )
    }
}
