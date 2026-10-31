package com.example.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.model.ClientEntity
import com.example.data.model.GstReturnEntity
import com.example.service.CsvExportHelper
import com.example.service.PdfReportGenerator
import com.example.ui.theme.*

@Composable
fun ReportsScreen(
    clients: List<ClientEntity>,
    gstReturns: List<GstReturnEntity>,
    isHindi: Boolean
) {
    val context = LocalContext.current

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(IvoryBackground)
            .testTag("reports_screen")
    ) {
        Surface(color = Color.White, tonalElevation = 2.dp, modifier = Modifier.fillMaxWidth()) {
            Column(modifier = Modifier.padding(14.dp)) {
                Text(
                    text = if (isHindi) "कर विवरणी एवं कम्प्लायंस रिपोर्ट्स (Tax Compliance Reports)" else "Statutory Reports & Exports",
                    fontSize = 15.sp,
                    fontWeight = FontWeight.Bold,
                    color = DeepMaroon
                )
                Text(
                    text = "Professional PDF & Excel reports with Shree Navya Lekha Associates branding",
                    fontSize = 11.sp,
                    color = Color.Gray
                )
            }
        }

        LazyColumn(
            modifier = Modifier.fillMaxSize(),
            contentPadding = PaddingValues(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            item {
                ReportActionCard(
                    title = "Client Master Compliance Directory",
                    description = "Full list of active tax clients, PAN, TAN, GSTIN, constitution, and contact information.",
                    icon = Icons.Default.People,
                    onPdf = {
                        val file = PdfReportGenerator.generateClientMasterPdf(context, clients)
                        PdfReportGenerator.viewOrSharePdf(context, file)
                    },
                    onExcel = {
                        val file = CsvExportHelper.exportClientsToCsv(context, clients)
                        CsvExportHelper.shareCsvFile(context, file, "Client Master Directory")
                    }
                )
            }

            item {
                ReportActionCard(
                    title = "GST Return Filing Lifecycle Report",
                    description = "Detailed statement of GSTR-1, GSTR-3B, CMP-08 filings, ARNs, turnovers, and pending statuses.",
                    icon = Icons.Default.ReceiptLong,
                    onPdf = {
                        val file = PdfReportGenerator.generateGstReturnsPdf(context, gstReturns)
                        PdfReportGenerator.viewOrSharePdf(context, file)
                    },
                    onExcel = {
                        val file = CsvExportHelper.exportGstReturnsToCsv(context, gstReturns)
                        CsvExportHelper.shareCsvFile(context, file, "GST Returns Statement")
                    }
                )
            }

            item {
                ReportActionCard(
                    title = "Pending Compliance & Due Dates Statement",
                    description = "Comprehensive list of pending returns requiring immediate attention before deadline.",
                    icon = Icons.Default.AssignmentLate,
                    onPdf = {
                        val pending = gstReturns.filter { it.filingStatus != "Filed" }
                        val file = PdfReportGenerator.generateGstReturnsPdf(context, pending)
                        PdfReportGenerator.viewOrSharePdf(context, file)
                    },
                    onExcel = {
                        val pending = gstReturns.filter { it.filingStatus != "Filed" }
                        val file = CsvExportHelper.exportGstReturnsToCsv(context, pending)
                        CsvExportHelper.shareCsvFile(context, file, "Pending Returns Statement")
                    }
                )
            }
        }
    }
}

@Composable
fun ReportActionCard(
    title: String,
    description: String,
    icon: ImageVector,
    onPdf: () -> Unit,
    onExcel: () -> Unit
) {
    Surface(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(12.dp),
        color = Color.White,
        border = androidx.compose.foundation.BorderStroke(1.dp, IvoryOutline),
        tonalElevation = 2.dp
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(
                    imageVector = icon,
                    contentDescription = null,
                    tint = DeepMaroon,
                    modifier = Modifier.size(24.dp)
                )
                Spacer(modifier = Modifier.width(10.dp))
                Text(
                    text = title,
                    fontSize = 14.sp,
                    fontWeight = FontWeight.Bold,
                    color = DeepMaroon
                )
            }

            Spacer(modifier = Modifier.height(6.dp))
            Text(
                text = description,
                fontSize = 12.sp,
                color = Color.DarkGray
            )

            Spacer(modifier = Modifier.height(12.dp))
            Divider(color = IvoryOutlineVariant, thickness = 0.8.dp)
            Spacer(modifier = Modifier.height(10.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.End,
                verticalAlignment = Alignment.CenterVertically
            ) {
                OutlinedButton(
                    onClick = onExcel,
                    shape = RoundedCornerShape(8.dp),
                    colors = ButtonDefaults.outlinedButtonColors(contentColor = DeepMaroon),
                    border = androidx.compose.foundation.BorderStroke(1.dp, DeepMaroon)
                ) {
                    Icon(Icons.Default.TableChart, contentDescription = null, modifier = Modifier.size(16.dp))
                    Spacer(modifier = Modifier.width(6.dp))
                    Text("Excel / CSV", fontSize = 12.sp)
                }

                Spacer(modifier = Modifier.width(8.dp))

                Button(
                    onClick = onPdf,
                    shape = RoundedCornerShape(8.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = DeepMaroon, contentColor = RoyalGoldLight)
                ) {
                    Icon(Icons.Default.PictureAsPdf, contentDescription = null, modifier = Modifier.size(16.dp))
                    Spacer(modifier = Modifier.width(6.dp))
                    Text("Print / Share PDF", fontSize = 12.sp)
                }
            }
        }
    }
}
