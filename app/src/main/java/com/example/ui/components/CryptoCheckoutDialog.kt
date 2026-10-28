package com.example.ui.components

import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import android.widget.Toast
import androidx.compose.animation.*
import androidx.compose.foundation.Canvas
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
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.payment.OrderStatus
import com.example.data.payment.PaymentBackendService
import com.example.data.payment.PaymentOrder
import com.example.ui.theme.*
import kotlinx.coroutines.delay
import java.util.Locale

@Composable
fun CryptoCheckoutDialog(
    order: PaymentOrder,
    onSuccess: (amountHtg: Double) -> Unit,
    onDismiss: () -> Unit
) {
    val context = LocalContext.current

    // Crypto details (USDT TRC20)
    val depositAddress = remember(order.orderId) {
        "TYk" + order.orderId.replace("-", "").take(10) + "7xQz8wMv" + (1000..9999).random()
    }
    val amountUsd = order.amount / 132.0 // 1 USD = 132 HTG
    val networkName = "USDT - TRC20 (Tron Network)"

    // 20-minute expiration countdown timer (1200 seconds)
    var secondsLeft by remember { mutableIntStateOf(1200) }
    var paymentStatus by remember { mutableStateOf("waiting") } // "waiting", "confirming", "finished", "partially_paid"
    var isSimulatingPayment by remember { mutableStateOf(false) }

    // Live countdown effect
    LaunchedEffect(key1 = Unit) {
        while (secondsLeft > 0 && paymentStatus != "finished") {
            delay(1000L)
            secondsLeft--
        }
    }

    // Auto status polling effect (simulate live blockchain listener)
    LaunchedEffect(key1 = order.orderId, key2 = paymentStatus) {
        while (paymentStatus == "waiting" || paymentStatus == "confirming") {
            delay(3500L)
            val currentOrder = PaymentBackendService.getOrder(order.orderId)
            if (currentOrder?.status == OrderStatus.COMPLETED) {
                paymentStatus = "finished"
                delay(1200L)
                onSuccess(order.amount)
            }
        }
    }

    AlertDialog(
        onDismissRequest = {
            if (paymentStatus != "finished") onDismiss()
        },
        title = null,
        text = {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .testTag("crypto_checkout_dialog"),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                // Header: Crypto Branding & Status
                Surface(
                    shape = RoundedCornerShape(12.dp),
                    color = Color(0xFF059669),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Row(
                        modifier = Modifier.padding(14.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Surface(
                            modifier = Modifier
                                .size(36.dp)
                                .clip(RoundedCornerShape(8.dp)),
                            color = Color(0xFF181A20)
                        ) {
                            Image(
                                painter = painterResource(id = R.drawable.ic_crypto_logo),
                                contentDescription = "Logo Crypto",
                                modifier = Modifier
                                    .fillMaxSize()
                                    .padding(2.dp),
                                contentScale = ContentScale.Fit
                            )
                        }

                        Spacer(modifier = Modifier.width(10.dp))

                        Column(modifier = Modifier.weight(1f)) {
                            Text(
                                text = "NOWPayments Crypto Gateway",
                                color = Color.White,
                                fontWeight = FontWeight.Bold,
                                fontSize = 15.sp
                            )
                            Text(
                                text = networkName,
                                color = BrandCyan,
                                fontSize = 12.sp,
                                fontWeight = FontWeight.SemiBold
                            )
                        }

                        // Countdown Badge
                        Surface(
                            shape = RoundedCornerShape(6.dp),
                            color = if (secondsLeft < 180) StatusRed else Color(0x33000000)
                        ) {
                            val min = secondsLeft / 60
                            val sec = secondsLeft % 60
                            Text(
                                text = String.format(Locale.US, "%02d:%02d", min, sec),
                                color = Color.White,
                                fontSize = 12.sp,
                                fontWeight = FontWeight.Bold,
                                fontFamily = FontFamily.Monospace,
                                modifier = Modifier.padding(horizontal = 6.dp, vertical = 3.dp)
                            )
                        }
                    }
                }

                Spacer(modifier = Modifier.height(14.dp))

                AnimatedContent(
                    targetState = paymentStatus,
                    label = "crypto_payment_state"
                ) { status ->
                    when (status) {
                        "finished" -> {
                            // Siksè konplè !
                            Column(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(vertical = 16.dp),
                                horizontalAlignment = Alignment.CenterHorizontally
                            ) {
                                Box(
                                    modifier = Modifier
                                        .size(64.dp)
                                        .clip(CircleShape)
                                        .background(StatusGreen.copy(alpha = 0.2f)),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Icon(
                                        imageVector = Icons.Default.CheckCircle,
                                        contentDescription = null,
                                        tint = StatusGreen,
                                        modifier = Modifier.size(42.dp)
                                    )
                                }

                                Spacer(modifier = Modifier.height(12.dp))

                                Text(
                                    text = "Peman an reyalize ak siksè ! 🎉",
                                    color = StatusGreen,
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 17.sp,
                                    textAlign = TextAlign.Center
                                )

                                Spacer(modifier = Modifier.height(6.dp))

                                Text(
                                    text = "+${String.format(Locale.US, "%,.2f", order.amount)} HTG ajoute sou kont ou.",
                                    color = DarkTextPrimary,
                                    fontWeight = FontWeight.SemiBold,
                                    fontSize = 14.sp
                                )

                                Spacer(modifier = Modifier.height(16.dp))

                                Button(
                                    onClick = { onSuccess(order.amount) },
                                    colors = ButtonDefaults.buttonColors(containerColor = StatusGreen),
                                    modifier = Modifier.fillMaxWidth()
                                ) {
                                    Text("Fèmen & Wè solde mwen")
                                }
                            }
                        }

                        "partially_paid" -> {
                            // Underpaid warning
                            Card(
                                modifier = Modifier.fillMaxWidth(),
                                colors = CardDefaults.cardColors(containerColor = DarkSurfaceVariant),
                                border = CardDefaults.outlinedCardBorder().copy(
                                    brush = androidx.compose.ui.graphics.SolidColor(StatusAmber)
                                )
                            ) {
                                Column(modifier = Modifier.padding(14.dp)) {
                                    Row(verticalAlignment = Alignment.CenterVertically) {
                                        Icon(imageVector = Icons.Default.Warning, contentDescription = null, tint = StatusAmber)
                                        Spacer(modifier = Modifier.width(8.dp))
                                        Text("Peman Enkonplè (Underpaid)", color = StatusAmber, fontWeight = FontWeight.Bold, fontSize = 14.sp)
                                    }
                                    Spacer(modifier = Modifier.height(6.dp))
                                    Text(
                                        text = "Ou voye mwens ke montan ki te mande a. Tanpri voye rès la sou menm adrès la anvan kwonomèt la fini.",
                                        color = DarkTextSecondary,
                                        fontSize = 12.sp
                                    )
                                }
                            }
                        }

                        else -> {
                            // "waiting" or "confirming"
                            Column(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalAlignment = Alignment.CenterHorizontally
                            ) {
                                // QR Code Drawing Simulation
                                Box(
                                    modifier = Modifier
                                        .size(150.dp)
                                        .clip(RoundedCornerShape(12.dp))
                                        .background(Color.White)
                                        .padding(8.dp),
                                    contentAlignment = Alignment.Center
                                ) {
                                    MockQRCodeCanvas(depositAddress)
                                }

                                Spacer(modifier = Modifier.height(12.dp))

                                // Montan Egzak
                                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                    Text(
                                        text = "Montan Egzak pou voye :",
                                        color = DarkTextMuted,
                                        fontSize = 12.sp
                                    )
                                    Text(
                                        text = "${String.format(Locale.US, "%.2f", amountUsd)} USDT",
                                        color = BrandCyan,
                                        fontWeight = FontWeight.ExtraBold,
                                        fontSize = 24.sp
                                    )
                                    Text(
                                        text = "≈ ${String.format(Locale.US, "%,.0f", order.amount)} HTG",
                                        color = DarkTextSecondary,
                                        fontSize = 12.sp
                                    )
                                }

                                Spacer(modifier = Modifier.height(10.dp))

                                // Adrès Wallet & Bouton Kopye
                                Surface(
                                    shape = RoundedCornerShape(8.dp),
                                    color = DarkSurfaceVariant,
                                    border = CardDefaults.outlinedCardBorder().copy(
                                        brush = androidx.compose.ui.graphics.SolidColor(DarkBorder)
                                    ),
                                    modifier = Modifier.fillMaxWidth()
                                ) {
                                    Row(
                                        modifier = Modifier.padding(10.dp),
                                        verticalAlignment = Alignment.CenterVertically,
                                        horizontalArrangement = Arrangement.SpaceBetween
                                    ) {
                                        Column(modifier = Modifier.weight(1f)) {
                                            Text(
                                                text = "Adrès depo tanporè :",
                                                color = DarkTextMuted,
                                                fontSize = 10.sp
                                            )
                                            Text(
                                                text = depositAddress,
                                                color = Color.White,
                                                fontSize = 12.sp,
                                                fontFamily = FontFamily.Monospace,
                                                fontWeight = FontWeight.Medium
                                            )
                                        }

                                        IconButton(
                                            onClick = {
                                                val clipboard = context.getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
                                                clipboard.setPrimaryClip(ClipData.newPlainText("NOWPayments Address", depositAddress))
                                                Toast.makeText(context, "Adrès la kopye ak siksè ! 📋", Toast.LENGTH_SHORT).show()
                                            },
                                            modifier = Modifier.testTag("copy_crypto_address_button")
                                        ) {
                                            Icon(
                                                imageVector = Icons.Default.ContentCopy,
                                                contentDescription = "Kopye",
                                                tint = BrandCyan
                                            )
                                        }
                                    }
                                }

                                Spacer(modifier = Modifier.height(10.dp))

                                // Avètisman Rezo
                                Card(
                                    modifier = Modifier.fillMaxWidth(),
                                    colors = CardDefaults.cardColors(containerColor = StatusAmber.copy(alpha = 0.12f)),
                                    border = CardDefaults.outlinedCardBorder().copy(
                                        brush = androidx.compose.ui.graphics.SolidColor(StatusAmber.copy(alpha = 0.4f))
                                    )
                                ) {
                                    Row(
                                        modifier = Modifier.padding(10.dp),
                                        verticalAlignment = Alignment.CenterVertically
                                    ) {
                                        Icon(
                                            imageVector = Icons.Default.WarningAmber,
                                            contentDescription = null,
                                            tint = StatusAmber,
                                            modifier = Modifier.size(18.dp)
                                        )
                                        Spacer(modifier = Modifier.width(8.dp))
                                        Text(
                                            text = "Avètisman: Voye sèlman USDT sou rezo TRC20 la. Si w itilize yon lòt rezo (eg: ERC-20), lajan w ap pèdi nèt.",
                                            color = StatusAmber,
                                            fontSize = 11.sp,
                                            lineHeight = 14.sp
                                        )
                                    }
                                }

                                Spacer(modifier = Modifier.height(14.dp))

                                // Status auto-detection banner
                                Row(
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.Center
                                ) {
                                    CircularProgressIndicator(
                                        modifier = Modifier.size(16.dp),
                                        color = BrandCyan,
                                        strokeWidth = 2.dp
                                    )
                                    Spacer(modifier = Modifier.width(8.dp))
                                    Text(
                                        text = if (paymentStatus == "confirming") "Tranzaksyon detekte, konfimasyon an kou..." else "Ap tann peman sou blockchain...",
                                        color = DarkTextSecondary,
                                        fontSize = 12.sp
                                    )
                                }

                                Spacer(modifier = Modifier.height(12.dp))

                                // Bouton simulation Webhook pou tès
                                Button(
                                    onClick = {
                                        isSimulatingPayment = true
                                        paymentStatus = "confirming"
                                        val result = PaymentBackendService.simulateSuccessfulNowPaymentsPayment(order.orderId)
                                        if (result.success) {
                                            paymentStatus = "finished"
                                        }
                                        isSimulatingPayment = false
                                    },
                                    colors = ButtonDefaults.buttonColors(containerColor = BrandBluePrimary),
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .testTag("simulate_crypto_payment_button"),
                                    shape = RoundedCornerShape(8.dp)
                                ) {
                                    Icon(imageVector = Icons.Default.Check, contentDescription = null, modifier = Modifier.size(16.dp))
                                    Spacer(modifier = Modifier.width(6.dp))
                                    Text("Simile Peman Resevwa (Webhook IPN)", fontSize = 12.sp)
                                }

                                Spacer(modifier = Modifier.height(6.dp))

                                TextButton(onClick = onDismiss) {
                                    Text("Fèmen fenèt la", color = DarkTextSecondary, fontSize = 12.sp)
                                }
                            }
                        }
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

/**
 * Geometric Mock QR Code Canvas pattern
 */
@Composable
fun MockQRCodeCanvas(seed: String) {
    Canvas(modifier = Modifier.fillMaxSize()) {
        val gridSize = 15
        val cellSize = size.width / gridSize
        val hash = seed.hashCode()

        for (row in 0 until gridSize) {
            for (col in 0 until gridSize) {
                // Corner positioning squares
                val isCorner = (row < 4 && col < 4) || (row < 4 && col >= gridSize - 4) || (row >= gridSize - 4 && col < 4)
                val isCornerBorder = isCorner && (row == 0 || row == 3 || col == 0 || col == 3 ||
                        row == gridSize - 1 || row == gridSize - 4 || col == gridSize - 1 || col == gridSize - 4)
                val isCornerCenter = (row in 1..2 && col in 1..2) ||
                        (row in 1..2 && col in gridSize - 3 until gridSize - 1) ||
                        (row in gridSize - 3 until gridSize - 1 && col in 1..2)

                val bit = ((hash + (row * 31) + (col * 17)) % 2) == 0

                if (isCornerBorder || isCornerCenter || (!isCorner && bit)) {
                    drawRect(
                        color = Color.Black,
                        topLeft = Offset(col * cellSize, row * cellSize),
                        size = Size(cellSize, cellSize)
                    )
                }
            }
        }
    }
}
