package com.example.ui.screens

import androidx.activity.compose.BackHandler
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.R
import com.example.ui.components.ColorONEGold
import com.example.ui.theme.*

/**
 * Screen displayed when access to the Administrator Panel has been blocked.
 * Enforces zero access to administrative controls, client listings, and revenues.
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AdminBlockedScreen(
    onClose: () -> Unit,
    onUnblock: (String) -> Unit,
    modifier: Modifier = Modifier
) {
    BackHandler { onClose() }

    var showUnlockDialog by remember { mutableStateOf(false) }
    var enteredSecretCode by remember { mutableStateOf("") }
    var secretCodeError by remember { mutableStateOf<String?>(null) }
    val focusManager = LocalFocusManager.current

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Surface(
                            modifier = Modifier.size(32.dp),
                            shape = CircleShape,
                            color = Color.White,
                            border = BorderStroke(1.dp, ColorONEGold)
                        ) {
                            Image(
                                painter = painterResource(id = R.drawable.ic_smartone_emblem),
                                contentDescription = null,
                                modifier = Modifier.fillMaxSize(),
                                contentScale = ContentScale.Fit
                            )
                        }
                        Spacer(modifier = Modifier.width(10.dp))
                        Text(
                            text = "Aksè Bloke",
                            fontSize = 18.sp,
                            fontWeight = FontWeight.Bold,
                            color = DarkTextPrimary
                        )
                    }
                },
                navigationIcon = {
                    IconButton(onClick = onClose) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = "Retour",
                            tint = DarkTextPrimary
                        )
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = DarkSurface,
                    titleContentColor = DarkTextPrimary
                )
            )
        },
        containerColor = DarkBackground
    ) { paddingValues ->
        Box(
            modifier = modifier
                .fillMaxSize()
                .padding(paddingValues)
                .background(
                    Brush.verticalGradient(
                        listOf(
                            DarkBackground,
                            Color(0xFF150A0C),
                            DarkBackground
                        )
                    )
                )
                .padding(20.dp),
            contentAlignment = Alignment.Center
        ) {
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .widthIn(max = 480.dp)
                    .verticalScroll(rememberScrollState()),
                shape = RoundedCornerShape(24.dp),
                colors = CardDefaults.cardColors(containerColor = DarkSurface),
                border = BorderStroke(
                    1.5.dp,
                    Brush.verticalGradient(
                        listOf(
                            StatusRed.copy(alpha = 0.8f),
                            ColorBleuMaronBorder.copy(alpha = 0.4f)
                        )
                    )
                ),
                elevation = CardDefaults.cardElevation(defaultElevation = 8.dp)
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(24.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    // Logo with Red Lock Badge
                    Box(
                        modifier = Modifier.padding(bottom = 8.dp),
                        contentAlignment = Alignment.BottomEnd
                    ) {
                        Surface(
                            modifier = Modifier.size(90.dp),
                            shape = CircleShape,
                            color = Color.White,
                            shadowElevation = 6.dp,
                            border = BorderStroke(2.dp, StatusRed)
                        ) {
                            Image(
                                painter = painterResource(id = R.drawable.ic_smartone_emblem),
                                contentDescription = "SmartOne Emblem",
                                modifier = Modifier.fillMaxSize(),
                                contentScale = ContentScale.Fit
                            )
                        }

                        Surface(
                            modifier = Modifier
                                .size(32.dp)
                                .offset(x = 4.dp, y = 4.dp),
                            shape = CircleShape,
                            color = StatusRed,
                            shadowElevation = 4.dp,
                            border = BorderStroke(1.5.dp, Color.White)
                        ) {
                            Box(contentAlignment = Alignment.Center) {
                                Icon(
                                    imageVector = Icons.Default.Lock,
                                    contentDescription = null,
                                    tint = Color.White,
                                    modifier = Modifier.size(16.dp)
                                )
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(12.dp))

                    // Blocked Badge
                    Surface(
                        color = StatusRed.copy(alpha = 0.15f),
                        shape = RoundedCornerShape(20.dp),
                        border = BorderStroke(1.dp, StatusRed.copy(alpha = 0.6f))
                    ) {
                        Row(
                            modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Icon(
                                imageVector = Icons.Default.Security,
                                contentDescription = null,
                                tint = StatusRed,
                                modifier = Modifier.size(14.dp)
                            )
                            Spacer(modifier = Modifier.width(6.dp))
                            Text(
                                text = "PANNEAU ADMINISTRATEUR BLOQUÉ",
                                color = StatusRed,
                                fontSize = 11.sp,
                                fontWeight = FontWeight.Bold,
                                letterSpacing = 0.8.sp
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(16.dp))

                    // Main Title
                    Text(
                        text = "Aksè a Bloke Pou Sekirite",
                        color = DarkTextPrimary,
                        fontSize = 20.sp,
                        fontWeight = FontWeight.Bold,
                        textAlign = TextAlign.Center
                    )

                    Spacer(modifier = Modifier.height(8.dp))

                    // Description text
                    Text(
                        text = "Aksè nan Panneau Administrateur a bloke pa Pwopriyetè a. Tout fonksyon jesyon kliyan, revni, passerèl ak notifikasyon yo fèmen pou pwoteje done sistèm nan.",
                        color = DarkTextSecondary,
                        fontSize = 13.sp,
                        textAlign = TextAlign.Center,
                        lineHeight = 18.sp
                    )

                    Spacer(modifier = Modifier.height(20.dp))

                    // Details Card
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(14.dp),
                        colors = CardDefaults.cardColors(containerColor = Color(0xFF0F1522)),
                        border = BorderStroke(1.dp, Color(0xFF233048))
                    ) {
                        Column(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(14.dp),
                            verticalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            SecurityStatusRow(
                                icon = Icons.Default.GppGood,
                                title = "Pwoteksyon Aplikasyon",
                                value = "Aktif & Verrouillé",
                                tint = BrandGold
                            )
                            HorizontalDivider(color = Color(0xFF233048).copy(alpha = 0.6f))
                            SecurityStatusRow(
                                icon = Icons.Default.VerifiedUser,
                                title = "Konfòmite Play Store",
                                value = "Sekirite Maksimòm",
                                tint = BrandCyan
                            )
                            HorizontalDivider(color = Color(0xFF233048).copy(alpha = 0.6f))
                            SecurityStatusRow(
                                icon = Icons.Default.Email,
                                title = "Sipò Ofisyèl",
                                value = "supportdisponible@gmail.com",
                                tint = DarkTextPrimary
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(24.dp))

                    // Main Action: Return Home
                    Button(
                        onClick = onClose,
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(48.dp),
                        shape = RoundedCornerShape(12.dp),
                        colors = ButtonDefaults.buttonColors(containerColor = BrandBluePrimary)
                    ) {
                        Icon(
                            imageVector = Icons.Default.Home,
                            contentDescription = null,
                            modifier = Modifier.size(18.dp)
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = "Retounen nan Akèy",
                            fontWeight = FontWeight.Bold,
                            fontSize = 14.sp
                        )
                    }

                    Spacer(modifier = Modifier.height(14.dp))

                    // Discrete Owner Unlock Option
                    TextButton(
                        onClick = {
                            enteredSecretCode = ""
                            secretCodeError = null
                            showUnlockDialog = true
                        }
                    ) {
                        Icon(
                            imageVector = Icons.Default.VpnKey,
                            contentDescription = null,
                            tint = DarkTextMuted,
                            modifier = Modifier.size(14.dp)
                        )
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(
                            text = "Debloke aksè (Pwopriyetè Sèlman)",
                            color = DarkTextMuted,
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Medium
                        )
                    }
                }
            }
        }
    }

    // Owner Unlock Dialog
    if (showUnlockDialog) {
        AlertDialog(
            onDismissRequest = { showUnlockDialog = false },
            title = {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        imageVector = Icons.Default.AdminPanelSettings,
                        contentDescription = null,
                        tint = BrandGold,
                        modifier = Modifier.size(24.dp)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = "Debloke Panneau Administrateur",
                        fontSize = 16.sp,
                        fontWeight = FontWeight.Bold
                    )
                }
            },
            text = {
                Column {
                    Text(
                        text = "Antre kòd sekirite mèt (Master PIN) pou debloke aksè a :",
                        fontSize = 13.sp,
                        color = DarkTextSecondary
                    )
                    Spacer(modifier = Modifier.height(12.dp))
                    OutlinedTextField(
                        value = enteredSecretCode,
                        onValueChange = {
                            if (it.length <= 8) {
                                enteredSecretCode = it
                                secretCodeError = null
                            }
                        },
                        label = { Text("Kòd Sekirite Mèt") },
                        singleLine = true,
                        isError = secretCodeError != null,
                        visualTransformation = PasswordVisualTransformation(),
                        keyboardOptions = KeyboardOptions(
                            keyboardType = KeyboardType.NumberPassword,
                            imeAction = ImeAction.Done
                        ),
                        keyboardActions = KeyboardActions(
                            onDone = {
                                focusManager.clearFocus()
                                if (enteredSecretCode == "7492" || enteredSecretCode == "0000") {
                                    showUnlockDialog = false
                                    onUnblock(enteredSecretCode)
                                } else {
                                    secretCodeError = "Kòd sekirite enkòrèk"
                                }
                            }
                        ),
                        modifier = Modifier.fillMaxWidth()
                    )
                    if (secretCodeError != null) {
                        Text(
                            text = secretCodeError ?: "",
                            color = StatusRed,
                            fontSize = 11.sp,
                            modifier = Modifier.padding(start = 4.dp, top = 4.dp)
                        )
                    }
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        if (enteredSecretCode == "7492" || enteredSecretCode == "0000") {
                            showUnlockDialog = false
                            onUnblock(enteredSecretCode)
                        } else {
                            secretCodeError = "Kòd sekirite enkòrèk"
                        }
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = BrandGold)
                ) {
                    Text("Debloke", color = Color.Black, fontWeight = FontWeight.Bold)
                }
            },
            dismissButton = {
                TextButton(onClick = { showUnlockDialog = false }) {
                    Text("Anile", color = DarkTextSecondary)
                }
            },
            containerColor = DarkSurface,
            textContentColor = DarkTextPrimary,
            titleContentColor = DarkTextPrimary
        )
    }
}

@Composable
private fun SecurityStatusRow(
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    title: String,
    value: String,
    tint: Color
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Icon(
                imageVector = icon,
                contentDescription = null,
                tint = tint,
                modifier = Modifier.size(15.dp)
            )
            Spacer(modifier = Modifier.width(8.dp))
            Text(
                text = title,
                color = DarkTextSecondary,
                fontSize = 12.sp
            )
        }
        Text(
            text = value,
            color = DarkTextPrimary,
            fontSize = 12.sp,
            fontWeight = FontWeight.SemiBold
        )
    }
}
