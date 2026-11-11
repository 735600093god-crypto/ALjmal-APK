package com.example.ui.screens

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
import androidx.compose.material.icons.filled.DateRange
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.filled.LocationOn
import androidx.compose.material.icons.filled.ShoppingCart
import androidx.compose.material.icons.filled.Star
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.ScrollableTabRow
import androidx.compose.material3.Tab
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.model.Currency
import com.example.data.model.Product
import com.example.data.model.ProductCategory
import com.example.ui.components.ProductCard
import com.example.ui.theme.Amber400
import com.example.ui.theme.Emerald800
import com.example.ui.theme.Emerald900
import com.example.ui.theme.Stone100
import com.example.ui.theme.Stone400
import com.example.ui.theme.Stone500
import com.example.ui.theme.Stone900
import com.example.ui.theme.Stone950
import com.example.ui.theme.White

@Composable
fun CategoriesScreen(
    products: List<Product>,
    selectedCategory: ProductCategory?,
    cartMap: Map<String, Int>,
    favorites: Set<String>,
    currency: Currency,
    onCategorySelected: (ProductCategory?) -> Unit,
    onProductClick: (Product) -> Unit,
    onAddToCart: (Product) -> Unit,
    onDecrement: (Product) -> Unit,
    onToggleFavorite: (Product) -> Unit,
    modifier: Modifier = Modifier
) {
    val activeCategory = selectedCategory ?: ProductCategory.LOCAL_FRUITS
    val categoryProducts = products.filter { it.category == activeCategory }

    Column(
        modifier = modifier
            .fillMaxSize()
            .background(Stone100)
    ) {
        // Tab Row for the 5 categories
        ScrollableTabRow(
            selectedTabIndex = ProductCategory.values().indexOf(activeCategory),
            containerColor = White,
            contentColor = Emerald800,
            edgePadding = 12.dp
        ) {
            ProductCategory.values().forEach { cat ->
                val isSelected = cat == activeCategory
                Tab(
                    selected = isSelected,
                    onClick = { onCategorySelected(cat) },
                    text = {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Text(
                                text = cat.title,
                                fontWeight = if (isSelected) FontWeight.Black else FontWeight.Medium,
                                fontSize = 12.sp,
                                color = if (isSelected) Emerald900 else Stone500
                            )
                            Spacer(modifier = Modifier.width(4.dp))
                            Box(
                                modifier = Modifier
                                    .clip(RoundedCornerShape(8.dp))
                                    .background(if (isSelected) Amber400 else Stone100)
                                    .padding(horizontal = 4.dp, vertical = 1.dp)
                            ) {
                                Text(
                                    text = cat.badgeLabel,
                                    fontSize = 8.5.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = Stone950
                                )
                            }
                        }
                    }
                )
            }
        }

        // Content
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = 12.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            item {
                Spacer(modifier = Modifier.height(10.dp))
            }

            // Category Banner Card
            item {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(18.dp),
                    colors = CardDefaults.cardColors(containerColor = White)
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(14.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Box(
                            modifier = Modifier
                                .size(44.dp)
                                .clip(RoundedCornerShape(12.dp))
                                .background(Amber400.copy(alpha = 0.2f)),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                imageVector = when (activeCategory) {
                                    ProductCategory.LOCAL_FRUITS -> Icons.Default.Favorite
                                    ProductCategory.IMPORTED_FRUITS -> Icons.Default.LocationOn
                                    ProductCategory.VEGETABLES -> Icons.Default.ShoppingCart
                                    ProductCategory.PREMIUM_DATES -> Icons.Default.Star
                                    ProductCategory.DRIED_FRUITS -> Icons.Default.DateRange
                                },
                                contentDescription = null,
                                tint = Emerald900,
                                modifier = Modifier.size(24.dp)
                            )
                        }

                        Spacer(modifier = Modifier.width(12.dp))

                        Column {
                            Text(
                                text = "${activeCategory.title} (${categoryProducts.size} صنف)",
                                fontSize = 15.sp,
                                fontWeight = FontWeight.Black,
                                color = Stone900
                            )
                            Text(
                                text = activeCategory.subtitle,
                                fontSize = 11.sp,
                                color = Stone500
                            )
                        }
                    }
                }
            }

            // Products Grid
            val chunked = categoryProducts.chunked(2)
            items(chunked.size) { index ->
                val rowProducts = chunked[index]
                Row(
                    modifier = Modifier.fillMaxWidth(),
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

            item {
                Spacer(modifier = Modifier.height(80.dp))
            }
        }
    }
}
