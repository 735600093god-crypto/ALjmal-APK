package com.example.ui.screens

import android.content.Intent
import android.net.Uri
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
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Send
import androidx.compose.material.icons.filled.ShoppingCart
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.RadioButton
import androidx.compose.material3.RadioButtonDefaults
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
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.AsyncImage
import coil.request.ImageRequest
import com.example.data.model.CartItem
import com.example.data.model.Currency
import com.example.data.model.OrderSubmission
import com.example.data.model.Product
import com.example.data.model.WalletAccount
import com.example.ui.theme.Amber400
import com.example.ui.theme.Amber500
import com.example.ui.theme.Amber950
import com.example.ui.theme.Emerald300
import com.example.ui.theme.Emerald50
import com.example.ui.theme.Emerald600
import com.example.ui.theme.Emerald800
import com.example.ui.theme.Emerald900
import com.example.ui.theme.Emerald950
import com.example.ui.theme.Red500
import com.example.ui.theme.Stone100
import com.example.ui.theme.Stone200
import com.example.ui.theme.Stone300
import com.example.ui.theme.Stone400
import com.example.ui.theme.Stone500
import com.example.ui.theme.Stone700
import com.example.ui.theme.Stone900
import com.example.ui.theme.Stone950
import com.example.ui.theme.White

@Composable
fun CartScreen(
    cartItems: List<CartItem>,
    subtotalYer: Double,
    deliveryFeeYer: Double,
    totalYer: Double,
    currency: Currency,
    defaultLocation: String,
    storeWhatsApp: String,
    walletAccounts: List<WalletAccount> = emptyList(),
    onAddToCart: (Product) -> Unit,
    onDecrement: (Product) -> Unit,
    onRemoveFromCart: (Product) -> Unit,
    onClearCart: () -> Unit,
    onSubmitOrder: (String, String, String, String, String, String?) -> OrderSubmission?,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current

    var customerName by remember { mutableStateOf("") }
    var customerPhone by remember { mutableStateOf("") }
    var deliveryAddress by remember { mutableStateOf(defaultLocation) }
    var deliveryNotes by remember { mutableStateOf("") }
    // Default to Cash or first wallet
    var selectedWalletId by remember(walletAccounts) {
        mutableStateOf(walletAccounts.firstOrNull { it.id == "w_cash" }?.id ?: walletAccounts.firstOrNull()?.id ?: "cash")
    }
    var validationError by remember { mutableStateOf<String?>(null) }

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

        // Header Title
        item {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Box(
                        modifier = Modifier
                            .size(36.dp)
                            .clip(RoundedCornerShape(10.dp))
                            .background(Emerald900),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = Icons.Default.ShoppingCart,
                            contentDescription = null,
                            tint = Amber400,
                            modifier = Modifier.size(18.dp)
                        )
                    }
                    Spacer(modifier = Modifier.width(10.dp))
                    Column {
                        Text(
                            text = "سلة المشتريات",
                            fontSize = 17.sp,
                            fontWeight = FontWeight.Black,
                            color = Stone900
                        )
                        Text(
                            text = "${cartItems.sumOf { it.quantity }} عناصر مختارة",
                            fontSize = 11.sp,
                            color = Stone500
                        )
                    }
                }

                if (cartItems.isNotEmpty()) {
                    TextButton(onClick = onClearCart) {
                        Text("تفريغ السلة", color = Red500, fontSize = 11.sp, fontWeight = FontWeight.Bold)
                    }
                }
            }
        }

        if (cartItems.isEmpty()) {
            item {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(20.dp),
                    colors = CardDefaults.cardColors(containerColor = White)
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(30.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Box(
                            modifier = Modifier
                                .size(70.dp)
                                .clip(CircleShape)
                                .background(Stone100),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                imageVector = Icons.Default.ShoppingCart,
                                contentDescription = null,
                                tint = Stone400,
                                modifier = Modifier.size(32.dp)
                            )
                        }
                        Spacer(modifier = Modifier.height(14.dp))
                        Text(
                            text = "السلة فارغة حالياً",
                            fontSize = 15.sp,
                            fontWeight = FontWeight.Bold,
                            color = Stone900
                        )
                        Spacer(modifier = Modifier.height(6.dp))
                        Text(
                            text = "تصفح أقسام متجر الجمل وأضف الخضار والفواكه والتمور الطازجة إلى سلتك",
                            fontSize = 11.sp,
                            color = Stone500,
                            textAlign = TextAlign.Center
                        )
                    }
                }
            }
        } else {
            // Cart Items List
            items(cartItems) { item ->
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(16.dp),
                    colors = CardDefaults.cardColors(containerColor = White),
                    elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(10.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        // Product image
                        Box(
                            modifier = Modifier
                                .size(64.dp)
                                .clip(RoundedCornerShape(12.dp))
                                .background(Stone100)
                        ) {
                            AsyncImage(
                                model = ImageRequest.Builder(LocalContext.current)
                                    .data(item.product.imageUrl)
                                    .crossfade(true)
                                    .build(),
                                contentDescription = item.product.name,
                                contentScale = ContentScale.Crop,
                                modifier = Modifier.fillMaxSize()
                            )
                        }

                        Spacer(modifier = Modifier.width(10.dp))

                        // Info
                        Column(modifier = Modifier.weight(1f)) {
                            Text(
                                text = item.product.name,
                                fontSize = 12.sp,
                                fontWeight = FontWeight.Bold,
                                color = Stone900,
                                maxLines = 1
                            )
                            Text(
                                text = "${item.product.origin} • /${item.product.unit}",
                                fontSize = 9.5.sp,
                                color = Stone500
                            )
                            Spacer(modifier = Modifier.height(4.dp))
                            Text(
                                text = "${item.totalPriceYer.toInt()} ر.ي",
                                fontSize = 13.sp,
                                fontWeight = FontWeight.Black,
                                color = Emerald900
                            )
                        }

                        // Stepper & Delete
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Row(
                                modifier = Modifier
                                    .clip(RoundedCornerShape(8.dp))
                                    .background(Emerald50)
                                    .border(1.dp, Emerald300, RoundedCornerShape(8.dp))
                                    .padding(horizontal = 4.dp, vertical = 2.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Box(
                                    modifier = Modifier
                                        .size(24.dp)
                                        .clip(RoundedCornerShape(6.dp))
                                        .background(White)
                                        .clickable { onDecrement(item.product) },
                                    contentAlignment = Alignment.Center
                                ) {
                                    Text("−", fontSize = 13.sp, fontWeight = FontWeight.Bold, color = Emerald900)
                                }

                                Text(
                                    text = item.quantity.toString(),
                                    fontSize = 12.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = Emerald950,
                                    modifier = Modifier.padding(horizontal = 8.dp)
                                )

                                Box(
                                    modifier = Modifier
                                        .size(24.dp)
                                        .clip(RoundedCornerShape(6.dp))
                                        .background(Emerald800)
                                        .clickable { onAddToCart(item.product) },
                                    contentAlignment = Alignment.Center
                                ) {
                                    Icon(
                                        imageVector = Icons.Default.Add,
                                        contentDescription = "زيادة",
                                        tint = White,
                                        modifier = Modifier.size(12.dp)
                                    )
                                }
                            }

                            Spacer(modifier = Modifier.width(6.dp))

                            Box(
                                modifier = Modifier
                                    .size(28.dp)
                                    .clip(CircleShape)
                                    .clickable { onRemoveFromCart(item.product) },
                                contentAlignment = Alignment.Center
                            ) {
                                Icon(
                                    imageVector = Icons.Default.Close,
                                    contentDescription = "حذف",
                                    tint = Stone400,
                                    modifier = Modifier.size(16.dp)
                                )
                            }
                        }
                    }
                }
            }

            // Price Breakdown Card
            item {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(18.dp),
                    colors = CardDefaults.cardColors(containerColor = White)
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(14.dp),
                        verticalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Text(
                            text = "ملخص الفاتورة",
                            fontSize = 13.sp,
                            fontWeight = FontWeight.Bold,
                            color = Stone900
                        )

                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Text("إجمالي الأصناف:", fontSize = 11.5.sp, color = Stone700)
                            Text("${subtotalYer.toInt()} ر.ي", fontSize = 11.5.sp, fontWeight = FontWeight.Bold, color = Stone900)
                        }

                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Text("رسوم التوصيل المبرد (صنعاء):", fontSize = 11.5.sp, color = Stone700)
                            Text("${deliveryFeeYer.toInt()} ر.ي", fontSize = 11.5.sp, fontWeight = FontWeight.Bold, color = Stone900)
                        }

                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(1.dp)
                                .background(Stone200)
                        )

                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text("المبلغ الإجمالي المستحق:", fontSize = 13.sp, fontWeight = FontWeight.Black, color = Stone900)
                            Text(
                                text = "${totalYer.toInt()} ر.ي",
                                fontSize = 16.sp,
                                fontWeight = FontWeight.Black,
                                color = Emerald900
                            )
                        }
                    }
                }
            }

            // Customer Details Form
            item {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(18.dp),
                    colors = CardDefaults.cardColors(containerColor = White)
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(14.dp),
                        verticalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        Text(
                            text = "بيانات التوصيل والاستلام",
                            fontSize = 13.sp,
                            fontWeight = FontWeight.Bold,
                            color = Stone900
                        )

                        OutlinedTextField(
                            value = customerName,
                            onValueChange = {
                                customerName = it
                                validationError = null
                            },
                            label = { Text("الاسم الكريم", fontSize = 11.sp) },
                            placeholder = { Text("مثال: عبد الرحمن صالح", fontSize = 11.sp) },
                            modifier = Modifier.fillMaxWidth().testTag("cart_input_name"),
                            singleLine = true,
                            shape = RoundedCornerShape(12.dp)
                        )

                        OutlinedTextField(
                            value = customerPhone,
                            onValueChange = {
                                customerPhone = it
                                validationError = null
                            },
                            label = { Text("رقم الهاتف للتواصل والواتساب", fontSize = 11.sp) },
                            placeholder = { Text("مثال: 771234567", fontSize = 11.sp) },
                            modifier = Modifier.fillMaxWidth().testTag("cart_input_phone"),
                            singleLine = true,
                            shape = RoundedCornerShape(12.dp)
                        )

                        OutlinedTextField(
                            value = deliveryAddress,
                            onValueChange = {
                                deliveryAddress = it
                                validationError = null
                            },
                            label = { Text("عنوان التوصيل بصنعاء", fontSize = 11.sp) },
                            placeholder = { Text("مثال: حدة - شارع بيروت - جوار المدرسة", fontSize = 11.sp) },
                            modifier = Modifier.fillMaxWidth().testTag("cart_input_address"),
                            singleLine = true,
                            shape = RoundedCornerShape(12.dp)
                        )

                        OutlinedTextField(
                            value = deliveryNotes,
                            onValueChange = { deliveryNotes = it },
                            label = { Text("ملاحظات خاصة على الطلب (اختياري)", fontSize = 11.sp) },
                            placeholder = { Text("مثال: يرجى اختيار فاكهة شديدة الحلاوة", fontSize = 11.sp) },
                            modifier = Modifier.fillMaxWidth(),
                            singleLine = true,
                            shape = RoundedCornerShape(12.dp)
                        )

                        Spacer(modifier = Modifier.height(4.dp))

                        // Payment Methods
                        Text(
                            text = "طريقة الدفع والحسابات الإلكترونية:",
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Bold,
                            color = Stone900
                        )

                        walletAccounts.filter { it.isEnabled }.forEach { wallet ->
                            val isSelected = wallet.id == selectedWalletId
                            Column(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .clip(RoundedCornerShape(12.dp))
                                    .background(if (isSelected) Emerald50 else Stone100)
                                    .border(1.dp, if (isSelected) Emerald600 else Stone200, RoundedCornerShape(12.dp))
                                    .clickable { selectedWalletId = wallet.id }
                                    .padding(horizontal = 10.dp, vertical = 8.dp)
                            ) {
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    RadioButton(
                                        selected = isSelected,
                                        onClick = { selectedWalletId = wallet.id },
                                        colors = RadioButtonDefaults.colors(selectedColor = Emerald800)
                                    )
                                    Spacer(modifier = Modifier.width(6.dp))
                                    Column(modifier = Modifier.weight(1f)) {
                                        Text(
                                            text = wallet.name,
                                            fontSize = 12.sp,
                                            fontWeight = if (isSelected) FontWeight.Black else FontWeight.Bold,
                                            color = if (isSelected) Emerald950 else Stone900
                                        )
                                        if (wallet.accountNumber != "عند الاستلام") {
                                            Text(
                                                text = "رقم الحساب/المحفظة: ${wallet.accountNumber}",
                                                fontSize = 10.sp,
                                                fontWeight = FontWeight.Bold,
                                                color = Emerald900
                                            )
                                        }
                                        if (wallet.accountHolder.isNotBlank() && wallet.accountNumber != "عند الاستلام") {
                                            Text(
                                                text = "باسم: ${wallet.accountHolder}",
                                                fontSize = 9.5.sp,
                                                color = Stone500
                                            )
                                        }
                                    }
                                }

                                if (isSelected && wallet.accountNumber != "عند الاستلام") {
                                    Spacer(modifier = Modifier.height(4.dp))
                                    Box(
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .clip(RoundedCornerShape(8.dp))
                                            .background(White)
                                            .border(1.dp, Emerald300, RoundedCornerShape(8.dp))
                                            .padding(horizontal = 10.dp, vertical = 6.dp)
                                    ) {
                                        Column {
                                            Text(
                                                text = "📌 تفاصيل التحويل: حول إلى ${wallet.name} برقم: ${wallet.accountNumber}",
                                                fontSize = 10.sp,
                                                fontWeight = FontWeight.Bold,
                                                color = Emerald900
                                            )
                                            if (wallet.note.isNotBlank()) {
                                                Text(
                                                    text = wallet.note,
                                                    fontSize = 9.sp,
                                                    color = Stone500
                                                )
                                            }
                                        }
                                    }
                                }
                            }
                        }

                        if (validationError != null) {
                            Text(
                                text = validationError ?: "",
                                fontSize = 11.sp,
                                color = Red500,
                                fontWeight = FontWeight.Bold
                            )
                        }

                        Spacer(modifier = Modifier.height(6.dp))

                        // Submit via WhatsApp Button
                        Button(
                            onClick = {
                                if (customerName.isBlank()) {
                                    validationError = "يرجى كتابة الاسم الكريم للمتابعة"
                                    return@Button
                                }
                                if (customerPhone.isBlank()) {
                                    validationError = "يرجى كتابة رقم الهاتف"
                                    return@Button
                                }
                                if (deliveryAddress.isBlank()) {
                                    validationError = "يرجى تحديد عنوان التوصيل"
                                    return@Button
                                }

                                val chosenWallet = walletAccounts.find { it.id == selectedWalletId }
                                val paymentName = chosenWallet?.name ?: "الدفع نقداً عند الاستلام"
                                val walletDetails = if (chosenWallet != null && chosenWallet.accountNumber != "عند الاستلام") {
                                    "${chosenWallet.name} - رقم: ${chosenWallet.accountNumber} (باسم: ${chosenWallet.accountHolder})"
                                } else null

                                val order = onSubmitOrder(
                                    customerName,
                                    customerPhone,
                                    deliveryAddress,
                                    deliveryNotes,
                                    paymentName,
                                    walletDetails
                                )

                                if (order != null) {
                                    // Open WhatsApp with formatted order message
                                    val message = order.generateWhatsAppMessage()
                                    val url = "https://wa.me/$storeWhatsApp?text=${Uri.encode(message)}"
                                    val intent = Intent(Intent.ACTION_VIEW, Uri.parse(url))
                                    context.startActivity(intent)
                                }
                            },
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(50.dp)
                                .testTag("submit_order_button"),
                            shape = RoundedCornerShape(14.dp),
                            colors = ButtonDefaults.buttonColors(containerColor = Emerald800)
                        ) {
                            Icon(
                                imageVector = Icons.Default.Send,
                                contentDescription = null,
                                tint = White,
                                modifier = Modifier.size(16.dp)
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(
                                text = "إرسال وتأكيد الطلب عبر واتساب (${totalYer.toInt()} ر.ي)",
                                fontSize = 12.5.sp,
                                fontWeight = FontWeight.Black,
                                color = White
                            )
                        }
                    }
                }
            }
        }

        item {
            Spacer(modifier = Modifier.height(80.dp))
        }
    }
}
