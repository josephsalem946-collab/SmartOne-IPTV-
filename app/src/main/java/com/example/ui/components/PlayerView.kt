package com.example.ui.components

import androidx.compose.animation.core.*
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material.icons.outlined.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.ui.input.pointer.PointerEventPass
import androidx.compose.ui.input.pointer.pointerInput
import kotlinx.coroutines.delay
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.data.model.Channel
import com.example.data.network.ConnectionQuality
import com.example.data.network.NetworkStatusMonitor
import com.example.ui.theme.*

@Composable
fun PlayerView(
    channel: Channel?,
    isPlaying: Boolean,
    isFavorite: Boolean,
    freeMinutesLeft: Int,
    hasActiveSubscription: Boolean,
    onTogglePlayPause: () -> Unit,
    onStop: () -> Unit,
    onToggleFavorite: () -> Unit,
    onOpenPlans: () -> Unit,
    modifier: Modifier = Modifier
) {
    var isExpandedAspect by remember { mutableStateOf(false) }
    var areOverlaysVisible by remember { mutableStateOf(true) }
    var lastPlayerInteractionTime by remember { mutableLongStateOf(System.currentTimeMillis()) }

    val networkStatus by NetworkStatusMonitor.networkStatus.collectAsStateWithLifecycle()
    var selectedQuality by remember { mutableStateOf("Auto (Adaptatif)") }
    var showQualityMenu by remember { mutableStateOf(false) }

    val effectiveResolution = remember(selectedQuality, networkStatus.quality, channel?.resolution) {
        if (selectedQuality != "Auto (Adaptatif)") {
            selectedQuality
        } else when (networkStatus.quality) {
            ConnectionQuality.WEAK -> "480p SD (Réseau Faible)"
            ConnectionQuality.OFFLINE -> "Cache Hors-Ligne"
            ConnectionQuality.MODERATE -> "720p HD"
            ConnectionQuality.EXCELLENT -> channel?.resolution ?: "1080p FHD"
        }
    }

    // Auto-hide overlays after 30 seconds of inactivity
    LaunchedEffect(lastPlayerInteractionTime) {
        delay(30_000L) // 30 seconds
        areOverlaysVisible = false
    }

    // Always reveal overlays when a channel is loaded or changed
    LaunchedEffect(channel?.id) {
        lastPlayerInteractionTime = System.currentTimeMillis()
        areOverlaysVisible = true
    }

    Card(
        modifier = modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(16.dp))
            .pointerInput(Unit) {
                awaitPointerEventScope {
                    while (true) {
                        val event = awaitPointerEvent(pass = PointerEventPass.Initial)
                        if (event.changes.any { it.pressed }) {
                            lastPlayerInteractionTime = System.currentTimeMillis()
                            areOverlaysVisible = true
                        }
                    }
                }
            }
            .testTag("player_card"),
        colors = CardDefaults.cardColors(containerColor = Color(0xFF070B12)),
        elevation = CardDefaults.cardElevation(defaultElevation = 8.dp)
    ) {
        Column {
            // Video / Stream Canvas Screen
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .aspectRatio(if (isExpandedAspect) 2.1f else 1.77f) // 16:9 or 21:9
                    .background(
                        Brush.verticalGradient(
                            listOf(Color(0xFF0F172A), Color(0xFF030712))
                        )
                    )
                    .pointerInput(Unit) {
                        detectTapGestures {
                            // Any tap on the video screen reveals overlays and resets inactivity timer
                            lastPlayerInteractionTime = System.currentTimeMillis()
                            areOverlaysVisible = true
                        }
                    }
            ) {
                if (channel != null) {
                    // Animated Live Waveform / Visualizer background when playing
                    if (isPlaying) {
                        StreamVisualizer(
                            modifier = Modifier
                                .fillMaxSize()
                                .padding(horizontal = 24.dp, vertical = 20.dp)
                        )
                    }

                    // Top Badges Overlay (EN DIRECT, 4K UHD 60fps, 59 min gratuit)
                    BoxOverlayVisibility(
                        visible = areOverlaysVisible,
                        modifier = Modifier.align(Alignment.TopStart)
                    ) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(12.dp),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                                // Live blinking indicator ("EN DIRECT" / "EN PAUSE")
                                Surface(
                                    shape = RoundedCornerShape(6.dp),
                                    color = if (isPlaying) StatusRed else Color.Gray,
                                    modifier = Modifier.padding(end = 4.dp)
                                ) {
                                    Row(
                                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
                                        verticalAlignment = Alignment.CenterVertically
                                    ) {
                                        Box(
                                            modifier = Modifier
                                                .size(6.dp)
                                                .clip(CircleShape)
                                                .background(Color.White)
                                        )
                                        Spacer(modifier = Modifier.width(4.dp))
                                        Text(
                                            text = if (isPlaying) "EN DIRECT" else "EN PAUSE",
                                            color = Color.White,
                                            fontSize = 11.sp,
                                            fontWeight = FontWeight.Bold
                                        )
                                    }
                                }

                                // Adaptive Resolution badge ("Auto 480p / 4K UHD")
                                Box {
                                    Surface(
                                        shape = RoundedCornerShape(6.dp),
                                        color = if (networkStatus.isWeak) StatusAmber.copy(alpha = 0.25f) else Color(0x66000000),
                                        modifier = Modifier.clickable { showQualityMenu = true }
                                    ) {
                                        Row(
                                            modifier = Modifier.padding(horizontal = 6.dp, vertical = 3.dp),
                                            verticalAlignment = Alignment.CenterVertically
                                        ) {
                                            if (networkStatus.isWeak) {
                                                Icon(
                                                    imageVector = Icons.Default.SignalCellularAlt,
                                                    contentDescription = null,
                                                    tint = StatusAmber,
                                                    modifier = Modifier.size(12.dp)
                                                )
                                                Spacer(modifier = Modifier.width(3.dp))
                                            }
                                            Text(
                                                text = effectiveResolution,
                                                color = if (networkStatus.isWeak) StatusAmber else BrandCyan,
                                                fontSize = 11.sp,
                                                fontWeight = FontWeight.SemiBold
                                            )
                                            Spacer(modifier = Modifier.width(2.dp))
                                            Icon(
                                                imageVector = Icons.Default.ArrowDropDown,
                                                contentDescription = "Changer la qualité",
                                                tint = Color.White.copy(alpha = 0.7f),
                                                modifier = Modifier.size(14.dp)
                                            )
                                        }
                                    }

                                    DropdownMenu(
                                        expanded = showQualityMenu,
                                        onDismissRequest = { showQualityMenu = false }
                                    ) {
                                        listOf(
                                            "Auto (Adaptatif)",
                                            "4K UHD 60fps",
                                            "1080p FHD",
                                            "720p HD",
                                            "480p SD (Réseau Faible)",
                                            "360p Fluide (Très Faible)"
                                        ).forEach { quality ->
                                            DropdownMenuItem(
                                                text = {
                                                    Row(verticalAlignment = Alignment.CenterVertically) {
                                                        Text(
                                                            text = quality,
                                                            color = if (selectedQuality == quality) BrandCyan else Color.White,
                                                            fontWeight = if (selectedQuality == quality) FontWeight.Bold else FontWeight.Normal
                                                        )
                                                    }
                                                },
                                                onClick = {
                                                    selectedQuality = quality
                                                    showQualityMenu = false
                                                }
                                            )
                                        }
                                    }
                                }
                            }

                            // Free quota or plan badge ("59 min gratuit" / "VIP Actif")
                            if (!hasActiveSubscription) {
                                Surface(
                                    shape = RoundedCornerShape(6.dp),
                                    color = if (freeMinutesLeft < 10) StatusAmber.copy(alpha = 0.25f) else Color(0x66000000)
                                ) {
                                    Row(
                                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 3.dp),
                                        verticalAlignment = Alignment.CenterVertically
                                    ) {
                                        Icon(
                                            imageVector = Icons.Default.Timer,
                                            contentDescription = null,
                                            tint = if (freeMinutesLeft < 10) StatusAmber else BrandCyan,
                                            modifier = Modifier.size(13.dp)
                                        )
                                        Spacer(modifier = Modifier.width(4.dp))
                                        Text(
                                            text = "$freeMinutesLeft min gratuit",
                                            color = Color.White,
                                            fontSize = 11.sp,
                                            fontWeight = FontWeight.Medium
                                        )
                                    }
                                }
                            } else {
                                Surface(
                                    shape = RoundedCornerShape(6.dp),
                                    color = StatusGreen.copy(alpha = 0.2f)
                                ) {
                                    Row(
                                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 3.dp),
                                        verticalAlignment = Alignment.CenterVertically
                                    ) {
                                        Icon(
                                            imageVector = Icons.Default.Devices,
                                            contentDescription = null,
                                            tint = StatusGreen,
                                            modifier = Modifier.size(13.dp)
                                        )
                                        Spacer(modifier = Modifier.width(4.dp))
                                        Text(
                                            text = "VIP (2 écrans max)",
                                            color = StatusGreen,
                                            fontSize = 11.sp,
                                            fontWeight = FontWeight.Bold
                                        )
                                    }
                                }
                            }
                        }
                    }

                    // Center Channel details (Zone centrale)
                    BoxOverlayVisibility(
                        visible = areOverlaysVisible,
                        modifier = Modifier.align(Alignment.Center)
                    ) {
                        Column(
                            modifier = Modifier
                                .padding(horizontal = 16.dp),
                            horizontalAlignment = Alignment.CenterHorizontally
                        ) {
                            Text(
                                text = channel.countryCode,
                                fontSize = 32.sp
                            )
                            Spacer(modifier = Modifier.height(4.dp))
                            Text(
                                text = channel.name,
                                color = Color.White,
                                fontSize = 18.sp,
                                fontWeight = FontWeight.Bold,
                                maxLines = 1,
                                overflow = TextOverflow.Ellipsis
                            )
                            Text(
                                text = channel.currentProgram,
                                color = DarkTextSecondary,
                                fontSize = 13.sp,
                                maxLines = 1,
                                overflow = TextOverflow.Ellipsis
                            )
                        }
                    }

                    // Low-bandwidth & weak connection badge inside player
                    if (networkStatus.isWeak && isPlaying) {
                        Surface(
                            modifier = Modifier
                                .align(Alignment.BottomCenter)
                                .padding(bottom = 38.dp),
                            shape = RoundedCornerShape(20.dp),
                            color = Color(0xDD1A1400)
                        ) {
                            Row(
                                modifier = Modifier.padding(horizontal = 10.dp, vertical = 4.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Icon(
                                    imageVector = Icons.Default.Speed,
                                    contentDescription = null,
                                    tint = StatusAmber,
                                    modifier = Modifier.size(13.dp)
                                )
                                Spacer(modifier = Modifier.width(6.dp))
                                Text(
                                    text = "Réseau faible : Flux fluide à $effectiveResolution",
                                    color = StatusAmber,
                                    fontSize = 11.sp,
                                    fontWeight = FontWeight.Medium
                                )
                            }
                        }
                    }

                    // Bottom stream metadata overlay (Informations techniques & Icône Plein écran)
                    BoxOverlayVisibility(
                        visible = areOverlaysVisible,
                        modifier = Modifier.align(Alignment.BottomStart)
                    ) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .background(
                                    Brush.verticalGradient(
                                        listOf(Color.Transparent, Color(0xCC000000))
                                    )
                                )
                                .padding(horizontal = 12.dp, vertical = 8.dp),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            // Informations techniques (en bas à gauche) : Indique la catégorie (General • Global) et le débit binaire (12 Mbps)
                            Text(
                                text = "${channel.category} • ${channel.country} • ${channel.bitrate}",
                                color = DarkTextSecondary,
                                fontSize = 11.sp,
                                fontWeight = FontWeight.Medium
                            )

                            // Icône Plein écran
                            IconButton(
                                onClick = {
                                    lastPlayerInteractionTime = System.currentTimeMillis()
                                    areOverlaysVisible = true
                                    isExpandedAspect = !isExpandedAspect
                                },
                                modifier = Modifier
                                    .size(28.dp)
                                    .testTag("player_fullscreen_button")
                            ) {
                                Icon(
                                    imageVector = if (isExpandedAspect) Icons.Default.FullscreenExit else Icons.Default.Fullscreen,
                                    contentDescription = if (isExpandedAspect) "Quitter plein écran" else "Plein écran",
                                    tint = Color.White,
                                    modifier = Modifier.size(20.dp)
                                )
                            }
                        }
                    }
                } else {
                    // Empty state
                    Box(
                        modifier = Modifier.fillMaxSize(),
                        contentAlignment = Alignment.Center
                    ) {
                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            Icon(
                                imageVector = Icons.Default.Tv,
                                contentDescription = null,
                                tint = DarkTextMuted,
                                modifier = Modifier.size(48.dp)
                            )
                            Spacer(modifier = Modifier.height(8.dp))
                            Text(
                                text = "Choisissez une chaîne pour commencer le streaming",
                                color = DarkTextSecondary,
                                fontSize = 14.sp
                            )
                        }
                    }
                }
            }

            // Player Action Controls Toolbar
            if (channel != null) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 12.dp, vertical = 8.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        // Play/Pause button
                        FilledIconButton(
                            onClick = {
                                lastPlayerInteractionTime = System.currentTimeMillis()
                                areOverlaysVisible = true
                                onTogglePlayPause()
                            },
                            colors = IconButtonDefaults.filledIconButtonColors(
                                containerColor = BrandBluePrimary,
                                contentColor = Color.White
                            ),
                            modifier = Modifier
                                .size(40.dp)
                                .testTag("player_play_pause_button")
                        ) {
                            Icon(
                                imageVector = if (isPlaying) Icons.Default.Pause else Icons.Default.PlayArrow,
                                contentDescription = if (isPlaying) "Pause" else "Lecture"
                            )
                        }

                        // Stop button
                        OutlinedIconButton(
                            onClick = {
                                lastPlayerInteractionTime = System.currentTimeMillis()
                                areOverlaysVisible = true
                                onStop()
                            },
                            colors = IconButtonDefaults.outlinedIconButtonColors(contentColor = DarkTextPrimary),
                            modifier = Modifier
                                .size(40.dp)
                                .testTag("player_stop_button")
                        ) {
                            Icon(imageVector = Icons.Default.Stop, contentDescription = "Arrêter")
                        }

                        // Favorite button
                        IconButton(
                            onClick = {
                                lastPlayerInteractionTime = System.currentTimeMillis()
                                areOverlaysVisible = true
                                onToggleFavorite()
                            },
                            modifier = Modifier.testTag("player_fav_button")
                        ) {
                            Icon(
                                imageVector = if (isFavorite) Icons.Filled.Star else Icons.Outlined.StarBorder,
                                contentDescription = "Favori",
                                tint = if (isFavorite) BrandGold else DarkTextSecondary
                            )
                        }
                    }

                    if (!hasActiveSubscription) {
                        Button(
                            onClick = {
                                lastPlayerInteractionTime = System.currentTimeMillis()
                                areOverlaysVisible = true
                                onOpenPlans()
                            },
                            colors = ButtonDefaults.buttonColors(containerColor = BrandGoldDark),
                            contentPadding = PaddingValues(horizontal = 12.dp, vertical = 6.dp),
                            shape = RoundedCornerShape(8.dp),
                            modifier = Modifier.testTag("player_subscribe_button")
                        ) {
                            Icon(
                                imageVector = Icons.Default.Star,
                                contentDescription = null,
                                tint = Color.Black,
                                modifier = Modifier.size(16.dp)
                            )
                            Spacer(modifier = Modifier.width(4.dp))
                            Text(
                                text = "Illimité",
                                color = Color.Black,
                                fontWeight = FontWeight.Bold,
                                fontSize = 12.sp
                            )
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun StreamVisualizer(modifier: Modifier = Modifier) {
    val infiniteTransition = rememberInfiniteTransition(label = "audio_visualizer")
    val barCount = 18

    Row(
        modifier = modifier,
        horizontalArrangement = Arrangement.SpaceEvenly,
        verticalAlignment = Alignment.CenterVertically
    ) {
        for (i in 0 until barCount) {
            val animHeight by infiniteTransition.animateFloat(
                initialValue = 0.15f,
                targetValue = 0.85f,
                animationSpec = infiniteRepeatable(
                    animation = tween(durationMillis = 350 + (i * 45) % 400, easing = FastOutSlowInEasing),
                    repeatMode = RepeatMode.Reverse
                ),
                label = "bar_$i"
            )

            Box(
                modifier = Modifier
                    .width(3.dp)
                    .fillMaxHeight(animHeight)
                    .clip(RoundedCornerShape(2.dp))
                    .background(
                        Brush.verticalGradient(
                            listOf(BrandCyan.copy(alpha = 0.8f), BrandBluePrimary.copy(alpha = 0.3f))
                        )
                    )
            )
        }
    }
}

@Composable
private fun BoxOverlayVisibility(
    visible: Boolean,
    modifier: Modifier = Modifier,
    content: @Composable () -> Unit
) {
    androidx.compose.animation.AnimatedVisibility(
        visible = visible,
        enter = fadeIn(),
        exit = fadeOut(),
        modifier = modifier
    ) {
        content()
    }
}
