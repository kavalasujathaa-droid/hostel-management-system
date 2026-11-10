package com.hostelmanagement.app.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.hostelmanagement.app.R
import com.hostelmanagement.app.core.theme.BrandOrange
import com.hostelmanagement.app.core.theme.BrandTeal
import com.hostelmanagement.app.core.theme.SoftTeal
import com.hostelmanagement.app.core.theme.SoftTealBorder
import com.hostelmanagement.app.core.theme.TextSecondary

@Composable
fun SmartTrackLogoIcon(
    modifier: Modifier = Modifier,
    size: Dp = 48.dp
) {
    Box(
        modifier = modifier
            .size(size)
            .clip(RoundedCornerShape(size * 0.28f))
            .background(SoftTeal)
            .border(1.dp, SoftTealBorder, RoundedCornerShape(size * 0.28f)),
        contentAlignment = Alignment.Center
    ) {
        Icon(
            painter = painterResource(id = R.drawable.ic_smarttrack_logo),
            contentDescription = "SmartTrack Logo",
            tint = androidx.compose.ui.graphics.Color.Unspecified,
            modifier = Modifier.size(size * 0.72f)
        )
    }
}

@Composable
fun SmartTrackWordmark(
    modifier: Modifier = Modifier,
    fontSize: Float = 26f
) {
    Row(
        modifier = modifier,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(
            text = "SMART",
            color = BrandTeal,
            fontSize = fontSize.sp,
            fontWeight = FontWeight.ExtraBold,
            letterSpacing = 0.5.sp
        )
        Text(
            text = "TRACK",
            color = BrandOrange,
            fontSize = fontSize.sp,
            fontWeight = FontWeight.ExtraBold,
            letterSpacing = 0.5.sp
        )
    }
}

@Composable
fun SmartTrackBrandHeader(
    modifier: Modifier = Modifier,
    iconSize: Dp = 56.dp,
    showSubtitle: Boolean = true,
    subtitleText: String = "Hostel Management System"
) {
    Column(
        modifier = modifier,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        SmartTrackLogoIcon(size = iconSize)
        Spacer(modifier = Modifier.height(12.dp))
        SmartTrackWordmark(fontSize = 26f)
        if (showSubtitle) {
            Spacer(modifier = Modifier.height(4.dp))
            Text(
                text = subtitleText,
                color = TextSecondary,
                fontSize = 13.sp,
                fontWeight = FontWeight.Medium
            )
        }
    }
}
