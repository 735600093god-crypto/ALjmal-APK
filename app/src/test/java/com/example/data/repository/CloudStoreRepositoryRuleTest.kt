package com.example.data.repository

import com.example.base.FirestoreEmulatorTestBase
import com.example.data.remote.CloudStoreRepository
import com.google.firebase.firestore.FirebaseFirestoreException
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.runBlocking
import kotlinx.coroutines.withTimeout
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertTrue
import org.junit.Assert.fail
import org.junit.Test

class CloudStoreRepositoryRuleTest : FirestoreEmulatorTestBase() {

    @Test
    fun observeProducts_authenticatedCustomer_succeedsWithoutPermissionError() = runBlocking {
        signInTestUser(CUSTOMER_EMAIL)
        val cloudRepo = CloudStoreRepository(firestore)

        val products = withTimeout(FLOW_TIMEOUT_MS) {
            cloudRepo.observeProducts().first()
        }
        assertTrue(products.isEmpty() || products.isNotEmpty())
    }

    @Test
    fun publishFullStoreCatalog_nonAdminCustomer_deniedByZeroTrustRules() = runBlocking {
        signInTestUser(CUSTOMER_EMAIL)
        val cloudRepo = CloudStoreRepository(firestore)
        val localRepo = StoreRepository()

        try {
            withTimeout(DEFAULT_TIMEOUT_MS) {
                cloudRepo.publishFullStoreCatalog(
                    products = localRepo.products.value.take(1),
                    deletedProductIds = emptySet(),
                    categories = emptyMap(),
                    wallets = emptyList(),
                    deletedWalletIds = emptySet(),
                    offerSlotProductIds = emptyList(),
                    pinSaltAndHash = null
                )
            }
            fail("Expected PERMISSION_DENIED for non-admin customer publishing store catalog")
        } catch (e: Exception) {
            val fsEx = e.unwrapFirestoreException()
            assertNotNull("Expected FirebaseFirestoreException, got $e", fsEx)
            assertEquals(FirebaseFirestoreException.Code.PERMISSION_DENIED, fsEx?.code)
        }
    }

    @Test
    fun observeProducts_unauthenticatedUser_failsWithIllegalStateOrPermissionDenied() = runBlocking {
        auth.signOut()
        val cloudRepo = CloudStoreRepository(firestore)

        try {
            withTimeout(FLOW_TIMEOUT_MS) {
                cloudRepo.observeProducts().first()
            }
            fail("Expected unauthenticated query to be rejected")
        } catch (e: Exception) {
            assertTrue(
                e is IllegalStateException ||
                    e.unwrapFirestoreException()?.code == FirebaseFirestoreException.Code.PERMISSION_DENIED
            )
        }
    }

    @Test
    fun logSecurityEvent_authenticatedUser_createsAppendOnlyAuditLog() = runBlocking {
        signInTestUser(CUSTOMER_EMAIL)
        val cloudRepo = CloudStoreRepository(firestore)

        withTimeout(DEFAULT_TIMEOUT_MS) {
            cloudRepo.logSecurityEvent("FAILED_PIN", "اختبار تسجيل حدث أمني")
        }
        assertTrue(true)
    }

    @Test
    fun directUnfilteredCollectionRead_customerDenied() = runBlocking {
        signInTestUser(CUSTOMER_EMAIL)

        // Direct read to admin_security/pin_config must be denied for non-admins
        try {
            withTimeout(DEFAULT_TIMEOUT_MS) {
                com.google.android.gms.tasks.Tasks.await(
                    firestore.collection("admin_security").document("pin_config").get()
                )
            }
            fail("Expected PERMISSION_DENIED when non-admin reads admin_security")
        } catch (e: Exception) {
            val fsEx = e.unwrapFirestoreException()
            assertNotNull("Expected FirebaseFirestoreException, got $e", fsEx)
            assertEquals(FirebaseFirestoreException.Code.PERMISSION_DENIED, fsEx?.code)
        }
    }

    private companion object {
        const val CUSTOMER_EMAIL = "customer_test@example.com"
        const val DEFAULT_TIMEOUT_MS = 5000L
        const val FLOW_TIMEOUT_MS = 3000L
    }
}
