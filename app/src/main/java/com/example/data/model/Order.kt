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
    val recipientName: String = "",
    val recipientPhone: String = "",
    val deliveryAddress: String = "",
    val googleMapsLocationUrl: String = "",
    val deliveryNotes: String = "",
    val paymentMethodName: String,
    val walletDetails: String? = null,
    val paymentTransactionNumber: String = "",
    val items: List<CartItem>,
    val subtotalYer: Double,
    val deliveryFeeYer: Double,
    val totalYer: Double,
    val timestamp: Long = System.currentTimeMillis()
) {
    fun generateWhatsAppMessage(): String {
        val itemsSummary = items.joinToString("\n") {
            val lineTotal = (it.product.priceYer * it.quantity).toInt()
            "▪️ ${it.product.name}\n   الكمية: ${it.quantity} ${it.product.unit} × ${it.product.priceYer.toInt()} ر.ي = $lineTotal ر.ي"
        }

        val recipientSection = buildString {
            if (recipientName.isNotBlank() || recipientPhone.isNotBlank()) {
                append("\n\n*بيانات المستلم (حارس / ابن / آخر):*")
                if (recipientName.isNotBlank()) append("\n🤝 اسم المستلم: $recipientName")
                if (recipientPhone.isNotBlank()) append("\n📞 رقم المستلم: $recipientPhone")
            }
        }

        val locationSection = buildString {
            append("\n\n*بيانات العنوان والموقع:*")
            if (deliveryAddress.isNotBlank()) {
                append("\n🏠 العنوان: $deliveryAddress")
            } else if (googleMapsLocationUrl.isBlank()) {
                append("\n🏠 العنوان: لم يُحدد (سيتم التنسيق عبر الاتصال)")
            }
            if (googleMapsLocationUrl.isNotBlank()) {
                append("\n📍 موقع خرائط جوجل المباشر:\n$googleMapsLocationUrl")
            }
            if (deliveryNotes.isNotBlank()) {
                append("\n📝 ملاحظات: $deliveryNotes")
            }
        }

        val paymentSection = buildString {
            append("\n\n*بيانات الدفع:*")
            append("\n💳 طريقة الدفع: $paymentMethodName")
            if (!walletDetails.isNullOrBlank()) {
                append("\n🏦 المحفظة/الحساب: $walletDetails")
            }
            if (paymentTransactionNumber.isNotBlank()) {
                append("\n🧾 رقم عملية الدفع: $paymentTransactionNumber")
            }
        }

        return """
            السلام عليكم ورحمة الله وبركاته،
            طلب جديد من تطبيق *متجر الجمل* 🛒
            رقم الطلب: *$orderId*

            *بيانات الزبون:*
            👤 الاسم: $customerName
            📱 الهاتف: $customerPhone$recipientSection$locationSection$paymentSection

            *تفاصيل السلة (${items.sumOf { it.quantity }} عناصر):*
            $itemsSummary

            *ملخص الفاتورة:*
            💵 إجمالي الأصناف: ${subtotalYer.toInt()} ر.ي
            🚚 رسوم التوصيل: ${deliveryFeeYer.toInt()} ر.ي
            ⭐️ *مجموع الطلب الإجمالي:* ${totalYer.toInt()} ر.ي

            الفرع الرئيسي: صنعاء - حدة داخل التوفير هايبر
            شكراً لثقتكم بمتجر الجمل!
        """.trimIndent()
    }
}
