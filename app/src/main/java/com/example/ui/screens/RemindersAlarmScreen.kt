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
import com.example.data.model.TaxReminderEntity
import com.example.service.TaxReminderAlarmReceiver
import com.example.ui.theme.*

@Composable
fun RemindersAlarmScreen(
    reminders: List<TaxReminderEntity>,
    isHindi: Boolean,
    onTestAlarm: (String, String) -> Unit,
    onSnooze: (Long) -> Unit,
    onMarkFiled: (Long) -> Unit,
    onAddReminder: (TaxReminderEntity) -> Unit
) {
    val context = LocalContext.current
    var showAddDialog by remember { mutableStateOf(false) }

    Scaffold(
        floatingActionButton = {
            FloatingActionButton(
                onClick = { showAddDialog = true },
                containerColor = DeepMaroon,
                contentColor = RoyalGoldLight,
                modifier = Modifier.testTag("add_reminder_fab")
            ) {
                Icon(Icons.Default.AlarmAdd, contentDescription = "Schedule Reminder")
            }
        }
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .background(IvoryBackground)
                .testTag("reminders_screen")
        ) {
            Surface(color = Color.White, tonalElevation = 2.dp, modifier = Modifier.fillMaxWidth()) {
                Column(modifier = Modifier.padding(14.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column {
                            Text(
                                text = if (isHindi) "स्वचालित अनुस्मारक एवं अलार्म (Automated Alarms)" else "Automated Reminders & Ringing Alarms",
                                fontSize = 15.sp,
                                fontWeight = FontWeight.Bold,
                                color = DeepMaroon
                            )
                            Text(
                                text = "15d, 7d, 3d, 1d, On Due Date & Daily after until filed",
                                fontSize = 11.sp,
                                color = Color.Gray
                            )
                        }

                        // Test Ringing Alarm Button
                        Button(
                            onClick = {
                                onTestAlarm("Marudhar Textiles", "GSTR-3B")
                            },
                            colors = ButtonDefaults.buttonColors(containerColor = StatusOverdueRed, contentColor = Color.White),
                            shape = RoundedCornerShape(20.dp),
                            contentPadding = PaddingValues(horizontal = 10.dp, vertical = 4.dp)
                        ) {
                            Icon(Icons.Default.NotificationsActive, contentDescription = null, modifier = Modifier.size(16.dp))
                            Spacer(modifier = Modifier.width(4.dp))
                            Text("Test Ring", fontSize = 11.sp)
                        }
                    }

                    Spacer(modifier = Modifier.height(6.dp))

                    // Stop Alarm Button in case one is currently ringing
                    OutlinedButton(
                        onClick = { TaxReminderAlarmReceiver.stopActiveAlarm() },
                        colors = ButtonDefaults.outlinedButtonColors(contentColor = DeepMaroon),
                        border = androidx.compose.foundation.BorderStroke(1.dp, DeepMaroon),
                        shape = RoundedCornerShape(8.dp),
                        modifier = Modifier.fillMaxWidth(),
                        contentPadding = PaddingValues(vertical = 4.dp)
                    ) {
                        Icon(Icons.Default.VolumeOff, contentDescription = null, modifier = Modifier.size(16.dp))
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(if (isHindi) "सक्रिय अलार्म बंद करें (Silence Ringing Alarm)" else "Silence / Stop Active Ringing Alarm", fontSize = 12.sp)
                    }
                }
            }

            if (reminders.isEmpty()) {
                Box(modifier = Modifier.fillMaxSize().padding(32.dp), contentAlignment = Alignment.Center) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Icon(Icons.Default.AlarmOn, contentDescription = null, tint = Color.Gray, modifier = Modifier.size(48.dp))
                        Spacer(modifier = Modifier.height(8.dp))
                        Text("No active reminders configured.", color = Color.Gray)
                        Spacer(modifier = Modifier.height(8.dp))
                        Button(
                            onClick = { showAddDialog = true },
                            colors = ButtonDefaults.buttonColors(containerColor = DeepMaroon, contentColor = RoyalGoldLight)
                        ) {
                            Text("+ Add Alarm Reminder")
                        }
                    }
                }
            } else {
                LazyColumn(
                    modifier = Modifier.fillMaxSize(),
                    contentPadding = PaddingValues(12.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    items(reminders, key = { it.id }) { item ->
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
                                            text = "${item.returnType} • Period: ${item.taxPeriod}",
                                            fontSize = 12.sp,
                                            color = RoyalGoldDark,
                                            fontWeight = FontWeight.SemiBold
                                        )
                                    }

                                    Surface(
                                        shape = RoundedCornerShape(8.dp),
                                        color = if (item.isAlarmRinging) StatusOverdueRedContainer else StatusPendingAmberContainer
                                    ) {
                                        Text(
                                            text = if (item.isAlarmRinging) "Ringing Alarm" else "Notification",
                                            color = if (item.isAlarmRinging) StatusOverdueRed else StatusPendingAmber,
                                            fontSize = 11.sp,
                                            fontWeight = FontWeight.Bold,
                                            modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                                        )
                                    }
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
                                            text = "Due: ${item.dueDate} at ${item.reminderTime}",
                                            fontSize = 12.sp,
                                            color = Color.DarkGray
                                        )
                                        Text(
                                            text = "Trigger Schedule: ${item.reminderType.replace('_', ' ')}",
                                            fontSize = 10.sp,
                                            color = Color.Gray
                                        )
                                    }

                                    Row {
                                        OutlinedButton(
                                            onClick = { onSnooze(item.id) },
                                            shape = RoundedCornerShape(8.dp),
                                            contentPadding = PaddingValues(horizontal = 8.dp, vertical = 2.dp)
                                        ) {
                                            Text("Snooze (10m)", fontSize = 11.sp, color = DeepMaroon)
                                        }

                                        Spacer(modifier = Modifier.width(6.dp))

                                        Button(
                                            onClick = { onMarkFiled(item.id) },
                                            colors = ButtonDefaults.buttonColors(containerColor = StatusFiledGreen, contentColor = Color.White),
                                            shape = RoundedCornerShape(8.dp),
                                            contentPadding = PaddingValues(horizontal = 8.dp, vertical = 2.dp)
                                        ) {
                                            Text("Mark Filed", fontSize = 11.sp)
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
        var clientName by remember { mutableStateOf("Marudhar Textiles") }
        var returnType by remember { mutableStateOf("GSTR-3B") }
        var dueDate by remember { mutableStateOf("2024-11-20") }
        var reminderType by remember { mutableStateOf("3_DAYS") }
        var isRinging by remember { mutableStateOf(true) }

        AlertDialog(
            onDismissRequest = { showAddDialog = false },
            title = { Text("Schedule Compliance Alarm") },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    OutlinedTextField(
                        value = clientName,
                        onValueChange = { clientName = it },
                        label = { Text("Client Name") },
                        modifier = Modifier.fillMaxWidth()
                    )
                    OutlinedTextField(
                        value = returnType,
                        onValueChange = { returnType = it },
                        label = { Text("Return Type (GSTR-1, 3B, ITR, TDS)") },
                        modifier = Modifier.fillMaxWidth()
                    )
                    OutlinedTextField(
                        value = dueDate,
                        onValueChange = { dueDate = it },
                        label = { Text("Due Date (YYYY-MM-DD)") },
                        modifier = Modifier.fillMaxWidth()
                    )
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Checkbox(checked = isRinging, onCheckedChange = { isRinging = it })
                        Text("Ring Alarm Sound & Vibrate", fontSize = 12.sp)
                    }
                }
            },
            confirmButton = {
                Button(onClick = {
                    val rem = TaxReminderEntity(
                        clientId = "CLIENT-1",
                        clientName = clientName,
                        returnType = returnType,
                        taxPeriod = "Current",
                        dueDate = dueDate,
                        reminderType = reminderType,
                        isAlarmRinging = isRinging
                    )
                    onAddReminder(rem)
                    showAddDialog = false
                }) {
                    Text("Schedule Alarm")
                }
            },
            dismissButton = {
                TextButton(onClick = { showAddDialog = false }) { Text("Cancel") }
            }
        )
    }
}
