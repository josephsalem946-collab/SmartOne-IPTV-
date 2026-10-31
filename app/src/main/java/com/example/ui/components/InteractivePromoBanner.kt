package com.example.ui.components

import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import android.content.Intent
import android.net.Uri
import android.widget.Toast
import androidx.compose.animation.*
import androidx.compose.animation.core.*
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.KeyboardArrowLeft
import androidx.compose.material.icons.automirrored.filled.KeyboardArrowRight
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.R
import com.example.data.repository.SmartOneRepository
import com.example.ui.theme.*
import kotlinx.coroutines.delay

/**
 * Compact & Responsive Interactive Promotional Banner Carousel for SmartOne IPTV.
 * Features 4 rotating interactive slides:
 * 1. USDT TRC20 Warning
 * 2. MonCash & Natcash Local Payments (HTG)
 * 3. Fast & Secure SmartOne IPTV Recharge
 * 4. 24/7 Official Email Support
 *
 * - Auto-advances every 60 seconds.
 * - Miniaturized navigation chevrons for clean display on all screens without overflow.
 * - Zero bottom dots as requested for ultra-compact layout.
 */
@Composable
fun InteractivePromoBanner(
    modifier: Modifier = Modifier,
    onNavigateToDeposit: () -> Unit = {},
    onOpenSupport: () -> Unit = {}
) {
    val context = LocalContext.current
    var currentSlide by remember { mutableIntStateOf(0) }
    val totalSlides = 4

    // 60-second auto-timer with reset upon manual interaction
    var timerResetTrigger by remember { mutableLongStateOf(System.currentTimeMillis()) }

    LaunchedEffect(timerResetTrigger) {
        while (true) {
            delay(60_000L) // 60 segonn
            currentSlide = (currentSlide + 1) % totalSlides
        }
    }

    // Subtle pulse animation for warning badge
    val infiniteTransition = rememberInfiniteTransition(label = "pulse_badge")
    val badgeAlpha by infiniteTransition.animateFloat(
        initialValue = 1f,
        targetValue = 0.75f,
        animationSpec = infiniteRepeatable(
            animation = tween(1200, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "pulse_alpha"
    )

    Card(
        modifier = modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(12.dp))
            .border(
                width = 1.dp,
                color = BrandCyan.copy(alpha = 0.35f),
                shape = RoundedCornerShape(12.dp)
            )
            .testTag("interactive_promo_banner"),
        colors = CardDefaults.cardColors(containerColor = Color(0xFF0F172A)),
        shape = RoundedCornerShape(12.dp),
        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
    ) {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .background(
                    Brush.linearGradient(
                        listOf(
                            Color(0xFF0F172A),
                            Color(0xFF1E293B)
                        )
                    )
                )
                .padding(vertical = 6.dp, horizontal = 4.dp)
        ) {
            // Content Container with Padding for compact side chevrons
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(start = 28.dp, end = 28.dp)
                    .heightIn(min = 58.dp),
                contentAlignment = Alignment.Center
            ) {
                AnimatedContent(
                    targetState = currentSlide,
                    transitionSpec = {
                        if (targetState > initialState) {
                            (slideInHorizontally { width -> width / 2 } + fadeIn()).togetherWith(
                                slideOutHorizontally { width -> -width / 2 } + fadeOut()
                            )
                        } else {
                            (slideInHorizontally { width -> -width / 2 } + fadeIn()).togetherWith(
                                slideOutHorizontally { width -> width / 2 } + fadeOut()
                            )
                        }
                    },
                    label = "slide_transition"
                ) { slideIndex ->
                    when (slideIndex) {
                        0 -> Slide1UsdtWarningCompact(
                            badgeAlpha = badgeAlpha,
                            onRechargeClick = onNavigateToDeposit
                        )
                        1 -> Slide2MonCashNatcashCompact(
                            onPayClick = onNavigateToDeposit
                        )
                        2 -> Slide3RechargeIptvCompact(
                            onSupportClick = {
                                val intent = Intent(Intent.ACTION_SENDTO).apply {
                                    data = Uri.parse("mailto:${SmartOneRepository.SUPPORT_EMAIL}")
                                    putExtra(Intent.EXTRA_SUBJECT, "Demande de Support SmartOne IPTV")
                                }
                                runCatching { context.startActivity(intent) }
                                    .onFailure {
                                        Toast.makeText(context, "Sipò: ${SmartOneRepository.SUPPORT_EMAIL}", Toast.LENGTH_SHORT).show()
                                    }
                            }
                        )
                        3 -> Slide4EmailSupportCompact(
                            onCopyClick = {
                                val clipboard = context.getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
                                val clip = ClipData.newPlainText("SmartOne Support Email", SmartOneRepository.SUPPORT_EMAIL)
                                clipboard.setPrimaryClip(clip)
                                Toast.makeText(context, "Adres imèl ${SmartOneRepository.SUPPORT_EMAIL} kopye!", Toast.LENGTH_SHORT).show()
                            }
                        )
                    }
                }
            }

            // Left Navigation Chevron (Minò & Konpak)
            Surface(
                modifier = Modifier
                    .align(Alignment.CenterStart)
                    .size(24.dp)
                    .clip(CircleShape)
                    .clickable {
                        currentSlide = (currentSlide - 1 + totalSlides) % totalSlides
                        timerResetTrigger = System.currentTimeMillis()
                    },
                shape = CircleShape,
                color = Color(0x33F59E0B),
                border = CardDefaults.outlinedCardBorder().copy(
                    brush = androidx.compose.ui.graphics.SolidColor(Color(0x88F59E0B))
                )
            ) {
                Box(contentAlignment = Alignment.Center) {
                    Icon(
                        imageVector = Icons.AutoMirrored.Filled.KeyboardArrowLeft,
                        contentDescription = "Slide Presedan",
                        tint = Color(0xFFFBBF24),
                        modifier = Modifier.size(14.dp)
                    )
                }
            }

            // Right Navigation Chevron (Minò & Konpak)
            Surface(
                modifier = Modifier
                    .align(Alignment.CenterEnd)
                    .size(24.dp)
                    .clip(CircleShape)
                    .clickable {
                        currentSlide = (currentSlide + 1) % totalSlides
                        timerResetTrigger = System.currentTimeMillis()
                    },
                shape = CircleShape,
                color = Color(0x33F59E0B),
                border = CardDefaults.outlinedCardBorder().copy(
                    brush = androidx.compose.ui.graphics.SolidColor(Color(0x88F59E0B))
                )
            ) {
                Box(contentAlignment = Alignment.Center) {
                    Icon(
                        imageVector = Icons.AutoMirrored.Filled.KeyboardArrowRight,
                        contentDescription = "Slide Swivan",
                        tint = Color(0xFFFBBF24),
                        modifier = Modifier.size(14.dp)
                    )
                }
            }
        }
    }
}

/**
 * SLIDE 1: AVÈTISMAN USDT TRC20 SÈLMAN (Compact)
 */
@Composable
private fun Slide1UsdtWarningCompact(
    badgeAlpha: Float,
    onRechargeClick: () -> Unit
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Row(
            modifier = Modifier.weight(1f),
            verticalAlignment = Alignment.CenterVertically
        ) {
            // USDT TRC20 Logo
            Box(
                modifier = Modifier
                    .size(30.dp)
                    .clip(CircleShape)
            ) {
                Image(
                    painter = painterResource(id = R.drawable.ic_usdt_trc20),
                    contentDescription = "USDT TRC20",
                    modifier = Modifier.fillMaxSize()
                )
                Surface(
                    modifier = Modifier.align(Alignment.BottomEnd),
                    shape = RoundedCornerShape(2.dp),
                    color = Color(0xFFDC2626)
                ) {
                    Text(
                        text = "TRC20",
                        color = Color.White,
                        fontSize = 6.sp,
                        fontWeight = FontWeight.Black,
                        modifier = Modifier.padding(horizontal = 1.5.dp, vertical = 0.5.dp)
                    )
                }
            }

            Spacer(modifier = Modifier.width(7.dp))

            Column(modifier = Modifier.weight(1f)) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(4.dp)
                ) {
                    Surface(
                        shape = RoundedCornerShape(3.dp),
                        color = Color(0x33EF4444).copy(alpha = badgeAlpha * 0.4f),
                        border = CardDefaults.outlinedCardBorder().copy(
                            brush = androidx.compose.ui.graphics.SolidColor(Color(0xFFEF4444))
                        )
                    ) {
                        Row(
                            modifier = Modifier.padding(horizontal = 3.dp, vertical = 0.5.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Icon(
                                imageVector = Icons.Default.Warning,
                                contentDescription = null,
                                tint = Color(0xFFF87171),
                                modifier = Modifier.size(8.dp)
                            )
                            Spacer(modifier = Modifier.width(2.dp))
                            Text(
                                text = "AVÈTISMAN",
                                color = Color(0xFFF87171),
                                fontSize = 8.sp,
                                fontWeight = FontWeight.Bold
                            )
                        }
                    }

                    Text(
                        text = "USDT TRC20 SÈLMAN",
                        color = BrandCyan,
                        fontSize = 10.sp,
                        fontWeight = FontWeight.Bold,
                        maxLines = 1
                    )
                }

                Spacer(modifier = Modifier.height(1.dp))

                Text(
                    text = "Voye sèlman sou rezo TRC20. Si w itilize yon lòt rezo (eg: ERC-20), lajan an ap pèdi nèt!",
                    color = Color(0xFFCBD5E1),
                    fontSize = 8.5.sp,
                    lineHeight = 11.sp,
                    maxLines = 2,
                    overflow = TextOverflow.Ellipsis
                )
            }
        }

        Spacer(modifier = Modifier.width(6.dp))

        // Action Button: Recharge ⚡
        Button(
            onClick = onRechargeClick,
            colors = ButtonDefaults.buttonColors(containerColor = Color.Transparent),
            contentPadding = PaddingValues(horizontal = 7.dp, vertical = 2.dp),
            shape = RoundedCornerShape(8.dp),
            modifier = Modifier
                .height(26.dp)
                .background(
                    Brush.horizontalGradient(
                        listOf(Color(0xFFF59E0B), Color(0xFFD97706))
                    ),
                    shape = RoundedCornerShape(8.dp)
                )
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Text(
                    text = "Recharge",
                    color = Color(0xFF020617),
                    fontWeight = FontWeight.Bold,
                    fontSize = 9.5.sp
                )
                Spacer(modifier = Modifier.width(2.dp))
                Icon(
                    imageVector = Icons.Default.Bolt,
                    contentDescription = null,
                    tint = Color(0xFF020617),
                    modifier = Modifier.size(10.dp)
                )
            }
        }
    }
}

/**
 * SLIDE 2: NOUVO SLIDE MONCASH AK NATCASH (Peman Lokal HTG)
 */
@Composable
private fun Slide2MonCashNatcashCompact(
    onPayClick: () -> Unit
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Row(
            modifier = Modifier.weight(1f),
            verticalAlignment = Alignment.CenterVertically
        ) {
            // MonCash & Natcash Mini Dual Logos
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(2.dp)
            ) {
                Surface(
                    modifier = Modifier
                        .size(22.dp)
                        .clip(RoundedCornerShape(5.dp)),
                    color = Color.White
                ) {
                    Image(
                        painter = painterResource(id = R.drawable.ic_moncash_logo),
                        contentDescription = "MonCash",
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(1.dp),
                        contentScale = ContentScale.Fit
                    )
                }
                Surface(
                    modifier = Modifier
                        .size(22.dp)
                        .clip(RoundedCornerShape(5.dp)),
                    color = Color.White
                ) {
                    Image(
                        painter = painterResource(id = R.drawable.ic_natcash_logo),
                        contentDescription = "Natcash",
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(1.dp),
                        contentScale = ContentScale.Fit
                    )
                }
            }

            Spacer(modifier = Modifier.width(7.dp))

            Column(modifier = Modifier.weight(1f)) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(4.dp)
                ) {
                    Surface(
                        shape = RoundedCornerShape(3.dp),
                        color = Color(0x3322C55E),
                        border = CardDefaults.outlinedCardBorder().copy(
                            brush = androidx.compose.ui.graphics.SolidColor(Color(0xFF22C55E))
                        )
                    ) {
                        Text(
                            text = "MONCASH & NATCASH",
                            color = Color(0xFF86EFAC),
                            fontSize = 8.sp,
                            fontWeight = FontWeight.Bold,
                            modifier = Modifier.padding(horizontal = 3.dp, vertical = 0.5.dp)
                        )
                    }

                    Text(
                        text = "Peman An Goud (HTG)",
                        color = Color(0xFFFBBF24),
                        fontSize = 10.sp,
                        fontWeight = FontWeight.Bold,
                        maxLines = 1
                    )
                }

                Spacer(modifier = Modifier.height(1.dp))

                Text(
                    text = "Fè depo ak renouvèlman rapid an Goud (HTG) avèk MonCash ak Natcash.",
                    color = Color(0xFFCBD5E1),
                    fontSize = 8.5.sp,
                    lineHeight = 11.sp,
                    maxLines = 2,
                    overflow = TextOverflow.Ellipsis
                )
            }
        }

        Spacer(modifier = Modifier.width(6.dp))

        // Action Button: Peye 📱
        Button(
            onClick = onPayClick,
            colors = ButtonDefaults.buttonColors(containerColor = Color.Transparent),
            contentPadding = PaddingValues(horizontal = 7.dp, vertical = 2.dp),
            shape = RoundedCornerShape(8.dp),
            modifier = Modifier
                .height(26.dp)
                .background(
                    Brush.horizontalGradient(
                        listOf(Color(0xFF10B981), Color(0xFF059669))
                    ),
                    shape = RoundedCornerShape(8.dp)
                )
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Text(
                    text = "Peye",
                    color = Color.White,
                    fontWeight = FontWeight.Bold,
                    fontSize = 9.5.sp
                )
                Spacer(modifier = Modifier.width(2.dp))
                Icon(
                    imageVector = Icons.Default.PhoneAndroid,
                    contentDescription = null,
                    tint = Color.White,
                    modifier = Modifier.size(10.dp)
                )
            }
        }
    }
}

/**
 * SLIDE 3: RECHARGE SMARTONE IPTV (Compact)
 */
@Composable
private fun Slide3RechargeIptvCompact(
    onSupportClick: () -> Unit
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Row(
            modifier = Modifier.weight(1f),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(
                modifier = Modifier
                    .size(30.dp)
                    .clip(RoundedCornerShape(7.dp))
                    .background(
                        Brush.linearGradient(
                            listOf(Color(0xFF06B6D4), Color(0xFF2563EB))
                        )
                    ),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = Icons.Default.Tv,
                    contentDescription = null,
                    tint = Color.White,
                    modifier = Modifier.size(16.dp)
                )
            }

            Spacer(modifier = Modifier.width(7.dp))

            Column(modifier = Modifier.weight(1f)) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(4.dp)
                ) {
                    Surface(
                        shape = RoundedCornerShape(3.dp),
                        color = Color(0x3306B6D4),
                        border = CardDefaults.outlinedCardBorder().copy(
                            brush = androidx.compose.ui.graphics.SolidColor(Color(0xFF06B6D4))
                        )
                    ) {
                        Text(
                            text = "SMARTONE IPTV",
                            color = Color(0xFF67E8F9),
                            fontSize = 8.sp,
                            fontWeight = FontWeight.Bold,
                            modifier = Modifier.padding(horizontal = 3.dp, vertical = 0.5.dp)
                        )
                    }

                    Text(
                        text = "Sèvis Rapid & Sekirize",
                        color = Color(0xFFFBBF24),
                        fontSize = 10.sp,
                        fontWeight = FontWeight.Bold,
                        maxLines = 1
                    )
                }

                Spacer(modifier = Modifier.height(1.dp))

                Text(
                    text = "Kontakte nou kounye a pou nou ka recharge kont SmartOne IPTV ou byen vit!",
                    color = Color(0xFFCBD5E1),
                    fontSize = 8.5.sp,
                    lineHeight = 11.sp,
                    maxLines = 2,
                    overflow = TextOverflow.Ellipsis
                )
            }
        }

        Spacer(modifier = Modifier.width(6.dp))

        // Action Button: Pou Sipò 🎧
        Button(
            onClick = onSupportClick,
            colors = ButtonDefaults.buttonColors(containerColor = Color.Transparent),
            contentPadding = PaddingValues(horizontal = 7.dp, vertical = 2.dp),
            shape = RoundedCornerShape(8.dp),
            modifier = Modifier
                .height(26.dp)
                .background(
                    Brush.horizontalGradient(
                        listOf(Color(0xFFF59E0B), Color(0xFFD97706))
                    ),
                    shape = RoundedCornerShape(8.dp)
                )
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Text(
                    text = "Pou Sipò",
                    color = Color(0xFF020617),
                    fontWeight = FontWeight.Bold,
                    fontSize = 9.5.sp
                )
                Spacer(modifier = Modifier.width(2.dp))
                Icon(
                    imageVector = Icons.Default.HeadsetMic,
                    contentDescription = null,
                    tint = Color(0xFF020617),
                    modifier = Modifier.size(10.dp)
                )
            }
        }
    }
}

/**
 * SLIDE 4: SIPÒ OFISYÈL EMAIL (Compact)
 */
@Composable
private fun Slide4EmailSupportCompact(
    onCopyClick: () -> Unit
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Row(
            modifier = Modifier.weight(1f),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(
                modifier = Modifier
                    .size(30.dp)
                    .clip(RoundedCornerShape(7.dp))
                    .background(Color(0xFF1E293B))
                    .border(1.dp, Color(0xFF334155), RoundedCornerShape(7.dp)),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = Icons.Default.MarkEmailRead,
                    contentDescription = null,
                    tint = Color(0xFFFBBF24),
                    modifier = Modifier.size(16.dp)
                )
            }

            Spacer(modifier = Modifier.width(7.dp))

            Column(modifier = Modifier.weight(1f)) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(4.dp)
                ) {
                    Surface(
                        shape = RoundedCornerShape(3.dp),
                        color = Color(0x33F59E0B),
                        border = CardDefaults.outlinedCardBorder().copy(
                            brush = androidx.compose.ui.graphics.SolidColor(Color(0xFFF59E0B))
                        )
                    ) {
                        Text(
                            text = "SIPÒ OFISYÈL",
                            color = Color(0xFFFCD34D),
                            fontSize = 8.sp,
                            fontWeight = FontWeight.Bold,
                            modifier = Modifier.padding(horizontal = 3.dp, vertical = 0.5.dp)
                        )
                    }

                    Text(
                        text = "Atansyon 24/7",
                        color = Color(0xFF94A3B8),
                        fontSize = 9.sp,
                        fontWeight = FontWeight.Medium,
                        maxLines = 1
                    )
                }

                Spacer(modifier = Modifier.height(1.dp))

                Text(
                    text = SmartOneRepository.SUPPORT_EMAIL,
                    color = BrandCyan,
                    fontSize = 9.sp,
                    fontWeight = FontWeight.Bold,
                    letterSpacing = 0.2.sp,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
            }
        }

        Spacer(modifier = Modifier.width(6.dp))

        // Action Button: Kopye 📋
        Button(
            onClick = onCopyClick,
            colors = ButtonDefaults.buttonColors(containerColor = Color.Transparent),
            contentPadding = PaddingValues(horizontal = 7.dp, vertical = 2.dp),
            shape = RoundedCornerShape(8.dp),
            modifier = Modifier
                .height(26.dp)
                .background(
                    Brush.horizontalGradient(
                        listOf(Color(0xFF06B6D4), Color(0xFF2563EB))
                    ),
                    shape = RoundedCornerShape(8.dp)
                )
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Text(
                    text = "Kopye",
                    color = Color.White,
                    fontWeight = FontWeight.Bold,
                    fontSize = 9.5.sp
                )
                Spacer(modifier = Modifier.width(2.dp))
                Icon(
                    imageVector = Icons.Default.ContentCopy,
                    contentDescription = null,
                    tint = Color.White,
                    modifier = Modifier.size(10.dp)
                )
            }
        }
    }
}
