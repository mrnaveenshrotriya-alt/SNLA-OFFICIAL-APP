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
import com.example.data.model.TdsReturnEntity
import com.example.service.WhatsAppHelper
import com.example.ui.components.StatusBadge
import com.example.ui.theme.*

@Composable
fun TdsManagementScreen(
    tdsReturns: List<TdsReturnEntity>,
    clients: List<ClientEntity>,
    isHindi: Boolean,
    onAddTds: (TdsReturnEntity) -> Unit,
    onUpdateTds: (TdsReturnEntity) -> Unit
) {
    val context = LocalContext.current
    var showAddDialog by remember { mutableStateOf(false) }

    Scaffold(
        floatingActionButton = {
            FloatingActionButton(
                onClick = { showAddDialog = true },
                containerColor = DeepMaroon,
                contentColor = RoyalGoldLight,
                modifier = Modifier.testTag("add_tds_fab")
            ) {
                Icon(Icons.Default.Add, contentDescription = "Add TDS Return")
            }
        }
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .background(IvoryBackground)
                .testTag("tds_screen")
        ) {
            Surface(color = Color.White, tonalElevation = 2.dp, modifier = Modifier.fillMaxWidth()) {
                Column(modifier = Modifier.padding(14.dp)) {
                    Text(
                        text = if (isHindi) "टीडीएस / टीसीएस रिटर्न प्रबंधन (TDS / TCS Return Hub)" else "TDS / TCS Return Management",
                        fontSize = 15.sp,
                        fontWeight = FontWeight.Bold,
                        color = DeepMaroon
                    )
                    Text(
                        text = "Form 24Q (Salary) • Form 26Q (Non-Salary) • Form 27Q • Form 27EQ (TCS) • Form 16/16A",
                        fontSize = 11.sp,
                        color = Color.Gray
                    )
                }
            }

            if (tdsReturns.isEmpty()) {
                Box(modifier = Modifier.fillMaxSize().padding(32.dp), contentAlignment = Alignment.Center) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Icon(Icons.Default.FactCheck, contentDescription = null, tint = Color.Gray, modifier = Modifier.size(48.dp))
                        Spacer(modifier = Modifier.height(8.dp))
                        Text("No TDS / TCS records found.", color = Color.Gray)
                        Spacer(modifier = Modifier.height(8.dp))
                        Button(
                            onClick = { showAddDialog = true },
                            colors = ButtonDefaults.buttonColors(containerColor = DeepMaroon, contentColor = RoyalGoldLight)
                        ) {
                            Text("+ Add TDS Record")
                        }
                    }
                }
            } else {
                LazyColumn(
                    modifier = Modifier.fillMaxSize(),
                    contentPadding = PaddingValues(12.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    items(tdsReturns, key = { it.id }) { item ->
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
                                            text = "${item.returnType} • Quarter ${item.quarter} (FY ${item.financialYear})",
                                            fontSize = 12.sp,
                                            color = RoyalGoldDark,
                                            fontWeight = FontWeight.SemiBold
                                        )
                                    }
                                    StatusBadge(status = item.filingStatus)
                                }

                                Spacer(modifier = Modifier.height(8.dp))
                                Divider(color = IvoryOutlineVariant, thickness = 0.8.dp)
                                Spacer(modifier = Modifier.height(8.dp))

                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.SpaceBetween
                                ) {
                                    Text(
                                        text = "Due Date: ${item.dueDate}",
                                        fontSize = 11.sp,
                                        color = StatusOverdueRed
                                    )
                                    Text(
                                        text = "Tax Deducted: ₹${"%,.0f".format(item.taxDeducted)}",
                                        fontSize = 12.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = DeepMaroon
                                    )
                                }

                                if (item.acknowledgementNumber.isNotEmpty()) {
                                    Text(
                                        text = "Token / PRN: ${item.acknowledgementNumber}",
                                        fontSize = 11.sp,
                                        color = StatusFiledGreen,
                                        fontWeight = FontWeight.SemiBold
                                    )
                                } else {
                                    Spacer(modifier = Modifier.height(8.dp))
                                    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.End) {
                                        TextButton(onClick = {
                                            val msg = WhatsAppHelper.buildMessage(
                                                WhatsAppHelper.MessageType.TDS_DUE,
                                                item.clientName,
                                                item.returnType,
                                                item.quarter,
                                                item.dueDate,
                                                isHindi = isHindi
                                            )
                                            WhatsAppHelper.openWhatsApp(context, "9829012345", msg)
                                        }) {
                                            Icon(Icons.Default.Chat, contentDescription = null, tint = StatusFiledGreen, modifier = Modifier.size(14.dp))
                                            Spacer(modifier = Modifier.width(4.dp))
                                            Text("WhatsApp Reminder", fontSize = 11.sp, color = StatusFiledGreen)
                                        }

                                        Spacer(modifier = Modifier.width(6.dp))

                                        OutlinedButton(
                                            onClick = {
                                                onUpdateTds(item.copy(filingStatus = "Filed", acknowledgementNumber = "PRN-${System.currentTimeMillis() % 100000000}"))
                                            },
                                            shape = RoundedCornerShape(8.dp),
                                            contentPadding = PaddingValues(horizontal = 8.dp, vertical = 2.dp)
                                        ) {
                                            Text("Mark Filed", fontSize = 11.sp, color = DeepMaroon)
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
        var returnType by remember { mutableStateOf("Form 26Q") }
        var quarter by remember { mutableStateOf("Q2") }
        var dueDate by remember { mutableStateOf("2024-10-31") }
        var taxDeducted by remember { mutableStateOf("45000") }

        AlertDialog(
            onDismissRequest = { showAddDialog = false },
            title = { Text("Add TDS / TCS Return") },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    OutlinedTextField(
                        value = returnType,
                        onValueChange = { returnType = it },
                        label = { Text("Form Type (24Q, 26Q, 27Q, 27EQ)") },
                        modifier = Modifier.fillMaxWidth()
                    )
                    OutlinedTextField(
                        value = quarter,
                        onValueChange = { quarter = it },
                        label = { Text("Quarter (Q1, Q2, Q3, Q4)") },
                        modifier = Modifier.fillMaxWidth()
                    )
                    OutlinedTextField(
                        value = dueDate,
                        onValueChange = { dueDate = it },
                        label = { Text("Due Date (YYYY-MM-DD)") },
                        modifier = Modifier.fillMaxWidth()
                    )
                    OutlinedTextField(
                        value = taxDeducted,
                        onValueChange = { taxDeducted = it },
                        label = { Text("Total Tax Deducted (₹)") },
                        modifier = Modifier.fillMaxWidth()
                    )
                }
            },
            confirmButton = {
                Button(onClick = {
                    val entity = TdsReturnEntity(
                        clientId = selectedClient.clientId,
                        clientName = selectedClient.businessName,
                        tan = selectedClient.tan.ifEmpty { "JPRM01234D" },
                        pan = selectedClient.pan,
                        financialYear = "2024-25",
                        quarter = quarter,
                        returnType = returnType,
                        dueDate = dueDate,
                        taxDeducted = taxDeducted.toDoubleOrNull() ?: 0.0,
                        filingStatus = "Pending"
                    )
                    onAddTds(entity)
                    showAddDialog = false
                }) {
                    Text("Add TDS Record")
                }
            },
            dismissButton = {
                TextButton(onClick = { showAddDialog = false }) { Text("Cancel") }
            }
        )
    }
}
