package com.example.ui.screens

import android.content.Intent
import android.net.Uri
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
import androidx.compose.material.icons.filled.Call
import androidx.compose.material.icons.filled.ChevronLeft
import androidx.compose.material.icons.filled.KeyboardArrowLeft
import androidx.compose.material.icons.filled.Send
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.AsyncImage
import coil.request.ImageRequest
import com.example.R
import com.example.data.model.CategoryCustomization
import com.example.data.model.Currency
import com.example.data.model.Product
import com.example.data.model.ProductCategory
import com.example.ui.components.CategoryShowcaseSection
import com.example.ui.components.HeroBanner
import com.example.ui.components.ProductCard
import com.example.ui.components.SpecialOffersSection
import com.example.ui.theme.Amber300
import com.example.ui.theme.Amber400
import com.example.ui.theme.Amber500
import com.example.ui.theme.Emerald200
import com.example.ui.theme.Emerald50
import com.example.ui.theme.Emerald600
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
    categoryCustomizations: Map<String, CategoryCustomization> = emptyMap(),
    onCategorySelected: (ProductCategory?) -> Unit,
    onProductClick: (Product) -> Unit,
    onAddToCart: (Product) -> Unit,
    onDecrement: (Product) -> Unit,
    onToggleFavorite: (Product) -> Unit,
    onLocationClick: () -> Unit,
    onManageSlotsClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current

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

        // 1. Hero Banner with Level Yellow Call Strip and WhatsApp Order Button
        item {
            HeroBanner(
                storePhone = storePhone,
                storeWhatsApp = storeWhatsApp,
                onLocationClick = onLocationClick
            )
        }

        // Official Slogan & Produce Poster Showcase + Quick Leveled Contact Strip
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

                    Spacer(modifier = Modifier.height(10.dp))

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
                                fontSize = 13.5.sp,
                                fontWeight = FontWeight.Black,
                                color = Emerald900
                            )
                            Text(
                                text = "صنعاء - التوفير هايبر فرع حده",
                                fontSize = 10.5.sp,
                                color = Stone500,
                                fontWeight = FontWeight.Medium
                            )
                        }

                        Box(
                            modifier = Modifier
                                .clip(RoundedCornerShape(8.dp))
                                .background(Amber400)
                                .padding(horizontal = 8.dp, vertical = 3.dp)
                        ) {
                            Text(
                                text = "طازج يومياً",
                                fontSize = 9.5.sp,
                                fontWeight = FontWeight.Black,
                                color = Stone950
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(10.dp))

                    // Contact Strip: Yellow Rectangle for Call + WhatsApp Order with Arrow
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(46.dp),
                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        // Level Yellow Call Rectangle
                        Box(
                            modifier = Modifier
                                .weight(1.15f)
                                .fillMaxSize()
                                .clip(RoundedCornerShape(12.dp))
                                .background(Amber400)
                                .border(1.dp, Amber500, RoundedCornerShape(12.dp))
                                .clickable {
                                    val intent = Intent(Intent.ACTION_DIAL).apply {
                                        data = Uri.parse("tel:$storePhone")
                                    }
                                    context.startActivity(intent)
                                }
                                .padding(horizontal = 8.dp),
                            contentAlignment = Alignment.Center
                        ) {
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.Center
                            ) {
                                Icon(
                                    imageVector = Icons.Default.Call,
                                    contentDescription = "اتصال",
                                    tint = Stone950,
                                    modifier = Modifier.size(16.dp)
                                )
                                Spacer(modifier = Modifier.width(6.dp))
                                Text(
                                    text = "اتصل:",
                                    color = Stone950,
                                    fontSize = 12.sp,
                                    fontWeight = FontWeight.Black
                                )
                                Spacer(modifier = Modifier.width(4.dp))
                                Text(
                                    text = "776 662 630",
                                    color = Stone950,
                                    fontSize = 13.5.sp,
                                    fontWeight = FontWeight.Black
                                )
                            }
                        }

                        // WhatsApp Order Button with Arrow
                        Box(
                            modifier = Modifier
                                .weight(1.05f)
                                .fillMaxSize()
                                .clip(RoundedCornerShape(12.dp))
                                .background(Emerald600)
                                .border(1.dp, Emerald600, RoundedCornerShape(12.dp))
                                .clickable {
                                    val url = "https://wa.me/$storeWhatsApp?text=${Uri.encode("السلام عليكم، أود طلب خضار وفواكه وتمور من متجر الجمل")}"
                                    val intent = Intent(Intent.ACTION_VIEW, Uri.parse(url))
                                    context.startActivity(intent)
                                }
                                .padding(horizontal = 8.dp),
                            contentAlignment = Alignment.Center
                        ) {
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.Center
                            ) {
                                Icon(
                                    imageVector = Icons.Default.Send,
                                    contentDescription = "واتساب",
                                    tint = White,
                                    modifier = Modifier.size(15.dp)
                                )
                                Spacer(modifier = Modifier.width(5.dp))
                                Text(
                                    text = "طلب الواتس",
                                    color = White,
                                    fontSize = 12.5.sp,
                                    fontWeight = FontWeight.Black
                                )
                                Spacer(modifier = Modifier.width(4.dp))
                                Icon(
                                    imageVector = Icons.Default.ChevronLeft,
                                    contentDescription = null,
                                    tint = Amber300,
                                    modifier = Modifier.size(18.dp)
                                )
                            }
                        }
                    }
                }
            }
        }

        // 2. Categories Showcase with Photos (Controlled & changeable from Admin)
        item {
            CategoryShowcaseSection(
                selectedCategory = selectedCategory,
                categoryCustomizations = categoryCustomizations,
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
                    categoryCustomizations = categoryCustomizations,
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
                            categoryCustomizations = categoryCustomizations,
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
    categoryCustomizations: Map<String, CategoryCustomization>,
    onProductClick: (Product) -> Unit,
    onAddToCart: (Product) -> Unit,
    onDecrement: (Product) -> Unit,
    onToggleFavorite: (Product) -> Unit,
    onViewAllClick: () -> Unit
) {
    val custom = categoryCustomizations[category.id]
    val title = custom?.customTitle ?: category.title
    val subtitle = custom?.customSubtitle ?: category.subtitle
    val imageUrl = custom?.customImageUrl ?: category.defaultImageUrl

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
            // Section Header with Category Thumbnail
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
                    // Category Photo Thumbnail
                    Box(
                        modifier = Modifier
                            .size(42.dp)
                            .clip(RoundedCornerShape(10.dp))
                            .background(Stone200)
                    ) {
                        AsyncImage(
                            model = ImageRequest.Builder(LocalContext.current)
                                .data(imageUrl)
                                .crossfade(true)
                                .build(),
                            contentDescription = title,
                            contentScale = ContentScale.Crop,
                            modifier = Modifier.fillMaxSize()
                        )
                    }

                    Spacer(modifier = Modifier.width(8.dp))

                    Column {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Text(
                                text = title,
                                fontSize = 13.5.sp,
                                fontWeight = FontWeight.Black,
                                color = Stone900
                            )
                            Spacer(modifier = Modifier.width(6.dp))
                            Box(
                                modifier = Modifier
                                    .clip(RoundedCornerShape(6.dp))
                                    .background(Amber400)
                                    .padding(horizontal = 5.dp, vertical = 1.dp)
                            ) {
                                Text(
                                    text = category.badgeLabel,
                                    color = Stone950,
                                    fontSize = 8.5.sp,
                                    fontWeight = FontWeight.Bold
                                )
                            }
                        }

                        Text(
                            text = subtitle,
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
