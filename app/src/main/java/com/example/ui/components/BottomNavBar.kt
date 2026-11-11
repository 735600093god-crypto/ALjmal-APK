package com.example.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.List
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.ShoppingCart
import androidx.compose.material.icons.filled.Star
import androidx.compose.material3.Icon
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.ui.theme.Amber500
import com.example.ui.theme.Emerald800
import com.example.ui.theme.Emerald900
import com.example.ui.theme.Stone400
import com.example.ui.theme.Stone950
import com.example.ui.theme.White
import com.example.ui.viewmodel.AppTab

@Composable
fun BottomNavBar(
    currentTab: AppTab,
    cartItemCount: Int,
    onTabSelected: (AppTab) -> Unit,
    modifier: Modifier = Modifier
) {
    Surface(
        modifier = modifier
            .fillMaxWidth()
            .shadow(16.dp),
        color = White,
        tonalElevation = 6.dp
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .navigationBarsPadding()
                .padding(vertical = 6.dp, horizontal = 4.dp),
            horizontalArrangement = Arrangement.SpaceAround,
            verticalAlignment = Alignment.CenterVertically
        ) {
            // 1. Home
            NavTabItem(
                tab = AppTab.HOME,
                isSelected = currentTab == AppTab.HOME,
                icon = Icons.Default.Home,
                onClick = { onTabSelected(AppTab.HOME) }
            )

            // 2. Offers
            NavTabItem(
                tab = AppTab.OFFERS,
                isSelected = currentTab == AppTab.OFFERS,
                icon = Icons.Default.Star,
                onClick = { onTabSelected(AppTab.OFFERS) }
            )

            // 3. Categories
            NavTabItem(
                tab = AppTab.CATEGORIES,
                isSelected = currentTab == AppTab.CATEGORIES,
                icon = Icons.Default.List,
                onClick = { onTabSelected(AppTab.CATEGORIES) }
            )

            // 4. Cart (with badge)
            NavTabItem(
                tab = AppTab.CART,
                isSelected = currentTab == AppTab.CART,
                icon = Icons.Default.ShoppingCart,
                badgeCount = cartItemCount,
                onClick = { onTabSelected(AppTab.CART) }
            )

            // 5. Account / Admin
            NavTabItem(
                tab = AppTab.ACCOUNT,
                isSelected = currentTab == AppTab.ACCOUNT,
                icon = Icons.Default.Person,
                onClick = { onTabSelected(AppTab.ACCOUNT) }
            )
        }
    }
}

@Composable
private fun NavTabItem(
    tab: AppTab,
    isSelected: Boolean,
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    badgeCount: Int = 0,
    onClick: () -> Unit
) {
    Box(
        modifier = Modifier
            .clip(RoundedCornerShape(12.dp))
            .clickable { onClick() }
            .padding(horizontal = 10.dp, vertical = 4.dp)
            .testTag("tab_${tab.name.lowercase()}"),
        contentAlignment = Alignment.Center
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            Box {
                Icon(
                    imageVector = icon,
                    contentDescription = tab.title,
                    tint = if (isSelected) Emerald800 else Stone400,
                    modifier = Modifier.size(22.dp)
                )

                if (badgeCount > 0) {
                    Box(
                        modifier = Modifier
                            .align(Alignment.TopEnd)
                            .padding(start = 14.dp, bottom = 10.dp)
                            .size(16.dp)
                            .clip(CircleShape)
                            .background(Amber500),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = if (badgeCount > 99) "99+" else badgeCount.toString(),
                            color = Stone950,
                            fontSize = 8.5.sp,
                            fontWeight = FontWeight.Black
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(2.dp))

            Text(
                text = tab.title,
                fontSize = 10.sp,
                fontWeight = if (isSelected) FontWeight.Black else FontWeight.Medium,
                color = if (isSelected) Emerald900 else Stone400
            )

            if (isSelected) {
                Spacer(modifier = Modifier.height(2.dp))
                Box(
                    modifier = Modifier
                        .size(5.dp)
                        .clip(CircleShape)
                        .background(Emerald800)
                )
            } else {
                Spacer(modifier = Modifier.height(7.dp))
            }
        }
    }
}
