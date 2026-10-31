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
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.model.ClientEntity
import com.example.data.model.EWayBillEntity
import com.example.ui.components.StatusBadge
import com.example.ui.theme.*

@Composable
fun EWayBillScreen(
    bills: List<EWayBillEntity>,
    clients: List<ClientEntity>,
    isHindi: Boolean,
    onAddBill: (EWayBillEntity) -> Unit
) {
    var showAddDialog by remember { mutableStateOf(false) }

    Scaffold(
        floatingActionButton = {
            FloatingActionButton(
                onClick = { showAddDialog = true },
                containerColor = DeepMaroon,
                contentColor = RoyalGoldLight,
                modifier = Modifier.testTag("add_eway_fab")
            ) {
                Icon(Icons.Default.Add, contentDescription = "Add E-Way Bill")
            }
        }
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .background(IvoryBackground)
                .testTag("eway_bill_screen")
        ) {
            Surface(color = Color.White, tonalElevation = 2.dp, modifier = Modifier.fillMaxWidth()) {
                Column(modifier = Modifier.padding(14.dp)) {
                    Text(
                        text = if (isHindi) "ई-वे बिल ट्रैकिंग (E-Way Bill Management)" else "E-Way Bill Compliance & Tracking",
                        fontSize = 15.sp,
                        fontWeight = FontWeight.Bold,
                        color = DeepMaroon
                    )
                    Text(
                        text = "Consignment Value • Vehicle No • Validity Expiry Tracking",
                        fontSize = 11.sp,
                        color = Color.Gray
                    )
                }
            }

            if (bills.isEmpty()) {
                Box(modifier = Modifier.fillMaxSize().padding(32.dp), contentAlignment = Alignment.Center) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Icon(Icons.Default.LocalShipping, contentDescription = null, tint = Color.Gray, modifier = Modifier.size(48.dp))
                        Spacer(modifier = Modifier.height(8.dp))
                        Text("No E-Way Bills active.", color = Color.Gray)
                        Spacer(modifier = Modifier.height(8.dp))
                        Button(
                            onClick = { showAddDialog = true },
                            colors = ButtonDefaults.buttonColors(containerColor = DeepMaroon, contentColor = RoyalGoldLight)
                        ) {
                            Text("+ Add E-Way Bill")
                        }
                    }
                }
            } else {
                LazyColumn(
                    modifier = Modifier.fillMaxSize(),
                    contentPadding = PaddingValues(12.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    items(bills, key = { it.id }) { bill ->
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
                                            text = bill.clientName,
                                            fontSize = 14.sp,
                                            fontWeight = FontWeight.Bold,
                                            color = DeepMaroon
                                        )
                                        Text(
                                            text = "EWB No: ${bill.ewayBillNumber}",
                                            fontSize = 12.sp,
                                            color = RoyalGoldDark,
                                            fontWeight = FontWeight.SemiBold
                                        )
                                    }
                                    StatusBadge(status = bill.status)
                                }

                                Spacer(modifier = Modifier.height(8.dp))
                                Divider(color = IvoryOutlineVariant, thickness = 0.8.dp)
                                Spacer(modifier = Modifier.height(8.dp))

                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.SpaceBetween
                                ) {
                                    Column {
                                        Text(text = "Vehicle: ${bill.vehicleNumber}", fontSize = 12.sp, color = Color.DarkGray)
                                        Text(text = "Valid Till: ${bill.validityExpiryDate}", fontSize = 11.sp, color = StatusOverdueRed)
                                    }
                                    Column(horizontalAlignment = Alignment.End) {
                                        Text(text = "Doc: ${bill.documentNumber}", fontSize = 11.sp, color = Color.Gray)
                                        Text(
                                            text = "₹ ${"%,.0f".format(bill.consignmentValue)}",
                                            fontSize = 13.sp,
                                            fontWeight = FontWeight.Bold,
                                            color = DeepMaroon
                                        )
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
        var ewayNo by remember { mutableStateOf("") }
        var vehicleNo by remember { mutableStateOf("") }
        var invoiceNo by remember { mutableStateOf("") }
        var value by remember { mutableStateOf("250000") }
        var validTill by remember { mutableStateOf("2024-11-02 23:59") }

        AlertDialog(
            onDismissRequest = { showAddDialog = false },
            title = { Text("Generate / Record E-Way Bill") },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    OutlinedTextField(
                        value = ewayNo,
                        onValueChange = { ewayNo = it },
                        label = { Text("12-digit E-Way Bill Number") },
                        modifier = Modifier.fillMaxWidth()
                    )
                    OutlinedTextField(
                        value = vehicleNo,
                        onValueChange = { vehicleNo = it.uppercase() },
                        label = { Text("Vehicle Number (e.g. RJ14 GC 1234)") },
                        modifier = Modifier.fillMaxWidth()
                    )
                    OutlinedTextField(
                        value = invoiceNo,
                        onValueChange = { invoiceNo = it },
                        label = { Text("Invoice / Doc Number") },
                        modifier = Modifier.fillMaxWidth()
                    )
                    OutlinedTextField(
                        value = value,
                        onValueChange = { value = it },
                        label = { Text("Consignment Value (₹)") },
                        modifier = Modifier.fillMaxWidth()
                    )
                    OutlinedTextField(
                        value = validTill,
                        onValueChange = { validTill = it },
                        label = { Text("Validity Expiry (YYYY-MM-DD HH:mm)") },
                        modifier = Modifier.fillMaxWidth()
                    )
                }
            },
            confirmButton = {
                Button(onClick = {
                    val entity = EWayBillEntity(
                        clientId = selectedClient.clientId,
                        clientName = selectedClient.businessName,
                        gstin = selectedClient.primaryGstin,
                        ewayBillNumber = ewayNo.ifBlank { "${System.currentTimeMillis() % 1000000000000L}" },
                        generationDate = "2024-10-31 10:00",
                        validityExpiryDate = validTill,
                        vehicleNumber = vehicleNo.ifBlank { "RJ14 GA 5678" },
                        documentNumber = invoiceNo.ifBlank { "INV-001" },
                        consignmentValue = value.toDoubleOrNull() ?: 0.0,
                        status = "Active"
                    )
                    onAddBill(entity)
                    showAddDialog = false
                }) {
                    Text("Save E-Way Bill")
                }
            },
            dismissButton = {
                TextButton(onClick = { showAddDialog = false }) { Text("Cancel") }
            }
        )
    }
}
