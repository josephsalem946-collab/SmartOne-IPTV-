package com.example.ui.theme

import androidx.compose.ui.graphics.Color

// SmartOne IPTV Refined Brand Palette (Vibrant, high-contrast, eye-friendly)
val BrandBluePrimary = Color(0xFF1D4ED8) // Royal Blue 700 - accessible, professional
val BrandBlueHover = Color(0xFF1E40AF)
val BrandBlueLight = Color(0xFFEFF6FF)
val BrandCyan = Color(0xFF0284C7)        // Ocean Sky 600 - crisp and distinct on white
val BrandCyanLight = Color(0xFFE0F2FE)
val BrandGold = Color(0xFFD97706)        // Warm Amber 600 - rich gold, highly readable
val BrandGoldDark = Color(0xFFB45309)    // Amber 700
val BrandGoldLight = Color(0xFFFEF3C7)

// Status & Accents (Accessible & Balanced)
val StatusGreen = Color(0xFF059669)      // Emerald 600
val StatusGreenLight = Color(0xFFD1FAE5)
val StatusRed = Color(0xFFDC2626)        // Red 600
val StatusRedLight = Color(0xFFFEE2E2)
val StatusAmber = Color(0xFFD97706)      // Amber 600
val StatusAmberLight = Color(0xFFFEF3C7)

// Premium Light Theme Palette (Fond blanc clair, doux pour les yeux)
val LightBackground = Color(0xFFF8FAFC)      // Soft slate off-white canvas (anti-fatigue)
val LightSurface = Color(0xFFFFFFFF)         // Pure crisp white cards & sheets
val LightSurfaceVariant = Color(0xFFF1F5F9)  // Slate-100 chips, input backgrounds, subtle cards
val LightBorder = Color(0xFFE2E8F0)          // Slate-200 clean borders
val LightBorderSubtle = Color(0xFFEDF2F7)
val LightTextPrimary = Color(0xFF0F172A)     // Slate-900 maximum clarity, restful reading
val LightTextSecondary = Color(0xFF334155)   // Slate-700 clear secondary body
val LightTextMuted = Color(0xFF64748B)       // Slate-500 captions, metadata, timestamps

// Couleur Bleu Maron Foncé (Deep Dark Navy) prescrite pour l'en-tête et l'inaction
val ColorBleuMaronFonce = Color(0xFF0D1B2A)  // Bleu maron foncé riche
val ColorBleuMaronBorder = Color(0xFF1E2D4A) // Bordure subtile de séparation en-tête

// Direct Aliases so all components and dialogs automatically inherit the refined light theme
val DarkBackground = LightBackground
val DarkSurface = LightSurface
val DarkSurfaceVariant = LightSurfaceVariant
val DarkBorder = LightBorder
val DarkTextPrimary = LightTextPrimary
val DarkTextSecondary = LightTextSecondary
val DarkTextMuted = LightTextMuted
