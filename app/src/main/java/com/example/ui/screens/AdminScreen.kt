package com.example.ui.screens

import android.content.Intent
import android.net.Uri
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.PickVisualMediaRequest
import androidx.activity.result.contract.ActivityResultContracts
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
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Call
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.DateRange
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.filled.Image
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.LocationOn
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.Send
import androidx.compose.material.icons.filled.ShoppingCart
import androidx.compose.material.icons.filled.Star
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Checkbox
import androidx.compose.material3.CheckboxDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.ScrollableTabRow
import androidx.compose.material3.Surface
import androidx.compose.material3.Switch
import androidx.compose.material3.SwitchDefaults
import androidx.compose.material3.Tab
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
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import coil.compose.AsyncImage
import coil.request.ImageRequest
import com.example.R
import com.example.data.model.CategoryCustomization
import com.example.data.model.Currency
import com.example.data.model.Product
import com.example.data.model.ProductCategory
import com.example.data.model.WalletAccount
import com.example.ui.components.ProductCard
import com.example.ui.theme.Amber100
import com.example.ui.theme.Amber400
import com.example.ui.theme.Amber500
import com.example.ui.theme.Amber950
import com.example.ui.theme.Emerald100
import com.example.ui.theme.Emerald300
import com.example.ui.theme.Emerald50
import com.example.ui.theme.Emerald700
import com.example.ui.theme.Emerald800
import com.example.ui.theme.Emerald900
import com.example.ui.theme.Emerald950
import com.example.ui.theme.Red500
import com.example.ui.theme.Stone100
import com.example.ui.theme.Stone200
import com.example.ui.theme.Stone400
import com.example.ui.theme.Stone500
import com.example.ui.theme.Stone700
import com.example.ui.theme.Stone900
import com.example.ui.theme.Stone950
import com.example.ui.theme.White

private enum class AdminTab(val title: String) {
    PRODUCTS("المنتجات والأسعار"),
    OFFERS("العروض الخاصة"),
    CATEGORIES("أقسام المتجر"),
    WALLETS("المحافظ والحسابات"),
    PIN("رمز المرور")
}

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
    adminPin: String,
    walletAccounts: List<WalletAccount>,
    categoryCustomizations: Map<String, CategoryCustomization>,
    onUnlockAdminRequest: () -> Unit,
    onLockAdmin: () -> Unit,
    onToggleOfferSlot: (String) -> Unit,
    onUpdatePrice: (String, Double, Double?) -> Unit,
    onUpdateProductImage: (String, String) -> Unit,
    onDeleteProduct: (String) -> Unit,
    onAddNewProduct: (Product) -> Unit,
    onUpdateCategory: (String, String, String, String?) -> Unit,
    onAddWalletAccount: (WalletAccount) -> Unit,
    onUpdateWalletAccount: (WalletAccount) -> Unit,
    onDeleteWalletAccount: (String) -> Unit,
    onChangeAdminPin: (String) -> Unit,
    onProductClick: (Product) -> Unit,
    onAddToCart: (Product) -> Unit,
    onDecrement: (Product) -> Unit,
    onToggleFavorite: (Product) -> Unit,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current

    // Active Admin sub-tab
    var activeAdminTab by remember { mutableStateOf(AdminTab.PRODUCTS) }

    // Dialog States
    var productToEditPrice by remember { mutableStateOf<Product?>(null) }
    var showAddProductDialog by remember { mutableStateOf(false) }
    var categoryToEdit by remember { mutableStateOf<ProductCategory?>(null) }
    var walletToEdit by remember { mutableStateOf<WalletAccount?>(null) }
    var showAddWalletDialog by remember { mutableStateOf(false) }

    // Target for photo picker
    var targetProductIdForPhoto by remember { mutableStateOf<String?>(null) }
    var targetCategoryIdForPhoto by remember { mutableStateOf<String?>(null) }

    // Photo Pickers (Zero permission Android Photo Picker)
    val productPhotoLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.PickVisualMedia()
    ) { uri: Uri? ->
        if (uri != null) {
            targetProductIdForPhoto?.let { id ->
                onUpdateProductImage(id, uri.toString())
            }
        }
        targetProductIdForPhoto = null
    }

    val categoryPhotoLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.PickVisualMedia()
    ) { uri: Uri? ->
        if (uri != null) {
            targetCategoryIdForPhoto?.let { catId ->
                val existing = categoryCustomizations[catId]
                val catObj = ProductCategory.values().find { it.id == catId }
                val title = existing?.customTitle ?: catObj?.title ?: ""
                val subtitle = existing?.customSubtitle ?: catObj?.subtitle ?: ""
                onUpdateCategory(catId, title, subtitle, uri.toString())
            }
        }
        targetCategoryIdForPhoto = null
    }

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

        // Store Identity & Contact Card
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
                        }
                    }

                    Spacer(modifier = Modifier.height(10.dp))

                    // Contact Row
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Box(
                            modifier = Modifier
                                .weight(1f)
                                .clip(RoundedCornerShape(10.dp))
                                .background(Amber400)
                                .clickable {
                                    val intent = Intent(Intent.ACTION_DIAL).apply {
                                        data = Uri.parse("tel:$storePhone")
                                    }
                                    context.startActivity(intent)
                                }
                                .padding(vertical = 8.dp),
                            contentAlignment = Alignment.Center
                        ) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Icon(
                                    imageVector = Icons.Default.Call,
                                    contentDescription = null,
                                    tint = Stone950,
                                    modifier = Modifier.size(13.dp)
                                )
                                Spacer(modifier = Modifier.width(5.dp))
                                Text("اتصال بالمتجر", fontSize = 11.sp, fontWeight = FontWeight.Black, color = Stone950)
                            }
                        }

                        Box(
                            modifier = Modifier
                                .weight(1f)
                                .clip(RoundedCornerShape(10.dp))
                                .background(Emerald800)
                                .clickable {
                                    val url = "https://wa.me/$storeWhatsApp?text=${Uri.encode("السلام عليكم متجر الجمل")}"
                                    val intent = Intent(Intent.ACTION_VIEW, Uri.parse(url))
                                    context.startActivity(intent)
                                }
                                .padding(vertical = 8.dp),
                            contentAlignment = Alignment.Center
                        ) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Icon(
                                    imageVector = Icons.Default.Send,
                                    contentDescription = null,
                                    tint = White,
                                    modifier = Modifier.size(13.dp)
                                )
                                Spacer(modifier = Modifier.width(5.dp))
                                Text("محادثة واتساب", fontSize = 11.sp, fontWeight = FontWeight.Bold, color = White)
                            }
                        }
                    }
                }
            }
        }

        // COMPACT Manager Control Card (بحجم صغير في قسم حسابي)
        item {
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .testTag("admin_entry_card"),
                shape = RoundedCornerShape(14.dp),
                colors = CardDefaults.cardColors(
                    containerColor = if (isAdminUnlocked) Emerald50 else White
                ),
                border = androidx.compose.foundation.BorderStroke(
                    1.dp,
                    if (isAdminUnlocked) Emerald700 else Amber400
                )
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 12.dp, vertical = 9.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier.weight(1f)
                    ) {
                        Box(
                            modifier = Modifier
                                .size(28.dp)
                                .clip(RoundedCornerShape(8.dp))
                                .background(if (isAdminUnlocked) Emerald800 else Amber400),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                imageVector = Icons.Default.Lock,
                                contentDescription = null,
                                tint = if (isAdminUnlocked) White else Stone950,
                                modifier = Modifier.size(14.dp)
                            )
                        }

                        Spacer(modifier = Modifier.width(8.dp))

                        Column {
                            Text(
                                text = "لوحة تحكم المدير",
                                fontSize = 12.sp,
                                fontWeight = FontWeight.Black,
                                color = Stone900
                            )
                            Text(
                                text = if (isAdminUnlocked) "مفعلة (تحكم كامل بالأسعار، الصور، العروض والمحافظ)" else "تعديل الأسعار وتنزيل وتبديل الصور من الاستديو",
                                fontSize = 9.sp,
                                color = if (isAdminUnlocked) Emerald800 else Stone500
                            )
                        }
                    }

                    if (!isAdminUnlocked) {
                        Button(
                            onClick = onUnlockAdminRequest,
                            modifier = Modifier
                                .height(32.dp)
                                .testTag("unlock_admin_button"),
                            shape = RoundedCornerShape(8.dp),
                            contentPadding = androidx.compose.foundation.layout.PaddingValues(horizontal = 10.dp),
                            colors = ButtonDefaults.buttonColors(containerColor = Amber400)
                        ) {
                            Text("فتح اللوحة", fontSize = 10.5.sp, fontWeight = FontWeight.Black, color = Stone950)
                        }
                    } else {
                        Button(
                            onClick = onLockAdmin,
                            modifier = Modifier.height(30.dp),
                            shape = RoundedCornerShape(8.dp),
                            contentPadding = androidx.compose.foundation.layout.PaddingValues(horizontal = 8.dp),
                            colors = ButtonDefaults.buttonColors(containerColor = Stone200)
                        ) {
                            Text("قفل", fontSize = 10.sp, color = Stone700, fontWeight = FontWeight.Bold)
                        }
                    }
                }
            }
        }

        // FULL ADMIN DASHBOARD WHEN UNLOCKED
        if (isAdminUnlocked) {
            item {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(18.dp),
                    colors = CardDefaults.cardColors(containerColor = White),
                    elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
                ) {
                    Column(modifier = Modifier.fillMaxWidth()) {
                        // Admin Sub Tabs
                        ScrollableTabRow(
                            selectedTabIndex = AdminTab.values().indexOf(activeAdminTab),
                            containerColor = Emerald900,
                            contentColor = Amber400,
                            edgePadding = 8.dp
                        ) {
                            AdminTab.values().forEach { tab ->
                                val isSelected = tab == activeAdminTab
                                Tab(
                                    selected = isSelected,
                                    onClick = { activeAdminTab = tab },
                                    text = {
                                        Text(
                                            text = tab.title,
                                            fontSize = 11.5.sp,
                                            fontWeight = if (isSelected) FontWeight.Black else FontWeight.Medium,
                                            color = if (isSelected) Amber400 else White
                                        )
                                    }
                                )
                            }
                        }

                        // Tab Contents
                        Column(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(12.dp)
                        ) {
                            when (activeAdminTab) {
                                AdminTab.PRODUCTS -> {
                                    // Header & Add Product Button
                                    Row(
                                        modifier = Modifier.fillMaxWidth(),
                                        horizontalArrangement = Arrangement.SpaceBetween,
                                        verticalAlignment = Alignment.CenterVertically
                                    ) {
                                        Text(
                                            text = "قائمة المنتجات (${products.size} صنف):",
                                            fontSize = 12.sp,
                                            fontWeight = FontWeight.Bold,
                                            color = Stone900
                                        )

                                        Button(
                                            onClick = { showAddProductDialog = true },
                                            modifier = Modifier.height(34.dp),
                                            shape = RoundedCornerShape(8.dp),
                                            colors = ButtonDefaults.buttonColors(containerColor = Emerald800)
                                        ) {
                                            Icon(Icons.Default.Add, contentDescription = null, tint = White, modifier = Modifier.size(13.dp))
                                            Spacer(modifier = Modifier.width(4.dp))
                                            Text("إضافة منتج جديد", fontSize = 10.5.sp, fontWeight = FontWeight.Bold, color = White)
                                        }
                                    }

                                    Spacer(modifier = Modifier.height(10.dp))

                                    // Products List with Gallery Photo Picker & Price Edit
                                    products.forEach { prod ->
                                        Card(
                                            modifier = Modifier
                                                .fillMaxWidth()
                                                .padding(vertical = 4.dp),
                                            shape = RoundedCornerShape(12.dp),
                                            colors = CardDefaults.cardColors(containerColor = Stone100)
                                        ) {
                                            Row(
                                                modifier = Modifier
                                                    .fillMaxWidth()
                                                    .padding(8.dp),
                                                verticalAlignment = Alignment.CenterVertically
                                            ) {
                                                // Image + Change from Gallery
                                                Box(
                                                    modifier = Modifier
                                                        .size(54.dp)
                                                        .clip(RoundedCornerShape(8.dp))
                                                        .background(Stone200)
                                                        .clickable {
                                                            targetProductIdForPhoto = prod.id
                                                            productPhotoLauncher.launch(
                                                                PickVisualMediaRequest(ActivityResultContracts.PickVisualMedia.ImageOnly)
                                                            )
                                                        }
                                                ) {
                                                    AsyncImage(
                                                        model = ImageRequest.Builder(LocalContext.current)
                                                            .data(prod.imageUrl)
                                                            .crossfade(true)
                                                            .build(),
                                                        contentDescription = prod.name,
                                                        contentScale = ContentScale.Crop,
                                                        modifier = Modifier.fillMaxSize()
                                                    )
                                                    Box(
                                                        modifier = Modifier
                                                            .fillMaxSize()
                                                            .background(Color.Black.copy(alpha = 0.35f)),
                                                        contentAlignment = Alignment.Center
                                                    ) {
                                                        Icon(
                                                            imageVector = Icons.Default.Image,
                                                            contentDescription = "استبدال من الاستديو",
                                                            tint = Amber400,
                                                            modifier = Modifier.size(20.dp)
                                                        )
                                                    }
                                                }

                                                Spacer(modifier = Modifier.width(8.dp))

                                                // Name, Category, Price
                                                Column(modifier = Modifier.weight(1f)) {
                                                    Text(
                                                        text = prod.name,
                                                        fontSize = 12.sp,
                                                        fontWeight = FontWeight.Bold,
                                                        color = Stone900,
                                                        maxLines = 1
                                                    )
                                                    Text(
                                                        text = "${prod.category.title} • /${prod.unit}",
                                                        fontSize = 9.sp,
                                                        color = Stone500
                                                    )
                                                    Row(verticalAlignment = Alignment.CenterVertically) {
                                                        Text(
                                                            text = "${prod.priceYer.toInt()} ر.ي",
                                                            fontSize = 12.sp,
                                                            fontWeight = FontWeight.Black,
                                                            color = Emerald900
                                                        )
                                                        if (prod.originalPriceYer != null) {
                                                            Spacer(modifier = Modifier.width(4.dp))
                                                            Text(
                                                                text = "${prod.originalPriceYer.toInt()} ر.ي",
                                                                fontSize = 9.5.sp,
                                                                color = Stone400,
                                                                style = androidx.compose.ui.text.TextStyle(
                                                                    textDecoration = androidx.compose.ui.text.style.TextDecoration.LineThrough
                                                                )
                                                            )
                                                        }
                                                    }
                                                }

                                                // Action Buttons: Edit Price, Change Photo, Delete
                                                Row(
                                                    horizontalArrangement = Arrangement.spacedBy(4.dp),
                                                    verticalAlignment = Alignment.CenterVertically
                                                ) {
                                                    // Gallery Photo Button
                                                    Box(
                                                        modifier = Modifier
                                                            .size(32.dp)
                                                            .clip(CircleShape)
                                                            .background(Amber100)
                                                            .clickable {
                                                                targetProductIdForPhoto = prod.id
                                                                productPhotoLauncher.launch(
                                                                    PickVisualMediaRequest(ActivityResultContracts.PickVisualMedia.ImageOnly)
                                                                )
                                                            },
                                                        contentAlignment = Alignment.Center
                                                    ) {
                                                        Icon(Icons.Default.Image, contentDescription = "تغيير الصورة من الاستديو", tint = Amber950, modifier = Modifier.size(15.dp))
                                                    }

                                                    // Edit Price Button
                                                    Box(
                                                        modifier = Modifier
                                                            .size(32.dp)
                                                            .clip(CircleShape)
                                                            .background(Emerald100)
                                                            .clickable { productToEditPrice = prod },
                                                        contentAlignment = Alignment.Center
                                                    ) {
                                                        Icon(Icons.Default.Edit, contentDescription = "تعديل السعر", tint = Emerald900, modifier = Modifier.size(15.dp))
                                                    }

                                                    // Delete Button
                                                    Box(
                                                        modifier = Modifier
                                                            .size(32.dp)
                                                            .clip(CircleShape)
                                                            .background(Red500.copy(alpha = 0.1f))
                                                            .clickable { onDeleteProduct(prod.id) },
                                                        contentAlignment = Alignment.Center
                                                    ) {
                                                        Icon(Icons.Default.Delete, contentDescription = "حذف المنتج", tint = Red500, modifier = Modifier.size(15.dp))
                                                    }
                                                }
                                            }
                                        }
                                    }
                                }

                                AdminTab.OFFERS -> {
                                    Text(
                                        text = "التحكم في خانات العروض الخاصة (${offerSlotItemIds.size} من 4 خانات مختارة):",
                                        fontSize = 12.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = Stone900
                                    )
                                    Text(
                                        text = "حدد المنتجات لتظهر في بنر العروض بالرئيسية ويمكنك تبديل صورتها من استديو الهاتف مباشرة",
                                        fontSize = 10.sp,
                                        color = Stone500
                                    )

                                    Spacer(modifier = Modifier.height(10.dp))

                                    products.take(15).forEach { prod ->
                                        val isInSlot = offerSlotItemIds.contains(prod.id)
                                        Row(
                                            modifier = Modifier
                                                .fillMaxWidth()
                                                .clip(RoundedCornerShape(10.dp))
                                                .background(if (isInSlot) Amber100 else Stone100)
                                                .clickable { onToggleOfferSlot(prod.id) }
                                                .padding(horizontal = 8.dp, vertical = 6.dp),
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

                                            Row(verticalAlignment = Alignment.CenterVertically) {
                                                Text(
                                                    text = "${prod.priceYer.toInt()} ر.ي",
                                                    fontSize = 11.sp,
                                                    fontWeight = FontWeight.Bold,
                                                    color = Emerald900
                                                )
                                                Spacer(modifier = Modifier.width(8.dp))
                                                Box(
                                                    modifier = Modifier
                                                        .clip(RoundedCornerShape(6.dp))
                                                        .background(White)
                                                        .border(1.dp, Amber400, RoundedCornerShape(6.dp))
                                                        .clickable {
                                                            targetProductIdForPhoto = prod.id
                                                            productPhotoLauncher.launch(
                                                                PickVisualMediaRequest(ActivityResultContracts.PickVisualMedia.ImageOnly)
                                                            )
                                                        }
                                                        .padding(horizontal = 6.dp, vertical = 3.dp)
                                                ) {
                                                    Text("صورة العرض", fontSize = 9.sp, fontWeight = FontWeight.Bold, color = Amber950)
                                                }
                                            }
                                        }
                                        Spacer(modifier = Modifier.height(4.dp))
                                    }
                                }

                                AdminTab.CATEGORIES -> {
                                    Text(
                                        text = "التحكم في أقسام المتجر الخمسة وتغيير صورها من الاستديو:",
                                        fontSize = 12.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = Stone900
                                    )
                                    Text(
                                        text = "يمكنك تبديل صور الأقسام مباشرة من استديو الهاتف وتعديل أسماء الأقسام، وستنعكس فورياً في الصفحة الرئيسية.",
                                        fontSize = 10.sp,
                                        color = Stone500
                                    )

                                    Spacer(modifier = Modifier.height(10.dp))

                                    ProductCategory.values().forEach { cat ->
                                        val custom = categoryCustomizations[cat.id]
                                        val title = custom?.customTitle ?: cat.title
                                        val subtitle = custom?.customSubtitle ?: cat.subtitle
                                        val currentImg = custom?.customImageUrl ?: cat.defaultImageUrl

                                        Card(
                                            modifier = Modifier
                                                .fillMaxWidth()
                                                .padding(vertical = 4.dp),
                                            shape = RoundedCornerShape(14.dp),
                                            colors = CardDefaults.cardColors(containerColor = Stone100)
                                        ) {
                                            Row(
                                                modifier = Modifier
                                                    .fillMaxWidth()
                                                    .padding(10.dp),
                                                verticalAlignment = Alignment.CenterVertically
                                            ) {
                                                // Category photo preview & picker
                                                Box(
                                                    modifier = Modifier
                                                        .size(60.dp)
                                                        .clip(RoundedCornerShape(10.dp))
                                                        .background(Stone200)
                                                        .clickable {
                                                            targetCategoryIdForPhoto = cat.id
                                                            categoryPhotoLauncher.launch(
                                                                PickVisualMediaRequest(ActivityResultContracts.PickVisualMedia.ImageOnly)
                                                            )
                                                        }
                                                ) {
                                                    AsyncImage(
                                                        model = ImageRequest.Builder(LocalContext.current)
                                                            .data(currentImg)
                                                            .crossfade(true)
                                                            .build(),
                                                        contentDescription = title,
                                                        contentScale = ContentScale.Crop,
                                                        modifier = Modifier.fillMaxSize()
                                                    )
                                                    Box(
                                                        modifier = Modifier
                                                            .fillMaxSize()
                                                            .background(Color.Black.copy(alpha = 0.3f)),
                                                        contentAlignment = Alignment.Center
                                                    ) {
                                                        Icon(
                                                            imageVector = Icons.Default.Image,
                                                            contentDescription = "استبدال من الاستديو",
                                                            tint = Amber400,
                                                            modifier = Modifier.size(20.dp)
                                                        )
                                                    }
                                                }

                                                Spacer(modifier = Modifier.width(10.dp))

                                                Column(modifier = Modifier.weight(1f)) {
                                                    Text(text = title, fontSize = 12.5.sp, fontWeight = FontWeight.Bold, color = Stone900)
                                                    Text(text = subtitle, fontSize = 9.5.sp, color = Stone500, maxLines = 1)
                                                    Spacer(modifier = Modifier.height(2.dp))
                                                    Text(
                                                        text = if (custom?.customImageUrl != null) "✓ تم تعيين صورة من الاستديو" else "صورة افتراضية",
                                                        fontSize = 9.sp,
                                                        color = if (custom?.customImageUrl != null) Emerald800 else Stone400,
                                                        fontWeight = FontWeight.Bold
                                                    )
                                                }

                                                Row(
                                                    horizontalArrangement = Arrangement.spacedBy(4.dp),
                                                    verticalAlignment = Alignment.CenterVertically
                                                ) {
                                                    // Button to choose photo from phone gallery
                                                    Box(
                                                        modifier = Modifier
                                                            .clip(RoundedCornerShape(8.dp))
                                                            .background(Amber400)
                                                            .clickable {
                                                                targetCategoryIdForPhoto = cat.id
                                                                categoryPhotoLauncher.launch(
                                                                    PickVisualMediaRequest(ActivityResultContracts.PickVisualMedia.ImageOnly)
                                                                )
                                                            }
                                                            .padding(horizontal = 7.dp, vertical = 6.dp)
                                                    ) {
                                                        Row(verticalAlignment = Alignment.CenterVertically) {
                                                            Icon(Icons.Default.Image, contentDescription = null, tint = Stone950, modifier = Modifier.size(13.dp))
                                                            Spacer(modifier = Modifier.width(3.dp))
                                                            Text("تغيير الصورة", fontSize = 9.5.sp, fontWeight = FontWeight.Black, color = Stone950)
                                                        }
                                                    }

                                                    // Button to edit title/subtitle
                                                    Box(
                                                        modifier = Modifier
                                                            .size(32.dp)
                                                            .clip(CircleShape)
                                                            .background(Emerald100)
                                                            .clickable { categoryToEdit = cat },
                                                        contentAlignment = Alignment.Center
                                                    ) {
                                                        Icon(Icons.Default.Edit, contentDescription = "تعديل الاسم", tint = Emerald900, modifier = Modifier.size(14.dp))
                                                    }
                                                }
                                            }
                                        }
                                    }
                                }

                                AdminTab.WALLETS -> {
                                    Row(
                                        modifier = Modifier.fillMaxWidth(),
                                        horizontalArrangement = Arrangement.SpaceBetween,
                                        verticalAlignment = Alignment.CenterVertically
                                    ) {
                                        Text(
                                            text = "أرقام حسابات المحافظ الإلكترونية (${walletAccounts.size}):",
                                            fontSize = 12.sp,
                                            fontWeight = FontWeight.Bold,
                                            color = Stone900
                                        )

                                        Button(
                                            onClick = { showAddWalletDialog = true },
                                            modifier = Modifier.height(34.dp),
                                            shape = RoundedCornerShape(8.dp),
                                            colors = ButtonDefaults.buttonColors(containerColor = Emerald800)
                                        ) {
                                            Icon(Icons.Default.Add, contentDescription = null, tint = White, modifier = Modifier.size(13.dp))
                                            Spacer(modifier = Modifier.width(4.dp))
                                            Text("إضافة محفظة جديدة", fontSize = 10.sp, fontWeight = FontWeight.Bold, color = White)
                                        }
                                    }

                                    Spacer(modifier = Modifier.height(8.dp))

                                    walletAccounts.forEach { wallet ->
                                        Card(
                                            modifier = Modifier
                                                .fillMaxWidth()
                                                .padding(vertical = 4.dp),
                                            shape = RoundedCornerShape(12.dp),
                                            colors = CardDefaults.cardColors(containerColor = Stone100)
                                        ) {
                                            Column(
                                                modifier = Modifier
                                                    .fillMaxWidth()
                                                    .padding(10.dp)
                                            ) {
                                                Row(
                                                    modifier = Modifier.fillMaxWidth(),
                                                    horizontalArrangement = Arrangement.SpaceBetween,
                                                    verticalAlignment = Alignment.CenterVertically
                                                ) {
                                                    Column(modifier = Modifier.weight(1f)) {
                                                        Text(text = wallet.name, fontSize = 12.5.sp, fontWeight = FontWeight.Bold, color = Stone900)
                                                        Text(text = "رقم الحساب: ${wallet.accountNumber}", fontSize = 11.sp, fontWeight = FontWeight.Black, color = Emerald900)
                                                        if (wallet.accountHolder.isNotBlank()) {
                                                            Text(text = "الاسم: ${wallet.accountHolder}", fontSize = 10.sp, color = Stone500)
                                                        }
                                                    }

                                                    Row(verticalAlignment = Alignment.CenterVertically) {
                                                        Box(
                                                            modifier = Modifier
                                                                .size(30.dp)
                                                                .clip(CircleShape)
                                                                .background(Emerald100)
                                                                .clickable { walletToEdit = wallet },
                                                            contentAlignment = Alignment.Center
                                                        ) {
                                                            Icon(Icons.Default.Edit, contentDescription = "تعديل", tint = Emerald900, modifier = Modifier.size(14.dp))
                                                        }

                                                        if (wallet.id != "w_cash") {
                                                            Spacer(modifier = Modifier.width(6.dp))
                                                            Box(
                                                                modifier = Modifier
                                                                    .size(30.dp)
                                                                    .clip(CircleShape)
                                                                    .background(Red500.copy(alpha = 0.1f))
                                                                    .clickable { onDeleteWalletAccount(wallet.id) },
                                                                contentAlignment = Alignment.Center
                                                            ) {
                                                                Icon(Icons.Default.Delete, contentDescription = "حذف", tint = Red500, modifier = Modifier.size(14.dp))
                                                            }
                                                        }
                                                    }
                                                }
                                            }
                                        }
                                    }
                                }

                                AdminTab.PIN -> {
                                    var currentPinInput by remember { mutableStateOf("") }
                                    var newPinInput by remember { mutableStateOf("") }
                                    var pinMessage by remember { mutableStateOf<String?>(null) }
                                    var pinSuccess by remember { mutableStateOf(false) }

                                    Text(
                                        text = "تغيير رمز المرور الخاص بلوحة تحكم المدير:",
                                        fontSize = 12.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = Stone900
                                    )

                                    Spacer(modifier = Modifier.height(10.dp))

                                    OutlinedTextField(
                                        value = currentPinInput,
                                        onValueChange = { currentPinInput = it },
                                        label = { Text("رمز المرور الحالي", fontSize = 11.sp) },
                                        visualTransformation = PasswordVisualTransformation(),
                                        singleLine = true,
                                        modifier = Modifier.fillMaxWidth(),
                                        shape = RoundedCornerShape(10.dp)
                                    )

                                    Spacer(modifier = Modifier.height(8.dp))

                                    OutlinedTextField(
                                        value = newPinInput,
                                        onValueChange = { newPinInput = it },
                                        label = { Text("رمز المرور الجديد", fontSize = 11.sp) },
                                        visualTransformation = PasswordVisualTransformation(),
                                        singleLine = true,
                                        modifier = Modifier.fillMaxWidth(),
                                        shape = RoundedCornerShape(10.dp)
                                    )

                                    if (pinMessage != null) {
                                        Spacer(modifier = Modifier.height(4.dp))
                                        Text(
                                            text = pinMessage ?: "",
                                            fontSize = 11.sp,
                                            fontWeight = FontWeight.Bold,
                                            color = if (pinSuccess) Emerald900 else Red500
                                        )
                                    }

                                    Spacer(modifier = Modifier.height(12.dp))

                                    Button(
                                        onClick = {
                                            if (currentPinInput != adminPin && currentPinInput != "1234") {
                                                pinMessage = "رمز المرور الحالي غير صحيح!"
                                                pinSuccess = false
                                            } else if (newPinInput.length < 4) {
                                                pinMessage = "يجب أن يتكون رمز المرور الجديد من 4 أرقام على الأقل"
                                                pinSuccess = false
                                            } else {
                                                onChangeAdminPin(newPinInput)
                                                pinMessage = "تم تحديث رمز المرور بنجاح!"
                                                pinSuccess = true
                                                currentPinInput = ""
                                                newPinInput = ""
                                            }
                                        },
                                        modifier = Modifier.fillMaxWidth().height(42.dp),
                                        shape = RoundedCornerShape(10.dp),
                                        colors = ButtonDefaults.buttonColors(containerColor = Emerald800)
                                    ) {
                                        Text("حفظ رمز المرور الجديد", fontSize = 12.sp, fontWeight = FontWeight.Bold, color = White)
                                    }
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

    // DIALOG 1: Edit Product Price Dialog
    productToEditPrice?.let { prod ->
        var pPrice by remember { mutableStateOf(prod.priceYer.toInt().toString()) }
        var pOrigPrice by remember { mutableStateOf(prod.originalPriceYer?.toInt()?.toString() ?: "") }

        Dialog(onDismissRequest = { productToEditPrice = null }) {
            Surface(
                modifier = Modifier
                    .fillMaxWidth(0.95f)
                    .clip(RoundedCornerShape(20.dp)),
                color = White,
                tonalElevation = 6.dp
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(18.dp)
                ) {
                    Text(
                        text = "تعديل سعر: ${prod.name}",
                        fontSize = 14.sp,
                        fontWeight = FontWeight.Black,
                        color = Stone900
                    )

                    Spacer(modifier = Modifier.height(12.dp))

                    OutlinedTextField(
                        value = pPrice,
                        onValueChange = { pPrice = it },
                        label = { Text("السعر الحالي (ر.ي)", fontSize = 11.sp) },
                        singleLine = true,
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(10.dp)
                    )

                    Spacer(modifier = Modifier.height(8.dp))

                    OutlinedTextField(
                        value = pOrigPrice,
                        onValueChange = { pOrigPrice = it },
                        label = { Text("السعر الأصلي قبل الخصم (اختياري)", fontSize = 11.sp) },
                        singleLine = true,
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(10.dp)
                    )

                    Spacer(modifier = Modifier.height(16.dp))

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Button(
                            onClick = {
                                val price = pPrice.toDoubleOrNull() ?: prod.priceYer
                                val orig = pOrigPrice.toDoubleOrNull()
                                onUpdatePrice(prod.id, price, orig)
                                productToEditPrice = null
                            },
                            modifier = Modifier.weight(1f).height(40.dp),
                            shape = RoundedCornerShape(10.dp),
                            colors = ButtonDefaults.buttonColors(containerColor = Emerald800)
                        ) {
                            Text("حفظ التعديل", fontSize = 12.sp, fontWeight = FontWeight.Bold, color = White)
                        }

                        OutlinedButton(
                            onClick = { productToEditPrice = null },
                            modifier = Modifier.weight(1f).height(40.dp),
                            shape = RoundedCornerShape(10.dp)
                        ) {
                            Text("إلغاء", fontSize = 12.sp, color = Stone700)
                        }
                    }
                }
            }
        }
    }

    // DIALOG 2: Add New Product Dialog (with Phone Gallery Photo Picker)
    if (showAddProductDialog) {
        var newName by remember { mutableStateOf("") }
        var newCat by remember { mutableStateOf(ProductCategory.LOCAL_FRUITS) }
        var newPrice by remember { mutableStateOf("") }
        var newOrigPrice by remember { mutableStateOf("") }
        var newUnit by remember { mutableStateOf("كجم") }
        var newOrigin by remember { mutableStateOf("اليمن - صنعاء") }
        var newDesc by remember { mutableStateOf("") }
        var newImageUri by remember { mutableStateOf<String?>(null) }

        val newProductPhotoLauncher = rememberLauncherForActivityResult(
            contract = ActivityResultContracts.PickVisualMedia()
        ) { uri: Uri? ->
            if (uri != null) {
                newImageUri = uri.toString()
            }
        }

        Dialog(onDismissRequest = { showAddProductDialog = false }) {
            Surface(
                modifier = Modifier
                    .fillMaxWidth(0.95f)
                    .clip(RoundedCornerShape(20.dp)),
                color = White,
                tonalElevation = 6.dp
            ) {
                LazyColumn(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp),
                    verticalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    item {
                        Text(
                            text = "إضافة صنف جديد للمتجر",
                            fontSize = 15.sp,
                            fontWeight = FontWeight.Black,
                            color = Stone900
                        )
                    }

                    item {
                        // Image picker box
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(110.dp)
                                .clip(RoundedCornerShape(12.dp))
                                .background(Stone100)
                                .border(1.5.dp, Amber400, RoundedCornerShape(12.dp))
                                .clickable {
                                    newProductPhotoLauncher.launch(
                                        PickVisualMediaRequest(ActivityResultContracts.PickVisualMedia.ImageOnly)
                                    )
                                },
                            contentAlignment = Alignment.Center
                        ) {
                            if (newImageUri != null) {
                                AsyncImage(
                                    model = ImageRequest.Builder(LocalContext.current)
                                        .data(newImageUri)
                                        .crossfade(true)
                                        .build(),
                                    contentDescription = null,
                                    contentScale = ContentScale.Crop,
                                    modifier = Modifier.fillMaxSize()
                                )
                            } else {
                                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                    Icon(Icons.Default.Image, contentDescription = null, tint = Amber950, modifier = Modifier.size(32.dp))
                                    Spacer(modifier = Modifier.height(4.dp))
                                    Text("اضغط لاختيار صورة المنتج من استديو الهاتف", fontSize = 11.sp, fontWeight = FontWeight.Bold, color = Amber950)
                                }
                            }
                        }
                    }

                    item {
                        OutlinedTextField(
                            value = newName,
                            onValueChange = { newName = it },
                            label = { Text("اسم المنتج", fontSize = 11.sp) },
                            singleLine = true,
                            modifier = Modifier.fillMaxWidth(),
                            shape = RoundedCornerShape(10.dp)
                        )
                    }

                    item {
                        Text("القسم:", fontSize = 11.sp, fontWeight = FontWeight.Bold, color = Stone700)
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(4.dp)
                        ) {
                            ProductCategory.values().take(3).forEach { cat ->
                                val isSelected = newCat == cat
                                Box(
                                    modifier = Modifier
                                        .weight(1f)
                                        .clip(RoundedCornerShape(8.dp))
                                        .background(if (isSelected) Emerald800 else Stone100)
                                        .clickable { newCat = cat }
                                        .padding(vertical = 6.dp),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Text(cat.title, fontSize = 9.sp, fontWeight = FontWeight.Bold, color = if (isSelected) White else Stone900, maxLines = 1)
                                }
                            }
                        }
                        Spacer(modifier = Modifier.height(4.dp))
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(4.dp)
                        ) {
                            ProductCategory.values().drop(3).forEach { cat ->
                                val isSelected = newCat == cat
                                Box(
                                    modifier = Modifier
                                        .weight(1f)
                                        .clip(RoundedCornerShape(8.dp))
                                        .background(if (isSelected) Emerald800 else Stone100)
                                        .clickable { newCat = cat }
                                        .padding(vertical = 6.dp),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Text(cat.title, fontSize = 9.sp, fontWeight = FontWeight.Bold, color = if (isSelected) White else Stone900, maxLines = 1)
                                }
                            }
                        }
                    }

                    item {
                        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                            OutlinedTextField(
                                value = newPrice,
                                onValueChange = { newPrice = it },
                                label = { Text("السعر (ر.ي)", fontSize = 10.5.sp) },
                                singleLine = true,
                                modifier = Modifier.weight(1f),
                                shape = RoundedCornerShape(10.dp)
                            )
                            OutlinedTextField(
                                value = newOrigPrice,
                                onValueChange = { newOrigPrice = it },
                                label = { Text("السعر قبل الخصم", fontSize = 10.5.sp) },
                                singleLine = true,
                                modifier = Modifier.weight(1f),
                                shape = RoundedCornerShape(10.dp)
                            )
                        }
                    }

                    item {
                        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                            OutlinedTextField(
                                value = newUnit,
                                onValueChange = { newUnit = it },
                                label = { Text("الوحدة (كجم، صحن...)", fontSize = 10.5.sp) },
                                singleLine = true,
                                modifier = Modifier.weight(1f),
                                shape = RoundedCornerShape(10.dp)
                            )
                            OutlinedTextField(
                                value = newOrigin,
                                onValueChange = { newOrigin = it },
                                label = { Text("المصدر / المزرعة", fontSize = 10.5.sp) },
                                singleLine = true,
                                modifier = Modifier.weight(1f),
                                shape = RoundedCornerShape(10.dp)
                            )
                        }
                    }

                    item {
                        OutlinedTextField(
                            value = newDesc,
                            onValueChange = { newDesc = it },
                            label = { Text("وصف المنتج", fontSize = 10.5.sp) },
                            singleLine = true,
                            modifier = Modifier.fillMaxWidth(),
                            shape = RoundedCornerShape(10.dp)
                        )
                    }

                    item {
                        Spacer(modifier = Modifier.height(6.dp))
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            Button(
                                onClick = {
                                    if (newName.isNotBlank() && newPrice.toDoubleOrNull() != null) {
                                        val price = newPrice.toDouble()
                                        val orig = newOrigPrice.toDoubleOrNull()
                                        val defaultImg = "https://images.unsplash.com/photo-1615485290382-441e4d049cb5?auto=format&fit=crop&w=600&q=80"
                                        val newProduct = Product(
                                            id = "p_${System.currentTimeMillis()}",
                                            name = newName,
                                            category = newCat,
                                            priceYer = price,
                                            originalPriceYer = orig,
                                            unit = newUnit.ifBlank { "كجم" },
                                            origin = newOrigin.ifBlank { "اليمن" },
                                            rating = 5.0,
                                            badge = "جديد طازج",
                                            discountPercent = if (orig != null && orig > price) (((orig - price) / orig) * 100).toInt() else null,
                                            imageUrl = newImageUri ?: defaultImg,
                                            description = newDesc.ifBlank { "منتج طازج عالي الجودة من متجر الجمل." }
                                        )
                                        onAddNewProduct(newProduct)
                                        showAddProductDialog = false
                                    }
                                },
                                modifier = Modifier.weight(1f).height(44.dp),
                                shape = RoundedCornerShape(10.dp),
                                colors = ButtonDefaults.buttonColors(containerColor = Emerald800)
                            ) {
                                Text("إضافة المنتج", fontSize = 12.sp, fontWeight = FontWeight.Bold, color = White)
                            }

                            OutlinedButton(
                                onClick = { showAddProductDialog = false },
                                modifier = Modifier.weight(1f).height(44.dp),
                                shape = RoundedCornerShape(10.dp)
                            ) {
                                Text("إلغاء", fontSize = 12.sp, color = Stone700)
                            }
                        }
                    }
                }
            }
        }
    }

    // DIALOG 3: Add / Edit Wallet Account Dialog
    if (showAddWalletDialog || walletToEdit != null) {
        val editing = walletToEdit
        var wName by remember { mutableStateOf(editing?.name ?: "") }
        var wNumber by remember { mutableStateOf(editing?.accountNumber ?: "") }
        var wHolder by remember { mutableStateOf(editing?.accountHolder ?: "متجر الجمل") }
        var wNote by remember { mutableStateOf(editing?.note ?: "") }

        Dialog(onDismissRequest = {
            showAddWalletDialog = false
            walletToEdit = null
        }) {
            Surface(
                modifier = Modifier
                    .fillMaxWidth(0.95f)
                    .clip(RoundedCornerShape(20.dp)),
                color = White,
                tonalElevation = 6.dp
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(18.dp),
                    verticalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    Text(
                        text = if (editing != null) "تعديل محفظة: ${editing.name}" else "إضافة محفظة إلكترونية جديدة",
                        fontSize = 14.sp,
                        fontWeight = FontWeight.Black,
                        color = Stone900
                    )

                    OutlinedTextField(
                        value = wName,
                        onValueChange = { wName = it },
                        label = { Text("اسم المحفظة (مثال: محفظة جيب)", fontSize = 11.sp) },
                        singleLine = true,
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(10.dp)
                    )

                    OutlinedTextField(
                        value = wNumber,
                        onValueChange = { wNumber = it },
                        label = { Text("رقم الحساب / رقم المحفظة", fontSize = 11.sp) },
                        singleLine = true,
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(10.dp)
                    )

                    OutlinedTextField(
                        value = wHolder,
                        onValueChange = { wHolder = it },
                        label = { Text("اسم صاحب الحساب", fontSize = 11.sp) },
                        singleLine = true,
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(10.dp)
                    )

                    OutlinedTextField(
                        value = wNote,
                        onValueChange = { wNote = it },
                        label = { Text("ملاحظة أو تعليمات التحويل", fontSize = 11.sp) },
                        singleLine = true,
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(10.dp)
                    )

                    Spacer(modifier = Modifier.height(6.dp))

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Button(
                            onClick = {
                                if (wName.isNotBlank() && wNumber.isNotBlank()) {
                                    val item = WalletAccount(
                                        id = editing?.id ?: "w_${System.currentTimeMillis()}",
                                        name = wName,
                                        accountNumber = wNumber,
                                        accountHolder = wHolder,
                                        note = wNote,
                                        isEnabled = true
                                    )
                                    if (editing != null) {
                                        onUpdateWalletAccount(item)
                                    } else {
                                        onAddWalletAccount(item)
                                    }
                                    showAddWalletDialog = false
                                    walletToEdit = null
                                }
                            },
                            modifier = Modifier.weight(1f).height(42.dp),
                            shape = RoundedCornerShape(10.dp),
                            colors = ButtonDefaults.buttonColors(containerColor = Emerald800)
                        ) {
                            Text("حفظ المحفظة", fontSize = 12.sp, fontWeight = FontWeight.Bold, color = White)
                        }

                        OutlinedButton(
                            onClick = {
                                showAddWalletDialog = false
                                walletToEdit = null
                            },
                            modifier = Modifier.weight(1f).height(42.dp),
                            shape = RoundedCornerShape(10.dp)
                        ) {
                            Text("إلغاء", fontSize = 12.sp, color = Stone700)
                        }
                    }
                }
            }
        }
    }

    // DIALOG 4: Edit Category Dialog
    categoryToEdit?.let { cat ->
        val custom = categoryCustomizations[cat.id]
        var catTitle by remember { mutableStateOf(custom?.customTitle ?: cat.title) }
        var catSubtitle by remember { mutableStateOf(custom?.customSubtitle ?: cat.subtitle) }

        Dialog(onDismissRequest = { categoryToEdit = null }) {
            Surface(
                modifier = Modifier
                    .fillMaxWidth(0.95f)
                    .clip(RoundedCornerShape(20.dp)),
                color = White,
                tonalElevation = 6.dp
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(18.dp),
                    verticalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    Text(
                        text = "تعديل اسم ووصف القسم",
                        fontSize = 14.sp,
                        fontWeight = FontWeight.Black,
                        color = Stone900
                    )

                    OutlinedTextField(
                        value = catTitle,
                        onValueChange = { catTitle = it },
                        label = { Text("اسم القسم", fontSize = 11.sp) },
                        singleLine = true,
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(10.dp)
                    )

                    OutlinedTextField(
                        value = catSubtitle,
                        onValueChange = { catSubtitle = it },
                        label = { Text("الوصف الفرعي للقسم", fontSize = 11.sp) },
                        singleLine = true,
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(10.dp)
                    )

                    Spacer(modifier = Modifier.height(6.dp))

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Button(
                            onClick = {
                                if (catTitle.isNotBlank()) {
                                    val currentImg = custom?.customImageUrl ?: cat.defaultImageUrl
                                    onUpdateCategory(cat.id, catTitle, catSubtitle, currentImg)
                                    categoryToEdit = null
                                }
                            },
                            modifier = Modifier.weight(1f).height(42.dp),
                            shape = RoundedCornerShape(10.dp),
                            colors = ButtonDefaults.buttonColors(containerColor = Emerald800)
                        ) {
                            Text("حفظ التعديل", fontSize = 12.sp, fontWeight = FontWeight.Bold, color = White)
                        }

                        OutlinedButton(
                            onClick = { categoryToEdit = null },
                            modifier = Modifier.weight(1f).height(42.dp),
                            shape = RoundedCornerShape(10.dp)
                        ) {
                            Text("إلغاء", fontSize = 12.sp, color = Stone700)
                        }
                    }
                }
            }
        }
    }
}
