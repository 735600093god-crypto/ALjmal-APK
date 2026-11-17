package com.example.data.remote

import android.content.Context
import com.example.R
import com.example.data.model.CategoryCustomization
import com.example.data.model.Product
import com.example.data.model.WalletAccount
import com.example.data.security.AdminSecurityManager
import com.google.firebase.Firebase
import com.google.firebase.Timestamp
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.auth.auth
import com.google.firebase.firestore.DocumentSnapshot
import com.google.firebase.firestore.FieldValue
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.FirebaseFirestoreException
import com.google.firebase.firestore.snapshots
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.emitAll
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.tasks.await
import java.util.UUID

data class CloudSyncMetadata(
    val syncVersion: Long = 0L,
    val lastSyncedByName: String = "",
    val lastSyncedAtMillis: Long? = null
)

class CloudStoreRepository(
    private val db: FirebaseFirestore
) {
    constructor(context: Context) : this(
        FirebaseFirestore.getInstance(
            context.applicationContext.getString(R.string.firestore_database_id)
        )
    )

    private val auth = Firebase.auth

    private fun requireUserId(): String {
        return auth.currentUser?.uid
            ?: throw IllegalStateException("يجب تسجيل الدخول أولاً قبل الاتصال بقاعدة البيانات.")
    }

    // Cache existing createdAt timestamps so updates satisfy immutable createdAt security rules
    private val productCreatedAt = mutableMapOf<String, Timestamp>()
    private val categoryCreatedAt = mutableMapOf<String, Timestamp>()
    private val walletCreatedAt = mutableMapOf<String, Timestamp>()
    private var configCreatedAt: Timestamp? = null
    private var pinConfigCreatedAt: Timestamp? = null
    private var currentSyncVersion: Long = 0L

    fun observeProducts(): Flow<List<Product>> {
        val path = COLLECTION_PRODUCTS
        return db.collection(path)
            .whereEqualTo("storeId", StoreProductDoc.STORE_ID_MAIN)
            .snapshots()
            .map { snapshot ->
                snapshot.documents.mapNotNull { doc ->
                    val item = doc.toObject(
                        StoreProductDoc::class.java,
                        DocumentSnapshot.ServerTimestampBehavior.ESTIMATE
                    )
                    if (item != null) {
                        item.createdAt?.let { productCreatedAt[item.id] = it }
                        item.toDomainProduct()
                    } else null
                }
            }
            .catch { error ->
                val firestoreError = generateSequence(error) { it.cause }
                    .filterIsInstance<FirebaseFirestoreException>()
                    .firstOrNull()
                if (firestoreError == null && error is CancellationException) throw error
                val toLog = firestoreError ?: (error as? Exception)
                if (toLog != null && auth.currentUser != null) {
                    handleFirestoreError(toLog, OperationType.LIST, path)
                }
                throw error
            }
    }

    fun observeCategories(): Flow<Map<String, CategoryCustomization>> {
        val path = COLLECTION_CATEGORIES
        return db.collection(path)
            .whereEqualTo("storeId", StoreProductDoc.STORE_ID_MAIN)
            .snapshots()
            .map { snapshot ->
                snapshot.documents.mapNotNull { doc ->
                    val item = doc.toObject(
                        StoreCategoryDoc::class.java,
                        DocumentSnapshot.ServerTimestampBehavior.ESTIMATE
                    )
                    if (item != null) {
                        item.createdAt?.let { categoryCreatedAt[item.id] = it }
                        item.id to item.toDomainCategory()
                    } else null
                }.toMap()
            }
            .catch { error ->
                val firestoreError = generateSequence(error) { it.cause }
                    .filterIsInstance<FirebaseFirestoreException>()
                    .firstOrNull()
                if (firestoreError == null && error is CancellationException) throw error
                val toLog = firestoreError ?: (error as? Exception)
                if (toLog != null && auth.currentUser != null) {
                    handleFirestoreError(toLog, OperationType.LIST, path)
                }
                throw error
            }
    }

    fun observeWallets(): Flow<List<WalletAccount>> {
        val path = COLLECTION_WALLETS
        return db.collection(path)
            .whereEqualTo("storeId", StoreProductDoc.STORE_ID_MAIN)
            .snapshots()
            .map { snapshot ->
                snapshot.documents.mapNotNull { doc ->
                    val item = doc.toObject(
                        StoreWalletDoc::class.java,
                        DocumentSnapshot.ServerTimestampBehavior.ESTIMATE
                    )
                    if (item != null) {
                        item.createdAt?.let { walletCreatedAt[item.id] = it }
                        item.toDomainWallet()
                    } else null
                }
            }
            .catch { error ->
                val firestoreError = generateSequence(error) { it.cause }
                    .filterIsInstance<FirebaseFirestoreException>()
                    .firstOrNull()
                if (firestoreError == null && error is CancellationException) throw error
                val toLog = firestoreError ?: (error as? Exception)
                if (toLog != null && auth.currentUser != null) {
                    handleFirestoreError(toLog, OperationType.LIST, path)
                }
                throw error
            }
    }

    fun observeStoreConfig(): Flow<Pair<List<String>, CloudSyncMetadata>?> {
        val docRef = db.collection(COLLECTION_CONFIG).document(DOC_MAIN_CONFIG)
        return docRef.snapshots()
            .map { snapshot ->
                if (!snapshot.exists()) {
                    null
                } else {
                    val doc = snapshot.toObject(
                        StoreConfigDoc::class.java,
                        DocumentSnapshot.ServerTimestampBehavior.ESTIMATE
                    ) ?: return@map null
                    doc.createdAt?.let { configCreatedAt = it }
                    currentSyncVersion = doc.syncVersion
                    val meta = CloudSyncMetadata(
                        syncVersion = doc.syncVersion,
                        lastSyncedByName = doc.lastSyncedByName,
                        lastSyncedAtMillis = doc.updatedAt?.toDate()?.time
                    )
                    doc.offerSlotProductIds.take(4) to meta
                }
            }
            .catch { error ->
                val firestoreError = generateSequence(error) { it.cause }
                    .filterIsInstance<FirebaseFirestoreException>()
                    .firstOrNull()
                if (firestoreError == null && error is CancellationException) throw error
                val toLog = firestoreError ?: (error as? Exception)
                if (toLog != null && auth.currentUser != null) {
                    handleFirestoreError(toLog, OperationType.GET, docRef.path)
                }
                throw error
            }
    }

    suspend fun ensureUserProfile(user: FirebaseUser) {
        val docRef = db.collection(COLLECTION_USERS).document(user.uid)
        try {
            val existing = docRef.get().await()
            val safeName = AdminSecurityManager.sanitizeText(
                user.displayName?.takeIf { it.isNotBlank() } ?: "عميل متجر الجمل",
                80
            )
            val safeEmail = AdminSecurityManager.sanitizeText(
                user.email?.takeIf { it.isNotBlank() } ?: "user@aljamal.local",
                120
            )
            if (!existing.exists()) {
                val payload = mapOf(
                    "userId" to user.uid,
                    "displayName" to safeName,
                    "email" to safeEmail,
                    "role" to "customer",
                    "createdAt" to FieldValue.serverTimestamp(),
                    "updatedAt" to FieldValue.serverTimestamp()
                )
                docRef.set(payload).await()
            } else {
                docRef.update(
                    mapOf(
                        "displayName" to safeName,
                        "email" to safeEmail,
                        "updatedAt" to FieldValue.serverTimestamp()
                    )
                ).await()
            }
        } catch (e: CancellationException) {
            throw e
        } catch (e: Exception) {
            handleFirestoreError(e, OperationType.WRITE, docRef.path)
        }
    }

    suspend fun checkIsCloudAdmin(user: FirebaseUser): Boolean {
        if (user.isEmailVerified && user.email?.equals(BOOTSTRAPPED_ADMIN_EMAIL, ignoreCase = true) == true) {
            return true
        }
        val adminDocRef = db.collection(COLLECTION_ADMINS).document(user.uid)
        return try {
            val snap = adminDocRef.get().await()
            snap.exists()
        } catch (e: CancellationException) {
            throw e
        } catch (e: Exception) {
            handleFirestoreError(e, OperationType.GET, adminDocRef.path)
            false
        }
    }

    suspend fun fetchAdminSecurityPinConfig(): AdminSecurityDoc? {
        requireUserId()
        val docRef = db.collection(COLLECTION_ADMIN_SECURITY).document(DOC_PIN_CONFIG)
        return try {
            val snap = docRef.get().await()
            if (!snap.exists()) null else {
                snap.toObject(
                    AdminSecurityDoc::class.java,
                    DocumentSnapshot.ServerTimestampBehavior.ESTIMATE
                )?.also { doc ->
                    doc.createdAt?.let { pinConfigCreatedAt = it }
                }
            }
        } catch (e: CancellationException) {
            throw e
        } catch (e: Exception) {
            handleFirestoreError(e, OperationType.GET, docRef.path)
            null
        }
    }

    fun observeAdminSecurityPinConfig(): Flow<AdminSecurityDoc?> = flow {
        requireUserId()
        val docRef = db.collection(COLLECTION_ADMIN_SECURITY).document(DOC_PIN_CONFIG)
        emitAll(
            docRef.snapshots()
                .map { snap ->
                    if (!snap.exists()) null else {
                        snap.toObject(
                            AdminSecurityDoc::class.java,
                            DocumentSnapshot.ServerTimestampBehavior.ESTIMATE
                        )?.also { doc ->
                            doc.createdAt?.let { pinConfigCreatedAt = it }
                        }
                    }
                }
                .catch { error ->
                    val firestoreError = generateSequence(error) { it.cause }
                        .filterIsInstance<FirebaseFirestoreException>()
                        .firstOrNull()
                    if (firestoreError == null && error is CancellationException) throw error
                    val toLog = firestoreError ?: (error as? Exception)
                    if (toLog != null && auth.currentUser != null) {
                        handleFirestoreError(toLog, OperationType.GET, docRef.path)
                    }
                    throw error
                }
        )
    }

    suspend fun publishFullStoreCatalog(
        products: List<Product>,
        deletedProductIds: Set<String>,
        categories: Map<String, CategoryCustomization>,
        wallets: List<WalletAccount>,
        deletedWalletIds: Set<String>,
        offerSlotProductIds: List<String>,
        pinSaltAndHash: Pair<String, String>? = null
    ): Long {
        val uid = requireUserId()
        val user = auth.currentUser
        val nextVersion = currentSyncVersion + 1L
        val signerName = AdminSecurityManager.sanitizeText(
            user?.displayName?.takeIf { it.isNotBlank() } ?: user?.email ?: "المدير",
            80
        )

        val operations = mutableListOf<BatchOp>()

        products.forEach { prod ->
            val cleanId = sanitizeDocumentId(prod.id)
            val safeImg = if (AdminSecurityManager.isSafeImageUrl(prod.imageUrl)) {
                prod.imageUrl
            } else {
                DEFAULT_SAFE_PRODUCT_IMAGE
            }
            val safeProd = prod.copy(
                id = cleanId,
                name = AdminSecurityManager.sanitizeText(prod.name, 120).ifBlank { "صنف متجر الجمل" },
                unit = AdminSecurityManager.sanitizeText(prod.unit, 50).ifBlank { "كجم" },
                origin = AdminSecurityManager.sanitizeText(prod.origin, 80).ifBlank { "اليمن" },
                badge = prod.badge?.let { AdminSecurityManager.sanitizeText(it, 60) }?.takeIf { it.isNotBlank() },
                imageUrl = safeImg,
                description = AdminSecurityManager.sanitizeText(prod.description, 1000)
            )
            val docModel = StoreProductDoc.fromDomain(safeProd, uid)
            val ref = db.collection(COLLECTION_PRODUCTS).document(cleanId)
            val existingTs = productCreatedAt[cleanId]
            operations += BatchOp.Set(ref, docModel.toWriteMap(uid, existingTs))
        }

        deletedProductIds.forEach { id ->
            val cleanId = sanitizeDocumentId(id)
            operations += BatchOp.Delete(db.collection(COLLECTION_PRODUCTS).document(cleanId))
        }

        categories.values.forEach { cat ->
            val cleanId = sanitizeDocumentId(cat.categoryId)
            val safeImg = cat.customImageUrl?.takeIf { AdminSecurityManager.isSafeImageUrl(it) }
            val docModel = StoreCategoryDoc(
                id = cleanId,
                storeId = StoreProductDoc.STORE_ID_MAIN,
                ownerId = uid,
                customTitle = AdminSecurityManager.sanitizeText(cat.customTitle, 80).ifBlank { cleanId },
                customSubtitle = AdminSecurityManager.sanitizeText(cat.customSubtitle, 160),
                customImageUrl = safeImg
            )
            val ref = db.collection(COLLECTION_CATEGORIES).document(cleanId)
            val existingTs = categoryCreatedAt[cleanId]
            operations += BatchOp.Set(ref, docModel.toWriteMap(uid, existingTs))
        }

        wallets.forEach { wallet ->
            val cleanId = sanitizeDocumentId(wallet.id)
            val docModel = StoreWalletDoc.fromDomain(
                wallet.copy(
                    id = cleanId,
                    name = AdminSecurityManager.sanitizeText(wallet.name, 80).ifBlank { "محفظة" },
                    accountNumber = AdminSecurityManager.sanitizeText(wallet.accountNumber, 60).ifBlank { "غير محدد" },
                    accountHolder = AdminSecurityManager.sanitizeText(wallet.accountHolder, 80),
                    note = AdminSecurityManager.sanitizeText(wallet.note, 200)
                ),
                uid
            )
            val ref = db.collection(COLLECTION_WALLETS).document(cleanId)
            val existingTs = walletCreatedAt[cleanId]
            operations += BatchOp.Set(ref, docModel.toWriteMap(uid, existingTs))
        }

        deletedWalletIds.forEach { id ->
            val cleanId = sanitizeDocumentId(id)
            operations += BatchOp.Delete(db.collection(COLLECTION_WALLETS).document(cleanId))
        }

        val configRef = db.collection(COLLECTION_CONFIG).document(DOC_MAIN_CONFIG)
        if (configCreatedAt == null) {
            try {
                val snap = configRef.get().await()
                if (snap.exists()) {
                    configCreatedAt = snap.getTimestamp("createdAt")
                }
            } catch (_: Exception) {
                // Keep null -> FieldValue.serverTimestamp() for first creation
            }
        }
        val configPayload = mapOf(
            "storeId" to StoreProductDoc.STORE_ID_MAIN,
            "ownerId" to uid,
            "offerSlotProductIds" to offerSlotProductIds.map { sanitizeDocumentId(it) }.take(4),
            "syncVersion" to nextVersion,
            "lastSyncedByName" to signerName,
            "createdAt" to (configCreatedAt ?: FieldValue.serverTimestamp()),
            "updatedAt" to FieldValue.serverTimestamp()
        )
        operations += BatchOp.Set(configRef, configPayload)

        if (pinSaltAndHash != null) {
            val (salt, hash) = pinSaltAndHash
            val pinRef = db.collection(COLLECTION_ADMIN_SECURITY).document(DOC_PIN_CONFIG)
            if (pinConfigCreatedAt == null) {
                try {
                    val snap = pinRef.get().await()
                    if (snap.exists()) {
                        pinConfigCreatedAt = snap.getTimestamp("createdAt")
                    }
                } catch (_: Exception) {}
            }
            val pinPayload = mapOf(
                "ownerId" to uid,
                "pinHash" to hash,
                "pinSalt" to salt,
                "createdAt" to (pinConfigCreatedAt ?: FieldValue.serverTimestamp()),
                "updatedAt" to FieldValue.serverTimestamp()
            )
            operations += BatchOp.Set(pinRef, pinPayload)
        }

        val logId = "sync_${UUID.randomUUID().toString().replace("-", "").take(16)}"
        val logRef = db.collection(COLLECTION_SECURITY_LOGS).document(logId)
        operations += BatchOp.Set(
            logRef,
            mapOf(
                "userId" to uid,
                "eventType" to "CLOUD_SYNC",
                "details" to "حفظ ومزامنة ${products.size} صنف و${wallets.size} محفظة (الإصدار $nextVersion)",
                "createdAt" to FieldValue.serverTimestamp()
            )
        )

        try {
            // Firestore batches allow up to 500 operations per commit
            operations.chunked(400).forEach { chunk ->
                val batch = db.batch()
                chunk.forEach { op ->
                    when (op) {
                        is BatchOp.Set -> batch.set(op.ref, op.data)
                        is BatchOp.Delete -> batch.delete(op.ref)
                    }
                }
                batch.commit().await()
            }
            currentSyncVersion = nextVersion
            return nextVersion
        } catch (e: CancellationException) {
            throw e
        } catch (e: Exception) {
            handleFirestoreError(e, OperationType.WRITE, configRef.path)
            throw e
        }
    }

    suspend fun logSecurityEvent(eventType: String, details: String) {
        val uid = auth.currentUser?.uid ?: return
        val logId = "sec_${UUID.randomUUID().toString().replace("-", "").take(16)}"
        val docRef = db.collection(COLLECTION_SECURITY_LOGS).document(logId)
        val payload = mapOf(
            "userId" to uid,
            "eventType" to eventType,
            "details" to AdminSecurityManager.sanitizeText(details, 250).ifBlank { eventType },
            "createdAt" to FieldValue.serverTimestamp()
        )
        try {
            docRef.set(payload).await()
        } catch (e: CancellationException) {
            throw e
        } catch (e: Exception) {
            handleFirestoreError(e, OperationType.CREATE, docRef.path)
        }
    }

    private sealed interface BatchOp {
        data class Set(
            val ref: com.google.firebase.firestore.DocumentReference,
            val data: Map<String, Any>
        ) : BatchOp

        data class Delete(
            val ref: com.google.firebase.firestore.DocumentReference
        ) : BatchOp
    }

    companion object {
        const val BOOTSTRAPPED_ADMIN_EMAIL = "akrmshaker8@gmail.com"
        const val FIREBASE_PROJECT_ID = "gen-lang-client-0198222948"
        const val FIREBASE_AUTH_DOMAIN = "gen-lang-client-0198222948.firebaseapp.com"
        const val FIREBASE_WEB_APP_ID = "1:315465823648:web:aac7daf4a26ea4d20920c5"
        const val COLLECTION_USERS = "users"
        const val COLLECTION_ADMINS = "admins"
        const val COLLECTION_PRODUCTS = "store_products"
        const val COLLECTION_CATEGORIES = "store_categories"
        const val COLLECTION_WALLETS = "store_wallets"
        const val COLLECTION_CONFIG = "store_config"
        const val COLLECTION_ADMIN_SECURITY = "admin_security"
        const val COLLECTION_SECURITY_LOGS = "security_logs"

        const val DOC_MAIN_CONFIG = "main"
        const val DOC_PIN_CONFIG = "pin_config"

        private const val DEFAULT_SAFE_PRODUCT_IMAGE =
            "https://images.unsplash.com/photo-1615485290382-441e4d049cb5?auto=format&fit=crop&w=600&q=80"

        fun sanitizeDocumentId(raw: String): String {
            val cleaned = raw.replace(Regex("[^a-zA-Z0-9_\\-]"), "_")
            return cleaned.take(120).ifBlank { "id_${System.currentTimeMillis()}" }
        }
    }
}
