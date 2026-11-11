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
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Call
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.Place
import androidx.compose.material.icons.filled.Send
import androidx.compose.material.icons.filled.Star
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Checkbox
import androidx.compose.material3.CheckboxDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Switch
import androidx.compose.material3.SwitchDefaults
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.R
import com.example.data.model.Currency
import com.example.data.model.Product
import com.example.ui.components.ProductCard
import com.example.ui.theme.Amber100
import com.example.ui.theme.Amber400
import com.example.ui.theme.Amber500
import com.example.ui.theme.Amber950
import com.example.ui.theme.Emerald50
import com.example.ui.theme.Emerald800
import com.example.ui.theme.Emerald900
import com.example.ui.theme.Emerald950
import com.example.ui.theme.Stone100
import com.example.ui.theme.Stone200
import com.example.ui.theme.Stone400
import com.example.ui.theme.Stone500
import com.example.ui.theme.Stone700
import com.example.ui.theme.Stone900
import com.example.ui.theme.Stone950
import com.example.ui.theme.White

@Composable
fun AdminScreen(
    products: List<Product>,
    favorites: Set<String>,
    cartMap: Map<String, Int>,
    offerSlotItemIds: List<String>,
    isAdminUnlocked: Boolean,
    currency: Currency,
    storePhone: String,
    storeWhatsApp: String,
    storeLocation: String,
    onUnlockAdminRequest: () -> Unit,
    onLockAdmin: () -> Unit,
    onToggleOfferSlot: (String) -> Unit,
    onUpdatePrice: (String, Double, Double?) -> Unit,
    onProductClick: (Product) -> Unit,
    onAddToCart: (Product) -> Unit,
    onDecrement: (Product) -> Unit,
    onToggleFavorite: (Product) -> Unit,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    var editingProduct by remember { mutableStateOf<Product?>(null) }
    var editPriceText by remember { mutableStateOf("") }
    var editOrigPriceText by remember { mutableStateOf("") }

    LazyColumn(
        modifier = modifier
            .fillMaxSize()
            .background(Stone100)
            .padding(horizontal = 14.dp),
        verticalArrangement = Arrangement.spacedBy(14.dp)
    ) {
        item {
            Spacer(modifier = Modifier.height(6.dp))
        }

        // Store Identity & Contact Card
        item {
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(22.dp),
                colors = CardDefaults.cardColors(containerColor = White),
                elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Box(
                        modifier = Modifier
                            .size(72.dp)
                            .clip(CircleShape)
                            .border(2.dp, Amber400, CircleShape)
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

                    Spacer(modifier = Modifier.height(10.dp))

                    Text(
                        text = "متجر الجمل للخضار والفواكه والتمور",
                        fontSize = 16.sp,
                        fontWeight = FontWeight.Black,
                        color = Emerald950
                    )

                    Text(
                        text = storeLocation,
                        fontSize = 11.sp,
                        color = Stone500,
                        fontWeight = FontWeight.Medium
                    )

                    Spacer(modifier = Modifier.height(14.dp))

                    // Contact Row
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Box(
                            modifier = Modifier
                                .weight(1f)
                                .clip(RoundedCornerShape(12.dp))
                                .background(Amber400)
                                .clickable {
                                    val intent = Intent(Intent.ACTION_DIAL).apply {
                                        data = Uri.parse("tel:$storePhone")
                                    }
                                    context.startActivity(intent)
                                }
                                .padding(vertical = 10.dp),
                            contentAlignment = Alignment.Center
                        ) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Icon(
                                    imageVector = Icons.Default.Call,
                                    contentDescription = null,
                                    tint = Stone950,
                                    modifier = Modifier.size(14.dp)
                                )
                                Spacer(modifier = Modifier.width(6.dp))
                                Text("اتصال بالمتجر", fontSize = 11.sp, fontWeight = FontWeight.Black, color = Stone950)
                            }
                        }

                        Box(
                            modifier = Modifier
                                .weight(1f)
                                .clip(RoundedCornerShape(12.dp))
                                .background(Emerald800)
                                .clickable {
                                    val url = "https://wa.me/$storeWhatsApp?text=${Uri.encode("السلام عليكم متجر الجمل")}"
                                    val intent = Intent(Intent.ACTION_VIEW, Uri.parse(url))
                                    context.startActivity(intent)
                                }
                                .padding(vertical = 10.dp),
                            contentAlignment = Alignment.Center
                        ) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Icon(
                                    imageVector = Icons.Default.Send,
                                    contentDescription = null,
                                    tint = White,
                                    modifier = Modifier.size(14.dp)
                                )
                                Spacer(modifier = Modifier.width(6.dp))
                                Text("محادثة واتساب", fontSize = 11.sp, fontWeight = FontWeight.Bold, color = White)
                            }
                        }
                    }
                }
            }
        }

        // Store Manager (Admin) Section
        item {
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(22.dp),
                colors = CardDefaults.cardColors(containerColor = White),
                border = androidx.compose.foundation.BorderStroke(1.5.dp, Amber400)
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp)
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Box(
                                modifier = Modifier
                                    .size(34.dp)
                                    .clip(RoundedCornerShape(10.dp))
                                    .background(Amber400),
                                contentAlignment = Alignment.Center
                            ) {
                                Icon(
                                    imageVector = Icons.Default.Lock,
                                    contentDescription = null,
                                    tint = Stone950,
                                    modifier = Modifier.size(16.dp)
                                )
                            }
                            Spacer(modifier = Modifier.width(10.dp))
                            Column {
                                Text(
                                    text = "لوحة تحكم المدير",
                                    fontSize = 14.sp,
                                    fontWeight = FontWeight.Black,
                                    color = Stone900
                                )
                                Text(
                                    text = if (isAdminUnlocked) "الوضع مفعل (صلاحيات كاملة)" else "محمية برمز مرور",
                                    fontSize = 10.sp,
                                    color = if (isAdminUnlocked) Emerald800 else Stone400,
                                    fontWeight = FontWeight.Bold
                                )
                            }
                        }

                        if (isAdminUnlocked) {
                            TextButton(onClick = onLockAdmin) {
                                Text("قفل اللوحة", color = Stone500, fontSize = 11.sp)
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(10.dp))

                    if (!isAdminUnlocked) {
                        Button(
                            onClick = onUnlockAdminRequest,
                            modifier = Modifier.fillMaxWidth().height(42.dp),
                            shape = RoundedCornerShape(12.dp),
                            colors = ButtonDefaults.buttonColors(containerColor = Amber400)
                        ) {
                            Text("فتح لوحة التحكم للمدير", fontSize = 12.sp, fontWeight = FontWeight.Black, color = Stone950)
                        }
                    } else {
                        // Admin Controls:
                        Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                            // Section: Offer Slots
                            Text(
                                text = "التحكم في خانات العروض الخاصة (${offerSlotItemIds.size} من 4 خانات مختارة):",
                                fontSize = 12.sp,
                                fontWeight = FontWeight.Bold,
                                color = Stone900
                            )

                            products.take(12).forEach { prod ->
                                val isInSlot = offerSlotItemIds.contains(prod.id)
                                Row(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .clip(RoundedCornerShape(10.dp))
                                        .background(if (isInSlot) Amber100 else Stone100)
                                        .clickable { onToggleOfferSlot(prod.id) }
                                        .padding(horizontal = 10.dp, vertical = 6.dp),
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Row(
                                        verticalAlignment = Alignment.CenterVertically,
                                        modifier = Modifier.weight(1f)
                                    ) {
                                        Checkbox(
                                            checked = isInSlot,
                                            onCheckedChange = { onToggleOfferSlot(prod.id) },
                                            colors = CheckboxDefaults.colors(checkedColor = Amber500)
                                        )
                                        Spacer(modifier = Modifier.width(4.dp))
                                        Text(
                                            text = prod.name,
                                            fontSize = 11.5.sp,
                                            fontWeight = if (isInSlot) FontWeight.Bold else FontWeight.Normal,
                                            color = Stone900
                                        )
                                    }

                                    Text(
                                        text = "${prod.priceYer.toInt()} ر.ي",
                                        fontSize = 11.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = Emerald900
                                    )
                                }
                            }
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
        }

        item {
            Spacer(modifier = Modifier.height(80.dp))
        }
    }
}
