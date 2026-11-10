package com.hostelmanagement.app.ui.components

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.NavigationBarItemDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import com.hostelmanagement.app.core.theme.AppDimens
import com.hostelmanagement.app.core.theme.AppTypography
import com.hostelmanagement.app.core.theme.PureWhite
import com.hostelmanagement.app.core.theme.Slate100
import com.hostelmanagement.app.core.theme.Slate200
import com.hostelmanagement.app.core.theme.Slate400
import com.hostelmanagement.app.core.theme.Slate900

data class BottomNavItem(
    val route: String,
    val title: String,
    val icon: ImageVector
)

@Composable
fun AppBottomBar(
    items: List<BottomNavItem>,
    currentRoute: String?,
    onItemClick: (String) -> Unit,
    modifier: Modifier = Modifier
) {
    Column(modifier = modifier.fillMaxWidth()) {
        HorizontalDivider(thickness = AppDimens.HairlineBorder, color = Slate200)
        NavigationBar(
            containerColor = PureWhite,
            tonalElevation = AppDimens.Spacing2 * 0
        ) {
            items.forEach { item ->
                val selected = currentRoute == item.route
                NavigationBarItem(
                    selected = selected,
                    onClick = { onItemClick(item.route) },
                    icon = {
                        Icon(
                            imageVector = item.icon,
                            contentDescription = item.title
                        )
                    },
                    label = {
                        Text(
                            text = item.title,
                            style = AppTypography.labelSmall
                        )
                    },
                    colors = NavigationBarItemDefaults.colors(
                        selectedIconColor = Slate900,
                        selectedTextColor = Slate900,
                        unselectedIconColor = Slate400,
                        unselectedTextColor = Slate400,
                        indicatorColor = Slate100
                    )
                )
            }
        }
    }
}
