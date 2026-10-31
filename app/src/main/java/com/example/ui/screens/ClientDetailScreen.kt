package com.example.ui.screens

import android.content.Intent
import android.net.Uri
import androidx.activity.compose.BackHandler
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
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
import com.example.data.model.*
import com.example.service.PdfReportGenerator
import com.example.service.WhatsAppHelper
import com.example.ui.components.StatusBadge
import com.example.ui.theme.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ClientDetailScreen(
    client: ClientEntity,
    gstReturns: List<GstReturnEntity>,
    itrReturns: List<ItrReturnEntity>,
    invoices: List<ClientInvoiceEntity>,
    documents: List<ClientDocumentEntity>,
    isHindi: Boolean,
    onBack: () -> Unit,
    onEditClient: (ClientEntity) -> Unit,
    onDeleteClient: (ClientEntity) -> Unit,
    onAddGstReturn: (GstReturnEntity) -> Unit,
    onMarkGstFiled: (Long, String) -> Unit,
    onAddInvoice: (ClientInvoiceEntity) -> Unit
) {
    BackHandler { onBack() }
    val context = LocalContext.current
    var selectedTab by remember { mutableStateOf(0) }
    var showAddReturnDialog by remember { mutableStateOf(false) }
    var showAddInvoiceDialog by remember { mutableStateOf(false) }

    val clientGst = gstReturns.filter { it.clientId == client.clientId }
    val clientItr = itrReturns.filter { it.clientId == client.clientId }
    val clientInvoices = invoices.filter { it.clientId == client.clientId }
    val clientDocs = documents.filter { it.clientId == client.clientId }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Column {
                        Text(
                            text = client.businessName,
                            fontSize = 16.sp,
                            fontWeight = FontWeight.Bold,
                            color = Color.White
                        )
                        Text(
                            text = "Client ID: ${client.clientId} • ${client.status}",
                            fontSize = 11.sp,
                            color = RoyalGoldLight
                        )
                    }
                },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = "Back",
                            tint = Color.White
                        )
                    }
                },
                actions = {
                    IconButton(onClick = {
                        val msg = WhatsAppHelper.buildMessage(
                            type = WhatsAppHelper.MessageType.GST_DUE,
                            clientName = client.proprietorName,
                            returnOrDocType = "GSTR-1 & 3B",
                            period = "Current Month",
                            dueDate = "Upcoming Due Date",
                            isHindi = isHindi
                        )
                        WhatsAppHelper.openWhatsApp(context, client.whatsAppNumber.ifEmpty { client.mobileNumber }, msg)
                    }) {
                        Icon(Icons.Default.Share, contentDescription = "Send WhatsApp Reminder", tint = RoyalGoldLight)
                    }
                    IconButton(onClick = { onDeleteClient(client) }) {
                        Icon(Icons.Default.Delete, contentDescription = "Delete Client", tint = StatusOverdueRed)
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = DeepMaroon
                )
            )
        }
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .background(IvoryBackground)
                .testTag("client_detail_screen")
        ) {
            // Client Quick Contact & Status Banner
            Surface(
                color = Color.White,
                tonalElevation = 2.dp,
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(modifier = Modifier.padding(14.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column {
                            Text(
                                text = "Proprietor / Director: ${client.proprietorName}",
                                fontSize = 13.sp,
                                fontWeight = FontWeight.SemiBold,
                                color = DeepMaroon
                            )
                            Text(
                                text = "Constitution: ${client.constitution} (${client.businessType})",
                                fontSize = 12.sp,
                                color = Color.DarkGray
                            )
                            Text(
                                text = "PAN: ${client.pan} | GSTIN: ${client.primaryGstin.ifEmpty { "Unregistered" }}",
                                fontSize = 11.sp,
                                color = RoyalGoldDark,
                                fontWeight = FontWeight.Bold
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(10.dp))

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Button(
                            onClick = {
                                val intent = Intent(Intent.ACTION_DIAL, Uri.parse("tel:${client.mobileNumber}"))
                                context.startActivity(intent)
                            },
                            colors = ButtonDefaults.buttonColors(containerColor = DeepMaroon, contentColor = Color.White),
                            shape = RoundedCornerShape(8.dp),
                            modifier = Modifier.weight(1f),
                            contentPadding = PaddingValues(vertical = 6.dp)
                        ) {
                            Icon(Icons.Default.Call, contentDescription = null, modifier = Modifier.size(14.dp))
                            Spacer(modifier = Modifier.width(4.dp))
                            Text("Call", fontSize = 12.sp)
                        }

                        Button(
                            onClick = {
                                val msg = WhatsAppHelper.buildMessage(
                                    WhatsAppHelper.MessageType.GST_DUE,
                                    client.proprietorName,
                                    "GST Return",
                                    "Current Period",
                                    "Due Date",
                                    isHindi = isHindi
                                )
                                WhatsAppHelper.openWhatsApp(context, client.whatsAppNumber, msg)
                            },
                            colors = ButtonDefaults.buttonColors(containerColor = StatusFiledGreen, contentColor = Color.White),
                            shape = RoundedCornerShape(8.dp),
                            modifier = Modifier.weight(1f),
                            contentPadding = PaddingValues(vertical = 6.dp)
                        ) {
                            Icon(Icons.Default.Chat, contentDescription = null, modifier = Modifier.size(14.dp))
                            Spacer(modifier = Modifier.width(4.dp))
                            Text("WhatsApp", fontSize = 12.sp)
                        }

                        OutlinedButton(
                            onClick = {
                                val msg = WhatsAppHelper.buildMessage(
                                    WhatsAppHelper.MessageType.DOCUMENT_REQUEST,
                                    client.proprietorName,
                                    "ITR & GST Records",
                                    "FY 2024-25",
                                    "Urgent",
                                    isHindi = isHindi
                                )
                                WhatsAppHelper.sendEmail(context, client.emailAddress, "Document Request - Shree Navya Lekha Associates", msg)
                            },
                            shape = RoundedCornerShape(8.dp),
                            modifier = Modifier.weight(1f),
                            contentPadding = PaddingValues(vertical = 6.dp)
                        ) {
                            Icon(Icons.Default.Email, contentDescription = null, modifier = Modifier.size(14.dp), tint = DeepMaroon)
                            Spacer(modifier = Modifier.width(4.dp))
                            Text("Email", fontSize = 12.sp, color = DeepMaroon)
                        }
                    }
                }
            }

            // Tab Navigation (GST Returns, ITR, Invoices, Documents)
            TabRow(
                selectedTabIndex = selectedTab,
                containerColor = Color.White,
                contentColor = DeepMaroon
            ) {
                Tab(
                    selected = selectedTab == 0,
                    onClick = { selectedTab = 0 },
                    text = { Text("GST (${clientGst.size})", fontSize = 11.sp) }
                )
                Tab(
                    selected = selectedTab == 1,
                    onClick = { selectedTab = 1 },
                    text = { Text("ITR (${clientItr.size})", fontSize = 11.sp) }
                )
                Tab(
                    selected = selectedTab == 2,
                    onClick = { selectedTab = 2 },
                    text = { Text("Invoices (${clientInvoices.size})", fontSize = 11.sp) }
                )
                Tab(
                    selected = selectedTab == 3,
                    onClick = { selectedTab = 3 },
                    text = { Text("Docs (${clientDocs.size})", fontSize = 11.sp) }
                )
            }

            // Tab Content
            when (selectedTab) {
                0 -> {
                    // GST Returns List
                    Box(modifier = Modifier.fillMaxSize()) {
                        if (clientGst.isEmpty()) {
                            EmptyComplianceState(title = "No GST returns added", actionLabel = "+ Add GST Return") {
                                showAddReturnDialog = true
                            }
                        } else {
                            LazyColumn(
                                modifier = Modifier.fillMaxSize(),
                                contentPadding = PaddingValues(12.dp),
                                verticalArrangement = Arrangement.spacedBy(8.dp)
                            ) {
                                items(clientGst) { ret ->
                                    GstReturnCard(
                                        ret = ret,
                                        onMarkFiled = { arn -> onMarkGstFiled(ret.id, arn) }
                                    )
                                }
                            }
                        }
                        FloatingActionButton(
                            onClick = { showAddReturnDialog = true },
                            containerColor = DeepMaroon,
                            contentColor = RoyalGoldLight,
                            modifier = Modifier
                                .align(Alignment.BottomEnd)
                                .padding(16.dp)
                        ) {
                            Icon(Icons.Default.Add, contentDescription = "Add GST Return")
                        }
                    }
                }
                1 -> {
                    // Income Tax Returns
                    LazyColumn(
                        modifier = Modifier.fillMaxSize(),
                        contentPadding = PaddingValues(12.dp),
                        verticalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        items(clientItr) { itr ->
                            Surface(
                                modifier = Modifier.fillMaxWidth(),
                                shape = RoundedCornerShape(10.dp),
                                color = Color.White,
                                border = androidx.compose.foundation.BorderStroke(1.dp, IvoryOutline)
                            ) {
                                Column(modifier = Modifier.padding(12.dp)) {
                                    Row(
                                        modifier = Modifier.fillMaxWidth(),
                                        horizontalArrangement = Arrangement.SpaceBetween
                                    ) {
                                        Text(
                                            text = "${itr.itrForm} • AY ${itr.assessmentYear}",
                                            fontSize = 14.sp,
                                            fontWeight = FontWeight.Bold,
                                            color = DeepMaroon
                                        )
                                        StatusBadge(status = itr.filingStatus)
                                    }
                                    Spacer(modifier = Modifier.height(4.dp))
                                    Text(
                                        text = "Due Date: ${itr.filingDueDate} | Income: ₹${"%,.0f".format(itr.taxableIncome)}",
                                        fontSize = 12.sp,
                                        color = Color.DarkGray
                                    )
                                    if (itr.acknowledgementNumber.isNotEmpty()) {
                                        Text(
                                            text = "Ack No: ${itr.acknowledgementNumber}",
                                            fontSize = 11.sp,
                                            color = RoyalGoldDark,
                                            fontWeight = FontWeight.SemiBold
                                        )
                                    }
                                    if (itr.isTaxAuditApplicable) {
                                        Spacer(modifier = Modifier.height(4.dp))
                                        Text(
                                            text = "Tax Audit: ${itr.auditForm} (${itr.auditStatus})",
                                            fontSize = 11.sp,
                                            color = StatusPendingAmber,
                                            fontWeight = FontWeight.SemiBold
                                        )
                                    }
                                }
                            }
                        }
                    }
                }
                2 -> {
                    // Invoices & Ledger
                    Box(modifier = Modifier.fillMaxSize()) {
                        LazyColumn(
                            modifier = Modifier.fillMaxSize(),
                            contentPadding = PaddingValues(12.dp),
                            verticalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            items(clientInvoices) { inv ->
                                Surface(
                                    modifier = Modifier.fillMaxWidth(),
                                    shape = RoundedCornerShape(10.dp),
                                    color = Color.White,
                                    border = androidx.compose.foundation.BorderStroke(1.dp, IvoryOutline)
                                ) {
                                    Column(modifier = Modifier.padding(12.dp)) {
                                        Row(
                                            modifier = Modifier.fillMaxWidth(),
                                            horizontalArrangement = Arrangement.SpaceBetween
                                        ) {
                                            Text(
                                                text = inv.invoiceNumber,
                                                fontSize = 14.sp,
                                                fontWeight = FontWeight.Bold,
                                                color = DeepMaroon
                                            )
                                            StatusBadge(status = inv.paymentStatus)
                                        }
                                        Text(
                                            text = inv.serviceType,
                                            fontSize = 12.sp,
                                            color = Color.DarkGray
                                        )
                                        Spacer(modifier = Modifier.height(4.dp))
                                        Row(
                                            modifier = Modifier.fillMaxWidth(),
                                            horizontalArrangement = Arrangement.SpaceBetween
                                        ) {
                                            Text(
                                                text = "Total: ₹${"%,.2f".format(inv.totalAmount)} | Balance: ₹${"%,.2f".format(inv.pendingAmount)}",
                                                fontSize = 12.sp,
                                                fontWeight = FontWeight.SemiBold,
                                                color = DeepMaroon
                                            )
                                            IconButton(
                                                onClick = {
                                                    val file = PdfReportGenerator.generateInvoicePdf(context, inv)
                                                    PdfReportGenerator.viewOrSharePdf(context, file)
                                                },
                                                modifier = Modifier.size(24.dp)
                                            ) {
                                                Icon(Icons.Default.Print, contentDescription = "Print Invoice", tint = RoyalGoldDark)
                                            }
                                        }
                                    }
                                }
                            }
                        }
                        FloatingActionButton(
                            onClick = { showAddInvoiceDialog = true },
                            containerColor = DeepMaroon,
                            contentColor = RoyalGoldLight,
                            modifier = Modifier
                                .align(Alignment.BottomEnd)
                                .padding(16.dp)
                        ) {
                            Icon(Icons.Default.Receipt, contentDescription = "Create Invoice")
                        }
                    }
                }
                3 -> {
                    // Documents
                    LazyColumn(
                        modifier = Modifier.fillMaxSize(),
                        contentPadding = PaddingValues(12.dp),
                        verticalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        items(clientDocs) { doc ->
                            Surface(
                                modifier = Modifier.fillMaxWidth(),
                                shape = RoundedCornerShape(10.dp),
                                color = Color.White,
                                border = androidx.compose.foundation.BorderStroke(1.dp, IvoryOutline)
                            ) {
                                Row(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .padding(12.dp),
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Icon(Icons.Default.Description, contentDescription = null, tint = DeepMaroon)
                                    Spacer(modifier = Modifier.width(12.dp))
                                    Column(modifier = Modifier.weight(1f)) {
                                        Text(
                                            text = doc.title,
                                            fontSize = 13.sp,
                                            fontWeight = FontWeight.Bold,
                                            color = DeepMaroon
                                        )
                                        Text(
                                            text = "${doc.documentCategory} • FY ${doc.financialYear}",
                                            fontSize = 11.sp,
                                            color = Color.Gray
                                        )
                                    }
                                    StatusBadge(status = "Archived")
                                }
                            }
                        }
                    }
                }
            }
        }
    }

    if (showAddReturnDialog) {
        AddReturnDialog(
            clientId = client.clientId,
            clientName = client.businessName,
            gstin = client.primaryGstin,
            onDismiss = { showAddReturnDialog = false },
            onConfirm = { ret ->
                onAddGstReturn(ret)
                showAddReturnDialog = false
            }
        )
    }

    if (showAddInvoiceDialog) {
        AddInvoiceDialog(
            clientId = client.clientId,
            clientName = client.businessName,
            onDismiss = { showAddInvoiceDialog = false },
            onConfirm = { inv ->
                onAddInvoice(inv)
                showAddInvoiceDialog = false
            }
        )
    }
}

@Composable
fun GstReturnCard(
    ret: GstReturnEntity,
    onMarkFiled: (String) -> Unit
) {
    var showArnDialog by remember { mutableStateOf(false) }
    var arnInput by remember { mutableStateOf("") }

    Surface(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(10.dp),
        color = Color.White,
        border = androidx.compose.foundation.BorderStroke(1.dp, IvoryOutline)
    ) {
        Column(modifier = Modifier.padding(12.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "${ret.returnType} • ${ret.taxPeriod}",
                    fontSize = 14.sp,
                    fontWeight = FontWeight.Bold,
                    color = DeepMaroon
                )
                StatusBadge(status = ret.filingStatus)
            }
            Spacer(modifier = Modifier.height(4.dp))
            Text(
                text = "Due Date: ${ret.originalDueDate} | Turnover: ₹${"%,.0f".format(ret.taxableTurnover)}",
                fontSize = 12.sp,
                color = Color.DarkGray
            )
            if (ret.arnNumber.isNotEmpty()) {
                Text(
                    text = "ARN: ${ret.arnNumber} (${ret.actualFilingDate})",
                    fontSize = 11.sp,
                    color = StatusFiledGreen,
                    fontWeight = FontWeight.SemiBold
                )
            } else if (ret.filingStatus != "Filed") {
                Spacer(modifier = Modifier.height(6.dp))
                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.End) {
                    OutlinedButton(
                        onClick = { showArnDialog = true },
                        shape = RoundedCornerShape(8.dp),
                        contentPadding = PaddingValues(horizontal = 8.dp, vertical = 2.dp)
                    ) {
                        Text("Mark as Filed", fontSize = 11.sp, color = DeepMaroon)
                    }
                }
            }
        }
    }

    if (showArnDialog) {
        AlertDialog(
            onDismissRequest = { showArnDialog = false },
            title = { Text("Enter Filing ARN") },
            text = {
                OutlinedTextField(
                    value = arnInput,
                    onValueChange = { arnInput = it.uppercase() },
                    label = { Text("ARN Number (e.g. AA0810240019284)") },
                    modifier = Modifier.fillMaxWidth()
                )
            },
            confirmButton = {
                Button(onClick = {
                    onMarkFiled(arnInput.ifEmpty { "ARN-${System.currentTimeMillis() % 1000000}" })
                    showArnDialog = false
                }) {
                    Text("Confirm Filed")
                }
            },
            dismissButton = {
                TextButton(onClick = { showArnDialog = false }) {
                    Text("Cancel")
                }
            }
        )
    }
}

@Composable
fun EmptyComplianceState(title: String, actionLabel: String, onAction: () -> Unit) {
    Box(
        modifier = Modifier
            .fillMaxSize()
            .padding(24.dp),
        contentAlignment = Alignment.Center
    ) {
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            Icon(Icons.Default.Assignment, contentDescription = null, tint = Color.Gray, modifier = Modifier.size(48.dp))
            Spacer(modifier = Modifier.height(8.dp))
            Text(title, fontSize = 14.sp, color = Color.Gray)
            Spacer(modifier = Modifier.height(8.dp))
            Button(
                onClick = onAction,
                colors = ButtonDefaults.buttonColors(containerColor = DeepMaroon, contentColor = RoyalGoldLight)
            ) {
                Text(actionLabel)
            }
        }
    }
}

@Composable
fun AddReturnDialog(
    clientId: String,
    clientName: String,
    gstin: String,
    onDismiss: () -> Unit,
    onConfirm: (GstReturnEntity) -> Unit
) {
    var returnType by remember { mutableStateOf("GSTR-1") }
    var taxPeriod by remember { mutableStateOf("November 2024") }
    var dueDate by remember { mutableStateOf("2024-12-11") }
    var turnover by remember { mutableStateOf("1000000") }
    var taxLiability by remember { mutableStateOf("180000") }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Add GST Return Record") },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                OutlinedTextField(
                    value = returnType,
                    onValueChange = { returnType = it },
                    label = { Text("Return Type (GSTR-1, GSTR-3B, CMP-08)") },
                    modifier = Modifier.fillMaxWidth()
                )
                OutlinedTextField(
                    value = taxPeriod,
                    onValueChange = { taxPeriod = it },
                    label = { Text("Tax Period (e.g. November 2024)") },
                    modifier = Modifier.fillMaxWidth()
                )
                OutlinedTextField(
                    value = dueDate,
                    onValueChange = { dueDate = it },
                    label = { Text("Due Date (YYYY-MM-DD)") },
                    modifier = Modifier.fillMaxWidth()
                )
                OutlinedTextField(
                    value = turnover,
                    onValueChange = { turnover = it },
                    label = { Text("Turnover (₹)") },
                    modifier = Modifier.fillMaxWidth()
                )
                OutlinedTextField(
                    value = taxLiability,
                    onValueChange = { taxLiability = it },
                    label = { Text("Tax Liability (₹)") },
                    modifier = Modifier.fillMaxWidth()
                )
            }
        },
        confirmButton = {
            Button(onClick = {
                val ret = GstReturnEntity(
                    clientId = clientId,
                    clientName = clientName,
                    gstin = gstin,
                    returnType = returnType,
                    financialYear = "2024-25",
                    taxPeriod = taxPeriod,
                    originalDueDate = dueDate,
                    taxableTurnover = turnover.toDoubleOrNull() ?: 0.0,
                    taxLiability = taxLiability.toDoubleOrNull() ?: 0.0,
                    filingStatus = "Pending"
                )
                onConfirm(ret)
            }) {
                Text("Add Return")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) { Text("Cancel") }
        }
    )
}

@Composable
fun AddInvoiceDialog(
    clientId: String,
    clientName: String,
    onDismiss: () -> Unit,
    onConfirm: (ClientInvoiceEntity) -> Unit
) {
    var serviceType by remember { mutableStateOf("GST Return Filing & Accounting Retainer") }
    var amount by remember { mutableStateOf("5000") }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Create Client Invoice") },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                OutlinedTextField(
                    value = serviceType,
                    onValueChange = { serviceType = it },
                    label = { Text("Service Description") },
                    modifier = Modifier.fillMaxWidth()
                )
                OutlinedTextField(
                    value = amount,
                    onValueChange = { amount = it },
                    label = { Text("Invoice Amount (₹)") },
                    modifier = Modifier.fillMaxWidth()
                )
            }
        },
        confirmButton = {
            Button(onClick = {
                val amt = amount.toDoubleOrNull() ?: 0.0
                val inv = ClientInvoiceEntity(
                    invoiceNumber = "INV-2024-${System.currentTimeMillis() % 10000}",
                    clientId = clientId,
                    clientName = clientName,
                    serviceType = serviceType,
                    invoiceDate = "2024-10-31",
                    paymentDueDate = "2024-11-15",
                    totalAmount = amt,
                    amountReceived = 0.0,
                    pendingAmount = amt,
                    paymentStatus = "Unpaid"
                )
                onConfirm(inv)
            }) {
                Text("Generate Invoice")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) { Text("Cancel") }
        }
    )
}
