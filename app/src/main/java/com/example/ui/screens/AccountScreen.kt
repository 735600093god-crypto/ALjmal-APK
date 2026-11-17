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
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.Send
import androidx.compose.material.icons.filled.Call
import androidx.compose.material.icons.filled.FavoriteBorder
import androidx.compose.material.icons.filled.LocationOn
import androidx.compose.material.icons.filled.Verified
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.R
import com.example.data.model.Currency
import com.example.data.model.Product
import com.example.ui.components.ProductCard
import com.example.ui.theme.Amber400
import com.example.ui.theme.Emerald100
import com.example.ui.theme.Emerald50
import com.example.ui.theme.Emerald800
import com.example.ui.theme.Emerald900
import com.example.ui.theme.Emerald950
import com.example.ui.theme.Stone100
import com.example.ui.theme.Stone400
import com.example.ui.theme.Stone500
import com.example.ui.theme.Stone700
import com.example.ui.theme.Stone900
import com.example.ui.theme.Stone950
import com.example.ui.theme.White

@Composable
fun AccountScreen(
    products: List<Product>,
    favorites: Set<String>,
    cartMap: Map<String, Int>,
    currency: Currency,
    storePhone: String,
    storeWhatsApp: String,
    storeLocation: String,
    onProductClick: (Product) -> Unit,
    onAddToCart: (Product) -> Unit,
    onDecrement: (Product) -> Unit,
    onToggleFavorite: (Product) -> Unit,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current

    LazyColumn(
        modifier = modifier
            .fillMaxSize()
            .background(Stone100)
            .padding(horizontal = 14.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        item {
            Spacer(modifier = Modifier.height(6.dp))
        }

        // Store Identity & Customer Contact Card
        item {
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(20.dp),
                colors = CardDefaults.cardColors(containerColor = White),
                elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(14.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Box(
                            modifier = Modifier
                                .size(54.dp)
                                .clip(CircleShape)
                                .border(1.5.dp, Amber400, CircleShape)
                                .background(White)
                                .padding(2.dp)
                        ) {
                            Image(
                                painter = painterResource(id = R.drawable.ic_aljamal_logo),
                                contentDescription = "شعار متجر الجمل",
                                contentScale = ContentScale.Crop,
                                modifier = Modifier
                                    .fillMaxSize()
                                    .clip(CircleShape)
                            )
                        }

                        Spacer(modifier = Modifier.width(12.dp))

                        Column(modifier = Modifier.weight(1f)) {
                            Text(
                                text = "متجر الجمل للخضار والفواكه والتمور",
                                fontSize = 14.sp,
                                fontWeight = FontWeight.Black,
                                color = Emerald950
                            )
                            Text(
                                text = storeLocation,
                                fontSize = 10.5.sp,
                                color = Stone500,
                                fontWeight = FontWeight.Medium
                            )
                            Text(
                                text = "متصل ومتزامن تلقائياً مع متجر الجمل ✓",
                                fontSize = 9.5.sp,
                                color = Emerald800,
                                fontWeight = FontWeight.Bold
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(12.dp))

                    // Contact Row
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Box(
                            modifier = Modifier
                                .weight(1f)
                                .height(48.dp)
                                .clip(RoundedCornerShape(10.dp))
                                .background(Amber400)
                                .clickable {
                                    val intent = Intent(Intent.ACTION_DIAL).apply {
                                        data = Uri.parse("tel:$storePhone")
                                    }
                                    context.startActivity(intent)
                                }
                                .testTag("call_store_button"),
                            contentAlignment = Alignment.Center
                        ) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Icon(
                                    imageVector = Icons.Default.Call,
                                    contentDescription = "اتصال بالمتجر",
                                    tint = Stone950,
                                    modifier = Modifier.size(15.dp)
                                )
                                Spacer(modifier = Modifier.width(5.dp))
                                Text("اتصال بالمتجر", fontSize = 11.5.sp, fontWeight = FontWeight.Black, color = Stone950)
                            }
                        }

                        Box(
                            modifier = Modifier
                                .weight(1f)
                                .height(48.dp)
                                .clip(RoundedCornerShape(10.dp))
                                .background(Emerald800)
                                .clickable {
                                    val url = "https://wa.me/$storeWhatsApp?text=${Uri.encode("السلام عليكم متجر الجمل")}"
                                    val intent = Intent(Intent.ACTION_VIEW, Uri.parse(url))
                                    context.startActivity(intent)
                                }
                                .testTag("whatsapp_store_button"),
                            contentAlignment = Alignment.Center
                        ) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Icon(
                                    imageVector = Icons.AutoMirrored.Filled.Send,
                                    contentDescription = "محادثة واتساب",
                                    tint = White,
                                    modifier = Modifier.size(15.dp)
                                )
                                Spacer(modifier = Modifier.width(5.dp))
                                Text("محادثة واتساب", fontSize = 11.5.sp, fontWeight = FontWeight.Bold, color = White)
                            }
                        }
                    }
                }
            }
        }

        // Store Highlights Card
        item {
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(containerColor = Emerald50),
                elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(12.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Box(
                        modifier = Modifier
                            .size(36.dp)
                            .clip(RoundedCornerShape(10.dp))
                            .background(Emerald100),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = Icons.Default.Verified,
                            contentDescription = null,
                            tint = Emerald900,
                            modifier = Modifier.size(20.dp)
                        )
                    }
                    Spacer(modifier = Modifier.width(10.dp))
                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            text = "جودة نختارها لك... وطعم يستحق التجربة",
                            fontSize = 11.5.sp,
                            fontWeight = FontWeight.Black,
                            color = Emerald950
                        )
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(
                                imageVector = Icons.Default.LocationOn,
                                contentDescription = null,
                                tint = Emerald800,
                                modifier = Modifier.size(12.dp)
                            )
                            Spacer(modifier = Modifier.width(3.dp))
                            Text(
                                text = "توصيل سريع وطازج يومياً في صنعاء - الأسعار والعروض محدثة تلقائياً",
                                fontSize = 9.5.sp,
                                color = Stone700,
                                fontWeight = FontWeight.Medium
                            )
                        }
                    }
                }
            }
        }

        // Favorites Section
        val favoriteProducts = products.filter { favorites.contains(it.id) }
        if (favoriteProducts.isNotEmpty()) {
            item {
                Text(
                    text = "الأصناف المفضلة لديك (${favoriteProducts.size})",
                    fontSize = 14.sp,
                    fontWeight = FontWeight.Black,
                    color = Stone900,
                    modifier = Modifier.padding(horizontal = 4.dp)
                )
            }

            val chunked = favoriteProducts.chunked(2)
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
                                isFavorite = true,
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
        } else {
            item {
                Surface(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(16.dp),
                    color = White
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(24.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Icon(
                            imageVector = Icons.Default.FavoriteBorder,
                            contentDescription = null,
                            tint = Stone400,
                            modifier = Modifier.size(36.dp)
                        )
                        Spacer(modifier = Modifier.height(8.dp))
                        Text(
                            text = "لا توجد أصناف في المفضلة حالياً",
                            fontSize = 13.sp,
                            fontWeight = FontWeight.Bold,
                            color = Stone700
                        )
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(
                            text = "اضغط على رمز القلب بجانب أي منتج لإضافته هنا لسهولة الوصول إليه لاحقاً.",
                            fontSize = 11.sp,
                            color = Stone500,
                            textAlign = TextAlign.Center
                        )
                    }
                }
            }
        }

        item {
            Spacer(modifier = Modifier.height(80.dp))
        }
    }
}
