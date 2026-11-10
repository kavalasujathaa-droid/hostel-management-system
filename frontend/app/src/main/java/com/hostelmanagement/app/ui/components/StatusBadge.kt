package com.hostelmanagement.app.ui.components

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.unit.dp
import com.hostelmanagement.app.core.theme.AppDimens
import com.hostelmanagement.app.core.theme.AppShapes
import com.hostelmanagement.app.core.theme.AppTypography
import com.hostelmanagement.app.core.theme.StatusNeutralBg
import com.hostelmanagement.app.core.theme.StatusNeutralBorder
import com.hostelmanagement.app.core.theme.StatusNeutralText
import com.hostelmanagement.app.core.theme.StatusOverdueBg
import com.hostelmanagement.app.core.theme.StatusOverdueBorder
import com.hostelmanagement.app.core.theme.StatusOverdueText
import com.hostelmanagement.app.core.theme.StatusPaidBg
import com.hostelmanagement.app.core.theme.StatusPaidBorder
import com.hostelmanagement.app.core.theme.StatusPaidText
import com.hostelmanagement.app.core.theme.StatusPendingBg
import com.hostelmanagement.app.core.theme.StatusPendingBorder
import com.hostelmanagement.app.core.theme.StatusPendingText

@Composable
fun StatusBadge(
    status: String,
    modifier: Modifier = Modifier,
    text: String? = null
) {
    val displayStatus = text ?: status
    val upper = displayStatus.uppercase()
    val matchKey = status.uppercase()

    val (bgColor, borderColor, textColor) = when {
        matchKey in listOf("PAID", "APPROVED", "ACTIVE") || upper in listOf("PAID", "APPROVED", "ACTIVE") -> Triple(StatusPaidBg, StatusPaidBorder, StatusPaidText)
        matchKey in listOf("PENDING", "OPEN") || upper in listOf("PENDING", "OPEN") -> Triple(StatusPendingBg, StatusPendingBorder, StatusPendingText)
        matchKey in listOf("UNPAID", "OVERDUE", "REJECTED", "FULL") || upper in listOf("UNPAID", "OVERDUE", "REJECTED", "FULL") -> Triple(StatusOverdueBg, StatusOverdueBorder, StatusOverdueText)
        matchKey.contains("FREE") || matchKey == "AVAILABLE" || upper.contains("FREE") || upper == "AVAILABLE" -> Triple(StatusPaidBg, StatusPaidBorder, StatusPaidText)
        else -> Triple(StatusNeutralBg, StatusNeutralBorder, StatusNeutralText)
    }

    Surface(
        color = bgColor,
        border = BorderStroke(1.dp, borderColor),
        shape = AppShapes.small,
        modifier = modifier.semantics {
            contentDescription = "Status: $upper"
        }
    ) {
        Text(
            text = upper,
            style = AppTypography.labelSmall,
            color = textColor,
            modifier = Modifier.padding(horizontal = AppDimens.Spacing8, vertical = AppDimens.Spacing4)
        )
    }
}
