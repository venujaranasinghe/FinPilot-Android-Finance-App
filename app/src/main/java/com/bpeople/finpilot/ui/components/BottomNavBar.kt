package com.bpeople.finpilot.ui.components

import android.os.Build
import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.spring
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.CompareArrows
import androidx.compose.material.icons.rounded.EmojiEvents
import androidx.compose.material.icons.rounded.Home
import androidx.compose.material.icons.rounded.Person
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.BlurEffect
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.TileMode
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

private val OrangePrimary = Color(0xFFF97316)

enum class NavTab {
    HOME, TRANSACTIONS, GOALS, PROFILE,
    // Legacy values — treated as HOME/TRANSACTIONS by the bar renderer
    DASHBOARD, INCOME, EXPENSE,
}

private fun NavTab.resolved(): NavTab = when (this) {
    NavTab.DASHBOARD -> NavTab.HOME
    NavTab.INCOME, NavTab.EXPENSE -> NavTab.TRANSACTIONS
    else -> this
}

private data class NavItem(
    val tab: NavTab,
    val label: String,
    val icon: ImageVector,
)

private val navItems = listOf(
    NavItem(NavTab.HOME, "Home", Icons.Rounded.Home),
    NavItem(NavTab.TRANSACTIONS, "Transactions", Icons.Rounded.CompareArrows),
    NavItem(NavTab.GOALS, "Goals", Icons.Rounded.EmojiEvents),
    NavItem(NavTab.PROFILE, "Profile", Icons.Rounded.Person),
)

@Composable
fun FinPilotBottomNavBar(
    modifier: Modifier = Modifier,
    currentTab: NavTab,
    onNavigateToDashboard: () -> Unit = {},
    onNavigateToIncome: () -> Unit = {},
    onNavigateToExpense: () -> Unit = {},
    onNavigateToTransactions: () -> Unit = {},
    onNavigateToGoals: () -> Unit = {},
    onNavigateToProfile: () -> Unit = {},
) {
    val active = currentTab.resolved()

    val blurModifier = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
        Modifier.graphicsLayer {
            renderEffect = BlurEffect(20f, 20f, TileMode.Clamp)
        }
    } else {
        Modifier
    }

    Box(
        modifier = modifier
            .fillMaxWidth()
            .navigationBarsPadding()
            .padding(start = 16.dp, end = 16.dp, bottom = 16.dp),
    ) {
        // Blur layer (API 31+) — drawn behind the card
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            Box(
                modifier = Modifier
                    .matchParentSize()
                    .clip(RoundedCornerShape(28.dp))
                    .background(Color.White.copy(alpha = 0.15f))
                    .then(blurModifier),
            )
        }

        Card(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(28.dp),
            colors = CardDefaults.cardColors(
                containerColor = Color.White.copy(
                    alpha = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) 0.88f else 0.97f,
                ),
            ),
            elevation = CardDefaults.cardElevation(defaultElevation = 24.dp),
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 8.dp, vertical = 10.dp),
                horizontalArrangement = Arrangement.SpaceAround,
                verticalAlignment = Alignment.CenterVertically,
            ) {
                navItems.forEach { item ->
                    NavBarItem(
                        item = item,
                        isActive = item.tab == active,
                        onClick = {
                            when (item.tab) {
                                NavTab.HOME -> onNavigateToDashboard()
                                NavTab.TRANSACTIONS -> onNavigateToTransactions()
                                NavTab.GOALS -> onNavigateToGoals()
                                NavTab.PROFILE -> onNavigateToProfile()
                                else -> {}
                            }
                        },
                    )
                }
            }
        }
    }
}

@Composable
private fun NavBarItem(
    item: NavItem,
    isActive: Boolean,
    onClick: () -> Unit,
) {
    val iconTint by animateColorAsState(
        targetValue = if (isActive) OrangePrimary else Color(0xFF9CA3AF),
        animationSpec = spring(stiffness = Spring.StiffnessMediumLow),
        label = "nav_tint_${item.tab}",
    )
    val labelColor by animateColorAsState(
        targetValue = if (isActive) OrangePrimary else Color.Transparent,
        animationSpec = spring(stiffness = Spring.StiffnessMediumLow),
        label = "nav_label_${item.tab}",
    )
    val scale by animateFloatAsState(
        targetValue = if (isActive) 1.08f else 1f,
        animationSpec = spring(
            dampingRatio = Spring.DampingRatioMediumBouncy,
            stiffness = Spring.StiffnessMedium,
        ),
        label = "nav_scale_${item.tab}",
    )

    Column(
        modifier = Modifier
            .scale(scale)
            .clip(RoundedCornerShape(16.dp))
            .clickable(onClick = onClick)
            .padding(horizontal = 14.dp, vertical = 6.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(3.dp),
    ) {
        Icon(
            imageVector = item.icon,
            contentDescription = item.label,
            tint = iconTint,
            modifier = Modifier.size(24.dp),
        )

        // Label — only visible when active; invisible text keeps column height stable
        Text(
            text = item.label,
            fontSize = 10.sp,
            fontWeight = FontWeight.SemiBold,
            color = labelColor,
        )

        // Orange dot indicator
        Box(
            modifier = Modifier
                .size(4.dp)
                .clip(CircleShape)
                .background(if (isActive) OrangePrimary else Color.Transparent),
        )
    }
}
