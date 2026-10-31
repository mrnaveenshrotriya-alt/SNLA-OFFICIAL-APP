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
import com.example.data.model.ClientDocumentEntity
import com.example.data.model.ClientEntity
import com.example.ui.components.StatusBadge
import com.example.ui.theme.*

@Composable
fun DocumentsScreen(
    documents: List<ClientDocumentEntity>,
    clients: List<ClientEntity>,
    isHindi: Boolean,
    onAddDocument: (ClientDocumentEntity) -> Unit,
    onDeleteDocument: (ClientDocumentEntity) -> Unit
) {
    var selectedCategory by remember { mutableStateOf("All") }
    var showAddDialog by remember { mutableStateOf(false) }

    val filtered = documents.filter {
        if (selectedCategory == "All") true else it.documentCategory.contains(selectedCategory, ignoreCase = true)
    }

    Scaffold(
        floatingActionButton = {
            FloatingActionButton(
                onClick = { showAddDialog = true },
                containerColor = DeepMaroon,
                contentColor = RoyalGoldLight,
                modifier = Modifier.testTag("add_doc_fab")
            ) {
                Icon(Icons.Default.UploadFile, contentDescription = "Upload Document")
            }
        }
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .background(IvoryBackground)
                .testTag("documents_screen")
        ) {
            Surface(color = Color.White, tonalElevation = 2.dp, modifier = Modifier.fillMaxWidth()) {
                Column(modifier = Modifier.padding(14.dp)) {
                    Text(
                        text = if (isHindi) "क्लाइंट दस्तावेज प्रबंधन (Client Documents)" else "Client Tax Documents & Vault",
                        fontSize = 15.sp,
                        fontWeight = FontWeight.Bold,
                        color = DeepMaroon
                    )
                    Text(
                        text = "PAN • GST Certificate • ITR-V • Tax Audit Reports • Financials • Challans",
                        fontSize = 11.sp,
                        color = Color.Gray
                    )

                    Spacer(modifier = Modifier.height(10.dp))

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        listOf("All", "GST", "ITR", "Audit", "Financials").forEach { cat ->
                            FilterChip(
                                selected = selectedCategory == cat,
                                onClick = { selectedCategory = cat },
                                label = { Text(cat, fontSize = 11.sp) },
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
                        Icon(Icons.Default.FolderOpen, contentDescription = null, tint = Color.Gray, modifier = Modifier.size(48.dp))
                        Spacer(modifier = Modifier.height(8.dp))
                        Text("No documents stored in this category.", color = Color.Gray)
                        Spacer(modifier = Modifier.height(8.dp))
                        Button(
                            onClick = { showAddDialog = true },
                            colors = ButtonDefaults.buttonColors(containerColor = DeepMaroon, contentColor = RoyalGoldLight)
                        ) {
                            Text("+ Attach Document")
                        }
                    }
                }
            } else {
                LazyColumn(
                    modifier = Modifier.fillMaxSize(),
                    contentPadding = PaddingValues(12.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    items(filtered, key = { it.id }) { doc ->
                        Surface(
                            modifier = Modifier.fillMaxWidth(),
                            shape = RoundedCornerShape(12.dp),
                            color = Color.White,
                            border = androidx.compose.foundation.BorderStroke(1.dp, IvoryOutline),
                            tonalElevation = 2.dp
                        ) {
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(14.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Icon(Icons.Default.Description, contentDescription = null, tint = DeepMaroon, modifier = Modifier.size(32.dp))
                                Spacer(modifier = Modifier.width(12.dp))
                                Column(modifier = Modifier.weight(1f)) {
                                    Text(
                                        text = doc.title,
                                        fontSize = 14.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = DeepMaroon
                                    )
                                    Text(
                                        text = "${doc.clientName} • Category: ${doc.documentCategory}",
                                        fontSize = 12.sp,
                                        color = Color.DarkGray
                                    )
                                    Text(
                                        text = "FY ${doc.financialYear} • Uploaded on: ${doc.uploadDate}",
                                        fontSize = 10.sp,
                                        color = Color.Gray
                                    )
                                }
                                IconButton(onClick = { onDeleteDocument(doc) }) {
                                    Icon(Icons.Default.DeleteOutline, contentDescription = "Delete", tint = Color.Gray)
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
        var docTitle by remember { mutableStateOf("GST Registration Certificate REG-06") }
        var category by remember { mutableStateOf("GST Certificate") }
        var fy by remember { mutableStateOf("2024-25") }

        AlertDialog(
            onDismissRequest = { showAddDialog = false },
            title = { Text("Upload Client Document") },
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
                        value = docTitle,
                        onValueChange = { docTitle = it },
                        label = { Text("Document Title") },
                        modifier = Modifier.fillMaxWidth()
                    )
                    OutlinedTextField(
                        value = category,
                        onValueChange = { category = it },
                        label = { Text("Category (PAN, GST, ITR-V, Audit, Financials)") },
                        modifier = Modifier.fillMaxWidth()
                    )
                    OutlinedTextField(
                        value = fy,
                        onValueChange = { fy = it },
                        label = { Text("Financial Year") },
                        modifier = Modifier.fillMaxWidth()
                    )
                }
            },
            confirmButton = {
                Button(onClick = {
                    val doc = ClientDocumentEntity(
                        clientId = selectedClient.clientId,
                        clientName = selectedClient.businessName,
                        documentCategory = category,
                        title = docTitle,
                        financialYear = fy
                    )
                    onAddDocument(doc)
                    showAddDialog = false
                }) {
                    Text("Attach Document")
                }
            },
            dismissButton = {
                TextButton(onClick = { showAddDialog = false }) { Text("Cancel") }
            }
        )
    }
}
