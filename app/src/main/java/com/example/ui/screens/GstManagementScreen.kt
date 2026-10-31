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
import com.example.data.model.GstReturnEntity
import com.example.service.CsvExportHelper
import com.example.service.PdfReportGenerator
import com.example.service.WhatsAppHelper
import com.example.ui.components.StatusBadge
import com.example.ui.theme.*

@Composable
fun GstManagementScreen(
    returns: List<GstReturnEntity>,
    clients: List<ClientEntity>,
    isHindi: Boolean,
    onAddReturn: (GstReturnEntity) -> Unit,
    onMarkFiled: (Long, String) -> Unit,
    onAutoAssign: (ClientEntity) -> Unit
) {
    val context = LocalContext.current
    var selectedType by remember { mutableStateOf("All") }
    var selectedStatus by remember { mutableStateOf("All") }
    var showAddDialog by remember { mutableStateOf(false) }

    val filteredReturns = returns.filter {
        val matchesType = if (selectedType == "All") true else it.returnType == selectedType
        val matchesStatus = when (selectedStatus) {
            "Pending" -> it.filingStatus != "Filed" && it.filingStatus != "Nil Filed"
            "Filed" -> it.filingStatus == "Filed" || it.filingStatus == "Nil Filed"
            else -> true
        }
        matchesType && matchesStatus
    }

    Scaffold(
        floatingActionButton = {
            FloatingActionButton(
                onClick = { showAddDialog = true },
                containerColor = DeepMaroon,
                contentColor = RoyalGoldLight,
                modifier = Modifier.testTag("add_gst_fab")
            ) {
                Icon(Icons.Default.Add, contentDescription = "Add GST Return")
            }
        }
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .background(IvoryBackground)
                .testTag("gst_management_screen")
        ) {
            // Header summary bar
            Surface(color = Color.White, tonalElevation = 2.dp, modifier = Modifier.fillMaxWidth()) {
                Column(modifier = Modifier.padding(12.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column {
                            Text(
                                text = if (isHindi) "जीएसटी रिटर्न प्रबंधन (GST Return Filing Hub)" else "GST Return Filing Management",
                                fontSize = 15.sp,
                                fontWeight = FontWeight.Bold,
                                color = DeepMaroon
                            )
                            Text(
                                text = "GSTR-1 • GSTR-3B • CMP-08 • GSTR-9 • GSTR-9C",
                                fontSize = 11.sp,
                                color = Color.Gray
                            )
                        }

                        Row {
                            IconButton(onClick = {
                                val file = CsvExportHelper.exportGstReturnsToCsv(context, filteredReturns)
                                CsvExportHelper.shareCsvFile(context, file, "GST Returns Report")
                            }) {
                                Icon(Icons.Default.FileDownload, contentDescription = "Export Excel", tint = DeepMaroon)
                            }

                            IconButton(onClick = {
                                val file = PdfReportGenerator.generateGstReturnsPdf(context, filteredReturns)
                                PdfReportGenerator.viewOrSharePdf(context, file)
                            }) {
                                Icon(Icons.Default.Print, contentDescription = "Print PDF", tint = RoyalGoldDark)
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(8.dp))

                    // Filters
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        listOf("All", "GSTR-1", "GSTR-3B", "CMP-08", "GSTR-9").forEach { type ->
                            FilterChip(
                                selected = selectedType == type,
                                onClick = { selectedType = type },
                                label = { Text(type, fontSize = 11.sp) },
                                colors = FilterChipDefaults.filterChipColors(
                                    selectedContainerColor = DeepMaroon,
                                    selectedLabelColor = RoyalGoldLight
                                )
                            )
                        }
                    }

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        listOf("All", "Pending", "Filed").forEach { status ->
                            FilterChip(
                                selected = selectedStatus == status,
                                onClick = { selectedStatus = status },
                                label = { Text(status, fontSize = 11.sp) },
                                colors = FilterChipDefaults.filterChipColors(
                                    selectedContainerColor = RoyalGoldDark,
                                    selectedLabelColor = Color.White
                                )
                            )
                        }
                    }
                }
            }

            // List of returns
            if (filteredReturns.isEmpty()) {
                Box(
                    modifier = Modifier.fillMaxSize().padding(32.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Text("No GST return records match the filter.", color = Color.Gray)
                }
            } else {
                LazyColumn(
                    modifier = Modifier.fillMaxSize(),
                    contentPadding = PaddingValues(12.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    items(filteredReturns, key = { it.id }) { ret ->
                        GstReturnCardFull(
                            ret = ret,
                            isHindi = isHindi,
                            onMarkFiled = { arn -> onMarkFiled(ret.id, arn) },
                            onSendReminder = {
                                val msg = WhatsAppHelper.buildMessage(
                                    WhatsAppHelper.MessageType.GST_DUE,
                                    ret.clientName,
                                    ret.returnType,
                                    ret.taxPeriod,
                                    ret.originalDueDate,
                                    isHindi = isHindi
                                )
                                WhatsAppHelper.openWhatsApp(context, "9829012345", msg)
                            }
                        )
                    }
                }
            }
        }
    }

    if (showAddDialog) {
        val clientList = clients.ifEmpty {
            listOf(ClientEntity("DEFAULT", "Marudhar Textiles", "Rajesh Sharma", "Pvt Ltd", "Regular", "", mobileNumber = "9829012345", whatsAppNumber = "9829012345", emailAddress = "", pan = "AABCM1234F"))
        }
        var selectedClient by remember { mutableStateOf(clientList.first()) }
        AddReturnDialog(
            clientId = selectedClient.clientId,
            clientName = selectedClient.businessName,
            gstin = selectedClient.primaryGstin,
            onDismiss = { showAddDialog = false },
            onConfirm = { newRet ->
                onAddReturn(newRet)
                showAddDialog = false
            }
        )
    }
}

@Composable
fun GstReturnCardFull(
    ret: GstReturnEntity,
    isHindi: Boolean,
    onMarkFiled: (String) -> Unit,
    onSendReminder: () -> Unit
) {
    var showArnDialog by remember { mutableStateOf(false) }
    var arnInput by remember { mutableStateOf("") }

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
                        text = ret.clientName,
                        fontSize = 14.sp,
                        fontWeight = FontWeight.Bold,
                        color = DeepMaroon
                    )
                    Text(
                        text = "GSTIN: ${ret.gstin.ifEmpty { "Unspecified" }}",
                        fontSize = 11.sp,
                        color = Color.DarkGray
                    )
                }
                StatusBadge(status = ret.filingStatus)
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
                        text = "${ret.returnType} • ${ret.taxPeriod}",
                        fontSize = 13.sp,
                        fontWeight = FontWeight.Bold,
                        color = RoyalGoldDark
                    )
                    Text(
                        text = "Due Date: ${ret.originalDueDate}",
                        fontSize = 11.sp,
                        color = StatusOverdueRed,
                        fontWeight = FontWeight.SemiBold
                    )
                }

                Column(horizontalAlignment = Alignment.End) {
                    Text(
                        text = "Turnover: ₹${"%,.0f".format(ret.taxableTurnover)}",
                        fontSize = 12.sp,
                        color = Color.DarkGray
                    )
                    Text(
                        text = "Tax Liability: ₹${"%,.0f".format(ret.taxLiability)}",
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Bold,
                        color = DeepMaroon
                    )
                }
            }

            if (ret.arnNumber.isNotEmpty()) {
                Spacer(modifier = Modifier.height(6.dp))
                Text(
                    text = "ARN: ${ret.arnNumber} (Filed on ${ret.actualFilingDate})",
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
                        Text(if (isHindi) "व्हाट्सएप रिमाइंडर" else "WhatsApp Reminder", fontSize = 11.sp, color = StatusFiledGreen)
                    }

                    Spacer(modifier = Modifier.width(6.dp))

                    Button(
                        onClick = { showArnDialog = true },
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

    if (showArnDialog) {
        AlertDialog(
            onDismissRequest = { showArnDialog = false },
            title = { Text("Filing Confirmation") },
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
                    onMarkFiled(arnInput.ifEmpty { "ARN-${System.currentTimeMillis() % 10000000}" })
                    showArnDialog = false
                }) {
                    Text("Confirm Filed")
                }
            },
            dismissButton = {
                TextButton(onClick = { showArnDialog = false }) { Text("Cancel") }
            }
        )
    }
}
