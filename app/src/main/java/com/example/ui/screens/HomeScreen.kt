package com.example.ui.screens

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ChevronLeft
import androidx.compose.material.icons.filled.KeyboardArrowLeft
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.R
import com.example.data.model.Currency
import com.example.data.model.Product
import com.example.data.model.ProductCategory
import com.example.ui.components.CategoryChips
import com.example.ui.components.HeroBanner
import com.example.ui.components.ProductCard
import com.example.ui.components.SpecialOffersSection
import com.example.ui.theme.Amber400
import com.example.ui.theme.Emerald200
import com.example.ui.theme.Emerald50
import com.example.ui.theme.Emerald800
import com.example.ui.theme.Emerald900
import com.example.ui.theme.Stone100
import com.example.ui.theme.Stone200
import com.example.ui.theme.Stone400
import com.example.ui.theme.Stone500
import com.example.ui.theme.Stone900
import com.example.ui.theme.Stone950
import com.example.ui.theme.White

@Composable
fun HomeScreen(
    products: List<Product>,
    slotProducts: List<Product>,
    cartMap: Map<String, Int>,
    favorites: Set<String>,
    selectedCategory: ProductCategory?,
    currency: Currency,
    storePhone: String,
    storeWhatsApp: String,
    onCategorySelected: (ProductCategory?) -> Unit,
    onProductClick: (Product) -> Unit,
    onAddToCart: (Product) -> Unit,
    onDecrement: (Product) -> Unit,
    onToggleFavorite: (Product) -> Unit,
    onLocationClick: () -> Unit,
    onManageSlotsClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    LazyColumn(
        modifier = modifier
            .fillMaxSize()
            .background(Stone100)
            .padding(horizontal = 12.dp),
        verticalArrangement = Arrangement.spacedBy(14.dp)
    ) {
        item {
            Spacer(modifier = Modifier.height(4.dp))
        }

        // 1. Hero Banner
        item {
            HeroBanner(
                storePhone = storePhone,
                storeWhatsApp = storeWhatsApp,
                onLocationClick = onLocationClick
            )
        }

        // Official Slogan & Produce Poster Showcase
        item {
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(22.dp),
                colors = CardDefaults.cardColors(containerColor = White),
                elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
            ) {
                Column(modifier = Modifier.padding(10.dp)) {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(180.dp)
                            .clip(RoundedCornerShape(16.dp))
                    ) {
                        Image(
                            painter = painterResource(id = R.drawable.aljamal_poster),
                            contentDescription = "جودة نختارها لك وطعم يستحق التجربه",
                            contentScale = ContentScale.Crop,
                            modifier = Modifier.fillMaxSize()
                        )
                    }

                    Spacer(modifier = Modifier.height(8.dp))

                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 4.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column {
                            Text(
                                text = "جودة نختارها لك... وطعم يستحق التجربه",
                                fontSize = 13.sp,
                                fontWeight = FontWeight.Black,
                                color = Emerald900
                            )
                            Text(
                                text = "صنعاء - التوفير هايبر فرع حده • هاتف: 776 662 630",
                                fontSize = 10.sp,
                                color = Stone500,
                                fontWeight = FontWeight.Medium
                            )
                        }

                        Box(
                            modifier = Modifier
                                .clip(RoundedCornerShape(8.dp))
                                .background(Amber400)
                                .padding(horizontal = 7.dp, vertical = 3.dp)
                        ) {
                            Text(
                                text = "طازج يومياً",
                                fontSize = 9.sp,
                                fontWeight = FontWeight.Black,
                                color = Stone950
                            )
                        }
                    }
                }
            }
        }

        // 2. Category Chips
        item {
            CategoryChips(
                selectedCategory = selectedCategory,
                onCategorySelected = onCategorySelected
            )
        }

        // 3. Special Offers 4-slot Section
        item {
            SpecialOffersSection(
                slotProducts = slotProducts,
                currency = currency,
                onManageSlotsClick = onManageSlotsClick,
                onProductClick = onProductClick,
                onAddToCart = onAddToCart
            )
        }

        // 4. Products Grouped by Category (or filtered)
        if (selectedCategory != null) {
            val categoryProducts = products.filter { it.category == selectedCategory }
            item {
                CategorySectionView(
                    category = selectedCategory,
                    products = categoryProducts,
                    currency = currency,
                    cartMap = cartMap,
                    favorites = favorites,
                    onProductClick = onProductClick,
                    onAddToCart = onAddToCart,
                    onDecrement = onDecrement,
                    onToggleFavorite = onToggleFavorite,
                    onViewAllClick = {}
                )
            }
        } else {
            // Show all 5 categories exactly as rendered in the HTML
            ProductCategory.values().forEach { cat ->
                val categoryProducts = products.filter { it.category == cat }
                if (categoryProducts.isNotEmpty()) {
                    item(key = cat.id) {
                        CategorySectionView(
                            category = cat,
                            products = categoryProducts,
                            currency = currency,
                            cartMap = cartMap,
                            favorites = favorites,
                            onProductClick = onProductClick,
                            onAddToCart = onAddToCart,
                            onDecrement = onDecrement,
                            onToggleFavorite = onToggleFavorite,
                            onViewAllClick = { onCategorySelected(cat) }
                        )
                    }
                }
            }
        }

        item {
            Spacer(modifier = Modifier.height(80.dp)) // padding for bottom nav
        }
    }
}

@Composable
private fun CategorySectionView(
    category: ProductCategory,
    products: List<Product>,
    currency: Currency,
    cartMap: Map<String, Int>,
    favorites: Set<String>,
    onProductClick: (Product) -> Unit,
    onAddToCart: (Product) -> Unit,
    onDecrement: (Product) -> Unit,
    onToggleFavorite: (Product) -> Unit,
    onViewAllClick: () -> Unit
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(24.dp),
        colors = CardDefaults.cardColors(containerColor = White),
        elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(14.dp)
        ) {
            // Section Header matching HTML
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(bottom = 10.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier.weight(1f)
                ) {
                    Box(
                        modifier = Modifier
                            .clip(RoundedCornerShape(8.dp))
                            .background(Amber400)
                            .padding(horizontal = 7.dp, vertical = 2.dp)
                    ) {
                        Text(
                            text = category.badgeLabel,
                            color = Stone950,
                            fontSize = 10.sp,
                            fontWeight = FontWeight.Black
                        )
                    }

                    Spacer(modifier = Modifier.width(8.dp))

                    Column {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Text(
                                text = category.title,
                                fontSize = 13.5.sp,
                                fontWeight = FontWeight.Black,
                                color = Stone900
                            )
                            Spacer(modifier = Modifier.width(6.dp))
                            Box(
                                modifier = Modifier
                                    .clip(RoundedCornerShape(6.dp))
                                    .background(Emerald50)
                                    .border(1.dp, Emerald200.copy(alpha = 0.6f), RoundedCornerShape(6.dp))
                                    .padding(horizontal = 5.dp, vertical = 1.dp)
                            ) {
                                Text(
                                    text = when (category) {
                                        ProductCategory.LOCAL_FRUITS -> "قطاف محلي طازج"
                                        ProductCategory.IMPORTED_FRUITS -> "عالمي نخب أول"
                                        ProductCategory.VEGETABLES -> "طازج اليوم"
                                        ProductCategory.PREMIUM_DATES -> "نخب أول ملوكي"
                                        ProductCategory.DRIED_FRUITS -> "طبيعي 100%"
                                    },
                                    color = Emerald800,
                                    fontSize = 8.5.sp,
                                    fontWeight = FontWeight.Bold
                                )
                            }
                        }

                        Text(
                            text = category.subtitle,
                            fontSize = 9.5.sp,
                            color = Stone400,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis
                        )
                    }
                }

                Row(
                    modifier = Modifier
                        .clickable { onViewAllClick() }
                        .padding(start = 6.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "عرض الكل (${products.size})",
                        color = Emerald800,
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold
                    )
                    Icon(
                        imageVector = Icons.Default.KeyboardArrowLeft,
                        contentDescription = null,
                        tint = Emerald800,
                        modifier = Modifier.size(16.dp)
                    )
                }
            }

            Spacer(modifier = Modifier.height(4.dp))

            // 2-column Grid of Products
            val chunked = products.chunked(2)
            chunked.forEach { rowProducts ->
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 5.dp),
                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    rowProducts.forEach { product ->
                        Box(modifier = Modifier.weight(1f)) {
                            ProductCard(
                                product = product,
                                currency = currency,
                                cartQuantity = cartMap[product.id] ?: 0,
                                isFavorite = favorites.contains(product.id),
                                onProductClick = { onProductClick(product) },
                                onAddToCart = { onAddToCart(product) },
                                onDecrement = { onDecrement(product) },
                                onToggleFavorite = { onToggleFavorite(product) }
                            )
                        }
                    }

                    if (rowProducts.size == 1) {
                        Spacer(modifier = Modifier.weight(1f))
                    }
                }
            }
        }
    }
}
