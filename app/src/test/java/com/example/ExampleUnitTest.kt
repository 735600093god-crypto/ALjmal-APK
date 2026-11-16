package com.example

import com.example.data.model.CartItem
import com.example.data.model.Currency
import com.example.data.model.OrderSubmission
import com.example.data.model.ProductCategory
import com.example.data.repository.StoreRepository
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertTrue
import org.junit.Test

class ExampleUnitTest {

    @Test
    fun testStoreProductsLoadedCorrectly() {
        val repository = StoreRepository()
        val products = repository.products.value

        assertEquals(30, products.size)
        assertEquals(6, products.count { it.category == ProductCategory.LOCAL_FRUITS })
        assertEquals(6, products.count { it.category == ProductCategory.IMPORTED_FRUITS })
        assertEquals(7, products.count { it.category == ProductCategory.VEGETABLES })
        assertEquals(5, products.count { it.category == ProductCategory.PREMIUM_DATES })
        assertEquals(6, products.count { it.category == ProductCategory.DRIED_FRUITS })
    }

    @Test
    fun testCartOperations() {
        val repository = StoreRepository()
        repository.clearCart()
        assertEquals(0, repository.cart.value.size)

        repository.addToCart("p1")
        assertEquals(1, repository.cart.value["p1"])

        repository.addToCart("p1")
        assertEquals(2, repository.cart.value["p1"])

        repository.decrementFromCart("p1")
        assertEquals(1, repository.cart.value["p1"])

        repository.removeFromCart("p1")
        assertTrue(repository.cart.value["p1"] == null)
    }

    @Test
    fun testWhatsAppMessageFormatting() {
        val repository = StoreRepository()
        val product1 = repository.products.value.first { it.id == "p1" }

        val order = OrderSubmission(
            orderId = "JM-1234",
            customerName = "عبدالرحمن",
            customerPhone = "776662630",
            recipientName = "أحمد (الحارس)",
            recipientPhone = "771112233",
            deliveryAddress = "صنعاء - حدة",
            googleMapsLocationUrl = "https://maps.google.com/?q=15.3694,44.1910",
            deliveryNotes = "سريع",
            paymentMethodName = "محفظة جيب (Jeeb)",
            walletDetails = "محفظة جيب (Jeeb) - رقم: 776662630",
            paymentTransactionNumber = "987654321",
            items = listOf(CartItem(product1, 2)),
            subtotalYer = 56.0,
            deliveryFeeYer = 5.0,
            totalYer = 61.0
        )

        val message = order.generateWhatsAppMessage()
        assertTrue(message.contains("متجر الجمل"))
        assertTrue(message.contains("عبدالرحمن"))
        assertTrue(message.contains("776662630"))
        assertTrue(message.contains("أحمد (الحارس)"))
        assertTrue(message.contains("771112233"))
        assertTrue(message.contains("https://maps.google.com/?q=15.3694,44.1910"))
        assertTrue(message.contains("987654321"))
        assertTrue(message.contains("61"))
    }

    @Test
    fun testCurrencyFormatting() {
        val repository = StoreRepository()
        val product = repository.products.value.first { it.id == "p1" }

        val yerPrice = product.getFormattedPrice(Currency.YER)
        assertTrue(yerPrice.contains("ر.ي"))

        val sarPrice = product.getFormattedPrice(Currency.SAR)
        assertTrue(sarPrice.contains("ر.س"))

        val usdPrice = product.getFormattedPrice(Currency.USD)
        assertTrue(usdPrice.contains("$"))
    }

    @Test
    fun testAdminPasswordAndFirewallLockout() {
        val targetPassword = com.example.data.security.AdminSecurityManager.DEFAULT_ADMIN_PASSWORD
        assertEquals("!Admin@Aljamal2026", targetPassword)

        val hash1 = com.example.data.security.AdminSecurityManager.sha256Hex("salt1234", targetPassword)
        val hash2 = com.example.data.security.AdminSecurityManager.sha256Hex("salt1234", "!Admin@Aljamal2026")
        val wrongHash = com.example.data.security.AdminSecurityManager.sha256Hex("salt1234", "00967776662630")

        assertEquals(64, hash1.length)
        assertTrue(com.example.data.security.AdminSecurityManager.constantTimeEquals(hash1, hash2))
        assertTrue(!com.example.data.security.AdminSecurityManager.constantTimeEquals(hash1, wrongHash))
        assertTrue(com.example.data.security.AdminSecurityManager.isSafeImageUrl("https://example.com/a.jpg"))
        assertTrue(com.example.data.security.AdminSecurityManager.isSafeImageUrl("data:image/jpeg;base64,/9j/4AAQ"))
        assertTrue(!com.example.data.security.AdminSecurityManager.isSafeImageUrl("javascript:alert(1)"))
    }

    @Test
    fun testPendingAdminChangesTracking() {
        val repository = StoreRepository()
        assertEquals(false, repository.hasUnsavedChanges.value)

        repository.updateProductPrice("p1", 32.0, 40.0)
        assertEquals(true, repository.hasUnsavedChanges.value)
        assertTrue(repository.pendingChangeSummaries.value.isNotEmpty())

        repository.markWorkSavedAndSynced()
        assertEquals(false, repository.hasUnsavedChanges.value)
        assertTrue(repository.pendingChangeSummaries.value.isEmpty())
    }

    @Test
    fun testAdminWebDashboardLinkAndCloudOrdering() {
        val repository = StoreRepository()
        assertEquals("https://spiffy-sawine-8f76cc.netlify.app", repository.adminWebDashboardUrl)
        assertEquals("akrmshaker8@gmail.com", com.example.data.remote.CloudStoreRepository.BOOTSTRAPPED_ADMIN_EMAIL)
        assertEquals("gen-lang-client-0198222948", com.example.data.remote.CloudStoreRepository.FIREBASE_PROJECT_ID)
        assertEquals("1:315465823648:web:aac7daf4a26ea4d20920c5", com.example.data.remote.CloudStoreRepository.FIREBASE_WEB_APP_ID)

        val defaultOne = repository.products.value.first { it.id == "p1" }
        val defaultTwo = repository.products.value.first { it.id == "p2" }
        val webAdded = defaultOne.copy(id = "p_1760000000000", name = "منتج مضاف من الويب")

        // Firestore returns doc IDs out of catalog order; applyCloudProducts must keep custom web products on top
        repository.applyCloudProducts(listOf(defaultTwo, defaultOne, webAdded))
        val updated = repository.products.value
        assertEquals(3, updated.size)
        assertEquals("p_1760000000000", updated[0].id)
        assertEquals("p1", updated[1].id)
        assertEquals("p2", updated[2].id)
    }
}
