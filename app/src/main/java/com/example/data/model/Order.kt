package com.example.data.model

data class CartItem(
    val product: Product,
    val quantity: Int
) {
    val totalPriceYer: Double
        get() = product.priceYer * quantity
}

data class WalletAccount(
    val id: String,
    val name: String,
    val accountNumber: String,
    val accountHolder: String,
    val note: String = "",
    val isEnabled: Boolean = true
)

data class CategoryCustomization(
    val categoryId: String,
    val customTitle: String,
    val customSubtitle: String,
    val customImageUrl: String? = null
)

data class OrderSubmission(
    val orderId: String,
    val customerName: String,
    val customerPhone: String,
    val deliveryAddress: String,
    val deliveryNotes: String,
    val paymentMethodName: String,
    val walletDetails: String? = null,
    val items: List<CartItem>,
    val subtotalYer: Double,
    val deliveryFeeYer: Double,
    val totalYer: Double,
    val timestamp: Long = System.currentTimeMillis()
) {
    fun generateWhatsAppMessage(): String {
        val itemsSummary = items.joinToString("\n") {
            "▪️ ${it.product.name} (${it.quantity} ${it.product.unit}) = ${(it.product.priceYer * it.quantity).toInt()} ر.ي"
        }

        val walletInfo = if (!walletDetails.isNullOrBlank()) "\n💳 تفاصيل الحساب: $walletDetails" else ""

        return """
            السلام عليكم ورحمة الله وبركاته،
            طلب جديد من تطبيق *متجر الجمل* 🛒
            
            *بيانات العميل:*
            👤 الاسم: $customerName
            📱 الهاتف: $customerPhone
            📍 العنوان: $deliveryAddress
            📝 ملاحظات: ${if (deliveryNotes.isNotBlank()) deliveryNotes else "لا توجد"}
            💳 طريقة الدفع: $paymentMethodName$walletInfo
            
            *تفاصيل الأصناف:*
            $itemsSummary
            
            💵 إجمالي الأصناف: ${subtotalYer.toInt()} ر.ي
            🚚 رسوم التوصيل: ${deliveryFeeYer.toInt()} ر.ي
            ⭐️ *الإجمالي الكلي:* ${totalYer.toInt()} ر.ي
            
            الفرع الرئيسي: صنعاء - حدة داخل التوفير هايبر
            شكراً لثقتكم بمتجر الجمل!
        """.trimIndent()
    }
}
