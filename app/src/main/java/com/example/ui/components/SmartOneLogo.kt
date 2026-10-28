package com.example.ui.components

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

// Exact colors extracted from the official SmartOne IPTV Logo image:
val LogoSapphireBlue = Color(0xFF3B82F6) // "Smart" metallic blue
val LogoTealCyan = Color(0xFF06B6D4)     // "One" turquoise cyan
val LogoCopperGold = Color(0xFFE0A96D)   // "IPTV" 3D extruded metallic copper-gold
val LogoStarGlow = Color(0xFFFBBF24)     // Celestial gold star

enum class SmartOneLogoSize(val iconSize: Dp, val fontSize: TextUnit, val spacing: Dp) {
    HEADER_COMPACT(20.dp, 12.sp, 5.dp),
    SMALL(22.dp, 13.sp, 5.dp),
    MEDIUM(26.dp, 15.sp, 6.dp),
    LARGE(40.dp, 19.sp, 8.dp)
}

/**
 * Brand Logo component (Emblem + "Smart" [Blue] + "One" [Cyan] + "IPTV" [Copper Gold])
 * Exactly matching the user's official logo typography and emblem.
 */
@Composable
fun SmartOneBrandLogo(
    modifier: Modifier = Modifier,
    size: SmartOneLogoSize = SmartOneLogoSize.MEDIUM,
    showSubtitle: Boolean = false
) {
    Column(
        modifier = modifier,
        horizontalAlignment = Alignment.Start
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Sapphire Planet + Golden Ring Emblem
            Box(
                modifier = Modifier
                    .size(size.iconSize)
                    .clip(CircleShape)
                    .background(Color(0xFF0A0F1D)),
                contentAlignment = Alignment.Center
            ) {
                Image(
                    painter = painterResource(id = R.drawable.ic_smartone_emblem),
                    contentDescription = "SmartOne Emblem",
                    modifier = Modifier.fillMaxSize(),
                    contentScale = ContentScale.Fit
                )
            }

            Spacer(modifier = Modifier.width(size.spacing))

            // 3D-styled brand name with official colors
            Row(verticalAlignment = Alignment.CenterVertically) {
                Text(
                    text = "Smart",
                    color = LogoSapphireBlue,
                    fontWeight = FontWeight.Bold,
                    fontSize = size.fontSize,
                    letterSpacing = 0.3.sp
                )
                Text(
                    text = "One ",
                    color = LogoTealCyan,
                    fontWeight = FontWeight.ExtraBold,
                    fontSize = size.fontSize,
                    letterSpacing = 0.3.sp
                )
                Surface(
                    shape = RoundedCornerShape(3.dp),
                    color = LogoCopperGold.copy(alpha = 0.18f),
                    border = CardDefaults.outlinedCardBorder().copy(
                        brush = Brush.horizontalGradient(listOf(LogoCopperGold, LogoStarGlow))
                    )
                ) {
                    Text(
                        text = "IPTV",
                        color = LogoCopperGold,
                        fontWeight = FontWeight.Black,
                        fontSize = (size.fontSize.value - 2.5f).coerceAtLeast(9f).sp,
                        letterSpacing = 0.4.sp,
                        modifier = Modifier.padding(horizontal = 4.dp, vertical = 0.5.dp)
                    )
                }
            }
        }

        if (showSubtitle) {
            Spacer(modifier = Modifier.height(2.dp))
            Text(
                text = "🌐 High-Quality Global Streaming 🌐",
                color = DarkTextMuted,
                fontSize = 10.sp,
                fontWeight = FontWeight.Medium,
                letterSpacing = 0.5.sp
            )
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
    height: Dp = 120.dp
) {
    Card(
        modifier = modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(14.dp)),
        colors = CardDefaults.cardColors(containerColor = Color(0xFF080D18)),
        border = CardDefaults.outlinedCardBorder().copy(
            brush = Brush.linearGradient(
                listOf(
                    LogoSapphireBlue.copy(alpha = 0.6f),
                    LogoTealCyan.copy(alpha = 0.4f),
                    LogoCopperGold.copy(alpha = 0.5f)
                )
            )
        ),
        shape = RoundedCornerShape(14.dp)
    ) {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(height)
                .background(
                    Brush.verticalGradient(
                        listOf(
                            Color(0xFF0A1122),
                            Color(0xFF060913)
                        )
                    )
                ),
            contentAlignment = Alignment.Center
        ) {
            Image(
                painter = painterResource(id = R.drawable.smartone_brand_banner),
                contentDescription = "SmartOne IPTV Official Logo - High-Quality Global Streaming",
                modifier = Modifier
                    .fillMaxSize()
                    .padding(8.dp),
                contentScale = ContentScale.Fit
            )
        }
    }
}
