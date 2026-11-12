package com.example.ui.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.data.model.CartItem
import com.example.data.model.CategoryCustomization
import com.example.data.model.Currency
import com.example.data.model.OrderSubmission
import com.example.data.model.Product
import com.example.data.model.ProductCategory
import com.example.data.model.WalletAccount
import com.example.data.repository.StoreRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn

enum class AppTab(val title: String) {
    HOME("الرئيسية"),
    OFFERS("العروض"),
    CATEGORIES("التصنيفات"),
    CART("السلة"),
    ACCOUNT("حسابي")
}

data class StoreUiState(
    val currentTab: AppTab = AppTab.HOME,
    val selectedCategory: ProductCategory? = null,
    val searchQuery: String = "",
    val deliveryLocation: String = "حدة - داخل التوفير هايبر",
    val currency: Currency = Currency.YER,
    val allProducts: List<Product> = emptyList(),
    val filteredProducts: List<Product> = emptyList(),
    val cartItems: List<CartItem> = emptyList(),
    val cartTotalCount: Int = 0,
    val subtotalYer: Double = 0.0,
    val deliveryFeeYer: Double = 5.0,
    val totalYer: Double = 5.0,
    val favorites: Set<String> = emptySet(),
    val offerSlotItemIds: List<String> = emptyList(),
    val offerSlotProducts: List<Product> = emptyList(),
    val selectedProductForDetail: Product? = null,
    val showLocationPicker: Boolean = false,
    val showAdminLogin: Boolean = false,
    val isAdminUnlocked: Boolean = false,
    val adminPin: String = "1234",
    val walletAccounts: List<WalletAccount> = emptyList(),
    val categoryCustomizations: Map<String, CategoryCustomization> = emptyMap(),
    val recentSubmittedOrder: OrderSubmission? = null,
    val showOrderSuccessDialog: Boolean = false
)

class StoreViewModel(
    val repository: StoreRepository = StoreRepository()
) : ViewModel() {

    private val _currentTab = MutableStateFlow(AppTab.HOME)
    private val _selectedCategory = MutableStateFlow<ProductCategory?>(null)
    private val _searchQuery = MutableStateFlow("")
    private val _selectedProductForDetail = MutableStateFlow<Product?>(null)
    private val _showLocationPicker = MutableStateFlow(false)
    private val _showAdminLogin = MutableStateFlow(false)
    private val _isAdminUnlocked = MutableStateFlow(false)
    private val _recentSubmittedOrder = MutableStateFlow<OrderSubmission?>(null)
    private val _showOrderSuccessDialog = MutableStateFlow(false)

    val uiState: StateFlow<StoreUiState> = combine(
        combine(
            repository.products,
            repository.cart,
            repository.favorites,
            repository.offerSlotProductIds,
            repository.deliveryLocation
        ) { p, c, f, o, loc ->
            Tuple5(p, c, f, o, loc)
        },
        combine(
            repository.currency,
            repository.adminPin,
            repository.walletAccounts,
            repository.categoryCustomizations
        ) { curr, pin, wallets, cats ->
            Tuple4(curr, pin, wallets, cats)
        },
        combine(
            _currentTab,
            _selectedCategory,
            _searchQuery,
            _selectedProductForDetail,
            _showLocationPicker
        ) { tab, cat, query, detail, showLoc ->
            Tuple5(tab, cat, query, detail, showLoc)
        },
        combine(
            _showAdminLogin,
            _isAdminUnlocked,
            _recentSubmittedOrder,
            _showOrderSuccessDialog
        ) { showAdm, adminUnlocked, recentOrder, showOrderDialog ->
            Tuple4(showAdm, adminUnlocked, recentOrder, showOrderDialog)
        }
    ) { (p, c, f, o, loc), (curr, pin, wallets, cats), (tab, cat, query, detail, showLoc), (showAdm, adminUnlocked, recentOrder, showOrderDialog) ->
        val filtered = p.filter { prod ->
            val matchCat = (cat == null || prod.category == cat)
            val matchQuery = query.isBlank() ||
                    prod.name.contains(query, ignoreCase = true) ||
                    prod.origin.contains(query, ignoreCase = true) ||
                    prod.category.title.contains(query, ignoreCase = true)
            matchCat && matchQuery
        }

        val cartList = c.mapNotNull { (id, qty) ->
            val product = p.find { it.id == id }
            if (product != null && qty > 0) CartItem(product, qty) else null
        }

        val totalCount = cartList.sumOf { it.quantity }
        val subtotal = cartList.sumOf { it.totalPriceYer }
        val deliveryFee = if (subtotal > 0) 5.0 else 0.0
        val total = subtotal + deliveryFee

        val slotProducts = o.mapNotNull { id -> p.find { it.id == id } }

        StoreUiState(
            currentTab = tab,
            selectedCategory = cat,
            searchQuery = query,
            deliveryLocation = loc,
            currency = curr,
            allProducts = p,
            filteredProducts = filtered,
            cartItems = cartList,
            cartTotalCount = totalCount,
            subtotalYer = subtotal,
            deliveryFeeYer = deliveryFee,
            totalYer = total,
            favorites = f,
            offerSlotItemIds = o,
            offerSlotProducts = slotProducts,
            selectedProductForDetail = detail,
            showLocationPicker = showLoc,
            showAdminLogin = showAdm,
            isAdminUnlocked = adminUnlocked,
            adminPin = pin,
            walletAccounts = wallets,
            categoryCustomizations = cats,
            recentSubmittedOrder = recentOrder,
            showOrderSuccessDialog = showOrderDialog
        )
    }.stateIn(
        viewModelScope,
        SharingStarted.WhileSubscribed(5000),
        StoreUiState()
    )

    fun selectTab(tab: AppTab) {
        _currentTab.value = tab
    }

    fun selectCategory(category: ProductCategory?) {
        _selectedCategory.value = category
    }

    fun updateSearchQuery(query: String) {
        _searchQuery.value = query
    }

    fun toggleFavorite(productId: String) {
        repository.toggleFavorite(productId)
    }

    fun addToCart(productId: String) {
        repository.addToCart(productId)
    }

    fun decrementFromCart(productId: String) {
        repository.decrementFromCart(productId)
    }

    fun removeFromCart(productId: String) {
        repository.removeFromCart(productId)
    }

    fun clearCart() {
        repository.clearCart()
    }

    fun setCurrency(currency: Currency) {
        repository.setCurrency(currency)
    }

    fun setLocation(location: String) {
        repository.setDeliveryLocation(location)
        _showLocationPicker.value = false
    }

    fun openLocationPicker() {
        _showLocationPicker.value = true
    }

    fun closeLocationPicker() {
        _showLocationPicker.value = false
    }

    fun openProductDetail(product: Product) {
        _selectedProductForDetail.value = product
    }

    fun closeProductDetail() {
        _selectedProductForDetail.value = null
    }

    fun openAdminLogin() {
        _showAdminLogin.value = true
    }

    fun closeAdminLogin() {
        _showAdminLogin.value = false
    }

    fun unlockAdmin() {
        _isAdminUnlocked.value = true
        _showAdminLogin.value = false
    }

    fun lockAdmin() {
        _isAdminUnlocked.value = false
    }

    fun changeAdminPin(newPin: String) {
        repository.setAdminPin(newPin)
    }

    fun toggleOfferSlot(productId: String) {
        repository.toggleOfferSlot(productId)
    }

    fun updateProductPrice(productId: String, newPrice: Double, newOriginalPrice: Double?) {
        repository.updateProductPrice(productId, newPrice, newOriginalPrice)
    }

    fun updateProductImage(productId: String, newImageUrl: String) {
        repository.updateProductImage(productId, newImageUrl)
    }

    fun updateProduct(product: Product) {
        repository.updateProduct(product)
    }

    fun deleteProduct(productId: String) {
        repository.deleteProduct(productId)
    }

    fun addNewProduct(product: Product) {
        repository.addNewProduct(product)
    }

    fun updateCategory(categoryId: String, title: String, subtitle: String, imageUrl: String?) {
        repository.updateCategory(categoryId, title, subtitle, imageUrl)
    }

    fun addWalletAccount(account: WalletAccount) {
        repository.addWalletAccount(account)
    }

    fun updateWalletAccount(account: WalletAccount) {
        repository.updateWalletAccount(account)
    }

    fun deleteWalletAccount(id: String) {
        repository.deleteWalletAccount(id)
    }

    fun submitOrder(
        customerName: String,
        customerPhone: String,
        deliveryAddress: String,
        deliveryNotes: String,
        paymentMethodName: String,
        walletDetails: String?
    ): OrderSubmission? {
        val state = uiState.value
        if (state.cartItems.isEmpty()) return null

        val order = OrderSubmission(
            orderId = "JM-${(1000..9999).random()}",
            customerName = customerName,
            customerPhone = customerPhone,
            deliveryAddress = deliveryAddress,
            deliveryNotes = deliveryNotes,
            paymentMethodName = paymentMethodName,
            walletDetails = walletDetails,
            items = state.cartItems,
            subtotalYer = state.subtotalYer,
            deliveryFeeYer = state.deliveryFeeYer,
            totalYer = state.totalYer
        )

        _recentSubmittedOrder.value = order
        _showOrderSuccessDialog.value = true
        repository.clearCart()
        return order
    }

    fun dismissOrderSuccessDialog() {
        _showOrderSuccessDialog.value = false
        _currentTab.value = AppTab.HOME
    }
}

// Data helper classes for multi-flow combine
private data class Tuple4<A, B, C, D>(val a: A, val b: B, val c: C, val d: D)
private data class Tuple5<A, B, C, D, E>(val a: A, val b: B, val c: C, val d: D, val e: E)
