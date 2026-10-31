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
fun TaxAuditScreen(
    auditCases: List<ItrReturnEntity>,
    clients: List<ClientEntity>,
    isHindi: Boolean,
    onUpdateAuditCase: (ItrReturnEntity) -> Unit,
    onAddAuditCase: (ItrReturnEntity) -> Unit
) {
    val context = LocalContext.current
    var selectedFilter by remember { mutableStateOf("All Audits") }
    var showAddDialog by remember { mutableStateOf(false) }

    val auditList = auditCases.filter { it.isTaxAuditApplicable }
    val filtered = auditList.filter {
        when (selectedFilter) {
            "Pending" -> it.auditStatus != "Approved" && it.auditStatus != "Uploaded"
            "Uploaded" -> it.auditStatus == "Uploaded"
            "Approved" -> it.auditStatus == "Approved"
            else -> true
        }
    }

    Scaffold(
        floatingActionButton = {
            FloatingActionButton(
                onClick = { showAddDialog = true },
                containerColor = DeepMaroon,
                contentColor = RoyalGoldLight,
                modifier = Modifier.testTag("add_tax_audit_fab")
            ) {
                Icon(Icons.Default.Add, contentDescription = "New Tax Audit")
            }
        }
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .background(IvoryBackground)
                .testTag("tax_audit_screen")
        ) {
            Surface(color = Color.White, tonalElevation = 2.dp, modifier = Modifier.fillMaxWidth()) {
                Column(modifier = Modifier.padding(14.dp)) {
                    Text(
                        text = if (isHindi) "टैक्स ऑडिट प्रबंधन (Section 44AB Tax Audit)" else "Section 44AB Tax Audit Management",
                        fontSize = 15.sp,
                        fontWeight = FontWeight.Bold,
                        color = DeepMaroon
                    )
                    Text(
                        text = "Form 3CA-3CD (Corporate) • Form 3CB-3CD (Non-Corporate) • CBDT Due Dates",
                        fontSize = 11.sp,
                        color = Color.Gray
                    )

                    Spacer(modifier = Modifier.height(10.dp))

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        listOf("All Audits", "Pending", "Uploaded", "Approved").forEach { f ->
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
                        Icon(Icons.Default.VerifiedUser, contentDescription = null, tint = Color.Gray, modifier = Modifier.size(48.dp))
                        Spacer(modifier = Modifier.height(8.dp))
                        Text("No Tax Audit cases match filter.", color = Color.Gray)
                        Spacer(modifier = Modifier.height(8.dp))
                        Button(
                            onClick = { showAddDialog = true },
                            colors = ButtonDefaults.buttonColors(containerColor = DeepMaroon, contentColor = RoyalGoldLight)
                        ) {
                            Text("+ Mark Client for Tax Audit")
                        }
                    }
                }
            } else {
                LazyColumn(
                    modifier = Modifier.fillMaxSize(),
                    contentPadding = PaddingValues(12.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    items(filtered, key = { it.id }) { item ->
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
                                            text = item.clientName,
                                            fontSize = 14.sp,
                                            fontWeight = FontWeight.Bold,
                                            color = DeepMaroon
                                        )
                                        Text(
                                            text = "PAN: ${item.pan} • Form: ${item.auditForm}",
                                            fontSize = 12.sp,
                                            color = RoyalGoldDark,
                                            fontWeight = FontWeight.SemiBold
                                        )
                                    }
                                    StatusBadge(status = item.auditStatus)
                                }

                                Spacer(modifier = Modifier.height(8.dp))
                                HorizontalDivider(color = IvoryOutlineVariant, thickness = 0.8.dp)
                                Spacer(modifier = Modifier.height(8.dp))

                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.SpaceBetween
                                ) {
                                    Column {
                                        Text(
                                            text = "AY: ${item.assessmentYear} | ITR Form: ${item.itrForm}",
                                            fontSize = 12.sp,
                                            color = Color.DarkGray
                                        )
                                        Text(
                                            text = "Audit Due Date: 30 Sept / 07 Oct",
                                            fontSize = 11.sp,
                                            color = StatusOverdueRed
                                        )
                                    }

                                    Column(horizontalAlignment = Alignment.End) {
                                        Text(
                                            text = "Income: ₹${"%,.0f".format(item.taxableIncome)}",
                                            fontSize = 12.sp,
                                            color = Color.DarkGray
                                        )
                                        Text(
                                            text = "Tax: ₹${"%,.0f".format(item.taxPayable)}",
                                            fontSize = 12.sp,
                                            fontWeight = FontWeight.Bold,
                                            color = DeepMaroon
                                        )
                                    }
                                }

                                if (item.auditFilingDate.isNotEmpty()) {
                                    Spacer(modifier = Modifier.height(6.dp))
                                    Text(
                                        text = "Audit Report Uploaded: ${item.auditFilingDate}",
                                        fontSize = 11.sp,
                                        color = StatusFiledGreen,
                                        fontWeight = FontWeight.SemiBold
                                    )
                                } else {
                                    Spacer(modifier = Modifier.height(10.dp))
                                    Row(
                                        modifier = Modifier.fillMaxWidth(),
                                        horizontalArrangement = Arrangement.End
                                    ) {
                                        TextButton(onClick = {
                                            val msg = WhatsAppHelper.buildMessage(
                                                WhatsAppHelper.MessageType.DOCUMENT_REQUEST,
                                                item.clientName,
                                                "Tax Audit Books & Balance Sheet (Sec 44AB)",
                                                item.assessmentYear,
                                                item.filingDueDate,
                                                isHindi = isHindi
                                            )
                                            WhatsAppHelper.openWhatsApp(context, "9829012345", msg)
                                        }) {
                                            Icon(Icons.Default.Chat, contentDescription = null, tint = StatusFiledGreen, modifier = Modifier.size(16.dp))
                                            Spacer(modifier = Modifier.width(4.dp))
                                            Text(if (isHindi) "ऑडिट बुक्स मांगें" else "Request Audit Books", fontSize = 11.sp, color = StatusFiledGreen)
                                        }

                                        Spacer(modifier = Modifier.width(6.dp))

                                        Button(
                                            onClick = {
                                                onUpdateAuditCase(
                                                    item.copy(
                                                        auditStatus = "Uploaded",
                                                        auditFilingDate = "2024-10-31"
                                                    )
                                                )
                                            },
                                            colors = ButtonDefaults.buttonColors(containerColor = DeepMaroon, contentColor = RoyalGoldLight),
                                            shape = RoundedCornerShape(8.dp),
                                            contentPadding = PaddingValues(horizontal = 10.dp, vertical = 2.dp)
                                        ) {
                                            Text("Mark Uploaded", fontSize = 11.sp)
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
        var auditForm by remember { mutableStateOf("Form 3CA-3CD") }
        var ay by remember { mutableStateOf("2024-25") }
        var taxableIncome by remember { mutableStateOf("3500000") }

        AlertDialog(
            onDismissRequest = { showAddDialog = false },
            title = { Text("Assign Tax Audit Case (Sec 44AB)") },
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
                        value = auditForm,
                        onValueChange = { auditForm = it },
                        label = { Text("Form (Form 3CA-3CD or 3CB-3CD)") },
                        modifier = Modifier.fillMaxWidth()
                    )
                    OutlinedTextField(
                        value = ay,
                        onValueChange = { ay = it },
                        label = { Text("Assessment Year (e.g. 2024-25)") },
                        modifier = Modifier.fillMaxWidth()
                    )
                    OutlinedTextField(
                        value = taxableIncome,
                        onValueChange = { taxableIncome = it },
                        label = { Text("Estimated Taxable Income (₹)") },
                        modifier = Modifier.fillMaxWidth()
                    )
                }
            },
            confirmButton = {
                Button(onClick = {
                    val entity = ItrReturnEntity(
                        clientId = selectedClient.clientId,
                        clientName = selectedClient.businessName,
                        pan = selectedClient.pan,
                        financialYear = "2023-24",
                        assessmentYear = ay,
                        itrForm = if (selectedClient.businessType.contains("Ltd", ignoreCase = true)) "ITR-6" else "ITR-3",
                        filingDueDate = "2024-11-15",
                        taxableIncome = taxableIncome.toDoubleOrNull() ?: 0.0,
                        taxPayable = (taxableIncome.toDoubleOrNull() ?: 0.0) * 0.25,
                        isTaxAuditApplicable = true,
                        auditForm = auditForm,
                        auditStatus = "In Progress",
                        filingStatus = "Pending"
                    )
                    onAddAuditCase(entity)
                    showAddDialog = false
                }) {
                    Text("Add Audit Case")
                }
            },
            dismissButton = {
                TextButton(onClick = { showAddDialog = false }) { Text("Cancel") }
            }
        )
    }
}
