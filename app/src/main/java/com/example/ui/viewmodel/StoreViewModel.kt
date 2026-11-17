package com.example.ui.viewmodel

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.data.model.CartItem
import com.example.data.model.CategoryCustomization
import com.example.data.model.Currency
import com.example.data.model.OrderSubmission
import com.example.data.model.Product
import com.example.data.model.ProductCategory
import com.example.data.model.WalletAccount
import com.example.data.remote.CloudStoreRepository
import com.example.data.repository.StoreRepository
import com.example.data.security.AdminSecurityManager
import com.google.firebase.firestore.FirebaseFirestoreException
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

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
    val offerSlotProducts: List<Product> = emptyList(),
    val selectedProductForDetail: Product? = null,
    val showLocationPicker: Boolean = false,
    val walletAccounts: List<WalletAccount> = emptyList(),
    val categoryCustomizations: Map<String, CategoryCustomization> = emptyMap(),
    val recentSubmittedOrder: OrderSubmission? = null,
    val showOrderSuccessDialog: Boolean = false
)

class StoreViewModel(
    val repository: StoreRepository = StoreRepository(),
    private val cloudRepository: CloudStoreRepository? = null
) : ViewModel() {

    private val _currentTab = MutableStateFlow(AppTab.HOME)
    private val _selectedCategory = MutableStateFlow<ProductCategory?>(null)
    private val _searchQuery = MutableStateFlow("")
    private val _selectedProductForDetail = MutableStateFlow<Product?>(null)
    private val _showLocationPicker = MutableStateFlow(false)
    private val _recentSubmittedOrder = MutableStateFlow<OrderSubmission?>(null)
    private val _showOrderSuccessDialog = MutableStateFlow(false)

    init {
        if (cloudRepository != null) {
            initializeCloudSync(cloudRepository)
        }
    }

    private fun initializeCloudSync(cloud: CloudStoreRepository) {
        // Real-time Products listener (synced from Web Admin)
        viewModelScope.launch {
            cloud.observeProducts()
                .catch { error ->
                    val fsErr = generateSequence(error) { it.cause }
                        .filterIsInstance<FirebaseFirestoreException>()
                        .firstOrNull()
                    if (fsErr == null && error is CancellationException) throw error
                    Log.w(TAG, "Error observing products", fsErr ?: error)
                }
                .collect { cloudProducts ->
                    repository.applyCloudProducts(cloudProducts)
                }
        }

        // Real-time Categories listener (synced from Web Admin)
        viewModelScope.launch {
            cloud.observeCategories()
                .catch { error ->
                    val fsErr = generateSequence(error) { it.cause }
                        .filterIsInstance<FirebaseFirestoreException>()
                        .firstOrNull()
                    if (fsErr == null && error is CancellationException) throw error
                    Log.w(TAG, "Error observing categories", fsErr ?: error)
                }
                .collect { cloudCategories ->
                    repository.applyCloudCategories(cloudCategories)
                }
        }

        // Real-time Wallets listener (synced from Web Admin)
        viewModelScope.launch {
            cloud.observeWallets()
                .catch { error ->
                    val fsErr = generateSequence(error) { it.cause }
                        .filterIsInstance<FirebaseFirestoreException>()
                        .firstOrNull()
                    if (fsErr == null && error is CancellationException) throw error
                    Log.w(TAG, "Error observing wallets", fsErr ?: error)
                }
                .collect { cloudWallets ->
                    repository.applyCloudWallets(cloudWallets)
                }
        }

        // Real-time Store Config (Special Offers synced from Web Admin) listener
        viewModelScope.launch {
            cloud.observeStoreConfig()
                .catch { error ->
                    val fsErr = generateSequence(error) { it.cause }
                        .filterIsInstance<FirebaseFirestoreException>()
                        .firstOrNull()
                    if (fsErr == null && error is CancellationException) throw error
                    Log.w(TAG, "Error observing store config", fsErr ?: error)
                }
                .collect { pair ->
                    if (pair != null) {
                        val (offerIds, _) = pair
                        repository.applyCloudOfferSlots(offerIds)
                    }
                }
        }
    }

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
            repository.walletAccounts,
            repository.categoryCustomizations
        ) { curr, wallets, cats ->
            Triple(curr, wallets, cats)
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
            _recentSubmittedOrder,
            _showOrderSuccessDialog
        ) { recentOrder, showOrderDialog ->
            recentOrder to showOrderDialog
        }
    ) { core, catalog, nav, orderState ->
        val (p, c, f, o, loc) = core
        val (curr, wallets, cats) = catalog
        val (tab, cat, query, detail, showLoc) = nav
        val (recentOrder, showOrderDialog) = orderState

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
            offerSlotProducts = slotProducts,
            selectedProductForDetail = detail,
            showLocationPicker = showLoc,
            walletAccounts = wallets,
            categoryCustomizations = cats,
            recentSubmittedOrder = recentOrder,
            showOrderSuccessDialog = showOrderDialog
        )
    }.stateIn(
        viewModelScope,
        SharingStarted.WhileSubscribed(STOP_TIMEOUT_MILLIS),
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

    fun submitOrder(
        customerName: String,
        customerPhone: String,
        recipientName: String,
        recipientPhone: String,
        deliveryAddress: String,
        googleMapsLocationUrl: String,
        deliveryNotes: String,
        paymentMethodName: String,
        walletDetails: String?,
        paymentTransactionNumber: String
    ): OrderSubmission? {
        val state = uiState.value
        if (state.cartItems.isEmpty()) return null

        val order = OrderSubmission(
            orderId = "JM-${(1000..9999).random()}",
            customerName = AdminSecurityManager.sanitizeText(customerName, 80),
            customerPhone = AdminSecurityManager.sanitizeText(customerPhone, 25),
            recipientName = AdminSecurityManager.sanitizeText(recipientName, 80),
            recipientPhone = AdminSecurityManager.sanitizeText(recipientPhone, 25),
            deliveryAddress = AdminSecurityManager.sanitizeText(deliveryAddress, 250),
            googleMapsLocationUrl = googleMapsLocationUrl.trim().take(300),
            deliveryNotes = AdminSecurityManager.sanitizeText(deliveryNotes, 250),
            paymentMethodName = paymentMethodName,
            walletDetails = walletDetails,
            paymentTransactionNumber = AdminSecurityManager.sanitizeText(paymentTransactionNumber, 80),
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

    private companion object {
        const val TAG = "StoreVM"
        const val STOP_TIMEOUT_MILLIS = 5000L
    }
}

private data class Tuple5<A, B, C, D, E>(val a: A, val b: B, val c: C, val d: D, val e: E)
