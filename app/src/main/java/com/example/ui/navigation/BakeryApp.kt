package com.example.ui.navigation

import android.widget.Toast
import androidx.activity.compose.BackHandler
import androidx.compose.animation.Crossfade
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Assessment
import androidx.compose.material.icons.filled.DeleteSweep
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.LocalShipping
import androidx.compose.material.icons.filled.PointOfSale
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.NavigationBarItemDefaults
import androidx.compose.material3.NavigationRail
import androidx.compose.material3.NavigationRailItem
import androidx.compose.material3.NavigationRailItemDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalLayoutDirection
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.LayoutDirection
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.ui.screens.arrivals.ArrivalsScreen
import com.example.ui.screens.dashboard.DashboardScreen
import com.example.ui.screens.expenses.ExpensesScreen
import com.example.ui.screens.pin.PinLockScreen
import com.example.ui.screens.products.ProductsScreen
import com.example.ui.screens.reports.ReportsScreen
import com.example.ui.screens.sales.SalesScreen
import com.example.ui.screens.settings.SettingsScreen
import com.example.ui.screens.waste.WasteReconciliationScreen
import com.example.ui.theme.KhoubzaForestGreen
import com.example.ui.theme.KhoubzaWheatGold
import com.example.ui.viewmodels.BakeryViewModel
import com.example.ui.viewmodels.UiMessage

enum class Screen(val title: String, val icon: ImageVector, val tag: String) {
    DASHBOARD("الرئيسية", Icons.Default.Home, "tab_dashboard"),
    ARRIVALS("السلعة الواصلة", Icons.Default.LocalShipping, "tab_arrivals"),
    SALES("المبيعات", Icons.Default.PointOfSale, "tab_sales"),
    WASTE("البقايا", Icons.Default.DeleteSweep, "tab_waste"),
    REPORTS("التقارير", Icons.Default.Assessment, "tab_reports"),
    SETTINGS("الإعدادات", Icons.Default.Settings, "tab_settings"),
    EXPENSES("المصاريف", Icons.Default.Home, "sub_expenses"),
    PRODUCTS("كتالوج المنتجات", Icons.Default.Home, "sub_products")
}

@Composable
fun BakeryApp(viewModel: BakeryViewModel) {
    val context = LocalContext.current
    val isUnlocked by viewModel.isUnlocked.collectAsState()
    val isPinEnabled by viewModel.isPinProtectionEnabled.collectAsState()

    var currentScreen by remember { mutableStateOf(Screen.DASHBOARD) }
    var screenStack by remember { mutableStateOf(listOf(Screen.DASHBOARD)) }

    val snackbarHostState = remember { SnackbarHostState() }

    fun navigateTo(screen: Screen) {
        if (currentScreen != screen) {
            screenStack = screenStack + screen
            currentScreen = screen
        }
    }

    fun navigateBack() {
        if (screenStack.size > 1) {
            val newStack = screenStack.dropLast(1)
            screenStack = newStack
            currentScreen = newStack.last()
        }
    }

    BackHandler(enabled = screenStack.size > 1) {
        navigateBack()
    }

    // Observe UI feedback messages
    LaunchedEffect(Unit) {
        viewModel.uiMessages.collect { msg ->
            when (msg) {
                is UiMessage.Success -> {
                    Toast.makeText(context, msg.message, Toast.LENGTH_SHORT).show()
                }
                is UiMessage.Error -> {
                    Toast.makeText(context, msg.message, Toast.LENGTH_LONG).show()
                }
            }
        }
    }

    // Force native Arabic RTL across the entire application
    CompositionLocalProvider(LocalLayoutDirection provides LayoutDirection.Rtl) {
        if (isPinEnabled && !isUnlocked) {
            PinLockScreen(viewModel = viewModel)
        } else {
            BoxWithConstraints(modifier = Modifier.fillMaxSize()) {
                val isWideScreen = maxWidth >= 600.dp
                val primaryTabs = listOf(
                    Screen.DASHBOARD,
                    Screen.ARRIVALS,
                    Screen.SALES,
                    Screen.WASTE,
                    Screen.REPORTS,
                    Screen.SETTINGS
                )

                if (isWideScreen) {
                    // Tablet Layout with Navigation Rail
                    Row(modifier = Modifier.fillMaxSize()) {
                        NavigationRail(
                            containerColor = MaterialTheme.colorScheme.surface,
                            contentColor = KhoubzaForestGreen,
                            modifier = Modifier.fillMaxHeight()
                        ) {
                            primaryTabs.forEach { screen ->
                                val selected = currentScreen == screen
                                NavigationRailItem(
                                    selected = selected,
                                    onClick = { navigateTo(screen) },
                                    icon = { Icon(screen.icon, contentDescription = screen.title) },
                                    label = { Text(screen.title, fontSize = 11.sp) },
                                    colors = NavigationRailItemDefaults.colors(
                                        selectedIconColor = Color.White,
                                        selectedTextColor = KhoubzaForestGreen,
                                        indicatorColor = KhoubzaForestGreen
                                    ),
                                    modifier = Modifier.testTag(screen.tag)
                                )
                            }
                        }

                        Box(modifier = Modifier.weight(1f)) {
                            ScreenContent(
                                currentScreen = currentScreen,
                                viewModel = viewModel,
                                onNavigateTo = { navigateTo(it) }
                            )
                        }
                    }
                } else {
                    // Mobile Layout with Bottom Navigation Bar
                    Scaffold(
                        snackbarHost = { SnackbarHost(snackbarHostState) },
                        bottomBar = {
                            NavigationBar(
                                containerColor = MaterialTheme.colorScheme.surface,
                                tonalElevation = 8.dp
                            ) {
                                primaryTabs.forEach { screen ->
                                    val selected = currentScreen == screen
                                    NavigationBarItem(
                                        selected = selected,
                                        onClick = { navigateTo(screen) },
                                        icon = {
                                            Icon(
                                                imageVector = screen.icon,
                                                contentDescription = screen.title
                                            )
                                        },
                                        label = {
                                            Text(
                                                text = screen.title,
                                                fontSize = 10.sp,
                                                fontWeight = if (selected) FontWeight.Bold else FontWeight.Normal
                                            )
                                        },
                                        colors = NavigationBarItemDefaults.colors(
                                            selectedIconColor = Color.White,
                                            selectedTextColor = KhoubzaForestGreen,
                                            indicatorColor = KhoubzaForestGreen,
                                            unselectedIconColor = MaterialTheme.colorScheme.onSurfaceVariant,
                                            unselectedTextColor = MaterialTheme.colorScheme.onSurfaceVariant
                                        ),
                                        modifier = Modifier.testTag(screen.tag)
                                    )
                                }
                            }
                        }
                    ) { paddingValues ->
                        Box(modifier = Modifier.padding(paddingValues)) {
                            ScreenContent(
                                currentScreen = currentScreen,
                                viewModel = viewModel,
                                onNavigateTo = { navigateTo(it) }
                            )
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun ScreenContent(
    currentScreen: Screen,
    viewModel: BakeryViewModel,
    onNavigateTo: (Screen) -> Unit
) {
    Crossfade(targetState = currentScreen, label = "ScreenTransition") { screen ->
        when (screen) {
            Screen.DASHBOARD -> DashboardScreen(
                viewModel = viewModel,
                onNavigateToArrivals = { onNavigateTo(Screen.ARRIVALS) },
                onNavigateToSales = { onNavigateTo(Screen.SALES) },
                onNavigateToWaste = { onNavigateTo(Screen.WASTE) },
                onNavigateToExpenses = { onNavigateTo(Screen.EXPENSES) }
            )
            Screen.ARRIVALS -> ArrivalsScreen(
                viewModel = viewModel
            )
            Screen.SALES -> SalesScreen(
                viewModel = viewModel,
                onNavigateToArrivals = { onNavigateTo(Screen.ARRIVALS) }
            )
            Screen.WASTE -> WasteReconciliationScreen(
                viewModel = viewModel
            )
            Screen.REPORTS -> ReportsScreen(
                viewModel = viewModel
            )
            Screen.SETTINGS -> SettingsScreen(
                viewModel = viewModel,
                onNavigateToProducts = { onNavigateTo(Screen.PRODUCTS) }
            )
            Screen.EXPENSES -> ExpensesScreen(
                viewModel = viewModel
            )
            Screen.PRODUCTS -> ProductsScreen(
                viewModel = viewModel
            )
        }
    }
}
