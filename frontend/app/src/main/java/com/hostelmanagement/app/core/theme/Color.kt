package com.hostelmanagement.app.core.theme

import androidx.compose.ui.graphics.Color

// ==========================================
// SmartTracking Modern Purple-White Theme Tokens
// ==========================================

// Core Purple Brand Palette
val Purple950 = Color(0xFF1E1B4B) // Deep Indigo text
val Purple900 = Color(0xFF3B0764)
val Purple800 = Color(0xFF5B21B6)
val Purple700 = Color(0xFF6D28D9) // Deep Purple fill
val Purple600 = Color(0xFF7C3AED) // Primary Brand Accent
val Purple500 = Color(0xFF8B5CF6) // Secondary / Active
val Purple400 = Color(0xFFA78BFA)
val Purple300 = Color(0xFFC4B5FD)
val Purple200 = Color(0xFFDDD6FE) // Focus borders
val Purple100 = Color(0xFFEDE9FE) // Chip & card tint
val Purple50  = Color(0xFFF5F3FF) // Subtle container tint
val PurpleAppBg = Color(0xFFFAF8FF) // Crisp white-violet background

// Backward-compatible Slate / Neutral Mapping
val Slate950 = Color(0xFF0F172A)
val Slate900 = Purple950          // Primary text & dark accents
val Slate800 = Color(0xFF1E293B)
val Slate700 = Color(0xFF334155)
val Slate600 = Color(0xFF64748B)  // Secondary descriptive text
val Slate500 = Color(0xFF94A3B8)
val Slate400 = Color(0xFFA1A1AA)  // Placeholders
val Slate300 = Color(0xFFCBD5E1)  // Border default
val Slate200 = Color(0xFFE2E8F0)
val Slate100 = Purple50           // Surface variant & soft chips
val Slate50  = PurpleAppBg        // App background
val PureWhite = Color(0xFFFFFFFF)

// Interactive Brand Tokens
val AccentPrimary = Purple600
val AccentInteractive = Purple600
val BorderSubtle = Color(0xFFE9D5FF) // 1dp hairline purple border

// Semantic Status Tokens
val StatusPaidBg = Color(0xFFECFDF5)
val StatusPaidBorder = Color(0xFFA7F3D0)
val StatusPaidText = Color(0xFF065F46)

val StatusPendingBg = Color(0xFFFFFBEB)
val StatusPendingBorder = Color(0xFFFDE68A)
val StatusPendingText = Color(0xFF92400E)

val StatusOverdueBg = Color(0xFFFEF2F2)
val StatusOverdueBorder = Color(0xFFFECACA)
val StatusOverdueText = Color(0xFF991B1B)

val StatusNeutralBg = Purple50
val StatusNeutralBorder = BorderSubtle
val StatusNeutralText = Purple700
