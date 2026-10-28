package com.example.ui.screens

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Star
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.model.Channel
import com.example.ui.components.ChannelCard
import com.example.ui.theme.*

@Composable
fun FavoritesScreen(
    channels: List<Channel>,
    favorites: Set<String>,
    currentPlaying: Channel?,
    isPlaying: Boolean,
    onSelectChannel: (Channel) -> Unit,
    onToggleFavorite: (String) -> Unit,
    modifier: Modifier = Modifier
) {
    val favoriteChannels = channels.filter { favorites.contains(it.id) }

    LazyColumn(
        modifier = modifier.fillMaxSize(),
        contentPadding = PaddingValues(16.dp),
        verticalArrangement = Arrangement.spacedBy(10.dp)
    ) {
        item {
            Text(
                text = "Mes Chaînes Favorites ⭐",
                color = DarkTextPrimary,
                fontSize = 18.sp,
                fontWeight = FontWeight.Bold,
                modifier = Modifier.padding(bottom = 4.dp)
            )
        }

        if (favoriteChannels.isEmpty()) {
            item {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    colors = CardDefaults.cardColors(containerColor = DarkSurface)
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(32.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Icon(
                            imageVector = Icons.Default.Star,
                            contentDescription = null,
                            tint = BrandGold,
                            modifier = Modifier.size(48.dp)
                        )
                        Spacer(modifier = Modifier.height(12.dp))
                        Text(
                            text = "Vous n'avez pas encore de favoris.",
                            color = DarkTextPrimary,
                            fontSize = 15.sp,
                            fontWeight = FontWeight.SemiBold
                        )
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(
                            text = "Ajoutez des chaînes en touchant l'icône d'étoile ⭐ sur n'importe quelle chaîne.",
                            color = DarkTextSecondary,
                            fontSize = 13.sp
                        )
                    }
                }
            }
        } else {
            items(favoriteChannels, key = { it.id }) { channel ->
                ChannelCard(
                    channel = channel,
                    isCurrentlyPlaying = currentPlaying?.id == channel.id && isPlaying,
                    isFavorite = true,
                    onSelect = { onSelectChannel(channel) },
                    onToggleFavorite = { onToggleFavorite(channel.id) }
                )
            }
        }
    }
}
