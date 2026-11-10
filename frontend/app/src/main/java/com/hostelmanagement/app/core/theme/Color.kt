package com.hostelmanagement.app.core.theme

import androidx.compose.ui.graphics.Color

// ==========================================
// Official SmartTrack Brand Palette Tokens
// ==========================================

// Primary Brand Teal Tokens
val BrandTeal = Color(0xFF149EA5)           // Primary Teal
val BrandTealDark = Color(0xFF0F7D83)       // Active / Pressed Teal
val BrandTealLight = Color(0xFF1DBAC2)      // Light Accent Teal
val SoftTeal = Color(0xFFE7F5F5)            // Soft Teal Container Background
val SoftTealBorder = Color(0xFFBCE3E5)      // Subtle Teal Border

// Secondary Brand Orange Tokens
val BrandOrange = Color(0xFFF07832)         // Accent Brand Orange
val BrandOrangeDark = Color(0xFFD66320)     // Pressed Orange
val BrandOrangeLight = Color(0xFFFF8B4A)    // Highlight Orange
val SoftOrange = Color(0xFFFFF1E8)          // Soft Orange Container
val SoftOrangeBorder = Color(0xFFFFD9C2)    // Subtle Orange Border

// Neutral Canvas & Surfaces
val CanvasBackground = Color(0xFFF5F8F8)    // Clean App Canvas
val SurfaceWhite = Color(0xFFFFFFFF)        // Card Surfaces & Modals
val SurfaceVariantNeutral = Color(0xFFEDF2F2) // Secondary Neutral Surface

// Charcoal Typography Hierarchy
val BrandCharcoal = Color(0xFF292A30)       // Deep Brand Charcoal
val TextPrimary = Color(0xFF202329)         // Primary Headlines & Body
val TextSecondary = Color(0xFF747B82)       // Secondary Slate Captions
val TextMuted = Color(0xFF9AA0A6)           // Placeholders & Minor Text

// Borders & Dividers
val BorderSubtle = Color(0xFFE5E9E9)        // 1dp Hairline Surface Borders
val BorderStrong = Color(0xFFCCD2D2)        // Active / Focused Borders

// Semantic Status Tokens
val StatusPaidBg = Color(0xFFE6F5EE)
val StatusPaidBorder = Color(0xFFA3E6CD)
val StatusPaidText = Color(0xFF16845B)
val StatusGreen = StatusPaidText

val StatusPendingBg = Color(0xFFFCF4E6)
val StatusPendingBorder = Color(0xFFFCE2B8)
val StatusPendingText = Color(0xFFD58A22)

val StatusOverdueBg = Color(0xFFFDECEE)
val StatusOverdueBorder = Color(0xFFF9B6BC)
val StatusOverdueText = Color(0xFFD84A55)

val StatusNeutralBg = SoftTeal
val StatusNeutralBorder = SoftTealBorder
val StatusNeutralText = BrandTeal

// ==========================================
// Compatibility & System Aliases
// ==========================================
val BrandPrimary = BrandTeal
val BrandPrimaryDark = BrandTealDark
val BrandPrimaryLight = BrandTealLight
val BrandContainer = SoftTeal
val BrandOnContainer = BrandTealDark

val SurfaceNeutral = CanvasBackground
val PureWhite = SurfaceWhite

val Purple950 = BrandCharcoal
val Purple900 = BrandCharcoal
val Purple800 = BrandTealDark
val Purple700 = BrandTealDark
val Purple600 = BrandTeal
val Purple500 = BrandTeal
val Purple400 = BrandTealLight
val Purple300 = BrandTealLight
val Purple200 = SoftTealBorder
val Purple100 = SoftTeal
val Purple50 = CanvasBackground

val Slate900 = TextPrimary
val Slate800 = BrandCharcoal
val Slate700 = BrandCharcoal
val Slate600 = TextSecondary
val Slate500 = TextSecondary
val Slate400 = TextMuted
val Slate300 = BorderStrong
val Slate200 = BorderSubtle
val Slate100 = SurfaceVariantNeutral
val Slate50 = CanvasBackground

val PrimaryAction = BrandTeal
val PrimaryActionDark = BrandTealDark
val BackgroundLight = CanvasBackground
val SurfaceCard = SurfaceWhite
val TextTitle = TextPrimary
val TextBody = TextSecondary
