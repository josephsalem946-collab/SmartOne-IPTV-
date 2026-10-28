package com.example.ui.components

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
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.data.model.UserAccount
import com.example.data.network.NetworkStatusMonitor
import com.example.data.repository.SmartOneRepository
import com.example.ui.theme.*

@Composable
fun AppDrawer(
    currentUser: UserAccount?,
    selectedNav: String,
    onNavigate: (String) -> Unit,
    onSelectCategory: (String) -> Unit,
    onOpenProfile: () -> Unit,
    onOpenLanguagePicker: () -> Unit,
    onOpenAdmin: () -> Unit,
    onOpenAbout: () -> Unit,
    onOpenDownloadApp: () -> Unit,
    onOpenFaq: () -> Unit,
    onOpenPrivacy: () -> Unit,
    onOpenFeedback: () -> Unit,
    modifier: Modifier = Modifier
) {
    ModalDrawerSheet(
        modifier = modifier.width(310.dp),
        drawerContainerColor = DarkSurface,
        drawerContentColor = DarkTextPrimary
    ) {
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(vertical = 12.dp),
            contentPadding = PaddingValues(bottom = 24.dp)
        ) {
            // Official Brand Logo Banner
            item {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 14.dp, vertical = 6.dp)
                ) {
                    SmartOneFullBrandCard(height = 135.dp)
                }
            }

            // Profile Card Header
            item {
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 14.dp, vertical = 6.dp)
                        .clip(RoundedCornerShape(12.dp))
                        .clickable { onOpenProfile() }
                        .testTag("drawer_profile_header"),
                    colors = CardDefaults.cardColors(containerColor = DarkSurfaceVariant)
                ) {
                    Row(
                        modifier = Modifier.padding(14.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Box(
                            modifier = Modifier
                                .size(46.dp)
                                .clip(CircleShape)
                                .background(BrandBluePrimary.copy(alpha = 0.2f)),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                imageVector = if (currentUser != null) Icons.Default.Person else Icons.Default.PersonOutline,
                                contentDescription = null,
                                tint = BrandCyan,
                                modifier = Modifier.size(26.dp)
                            )
                        }

                        Spacer(modifier = Modifier.width(12.dp))

                        Column {
                            Text(
                                text = currentUser?.name ?: "Visiteur",
                                color = DarkTextPrimary,
                                fontWeight = FontWeight.Bold,
                                fontSize = 16.sp
                            )
                            Text(
                                text = if (currentUser != null) "Session sécurisée active" else "Connectez-vous pour synchroniser",
                                color = if (currentUser != null) StatusGreen else DarkTextSecondary,
                                fontSize = 12.sp
                            )
                        }
                    }
                }

                HorizontalDivider(
                    modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp),
                    color = DarkBorder
                )
            }

            // Main Navigation Items
            item {
                DrawerNavItem("home", "Toutes les chaînes", Icons.Default.Tv, selectedNav == "home") { onNavigate("home") }
                DrawerNavItem("fav", "Favoris", Icons.Default.Star, selectedNav == "fav") { onNavigate("fav") }
                DrawerNavItem("plans", "Abonnements", Icons.Default.CreditCard, selectedNav == "plans") { onNavigate("plans") }
                DrawerNavItem("deposit", "Dépôt (Wallet)", Icons.Default.AccountBalanceWallet, selectedNav == "deposit") { onNavigate("deposit") }
                DrawerNavItem("history", "Historique", Icons.AutoMirrored.Filled.ReceiptLong, selectedNav == "history") { onNavigate("history") }
                DrawerNavItem("download_app", "Télécharger l'APK", Icons.Default.Download, false) { onOpenDownloadApp() }
                DrawerNavItem("about", "À propos", Icons.Default.Info, selectedNav == "about") { onOpenAbout() }

                HorizontalDivider(
                    modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp),
                    color = DarkBorder
                )
            }

            // Explorer Categories Section
            item {
                Text(
                    text = "Explorer les catégories",
                    color = DarkTextMuted,
                    fontSize = 12.sp,
                    fontWeight = FontWeight.Bold,
                    modifier = Modifier.padding(horizontal = 16.dp, vertical = 6.dp)
                )

                val sampleCategories = listOf("Top News", "News", "Sports", "Movies", "Music", "Kids", "Documentary", "Science", "Technology", "Gaming")
                for (cat in sampleCategories) {
                    NavigationDrawerItem(
                        label = { Text(cat, fontSize = 14.sp) },
                        selected = false,
                        onClick = { onSelectCategory(cat) },
                        modifier = Modifier.padding(horizontal = 12.dp, vertical = 2.dp),
                        colors = NavigationDrawerItemDefaults.colors(
                            unselectedTextColor = DarkTextSecondary,
                            unselectedContainerColor = Color.Transparent
                        )
                    )
                }

                HorizontalDivider(
                    modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp),
                    color = DarkBorder
                )
            }

            // Settings & Languages
            item {
                Text(
                    text = "Préférences & Langue",
                    color = DarkTextMuted,
                    fontSize = 12.sp,
                    fontWeight = FontWeight.Bold,
                    modifier = Modifier.padding(horizontal = 16.dp, vertical = 6.dp)
                )

                DrawerNavItem("lang", "Langue (${SmartOneRepository.selectedLanguage.value})", Icons.Default.Language, false) {
                    onOpenLanguagePicker()
                }

                HorizontalDivider(
                    modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp),
                    color = DarkBorder
                )
            }

            // Réseau & Performance Faible Débit
            item {
                val netStatus by NetworkStatusMonitor.networkStatus.collectAsStateWithLifecycle()
                Text(
                    text = "Réseau & Performance",
                    color = DarkTextMuted,
                    fontSize = 12.sp,
                    fontWeight = FontWeight.Bold,
                    modifier = Modifier.padding(horizontal = 16.dp, vertical = 6.dp)
                )

                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 14.dp, vertical = 4.dp),
                    colors = CardDefaults.cardColors(containerColor = DarkSurfaceVariant),
                    shape = RoundedCornerShape(10.dp)
                ) {
                    Column(modifier = Modifier.padding(12.dp)) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Row(
                                modifier = Modifier.weight(1f),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Icon(
                                    imageVector = if (netStatus.isWeak) Icons.Default.SignalCellularAlt else Icons.Default.Wifi,
                                    contentDescription = null,
                                    tint = if (netStatus.isWeak) StatusAmber else StatusGreen,
                                    modifier = Modifier.size(20.dp)
                                )
                                Spacer(modifier = Modifier.width(10.dp))
                                Column {
                                    Text(
                                        text = "Mode Réseau Faible",
                                        color = DarkTextPrimary,
                                        fontSize = 13.sp,
                                        fontWeight = FontWeight.Bold
                                    )
                                    Text(
                                        text = if (netStatus.isLowDataModeEnabled) "Activé (Diffusion 480p/360p sans coupure)" else "Détection auto (${netStatus.connectionType})",
                                        color = if (netStatus.isLowDataModeEnabled) StatusAmber else DarkTextSecondary,
                                        fontSize = 10.sp
                                    )
                                }
                            }
                            Switch(
                                checked = netStatus.isLowDataModeEnabled,
                                onCheckedChange = { NetworkStatusMonitor.toggleLowDataMode(it) },
                                colors = SwitchDefaults.colors(
                                    checkedThumbColor = Color.White,
                                    checkedTrackColor = BrandCyan
                                )
                            )
                        }
                    }
                }

                HorizontalDivider(
                    modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp),
                    color = DarkBorder
                )
            }

            // Informations
            item {
                Text(
                    text = "Informations",
                    color = DarkTextMuted,
                    fontSize = 12.sp,
                    fontWeight = FontWeight.Bold,
                    modifier = Modifier.padding(horizontal = 16.dp, vertical = 6.dp)
                )

                DrawerNavItem("faq", "FAQ & Aide", Icons.AutoMirrored.Filled.HelpOutline, false) { onOpenFaq() }
                DrawerNavItem("privacy", "Politique de confidentialité", Icons.Default.Security, false) { onOpenPrivacy() }
                DrawerNavItem("feedback", "Commentaires", Icons.Default.RateReview, false) { onOpenFeedback() }

                HorizontalDivider(
                    modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp),
                    color = DarkBorder
                )
            }

            // Administration
            item {
                Text(
                    text = "Administration",
                    color = DarkTextMuted,
                    fontSize = 12.sp,
                    fontWeight = FontWeight.Bold,
                    modifier = Modifier.padding(horizontal = 16.dp, vertical = 6.dp)
                )

                DrawerNavItem("admin", "🛡️ Panneau Administrateur", Icons.Default.AdminPanelSettings, selectedNav == "admin") {
                    onOpenAdmin()
                }

                Spacer(modifier = Modifier.height(12.dp))

                // Support footer
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp, vertical = 4.dp)
                ) {
                    Text(
                        text = "Support Disponible",
                        color = DarkTextMuted,
                        fontSize = 11.sp
                    )
                    Text(
                        text = SmartOneRepository.SUPPORT_EMAIL,
                        color = BrandCyan,
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Medium
                    )
                }
            }
        }
    }
}

@Composable
fun DrawerNavItem(
    id: String,
    label: String,
    icon: ImageVector,
    selected: Boolean,
    onClick: () -> Unit
) {
    NavigationDrawerItem(
        icon = { Icon(imageVector = icon, contentDescription = null, tint = if (selected) BrandCyan else DarkTextSecondary) },
        label = { Text(label, fontSize = 14.sp, fontWeight = if (selected) FontWeight.Bold else FontWeight.Normal) },
        selected = selected,
        onClick = onClick,
        modifier = Modifier
            .padding(horizontal = 12.dp, vertical = 2.dp)
            .testTag("nav_item_$id"),
        colors = NavigationDrawerItemDefaults.colors(
            selectedContainerColor = BrandBluePrimary.copy(alpha = 0.2f),
            selectedTextColor = BrandCyan,
            unselectedContainerColor = Color.Transparent,
            unselectedTextColor = DarkTextPrimary
        ),
        shape = RoundedCornerShape(8.dp)
    )
}
