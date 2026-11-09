package com.example.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.input.pointer.PointerEventPass
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.unit.dp
import com.example.data.model.Channel

/**
 * Pure video player display displayed after 45 seconds of screen inactivity.
 * Shows strictly the video playback canvas without any buttons, text, overlays, or navigation,
 * until the user interacts with the screen again.
 */
@Composable
fun PureVideoPlayerScreen(
    channel: Channel?,
    isPlaying: Boolean,
    playerBrightness: Float,
    playerContrast: Float,
    onUserReaction: () -> Unit,
    modifier: Modifier = Modifier
) {
    Box(
        modifier = modifier
            .fillMaxSize()
            .background(Color.Black)
            .pointerInput(Unit) {
                awaitPointerEventScope {
                    while (true) {
                        val event = awaitPointerEvent(PointerEventPass.Initial)
                        if (event.changes.any { it.pressed }) {
                            onUserReaction()
                        }
                    }
                }
            }
            .clickable(
                interactionSource = remember { MutableInteractionSource() },
                indication = null
            ) {
                onUserReaction()
            }
            .testTag("pure_fullscreen_video_player"),
        contentAlignment = Alignment.Center
    ) {
        if (channel != null) {
            // Fullscreen Video Stream Canvas
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .background(
                        Brush.verticalGradient(
                            listOf(Color(0xFF0F172A), Color(0xFF030712))
                        )
                    ),
                contentAlignment = Alignment.Center
            ) {
                // Animated live stream audio/video waveform visualizer
                if (isPlaying) {
                    StreamVisualizer(
                        modifier = Modifier
                            .fillMaxWidth(0.92f)
                            .height(280.dp)
                    )
                }

                // Brightness adjustment layer
                if (playerBrightness < 1.0f) {
                    Box(
                        modifier = Modifier
                            .fillMaxSize()
                            .background(Color.Black.copy(alpha = (1.0f - playerBrightness) * 0.85f))
                    )
                } else if (playerBrightness > 1.0f) {
                    Box(
                        modifier = Modifier
                            .fillMaxSize()
                            .background(Color.White.copy(alpha = (playerBrightness - 1.0f) * 0.40f))
                    )
                }

                // Contrast adjustment layer
                if (playerContrast != 1.0f) {
                    if (playerContrast > 1.0f) {
                        Box(
                            modifier = Modifier
                                .fillMaxSize()
                                .background(
                                    Brush.radialGradient(
                                        colors = listOf(
                                            Color.Transparent,
                                            Color.Black.copy(alpha = (playerContrast - 1.0f) * 0.35f)
                                        )
                                    )
                                )
                        )
                    } else {
                        Box(
                            modifier = Modifier
                                .fillMaxSize()
                                .background(Color.Gray.copy(alpha = (1.0f - playerContrast) * 0.30f))
                        )
                    }
                }
            }
        }
    }
}
