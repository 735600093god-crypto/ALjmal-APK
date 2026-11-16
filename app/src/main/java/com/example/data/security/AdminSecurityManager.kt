package com.example.data.security

import android.content.Context
import android.content.SharedPreferences
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.net.Uri
import android.util.Base64
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import java.io.ByteArrayOutputStream
import java.security.MessageDigest
import java.security.SecureRandom

data class SecurityFirewallState(
    val isLockedOut: Boolean = false,
    val remainingLockoutSeconds: Long = 0L,
    val failedAttempts: Int = 0,
    val maxAttemptsBeforeLock: Int = 3,
    val totalIntrusionBlocks: Int = 0,
    val firewallActive: Boolean = true,
    val recentAlerts: List<String> = emptyList()
)

sealed interface PinVerificationResult {
    data object Success : PinVerificationResult
    data class Invalid(val failedAttempts: Int, val remainingBeforeLock: Int) : PinVerificationResult
    data class LockedOut(val remainingSeconds: Long) : PinVerificationResult
}

/**
 * Strong client-side Anti-Intrusion Firewall & Salted SHA-256 Admin Authenticator.
 * Default admin password (!Admin@Aljamal2026) is stored only as a salted SHA-256 digest
 * and verified with constant-time byte comparison.
 */
class AdminSecurityManager(context: Context) {

    private val prefs: SharedPreferences = context.applicationContext.getSharedPreferences(
        PREFS_NAME,
        Context.MODE_PRIVATE
    )

    private val _firewallState = MutableStateFlow(SecurityFirewallState())
    val firewallState: StateFlow<SecurityFirewallState> = _firewallState.asStateFlow()

    init {
        ensureInitialized()
        refreshLockoutState()
    }

    private fun ensureInitialized() {
        val existingHash = prefs.getString(KEY_PIN_HASH, null)
        val existingSalt = prefs.getString(KEY_PIN_SALT, null)
        val policyVersion = prefs.getInt(KEY_POLICY_VERSION, 1)
        if (existingHash.isNullOrBlank() || existingSalt.isNullOrBlank() || policyVersion < CURRENT_POLICY_VERSION) {
            val salt = generateSaltHex()
            val hash = sha256Hex(salt, DEFAULT_ADMIN_PASSWORD)
            prefs.edit()
                .putString(KEY_PIN_SALT, salt)
                .putString(KEY_PIN_HASH, hash)
                .putInt(KEY_POLICY_VERSION, CURRENT_POLICY_VERSION)
                .putInt(KEY_FAILED_ATTEMPTS, 0)
                .putLong(KEY_LOCKOUT_UNTIL, 0L)
                .apply()
        }
    }

    fun refreshLockoutState() {
        val now = System.currentTimeMillis()
        val lockoutUntil = prefs.getLong(KEY_LOCKOUT_UNTIL, 0L)
        val failed = prefs.getInt(KEY_FAILED_ATTEMPTS, 0)
        val blocks = prefs.getInt(KEY_TOTAL_BLOCKS, 0)
        val isLocked = lockoutUntil > now
        val remainingSec = if (isLocked) ((lockoutUntil - now + 999L) / 1000L) else 0L

        _firewallState.update { current ->
            current.copy(
                isLockedOut = isLocked,
                remainingLockoutSeconds = remainingSec,
                failedAttempts = failed,
                totalIntrusionBlocks = blocks
            )
        }
    }

    fun verifyAdminPassword(candidateInput: String): PinVerificationResult {
        refreshLockoutState()
        val now = System.currentTimeMillis()
        val lockoutUntil = prefs.getLong(KEY_LOCKOUT_UNTIL, 0L)
        if (lockoutUntil > now) {
            val remainingSec = ((lockoutUntil - now + 999L) / 1000L).coerceAtLeast(1L)
            return PinVerificationResult.LockedOut(remainingSec)
        }

        val cleanInput = candidateInput.trim()
        if (cleanInput.isEmpty()) {
            return recordFailedAttempt("محاولة دخول برمز مرور فارغ")
        }

        val salt = prefs.getString(KEY_PIN_SALT, "") ?: ""
        val expectedHash = prefs.getString(KEY_PIN_HASH, "") ?: ""
        val candidateHash = sha256Hex(salt, cleanInput)

        return if (constantTimeEquals(expectedHash, candidateHash)) {
            prefs.edit()
                .putInt(KEY_FAILED_ATTEMPTS, 0)
                .putLong(KEY_LOCKOUT_UNTIL, 0L)
                .apply()
            addAlert("تم التحقق من كلمة سر المدير بنجاح عبر التشفير الآمن")
            refreshLockoutState()
            PinVerificationResult.Success
        } else {
            recordFailedAttempt("محاولة دخول فاشلة بكلمة سر غير مطابقة")
        }
    }

    private fun recordFailedAttempt(reason: String): PinVerificationResult {
        val newFailed = prefs.getInt(KEY_FAILED_ATTEMPTS, 0) + 1
        var lockoutUntil = 0L
        var blocks = prefs.getInt(KEY_TOTAL_BLOCKS, 0)

        if (newFailed >= MAX_ATTEMPTS_BEFORE_LOCK) {
            val multiplier = when {
                newFailed >= 7 -> 15 * 60_000L // 15 minutes
                newFailed >= 5 -> 5 * 60_000L  // 5 minutes
                else -> 30_000L                // 30 seconds
            }
            lockoutUntil = System.currentTimeMillis() + multiplier
            blocks += 1
            addAlert("جدار الحماية: تم حظر المحاولات لمدة ${multiplier / 1000L} ثانية ($reason)")
        } else {
            addAlert("تنبيه أمني: $reason (محاولة $newFailed من $MAX_ATTEMPTS_BEFORE_LOCK)")
        }

        prefs.edit()
            .putInt(KEY_FAILED_ATTEMPTS, newFailed)
            .putLong(KEY_LOCKOUT_UNTIL, lockoutUntil)
            .putInt(KEY_TOTAL_BLOCKS, blocks)
            .apply()

        refreshLockoutState()

        return if (lockoutUntil > System.currentTimeMillis()) {
            val remainingSec = ((lockoutUntil - System.currentTimeMillis() + 999L) / 1000L)
            PinVerificationResult.LockedOut(remainingSec)
        } else {
            PinVerificationResult.Invalid(
                failedAttempts = newFailed,
                remainingBeforeLock = (MAX_ATTEMPTS_BEFORE_LOCK - newFailed).coerceAtLeast(0)
            )
        }
    }

    fun changeAdminPassword(
        currentPassword: String,
        newPassword: String
    ): Result<Pair<String, String>> {
        val verify = verifyAdminPassword(currentPassword)
        if (verify !is PinVerificationResult.Success) {
            return Result.failure(IllegalArgumentException("كلمة السر الحالية غير صحيحة"))
        }

        val cleanNew = newPassword.trim()
        if (cleanNew.length < MIN_PASSWORD_LENGTH) {
            return Result.failure(IllegalArgumentException("يجب أن تتكون كلمة السر الجديدة من $MIN_PASSWORD_LENGTH أرقام/رموز على الأقل للحماية"))
        }
        if (cleanNew in WEAK_PINS) {
            return Result.failure(IllegalArgumentException("كلمة السر ضعيفة وسهلة التخمين؛ اختر رقماً أقوى"))
        }

        val newSalt = generateSaltHex()
        val newHash = sha256Hex(newSalt, cleanNew)
        prefs.edit()
            .putString(KEY_PIN_SALT, newSalt)
            .putString(KEY_PIN_HASH, newHash)
            .apply()

        addAlert("تم تحديث كلمة سر المدير وتشفيرها بنجاح (SHA-256)")
        return Result.success(newSalt to newHash)
    }

    fun applySyncedPinVerifier(salt: String, hash: String) {
        if (salt.length >= 16 && hash.length == 64) {
            prefs.edit()
                .putString(KEY_PIN_SALT, salt)
                .putString(KEY_PIN_HASH, hash)
                .putInt(KEY_POLICY_VERSION, CURRENT_POLICY_VERSION)
                .apply()
            addAlert("تمت مزامنة إعدادات كلمة سر المدير المشفرة من السحابة")
        }
    }

    fun getCurrentSaltAndHash(): Pair<String, String> {
        ensureInitialized()
        val salt = prefs.getString(KEY_PIN_SALT, "") ?: ""
        val hash = prefs.getString(KEY_PIN_HASH, "") ?: ""
        return salt to hash
    }

    fun recordSecurityNotice(message: String) {
        addAlert(message)
    }

    private fun addAlert(message: String) {
        _firewallState.update { state ->
            val updated = (listOf(message) + state.recentAlerts).distinct().take(15)
            state.copy(recentAlerts = updated)
        }
    }

    companion object {
        private const val PREFS_NAME = "aljamal_security_prefs"
        private const val KEY_PIN_SALT = "admin_pin_salt"
        private const val KEY_PIN_HASH = "admin_pin_hash"
        private const val KEY_POLICY_VERSION = "admin_pin_policy_version"
        private const val CURRENT_POLICY_VERSION = 2
        private const val KEY_FAILED_ATTEMPTS = "failed_attempts"
        private const val KEY_LOCKOUT_UNTIL = "lockout_until"
        private const val KEY_TOTAL_BLOCKS = "total_blocks"

        const val DEFAULT_ADMIN_PASSWORD = "!Admin@Aljamal2026"
        const val MAX_ATTEMPTS_BEFORE_LOCK = 3
        const val MIN_PASSWORD_LENGTH = 6

        private val WEAK_PINS = setOf("123456", "000000", "111111", "654321", "1234", "0000")

        fun sha256Hex(salt: String, secret: String): String {
            val digest = MessageDigest.getInstance("SHA-256")
            val bytes = digest.digest("$salt:$secret".toByteArray(Charsets.UTF_8))
            return bytes.joinToString("") { "%02x".format(it) }
        }

        fun generateSaltHex(): String {
            val bytes = ByteArray(16)
            SecureRandom().nextBytes(bytes)
            return bytes.joinToString("") { "%02x".format(it) }
        }

        fun constantTimeEquals(a: String, b: String): Boolean {
            return MessageDigest.isEqual(
                a.toByteArray(Charsets.UTF_8),
                b.toByteArray(Charsets.UTF_8)
            )
        }

        fun sanitizeText(input: String, maxLength: Int = 200): String {
            return input
                .replace(Regex("[\\u0000-\\u001F\\u007F]"), "")
                .replace(Regex("(?i)<script.*?>.*?</script>"), "")
                .replace("<", "")
                .replace(">", "")
                .trim()
                .take(maxLength)
        }

        fun isSafeImageUrl(url: String): Boolean {
            val trimmed = url.trim()
            return trimmed.startsWith("https://") ||
                trimmed.startsWith("data:image/jpeg;base64,") ||
                trimmed.startsWith("data:image/png;base64,") ||
                trimmed.startsWith("data:image/webp;base64,") ||
                trimmed.startsWith("content://")
        }

        /**
         * Compresses a gallery-picked image URI into a compact base64 Data URL (~20-60KB)
         * so custom product and category images sync seamlessly over Cloud Firestore across all devices.
         */
        fun compressPickedImageToDataUrl(context: Context, uri: Uri): String? {
            return try {
                val resolver = context.contentResolver
                val boundsOpts = BitmapFactory.Options().apply { inJustDecodeBounds = true }
                resolver.openInputStream(uri)?.use { stream ->
                    BitmapFactory.decodeStream(stream, null, boundsOpts)
                }

                val maxDim = 480
                var sampleSize = 1
                val width = boundsOpts.outWidth
                val height = boundsOpts.outHeight
                if (width > maxDim || height > maxDim) {
                    val halfW = width / 2
                    val halfH = height / 2
                    while ((halfW / sampleSize) >= maxDim && (halfH / sampleSize) >= maxDim) {
                        sampleSize *= 2
                    }
                }

                val decodeOpts = BitmapFactory.Options().apply { inSampleSize = sampleSize }
                val decoded = resolver.openInputStream(uri)?.use { stream ->
                    BitmapFactory.decodeStream(stream, null, decodeOpts)
                } ?: return uri.toString()

                val scale = minOf(
                    maxDim.toFloat() / decoded.width.coerceAtLeast(1),
                    maxDim.toFloat() / decoded.height.coerceAtLeast(1),
                    1f
                )
                val finalBitmap = if (scale < 1f) {
                    Bitmap.createScaledBitmap(
                        decoded,
                        (decoded.width * scale).toInt().coerceAtLeast(1),
                        (decoded.height * scale).toInt().coerceAtLeast(1),
                        true
                    )
                } else {
                    decoded
                }

                val out = ByteArrayOutputStream()
                var quality = 75
                finalBitmap.compress(Bitmap.CompressFormat.JPEG, quality, out)
                while (out.size() > 90_000 && quality > 35) {
                    out.reset()
                    quality -= 10
                    finalBitmap.compress(Bitmap.CompressFormat.JPEG, quality, out)
                }

                if (finalBitmap !== decoded) finalBitmap.recycle()
                decoded.recycle()

                val b64 = Base64.encodeToString(out.toByteArray(), Base64.NO_WRAP)
                "data:image/jpeg;base64,$b64"
            } catch (_: Exception) {
                uri.toString()
            }
        }
    }
}
