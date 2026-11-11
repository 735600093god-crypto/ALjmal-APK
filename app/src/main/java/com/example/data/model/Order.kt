package com.example.data.model

data class CartItem(
    val product: Product,
    val quantity: Int
) {
    val totalPriceYer: Double
        get() = product.priceYer * quantity
}

enum class PaymentMethod(val title: String, val subtitle: String, val iconName: String) {
    CASH_ON_DELIVERY("الدفع نقداً عند الاستلام", "ادفع عند وصول الطلب لباب منزلك", "cash"),
    KURAIMI("بنك الكريمي (حاسب / m-floos)", "تحويل فوري لحساب المتجر بالكريمي", "bank"),
    JAWALI("محفظة جوالي", "الدفع عبر تطبيق جوالي", "wallet"),
    FLOOSAK("محفظة فلوسك", "الدفع السريع عبر فلوسك", "wallet"),
    ONE_CASH("محفظة ون كاش", "الدفع عبر ون كاش المعتمد", "wallet")
}

data class OrderSubmission(
    val orderId: String,
    val customerName: String,
    val customerPhone: String,
    val deliveryAddress: String,
    val deliveryNotes: String,
    val paymentMethod: PaymentMethod,
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

        return """
            السلام عليكم ورحمة الله وبركاته،
            طلب جديد من تطبيق *متجر الجمل* 🛒
            
            *بيانات العميل:*
            👤 الاسم: $customerName
            📱 الهاتف: $customerPhone
            📍 العنوان: $deliveryAddress
            📝 ملاحظات: ${if (deliveryNotes.isNotBlank()) deliveryNotes else "لا توجد"}
            💳 طريقة الدفع: ${paymentMethod.title}
            
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
