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
import com.example.data.model.StatutoryDueDateEntity
import com.example.ui.theme.*
import java.text.SimpleDateFormat
import java.util.*
import java.util.concurrent.TimeUnit

@Composable
fun DueDateCalendarScreen(
    dueDates: List<StatutoryDueDateEntity>,
    isHindi: Boolean,
    onUpdateDueDate: (StatutoryDueDateEntity) -> Unit
) {
    var selectedCategory by remember { mutableStateOf("All") }
    var editingDueDate by remember { mutableStateOf<StatutoryDueDateEntity?>(null) }

    val filtered = dueDates.filter {
        if (selectedCategory == "All") true else it.complianceCategory == selectedCategory
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(IvoryBackground)
            .testTag("due_date_calendar_screen")
    ) {
        Surface(color = Color.White, tonalElevation = 2.dp, modifier = Modifier.fillMaxWidth()) {
            Column(modifier = Modifier.padding(14.dp)) {
                Text(
                    text = if (isHindi) "भारतीय कर अनुपालन कैलेंडर (Tax Due Date Calendar)" else "Indian Statutory Tax Due Date Calendar",
                    fontSize = 15.sp,
                    fontWeight = FontWeight.Bold,
                    color = DeepMaroon
                )
                Text(
                    text = "Official Government Notified Deadlines • GST, Income Tax, TDS, Advance Tax",
                    fontSize = 11.sp,
                    color = Color.Gray
                )

                Spacer(modifier = Modifier.height(10.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    listOf("All", "GST", "Income Tax", "TDS/TCS", "Advance Tax").forEach { cat ->
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

        LazyColumn(
            modifier = Modifier.fillMaxSize(),
            contentPadding = PaddingValues(12.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            items(filtered, key = { it.id }) { item ->
                val finalDateStr = item.extendedDueDate.ifEmpty { item.originalDueDate }
                val daysRemaining = calculateDaysRemaining(finalDateStr)

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
                                    text = item.title,
                                    fontSize = 14.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = DeepMaroon
                                )
                                Text(
                                    text = item.periodDescription,
                                    fontSize = 12.sp,
                                    color = Color.DarkGray
                                )
                            }

                            // Days remaining badge
                            Surface(
                                shape = RoundedCornerShape(8.dp),
                                color = when {
                                    daysRemaining < 0 -> StatusOverdueRedContainer
                                    daysRemaining <= 5 -> StatusPendingAmberContainer
                                    else -> StatusFiledGreenContainer
                                }
                            ) {
                                Text(
                                    text = when {
                                        daysRemaining < 0 -> "${-daysRemaining}d Overdue"
                                        daysRemaining == 0L -> "Due Today!"
                                        else -> "$daysRemaining days left"
                                    },
                                    fontSize = 11.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = when {
                                        daysRemaining < 0 -> StatusOverdueRed
                                        daysRemaining <= 5 -> StatusPendingAmber
                                        else -> StatusFiledGreen
                                    },
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
                                    text = "Due Date: $finalDateStr",
                                    fontSize = 12.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = StatusOverdueRed
                                )
                                if (item.extendedDueDate.isNotEmpty()) {
                                    Text(
                                        text = "Original: ${item.originalDueDate} (Extended by Govt)",
                                        fontSize = 10.sp,
                                        color = Color.Gray
                                    )
                                }
                                Text(
                                    text = "Ref: ${item.notificationRef}",
                                    fontSize = 10.sp,
                                    color = RoyalGoldDark
                                )
                            }

                            IconButton(onClick = { editingDueDate = item }) {
                                Icon(Icons.Default.EditCalendar, contentDescription = "Edit Due Date", tint = DeepMaroon)
                            }
                        }
                    }
                }
            }
        }
    }

    if (editingDueDate != null) {
        val current = editingDueDate!!
        var newDate by remember { mutableStateOf(current.extendedDueDate.ifEmpty { current.originalDueDate }) }
        var newRef by remember { mutableStateOf(current.notificationRef) }

        AlertDialog(
            onDismissRequest = { editingDueDate = null },
            title = { Text("Update Due Date (Govt Extension)") },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    Text(current.title, fontWeight = FontWeight.Bold, color = DeepMaroon)
                    OutlinedTextField(
                        value = newDate,
                        onValueChange = { newDate = it },
                        label = { Text("Extended Due Date (YYYY-MM-DD)") },
                        modifier = Modifier.fillMaxWidth()
                    )
                    OutlinedTextField(
                        value = newRef,
                        onValueChange = { newRef = it },
                        label = { Text("Official Notification Ref / Circular") },
                        modifier = Modifier.fillMaxWidth()
                    )
                }
            },
            confirmButton = {
                Button(onClick = {
                    onUpdateDueDate(
                        current.copy(
                            extendedDueDate = newDate,
                            notificationRef = newRef
                        )
                    )
                    editingDueDate = null
                }) {
                    Text("Save Update")
                }
            },
            dismissButton = {
                TextButton(onClick = { editingDueDate = null }) { Text("Cancel") }
            }
        )
    }
}

private fun calculateDaysRemaining(dueDateStr: String): Long {
    val sdf = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())
    return try {
        val due = sdf.parse(dueDateStr) ?: return 0
        val diff = due.time - System.currentTimeMillis()
        TimeUnit.MILLISECONDS.toDays(diff)
    } catch (e: Exception) {
        0
    }
}
