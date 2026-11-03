package com.example.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
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
import com.example.data.model.Channel
import com.example.data.repository.SmartOneRepository
import com.example.ui.components.*
import com.example.ui.theme.*

@Composable
fun HomeScreen(
    channels: List<Channel>,
    currentPlaying: Channel?,
    isPlaying: Boolean,
    favorites: Set<String>,
    activeSubscription: ActiveSubscription?,
    freeSecondsLeft: Int,
    selectedCategory: String,
    searchQuery: String,
    onSelectChannel: (Channel) -> Unit,
    onTogglePlayPause: () -> Unit,
    onStop: () -> Unit,
    onToggleFavorite: (String) -> Unit,
    onSelectCategory: (String) -> Unit,
    onClearSearch: () -> Unit,
    onOpenPlans: () -> Unit,
    isImmersiveMode: Boolean = false,
    modifier: Modifier = Modifier
) {
    val listState = rememberLazyListState()

    // Filter channels based on category and search query
    val filteredChannels = remember(channels, selectedCategory, searchQuery) {
        channels.filter { channel ->
            val matchCategory = if (selectedCategory == "Toutes") true else channel.category.equals(selectedCategory, ignoreCase = true)
            val matchQuery = if (searchQuery.isBlank()) true else {
                channel.name.contains(searchQuery, ignoreCase = true) ||
                channel.currentProgram.contains(searchQuery, ignoreCase = true) ||
                channel.category.contains(searchQuery, ignoreCase = true) ||
                channel.country.contains(searchQuery, ignoreCase = true)
            }
            matchCategory && matchQuery
        }
    }

    Box(modifier = modifier.fillMaxSize()) {
        LazyColumn(
            state = listState,
            modifier = Modifier.fillMaxSize(),
            contentPadding = PaddingValues(horizontal = 14.dp, vertical = if (isImmersiveMode) 0.dp else 10.dp),
            verticalArrangement = Arrangement.spacedBy(if (isImmersiveMode) 0.dp else 12.dp)
        ) {
            if (!isImmersiveMode) {
                // Free Timer / VIP Banner
                item {
                    FreeTimerBanner(
                        activeSubscription = activeSubscription,
                        freeSecondsLeft = freeSecondsLeft,
                        onOpenPlans = onOpenPlans
                    )
                }

                // Official SmartOne IPTV Brand Strip
                item {
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(12.dp),
                        colors = CardDefaults.cardColors(containerColor = LightSurface),
                        border = CardDefaults.outlinedCardBorder().copy(
                            brush = androidx.compose.ui.graphics.SolidColor(LightBorder)
                        ),
                        elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
                    ) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(horizontal = 14.dp, vertical = 8.dp),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            SmartOneBrandLogo(size = SmartOneLogoSize.MEDIUM, showSubtitle = true)

                            Surface(
                                shape = RoundedCornerShape(20.dp),
                                color = BrandGoldLight,
                                border = CardDefaults.outlinedCardBorder().copy(
                                    brush = androidx.compose.ui.graphics.SolidColor(BrandGold)
                                )
                            ) {
                                Text(
                                    text = "4K • 60fps",
                                    color = BrandGoldDark,
                                    fontSize = 11.sp,
                                    fontWeight = FontWeight.Bold,
                                    modifier = Modifier.padding(horizontal = 10.dp, vertical = 3.dp)
                                )
                            }
                        }
                    }
                }
            }

            // TV Live Player
            item {
                PlayerView(
                    channel = currentPlaying,
                    isPlaying = isPlaying,
                    isFavorite = currentPlaying?.id?.let { favorites.contains(it) } ?: false,
                    freeMinutesLeft = freeSecondsLeft / 60,
                    hasActiveSubscription = activeSubscription != null,
                    onTogglePlayPause = onTogglePlayPause,
                    onStop = onStop,
                    onToggleFavorite = { currentPlaying?.let { onToggleFavorite(it.id) } },
                    onOpenPlans = onOpenPlans
                )
            }

            if (!isImmersiveMode) {
                // Interactive Promotional Banner Carousel (USDT TRC20, Recharge, Support)
                item {
                    InteractivePromoBanner(
                        onNavigateToDeposit = onOpenPlans
                    )
                }

                // Quick Zapping Express Row
                item {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 2.dp)
                    ) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(horizontal = 2.dp, vertical = 4.dp),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Text(
                                    text = "⚡ Zapping Express",
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 14.sp,
                                    color = DarkTextPrimary
                                )
                                Spacer(modifier = Modifier.width(6.dp))
                                Surface(
                                    shape = RoundedCornerShape(10.dp),
                                    color = StatusRed.copy(alpha = 0.12f)
                                ) {
                                    Text(
                                        text = "LIVE",
                                        color = StatusRed,
                                        fontSize = 9.sp,
                                        fontWeight = FontWeight.Black,
                                        modifier = Modifier.padding(horizontal = 5.dp, vertical = 1.dp)
                                    )
                                }
                            }
                            Text(
                                text = "1-Tap pour regarder",
                                fontSize = 11.sp,
                                color = DarkTextMuted
                            )
                        }

                        val quickZapScroll = rememberScrollState()
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .horizontalScroll(quickZapScroll),
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            channels.take(12).forEach { ch ->
                                val isChPlaying = currentPlaying?.id == ch.id && isPlaying
                                Surface(
                                    shape = RoundedCornerShape(12.dp),
                                    color = if (isChPlaying) BrandBlueLight else LightSurface,
                                    border = androidx.compose.foundation.BorderStroke(
                                        1.dp,
                                        if (isChPlaying) BrandBluePrimary else LightBorder
                                    ),
                                    modifier = Modifier
                                        .clip(RoundedCornerShape(12.dp))
                                        .clickable { onSelectChannel(ch) }
                                ) {
                                    Row(
                                        modifier = Modifier.padding(horizontal = 10.dp, vertical = 7.dp),
                                        verticalAlignment = Alignment.CenterVertically
                                    ) {
                                        Text(text = ch.countryCode, fontSize = 16.sp)
                                        Spacer(modifier = Modifier.width(6.dp))
                                        Column {
                                            Text(
                                                text = ch.name,
                                                fontSize = 12.sp,
                                                fontWeight = if (isChPlaying) FontWeight.Bold else FontWeight.SemiBold,
                                                color = if (isChPlaying) BrandBluePrimary else LightTextPrimary,
                                                maxLines = 1
                                            )
                                            Text(
                                                text = ch.resolution,
                                                fontSize = 9.5.sp,
                                                color = if (isChPlaying) BrandBluePrimary else LightTextMuted
                                            )
                                        }
                                    }
                                }
                            }
                        }
                    }
                }
            }

            if (!isImmersiveMode) {
                // Search filter badge if query active
                if (searchQuery.isNotBlank()) {
                    item {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clip(RoundedCornerShape(8.dp))
                                .background(BrandBlueLight)
                                .border(1.dp, BrandBluePrimary.copy(alpha = 0.25f), RoundedCornerShape(8.dp))
                                .padding(horizontal = 12.dp, vertical = 8.dp),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = "Recherche : \"$searchQuery\"",
                                color = BrandBluePrimary,
                                fontSize = 13.sp,
                                fontWeight = FontWeight.SemiBold
                            )
                            IconButton(onClick = onClearSearch, modifier = Modifier.size(24.dp)) {
                                Icon(imageVector = Icons.Default.Close, contentDescription = "Effacer", tint = BrandBluePrimary)
                            }
                        }
                    }
                }

                // Category Horizontal Chips Filter
                item {
                    val scrollState = rememberScrollState()
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .horizontalScroll(scrollState),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        for (cat in SmartOneRepository.categories) {
                            val isSelected = cat == selectedCategory
                            val catIcon = when (cat.lowercase()) {
                                "toutes" -> "📺 "
                                "sport", "sports" -> "⚽ "
                                "cinéma", "films", "cinema" -> "🎬 "
                                "infos", "actualités", "nouvelles" -> "📰 "
                                "musique" -> "🎵 "
                                "jeunesse", "enfants" -> "🧸 "
                                "haïti", "haiti" -> "🇭🇹 "
                                "international", "monde" -> "🌍 "
                                "divertissement" -> "🍿 "
                                else -> "✨ "
                            }
                            FilterChip(
                                selected = isSelected,
                                onClick = { onSelectCategory(cat) },
                                label = {
                                    Text(
                                        text = "$catIcon$cat",
                                        fontSize = 12.sp,
                                        fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium
                                    )
                                },
                                colors = FilterChipDefaults.filterChipColors(
                                    containerColor = LightSurface,
                                    labelColor = LightTextSecondary,
                                    selectedContainerColor = BrandBluePrimary,
                                    selectedLabelColor = Color.White
                                ),
                                border = FilterChipDefaults.filterChipBorder(
                                    enabled = true,
                                    selected = isSelected,
                                    borderColor = if (isSelected) BrandBluePrimary else LightBorder
                                ),
                                shape = RoundedCornerShape(20.dp),
                                modifier = Modifier.testTag("cat_chip_$cat")
                            )
                        }
                    }
                }

                // Channels Section Header
                item {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(top = 4.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = if (selectedCategory == "Toutes") "Toutes les chaînes" else selectedCategory,
                            color = DarkTextPrimary,
                            fontSize = 17.sp,
                            fontWeight = FontWeight.Bold
                        )
                        Text(
                            text = "${filteredChannels.size} disponible(s)",
                            color = DarkTextMuted,
                            fontSize = 12.sp
                        )
                    }
                }

                // Channels List
                if (filteredChannels.isEmpty()) {
                    item {
                        Card(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(vertical = 24.dp),
                            colors = CardDefaults.cardColors(containerColor = DarkSurface)
                        ) {
                            Column(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(24.dp),
                                horizontalAlignment = Alignment.CenterHorizontally
                            ) {
                                Icon(
                                    imageVector = Icons.Default.SearchOff,
                                    contentDescription = null,
                                    tint = DarkTextMuted,
                                    modifier = Modifier.size(40.dp)
                                )
                                Spacer(modifier = Modifier.height(8.dp))
                                Text(
                                    text = "Aucune chaîne trouvée dans cette catégorie",
                                    color = DarkTextSecondary,
                                    fontSize = 14.sp
                                )
                            }
                        }
                    }
                } else {
                    items(filteredChannels, key = { it.id }) { channel ->
                        ChannelCard(
                            channel = channel,
                            isCurrentlyPlaying = currentPlaying?.id == channel.id && isPlaying,
                            isFavorite = favorites.contains(channel.id),
                            onSelect = { onSelectChannel(channel) },
                            onToggleFavorite = { onToggleFavorite(channel.id) }
                        )
                    }
                }
            }
        }

        // Auto-hiding quick scroll to top button
        if (!isImmersiveMode) {
            QuickScrollButton(
                listState = listState,
                modifier = Modifier
                    .align(Alignment.BottomEnd)
                    .padding(16.dp)
            )
        }
    }
}
