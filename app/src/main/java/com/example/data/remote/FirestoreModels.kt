package com.example.data.remote

import com.example.data.model.CategoryCustomization
import com.example.data.model.Product
import com.example.data.model.ProductCategory
import com.example.data.model.WalletAccount
import com.google.firebase.Timestamp
import com.google.firebase.firestore.DocumentId
import com.google.firebase.firestore.FieldValue
import com.google.firebase.firestore.IgnoreExtraProperties
import com.google.firebase.firestore.ServerTimestamp

@IgnoreExtraProperties
data class StoreProductDoc(
    @DocumentId val id: String = "",
    val storeId: String = STORE_ID_MAIN,
    val ownerId: String = "",
    val name: String = "",
    val categoryId: String = ProductCategory.LOCAL_FRUITS.id,
    val priceYer: Double = 0.0,
    val originalPriceYer: Double? = null,
    val unit: String = "كجم",
    val origin: String = "",
    val rating: Double = 5.0,
    val badge: String? = null,
    val imageUrl: String = "",
    val discountPercent: Int? = null,
    val description: String = "",
    val inStock: Boolean = true,
    @ServerTimestamp val createdAt: Timestamp? = null,
    @ServerTimestamp val updatedAt: Timestamp? = null
) {
    fun toDomainProduct(): Product {
        val cat = ProductCategory.values().find { it.id == categoryId } ?: ProductCategory.LOCAL_FRUITS
        return Product(
            id = id,
            name = name,
            category = cat,
            priceYer = priceYer,
            originalPriceYer = originalPriceYer,
            unit = unit,
            origin = origin,
            rating = rating,
            badge = badge,
            imageUrl = imageUrl,
            discountPercent = discountPercent,
            description = description,
            inStock = inStock
        )
    }

    fun toWriteMap(ownerUid: String, existingCreatedAt: Timestamp?): Map<String, Any> {
        val raw = mapOf(
            "storeId" to STORE_ID_MAIN,
            "ownerId" to ownerUid,
            "name" to name,
            "categoryId" to categoryId,
            "priceYer" to priceYer,
            "originalPriceYer" to originalPriceYer,
            "unit" to unit,
            "origin" to origin,
            "rating" to rating.coerceIn(0.0, 5.0),
            "badge" to badge,
            "imageUrl" to imageUrl,
            "discountPercent" to discountPercent,
            "description" to description,
            "inStock" to inStock,
            "createdAt" to (existingCreatedAt ?: FieldValue.serverTimestamp()),
            "updatedAt" to FieldValue.serverTimestamp()
        )
        @Suppress("UNCHECKED_CAST")
        return raw.filterValues { it != null } as Map<String, Any>
    }

    companion object {
        const val STORE_ID_MAIN = "aljamal_main"

        fun fromDomain(product: Product, ownerUid: String): StoreProductDoc {
            return StoreProductDoc(
                id = product.id,
                storeId = STORE_ID_MAIN,
                ownerId = ownerUid,
                name = product.name,
                categoryId = product.category.id,
                priceYer = product.priceYer,
                originalPriceYer = product.originalPriceYer,
                unit = product.unit,
                origin = product.origin,
                rating = product.rating,
                badge = product.badge,
                imageUrl = product.imageUrl,
                discountPercent = product.discountPercent,
                description = product.description,
                inStock = product.inStock
            )
        }
    }
}

@IgnoreExtraProperties
data class StoreCategoryDoc(
    @DocumentId val id: String = "",
    val storeId: String = StoreProductDoc.STORE_ID_MAIN,
    val ownerId: String = "",
    val customTitle: String = "",
    val customSubtitle: String = "",
    val customImageUrl: String? = null,
    @ServerTimestamp val createdAt: Timestamp? = null,
    @ServerTimestamp val updatedAt: Timestamp? = null
) {
    fun toDomainCategory(): CategoryCustomization {
        return CategoryCustomization(
            categoryId = id,
            customTitle = customTitle,
            customSubtitle = customSubtitle,
            customImageUrl = customImageUrl
        )
    }

    fun toWriteMap(ownerUid: String, existingCreatedAt: Timestamp?): Map<String, Any> {
        val raw = mapOf(
            "storeId" to StoreProductDoc.STORE_ID_MAIN,
            "ownerId" to ownerUid,
            "customTitle" to customTitle,
            "customSubtitle" to customSubtitle,
            "customImageUrl" to customImageUrl,
            "createdAt" to (existingCreatedAt ?: FieldValue.serverTimestamp()),
            "updatedAt" to FieldValue.serverTimestamp()
        )
        @Suppress("UNCHECKED_CAST")
        return raw.filterValues { it != null } as Map<String, Any>
    }
}

@IgnoreExtraProperties
data class StoreWalletDoc(
    @DocumentId val id: String = "",
    val storeId: String = StoreProductDoc.STORE_ID_MAIN,
    val ownerId: String = "",
    val name: String = "",
    val accountNumber: String = "",
    val accountHolder: String = "",
    val note: String = "",
    val isEnabled: Boolean = true,
    @ServerTimestamp val createdAt: Timestamp? = null,
    @ServerTimestamp val updatedAt: Timestamp? = null
) {
    fun toDomainWallet(): WalletAccount {
        return WalletAccount(
            id = id,
            name = name,
            accountNumber = accountNumber,
            accountHolder = accountHolder,
            note = note,
            isEnabled = isEnabled
        )
    }

    fun toWriteMap(ownerUid: String, existingCreatedAt: Timestamp?): Map<String, Any> {
        return mapOf(
            "storeId" to StoreProductDoc.STORE_ID_MAIN,
            "ownerId" to ownerUid,
            "name" to name,
            "accountNumber" to accountNumber,
            "accountHolder" to accountHolder,
            "note" to note,
            "isEnabled" to isEnabled,
            "createdAt" to (existingCreatedAt ?: FieldValue.serverTimestamp()),
            "updatedAt" to FieldValue.serverTimestamp()
        )
    }

    companion object {
        fun fromDomain(wallet: WalletAccount, ownerUid: String): StoreWalletDoc {
            return StoreWalletDoc(
                id = wallet.id,
                storeId = StoreProductDoc.STORE_ID_MAIN,
                ownerId = ownerUid,
                name = wallet.name,
                accountNumber = wallet.accountNumber,
                accountHolder = wallet.accountHolder,
                note = wallet.note,
                isEnabled = wallet.isEnabled
            )
        }
    }
}

@IgnoreExtraProperties
data class StoreConfigDoc(
    @DocumentId val id: String = "main",
    val storeId: String = StoreProductDoc.STORE_ID_MAIN,
    val ownerId: String = "",
    val offerSlotProductIds: List<String> = emptyList(),
    val syncVersion: Long = 0L,
    val lastSyncedByName: String = "",
    @ServerTimestamp val createdAt: Timestamp? = null,
    @ServerTimestamp val updatedAt: Timestamp? = null
)

@IgnoreExtraProperties
data class AdminSecurityDoc(
    @DocumentId val id: String = "pin_config",
    val ownerId: String = "",
    val pinHash: String = "",
    val pinSalt: String = "",
    @ServerTimestamp val createdAt: Timestamp? = null,
    @ServerTimestamp val updatedAt: Timestamp? = null
)

@IgnoreExtraProperties
data class SecurityLogDoc(
    @DocumentId val id: String = "",
    val userId: String = "",
    val eventType: String = "",
    val details: String = "",
    @ServerTimestamp val createdAt: Timestamp? = null
)
