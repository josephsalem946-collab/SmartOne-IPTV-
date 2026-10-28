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
            contentPadding = PaddingValues(horizontal = 14.dp, vertical = 10.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
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
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(12.dp))
                        .background(
                            Brush.horizontalGradient(
                                listOf(
                                    Color(0xFF0F172A),
                                    Color(0xFF0B1120)
                                )
                            )
                        )
                        .border(1.dp, BrandCyan.copy(alpha = 0.25f), RoundedCornerShape(12.dp))
                        .padding(horizontal = 14.dp, vertical = 8.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    SmartOneBrandLogo(size = SmartOneLogoSize.MEDIUM, showSubtitle = true)

                    Surface(
                        shape = RoundedCornerShape(20.dp),
                        color = LogoCopperGold.copy(alpha = 0.15f),
                        border = CardDefaults.outlinedCardBorder().copy(
                            brush = androidx.compose.ui.graphics.SolidColor(LogoCopperGold)
                        )
                    ) {
                        Text(
                            text = "4K • 60fps",
                            color = LogoCopperGold,
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Bold,
                            modifier = Modifier.padding(horizontal = 8.dp, vertical = 3.dp)
                        )
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

            // Search filter badge if query active
            if (searchQuery.isNotBlank()) {
                item {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(8.dp))
                            .background(DarkSurfaceVariant)
                            .padding(horizontal = 12.dp, vertical = 8.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = "Recherche : \"$searchQuery\"",
                            color = BrandCyan,
                            fontSize = 13.sp,
                            fontWeight = FontWeight.Medium
                        )
                        IconButton(onClick = onClearSearch, modifier = Modifier.size(24.dp)) {
                            Icon(imageVector = Icons.Default.Close, contentDescription = "Effacer", tint = DarkTextSecondary)
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
                        FilterChip(
                            selected = isSelected,
                            onClick = { onSelectCategory(cat) },
                            label = { Text(cat, fontSize = 13.sp) },
                            colors = FilterChipDefaults.filterChipColors(
                                containerColor = DarkSurface,
                                labelColor = DarkTextSecondary,
                                selectedContainerColor = BrandBluePrimary,
                                selectedLabelColor = Color.White
                            ),
                            border = FilterChipDefaults.filterChipBorder(
                                enabled = true,
                                selected = isSelected,
                                borderColor = if (isSelected) BrandCyan else DarkBorder
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

        // Auto-hiding quick scroll to top button
        QuickScrollButton(
            listState = listState,
            modifier = Modifier
                .align(Alignment.BottomEnd)
                .padding(16.dp)
        )
    }
}
