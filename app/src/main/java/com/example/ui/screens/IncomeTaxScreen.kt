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
import com.example.data.model.ItrReturnEntity
import com.example.service.WhatsAppHelper
import com.example.ui.components.StatusBadge
import com.example.ui.theme.*

@Composable
fun IncomeTaxScreen(
    returns: List<ItrReturnEntity>,
    clients: List<ClientEntity>,
    isHindi: Boolean,
    onAddItr: (ItrReturnEntity) -> Unit,
    onUpdateItr: (ItrReturnEntity) -> Unit
) {
    val context = LocalContext.current
    var selectedAy by remember { mutableStateOf("All AY") }
    var selectedFilter by remember { mutableStateOf("All") }
    var showAddDialog by remember { mutableStateOf(false) }

    val filteredReturns = returns.filter {
        val matchesAy = if (selectedAy == "All AY") true else it.assessmentYear == selectedAy
        val matchesFilter = when (selectedFilter) {
            "Pending" -> it.filingStatus != "Filed"
            "Tax Audit" -> it.isTaxAuditApplicable
            "Filed" -> it.filingStatus == "Filed"
            else -> true
        }
        matchesAy && matchesFilter
    }

    Scaffold(
        floatingActionButton = {
            FloatingActionButton(
                onClick = { showAddDialog = true },
                containerColor = DeepMaroon,
                contentColor = RoyalGoldLight,
                modifier = Modifier.testTag("add_itr_fab")
            ) {
                Icon(Icons.Default.Add, contentDescription = "Add ITR")
            }
        }
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .background(IvoryBackground)
                .testTag("itr_screen")
        ) {
            Surface(color = Color.White, tonalElevation = 2.dp, modifier = Modifier.fillMaxWidth()) {
                Column(modifier = Modifier.padding(12.dp)) {
                    Text(
                        text = if (isHindi) "आयकर रिटर्न प्रबंधन (Income Tax & Audit Hub)" else "Income Tax & Tax Audit Management",
                        fontSize = 15.sp,
                        fontWeight = FontWeight.Bold,
                        color = DeepMaroon
                    )
                    Text(
                        text = "ITR-1 to ITR-7 • Tax Audits (3CA/3CD) • AY 2024-25 & 2025-26",
                        fontSize = 11.sp,
                        color = Color.Gray
                    )

                    Spacer(modifier = Modifier.height(8.dp))

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        listOf("All", "Pending", "Tax Audit", "Filed").forEach { filter ->
                            FilterChip(
                                selected = selectedFilter == filter,
                                onClick = { selectedFilter = filter },
                                label = { Text(filter, fontSize = 11.sp) },
                                colors = FilterChipDefaults.filterChipColors(
                                    selectedContainerColor = DeepMaroon,
                                    selectedLabelColor = RoyalGoldLight
                                )
                            )
                        }
                    }
                }
            }

            if (filteredReturns.isEmpty()) {
                Box(
                    modifier = Modifier.fillMaxSize().padding(32.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Text("No Income Tax records match criteria.", color = Color.Gray)
                }
            } else {
                LazyColumn(
                    modifier = Modifier.fillMaxSize(),
                    contentPadding = PaddingValues(12.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    items(filteredReturns, key = { it.id }) { itr ->
                        ItrCard(
                            itr = itr,
                            isHindi = isHindi,
                            onSendReminder = {
                                val msg = WhatsAppHelper.buildMessage(
                                    type = WhatsAppHelper.MessageType.ITR_DUE,
                                    clientName = itr.clientName,
                                    returnOrDocType = itr.itrForm,
                                    period = "AY ${itr.assessmentYear}",
                                    dueDate = itr.filingDueDate,
                                    isHindi = isHindi
                                )
                                WhatsAppHelper.openWhatsApp(context, "9829012345", msg)
                            },
                            onMarkFiled = { ackNo ->
                                onUpdateItr(
                                    itr.copy(
                                        filingStatus = "Filed",
                                        actualFilingDate = "2024-10-31",
                                        acknowledgementNumber = ackNo
                                    )
                                )
                            }
                        )
                    }
                }
            }
        }
    }

    if (showAddDialog) {
        AddItrDialog(
            clients = clients,
            onDismiss = { showAddDialog = false },
            onConfirm = { newItr ->
                onAddItr(newItr)
                showAddDialog = false
            }
        )
    }
}

@Composable
fun ItrCard(
    itr: ItrReturnEntity,
    isHindi: Boolean,
    onSendReminder: () -> Unit,
    onMarkFiled: (String) -> Unit
) {
    var showAckDialog by remember { mutableStateOf(false) }
    var ackInput by remember { mutableStateOf("") }

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
                        text = itr.clientName,
                        fontSize = 14.sp,
                        fontWeight = FontWeight.Bold,
                        color = DeepMaroon
                    )
                    Text(
                        text = "PAN: ${itr.pan} • Form: ${itr.itrForm} (${itr.returnNature})",
                        fontSize = 12.sp,
                        color = Color.DarkGray
                    )
                }
                StatusBadge(status = itr.filingStatus)
            }

            Spacer(modifier = Modifier.height(8.dp))
            Divider(color = IvoryOutlineVariant, thickness = 0.8.dp)
            Spacer(modifier = Modifier.height(8.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Column {
                    Text(
                        text = "AY: ${itr.assessmentYear} (FY ${itr.financialYear})",
                        fontSize = 12.sp,
                        fontWeight = FontWeight.SemiBold,
                        color = RoyalGoldDark
                    )
                    Text(
                        text = "Due Date: ${itr.extendedDueDate.ifEmpty { itr.filingDueDate }}",
                        fontSize = 11.sp,
                        color = StatusOverdueRed
                    )
                }

                Column(horizontalAlignment = Alignment.End) {
                    Text(
                        text = "Taxable Income: ₹${"%,.0f".format(itr.taxableIncome)}",
                        fontSize = 12.sp,
                        color = Color.DarkGray
                    )
                    Text(
                        text = "Tax Payable: ₹${"%,.0f".format(itr.taxPayable)}",
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Bold,
                        color = DeepMaroon
                    )
                }
            }

            if (itr.isTaxAuditApplicable) {
                Spacer(modifier = Modifier.height(6.dp))
                Surface(
                    color = GoldContainer,
                    shape = RoundedCornerShape(6.dp),
                    border = androidx.compose.foundation.BorderStroke(0.8.dp, RoyalGold)
                ) {
                    Text(
                        text = "Tax Audit: ${itr.auditForm} | Status: ${itr.auditStatus}",
                        fontSize = 11.sp,
                        fontWeight = FontWeight.SemiBold,
                        color = DeepMaroon,
                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                    )
                }
            }

            if (itr.acknowledgementNumber.isNotEmpty()) {
                Spacer(modifier = Modifier.height(6.dp))
                Text(
                    text = "ITR-V Ack: ${itr.acknowledgementNumber} (Filed on ${itr.actualFilingDate})",
                    fontSize = 11.sp,
                    color = StatusFiledGreen,
                    fontWeight = FontWeight.SemiBold
                )
            } else {
                Spacer(modifier = Modifier.height(10.dp))
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.End,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    TextButton(onClick = onSendReminder) {
                        Icon(Icons.Default.Chat, contentDescription = null, tint = StatusFiledGreen, modifier = Modifier.size(16.dp))
                        Spacer(modifier = Modifier.width(4.dp))
                        Text(if (isHindi) "व्हाट्सएप" else "WhatsApp", fontSize = 11.sp, color = StatusFiledGreen)
                    }

                    Spacer(modifier = Modifier.width(6.dp))

                    Button(
                        onClick = { showAckDialog = true },
                        colors = ButtonDefaults.buttonColors(containerColor = DeepMaroon, contentColor = RoyalGoldLight),
                        shape = RoundedCornerShape(8.dp),
                        contentPadding = PaddingValues(horizontal = 12.dp, vertical = 4.dp)
                    ) {
                        Text(if (isHindi) "फाइल मार्क करें" else "Mark as Filed", fontSize = 11.sp)
                    }
                }
            }
        }
    }

    if (showAckDialog) {
        AlertDialog(
            onDismissRequest = { showAckDialog = false },
            title = { Text("ITR Filing Acknowledgement") },
            text = {
                OutlinedTextField(
                    value = ackInput,
                    onValueChange = { ackInput = it },
                    label = { Text("15-digit Ack / E-Filing No") },
                    modifier = Modifier.fillMaxWidth()
                )
            },
            confirmButton = {
                Button(onClick = {
                    onMarkFiled(ackInput.ifEmpty { "ACK-${System.currentTimeMillis() % 10000000000L}" })
                    showAckDialog = false
                }) {
                    Text("Save Ack & Mark Filed")
                }
            },
            dismissButton = {
                TextButton(onClick = { showAckDialog = false }) { Text("Cancel") }
            }
        )
    }
}

@Composable
fun AddItrDialog(
    clients: List<ClientEntity>,
    onDismiss: () -> Unit,
    onConfirm: (ItrReturnEntity) -> Unit
) {
    val clientList = clients.ifEmpty {
        listOf(ClientEntity("DEFAULT", "Apeksha Gems", "Naveen Shrotriya", "Proprietor", "Trader", "", mobileNumber = "9414056789", whatsAppNumber = "9414056789", emailAddress = "", pan = "AABPS9876K"))
    }
    var selectedClient by remember { mutableStateOf(clientList.first()) }
    var itrForm by remember { mutableStateOf("ITR-3") }
    var ay by remember { mutableStateOf("2024-25") }
    var income by remember { mutableStateOf("1500000") }
    var taxPayable by remember { mutableStateOf("225000") }
    var dueDate by remember { mutableStateOf("2024-11-15") }
    var isAudit by remember { mutableStateOf(false) }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Add Income Tax Return") },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                OutlinedTextField(
                    value = selectedClient.businessName,
                    onValueChange = {},
                    label = { Text("Client Name") },
                    readOnly = true,
                    modifier = Modifier.fillMaxWidth()
                )
                Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                    OutlinedTextField(
                        value = itrForm,
                        onValueChange = { itrForm = it },
                        label = { Text("Form (ITR-1 to 7)") },
                        modifier = Modifier.weight(1f)
                    )
                    OutlinedTextField(
                        value = ay,
                        onValueChange = { ay = it },
                        label = { Text("AY (e.g. 2024-25)") },
                        modifier = Modifier.weight(1f)
                    )
                }
                OutlinedTextField(
                    value = income,
                    onValueChange = { income = it },
                    label = { Text("Total Taxable Income (₹)") },
                    modifier = Modifier.fillMaxWidth()
                )
                OutlinedTextField(
                    value = taxPayable,
                    onValueChange = { taxPayable = it },
                    label = { Text("Tax Payable (₹)") },
                    modifier = Modifier.fillMaxWidth()
                )
                OutlinedTextField(
                    value = dueDate,
                    onValueChange = { dueDate = it },
                    label = { Text("Due Date (YYYY-MM-DD)") },
                    modifier = Modifier.fillMaxWidth()
                )
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Checkbox(checked = isAudit, onCheckedChange = { isAudit = it })
                    Text("Tax Audit Applicable (3CA / 3CD)", fontSize = 12.sp)
                }
            }
        },
        confirmButton = {
            Button(onClick = {
                val inc = income.toDoubleOrNull() ?: 0.0
                val tax = taxPayable.toDoubleOrNull() ?: 0.0
                val item = ItrReturnEntity(
                    clientId = selectedClient.clientId,
                    clientName = selectedClient.businessName,
                    pan = selectedClient.pan,
                    financialYear = "2023-24",
                    assessmentYear = ay,
                    itrForm = itrForm,
                    filingDueDate = dueDate,
                    taxableIncome = inc,
                    taxPayable = tax,
                    isTaxAuditApplicable = isAudit,
                    auditStatus = if (isAudit) "Pending" else "Not Applicable",
                    filingStatus = "Pending"
                )
                onConfirm(item)
            }) {
                Text("Save ITR")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) { Text("Cancel") }
        }
    )
}
