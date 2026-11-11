package com.example

import com.example.data.model.CartItem
import com.example.data.model.Currency
import com.example.data.model.OrderSubmission
import com.example.data.model.PaymentMethod
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
            deliveryAddress = "صنعاء - حدة",
            deliveryNotes = "سريع",
            paymentMethod = PaymentMethod.CASH_ON_DELIVERY,
            items = listOf(CartItem(product1, 2)),
            subtotalYer = 56.0,
            deliveryFeeYer = 5.0,
            totalYer = 61.0
        )

        val message = order.generateWhatsAppMessage()
        assertTrue(message.contains("متجر الجمل"))
        assertTrue(message.contains("عبدالرحمن"))
        assertTrue(message.contains("776662630"))
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
}
