package com.example.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
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
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.model.ClientEntity
import com.example.service.CsvExportHelper
import com.example.service.PdfReportGenerator
import com.example.ui.components.StatusBadge
import com.example.ui.theme.*

@Composable
fun ClientsScreen(
    clients: List<ClientEntity>,
    searchQuery: String,
    isHindi: Boolean,
    onSearchQueryChange: (String) -> Unit,
    onSelectClient: (ClientEntity) -> Unit,
    onAddClient: (ClientEntity) -> Unit,
    onDeleteClient: (ClientEntity) -> Unit
) {
    val context = LocalContext.current
    var showAddDialog by remember { mutableStateOf(false) }
    var selectedFilter by remember { mutableStateOf("All") }

    val filteredClients = clients.filter {
        val matchesQuery = it.businessName.contains(searchQuery, ignoreCase = true) ||
                it.pan.contains(searchQuery, ignoreCase = true) ||
                it.primaryGstin.contains(searchQuery, ignoreCase = true) ||
                it.mobileNumber.contains(searchQuery)
        val matchesFilter = when (selectedFilter) {
            "Active" -> it.status == "Active"
            "Regular" -> !it.constitution.contains("Composition", ignoreCase = true)
            "Composition" -> it.constitution.contains("Composition", ignoreCase = true)
            else -> true
        }
        matchesQuery && matchesFilter
    }

    Scaffold(
        floatingActionButton = {
            FloatingActionButton(
                onClick = { showAddDialog = true },
                containerColor = DeepMaroon,
                contentColor = RoyalGoldLight,
                modifier = Modifier.testTag("add_client_fab")
            ) {
                Icon(Icons.Default.PersonAdd, contentDescription = "Add Client")
            }
        }
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .background(IvoryBackground)
                .testTag("clients_screen")
        ) {
            // Search & Action Header
            Surface(
                color = Color.White,
                tonalElevation = 2.dp,
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(modifier = Modifier.padding(12.dp)) {
                    OutlinedTextField(
                        value = searchQuery,
                        onValueChange = onSearchQueryChange,
                        modifier = Modifier
                            .fillMaxWidth()
                            .testTag("client_search_input"),
                        placeholder = { Text(if (isHindi) "क्लाइंट नाम, पैन या जीएसटी खोजें..." else "Search Client Name, PAN, GSTIN...") },
                        leadingIcon = { Icon(Icons.Default.Search, contentDescription = null, tint = DeepMaroon) },
                        trailingIcon = {
                            if (searchQuery.isNotEmpty()) {
                                IconButton(onClick = { onSearchQueryChange("") }) {
                                    Icon(Icons.Default.Clear, contentDescription = "Clear")
                                }
                            }
                        },
                        singleLine = true,
                        shape = RoundedCornerShape(12.dp)
                    )

                    Spacer(modifier = Modifier.height(8.dp))

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        // Filter chips
                        Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                            listOf("All", "Active", "Regular", "Composition").forEach { filter ->
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

                        // Export CSV & PDF Actions
                        Row {
                            IconButton(onClick = {
                                val file = CsvExportHelper.exportClientsToCsv(context, filteredClients)
                                CsvExportHelper.shareCsvFile(context, file, "Shree Navya Lekha Client Master Export")
                            }) {
                                Icon(Icons.Default.FileDownload, contentDescription = "Export Excel/CSV", tint = DeepMaroon)
                            }

                            IconButton(onClick = {
                                val file = PdfReportGenerator.generateClientMasterPdf(context, filteredClients)
                                PdfReportGenerator.viewOrSharePdf(context, file)
                            }) {
                                Icon(Icons.Default.Print, contentDescription = "Print PDF", tint = RoyalGoldDark)
                            }
                        }
                    }
                }
            }

            // Client List
            if (filteredClients.isEmpty()) {
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(32.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Icon(
                            imageVector = Icons.Default.PersonOff,
                            contentDescription = null,
                            tint = Color.Gray,
                            modifier = Modifier.size(54.dp)
                        )
                        Spacer(modifier = Modifier.height(8.dp))
                        Text(
                            text = if (isHindi) "कोई क्लाइंट नहीं मिला" else "No clients found",
                            fontSize = 15.sp,
                            fontWeight = FontWeight.SemiBold,
                            color = Color.DarkGray
                        )
                        Text(
                            text = if (isHindi) "नया क्लाइंट जोड़ने के लिए '+' दबाएं" else "Tap '+' to register your first tax client",
                            fontSize = 12.sp,
                            color = Color.Gray
                        )
                    }
                }
            } else {
                LazyColumn(
                    modifier = Modifier.fillMaxSize(),
                    contentPadding = PaddingValues(12.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    items(filteredClients, key = { it.clientId }) { client ->
                        ClientListItem(
                            client = client,
                            onClick = { onSelectClient(client) }
                        )
                    }
                }
            }
        }
    }

    if (showAddDialog) {
        AddClientDialog(
            isHindi = isHindi,
            onDismiss = { showAddDialog = false },
            onConfirm = { newClient ->
                onAddClient(newClient)
                showAddDialog = false
            }
        )
    }
}

@Composable
fun ClientListItem(
    client: ClientEntity,
    onClick: () -> Unit
) {
    Surface(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(12.dp))
            .clickable { onClick() }
            .testTag("client_item_${client.clientId}"),
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
                        text = client.businessName,
                        fontSize = 15.sp,
                        fontWeight = FontWeight.Bold,
                        color = DeepMaroon
                    )
                    Text(
                        text = "${client.proprietorName} • ${client.businessType}",
                        fontSize = 12.sp,
                        color = Color.DarkGray
                    )
                }
                StatusBadge(status = client.status)
            }

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
                        text = "PAN: ${client.pan}",
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Medium,
                        color = RoyalGoldDark
                    )
                    if (client.primaryGstin.isNotEmpty()) {
                        Text(
                            text = "GSTIN: ${client.primaryGstin}",
                            fontSize = 11.sp,
                            color = Color.Gray
                        )
                    }
                }

                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        imageVector = Icons.Default.Phone,
                        contentDescription = null,
                        tint = DeepMaroon,
                        modifier = Modifier.size(14.dp)
                    )
                    Spacer(modifier = Modifier.width(4.dp))
                    Text(
                        text = client.mobileNumber,
                        fontSize = 12.sp,
                        fontWeight = FontWeight.SemiBold,
                        color = DeepMaroon
                    )
                }
            }
        }
    }
}

@Composable
fun AddClientDialog(
    isHindi: Boolean,
    onDismiss: () -> Unit,
    onConfirm: (ClientEntity) -> Unit
) {
    var businessName by remember { mutableStateOf("") }
    var proprietorName by remember { mutableStateOf("") }
    var pan by remember { mutableStateOf("") }
    var gstin by remember { mutableStateOf("") }
    var mobile by remember { mutableStateOf("") }
    var email by remember { mutableStateOf("") }
    var address by remember { mutableStateOf("") }
    var businessType by remember { mutableStateOf("Proprietorship") }
    var constitution by remember { mutableStateOf("Trader") }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Text(
                text = if (isHindi) "नया क्लाइंट जोड़ें (Add Client)" else "Register New Tax Client",
                fontWeight = FontWeight.Bold,
                color = DeepMaroon
            )
        },
        text = {
            LazyColumn(
                modifier = Modifier.fillMaxWidth(),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                item {
                    OutlinedTextField(
                        value = businessName,
                        onValueChange = { businessName = it },
                        label = { Text("Company / Firm / Trade Name *") },
                        modifier = Modifier.fillMaxWidth()
                    )
                }
                item {
                    OutlinedTextField(
                        value = proprietorName,
                        onValueChange = { proprietorName = it },
                        label = { Text("Proprietor / Director / Partner *") },
                        modifier = Modifier.fillMaxWidth()
                    )
                }
                item {
                    Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                        OutlinedTextField(
                            value = pan,
                            onValueChange = { pan = it.uppercase() },
                            label = { Text("PAN *") },
                            modifier = Modifier.weight(1f)
                        )
                        OutlinedTextField(
                            value = mobile,
                            onValueChange = { mobile = it },
                            label = { Text("Mobile *") },
                            modifier = Modifier.weight(1f)
                        )
                    }
                }
                item {
                    OutlinedTextField(
                        value = gstin,
                        onValueChange = { gstin = it.uppercase() },
                        label = { Text("Primary GSTIN (Optional)") },
                        modifier = Modifier.fillMaxWidth()
                    )
                }
                item {
                    OutlinedTextField(
                        value = email,
                        onValueChange = { email = it },
                        label = { Text("Email Address") },
                        modifier = Modifier.fillMaxWidth()
                    )
                }
                item {
                    OutlinedTextField(
                        value = address,
                        onValueChange = { address = it },
                        label = { Text("Address / City / District") },
                        modifier = Modifier.fillMaxWidth()
                    )
                }
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    if (businessName.isNotBlank() && pan.isNotBlank()) {
                        val autoId = "SNL-${System.currentTimeMillis() % 10000}"
                        val client = ClientEntity(
                            clientId = autoId,
                            businessName = businessName,
                            proprietorName = proprietorName.ifBlank { businessName },
                            businessType = businessType,
                            constitution = constitution,
                            address = address.ifBlank { "Jaipur, Rajasthan" },
                            mobileNumber = mobile.ifBlank { "9829000000" },
                            whatsAppNumber = mobile.ifBlank { "9829000000" },
                            emailAddress = email.ifBlank { "client@example.com" },
                            pan = pan,
                            primaryGstin = gstin,
                            status = "Active"
                        )
                        onConfirm(client)
                    }
                },
                colors = ButtonDefaults.buttonColors(containerColor = DeepMaroon, contentColor = RoyalGoldLight)
            ) {
                Text(if (isHindi) "सुरक्षित करें" else "Save Client")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text(if (isHindi) "रद्द करें" else "Cancel")
            }
        }
    )
}
