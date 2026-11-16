package com.example.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.Security
import androidx.compose.material.icons.filled.Shield
import androidx.compose.material.icons.filled.Visibility
import androidx.compose.material.icons.filled.VisibilityOff
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import com.example.data.security.PinVerificationResult
import com.example.data.security.SecurityFirewallState
import com.example.ui.theme.Amber100
import com.example.ui.theme.Amber400
import com.example.ui.theme.Emerald100
import com.example.ui.theme.Emerald50
import com.example.ui.theme.Emerald800
import com.example.ui.theme.Emerald900
import com.example.ui.theme.Red50
import com.example.ui.theme.Red500
import com.example.ui.theme.Stone100
import com.example.ui.theme.Stone400
import com.example.ui.theme.Stone500
import com.example.ui.theme.Stone900
import com.example.ui.theme.Stone950
import com.example.ui.theme.White
import kotlinx.coroutines.delay

@Composable
fun AdminLoginDialog(
    firewallState: SecurityFirewallState,
    onVerifyPassword: (String) -> PinVerificationResult,
    onRefreshFirewall: () -> Unit,
    onSuccess: () -> Unit,
    onDismiss: () -> Unit
) {
    var password by remember { mutableStateOf("") }
    var showPassword by remember { mutableStateOf(false) }
    var errorMessage by remember { mutableStateOf<String?>(null) }

    // Live countdown ticker whenever the anti-intrusion firewall locks access
    LaunchedEffect(firewallState.isLockedOut) {
        while (firewallState.isLockedOut) {
            delay(1000L)
            onRefreshFirewall()
        }
    }

    Dialog(onDismissRequest = onDismiss) {
        Surface(
            modifier = Modifier
                .fillMaxWidth(0.92f)
                .clip(RoundedCornerShape(22.dp)),
            color = White,
            tonalElevation = 6.dp
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(20.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                // Top Header
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(
                            imageVector = Icons.Default.Shield,
                            contentDescription = null,
                            tint = Emerald800,
                            modifier = Modifier.size(18.dp)
                        )
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(
                            text = "مصادقة المدير المحمية",
                            fontSize = 15.sp,
                            fontWeight = FontWeight.Black,
                            color = Stone900
                        )
                    }
                    Box(
                        modifier = Modifier
                            .size(28.dp)
                            .clip(CircleShape)
                            .background(Stone100)
                            .clickable { onDismiss() },
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = Icons.Default.Close,
                            contentDescription = "إغلاق",
                            tint = Stone900,
                            modifier = Modifier.size(16.dp)
                        )
                    }
                }

                Spacer(modifier = Modifier.height(12.dp))

                // Security Shield Badge
                Surface(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(12.dp),
                    color = if (firewallState.isLockedOut) Red50 else Emerald50
                ) {
                    Row(
                        modifier = Modifier.padding(horizontal = 10.dp, vertical = 8.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(
                            imageVector = Icons.Default.Security,
                            contentDescription = null,
                            tint = if (firewallState.isLockedOut) Red500 else Emerald900,
                            modifier = Modifier.size(16.dp)
                        )
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(
                            text = if (firewallState.isLockedOut) {
                                "تم تفعيل جدار الحماية ضد الاختراق • الحظر مؤقت: ${firewallState.remainingLockoutSeconds} ث"
                            } else {
                                "جدار الحماية نشط • التشفير: SHA-256"
                            },
                            fontSize = 10.5.sp,
                            fontWeight = FontWeight.Bold,
                            color = if (firewallState.isLockedOut) Red500 else Emerald900
                        )
                    }
                }

                Spacer(modifier = Modifier.height(14.dp))

                Box(
                    modifier = Modifier
                        .size(52.dp)
                        .clip(CircleShape)
                        .background(if (firewallState.isLockedOut) Red50 else Amber100)
                        .border(1.dp, if (firewallState.isLockedOut) Red500 else Amber400, CircleShape),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = Icons.Default.Lock,
                        contentDescription = null,
                        tint = if (firewallState.isLockedOut) Red500 else Stone950,
                        modifier = Modifier.size(24.dp)
                    )
                }

                Spacer(modifier = Modifier.height(10.dp))

                Text(
                    text = "أدخل كلمة سر المدير المعتمدة لفتح لوحة التحكم والمزامنة السحابية",
                    fontSize = 11.sp,
                    color = Stone500,
                    textAlign = TextAlign.Center
                )

                Spacer(modifier = Modifier.height(14.dp))

                OutlinedTextField(
                    value = password,
                    onValueChange = {
                        password = it
                        errorMessage = null
                    },
                    enabled = !firewallState.isLockedOut,
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("admin_pin_input"),
                    placeholder = {
                        Text(
                            text = "أدخل كلمة سر المدير",
                            fontSize = 11.sp,
                            color = Stone400
                        )
                    },
                    visualTransformation = if (showPassword) VisualTransformation.None else PasswordVisualTransformation(),
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Password),
                    trailingIcon = {
                        IconButton(onClick = { showPassword = !showPassword }) {
                            Icon(
                                imageVector = if (showPassword) Icons.Default.VisibilityOff else Icons.Default.Visibility,
                                contentDescription = if (showPassword) "إخفاء كلمة السر" else "إظهار كلمة السر",
                                tint = Stone500
                            )
                        }
                    },
                    singleLine = true,
                    shape = RoundedCornerShape(12.dp)
                )

                if (errorMessage != null || firewallState.isLockedOut) {
                    Spacer(modifier = Modifier.height(6.dp))
                    Text(
                        text = if (firewallState.isLockedOut) {
                            "تم إيقاف المحاولات مؤقتاً لحماية المتجر. أعد المحاولة بعد ${firewallState.remainingLockoutSeconds} ثانية."
                        } else {
                            errorMessage ?: ""
                        },
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold,
                        color = Red500,
                        textAlign = TextAlign.Center
                    )
                }

                Spacer(modifier = Modifier.height(16.dp))

                Button(
                    onClick = {
                        when (val result = onVerifyPassword(password)) {
                            is PinVerificationResult.Success -> {
                                password = ""
                                errorMessage = null
                                onSuccess()
                            }

                            is PinVerificationResult.Invalid -> {
                                errorMessage = if (result.remainingBeforeLock > 0) {
                                    "كلمة السر غير صحيحة! متبقي ${result.remainingBeforeLock} محاولات قبل الحظر الأمني."
                                } else {
                                    "كلمة السر غير صحيحة."
                                }
                            }

                            is PinVerificationResult.LockedOut -> {
                                errorMessage = "تم حظر الدخول مؤقتاً لمدة ${result.remainingSeconds} ثانية."
                            }
                        }
                    },
                    enabled = !firewallState.isLockedOut && password.isNotBlank(),
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(46.dp)
                        .testTag("admin_pin_submit_button"),
                    shape = RoundedCornerShape(12.dp),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = Emerald800,
                        disabledContainerColor = Stone100
                    )
                ) {
                    Text(
                        text = if (firewallState.isLockedOut) "محظور مؤقتاً (${firewallState.remainingLockoutSeconds} ث)" else "تحقق ودخول للوحة التحكم",
                        fontSize = 13.sp,
                        fontWeight = FontWeight.Black,
                        color = if (firewallState.isLockedOut) Red500 else White
                    )
                }
            }
        }
    }
}
