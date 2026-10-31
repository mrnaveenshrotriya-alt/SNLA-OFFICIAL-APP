package com.example.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.model.ClientEntity
import com.example.data.model.ClientInvoiceEntity
import com.example.service.PdfReportGenerator
import com.example.service.WhatsAppHelper
import com.example.ui.components.StatusBadge
import com.example.ui.theme.*

@Composable
fun FeesPaymentScreen(
    invoices: List<ClientInvoiceEntity>,
    clients: List<ClientEntity>,
    isHindi: Boolean,
    onAddInvoice: (ClientInvoiceEntity) -> Unit,
    onUpdateInvoice: (ClientInvoiceEntity) -> Unit
) {
    val context = LocalContext.current
    var showAddDialog by remember { mutableStateOf(false) }
    var selectedFilter by remember { mutableStateOf("All") }

    val filtered = invoices.filter {
        when (selectedFilter) {
            "Pending" -> it.paymentStatus != "Paid"
            "Paid" -> it.paymentStatus == "Paid"
            else -> true
        }
    }

    val totalPending = invoices.filter { it.paymentStatus != "Paid" }.sumOf { it.pendingAmount }

    Scaffold(
        floatingActionButton = {
            FloatingActionButton(
                onClick = { showAddDialog = true },
                containerColor = DeepMaroon,
                contentColor = RoyalGoldLight,
                modifier = Modifier.testTag("add_invoice_fab")
            ) {
                Icon(Icons.Default.Add, contentDescription = "Create Invoice")
            }
        }
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .background(IvoryBackground)
                .testTag("fees_screen")
        ) {
            // Header summary banner
            Surface(color = Color.White, tonalElevation = 2.dp, modifier = Modifier.fillMaxWidth()) {
                Column(modifier = Modifier.padding(14.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column {
                            Text(
                                text = if (isHindi) "क्लाइंट फीस एवं इनवॉइसिंग (Billing & Ledgers)" else "Client Invoicing & Payment Ledgers",
                                fontSize = 15.sp,
                                fontWeight = FontWeight.Bold,
                                color = DeepMaroon
                            )
                            Text(
                                text = "GST Filing • ITR • Tax Audit • Retainers • UPI / NEFT",
                                fontSize = 11.sp,
                                color = Color.Gray
                            )
                        }

                        Surface(
                            color = MaroonContainer,
                            shape = RoundedCornerShape(8.dp),
                            border = androidx.compose.foundation.BorderStroke(1.dp, DeepMaroon.copy(alpha = 0.3f))
                        ) {
                            Column(modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp)) {
                                Text(if (isHindi) "कुल बकाया" else "Total Due", fontSize = 10.sp, color = DeepMaroon)
                                Text(
                                    text = "₹ ${"%,.2f".format(totalPending)}",
                                    fontSize = 14.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = DeepMaroon
                                )
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(10.dp))

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        listOf("All", "Pending", "Paid").forEach { f ->
                            FilterChip(
                                selected = selectedFilter == f,
                                onClick = { selectedFilter = f },
                                label = { Text(f, fontSize = 11.sp) },
                                colors = FilterChipDefaults.filterChipColors(
                                    selectedContainerColor = DeepMaroon,
                                    selectedLabelColor = RoyalGoldLight
                                )
                            )
                        }
                    }
                }
            }

            if (filtered.isEmpty()) {
                Box(modifier = Modifier.fillMaxSize().padding(32.dp), contentAlignment = Alignment.Center) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Icon(Icons.Default.Receipt, contentDescription = null, tint = Color.Gray, modifier = Modifier.size(48.dp))
                        Spacer(modifier = Modifier.height(8.dp))
                        Text("No invoices match.", color = Color.Gray)
                    }
                }
            } else {
                LazyColumn(
                    modifier = Modifier.fillMaxSize(),
                    contentPadding = PaddingValues(12.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    items(filtered, key = { it.invoiceNumber }) { inv ->
                        Surface(
                            modifier = Modifier.fillMaxWidth(),
                            shape = RoundedCornerShape(12.dp),
                            color = Color.White,
                            border = androidx.compose.foundation.BorderStroke(1.dp, IvoryOutline),
                            tonalElevation = 2.dp
                        ) {
                            Column(modifier = Modifier.padding(14.dp)) {
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Column(modifier = Modifier.weight(1f)) {
                                        Text(
                                            text = inv.clientName,
                                            fontSize = 14.sp,
                                            fontWeight = FontWeight.Bold,
                                            color = DeepMaroon
                                        )
                                        Text(
                                            text = "Inv #${inv.invoiceNumber} • ${inv.invoiceDate}",
                                            fontSize = 11.sp,
                                            color = Color.DarkGray
                                        )
                                    }
                                    StatusBadge(status = inv.paymentStatus)
                                }

                                Spacer(modifier = Modifier.height(6.dp))
                                Text(
                                    text = inv.serviceType,
                                    fontSize = 12.sp,
                                    color = RoyalGoldDark,
                                    fontWeight = FontWeight.SemiBold
                                )

                                Spacer(modifier = Modifier.height(8.dp))
                                Divider(color = IvoryOutlineVariant, thickness = 0.8.dp)
                                Spacer(modifier = Modifier.height(8.dp))

                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Column {
                                        Text(
                                            text = "Total: ₹${"%,.2f".format(inv.totalAmount)}",
                                            fontSize = 12.sp,
                                            color = Color.DarkGray
                                        )
                                        Text(
                                            text = "Pending: ₹${"%,.2f".format(inv.pendingAmount)}",
                                            fontSize = 13.sp,
                                            fontWeight = FontWeight.Bold,
                                            color = if (inv.pendingAmount > 0) StatusOverdueRed else StatusFiledGreen
                                        )
                                    }

                                    Row(verticalAlignment = Alignment.CenterVertically) {
                                        // WhatsApp Fee Reminder
                                        if (inv.pendingAmount > 0) {
                                            IconButton(onClick = {
                                                val msg = WhatsAppHelper.buildMessage(
                                                    type = WhatsAppHelper.MessageType.PAYMENT_REMINDER,
                                                    clientName = inv.clientName,
                                                    returnOrDocType = inv.serviceType,
                                                    period = inv.invoiceDate,
                                                    dueDate = inv.paymentDueDate,
                                                    amount = inv.pendingAmount,
                                                    isHindi = isHindi
                                                )
                                                WhatsAppHelper.openWhatsApp(context, "9829012345", msg)
                                            }) {
                                                Icon(Icons.Default.Chat, contentDescription = "Send WhatsApp Reminder", tint = StatusFiledGreen)
                                            }
                                        }

                                        // Print PDF Invoice
                                        IconButton(onClick = {
                                            val file = PdfReportGenerator.generateInvoicePdf(context, inv)
                                            PdfReportGenerator.viewOrSharePdf(context, file)
                                        }) {
                                            Icon(Icons.Default.Print, contentDescription = "Print PDF Invoice", tint = DeepMaroon)
                                        }

                                        // Quick Mark as Paid
                                        if (inv.paymentStatus != "Paid") {
                                            Button(
                                                onClick = {
                                                    onUpdateInvoice(
                                                        inv.copy(
                                                            amountReceived = inv.totalAmount,
                                                            pendingAmount = 0.0,
                                                            paymentStatus = "Paid",
                                                            transactionRef = "UPI-PAID-${System.currentTimeMillis() % 100000}"
                                                        )
                                                    )
                                                },
                                                colors = ButtonDefaults.buttonColors(containerColor = StatusFiledGreen, contentColor = Color.White),
                                                shape = RoundedCornerShape(8.dp),
                                                contentPadding = PaddingValues(horizontal = 8.dp, vertical = 2.dp)
                                            ) {
                                                Text("Mark Paid", fontSize = 11.sp)
                                            }
                                        }
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }
    }

    if (showAddDialog) {
        val clientList = clients.ifEmpty {
            listOf(ClientEntity("DEFAULT", "Marudhar Textiles", "Rajesh Sharma", "Pvt Ltd", "Manufacturer", "", mobileNumber = "9829012345", whatsAppNumber = "9829012345", emailAddress = "", pan = "AABCM1234F"))
        }
        var selectedClient by remember { mutableStateOf(clientList.first()) }
        var serviceType by remember { mutableStateOf("Quarterly GST Filing & Tax Advisory Retainer") }
        var totalAmount by remember { mutableStateOf("7500") }
        var dueDate by remember { mutableStateOf("2024-11-15") }

        AlertDialog(
            onDismissRequest = { showAddDialog = false },
            title = { Text("Generate New Invoice") },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    OutlinedTextField(
                        value = selectedClient.businessName,
                        onValueChange = {},
                        label = { Text("Client") },
                        readOnly = true,
                        modifier = Modifier.fillMaxWidth()
                    )
                    OutlinedTextField(
                        value = serviceType,
                        onValueChange = { serviceType = it },
                        label = { Text("Service Description") },
                        modifier = Modifier.fillMaxWidth()
                    )
                    OutlinedTextField(
                        value = totalAmount,
                        onValueChange = { totalAmount = it },
                        label = { Text("Total Fees (₹)") },
                        modifier = Modifier.fillMaxWidth()
                    )
                    OutlinedTextField(
                        value = dueDate,
                        onValueChange = { dueDate = it },
                        label = { Text("Payment Due Date") },
                        modifier = Modifier.fillMaxWidth()
                    )
                }
            },
            confirmButton = {
                Button(onClick = {
                    val amt = totalAmount.toDoubleOrNull() ?: 0.0
                    val inv = ClientInvoiceEntity(
                        invoiceNumber = "INV-2024-${System.currentTimeMillis() % 10000}",
                        clientId = selectedClient.clientId,
                        clientName = selectedClient.businessName,
                        serviceType = serviceType,
                        invoiceDate = "2024-10-31",
                        paymentDueDate = dueDate,
                        totalAmount = amt,
                        amountReceived = 0.0,
                        pendingAmount = amt,
                        paymentStatus = "Unpaid"
                    )
                    onAddInvoice(inv)
                    showAddDialog = false
                }) {
                    Text("Create Invoice")
                }
            },
            dismissButton = {
                TextButton(onClick = { showAddDialog = false }) { Text("Cancel") }
            }
        )
    }
}
