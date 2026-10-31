package com.example.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.model.GstReturnEntity
import com.example.data.model.StatutoryDueDateEntity
import com.example.ui.components.StatusBadge
import com.example.ui.navigation.TaxRoute
import com.example.ui.theme.*
import com.example.ui.viewmodel.DashboardStats

@Composable
fun DashboardScreen(
    stats: DashboardStats,
    recentGstReturns: List<GstReturnEntity>,
    dueDates: List<StatutoryDueDateEntity>,
    isHindi: Boolean,
    onNavigate: (TaxRoute) -> Unit,
    onQuickAddClient: () -> Unit,
    onQuickAddReturn: () -> Unit,
    onQuickAddPayment: () -> Unit,
    onTriggerAlarmTest: () -> Unit
) {
    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .background(IvoryBackground)
            .testTag("dashboard_screen"),
        contentPadding = PaddingValues(bottom = 80.dp)
    ) {
        // Hero Firm Banner
        item {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(
                        Brush.verticalGradient(
                            listOf(DeepMaroon, DeepMaroonDark)
                        )
                    )
                    .padding(16.dp)
            ) {
                Column {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column {
                            Text(
                                text = if (isHindi) "नमस्ते, कर सलाहकार" else "Welcome, Tax Advisor",
                                color = RoyalGoldLight,
                                fontSize = 12.sp,
                                fontWeight = FontWeight.SemiBold
                            )
                            Text(
                                text = "SHREE NAVYA LEKHA ASSOCIATES",
                                color = Color.White,
                                fontSize = 16.sp,
                                fontWeight = FontWeight.Bold,
                                letterSpacing = 0.5.sp
                            )
                        }

                        // Alarm Test Button
                        Button(
                            onClick = onTriggerAlarmTest,
                            colors = ButtonDefaults.buttonColors(
                                containerColor = RoyalGold,
                                contentColor = DeepMaroonDark
                            ),
                            shape = RoundedCornerShape(20.dp),
                            contentPadding = PaddingValues(horizontal = 12.dp, vertical = 6.dp),
                            modifier = Modifier.testTag("test_alarm_button")
                        ) {
                            Icon(
                                imageVector = Icons.Default.NotificationsActive,
                                contentDescription = null,
                                modifier = Modifier.size(16.dp)
                            )
                            Spacer(modifier = Modifier.width(4.dp))
                            Text(
                                text = if (isHindi) "अलार्म टेस्ट" else "Test Alarm",
                                fontSize = 12.sp,
                                fontWeight = FontWeight.Bold
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(14.dp))

                    // Outstanding Collections Bar
                    Surface(
                        modifier = Modifier.fillMaxWidth(),
                        color = Color.White.copy(alpha = 0.12f),
                        shape = RoundedCornerShape(12.dp),
                        border = androidx.compose.foundation.BorderStroke(1.dp, RoyalGold.copy(alpha = 0.5f))
                    ) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(12.dp),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Icon(
                                    imageVector = Icons.Default.AccountBalanceWallet,
                                    contentDescription = null,
                                    tint = RoyalGoldLight
                                )
                                Spacer(modifier = Modifier.width(8.dp))
                                Column {
                                    Text(
                                        text = if (isHindi) "कुल बकाया फीस" else "Pending Client Fees",
                                        color = Color.White.copy(alpha = 0.8f),
                                        fontSize = 11.sp
                                    )
                                    Text(
                                        text = "₹ ${"%,.2f".format(stats.totalPendingFees)}",
                                        color = RoyalGoldLight,
                                        fontSize = 16.sp,
                                        fontWeight = FontWeight.Bold
                                    )
                                }
                            }

                            OutlinedButton(
                                onClick = { onNavigate(TaxRoute.FeesPayment) },
                                colors = ButtonDefaults.outlinedButtonColors(contentColor = Color.White),
                                border = androidx.compose.foundation.BorderStroke(1.dp, Color.White.copy(alpha = 0.6f)),
                                shape = RoundedCornerShape(8.dp),
                                contentPadding = PaddingValues(horizontal = 10.dp, vertical = 4.dp)
                            ) {
                                Text(if (isHindi) "खाता देखें" else "View Ledger", fontSize = 11.sp)
                            }
                        }
                    }
                }
            }
        }

        // Quick Actions Row
        item {
            Column(modifier = Modifier.padding(16.dp)) {
                Text(
                    text = if (isHindi) "त्वरित कार्य (Quick Actions)" else "Quick Actions",
                    fontSize = 14.sp,
                    fontWeight = FontWeight.Bold,
                    color = DeepMaroon
                )
                Spacer(modifier = Modifier.height(10.dp))
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    QuickActionButton(
                        icon = Icons.Default.PersonAdd,
                        label = if (isHindi) "नया क्लाइंट" else "+ Client",
                        modifier = Modifier.weight(1f),
                        onClick = onQuickAddClient
                    )
                    QuickActionButton(
                        icon = Icons.Default.PostAdd,
                        label = if (isHindi) "रिटर्न जोड़ें" else "+ Return",
                        modifier = Modifier.weight(1f),
                        onClick = onQuickAddReturn
                    )
                    QuickActionButton(
                        icon = Icons.Default.Payment,
                        label = if (isHindi) "फीस रिकॉर्ड" else "+ Payment",
                        modifier = Modifier.weight(1f),
                        onClick = onQuickAddPayment
                    )
                    QuickActionButton(
                        icon = Icons.Default.PictureAsPdf,
                        label = if (isHindi) "रिपोर्ट्स" else "PDF Report",
                        modifier = Modifier.weight(1f),
                        onClick = { onNavigate(TaxRoute.Reports) }
                    )
                }
            }
        }

        // Key Compliance Summary Grid
        item {
            Column(modifier = Modifier.padding(horizontal = 16.dp)) {
                Text(
                    text = if (isHindi) "कम्प्लायंस सारांश (Compliance Overview)" else "Compliance Summary",
                    fontSize = 14.sp,
                    fontWeight = FontWeight.Bold,
                    color = DeepMaroon
                )
                Spacer(modifier = Modifier.height(10.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    StatCard(
                        title = if (isHindi) "कुल क्लाइंट्स" else "Total Clients",
                        count = stats.totalClients.toString(),
                        subtitle = "${stats.regularGstClients} Regular • ${stats.compositionClients} Comp",
                        icon = Icons.Default.People,
                        containerColor = MaroonContainer,
                        contentColor = DeepMaroon,
                        modifier = Modifier.weight(1f),
                        onClick = { onNavigate(TaxRoute.Clients) }
                    )
                    StatCard(
                        title = "GSTR-1 Pending",
                        count = stats.gstr1Pending.toString(),
                        subtitle = if (stats.gstr1Pending > 0) "Immediate Action" else "Compliant",
                        icon = Icons.Default.AssignmentLate,
                        containerColor = if (stats.gstr1Pending > 0) StatusPendingAmberContainer else StatusFiledGreenContainer,
                        contentColor = if (stats.gstr1Pending > 0) StatusPendingAmber else StatusFiledGreen,
                        modifier = Modifier.weight(1f),
                        onClick = { onNavigate(TaxRoute.GstManagement) }
                    )
                }

                Spacer(modifier = Modifier.height(8.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    StatCard(
                        title = "GSTR-3B Pending",
                        count = stats.gstr3bPending.toString(),
                        subtitle = "Monthly & CMP-08",
                        icon = Icons.Default.ReceiptLong,
                        containerColor = if (stats.gstr3bPending > 0) StatusOverdueRedContainer else StatusFiledGreenContainer,
                        contentColor = if (stats.gstr3bPending > 0) StatusOverdueRed else StatusFiledGreen,
                        modifier = Modifier.weight(1f),
                        onClick = { onNavigate(TaxRoute.GstManagement) }
                    )
                    StatCard(
                        title = "ITR Pending",
                        count = stats.itrPending.toString(),
                        subtitle = "${stats.taxAuditPending} Tax Audits",
                        icon = Icons.Default.Calculate,
                        containerColor = GoldContainer,
                        contentColor = RoyalGoldDark,
                        modifier = Modifier.weight(1f),
                        onClick = { onNavigate(TaxRoute.IncomeTax) }
                    )
                }

                Spacer(modifier = Modifier.height(8.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    StatCard(
                        title = "TDS Returns",
                        count = stats.tdsPending.toString(),
                        subtitle = "Form 24Q/26Q",
                        icon = Icons.Default.FactCheck,
                        containerColor = IvorySurfaceVariant,
                        contentColor = DeepMaroon,
                        modifier = Modifier.weight(1f),
                        onClick = { onNavigate(TaxRoute.TdsTcs) }
                    )
                    StatCard(
                        title = if (isHindi) "नियत तिथियां" else "Due Calendar",
                        count = stats.upcomingDueDatesCount.toString(),
                        subtitle = "Statutory Dates",
                        icon = Icons.Default.CalendarMonth,
                        containerColor = MaroonContainer,
                        contentColor = DeepMaroon,
                        modifier = Modifier.weight(1f),
                        onClick = { onNavigate(TaxRoute.DueDates) }
                    )
                }
            }
        }

        // Upcoming Due Dates Alert List
        item {
            Column(modifier = Modifier.padding(16.dp)) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = if (isHindi) "आगामी अंतिम तिथियां (Upcoming Due Dates)" else "Upcoming Statutory Deadlines",
                        fontSize = 14.sp,
                        fontWeight = FontWeight.Bold,
                        color = DeepMaroon
                    )
                    TextButton(onClick = { onNavigate(TaxRoute.DueDates) }) {
                        Text(if (isHindi) "सभी देखें" else "View All", color = RoyalGoldDark, fontSize = 12.sp)
                    }
                }

                dueDates.take(3).forEach { due ->
                    Surface(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 4.dp),
                        shape = RoundedCornerShape(10.dp),
                        color = Color.White,
                        tonalElevation = 1.dp,
                        border = androidx.compose.foundation.BorderStroke(0.8.dp, IvoryOutline)
                    ) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(12.dp),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Column(modifier = Modifier.weight(1f)) {
                                Text(
                                    text = due.title,
                                    fontSize = 13.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = DeepMaroon
                                )
                                Text(
                                    text = due.periodDescription,
                                    fontSize = 11.sp,
                                    color = Color.Gray
                                )
                            }
                            Column(horizontalAlignment = Alignment.End) {
                                Text(
                                    text = due.extendedDueDate.ifEmpty { due.originalDueDate },
                                    fontSize = 12.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = StatusOverdueRed
                                )
                                Text(
                                    text = due.complianceCategory,
                                    fontSize = 10.sp,
                                    color = RoyalGoldDark,
                                    fontWeight = FontWeight.SemiBold
                                )
                            }
                        }
                    }
                }
            }
        }

        // Recently Managed GST Returns
        item {
            Column(modifier = Modifier.padding(horizontal = 16.dp)) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = if (isHindi) "हाल ही में प्रबंधित रिटर्न" else "Recent Return Filings",
                        fontSize = 14.sp,
                        fontWeight = FontWeight.Bold,
                        color = DeepMaroon
                    )
                    TextButton(onClick = { onNavigate(TaxRoute.GstManagement) }) {
                        Text(if (isHindi) "जीएसटी मॉड्यूल" else "GST Hub", color = RoyalGoldDark, fontSize = 12.sp)
                    }
                }

                recentGstReturns.take(4).forEach { ret ->
                    Surface(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 4.dp),
                        shape = RoundedCornerShape(10.dp),
                        color = Color.White,
                        tonalElevation = 1.dp,
                        border = androidx.compose.foundation.BorderStroke(0.8.dp, IvoryOutline)
                    ) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(12.dp),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Column(modifier = Modifier.weight(1f)) {
                                Text(
                                    text = ret.clientName,
                                    fontSize = 13.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = DeepMaroon
                                )
                                Text(
                                    text = "${ret.returnType} • ${ret.taxPeriod}",
                                    fontSize = 11.sp,
                                    color = Color.DarkGray
                                )
                            }
                            StatusBadge(status = ret.filingStatus)
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun QuickActionButton(
    icon: ImageVector,
    label: String,
    modifier: Modifier = Modifier,
    onClick: () -> Unit
) {
    Surface(
        modifier = modifier
            .clip(RoundedCornerShape(10.dp))
            .clickable { onClick() },
        color = Color.White,
        border = androidx.compose.foundation.BorderStroke(1.dp, RoyalGold.copy(alpha = 0.6f)),
        tonalElevation = 2.dp
    ) {
        Column(
            modifier = Modifier.padding(vertical = 10.dp, horizontal = 4.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            Icon(
                imageVector = icon,
                contentDescription = null,
                tint = DeepMaroon,
                modifier = Modifier.size(20.dp)
            )
            Spacer(modifier = Modifier.height(4.dp))
            Text(
                text = label,
                fontSize = 10.sp,
                fontWeight = FontWeight.SemiBold,
                color = DeepMaroon
            )
        }
    }
}

@Composable
fun StatCard(
    title: String,
    count: String,
    subtitle: String,
    icon: ImageVector,
    containerColor: Color,
    contentColor: Color,
    modifier: Modifier = Modifier,
    onClick: () -> Unit
) {
    Surface(
        modifier = modifier
            .clip(RoundedCornerShape(12.dp))
            .clickable { onClick() },
        color = containerColor,
        border = androidx.compose.foundation.BorderStroke(0.8.dp, contentColor.copy(alpha = 0.25f)),
        tonalElevation = 2.dp
    ) {
        Column(
            modifier = Modifier.padding(12.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = title,
                    fontSize = 11.sp,
                    fontWeight = FontWeight.SemiBold,
                    color = contentColor
                )
                Icon(
                    imageVector = icon,
                    contentDescription = null,
                    tint = contentColor,
                    modifier = Modifier.size(16.dp)
                )
            }
            Spacer(modifier = Modifier.height(6.dp))
            Text(
                text = count,
                fontSize = 20.sp,
                fontWeight = FontWeight.Bold,
                color = contentColor
            )
            Spacer(modifier = Modifier.height(2.dp))
            Text(
                text = subtitle,
                fontSize = 10.sp,
                color = contentColor.copy(alpha = 0.8f)
            )
        }
    }
}
