package com.example.ui.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.data.model.CartItem
import com.example.data.model.Currency
import com.example.data.model.OrderSubmission
import com.example.data.model.PaymentMethod
import com.example.data.model.Product
import com.example.data.model.ProductCategory
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
        repository.products,
        repository.cart,
        repository.favorites,
        repository.offerSlotProductIds,
        repository.deliveryLocation,
        repository.currency,
        _currentTab,
        _selectedCategory,
        _searchQuery,
        _selectedProductForDetail,
        _showLocationPicker,
        _showAdminLogin,
        _isAdminUnlocked,
        _recentSubmittedOrder,
        _showOrderSuccessDialog
    ) { params ->
        @Suppress("UNCHECKED_CAST")
        val products = params[0] as List<Product>
        @Suppress("UNCHECKED_CAST")
        val cartMap = params[1] as Map<String, Int>
        @Suppress("UNCHECKED_CAST")
        val favs = params[2] as Set<String>
        @Suppress("UNCHECKED_CAST")
        val offerSlotIds = params[3] as List<String>
        val loc = params[4] as String
        val curr = params[5] as Currency
        val tab = params[6] as AppTab
        val cat = params[7] as ProductCategory?
        val query = params[8] as String
        val detailProduct = params[9] as Product?
        val showLoc = params[10] as Boolean
        val showAdm = params[11] as Boolean
        val adminUnlocked = params[12] as Boolean
        val recentOrder = params[13] as OrderSubmission?
        val showOrderDialog = params[14] as Boolean

        val filtered = products.filter { p ->
            val matchCat = (cat == null || p.category == cat)
            val matchQuery = query.isBlank() ||
                    p.name.contains(query, ignoreCase = true) ||
                    p.origin.contains(query, ignoreCase = true) ||
                    p.category.title.contains(query, ignoreCase = true)
            matchCat && matchQuery
        }

        val cartList = cartMap.mapNotNull { (id, qty) ->
            val product = products.find { it.id == id }
            if (product != null && qty > 0) CartItem(product, qty) else null
        }

        val totalCount = cartList.sumOf { it.quantity }
        val subtotal = cartList.sumOf { it.totalPriceYer }
        val deliveryFee = if (subtotal > 0) 5.0 else 0.0
        val total = subtotal + deliveryFee

        val slotProducts = offerSlotIds.mapNotNull { id -> products.find { it.id == id } }

        StoreUiState(
            currentTab = tab,
            selectedCategory = cat,
            searchQuery = query,
            deliveryLocation = loc,
            currency = curr,
            allProducts = products,
            filteredProducts = filtered,
            cartItems = cartList,
            cartTotalCount = totalCount,
            subtotalYer = subtotal,
            deliveryFeeYer = deliveryFee,
            totalYer = total,
            favorites = favs,
            offerSlotItemIds = offerSlotIds,
            offerSlotProducts = slotProducts,
            selectedProductForDetail = detailProduct,
            showLocationPicker = showLoc,
            showAdminLogin = showAdm,
            isAdminUnlocked = adminUnlocked,
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

    fun toggleOfferSlot(productId: String) {
        repository.toggleOfferSlot(productId)
    }

    fun updateProductPrice(productId: String, newPrice: Double, newOriginalPrice: Double?) {
        repository.updateProductPrice(productId, newPrice, newOriginalPrice)
    }

    fun submitOrder(
        customerName: String,
        customerPhone: String,
        deliveryAddress: String,
        deliveryNotes: String,
        paymentMethod: PaymentMethod
    ): OrderSubmission? {
        val state = uiState.value
        if (state.cartItems.isEmpty()) return null

        val order = OrderSubmission(
            orderId = "JM-${(1000..9999).random()}",
            customerName = customerName,
            customerPhone = customerPhone,
            deliveryAddress = deliveryAddress,
            deliveryNotes = deliveryNotes,
            paymentMethod = paymentMethod,
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
