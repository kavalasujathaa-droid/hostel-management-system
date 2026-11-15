package com.hostelmanagement.app.ui.auth

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowForward
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.hostelmanagement.app.R
import com.hostelmanagement.app.core.theme.AppDimens
import com.hostelmanagement.app.core.theme.PrimaryPurple
import com.hostelmanagement.app.core.theme.PureWhite
import com.hostelmanagement.app.core.theme.SoftLavenderBorder
import com.hostelmanagement.app.core.theme.SplashNightGradient
import com.hostelmanagement.app.core.theme.TextMuted
import com.hostelmanagement.app.core.theme.TextPrimary
import com.hostelmanagement.app.core.theme.TextSecondary
import com.hostelmanagement.app.ui.components.AppButton
import com.hostelmanagement.app.ui.components.SmartTrackLogoIcon

@Composable
fun SplashScreen(
    onGetStartedClick: () -> Unit
) {
    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(SplashNightGradient)
    ) {
        // Top Section: Illustration & Sky
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .align(Alignment.TopCenter)
                .padding(top = 40.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Image(
                painter = painterResource(id = R.drawable.ic_hostel_illustration),
                contentDescription = "SmartTracking Hostel Illustration",
                modifier = Modifier
                    .fillMaxWidth(0.92f)
                    .height(240.dp)
            )
        }

        // Bottom Section: Curved White Card with Branding & CTA
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .align(Alignment.BottomCenter)
                .clip(RoundedCornerShape(topStart = 32.dp, topEnd = 32.dp))
                .background(PureWhite)
                .padding(horizontal = AppDimens.Spacing24, vertical = AppDimens.Spacing28)
        ) {
            Column(
                modifier = Modifier.fillMaxWidth(),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.Center
            ) {
                // Official Brand Logo (Teal & Orange pin)
                SmartTrackLogoIcon(size = 56.dp)

                Spacer(modifier = Modifier.height(AppDimens.Spacing12))

                // Brand Title & Subtitle matching Screen 1
                Text(
                    text = "SmartTracking",
                    fontSize = 26.sp,
                    fontWeight = FontWeight.ExtraBold,
                    color = PrimaryPurple
                )

                Spacer(modifier = Modifier.height(AppDimens.Spacing4))

                Text(
                    text = "Hostel Management System",
                    fontSize = 14.sp,
                    fontWeight = FontWeight.Medium,
                    color = TextSecondary
                )

                Spacer(modifier = Modifier.height(AppDimens.Spacing20))

                // 3 Carousel / Progress Dots
                Row(
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Box(
                        modifier = Modifier
                            .size(width = 24.dp, height = 8.dp)
                            .clip(RoundedCornerShape(4.dp))
                            .background(PrimaryPurple)
                    )
                    Box(
                        modifier = Modifier
                            .size(8.dp)
                            .clip(CircleShape)
                            .background(SoftLavenderBorder)
                    )
                    Box(
                        modifier = Modifier
                            .size(8.dp)
                            .clip(CircleShape)
                            .background(SoftLavenderBorder)
                    )
                }

                Spacer(modifier = Modifier.height(AppDimens.Spacing24))

                // "Get Started ->" Primary Gradient Button
                AppButton(
                    text = "Get Started  →",
                    onClick = onGetStartedClick,
                    modifier = Modifier.fillMaxWidth()
                )

                Spacer(modifier = Modifier.height(AppDimens.Spacing12))

                // Supporting Subtitle
                Text(
                    text = "Manage your hostel easily",
                    fontSize = 13.sp,
                    color = TextMuted,
                    fontWeight = FontWeight.Normal
                )
            }
        }
    }
}
