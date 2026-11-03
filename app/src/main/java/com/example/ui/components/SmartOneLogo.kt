package com.example.ui.components

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.TextUnit
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.R
import com.example.ui.theme.*

// Exact colors prescribed by user and official SmartOne IPTV emblem:
val ColorSMBlue = Color(0xFF1D3557)   // #1d3557 - Deep Blue for "SM"
val ColorARTRed = Color(0xFF8B0000)   // #8b0000 - Deep Crimson Red for "ART"
val ColorONEGold = Color(0xFFD4AF37)  // #d4af37 - Golden Yellow for "ONE"
val LogoCopperGold = Color(0xFFD4AF37) // Metallic Gold Accent
val LogoSapphireBlue = Color(0xFF1D3557)
val LogoTealCyan = Color(0xFF06B6D4)

enum class SmartOneLogoSize(val iconSize: Dp, val fontSize: TextUnit, val spacing: Dp) {
    HEADER(44.dp, 18.sp, 9.dp),
    HEADER_COMPACT(34.dp, 16.sp, 8.dp),
    SMALL(26.dp, 13.sp, 6.dp),
    MEDIUM(36.dp, 17.sp, 8.dp),
    LARGE(50.dp, 21.sp, 11.dp)
}

/**
 * Tri-color bold brand typography for SMART ONE IPTV:
 * "SM" in Blue (#1d3557)
 * "ART" in Red (#8b0000)
 * "ONE" in Yellow/Gold (#d4af37)
 * With thick bold effect (FontWeight.Black) and optional IPTV badge.
 */
@Composable
fun SmartOneTriColorText(
    modifier: Modifier = Modifier,
    fontSize: TextUnit = 17.sp,
    showIptvBadge: Boolean = true,
    containerPill: Boolean = true
) {
    if (containerPill) {
        Surface(
            modifier = modifier,
            shape = RoundedCornerShape(20.dp),
            color = Color(0xFFF8FAFC), // Pure light surface so deep blue & deep red are ultra vibrant & sharp
            border = BorderStroke(1.dp, ColorONEGold.copy(alpha = 0.55f)),
            shadowElevation = 2.dp
        ) {
            Row(
                modifier = Modifier.padding(horizontal = 9.dp, vertical = 3.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "SM",
                    color = ColorSMBlue,
                    fontWeight = FontWeight.Black,
                    fontSize = fontSize,
                    letterSpacing = 0.4.sp
                )
                Text(
                    text = "ART",
                    color = ColorARTRed,
                    fontWeight = FontWeight.Black,
                    fontSize = fontSize,
                    letterSpacing = 0.4.sp
                )
                Text(
                    text = "ONE",
                    color = ColorONEGold,
                    fontWeight = FontWeight.Black,
                    fontSize = fontSize,
                    letterSpacing = 0.4.sp
                )
                if (showIptvBadge) {
                    Spacer(modifier = Modifier.width(5.dp))
                    Surface(
                        shape = RoundedCornerShape(4.dp),
                        color = Color(0xFF0F172A)
                    ) {
                        Text(
                            text = "IPTV",
                            color = Color.White,
                            fontWeight = FontWeight.Black,
                            fontSize = (fontSize.value - 4f).coerceAtLeast(10f).sp,
                            letterSpacing = 0.5.sp,
                            modifier = Modifier.padding(horizontal = 5.dp, vertical = 1.dp)
                        )
                    }
                }
            }
        }
    } else {
        Row(
            modifier = modifier,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = "SM",
                color = ColorSMBlue,
                fontWeight = FontWeight.Black,
                fontSize = fontSize,
                letterSpacing = 0.4.sp
            )
            Text(
                text = "ART",
                color = ColorARTRed,
                fontWeight = FontWeight.Black,
                fontSize = fontSize,
                letterSpacing = 0.4.sp
            )
            Text(
                text = "ONE",
                color = ColorONEGold,
                fontWeight = FontWeight.Black,
                fontSize = fontSize,
                letterSpacing = 0.4.sp
            )
            if (showIptvBadge) {
                Spacer(modifier = Modifier.width(5.dp))
                Surface(
                    shape = RoundedCornerShape(4.dp),
                    color = Color(0xFF0F172A),
                    border = BorderStroke(1.dp, ColorONEGold.copy(alpha = 0.5f))
                ) {
                    Text(
                        text = "IPTV",
                        color = ColorONEGold,
                        fontWeight = FontWeight.Black,
                        fontSize = (fontSize.value - 4f).coerceAtLeast(10f).sp,
                        letterSpacing = 0.5.sp,
                        modifier = Modifier.padding(horizontal = 4.5.dp, vertical = 1.dp)
                    )
                }
            }
        }
    }
}

/**
 * Brand Logo component (Official Circular Emblem + Tri-Color SMART ONE IPTV Typography)
 * Positioned across the entire application.
 */
@Composable
fun SmartOneBrandLogo(
    modifier: Modifier = Modifier,
    size: SmartOneLogoSize = SmartOneLogoSize.MEDIUM,
    showText: Boolean = true,
    showSubtitle: Boolean = false,
    usePill: Boolean = true
) {
    Row(
        modifier = modifier,
        verticalAlignment = Alignment.CenterVertically
    ) {
        // Official Emblem with bronze metallic ring & white disc
        Surface(
            modifier = Modifier.size(size.iconSize),
            shape = CircleShape,
            color = Color.White,
            shadowElevation = 3.dp,
            border = BorderStroke(1.5.dp, ColorONEGold)
        ) {
            Image(
                painter = painterResource(id = R.drawable.ic_smartone_emblem),
                contentDescription = "SmartOne IPTV Official Logo",
                modifier = Modifier.fillMaxSize(),
                contentScale = ContentScale.Fit
            )
        }

        if (showText) {
            Spacer(modifier = Modifier.width(size.spacing))

            Column(
                verticalArrangement = Arrangement.Center,
                horizontalAlignment = Alignment.Start
            ) {
                SmartOneTriColorText(
                    fontSize = size.fontSize,
                    showIptvBadge = true,
                    containerPill = usePill
                )

                if (showSubtitle) {
                    Spacer(modifier = Modifier.height(2.dp))
                    Text(
                        text = "High-Quality Global Streaming",
                        color = DarkTextMuted,
                        fontSize = 10.sp,
                        fontWeight = FontWeight.Medium,
                        letterSpacing = 0.4.sp
                    )
                }
            }
        }
    }
}

/**
 * Full Official SmartOne IPTV Brand Card
 * Showcases the exact visual logo from the user's reference image with high fidelity.
 */
@Composable
fun SmartOneFullBrandCard(
    modifier: Modifier = Modifier,
    height: Dp = 135.dp
) {
    Card(
        modifier = modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(16.dp)),
        colors = CardDefaults.cardColors(containerColor = Color(0xFF0A0F1E)),
        border = BorderStroke(
            1.5.dp,
            Brush.horizontalGradient(
                listOf(
                    ColorSMBlue,
                    ColorARTRed,
                    ColorONEGold
                )
            )
        ),
        shape = RoundedCornerShape(16.dp)
    ) {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(height)
                .background(
                    Brush.verticalGradient(
                        listOf(
                            Color(0xFF131D31),
                            Color(0xFF070B14)
                        )
                    )
                ),
            contentAlignment = Alignment.Center
        ) {
            Row(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(horizontal = 14.dp, vertical = 10.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.Center
            ) {
                // Official Emblem
                Surface(
                    modifier = Modifier.size(height * 0.72f),
                    shape = CircleShape,
                    color = Color.White,
                    shadowElevation = 6.dp,
                    border = BorderStroke(2.dp, ColorONEGold)
                ) {
                    Image(
                        painter = painterResource(id = R.drawable.ic_smartone_emblem),
                        contentDescription = "SmartOne IPTV Logo",
                        modifier = Modifier.fillMaxSize(),
                        contentScale = ContentScale.Fit
                    )
                }

                Spacer(modifier = Modifier.width(14.dp))

                Column(
                    horizontalAlignment = Alignment.Start,
                    verticalArrangement = Arrangement.Center
                ) {
                    SmartOneTriColorText(
                        fontSize = 18.sp,
                        showIptvBadge = true,
                        containerPill = true
                    )

                    Spacer(modifier = Modifier.height(4.dp))

                    Text(
                        text = "High-Quality Global Streaming",
                        color = Color(0xFFCBD5E1),
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Medium,
                        letterSpacing = 0.4.sp
                    )

                    Spacer(modifier = Modifier.height(4.dp))

                    Surface(
                        shape = RoundedCornerShape(4.dp),
                        color = ColorONEGold.copy(alpha = 0.16f),
                        border = BorderStroke(0.8.dp, ColorONEGold.copy(alpha = 0.45f))
                    ) {
                        Text(
                            text = "OFFICIAL PLATFORM",
                            color = ColorONEGold,
                            fontSize = 9.sp,
                            fontWeight = FontWeight.Bold,
                            letterSpacing = 0.6.sp,
                            modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                        )
                    }
                }
            }
        }
    }
}
