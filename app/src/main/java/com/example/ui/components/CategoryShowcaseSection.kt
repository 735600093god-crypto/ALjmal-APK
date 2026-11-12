package com.example.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
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
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Star
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.AsyncImage
import coil.request.ImageRequest
import com.example.data.model.CategoryCustomization
import com.example.data.model.ProductCategory
import com.example.ui.theme.Amber400
import com.example.ui.theme.Amber950
import com.example.ui.theme.Emerald100
import com.example.ui.theme.Emerald50
import com.example.ui.theme.Emerald700
import com.example.ui.theme.Emerald800
import com.example.ui.theme.Emerald900
import com.example.ui.theme.Emerald950
import com.example.ui.theme.Stone100
import com.example.ui.theme.Stone200
import com.example.ui.theme.Stone500
import com.example.ui.theme.Stone900
import com.example.ui.theme.Stone950
import com.example.ui.theme.White

@Composable
fun CategoryShowcaseSection(
    selectedCategory: ProductCategory?,
    categoryCustomizations: Map<String, CategoryCustomization>,
    onCategorySelected: (ProductCategory?) -> Unit,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier.fillMaxWidth(),
        shape = RoundedCornerShape(22.dp),
        colors = CardDefaults.cardColors(containerColor = White),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(12.dp)
        ) {
            // Header
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 4.dp, vertical = 2.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Box(
                        modifier = Modifier
                            .size(26.dp)
                            .clip(CircleShape)
                            .background(Emerald800),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = "٥",
                            color = White,
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Black
                        )
                    }
                    Spacer(modifier = Modifier.width(8.dp))
                    Column {
                        Text(
                            text = "أقسام المتجر الخمسة الرئيسية",
                            fontSize = 13.5.sp,
                            fontWeight = FontWeight.Black,
                            color = Stone900
                        )
                        Text(
                            text = "صور مخصصة طازجة ومختارة بعناية",
                            fontSize = 9.5.sp,
                            color = Stone500
                        )
                    }
                }

                if (selectedCategory != null) {
                    Box(
                        modifier = Modifier
                            .clip(RoundedCornerShape(8.dp))
                            .background(Emerald50)
                            .border(1.dp, Emerald700.copy(alpha = 0.4f), RoundedCornerShape(8.dp))
                            .clickable { onCategorySelected(null) }
                            .padding(horizontal = 8.dp, vertical = 4.dp)
                    ) {
                        Text(
                            text = "عرض الكل",
                            color = Emerald800,
                            fontSize = 10.5.sp,
                            fontWeight = FontWeight.Bold
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(10.dp))

            // Horizontal Visual Cards with Photos
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .horizontalScroll(rememberScrollState()),
                horizontalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                // "جميع الأقسام" Card
                val isAllSelected = selectedCategory == null
                Box(
                    modifier = Modifier
                        .width(115.dp)
                        .height(130.dp)
                        .clip(RoundedCornerShape(16.dp))
                        .background(if (isAllSelected) Emerald900 else Stone100)
                        .border(
                            width = if (isAllSelected) 2.dp else 1.dp,
                            color = if (isAllSelected) Amber400 else Stone200,
                            shape = RoundedCornerShape(16.dp)
                        )
                        .clickable { onCategorySelected(null) }
                        .padding(10.dp)
                        .testTag("cat_card_all"),
                    contentAlignment = Alignment.Center
                ) {
                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.Center
                    ) {
                        Box(
                            modifier = Modifier
                                .size(40.dp)
                                .clip(CircleShape)
                                .background(if (isAllSelected) Amber400 else Emerald50),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                imageVector = Icons.Default.Star,
                                contentDescription = null,
                                tint = if (isAllSelected) Stone950 else Emerald800,
                                modifier = Modifier.size(22.dp)
                            )
                        }

                        Spacer(modifier = Modifier.height(10.dp))

                        Text(
                            text = "جميع الأقسام",
                            color = if (isAllSelected) White else Stone900,
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Black
                        )

                        Spacer(modifier = Modifier.height(2.dp))

                        Text(
                            text = "كامل الأصناف",
                            color = if (isAllSelected) Amber400 else Stone500,
                            fontSize = 9.sp,
                            fontWeight = FontWeight.Medium
                        )
                    }
                }

                // The 5 Main Categories as Visual Photo Cards
                ProductCategory.values().forEach { category ->
                    val isSelected = selectedCategory == category
                    val custom = categoryCustomizations[category.id]
                    val title = custom?.customTitle ?: category.title
                    val imageUrl = custom?.customImageUrl ?: category.defaultImageUrl

                    Box(
                        modifier = Modifier
                            .width(135.dp)
                            .height(130.dp)
                            .clip(RoundedCornerShape(16.dp))
                            .background(Stone200)
                            .border(
                                width = if (isSelected) 2.5.dp else 1.dp,
                                color = if (isSelected) Amber400 else Color.Transparent,
                                shape = RoundedCornerShape(16.dp)
                            )
                            .clickable { onCategorySelected(category) }
                            .testTag("cat_card_${category.id}")
                    ) {
                        // Category Photo (loaded from gallery URI or default URL)
                        AsyncImage(
                            model = ImageRequest.Builder(LocalContext.current)
                                .data(imageUrl)
                                .crossfade(true)
                                .build(),
                            contentDescription = title,
                            contentScale = ContentScale.Crop,
                            modifier = Modifier.fillMaxSize()
                        )

                        // Dark Gradient Overlay for text readability
                        Box(
                            modifier = Modifier
                                .fillMaxSize()
                                .background(
                                    Brush.verticalGradient(
                                        colors = listOf(
                                            Color.Black.copy(alpha = 0.2f),
                                            Color.Black.copy(alpha = 0.4f),
                                            Color.Black.copy(alpha = 0.88f)
                                        )
                                    )
                                )
                        )

                        // Top Badges Row
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(8.dp),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Box(
                                modifier = Modifier
                                    .clip(RoundedCornerShape(6.dp))
                                    .background(Amber400)
                                    .padding(horizontal = 5.dp, vertical = 2.dp)
                            ) {
                                Text(
                                    text = category.badgeLabel,
                                    color = Stone950,
                                    fontSize = 8.5.sp,
                                    fontWeight = FontWeight.Black
                                )
                            }

                            if (isSelected) {
                                Box(
                                    modifier = Modifier
                                        .size(20.dp)
                                        .clip(CircleShape)
                                        .background(Amber400),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Icon(
                                        imageVector = Icons.Default.Check,
                                        contentDescription = "محدد",
                                        tint = Stone950,
                                        modifier = Modifier.size(14.dp)
                                    )
                                }
                            }
                        }

                        // Bottom Title & Info
                        Column(
                            modifier = Modifier
                                .align(Alignment.BottomStart)
                                .padding(8.dp)
                        ) {
                            Text(
                                text = title,
                                color = White,
                                fontSize = 11.5.sp,
                                fontWeight = FontWeight.Black,
                                maxLines = 1
                            )
                            Text(
                                text = when (category) {
                                    ProductCategory.LOCAL_FRUITS -> "قطاف بلدي صعدة"
                                    ProductCategory.IMPORTED_FRUITS -> "مستورد نخب أول"
                                    ProductCategory.VEGETABLES -> "خضار طازجة فجراً"
                                    ProductCategory.PREMIUM_DATES -> "تمور فاخرة ملكية"
                                    ProductCategory.DRIED_FRUITS -> "مجفف طبيعي 100%"
                                },
                                color = Amber400,
                                fontSize = 8.5.sp,
                                fontWeight = FontWeight.Bold,
                                maxLines = 1
                            )
                        }
                    }
                }
            }
        }
    }
}
