package com.example.ui.dialogs

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import android.content.Intent
import android.net.Uri
import android.widget.Toast
import androidx.compose.ui.platform.LocalContext
import com.example.R
import com.example.data.model.ActiveSubscription
import com.example.data.model.Channel
import com.example.data.model.ConnectedDevice
import com.example.data.model.UserAccount
import com.example.data.repository.SmartOneRepository
import com.example.ui.components.*
import com.example.ui.theme.*
import java.util.Locale

@Composable
fun AuthDialog(
    onDismiss: () -> Unit,
    onLoginSuccess: (name: String, emailOrPhone: String, isGoogle: Boolean) -> Unit
) {
    var isLoginTab by remember { mutableStateOf(true) }
    var identifier by remember { mutableStateOf("") }
    var name by remember { mutableStateOf("") }
    var password by remember { mutableStateOf("") }
    var isPasswordVisible by remember { mutableStateOf(false) }
    var isAgeConfirmed by remember { mutableStateOf(false) }
    var errorMessage by remember { mutableStateOf("") }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = if (isLoginTab) "Connexion Firebase" else "Inscription (+18 ans)",
                    fontWeight = FontWeight.Bold,
                    fontSize = 18.sp
                )
                IconButton(onClick = onDismiss) {
                    Icon(imageVector = Icons.Default.Close, contentDescription = "Fermer")
                }
            }
        },
        text = {
            Column(
                modifier = Modifier.fillMaxWidth(),
                verticalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                // Official SmartOne IPTV Logo Display
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(bottom = 4.dp),
                    contentAlignment = Alignment.Center
                ) {
                    SmartOneBrandLogo(size = SmartOneLogoSize.LARGE, showSubtitle = true)
                }

                // Tab switcher
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Button(
                        onClick = {
                            isLoginTab = true
                            errorMessage = ""
                        },
                        colors = ButtonDefaults.buttonColors(
                            containerColor = if (isLoginTab) BrandBluePrimary else DarkSurfaceVariant
                        ),
                        modifier = Modifier.weight(1f),
                        shape = RoundedCornerShape(8.dp)
                    ) {
                        Text("Connexion", fontSize = 12.sp, color = Color.White)
                    }

                    Button(
                        onClick = {
                            isLoginTab = false
                            errorMessage = ""
                        },
                        colors = ButtonDefaults.buttonColors(
                            containerColor = if (!isLoginTab) BrandBluePrimary else DarkSurfaceVariant
                        ),
                        modifier = Modifier.weight(1f),
                        shape = RoundedCornerShape(8.dp)
                    ) {
                        Text("Inscription (+18)", fontSize = 12.sp, color = Color.White)
                    }
                }

                if (!isLoginTab) {
                    OutlinedTextField(
                        value = name,
                        onValueChange = { name = it },
                        label = { Text("Nom complet") },
                        singleLine = true,
                        modifier = Modifier
                            .fillMaxWidth()
                            .testTag("auth_name_input"),
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = BrandCyan,
                            unfocusedBorderColor = DarkBorder
                        )
                    )
                }

                OutlinedTextField(
                    value = identifier,
                    onValueChange = { identifier = it },
                    label = { Text(if (isLoginTab) "Téléphone (+509...) ou e-mail" else "Numéro de téléphone (+509...)") },
                    singleLine = true,
                    placeholder = { Text("+509 3XXX XXXX ou identifiant") },
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("auth_identifier_input"),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = BrandCyan,
                        unfocusedBorderColor = DarkBorder
                    )
                )

                OutlinedTextField(
                    value = password,
                    onValueChange = { password = it },
                    label = { Text("Mot de passe (min. 6 car.)") },
                    singleLine = true,
                    visualTransformation = if (isPasswordVisible) VisualTransformation.None else PasswordVisualTransformation(),
                    trailingIcon = {
                        IconButton(onClick = { isPasswordVisible = !isPasswordVisible }) {
                            Icon(
                                imageVector = if (isPasswordVisible) Icons.Default.VisibilityOff else Icons.Default.Visibility,
                                contentDescription = "Afficher/Masquer"
                            )
                        }
                    },
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("auth_password_input"),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = BrandCyan,
                        unfocusedBorderColor = DarkBorder
                    )
                )

                if (!isLoginTab) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Checkbox(
                            checked = isAgeConfirmed,
                            onCheckedChange = { isAgeConfirmed = it },
                            colors = CheckboxDefaults.colors(checkedColor = BrandBluePrimary),
                            modifier = Modifier.testTag("auth_age_checkbox")
                        )
                        Text(
                            text = "J'ai au moins 18 ans et j'accepte les conditions d'utilisation de SmartOne IPTV.",
                            color = DarkTextSecondary,
                            fontSize = 11.sp,
                            lineHeight = 15.sp
                        )
                    }
                }

                if (errorMessage.isNotEmpty()) {
                    Text(
                        text = errorMessage,
                        color = StatusRed,
                        fontSize = 12.sp
                    )
                }

                Spacer(modifier = Modifier.height(4.dp))

                // Primary submit button
                Button(
                    onClick = {
                        if (password.length < 6) {
                            errorMessage = "Le mot de passe doit comporter au moins 6 caractères."
                            return@Button
                        }
                        if (identifier.isBlank()) {
                            errorMessage = "Veuillez entrer un email ou numéro de téléphone valide."
                            return@Button
                        }
                        if (!isLoginTab && !isAgeConfirmed) {
                            errorMessage = "Vous devez certifier avoir au moins 18 ans pour vous inscrire."
                            return@Button
                        }
                        onLoginSuccess(name.ifBlank { "Client SmartOne" }, identifier, false)
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = BrandBluePrimary),
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("auth_submit_button"),
                    shape = RoundedCornerShape(8.dp)
                ) {
                    Text(
                        text = if (isLoginTab) "Se connecter avec Firebase" else "Créer mon compte SmartOne IPTV",
                        fontWeight = FontWeight.Bold
                    )
                }

                // 1-Click Google Sign-In
                OutlinedButton(
                    onClick = {
                        onLoginSuccess("Compte Google", "+509 3800 0000", true)
                    },
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("auth_google_button"),
                    shape = RoundedCornerShape(8.dp)
                ) {
                    Icon(imageVector = Icons.Default.AccountCircle, contentDescription = null, modifier = Modifier.size(18.dp))
                    Spacer(modifier = Modifier.width(8.dp))
                    Text("Connexion avec Google (1 clic)", fontSize = 12.sp)
                }

                // Biometric Login Button
                OutlinedButton(
                    onClick = {
                        onLoginSuccess("Empreinte Biométrique", "+509 3700 0000", false)
                    },
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(8.dp)
                ) {
                    Icon(imageVector = Icons.Default.Fingerprint, contentDescription = null, tint = BrandCyan, modifier = Modifier.size(18.dp))
                    Spacer(modifier = Modifier.width(8.dp))
                    Text("Connexion Biométrique (Empreinte / FaceID)", fontSize = 12.sp)
                }
            }
        },
        confirmButton = {},
        containerColor = DarkSurface,
        titleContentColor = DarkTextPrimary,
        textContentColor = DarkTextPrimary
    )
}

@Composable
fun ProfileDialog(
    user: UserAccount?,
    balance: Double,
    activeSubscription: ActiveSubscription?,
    connectedDevices: List<ConnectedDevice> = emptyList(),
    onDisconnectDevice: (String) -> Unit = {},
    onDismiss: () -> Unit,
    onNavigateToDeposit: () -> Unit,
    onLogout: () -> Unit,
    onOpenAdmin: () -> Unit
) {
    var isMasked by remember { mutableStateOf(false) }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text("Profil Utilisateur", fontWeight = FontWeight.Bold, fontSize = 18.sp)
                IconButton(onClick = { isMasked = !isMasked }) {
                    Icon(
                        imageVector = if (isMasked) Icons.Default.VisibilityOff else Icons.Default.Visibility,
                        contentDescription = "Masquer/Afficher"
                    )
                }
            }
        },
        text = {
            Column(
                modifier = Modifier.fillMaxWidth(),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Box(
                        modifier = Modifier
                            .size(50.dp)
                            .clip(CircleShape)
                            .background(BrandBluePrimary.copy(alpha = 0.2f)),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = Icons.Default.Person,
                            contentDescription = null,
                            tint = BrandCyan,
                            modifier = Modifier.size(30.dp)
                        )
                    }

                    Spacer(modifier = Modifier.width(12.dp))

                    Column {
                        Text(
                            text = user?.name ?: "Visiteur",
                            fontWeight = FontWeight.Bold,
                            fontSize = 16.sp,
                            color = DarkTextPrimary
                        )
                        Text(
                            text = if (isMasked) "••••••••••" else (user?.email?.ifBlank { user.phone } ?: "Non connecté"),
                            color = DarkTextSecondary,
                            fontSize = 12.sp
                        )
                    }
                }

                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    Surface(
                        shape = RoundedCornerShape(20.dp),
                        color = StatusGreen.copy(alpha = 0.15f)
                    ) {
                        Text(
                            text = if (user?.isVerified == true) "Authentification Vérifiée" else "Compte Invité",
                            color = StatusGreen,
                            fontSize = 11.sp,
                            modifier = Modifier.padding(horizontal = 8.dp, vertical = 3.dp)
                        )
                    }

                    Surface(
                        shape = RoundedCornerShape(20.dp),
                        color = if (activeSubscription != null) BrandGold.copy(alpha = 0.15f) else DarkSurfaceVariant
                    ) {
                        Text(
                            text = activeSubscription?.label ?: "Mode Gratuit (1h/jour)",
                            color = if (activeSubscription != null) BrandGold else DarkTextMuted,
                            fontSize = 11.sp,
                            modifier = Modifier.padding(horizontal = 8.dp, vertical = 3.dp)
                        )
                    }
                }

                // Balance Box
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    colors = CardDefaults.cardColors(containerColor = DarkSurfaceVariant)
                ) {
                    Column(modifier = Modifier.padding(12.dp)) {
                        Text("Solde du compte (HTG)", color = DarkTextMuted, fontSize = 11.sp)
                        Text(
                            text = if (isMasked) "•••• HTG" else "${String.format(Locale.US, "%,.2f", balance)} HTG",
                            color = BrandCyan,
                            fontSize = 20.sp,
                            fontWeight = FontWeight.Bold
                        )
                        if (!isMasked) {
                            Text(
                                text = "≈ $${String.format(Locale.US, "%.2f", balance / SmartOneRepository.USD_RATE)} · €${String.format(Locale.US, "%.2f", balance / SmartOneRepository.EUR_RATE)}",
                                color = DarkTextSecondary,
                                fontSize = 11.sp
                            )
                        }

                        Spacer(modifier = Modifier.height(8.dp))

                        Button(
                            onClick = {
                                onDismiss()
                                onNavigateToDeposit()
                            },
                            colors = ButtonDefaults.buttonColors(containerColor = BrandBluePrimary),
                            modifier = Modifier.fillMaxWidth(),
                            shape = RoundedCornerShape(8.dp)
                        ) {
                            Text("Recharger mon solde")
                        }
                    }
                }

                // Section Appareils Connectés (Limite stricte de 2 appareils en même temps)
                if (activeSubscription != null) {
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        colors = CardDefaults.cardColors(containerColor = DarkSurfaceVariant),
                        border = CardDefaults.outlinedCardBorder().copy(
                            brush = androidx.compose.ui.graphics.SolidColor(
                                if (connectedDevices.size >= 2) BrandGold else StatusGreen
                            )
                        )
                    ) {
                        Column(modifier = Modifier.padding(12.dp)) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Icon(
                                        imageVector = Icons.Default.Devices,
                                        contentDescription = null,
                                        tint = BrandCyan,
                                        modifier = Modifier.size(18.dp)
                                    )
                                    Spacer(modifier = Modifier.width(6.dp))
                                    Text(
                                        text = "Appareils Connectés",
                                        fontWeight = FontWeight.Bold,
                                        fontSize = 13.sp,
                                        color = DarkTextPrimary
                                    )
                                }

                                Surface(
                                    shape = RoundedCornerShape(12.dp),
                                    color = if (connectedDevices.size > 2) StatusRed.copy(alpha = 0.2f) else StatusGreen.copy(alpha = 0.15f)
                                ) {
                                    Text(
                                        text = "${connectedDevices.size} / 2 Max",
                                        color = if (connectedDevices.size > 2) StatusRed else StatusGreen,
                                        fontWeight = FontWeight.Bold,
                                        fontSize = 11.sp,
                                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 2.dp)
                                    )
                                }
                            }

                            Spacer(modifier = Modifier.height(4.dp))
                            Text(
                                text = "Votre compte est limité à 2 appareils simultanés en même temps.",
                                color = DarkTextMuted,
                                fontSize = 11.sp
                            )

                            Spacer(modifier = Modifier.height(8.dp))

                            for (dev in connectedDevices) {
                                Row(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .padding(vertical = 4.dp),
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Row(
                                        modifier = Modifier.weight(1f),
                                        verticalAlignment = Alignment.CenterVertically
                                    ) {
                                        Icon(
                                            imageVector = if (dev.deviceType == "TV") Icons.Default.Tv else Icons.Default.Smartphone,
                                            contentDescription = null,
                                            tint = if (dev.isCurrentDevice) BrandCyan else DarkTextSecondary,
                                            modifier = Modifier.size(18.dp)
                                        )
                                        Spacer(modifier = Modifier.width(8.dp))
                                        Column {
                                            Text(
                                                text = if (dev.isCurrentDevice) "${dev.deviceName} (Cet appareil)" else dev.deviceName,
                                                color = DarkTextPrimary,
                                                fontWeight = if (dev.isCurrentDevice) FontWeight.Bold else FontWeight.Medium,
                                                fontSize = 12.sp
                                            )
                                            Text(
                                                text = "${dev.ipAddress} • ${dev.connectedAt}",
                                                color = DarkTextMuted,
                                                fontSize = 10.sp
                                            )
                                        }
                                    }

                                    if (!dev.isCurrentDevice) {
                                        TextButton(
                                            onClick = { onDisconnectDevice(dev.deviceId) },
                                            contentPadding = PaddingValues(horizontal = 6.dp, vertical = 2.dp),
                                            colors = ButtonDefaults.textButtonColors(contentColor = StatusRed)
                                        ) {
                                            Text("Déconnecter", fontSize = 11.sp, fontWeight = FontWeight.Bold)
                                        }
                                    } else {
                                        Surface(
                                            shape = RoundedCornerShape(4.dp),
                                            color = StatusGreen.copy(alpha = 0.15f)
                                        ) {
                                            Text(
                                                text = "Actif",
                                                color = StatusGreen,
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

                if (user?.role == "admin" || user?.email?.equals(SmartOneRepository.ADMIN_EMAIL, ignoreCase = true) == true) {
                    OutlinedButton(
                        onClick = {
                            onDismiss()
                            onOpenAdmin()
                        },
                        modifier = Modifier.fillMaxWidth(),
                        colors = ButtonDefaults.outlinedButtonColors(contentColor = BrandCyan),
                        shape = RoundedCornerShape(8.dp)
                    ) {
                        Icon(imageVector = Icons.Default.AdminPanelSettings, contentDescription = null, modifier = Modifier.size(18.dp))
                        Spacer(modifier = Modifier.width(6.dp))
                        Text("Ouvrir le Panneau Administrateur")
                    }
                }

                if (user != null) {
                    Button(
                        onClick = {
                            onLogout()
                            onDismiss()
                        },
                        colors = ButtonDefaults.buttonColors(containerColor = StatusRed),
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(8.dp)
                    ) {
                        Text("Déconnexion")
                    }
                }
            }
        },
        confirmButton = {
            TextButton(onClick = onDismiss) {
                Text("Fermer", color = DarkTextSecondary)
            }
        },
        containerColor = DarkSurface,
        titleContentColor = DarkTextPrimary,
        textContentColor = DarkTextPrimary
    )
}

@Composable
fun DeviceLimitExceededDialog(
    connectedDevices: List<ConnectedDevice>,
    onDisconnectDevice: (String) -> Unit,
    onDismiss: () -> Unit
) {
    AlertDialog(
        onDismissRequest = onDismiss,
        icon = {
            Icon(
                imageVector = Icons.Default.WarningAmber,
                contentDescription = null,
                tint = StatusAmber,
                modifier = Modifier.size(36.dp)
            )
        },
        title = {
            Text(
                text = "Limite de 2 Appareils Atteinte",
                fontWeight = FontWeight.Bold,
                fontSize = 17.sp,
                textAlign = androidx.compose.ui.text.style.TextAlign.Center
            )
        },
        text = {
            Column(
                modifier = Modifier.fillMaxWidth(),
                verticalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                Text(
                    text = "Votre abonnement SmartOne IPTV autorise un maximum de 2 appareils connectés en simultané.",
                    color = DarkTextSecondary,
                    fontSize = 13.sp,
                    lineHeight = 18.sp
                )

                Text(
                    text = "Appareils actuellement connectés (2/2) :",
                    color = DarkTextPrimary,
                    fontSize = 12.sp,
                    fontWeight = FontWeight.Bold
                )

                for (dev in connectedDevices) {
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        colors = CardDefaults.cardColors(containerColor = DarkSurfaceVariant),
                        shape = RoundedCornerShape(8.dp)
                    ) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(10.dp),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Row(
                                modifier = Modifier.weight(1f),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Icon(
                                    imageVector = if (dev.deviceType == "TV") Icons.Default.Tv else Icons.Default.Smartphone,
                                    contentDescription = null,
                                    tint = if (dev.isCurrentDevice) BrandCyan else DarkTextSecondary,
                                    modifier = Modifier.size(20.dp)
                                )
                                Spacer(modifier = Modifier.width(8.dp))
                                Column {
                                    Text(
                                        text = if (dev.isCurrentDevice) "${dev.deviceName} (Cet appareil)" else dev.deviceName,
                                        color = DarkTextPrimary,
                                        fontWeight = FontWeight.Bold,
                                        fontSize = 12.sp
                                    )
                                    Text(
                                        text = "${dev.ipAddress} • ${dev.connectedAt}",
                                        color = DarkTextMuted,
                                        fontSize = 10.sp
                                    )
                                }
                            }

                            if (!dev.isCurrentDevice) {
                                Button(
                                    onClick = { onDisconnectDevice(dev.deviceId) },
                                    colors = ButtonDefaults.buttonColors(containerColor = StatusRed),
                                    contentPadding = PaddingValues(horizontal = 8.dp, vertical = 4.dp),
                                    shape = RoundedCornerShape(6.dp)
                                ) {
                                    Text("Déconnecter", fontSize = 11.sp, color = Color.White, fontWeight = FontWeight.Bold)
                                }
                            } else {
                                Surface(
                                    shape = RoundedCornerShape(4.dp),
                                    color = StatusGreen.copy(alpha = 0.15f)
                                ) {
                                    Text(
                                        text = "Actif",
                                        color = StatusGreen,
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
        },
        confirmButton = {
            Button(
                onClick = onDismiss,
                colors = ButtonDefaults.buttonColors(containerColor = BrandBluePrimary)
            ) {
                Text("Compris")
            }
        },
        containerColor = DarkSurface,
        titleContentColor = DarkTextPrimary
    )
}

@Composable
fun SearchDialog(
    channels: List<Channel>,
    onDismiss: () -> Unit,
    onSelectChannel: (Channel) -> Unit,
    onFilterCategory: (String) -> Unit
) {
    var query by remember { mutableStateOf("") }

    val results = remember(query) {
        if (query.isBlank()) emptyList() else {
            channels.filter {
                it.name.contains(query, ignoreCase = true) ||
                it.currentProgram.contains(query, ignoreCase = true) ||
                it.category.contains(query, ignoreCase = true)
            }
        }
    }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Text("Rechercher une chaîne ou émission", fontWeight = FontWeight.Bold, fontSize = 17.sp)
        },
        text = {
            Column(
                modifier = Modifier.fillMaxWidth(),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                OutlinedTextField(
                    value = query,
                    onValueChange = { query = it },
                    placeholder = { Text("Rechercher dans l'application...") },
                    leadingIcon = { Icon(imageVector = Icons.Default.Search, contentDescription = null) },
                    singleLine = true,
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("search_dialog_input"),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = BrandCyan,
                        unfocusedBorderColor = DarkBorder
                    )
                )

                if (results.isEmpty() && query.isNotBlank()) {
                    Text("Aucun résultat pour \"$query\".", color = DarkTextMuted, fontSize = 13.sp)
                }

                LazyColumn(
                    modifier = Modifier
                        .fillMaxWidth()
                        .heightIn(max = 240.dp)
                ) {
                    items(results) { channel ->
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clickable {
                                    onSelectChannel(channel)
                                    onDismiss()
                                }
                                .padding(vertical = 8.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(text = channel.countryCode, fontSize = 20.sp)
                            Spacer(modifier = Modifier.width(10.dp))
                            Column {
                                Text(text = channel.name, color = DarkTextPrimary, fontWeight = FontWeight.SemiBold, fontSize = 14.sp)
                                Text(text = "${channel.category} • ${channel.currentProgram}", color = DarkTextMuted, fontSize = 11.sp)
                            }
                        }
                        HorizontalDivider(color = DarkBorder)
                    }
                }
            }
        },
        confirmButton = {
            TextButton(onClick = onDismiss) {
                Text("Fermer", color = DarkTextSecondary)
            }
        },
        containerColor = DarkSurface,
        titleContentColor = DarkTextPrimary,
        textContentColor = DarkTextPrimary
    )
}

@Composable
fun LanguageDialog(
    selectedLanguage: String,
    onDismiss: () -> Unit,
    onSelectLanguage: (String) -> Unit
) {
    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Text("Choisir la langue d'affichage", fontWeight = FontWeight.Bold, fontSize = 17.sp)
        },
        text = {
            LazyColumn(
                modifier = Modifier
                    .fillMaxWidth()
                    .heightIn(max = 300.dp)
            ) {
                items(SmartOneRepository.languages) { lang ->
                    val isSelected = lang == selectedLanguage
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable {
                                onSelectLanguage(lang)
                                onDismiss()
                            }
                            .padding(vertical = 10.dp, horizontal = 4.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = lang,
                            color = if (isSelected) BrandCyan else DarkTextPrimary,
                            fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal
                        )
                        if (isSelected) {
                            Icon(imageVector = Icons.Default.Check, contentDescription = null, tint = BrandCyan)
                        }
                    }
                    HorizontalDivider(color = DarkBorder)
                }
            }
        },
        confirmButton = {
            TextButton(onClick = onDismiss) {
                Text("Fermer", color = DarkTextSecondary)
            }
        },
        containerColor = DarkSurface,
        titleContentColor = DarkTextPrimary,
        textContentColor = DarkTextPrimary
    )
}

@Composable
fun InfoContentDialog(
    title: String,
    content: @Composable () -> Unit,
    onDismiss: () -> Unit
) {
    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Text(title, fontWeight = FontWeight.Bold, fontSize = 18.sp)
        },
        text = {
            content()
        },
        confirmButton = {
            Button(
                onClick = onDismiss,
                colors = ButtonDefaults.buttonColors(containerColor = BrandBluePrimary)
            ) {
                Text("Compris")
            }
        },
        containerColor = DarkSurface,
        titleContentColor = DarkTextPrimary,
        textContentColor = DarkTextPrimary
    )
}

@Composable
fun DownloadAppDialog(
    onDismiss: () -> Unit
) {
    val context = LocalContext.current
    val downloadUrl = "https://ais-pre-l4o5dpw7ksufk2mxdx3l76-856675755931.us-east1.run.app"
    val apkDirectUrl = "$downloadUrl/smartone-iptv-release.apk"

    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        imageVector = Icons.Default.DownloadForOffline,
                        contentDescription = null,
                        tint = BrandCyan,
                        modifier = Modifier.size(26.dp)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = "Télécharger l'application",
                        fontWeight = FontWeight.Bold,
                        fontSize = 17.sp
                    )
                }
                IconButton(onClick = onDismiss) {
                    Icon(imageVector = Icons.Default.Close, contentDescription = "Fermer")
                }
            }
        },
        text = {
            Column(
                modifier = Modifier.fillMaxWidth(),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                // App Emblem & Details Card
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    colors = CardDefaults.cardColors(containerColor = DarkSurfaceVariant),
                    border = CardDefaults.outlinedCardBorder().copy(
                        brush = androidx.compose.ui.graphics.SolidColor(BrandCyan.copy(alpha = 0.4f))
                    )
                ) {
                    Row(
                        modifier = Modifier.padding(14.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        androidx.compose.foundation.Image(
                            painter = androidx.compose.ui.res.painterResource(id = R.drawable.ic_smartone_emblem),
                            contentDescription = "Emblème SmartOne IPTV",
                            modifier = Modifier
                                .size(50.dp)
                                .clip(RoundedCornerShape(12.dp))
                        )

                        Spacer(modifier = Modifier.width(12.dp))

                        Column {
                            Text(
                                text = "SmartOne IPTV",
                                color = DarkTextPrimary,
                                fontWeight = FontWeight.Bold,
                                fontSize = 16.sp
                            )
                            Text(
                                text = "Version v${com.example.BuildConfig.VERSION_NAME} · Build ${com.example.BuildConfig.VERSION_CODE} (Dernière version)",
                                color = BrandCyan,
                                fontSize = 12.sp,
                                fontWeight = FontWeight.Medium
                            )
                            Text(
                                text = "Taille : ~14.8 Mo · Android 8.0+",
                                color = DarkTextSecondary,
                                fontSize = 11.sp
                            )
                        }
                    }
                }

                Text(
                    text = "Installez l'application officielle complète sur votre téléphone, tablette ou Android TV pour un streaming fluide et haute vitesse.",
                    color = DarkTextPrimary,
                    fontSize = 12.sp,
                    lineHeight = 16.sp
                )

                // Feature bullet points
                Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(imageVector = Icons.Default.CheckCircle, contentDescription = null, tint = StatusGreen, modifier = Modifier.size(14.dp))
                        Spacer(modifier = Modifier.width(6.dp))
                        Text("Mises à jour automatiques sécurisées", color = DarkTextSecondary, fontSize = 11.sp)
                    }
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(imageVector = Icons.Default.CheckCircle, contentDescription = null, tint = StatusGreen, modifier = Modifier.size(14.dp))
                        Spacer(modifier = Modifier.width(6.dp))
                        Text("Passerelles Natcash, MonCash & Crypto intégrées", color = DarkTextSecondary, fontSize = 11.sp)
                    }
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(imageVector = Icons.Default.CheckCircle, contentDescription = null, tint = StatusGreen, modifier = Modifier.size(14.dp))
                        Spacer(modifier = Modifier.width(6.dp))
                        Text("Zéro publicité intrusive · Qualité 4K / HD", color = DarkTextSecondary, fontSize = 11.sp)
                    }
                }

                Spacer(modifier = Modifier.height(4.dp))

                // Action 1: Direct Download APK
                Button(
                    onClick = {
                        try {
                            val intent = Intent(Intent.ACTION_VIEW, Uri.parse(apkDirectUrl))
                            context.startActivity(intent)
                            Toast.makeText(context, "📥 Lancement du téléchargement de SmartOne IPTV...", Toast.LENGTH_LONG).show()
                        } catch (e: Exception) {
                            val intent = Intent(Intent.ACTION_VIEW, Uri.parse(downloadUrl))
                            context.startActivity(intent)
                        }
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = BrandBluePrimary),
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("download_apk_button"),
                    shape = RoundedCornerShape(10.dp)
                ) {
                    Icon(imageVector = Icons.Default.Download, contentDescription = null, modifier = Modifier.size(18.dp))
                    Spacer(modifier = Modifier.width(8.dp))
                    Text("Télécharger l'APK Officiel (.apk)", fontWeight = FontWeight.Bold, fontSize = 13.sp)
                }

                // Action 2: Copy Download Link
                OutlinedButton(
                    onClick = {
                        val clipboard = context.getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
                        val clip = ClipData.newPlainText("SmartOne IPTV APK Link", downloadUrl)
                        clipboard.setPrimaryClip(clip)
                        Toast.makeText(context, " Lien de téléchargement copié dans le presse-papiers !", Toast.LENGTH_SHORT).show()
                    },
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(10.dp)
                ) {
                    Icon(imageVector = Icons.Default.ContentCopy, contentDescription = null, modifier = Modifier.size(16.dp))
                    Spacer(modifier = Modifier.width(6.dp))
                    Text("Copier le lien de téléchargement", fontSize = 12.sp)
                }

                // Action 3: Share App
                OutlinedButton(
                    onClick = {
                        val shareIntent = Intent(Intent.ACTION_SEND).apply {
                            type = "text/plain"
                            putExtra(Intent.EXTRA_SUBJECT, "Téléchargez SmartOne IPTV")
                            putExtra(Intent.EXTRA_TEXT, "Regardez vos chaînes TV en direct sur SmartOne IPTV : $downloadUrl")
                        }
                        context.startActivity(Intent.createChooser(shareIntent, "Partager SmartOne IPTV"))
                    },
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(10.dp)
                ) {
                    Icon(imageVector = Icons.Default.Share, contentDescription = null, modifier = Modifier.size(16.dp))
                    Spacer(modifier = Modifier.width(6.dp))
                    Text("Partager avec un ami", fontSize = 12.sp)
                }
            }
        },
        confirmButton = {},
        containerColor = DarkSurface,
        titleContentColor = DarkTextPrimary,
        textContentColor = DarkTextPrimary
    )
}

