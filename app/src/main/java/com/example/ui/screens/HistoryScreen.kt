package com.example.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.*
import androidx.compose.material.icons.filled.ArrowDownward
import androidx.compose.material.icons.filled.ArrowUpward
import androidx.compose.material.icons.filled.DeleteOutline
import androidx.compose.material.icons.filled.DeleteSweep
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.model.TransactionRecord
import com.example.ui.theme.*
import java.util.Locale

@Composable
fun HistoryScreen(
    transactions: List<TransactionRecord>,
    onDeleteTransaction: (String) -> Unit = {},
    onClearAllTransactions: () -> Unit = {},
    modifier: Modifier = Modifier
) {
    var showClearConfirmDialog by remember { mutableStateOf(false) }
    var transactionToDelete by remember { mutableStateOf<TransactionRecord?>(null) }

    Box(modifier = modifier.fillMaxSize()) {
        LazyColumn(
            modifier = Modifier.fillMaxSize(),
            contentPadding = PaddingValues(16.dp),
            verticalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            item {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(bottom = 4.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "Historique des transactions",
                        color = DarkTextPrimary,
                        fontSize = 18.sp,
                        fontWeight = FontWeight.Bold
                    )

                    if (transactions.isNotEmpty()) {
                        IconButton(
                            onClick = { showClearConfirmDialog = true },
                            modifier = Modifier
                                .size(36.dp)
                                .testTag("clear_history_button")
                        ) {
                            Icon(
                                imageVector = Icons.Default.DeleteSweep,
                                contentDescription = "Effacer tout l'historique",
                                tint = StatusRed,
                                modifier = Modifier.size(22.dp)
                            )
                        }
                    }
                }
            }

            if (transactions.isEmpty()) {
                item {
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        colors = CardDefaults.cardColors(containerColor = DarkSurface)
                    ) {
                        Column(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(32.dp),
                            horizontalAlignment = Alignment.CenterHorizontally
                        ) {
                            Icon(
                                imageVector = Icons.AutoMirrored.Filled.ReceiptLong,
                                contentDescription = null,
                                tint = DarkTextMuted,
                                modifier = Modifier.size(48.dp)
                            )
                            Spacer(modifier = Modifier.height(12.dp))
                            Text(
                                text = "Aucune transaction enregistrée pour le moment.",
                                color = DarkTextSecondary,
                                fontSize = 14.sp
                            )
                        }
                    }
                }
            } else {
                items(transactions, key = { it.transactionId }) { tx ->
                    val isDeposit = tx.description.contains("dépôt", ignoreCase = true) || tx.planId == null

                    Card(
                        modifier = Modifier
                            .fillMaxWidth()
                            .testTag("tx_item_${tx.transactionId}"),
                        colors = CardDefaults.cardColors(containerColor = DarkSurface),
                        border = CardDefaults.outlinedCardBorder().copy(
                            brush = androidx.compose.ui.graphics.SolidColor(DarkBorder)
                        ),
                        shape = RoundedCornerShape(12.dp)
                    ) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(12.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Box(
                                modifier = Modifier
                                    .size(38.dp)
                                    .clip(CircleShape)
                                    .background(
                                        if (isDeposit) StatusGreen.copy(alpha = 0.15f) else BrandCyan.copy(alpha = 0.15f)
                                    ),
                                contentAlignment = Alignment.Center
                            ) {
                                Icon(
                                    imageVector = if (isDeposit) Icons.Default.ArrowDownward else Icons.Default.ArrowUpward,
                                    contentDescription = null,
                                    tint = if (isDeposit) StatusGreen else BrandCyan,
                                    modifier = Modifier.size(18.dp)
                                )
                            }

                            Spacer(modifier = Modifier.width(10.dp))

                            Column(modifier = Modifier.weight(1f)) {
                                Text(
                                    text = tx.description,
                                    color = DarkTextPrimary,
                                    fontWeight = FontWeight.SemiBold,
                                    fontSize = 13.sp,
                                    maxLines = 2
                                )
                                Spacer(modifier = Modifier.height(2.dp))
                                Text(
                                    text = "${tx.paymentMethod} • ${tx.createdAt}",
                                    color = DarkTextMuted,
                                    fontSize = 11.sp
                                )
                            }

                            Spacer(modifier = Modifier.width(6.dp))

                            Column(horizontalAlignment = Alignment.End) {
                                Text(
                                    text = "${if (isDeposit) "+" else "-"}${String.format(Locale.US, "%,.2f", tx.amount)} HTG",
                                    color = if (isDeposit) StatusGreen else BrandGold,
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 13.sp
                                )
                                Surface(
                                    shape = RoundedCornerShape(4.dp),
                                    color = StatusGreen.copy(alpha = 0.15f)
                                ) {
                                    Text(
                                        text = tx.status,
                                        color = StatusGreen,
                                        fontSize = 9.sp,
                                        fontWeight = FontWeight.Bold,
                                        modifier = Modifier.padding(horizontal = 5.dp, vertical = 2.dp)
                                    )
                                }
                            }

                            // Individual Delete Icon Button
                            IconButton(
                                onClick = { transactionToDelete = tx },
                                modifier = Modifier
                                    .size(32.dp)
                                    .padding(start = 2.dp)
                            ) {
                                Icon(
                                    imageVector = Icons.Default.DeleteOutline,
                                    contentDescription = "Supprimer la transaction",
                                    tint = DarkTextMuted,
                                    modifier = Modifier.size(18.dp)
                                )
                            }
                        }
                    }
                }
            }
        }
    }

    // Clear All Confirmation Dialog
    if (showClearConfirmDialog) {
        AlertDialog(
            onDismissRequest = { showClearConfirmDialog = false },
            title = {
                Text(
                    text = "Effacer tout l'historique ?",
                    fontWeight = FontWeight.Bold,
                    fontSize = 17.sp,
                    color = DarkTextPrimary
                )
            },
            text = {
                Text(
                    text = "Èske ou sèten ou vle efase tout tranzaksyon ki anrejistre yo ? Aksyon sa a pa ka defèt.",
                    color = DarkTextSecondary,
                    fontSize = 13.sp
                )
            },
            confirmButton = {
                Button(
                    onClick = {
                        showClearConfirmDialog = false
                        onClearAllTransactions()
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = StatusRed)
                ) {
                    Text("Efase tout", color = Color.White, fontWeight = FontWeight.Bold)
                }
            },
            dismissButton = {
                TextButton(onClick = { showClearConfirmDialog = false }) {
                    Text("Anile", color = DarkTextSecondary)
                }
            },
            containerColor = DarkSurface,
            textContentColor = DarkTextPrimary,
            titleContentColor = DarkTextPrimary
        )
    }

    // Single Transaction Delete Confirmation Dialog
    transactionToDelete?.let { tx ->
        AlertDialog(
            onDismissRequest = { transactionToDelete = null },
            title = {
                Text(
                    text = "Supprimer cette transaction ?",
                    fontWeight = FontWeight.Bold,
                    fontSize = 17.sp,
                    color = DarkTextPrimary
                )
            },
            text = {
                Text(
                    text = "Èske ou vle retire tranzaksyon sa a nan istorik ou ?\n\n« ${tx.description} »\nMontan : ${String.format(Locale.US, "%,.2f", tx.amount)} HTG",
                    color = DarkTextSecondary,
                    fontSize = 13.sp
                )
            },
            confirmButton = {
                Button(
                    onClick = {
                        val id = tx.transactionId
                        transactionToDelete = null
                        onDeleteTransaction(id)
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = StatusRed)
                ) {
                    Text("Supprimer", color = Color.White, fontWeight = FontWeight.Bold)
                }
            },
            dismissButton = {
                TextButton(onClick = { transactionToDelete = null }) {
                    Text("Anile", color = DarkTextSecondary)
                }
            },
            containerColor = DarkSurface,
            textContentColor = DarkTextPrimary,
            titleContentColor = DarkTextPrimary
        )
    }
}
