package com.hostelmanagement.app.core.theme

import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Shapes

val AppShapes = Shapes(
    extraSmall = RoundedCornerShape(AppDimens.Spacing4),
    small = RoundedCornerShape(AppDimens.CornerSmall),
    medium = RoundedCornerShape(AppDimens.CornerMedium),
    large = RoundedCornerShape(AppDimens.CornerLarge),
    extraLarge = RoundedCornerShape(AppDimens.CornerXLarge)
)
