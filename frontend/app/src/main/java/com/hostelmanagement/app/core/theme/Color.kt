package com.hostelmanagement.app.core.theme

import androidx.compose.ui.graphics.Color

// ==========================================
// SmartTracking Brand Palette Tokens
// ==========================================

// Core Brand Purple Tokens
val BrandPrimary = Color(0xFF5946D8)        // Primary Action Purple
val BrandPrimaryDark = Color(0xFF4332B5)    // Active / Pressed
val BrandPrimaryLight = Color(0xFF7B6BF0)   // Light Accent
val BrandContainer = Color(0xFFEFECFF)      // Soft Purple Background Container
val BrandOnContainer = Color(0xFF20147B)    // Deep Purple on Container

// Neutral Surfaces & Canvas
val SurfaceNeutral = Color(0xFFF7F7FA)      // Clean Purple-White Canvas
val PureWhite = Color(0xFFFFFFFF)           // Card Surfaces & Modals
val SurfaceVariantNeutral = Color(0xFFF0EFF6) // Secondary Containers

// Charcoal Typography Hierarchy
val TextPrimary = Color(0xFF191927)         // Deep Charcoal Headlines & Titles
val TextSecondary = Color(0xFF747487)       // Slate Subtitles & Secondary Body
val TextMuted = Color(0xFF9E9EAF)           // Placeholders & Minor Captions

// Borders & Dividers
val BorderSubtle = Color(0xFFE9E8EF)        // 1dp Hairline Surface Borders
val BorderStrong = Color(0xFFD6D5E0)        // Active / Focused Borders

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

val StatusNeutralBg = BrandContainer
val StatusNeutralBorder = Color(0xFFD9D4FA)
val StatusNeutralText = BrandPrimary

// ==========================================
// Backward-Compatibility Aliases
// ==========================================
val Purple950 = TextPrimary
val Purple900 = BrandOnContainer
val Purple800 = Color(0xFF3B28A8)
val Purple700 = BrandPrimaryDark
val Purple600 = BrandPrimary
val Purple500 = BrandPrimaryLight
val Purple400 = Color(0xFFA598F8)
val Purple300 = Color(0xFFC7BFFB)
val Purple200 = Color(0xFFDDD8FC)
val Purple100 = Color(0xFFE6E2FC)
val Purple50  = BrandContainer
val PurpleAppBg = SurfaceNeutral

val Slate950 = TextPrimary
val Slate900 = TextPrimary
val Slate800 = Color(0xFF2D2D3F)
val Slate700 = Color(0xFF4C4C60)
val Slate600 = TextSecondary
val Slate500 = TextSecondary
val Slate400 = TextMuted
val Slate300 = BorderStrong
val Slate200 = BorderSubtle
val Slate100 = BrandContainer
val Slate50  = SurfaceNeutral

val AccentPrimary = BrandPrimary
val AccentInteractive = BrandPrimary
