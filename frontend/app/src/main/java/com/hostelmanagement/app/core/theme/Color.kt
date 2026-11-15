package com.hostelmanagement.app.core.theme

import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color

// ==========================================
// Official SmartTrack Brand Logo Tokens (Strictly Preserved)
// ==========================================
val BrandLogoTeal = Color(0xFF149EA5)         // Location pin in logo
val BrandLogoOrange = Color(0xFFF07832)       // Radiating wireless waves in logo
val BrandTeal = BrandLogoTeal
val BrandOrange = BrandLogoOrange

// ==========================================
// Approved SmartTrack Purple & Lavender UI Tokens
// ==========================================
val PrimaryPurple = Color(0xFF7138D8)         // Primary action, CTA buttons, active tabs
val DeepPurple = Color(0xFF4B239F)            // Splash gradient, dark hero banners, pressed state
val LavenderAccent = Color(0xFFA78BFA)        // Secondary lavender accents, focus outlines
val LightLavender = Color(0xFFF1EDFF)         // Soft lavender container backgrounds, pills
val SoftLavenderBorder = Color(0xFFEAE6F3)    // 1dp subtle lavender borders

// Neutral Canvas & Surfaces
val PageBackground = Color(0xFFF7F5FC)        // Clean light lavender page background
val WhiteSurface = Color(0xFFFFFFFF)          // Elevated cards, top bars, bottom nav, dialogs
val CanvasBackground = PageBackground
val SurfaceWhite = WhiteSurface
val SurfaceNeutral = PageBackground
val PureWhite = WhiteSurface
val SurfaceVariantNeutral = Color(0xFFF0ECF8) // Secondary soft surface

// Typography Colors
val TextPrimary = Color(0xFF19172A)           // Charcoal primary text, titles, prominent metrics
val TextSecondary = Color(0xFF777389)         // Muted secondary text, captions, subtitles
val TextMuted = Color(0xFFA5A1B8)             // Input placeholders, disabled text
val BrandCharcoal = TextPrimary

// Borders & Hairlines
val BorderSubtle = SoftLavenderBorder         // 1dp hairline surface borders (#EAE6F3)
val BorderStrong = Color(0xFFD4CDE6)          // Active / focused borders

// Semantic Status Tokens
val StatusPaidBg = Color(0xFFE6F5EE)
val StatusPaidBorder = Color(0xFFA3E6CD)
val StatusPaidText = Color(0xFF16845B)
val StatusGreen = StatusPaidText

val StatusPendingBg = Color(0xFFFCF4E6)
val StatusPendingBorder = Color(0xFFFCE2B8)
val StatusPendingText = Color(0xFFE69A27)

val StatusOverdueBg = Color(0xFFFDECEE)
val StatusOverdueBorder = Color(0xFFF9B6BC)
val StatusOverdueText = Color(0xFFD84A55)

val StatusNeutralBg = LightLavender
val StatusNeutralBorder = SoftLavenderBorder
val StatusNeutralText = PrimaryPurple

// Gradients
val PurpleGradient = Brush.horizontalGradient(
    colors = listOf(Color(0xFF7138D8), Color(0xFF5B24C6))
)
val PurpleGradientVertical = Brush.verticalGradient(
    colors = listOf(Color(0xFF4B239F), Color(0xFF7138D8))
)
val SplashNightGradient = Brush.verticalGradient(
    colors = listOf(Color(0xFF261250), Color(0xFF381A75), Color(0xFF4B239F), Color(0xFF6E38D5))
)

// Primary compatibility aliases
val BrandPrimary = PrimaryPurple
val BrandPrimaryDark = DeepPurple
val BrandPrimaryLight = LavenderAccent
val BrandContainer = LightLavender
val BrandOnContainer = DeepPurple

val Purple950 = Color(0xFF1E0E40)
val Purple900 = Color(0xFF2D1560)
val Purple800 = Color(0xFF3D1C80)
val Purple700 = Color(0xFF4B239F)
val Purple600 = Color(0xFF5E2CB8)
val Purple500 = Color(0xFF7138D8)
val Purple400 = Color(0xFF8B5CF6)
val Purple300 = Color(0xFFA78BFA)
val Purple200 = Color(0xFFC4B5FD)
val Purple100 = Color(0xFFF1EDFF)
val Purple50 = Color(0xFFF7F5FC)

val Slate900 = TextPrimary
val Slate800 = Color(0xFF2E2A44)
val Slate700 = Color(0xFF484360)
val Slate600 = TextSecondary
val Slate500 = TextSecondary
val Slate400 = TextMuted
val Slate300 = BorderStrong
val Slate200 = BorderSubtle
val Slate100 = SurfaceVariantNeutral
val Slate50 = PageBackground

val PrimaryAction = PrimaryPurple
val PrimaryActionDark = DeepPurple
val BackgroundLight = PageBackground
val SurfaceCard = WhiteSurface
val TextTitle = TextPrimary
val TextBody = TextSecondary
