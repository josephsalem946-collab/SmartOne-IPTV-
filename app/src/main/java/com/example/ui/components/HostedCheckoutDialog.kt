package com.example.ui.components

import android.content.Intent
import android.net.Uri
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import com.example.R
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.payment.PaymentGatewayType
import com.example.data.payment.PaymentOrder
import com.example.ui.theme.*
import java.util.Locale

@Composable
fun HostedCheckoutDialog(
    order: PaymentOrder,
    onComplete: (success: Boolean, message: String) -> Unit,
    onCancel: () -> Unit
) {
    val context = LocalContext.current
    var isProcessing by remember { mutableStateOf(false) }

    val isMonCash = order.gateway == PaymentGatewayType.MONCASH
    val isCrypto = order.gateway == PaymentGatewayType.NOWPAYMENTS_CRYPTO
    val gatewayBrandColor = when (order.gateway) {
        PaymentGatewayType.MONCASH -> Color(0xFFD81E05)
        PaymentGatewayType.NATCASH -> Color(0xFFFF6600)
        PaymentGatewayType.NOWPAYMENTS_CRYPTO -> Color(0xFF059669)
        PaymentGatewayType.STRIPE_CHECKOUT -> Color(0xFF0066FF)
    }
    val gatewayName = order.gateway.displayName

    AlertDialog(
        onDismissRequest = {
            if (!isProcessing) onCancel()
        },
        title = null,
        text = {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .testTag("hosted_checkout_dialog"),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                // Browser URL simulation Bar with SSL Lock
                Surface(
                    shape = RoundedCornerShape(8.dp),
                    color = Color(0xFF0F172A),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Row(
                        modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(
                            imageVector = Icons.Default.Lock,
                            contentDescription = "SSL Sécurisé",
                            tint = StatusGreen,
                            modifier = Modifier.size(13.dp)
                        )
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(
                            text = when (order.gateway) {
                                PaymentGatewayType.MONCASH -> "https://moncashbutton.digicelgroup.com/pay"
                                PaymentGatewayType.NATCASH -> "https://natpay.natcom.com.ht/pay"
                                PaymentGatewayType.NOWPAYMENTS_CRYPTO -> "https://nowpayments.io/payment/?iid=${order.orderId.take(14)}"
                                PaymentGatewayType.STRIPE_CHECKOUT -> "https://checkout.stripe.com/pay/${order.orderId.take(14)}"
                            },
                            color = DarkTextSecondary,
                            fontSize = 11.sp,
                            fontFamily = FontFamily.Monospace,
                            maxLines = 1
                        )
                    }
                }

                Spacer(modifier = Modifier.height(14.dp))

                // Official Gateway Header Banner
                Surface(
                    shape = RoundedCornerShape(12.dp),
                    color = gatewayBrandColor,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(
                        modifier = Modifier.padding(14.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            val logoResId = when (order.gateway) {
                                PaymentGatewayType.MONCASH -> R.drawable.ic_moncash_logo
                                PaymentGatewayType.NATCASH -> R.drawable.ic_natcash_logo
                                PaymentGatewayType.NOWPAYMENTS_CRYPTO -> R.drawable.ic_crypto_logo
                                else -> null
                            }
                            if (logoResId != null) {
                                Surface(
                                    modifier = Modifier
                                        .size(28.dp)
                                        .clip(RoundedCornerShape(6.dp)),
                                    color = Color.White
                                ) {
                                    Image(
                                        painter = painterResource(id = logoResId),
                                        contentDescription = gatewayName,
                                        modifier = Modifier
                                            .fillMaxSize()
                                            .padding(1.dp),
                                        contentScale = ContentScale.Fit
                                    )
                                }
                            } else {
                                Icon(
                                    imageVector = Icons.Default.VerifiedUser,
                                    contentDescription = null,
                                    tint = Color.White,
                                    modifier = Modifier.size(20.dp)
                                )
                            }
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(
                                text = "Portail Officiel $gatewayName",
                                color = Color.White,
                                fontWeight = FontWeight.Bold,
                                fontSize = 15.sp
                            )
                        }
                        Spacer(modifier = Modifier.height(2.dp))
                        Text(
                            text = "Checkout hébergé sécurisé · Chiffrement 256-bit",
                            color = Color.White.copy(alpha = 0.85f),
                            fontSize = 11.sp
                        )
                    }
                }

                Spacer(modifier = Modifier.height(14.dp))

                // Order summary card
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    colors = CardDefaults.cardColors(containerColor = DarkSurfaceVariant),
                    border = CardDefaults.outlinedCardBorder().copy(
                        brush = androidx.compose.ui.graphics.SolidColor(DarkBorder)
                    )
                ) {
                    Column(
                        modifier = Modifier.padding(14.dp),
                        verticalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Text("Marchand", color = DarkTextMuted, fontSize = 12.sp)
                            Text("SmartOne IPTV", color = DarkTextPrimary, fontWeight = FontWeight.SemiBold, fontSize = 12.sp)
                        }

                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Text("Commande ID", color = DarkTextMuted, fontSize = 12.sp)
                            Text(order.orderId.take(18) + "...", color = BrandCyan, fontSize = 11.sp, fontFamily = FontFamily.Monospace)
                        }

                        HorizontalDivider(color = DarkBorder, modifier = Modifier.padding(vertical = 4.dp))

                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text("Total à débiter", color = DarkTextPrimary, fontWeight = FontWeight.Bold, fontSize = 13.sp)
                            Text(
                                text = "${String.format(Locale.US, "%,.2f", order.amount)} HTG",
                                color = BrandGold,
                                fontWeight = FontWeight.ExtraBold,
                                fontSize = 18.sp
                            )
                        }
                    }
                }

                Spacer(modifier = Modifier.height(12.dp))

                Text(
                    text = "🔒 Vos données bancaires et numéros de portefeuille restent sur la page sécurisée officielle de $gatewayName. Notre application ne collecte aucun code secret.",
                    color = DarkTextSecondary,
                    fontSize = 11.sp,
                    lineHeight = 15.sp
                )

                Spacer(modifier = Modifier.height(16.dp))

                if (isProcessing) {
                    CircularProgressIndicator(
                        color = gatewayBrandColor,
                        modifier = Modifier.size(32.dp)
                    )
                    Spacer(modifier = Modifier.height(6.dp))
                    Text(
                        text = "Validation sécurisée via Webhook IPN...",
                        color = DarkTextSecondary,
                        fontSize = 12.sp
                    )
                } else {
                    // Action 1: Confirm on Hosted Checkout (triggers signed webhook)
                    Button(
                        onClick = {
                            isProcessing = true
                            val result = when (order.gateway) {
                                PaymentGatewayType.NOWPAYMENTS_CRYPTO -> com.example.data.payment.PaymentBackendService.simulateSuccessfulNowPaymentsPayment(order.orderId)
                                PaymentGatewayType.STRIPE_CHECKOUT -> com.example.data.payment.PaymentBackendService.simulateSuccessfulStripePayment(order.orderId)
                                else -> com.example.data.payment.PaymentBackendService.simulateSuccessfulGatewayPayment(order.orderId)
                            }
                            isProcessing = false
                            onComplete(result.success, result.message)
                        },
                        colors = ButtonDefaults.buttonColors(containerColor = gatewayBrandColor),
                        modifier = Modifier
                            .fillMaxWidth()
                            .testTag("confirm_hosted_payment_button"),
                        shape = RoundedCornerShape(10.dp)
                    ) {
                        Icon(imageVector = Icons.Default.Check, contentDescription = null, modifier = Modifier.size(18.dp))
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = "Confirmer sur $gatewayName",
                            fontWeight = FontWeight.Bold,
                            fontSize = 14.sp
                        )
                    }

                    Spacer(modifier = Modifier.height(8.dp))

                    // Action 2: Open in external browser intent
                    OutlinedButton(
                        onClick = {
                            try {
                                val intent = Intent(Intent.ACTION_VIEW, Uri.parse(order.checkoutUrl))
                                context.startActivity(intent)
                            } catch (e: Exception) {
                                // fallback if no browser
                            }
                        },
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(10.dp)
                    ) {
                        Icon(imageVector = Icons.Default.OpenInBrowser, contentDescription = null, modifier = Modifier.size(16.dp))
                        Spacer(modifier = Modifier.width(6.dp))
                        Text("Ouvrir dans le navigateur externe", fontSize = 12.sp)
                    }

                    Spacer(modifier = Modifier.height(6.dp))

                    TextButton(
                        onClick = onCancel,
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Text("Annuler la transaction", color = StatusRed, fontSize = 12.sp)
                    }
                }
            }
        },
        confirmButton = {},
        containerColor = DarkSurface,
        titleContentColor = DarkTextPrimary,
        textContentColor = DarkTextPrimary
    )
}
