package com.example.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.example.ui.theme.*

/**
 * Composant de Carte Produit Responsive et Anti-Débordement
 * Conçu selon les standards stricts UI/UX et Accessibilité Jetpack Compose :
 * - Hiérarchie typographique M3 avec tokens (Title, Label, Headline, Body)
 * - Tailles scalables en `sp` respectant le scaling système d'accessibilité
 * - Protection anti-débordement : maxLines, TextOverflow.Ellipsis, Row weight non-agressif
 * - Robustesse face au texte 2x plus long et scaling d'accessibilité (> 1.3x)
 */
@Composable
fun ResponsiveProductCard(
    title: String,
    category: String,
    price: String,
    originalPrice: String? = null,
    actionButtonLabel: String = "Acheter",
    onActionClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val density = LocalDensity.current
    val isLargeFont = density.fontScale >= 1.3f

    Card(
        modifier = modifier
            .fillMaxWidth()
            .wrapContentHeight()
            .clip(RoundedCornerShape(14.dp))
            .testTag("responsive_product_card"),
        colors = CardDefaults.cardColors(
            containerColor = DarkSurface
        ),
        border = CardDefaults.outlinedCardBorder().copy(
            brush = androidx.compose.ui.graphics.SolidColor(DarkBorder)
        ),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            // Catégorie / Badge (Caption / Label Token)
            Surface(
                shape = RoundedCornerShape(6.dp),
                color = DarkSurfaceVariant
            ) {
                Text(
                    text = category.uppercase(),
                    style = MaterialTheme.typography.labelSmall,
                    color = BrandCyan,
                    fontWeight = FontWeight.SemiBold,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                    modifier = Modifier
                        .padding(horizontal = 8.dp, vertical = 4.dp)
                        .testTag("product_category_badge")
                )
            }

            // Titre du produit : 2 lignes maximum avec ellipsis propre
            Text(
                text = title,
                style = MaterialTheme.typography.titleMedium,
                color = DarkTextPrimary,
                fontWeight = FontWeight.Bold,
                maxLines = 2,
                overflow = TextOverflow.Ellipsis,
                modifier = Modifier
                    .fillMaxWidth()
                    .testTag("product_title_text")
            )

            Spacer(modifier = Modifier.height(4.dp))

            // Section Prix et Bouton d'action
            // S'adapte intelligemment en Colonne si le scaling de texte utilisateur est très élevé (>= 130%)
            if (isLargeFont) {
                Column(
                    modifier = Modifier.fillMaxWidth(),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    PriceSection(price = price, originalPrice = originalPrice)

                    Button(
                        onClick = onActionClick,
                        colors = ButtonDefaults.buttonColors(containerColor = BrandBluePrimary),
                        shape = RoundedCornerShape(10.dp),
                        modifier = Modifier
                            .fillMaxWidth()
                            .defaultMinSize(minHeight = 48.dp)
                            .testTag("product_action_button")
                    ) {
                        Text(
                            text = actionButtonLabel,
                            style = MaterialTheme.typography.labelLarge,
                            fontWeight = FontWeight.SemiBold,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis
                        )
                    }
                }
            } else {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    // Conteneur Prix avec weight(1f, fill = false) pour ne jamais écraser le bouton
                    Box(modifier = Modifier.weight(1f, fill = false)) {
                        PriceSection(price = price, originalPrice = originalPrice)
                    }

                    Spacer(modifier = Modifier.width(12.dp))

                    Button(
                        onClick = onActionClick,
                        colors = ButtonDefaults.buttonColors(containerColor = BrandBluePrimary),
                        shape = RoundedCornerShape(10.dp),
                        contentPadding = PaddingValues(horizontal = 16.dp, vertical = 10.dp),
                        modifier = Modifier
                            .defaultMinSize(minWidth = 48.dp, minHeight = 48.dp)
                            .testTag("product_action_button")
                    ) {
                        Text(
                            text = actionButtonLabel,
                            style = MaterialTheme.typography.labelLarge,
                            fontWeight = FontWeight.SemiBold,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun PriceSection(
    price: String,
    originalPrice: String?,
    modifier: Modifier = Modifier
) {
    Column(modifier = modifier) {
        if (!originalPrice.isNullOrBlank()) {
            Text(
                text = originalPrice,
                style = MaterialTheme.typography.bodySmall,
                color = DarkTextMuted,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
        }
        Text(
            text = price,
            style = MaterialTheme.typography.headlineSmall,
            color = BrandCyan,
            fontWeight = FontWeight.Bold,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis,
            modifier = Modifier.testTag("product_price_text")
        )
    }
}
