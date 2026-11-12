package com.example

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.BackHandler
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.platform.LocalLayoutDirection
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.LayoutDirection
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.data.model.Currency
import com.example.ui.components.AdminLoginDialog
import com.example.ui.components.BottomNavBar
import com.example.ui.components.LocationPickerDialog
import com.example.ui.components.ProductDetailDialog
import com.example.ui.components.StoreHeader
import com.example.ui.screens.AdminScreen
import com.example.ui.screens.CartScreen
import com.example.ui.screens.CategoriesScreen
import com.example.ui.screens.HomeScreen
import com.example.ui.screens.OffersScreen
import com.example.ui.theme.Emerald100
import com.example.ui.theme.Emerald800
import com.example.ui.theme.Emerald900
import com.example.ui.theme.MyApplicationTheme
import com.example.ui.theme.Stone100
import com.example.ui.theme.Stone500
import com.example.ui.theme.Stone700
import com.example.ui.theme.Stone900
import com.example.ui.theme.White
import com.example.ui.viewmodel.AppTab
import com.example.ui.viewmodel.StoreViewModel

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            MyApplicationTheme {
                // Ensure natural RTL reading flow for Arabic e-commerce app
                CompositionLocalProvider(LocalLayoutDirection provides LayoutDirection.Rtl) {
                    AlJamalStoreApp()
                }
            }
        }
    }
}

@Composable
fun AlJamalStoreApp(
    viewModel: StoreViewModel = viewModel()
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()

    // Handle back button: return to Home tab if on another screen
    BackHandler(enabled = uiState.currentTab != AppTab.HOME) {
        viewModel.selectTab(AppTab.HOME)
    }

    val cartQuantities = uiState.cartItems.associate { it.product.id to it.quantity }

    Scaffold(
        modifier = Modifier.fillMaxSize(),
        topBar = {
            Column(modifier = Modifier.statusBarsPadding()) {
                StoreHeader(
                    deliveryLocation = uiState.deliveryLocation,
                    currency = uiState.currency,
                    searchQuery = uiState.searchQuery,
                    onSearchChange = { viewModel.updateSearchQuery(it) },
                    onLocationClick = { viewModel.openLocationPicker() },
                    onCurrencyClick = {
                        val nextCurrency = when (uiState.currency) {
                            Currency.YER -> Currency.SAR
                            Currency.SAR -> Currency.USD
                            Currency.USD -> Currency.YER
                        }
                        viewModel.setCurrency(nextCurrency)
                    }
                )
            }
        },
        bottomBar = {
            BottomNavBar(
                currentTab = uiState.currentTab,
                cartItemCount = uiState.cartTotalCount,
                onTabSelected = { viewModel.selectTab(it) }
            )
        }
    ) { innerPadding ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .background(Stone100)
        ) {
            when (uiState.currentTab) {
                AppTab.HOME -> {
                    HomeScreen(
                        products = uiState.filteredProducts,
                        slotProducts = uiState.offerSlotProducts,
                        cartMap = cartQuantities,
                        favorites = uiState.favorites,
                        selectedCategory = uiState.selectedCategory,
                        currency = uiState.currency,
                        storePhone = viewModel.repository.storePhone,
                        storeWhatsApp = viewModel.repository.storeWhatsApp,
                        categoryCustomizations = uiState.categoryCustomizations,
                        onCategorySelected = { viewModel.selectCategory(it) },
                        onProductClick = { viewModel.openProductDetail(it) },
                        onAddToCart = { viewModel.addToCart(it.id) },
                        onDecrement = { viewModel.decrementFromCart(it.id) },
                        onToggleFavorite = { viewModel.toggleFavorite(it.id) },
                        onLocationClick = { viewModel.openLocationPicker() },
                        onManageSlotsClick = {
                            if (uiState.isAdminUnlocked) {
                                viewModel.selectTab(AppTab.ACCOUNT)
                            } else {
                                viewModel.openAdminLogin()
                            }
                        }
                    )
                }

                AppTab.OFFERS -> {
                    OffersScreen(
                        products = uiState.allProducts,
                        cartMap = cartQuantities,
                        favorites = uiState.favorites,
                        currency = uiState.currency,
                        onProductClick = { viewModel.openProductDetail(it) },
                        onAddToCart = { viewModel.addToCart(it.id) },
                        onDecrement = { viewModel.decrementFromCart(it.id) },
                        onToggleFavorite = { viewModel.toggleFavorite(it.id) }
                    )
                }

                AppTab.CATEGORIES -> {
                    CategoriesScreen(
                        products = uiState.allProducts,
                        selectedCategory = uiState.selectedCategory,
                        cartMap = cartQuantities,
                        favorites = uiState.favorites,
                        currency = uiState.currency,
                        onCategorySelected = { viewModel.selectCategory(it) },
                        onProductClick = { viewModel.openProductDetail(it) },
                        onAddToCart = { viewModel.addToCart(it.id) },
                        onDecrement = { viewModel.decrementFromCart(it.id) },
                        onToggleFavorite = { viewModel.toggleFavorite(it.id) }
                    )
                }

                AppTab.CART -> {
                    CartScreen(
                        cartItems = uiState.cartItems,
                        subtotalYer = uiState.subtotalYer,
                        deliveryFeeYer = uiState.deliveryFeeYer,
                        totalYer = uiState.totalYer,
                        currency = uiState.currency,
                        defaultLocation = uiState.deliveryLocation,
                        storeWhatsApp = viewModel.repository.storeWhatsApp,
                        walletAccounts = uiState.walletAccounts,
                        onAddToCart = { viewModel.addToCart(it.id) },
                        onDecrement = { viewModel.decrementFromCart(it.id) },
                        onRemoveFromCart = { viewModel.removeFromCart(it.id) },
                        onClearCart = { viewModel.clearCart() },
                        onSubmitOrder = { name, phone, address, notes, payMethod, walletInfo ->
                            viewModel.submitOrder(name, phone, address, notes, payMethod, walletInfo)
                        }
                    )
                }

                AppTab.ACCOUNT -> {
                    AdminScreen(
                        products = uiState.allProducts,
                        favorites = uiState.favorites,
                        cartMap = cartQuantities,
                        offerSlotItemIds = uiState.offerSlotItemIds,
                        isAdminUnlocked = uiState.isAdminUnlocked,
                        currency = uiState.currency,
                        storePhone = viewModel.repository.storePhone,
                        storeWhatsApp = viewModel.repository.storeWhatsApp,
                        storeLocation = viewModel.repository.storeLocation,
                        adminPin = uiState.adminPin,
                        walletAccounts = uiState.walletAccounts,
                        categoryCustomizations = uiState.categoryCustomizations,
                        onUnlockAdminRequest = { viewModel.openAdminLogin() },
                        onLockAdmin = { viewModel.lockAdmin() },
                        onToggleOfferSlot = { viewModel.toggleOfferSlot(it) },
                        onUpdatePrice = { id, p, orig -> viewModel.updateProductPrice(id, p, orig) },
                        onUpdateProductImage = { id, imgUri -> viewModel.updateProductImage(id, imgUri) },
                        onDeleteProduct = { viewModel.deleteProduct(it) },
                        onAddNewProduct = { viewModel.addNewProduct(it) },
                        onUpdateCategory = { id, title, sub, img -> viewModel.updateCategory(id, title, sub, img) },
                        onAddWalletAccount = { viewModel.addWalletAccount(it) },
                        onUpdateWalletAccount = { viewModel.updateWalletAccount(it) },
                        onDeleteWalletAccount = { viewModel.deleteWalletAccount(it) },
                        onChangeAdminPin = { viewModel.changeAdminPin(it) },
                        onProductClick = { viewModel.openProductDetail(it) },
                        onAddToCart = { viewModel.addToCart(it.id) },
                        onDecrement = { viewModel.decrementFromCart(it.id) },
                        onToggleFavorite = { viewModel.toggleFavorite(it.id) }
                    )
                }
            }
        }
    }

    // Modal Dialogs
    // 1. Product Detail Sheet
    uiState.selectedProductForDetail?.let { product ->
        ProductDetailDialog(
            product = product,
            currency = uiState.currency,
            cartQuantity = cartQuantities[product.id] ?: 0,
            isFavorite = uiState.favorites.contains(product.id),
            onDismiss = { viewModel.closeProductDetail() },
            onAddToCart = { viewModel.addToCart(product.id) },
            onDecrement = { viewModel.decrementFromCart(product.id) },
            onToggleFavorite = { viewModel.toggleFavorite(product.id) }
        )
    }

    // 2. Delivery Location Picker
    if (uiState.showLocationPicker) {
        LocationPickerDialog(
            currentLocation = uiState.deliveryLocation,
            locations = viewModel.repository.sanaaLocations,
            onSelectLocation = { viewModel.setLocation(it) },
            onDismiss = { viewModel.closeLocationPicker() }
        )
    }

    // 3. Admin / Manager Login
    if (uiState.showAdminLogin) {
        AdminLoginDialog(
            expectedPin = uiState.adminPin,
            onSuccess = {
                viewModel.unlockAdmin()
                viewModel.selectTab(AppTab.ACCOUNT)
            },
            onDismiss = { viewModel.closeAdminLogin() }
        )
    }

    // 4. Order Success Dialog
    if (uiState.showOrderSuccessDialog) {
        Dialog(onDismissRequest = { viewModel.dismissOrderSuccessDialog() }) {
            Surface(
                modifier = Modifier
                    .fillMaxWidth(0.92f)
                    .clip(RoundedCornerShape(22.dp)),
                color = White,
                tonalElevation = 6.dp
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(20.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Box(
                        modifier = Modifier
                            .size(54.dp)
                            .clip(CircleShape)
                            .background(Emerald100),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = Icons.Default.CheckCircle,
                            contentDescription = null,
                            tint = Emerald800,
                            modifier = Modifier.size(32.dp)
                        )
                    }

                    Spacer(modifier = Modifier.height(12.dp))

                    Text(
                        text = "تم تجهيز وإرسال الطلب بنجاح!",
                        fontSize = 15.sp,
                        fontWeight = FontWeight.Black,
                        color = Emerald900,
                        textAlign = TextAlign.Center
                    )

                    Spacer(modifier = Modifier.height(6.dp))

                    uiState.recentSubmittedOrder?.let { order ->
                        Card(
                            modifier = Modifier.fillMaxWidth(),
                            shape = RoundedCornerShape(12.dp),
                            colors = CardDefaults.cardColors(containerColor = Stone100)
                        ) {
                            Column(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(10.dp),
                                verticalArrangement = Arrangement.spacedBy(4.dp)
                            ) {
                                Text("رقم الطلب: ${order.orderId}", fontSize = 11.sp, fontWeight = FontWeight.Bold, color = Stone900)
                                Text("الاسم: ${order.customerName}", fontSize = 11.sp, color = Stone700)
                                Text("العنوان: ${order.deliveryAddress}", fontSize = 11.sp, color = Stone700)
                                Text("المبلغ الإجمالي: ${order.totalYer.toInt()} ر.ي", fontSize = 11.sp, fontWeight = FontWeight.Black, color = Emerald800)
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(10.dp))

                    Text(
                        text = "تم فتح تطبيق واتساب لإرسال الفاتورة لخدمة العملاء بمتجر الجمل ومباشرة التوصيل.",
                        fontSize = 10.5.sp,
                        color = Stone500,
                        textAlign = TextAlign.Center
                    )

                    Spacer(modifier = Modifier.height(16.dp))

                    Button(
                        onClick = { viewModel.dismissOrderSuccessDialog() },
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(44.dp),
                        shape = RoundedCornerShape(12.dp),
                        colors = ButtonDefaults.buttonColors(containerColor = Emerald800)
                    ) {
                        Text("العودة للتسوق", fontSize = 12.sp, fontWeight = FontWeight.Bold, color = White)
                    }
                }
            }
        }
    }
}
