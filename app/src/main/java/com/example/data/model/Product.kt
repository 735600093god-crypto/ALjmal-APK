package com.example.data.model

enum class ProductCategory(
    val id: String,
    val title: String,
    val subtitle: String,
    val badgeLabel: String,
    val iconName: String,
    val defaultImageUrl: String
) {
    LOCAL_FRUITS(
        id = "local_fruits",
        title = "قسم الفواكه البلدي",
        subtitle = "رمان صعدي، عنب رازقي، مانجو، شمام، تين",
        badgeLabel = "قسم ١",
        iconName = "apple",
        defaultImageUrl = "https://images.unsplash.com/photo-1615485290382-441e4d049cb5?auto=format&fit=crop&w=600&q=80"
    ),
    IMPORTED_FRUITS(
        id = "imported_fruits",
        title = "قسم الفواكه المستوردة",
        subtitle = "تفاح واشنطن، أفوكادو، موز، كيوي، أناناس",
        badgeLabel = "قسم ٢",
        iconName = "globe",
        defaultImageUrl = "https://images.unsplash.com/photo-1519996529931-28324d5a630e?auto=format&fit=crop&w=600&q=80"
    ),
    VEGETABLES(
        id = "vegetables",
        title = "قسم الخضار",
        subtitle = "طماطم، خيار، ورقيات، بطاطس، كوسة، بروكلي",
        badgeLabel = "قسم ٣",
        iconName = "carrot",
        defaultImageUrl = "https://images.unsplash.com/photo-1540420773420-3366772f4999?auto=format&fit=crop&w=600&q=80"
    ),
    PREMIUM_DATES(
        id = "premium_dates",
        title = "قسم التمور",
        subtitle = "مجدول ملكي، عجوة، سكري رطب، خلاص، صقعي",
        badgeLabel = "قسم ٤",
        iconName = "crown",
        defaultImageUrl = "https://images.unsplash.com/photo-1596797038530-2c107229654b?auto=format&fit=crop&w=600&q=80"
    ),
    DRIED_FRUITS(
        id = "dried_fruits",
        title = "قسم الفواكه المجففة",
        subtitle = "تين تركي، مشمش، زبيب رازقي، قراصيا، صواني هدايا",
        badgeLabel = "قسم ٥",
        iconName = "sun",
        defaultImageUrl = "https://images.unsplash.com/photo-1582293041079-7814c2f12063?auto=format&fit=crop&w=600&q=80"
    )
}

enum class Currency(val code: String, val symbol: String, val rateFromYer: Double) {
    YER("YER", "ر.ي", 1.0),
    USD("USD", "$", 0.00187), // ~535 YER/USD
    SAR("SAR", "ر.س", 0.00704) // ~142 YER/SAR
}

data class Product(
    val id: String,
    val name: String,
    val category: ProductCategory,
    val priceYer: Double,
    val originalPriceYer: Double? = null,
    val unit: String,
    val origin: String,
    val rating: Double,
    val badge: String? = null,
    val imageUrl: String,
    val discountPercent: Int? = null,
    val description: String = "",
    val inStock: Boolean = true
) {
    fun getFormattedPrice(currency: Currency): String {
        return when (currency) {
            Currency.YER -> "${priceYer.toInt()} ${currency.symbol}"
            Currency.USD -> "${"%.2f".format(priceYer * currency.rateFromYer)} ${currency.symbol}"
            Currency.SAR -> "${"%.2f".format(priceYer * currency.rateFromYer)} ${currency.symbol}"
        }
    }

    fun getFormattedOriginalPrice(currency: Currency): String? {
        val orig = originalPriceYer ?: return null
        return when (currency) {
            Currency.YER -> "${orig.toInt()} ${currency.symbol}"
            Currency.USD -> "${"%.2f".format(orig * currency.rateFromYer)} ${currency.symbol}"
            Currency.SAR -> "${"%.2f".format(orig * currency.rateFromYer)} ${currency.symbol}"
        }
    }
}
