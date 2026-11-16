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
import com.example.data.remote.CloudSyncMetadata
import com.example.data.repository.StoreRepository
import com.example.data.security.AdminSecurityManager
import com.example.data.security.PinVerificationResult
import com.example.data.security.SecurityFirewallState
import com.google.firebase.auth.FirebaseUser
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

sealed interface CloudSyncStatus {
    data object Connected : CloudSyncStatus
    data object Syncing : CloudSyncStatus
    data class SyncedSuccess(val version: Long, val message: String) : CloudSyncStatus
    data class SyncError(val message: String) : CloudSyncStatus
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
    val walletAccounts: List<WalletAccount> = emptyList(),
    val categoryCustomizations: Map<String, CategoryCustomization> = emptyMap(),
    val recentSubmittedOrder: OrderSubmission? = null,
    val showOrderSuccessDialog: Boolean = false,
    // Cloud Sync & Security Firewall State
    val signedInUserEmail: String = "",
    val signedInUserName: String = "",
    val isCloudAdminAuthorized: Boolean = false,
    val hasUnsavedAdminChanges: Boolean = false,
    val pendingAdminChanges: List<String> = emptyList(),
    val cloudSyncStatus: CloudSyncStatus = CloudSyncStatus.Connected,
    val cloudSyncMetadata: CloudSyncMetadata = CloudSyncMetadata(),
    val firewallState: SecurityFirewallState = SecurityFirewallState()
)

class StoreViewModel(
    val repository: StoreRepository = StoreRepository(),
    private val cloudRepository: CloudStoreRepository? = null,
    private val securityManager: AdminSecurityManager? = null,
    private val currentUser: FirebaseUser? = null
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
    private val _isCloudAdminAuthorized = MutableStateFlow(false)
    private val _cloudSyncStatus = MutableStateFlow<CloudSyncStatus>(CloudSyncStatus.Connected)
    private val _cloudSyncMetadata = MutableStateFlow(CloudSyncMetadata())
    private val _fallbackFirewallState = MutableStateFlow(SecurityFirewallState())

    private var pendingPinSaltAndHash: Pair<String, String>? = null

    init {
        if (cloudRepository != null && currentUser != null) {
            initializeCloudSync(cloudRepository, currentUser)
        }
    }

    private fun initializeCloudSync(cloud: CloudStoreRepository, user: FirebaseUser) {
        viewModelScope.launch {
            cloud.ensureUserProfile(user)
            val isAdmin = cloud.checkIsCloudAdmin(user)
            _isCloudAdminAuthorized.value = isAdmin
            if (isAdmin) {
                cloud.observeAdminSecurityPinConfig()
                    .catch { error ->
                        val fsErr = generateSequence(error) { it.cause }
                            .filterIsInstance<FirebaseFirestoreException>()
                            .firstOrNull()
                        if (fsErr == null && error is CancellationException) throw error
                        Log.w(TAG, "Error observing admin PIN config", fsErr ?: error)
                    }
                    .collect { pinDoc ->
                        if (pinDoc != null && pinDoc.pinSalt.isNotBlank() && pinDoc.pinHash.isNotBlank()) {
                            securityManager?.applySyncedPinVerifier(pinDoc.pinSalt, pinDoc.pinHash)
                        }
                    }
            }
        }

        // Real-time Products listener
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

        // Real-time Categories listener
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

        // Real-time Wallets listener
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

        // Real-time Store Config (Special Offers & Sync Metadata) listener
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
                        val (offerIds, meta) = pair
                        repository.applyCloudOfferSlots(offerIds)
                        _cloudSyncMetadata.value = meta
                    }
                }
        }
    }

    private val firewallFlow: StateFlow<SecurityFirewallState> =
        securityManager?.firewallState ?: _fallbackFirewallState

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
            repository.categoryCustomizations,
            repository.hasUnsavedChanges,
            repository.pendingChangeSummaries
        ) { curr, wallets, cats, dirty, summaries ->
            Tuple5(curr, wallets, cats, dirty, summaries)
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
        },
        combine(
            _isCloudAdminAuthorized,
            _cloudSyncStatus,
            _cloudSyncMetadata,
            firewallFlow
        ) { isCloudAdm, syncStatus, syncMeta, fwState ->
            Tuple4(isCloudAdm, syncStatus, syncMeta, fwState)
        }
    ) { core, catalog, nav, modals, cloudAndSecurity ->
        val (p, c, f, o, loc) = core
        val (curr, wallets, cats, dirty, summaries) = catalog
        val (tab, cat, query, detail, showLoc) = nav
        val (showAdm, adminUnlocked, recentOrder, showOrderDialog) = modals
        val (isCloudAdm, syncStatus, syncMeta, fwState) = cloudAndSecurity

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
            walletAccounts = wallets,
            categoryCustomizations = cats,
            recentSubmittedOrder = recentOrder,
            showOrderSuccessDialog = showOrderDialog,
            signedInUserEmail = currentUser?.email ?: "",
            signedInUserName = currentUser?.displayName ?: "",
            isCloudAdminAuthorized = isCloudAdm,
            hasUnsavedAdminChanges = dirty,
            pendingAdminChanges = summaries,
            cloudSyncStatus = syncStatus,
            cloudSyncMetadata = syncMeta,
            firewallState = fwState
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

    fun openAdminLogin() {
        securityManager?.refreshLockoutState()
        _showAdminLogin.value = true
    }

    fun closeAdminLogin() {
        _showAdminLogin.value = false
    }

    fun refreshFirewallState() {
        securityManager?.refreshLockoutState()
    }

    fun verifyAdminPassword(candidate: String): PinVerificationResult {
        val manager = securityManager ?: return PinVerificationResult.Invalid(1, 2)
        val result = manager.verifyAdminPassword(candidate)
        viewModelScope.launch {
            when (result) {
                is PinVerificationResult.Success ->
                    cloudRepository?.logSecurityEvent("ADMIN_UNLOCK", "دخول مصرح به للوحة تحكم المدير")
                is PinVerificationResult.Invalid ->
                    cloudRepository?.logSecurityEvent("FAILED_PIN", "محاولة كلمة سر خاطئة (${result.failedAttempts})")
                is PinVerificationResult.LockedOut ->
                    cloudRepository?.logSecurityEvent("FIREWALL_LOCKOUT", "تم حظر الدخول مؤقتاً لمدة ${result.remainingSeconds} ثانية")
            }
        }
        return result
    }

    fun unlockAdmin() {
        _isAdminUnlocked.value = true
        _showAdminLogin.value = false
    }

    fun lockAdmin() {
        _isAdminUnlocked.value = false
    }

    fun changeAdminPassword(currentPassword: String, newPassword: String): Result<Unit> {
        val manager = securityManager ?: return Result.failure(IllegalStateException("مدير الحماية غير متاح"))
        val res = manager.changeAdminPassword(currentPassword, newPassword)
        res.onSuccess { saltAndHash ->
            pendingPinSaltAndHash = saltAndHash
            viewModelScope.launch {
                cloudRepository?.logSecurityEvent("PIN_CHANGED", "تم تغيير كلمة سر المدير وتحديث التشفير")
            }
        }
        return res.map { }
    }

    fun toggleOfferSlot(productId: String) {
        repository.toggleOfferSlot(productId)
    }

    fun updateProductPrice(productId: String, newPrice: Double, newOriginalPrice: Double?) {
        if (newPrice <= 0.0 || newPrice.isNaN() || newPrice.isInfinite()) return
        repository.updateProductPrice(productId, newPrice, newOriginalPrice)
    }

    fun updateProductImage(productId: String, newImageUrl: String) {
        if (!AdminSecurityManager.isSafeImageUrl(newImageUrl)) return
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
        val safeTitle = AdminSecurityManager.sanitizeText(title, 80)
        val safeSub = AdminSecurityManager.sanitizeText(subtitle, 160)
        val safeImg = imageUrl?.takeIf { AdminSecurityManager.isSafeImageUrl(it) }
        repository.updateCategory(categoryId, safeTitle, safeSub, safeImg)
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

    fun discardPendingAdminChanges() {
        repository.discardPendingChanges()
        _cloudSyncStatus.value = CloudSyncStatus.Connected
    }

    fun saveAndSyncWorkToCloud() {
        val cloud = cloudRepository
        if (cloud == null || currentUser == null) {
            _cloudSyncStatus.value = CloudSyncStatus.SyncError(
                "يرجى تسجيل الدخول بحساب Google أولاً لتفعيل المزامنة السحابية."
            )
            return
        }

        viewModelScope.launch {
            _cloudSyncStatus.value = CloudSyncStatus.Syncing
            try {
                val pinPair = pendingPinSaltAndHash ?: securityManager?.getCurrentSaltAndHash()
                val version = cloud.publishFullStoreCatalog(
                    products = repository.products.value,
                    deletedProductIds = repository.deletedProductIds.value,
                    categories = repository.categoryCustomizations.value,
                    wallets = repository.walletAccounts.value,
                    deletedWalletIds = repository.deletedWalletIds.value,
                    offerSlotProductIds = repository.offerSlotProductIds.value,
                    pinSaltAndHash = pinPair
                )
                pendingPinSaltAndHash = null
                repository.markWorkSavedAndSynced()
                securityManager?.recordSecurityNotice("تم حفظ العمل ومزامنته سحابياً (الإصدار $version) لجميع الأجهزة")
                _cloudSyncStatus.value = CloudSyncStatus.SyncedSuccess(
                    version = version,
                    message = "تم حفظ العمل ومزامنته بنجاح مع كافة الأجهزة المتصلة بالإنترنت ✓ (الإصدار #$version)"
                )
            } catch (e: CancellationException) {
                throw e
            } catch (e: FirebaseFirestoreException) {
                val msg = if (e.code == FirebaseFirestoreException.Code.PERMISSION_DENIED) {
                    "مرفوض من جدار الحماية السحابي: يجب تسجيل الدخول بحساب المدير المصرح له (${CloudStoreRepository.BOOTSTRAPPED_ADMIN_EMAIL}) لنشر التعديلات على كل الأجهزة."
                } else {
                    "تعذر الحفظ والمزامنة حالياً: ${e.localizedMessage ?: "خطأ في الاتصال"}"
                }
                securityManager?.recordSecurityNotice("فشل المزامنة: ${e.code}")
                _cloudSyncStatus.value = CloudSyncStatus.SyncError(msg)
            } catch (e: Exception) {
                _cloudSyncStatus.value = CloudSyncStatus.SyncError(
                    "تعذر المزامنة: ${e.localizedMessage ?: "يرجى التحقق من اتصال الإنترنت"}"
                )
            }
        }
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

private data class Tuple4<A, B, C, D>(val a: A, val b: B, val c: C, val d: D)
private data class Tuple5<A, B, C, D, E>(val a: A, val b: B, val c: C, val d: D, val e: E)
