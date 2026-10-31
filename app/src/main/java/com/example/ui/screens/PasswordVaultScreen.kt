package com.example.ui.screens

import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import android.widget.Toast
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
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.model.ClientEntity
import com.example.data.model.VaultCredentialEntity
import com.example.ui.theme.*

@Composable
fun PasswordVaultScreen(
    vaultEntries: List<VaultCredentialEntity>,
    clients: List<ClientEntity>,
    isVaultUnlocked: Boolean,
    isHindi: Boolean,
    onUnlockVault: (String) -> Boolean,
    onLockVault: () -> Unit,
    onDecryptPassword: (VaultCredentialEntity) -> String,
    onAddEntry: (String, String, String, String, String, String, String, String) -> Unit,
    onDeleteEntry: (VaultCredentialEntity) -> Unit
) {
    val context = LocalContext.current
    var pinInput by remember { mutableStateOf("") }
    var pinError by remember { mutableStateOf(false) }
    var showAddDialog by remember { mutableStateOf(false) }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(IvoryBackground)
            .testTag("password_vault_screen")
    ) {
        // Vault Header
        Surface(color = Color.White, tonalElevation = 2.dp, modifier = Modifier.fillMaxWidth()) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(14.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        imageVector = if (isVaultUnlocked) Icons.Default.LockOpen else Icons.Default.Lock,
                        contentDescription = null,
                        tint = if (isVaultUnlocked) StatusFiledGreen else DeepMaroon,
                        modifier = Modifier.size(26.dp)
                    )
                    Spacer(modifier = Modifier.width(10.dp))
                    Column {
                        Text(
                            text = if (isHindi) "सुरक्षित पासवर्ड वॉल्ट (AES-256 Vault)" else "Encrypted Government Portal Vault",
                            fontSize = 15.sp,
                            fontWeight = FontWeight.Bold,
                            color = DeepMaroon
                        )
                        Text(
                            text = if (isVaultUnlocked) "Vault Unlocked • AES-256 GCM Active" else "Protected • Enter Master PIN to View",
                            fontSize = 11.sp,
                            color = if (isVaultUnlocked) StatusFiledGreen else Color.Gray
                        )
                    }
                }

                if (isVaultUnlocked) {
                    IconButton(onClick = onLockVault) {
                        Icon(Icons.Default.Lock, contentDescription = "Lock Vault", tint = DeepMaroon)
                    }
                }
            }
        }

        if (!isVaultUnlocked) {
            // Lock Screen State
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(24.dp),
                contentAlignment = Alignment.Center
            ) {
                Surface(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(16.dp),
                    color = Color.White,
                    border = androidx.compose.foundation.BorderStroke(1.dp, RoyalGold),
                    tonalElevation = 4.dp
                ) {
                    Column(
                        modifier = Modifier.padding(24.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Icon(
                            imageVector = Icons.Default.Security,
                            contentDescription = null,
                            tint = DeepMaroon,
                            modifier = Modifier.size(48.dp)
                        )
                        Spacer(modifier = Modifier.height(10.dp))
                        Text(
                            text = if (isHindi) "मास्टर पिन दर्ज करें" else "Enter Master PIN",
                            fontSize = 16.sp,
                            fontWeight = FontWeight.Bold,
                            color = DeepMaroon
                        )
                        Text(
                            text = "GST, Income Tax, TRACES, E-Way & MCA Credentials",
                            fontSize = 11.sp,
                            color = Color.Gray
                        )

                        Spacer(modifier = Modifier.height(16.dp))

                        OutlinedTextField(
                            value = pinInput,
                            onValueChange = {
                                pinInput = it
                                pinError = false
                            },
                            label = { Text("Master PIN (Default: 1234)") },
                            visualTransformation = PasswordVisualTransformation(),
                            singleLine = true,
                            isError = pinError,
                            modifier = Modifier.fillMaxWidth().testTag("vault_pin_input")
                        )

                        if (pinError) {
                            Text(
                                text = "Incorrect PIN. Please re-try.",
                                color = StatusOverdueRed,
                                fontSize = 11.sp,
                                modifier = Modifier.padding(top = 4.dp)
                            )
                        }

                        Spacer(modifier = Modifier.height(16.dp))

                        Button(
                            onClick = {
                                val success = onUnlockVault(pinInput)
                                if (!success) {
                                    pinError = true
                                } else {
                                    pinInput = ""
                                }
                            },
                            colors = ButtonDefaults.buttonColors(containerColor = DeepMaroon, contentColor = RoyalGoldLight),
                            modifier = Modifier.fillMaxWidth().testTag("unlock_vault_button"),
                            shape = RoundedCornerShape(10.dp)
                        ) {
                            Text(if (isHindi) "वॉल्ट अनलॉक करें" else "Unlock Vault", fontWeight = FontWeight.Bold)
                        }
                    }
                }
            }
        } else {
            // Unlocked State - Show credentials list
            Scaffold(
                floatingActionButton = {
                    FloatingActionButton(
                        onClick = { showAddDialog = true },
                        containerColor = DeepMaroon,
                        contentColor = RoyalGoldLight,
                        modifier = Modifier.testTag("add_vault_fab")
                    ) {
                        Icon(Icons.Default.Add, contentDescription = "Add Credential")
                    }
                }
            ) { padding ->
                if (vaultEntries.isEmpty()) {
                    Box(
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(padding)
                            .padding(32.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            Icon(Icons.Default.VpnKey, contentDescription = null, tint = Color.Gray, modifier = Modifier.size(48.dp))
                            Spacer(modifier = Modifier.height(8.dp))
                            Text("No encrypted credentials saved.", color = Color.Gray)
                            Spacer(modifier = Modifier.height(8.dp))
                            Button(
                                onClick = { showAddDialog = true },
                                colors = ButtonDefaults.buttonColors(containerColor = DeepMaroon, contentColor = RoyalGoldLight)
                            ) {
                                Text("+ Store Portal Password")
                            }
                        }
                    }
                } else {
                    LazyColumn(
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(padding),
                        contentPadding = PaddingValues(12.dp),
                        verticalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        items(vaultEntries, key = { it.id }) { entry ->
                            VaultEntryCard(
                                entry = entry,
                                onDecrypt = { onDecryptPassword(entry) },
                                onDelete = { onDeleteEntry(entry) }
                            )
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
        var portalName by remember { mutableStateOf("GST Portal") }
        var loginId by remember { mutableStateOf("") }
        var password by remember { mutableStateOf("") }
        var regMobile by remember { mutableStateOf("") }
        var regEmail by remember { mutableStateOf("") }
        var notes by remember { mutableStateOf("") }

        AlertDialog(
            onDismissRequest = { showAddDialog = false },
            title = { Text("Store Encrypted Portal Password") },
            text = {
                LazyColumn(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    item {
                        OutlinedTextField(
                            value = selectedClient.businessName,
                            onValueChange = {},
                            label = { Text("Client") },
                            readOnly = true,
                            modifier = Modifier.fillMaxWidth()
                        )
                    }
                    item {
                        OutlinedTextField(
                            value = portalName,
                            onValueChange = { portalName = it },
                            label = { Text("Portal (GST, Income Tax, TRACES, E-Way, MCA)") },
                            modifier = Modifier.fillMaxWidth()
                        )
                    }
                    item {
                        OutlinedTextField(
                            value = loginId,
                            onValueChange = { loginId = it },
                            label = { Text("User ID / Login ID") },
                            modifier = Modifier.fillMaxWidth()
                        )
                    }
                    item {
                        OutlinedTextField(
                            value = password,
                            onValueChange = { password = it },
                            label = { Text("Password (Will be AES-256 Encrypted)") },
                            modifier = Modifier.fillMaxWidth()
                        )
                    }
                    item {
                        OutlinedTextField(
                            value = regMobile,
                            onValueChange = { regMobile = it },
                            label = { Text("Registered Mobile (for OTP)") },
                            modifier = Modifier.fillMaxWidth()
                        )
                    }
                    item {
                        OutlinedTextField(
                            value = notes,
                            onValueChange = { notes = it },
                            label = { Text("Security Notes / Hint") },
                            modifier = Modifier.fillMaxWidth()
                        )
                    }
                }
            },
            confirmButton = {
                Button(onClick = {
                    if (loginId.isNotBlank() && password.isNotBlank()) {
                        onAddEntry(
                            selectedClient.clientId,
                            selectedClient.businessName,
                            portalName,
                            loginId,
                            password,
                            regMobile,
                            regEmail,
                            notes
                        )
                        showAddDialog = false
                    }
                }) {
                    Text("Encrypt & Save")
                }
            },
            dismissButton = {
                TextButton(onClick = { showAddDialog = false }) { Text("Cancel") }
            }
        )
    }
}

@Composable
fun VaultEntryCard(
    entry: VaultCredentialEntity,
    onDecrypt: () -> String,
    onDelete: () -> Unit
) {
    val context = LocalContext.current
    var isRevealed by remember { mutableStateOf(false) }
    var decryptedPassword by remember { mutableStateOf("") }

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
                        text = entry.portalName,
                        fontSize = 14.sp,
                        fontWeight = FontWeight.Bold,
                        color = DeepMaroon
                    )
                    Text(
                        text = "Client: ${entry.clientName}",
                        fontSize = 11.sp,
                        color = Color.Gray
                    )
                }

                IconButton(onClick = onDelete) {
                    Icon(Icons.Default.DeleteOutline, contentDescription = "Delete", tint = Color.Gray)
                }
            }

            Spacer(modifier = Modifier.height(6.dp))

            Surface(
                color = IvorySurfaceVariant,
                shape = RoundedCornerShape(8.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(modifier = Modifier.padding(10.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = "Login ID: ${entry.loginId}",
                            fontSize = 12.sp,
                            fontWeight = FontWeight.SemiBold,
                            color = Color.DarkGray
                        )
                        IconButton(
                            onClick = {
                                val clipboard = context.getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
                                clipboard.setPrimaryClip(ClipData.newPlainText("Login ID", entry.loginId))
                                Toast.makeText(context, "Login ID copied", Toast.LENGTH_SHORT).show()
                            },
                            modifier = Modifier.size(24.dp)
                        ) {
                            Icon(Icons.Default.ContentCopy, contentDescription = "Copy Login ID", tint = DeepMaroon, modifier = Modifier.size(16.dp))
                        }
                    }

                    Spacer(modifier = Modifier.height(4.dp))

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = if (isRevealed) "Password: $decryptedPassword" else "Password: ••••••••••••",
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Bold,
                            color = DeepMaroon
                        )

                        Row {
                            IconButton(
                                onClick = {
                                    if (!isRevealed) {
                                        decryptedPassword = onDecrypt()
                                        isRevealed = true
                                    } else {
                                        isRevealed = false
                                        decryptedPassword = ""
                                    }
                                },
                                modifier = Modifier.size(24.dp)
                            ) {
                                Icon(
                                    imageVector = if (isRevealed) Icons.Default.VisibilityOff else Icons.Default.Visibility,
                                    contentDescription = "Toggle Visibility",
                                    tint = DeepMaroon,
                                    modifier = Modifier.size(16.dp)
                                )
                            }

                            Spacer(modifier = Modifier.width(6.dp))

                            IconButton(
                                onClick = {
                                    val plain = if (decryptedPassword.isNotEmpty()) decryptedPassword else onDecrypt()
                                    val clipboard = context.getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
                                    clipboard.setPrimaryClip(ClipData.newPlainText("Password", plain))
                                    Toast.makeText(context, "Password copied to clipboard", Toast.LENGTH_SHORT).show()
                                },
                                modifier = Modifier.size(24.dp)
                            ) {
                                Icon(Icons.Default.ContentCopy, contentDescription = "Copy Password", tint = RoyalGoldDark, modifier = Modifier.size(16.dp))
                            }
                        }
                    }
                }
            }

            if (entry.registeredMobile.isNotEmpty() || entry.securityNotes.isNotEmpty()) {
                Spacer(modifier = Modifier.height(6.dp))
                Text(
                    text = "Reg Mobile: ${entry.registeredMobile} ${if (entry.securityNotes.isNotEmpty()) "• ${entry.securityNotes}" else ""}",
                    fontSize = 10.sp,
                    color = Color.Gray
                )
            }
        }
    }
}
