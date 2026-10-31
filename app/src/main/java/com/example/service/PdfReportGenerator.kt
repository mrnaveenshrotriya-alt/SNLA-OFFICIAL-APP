package com.example.service

import android.content.Context
import android.content.Intent
import android.graphics.*
import android.graphics.pdf.PdfDocument
import androidx.core.content.FileProvider
import com.example.R
import com.example.data.model.*
import java.io.File
import java.io.FileOutputStream
import java.text.SimpleDateFormat
import java.util.*

object PdfReportGenerator {

    fun generateClientMasterPdf(context: Context, clients: List<ClientEntity>): File {
        val document = PdfDocument()
        val pageInfo = PdfDocument.PageInfo.Builder(595, 842, 1).create() // A4
        val page = document.startPage(pageInfo)
        val canvas = page.canvas

        drawCompanyHeader(context, canvas, "CLIENT MASTER COMPLIANCE DIRECTORY")

        val paintText = Paint().apply {
            color = Color.rgb(35, 30, 30)
            textSize = 9.5f
            isAntiAlias = true
        }
        val paintHeader = Paint().apply {
            color = Color.rgb(88, 24, 37) // Deep Maroon
            textSize = 10f
            typeface = Typeface.create(Typeface.DEFAULT, Typeface.BOLD)
            isAntiAlias = true
        }
        val paintLine = Paint().apply {
            color = Color.rgb(212, 175, 55) // Gold
            strokeWidth = 1f
        }

        // Table Header
        var y = 140f
        canvas.drawText("CLIENT ID", 30f, y, paintHeader)
        canvas.drawText("BUSINESS NAME", 110f, y, paintHeader)
        canvas.drawText("PAN", 270f, y, paintHeader)
        canvas.drawText("GSTIN", 350f, y, paintHeader)
        canvas.drawText("MOBILE", 470f, y, paintHeader)
        canvas.drawText("STATUS", 530f, y, paintHeader)
        y += 6f
        canvas.drawLine(30f, y, 565f, y, paintLine)
        y += 16f

        val rowPaint = Paint().apply {
            color = Color.rgb(248, 245, 240)
        }

        clients.take(28).forEachIndexed { index, client ->
            if (index % 2 == 1) {
                canvas.drawRect(28f, y - 11f, 567f, y + 4f, rowPaint)
            }
            canvas.drawText(client.clientId, 30f, y, paintText)
            val name = if (client.businessName.length > 22) client.businessName.take(20) + ".." else client.businessName
            canvas.drawText(name, 110f, y, paintText)
            canvas.drawText(client.pan, 270f, y, paintText)
            val gstin = if (client.primaryGstin.isNotEmpty()) client.primaryGstin else "-"
            canvas.drawText(gstin, 350f, y, paintText)
            canvas.drawText(client.mobileNumber, 470f, y, paintText)
            canvas.drawText(client.status, 530f, y, paintText)
            y += 20f
        }

        drawFooter(canvas, 1, 1)
        document.finishPage(page)

        return saveAndGetPdfFile(context, document, "Client_Master_Report.pdf")
    }

    fun generateGstReturnsPdf(context: Context, returns: List<GstReturnEntity>): File {
        val document = PdfDocument()
        val pageInfo = PdfDocument.PageInfo.Builder(595, 842, 1).create()
        val page = document.startPage(pageInfo)
        val canvas = page.canvas

        drawCompanyHeader(context, canvas, "GST RETURN FILING STATUS REPORT")

        val paintText = Paint().apply {
            color = Color.rgb(35, 30, 30)
            textSize = 9.5f
            isAntiAlias = true
        }
        val paintHeader = Paint().apply {
            color = Color.rgb(88, 24, 37)
            textSize = 10f
            typeface = Typeface.create(Typeface.DEFAULT, Typeface.BOLD)
            isAntiAlias = true
        }
        val paintLine = Paint().apply {
            color = Color.rgb(212, 175, 55)
            strokeWidth = 1f
        }

        var y = 140f
        canvas.drawText("CLIENT / TRADE NAME", 30f, y, paintHeader)
        canvas.drawText("TYPE", 210f, y, paintHeader)
        canvas.drawText("PERIOD", 265f, y, paintHeader)
        canvas.drawText("DUE DATE", 345f, y, paintHeader)
        canvas.drawText("STATUS", 420f, y, paintHeader)
        canvas.drawText("ARN / FILING DATE", 490f, y, paintHeader)
        y += 6f
        canvas.drawLine(30f, y, 565f, y, paintLine)
        y += 16f

        returns.take(28).forEachIndexed { index, r ->
            if (index % 2 == 1) {
                canvas.drawRect(28f, y - 11f, 567f, y + 4f, Paint().apply { color = Color.rgb(250, 246, 240) })
            }
            val name = if (r.clientName.length > 25) r.clientName.take(23) + ".." else r.clientName
            canvas.drawText(name, 30f, y, paintText)
            canvas.drawText(r.returnType, 210f, y, paintText)
            canvas.drawText(r.taxPeriod, 265f, y, paintText)
            canvas.drawText(r.originalDueDate, 345f, y, paintText)
            canvas.drawText(r.filingStatus, 420f, y, paintText)
            val arn = if (r.arnNumber.isNotEmpty()) r.arnNumber else if (r.actualFilingDate.isNotEmpty()) r.actualFilingDate else "-"
            canvas.drawText(arn, 490f, y, paintText)
            y += 20f
        }

        drawFooter(canvas, 1, 1)
        document.finishPage(page)

        return saveAndGetPdfFile(context, document, "GST_Returns_Report.pdf")
    }

    fun generateInvoicePdf(context: Context, invoice: ClientInvoiceEntity): File {
        val document = PdfDocument()
        val pageInfo = PdfDocument.PageInfo.Builder(595, 842, 1).create()
        val page = document.startPage(pageInfo)
        val canvas = page.canvas

        drawCompanyHeader(context, canvas, "TAX CONSULTANCY & FILING INVOICE")

        val paintBold = Paint().apply {
            color = Color.rgb(88, 24, 37)
            textSize = 12f
            typeface = Typeface.create(Typeface.DEFAULT, Typeface.BOLD)
            isAntiAlias = true
        }
        val paintRegular = Paint().apply {
            color = Color.rgb(40, 40, 40)
            textSize = 11f
            isAntiAlias = true
        }

        var y = 145f
        // Invoice Meta Box
        canvas.drawRoundRect(30f, y, 565f, y + 60f, 6f, 6f, Paint().apply {
            color = Color.rgb(253, 249, 243)
            style = Paint.Style.FILL
        })
        canvas.drawRoundRect(30f, y, 565f, y + 60f, 6f, 6f, Paint().apply {
            color = Color.rgb(212, 175, 55)
            style = Paint.Style.STROKE
            strokeWidth = 1f
        })

        canvas.drawText("Invoice No: ${invoice.invoiceNumber}", 45f, y + 25f, paintBold)
        canvas.drawText("Invoice Date: ${invoice.invoiceDate}", 45f, y + 45f, paintRegular)
        canvas.drawText("Payment Due: ${invoice.paymentDueDate}", 340f, y + 25f, paintRegular)
        canvas.drawText("Status: ${invoice.paymentStatus.uppercase()}", 340f, y + 45f, paintBold)

        y += 85f
        // Bill To
        canvas.drawText("BILL TO (CLIENT DETAILS):", 30f, y, paintBold)
        y += 18f
        canvas.drawText(invoice.clientName, 30f, y, paintBold)
        y += 16f
        canvas.drawText("Client ID: ${invoice.clientId}", 30f, y, paintRegular)
        y += 30f

        // Table Header
        canvas.drawRect(30f, y, 565f, y + 24f, Paint().apply { color = Color.rgb(88, 24, 37) })
        val tableHeaderText = Paint().apply {
            color = Color.WHITE
            textSize = 10f
            typeface = Typeface.create(Typeface.DEFAULT, Typeface.BOLD)
            isAntiAlias = true
        }
        canvas.drawText("SR", 40f, y + 16f, tableHeaderText)
        canvas.drawText("DESCRIPTION OF TAX SERVICES", 70f, y + 16f, tableHeaderText)
        canvas.drawText("AMOUNT (INR ₹)", 450f, y + 16f, tableHeaderText)

        y += 24f
        // Item Row
        canvas.drawRect(30f, y, 565f, y + 35f, Paint().apply { color = Color.rgb(250, 248, 245) })
        canvas.drawText("1", 40f, y + 22f, paintRegular)
        canvas.drawText(invoice.serviceType, 70f, y + 22f, paintRegular)
        canvas.drawText("₹ ${"%,.2f".format(invoice.totalAmount)}", 450f, y + 22f, paintBold)

        y += 50f
        // Summary
        canvas.drawLine(350f, y, 565f, y, Paint().apply { color = Color.rgb(212, 175, 55); strokeWidth = 1f })
        y += 20f
        canvas.drawText("Total Invoice Amount:", 340f, y, paintRegular)
        canvas.drawText("₹ ${"%,.2f".format(invoice.totalAmount)}", 470f, y, paintBold)
        y += 20f
        canvas.drawText("Amount Received:", 340f, y, paintRegular)
        canvas.drawText("₹ ${"%,.2f".format(invoice.amountReceived)}", 470f, y, paintRegular)
        y += 22f
        canvas.drawText("Balance Due:", 340f, y, paintBold)
        canvas.drawText("₹ ${"%,.2f".format(invoice.pendingAmount)}", 470f, y, paintBold)

        y += 45f
        // Payment Info
        canvas.drawText("Bank / UPI Payment Instructions:", 30f, y, paintBold)
        y += 18f
        canvas.drawText("UPI ID: naveen.tax@okhdfcbank", 30f, y, paintRegular)
        y += 16f
        canvas.drawText("Bank: HDFC Bank Ltd, Tonk Road Jaipur | A/c No: 50200039281928 | IFSC: HDFC0000123", 30f, y, paintRegular)

        y += 40f
        canvas.drawText("Authorized Signatory", 420f, y, paintBold)
        canvas.drawText("Shree Navya Lekha Associates", 400f, y + 15f, paintRegular)

        drawFooter(canvas, 1, 1)
        document.finishPage(page)

        return saveAndGetPdfFile(context, document, "Invoice_${invoice.invoiceNumber}.pdf")
    }

    private fun drawCompanyHeader(context: Context, canvas: Canvas, reportTitle: String) {
        val bannerPaint = Paint().apply {
            color = Color.rgb(88, 24, 37) // Deep Maroon
            style = Paint.Style.FILL
        }
        canvas.drawRect(0f, 0f, 595f, 95f, bannerPaint)

        // Gold line accent
        val goldLine = Paint().apply {
            color = Color.rgb(212, 175, 55) // Royal Gold
            strokeWidth = 3f
        }
        canvas.drawLine(0f, 95f, 595f, 95f, goldLine)

        // Draw Logo bitmap if available
        try {
            val logoBitmap = BitmapFactory.decodeResource(context.resources, R.drawable.app_logo)
            if (logoBitmap != null) {
                val scaled = Bitmap.createScaledBitmap(logoBitmap, 60, 60, true)
                canvas.drawBitmap(scaled, 30f, 18f, null)
            }
        } catch (ignored: Exception) {}

        val titlePaint = Paint().apply {
            color = Color.rgb(255, 223, 115) // Bright Gold
            textSize = 17f
            typeface = Typeface.create(Typeface.DEFAULT, Typeface.BOLD)
            isAntiAlias = true
        }
        val subPaint = Paint().apply {
            color = Color.WHITE
            textSize = 9.5f
            isAntiAlias = true
        }
        canvas.drawText("SHREE NAVYA LEKHA ASSOCIATES", 105f, 38f, titlePaint)
        canvas.drawText("Tax Consultants • GST & Income Tax Advisory • Corporate Compliance", 105f, 54f, subPaint)
        canvas.drawText("Jaipur, Rajasthan • Mobile: +91 9414056789 • Email: info@shreenavyalekha.com", 105f, 68f, subPaint)

        // Sub-title bar
        val barPaint = Paint().apply {
            color = Color.rgb(250, 247, 242)
        }
        canvas.drawRect(0f, 97f, 595f, 122f, barPaint)

        val reportTitlePaint = Paint().apply {
            color = Color.rgb(88, 24, 37)
            textSize = 11f
            typeface = Typeface.create(Typeface.DEFAULT, Typeface.BOLD)
            isAntiAlias = true
        }
        canvas.drawText(reportTitle, 30f, 114f, reportTitlePaint)

        val dateStr = SimpleDateFormat("dd MMM yyyy, hh:mm a", Locale.getDefault()).format(Date())
        val datePaint = Paint().apply {
            color = Color.rgb(100, 100, 100)
            textSize = 9f
            isAntiAlias = true
        }
        canvas.drawText("Generated on: $dateStr", 390f, 114f, datePaint)
    }

    private fun drawFooter(canvas: Canvas, pageNumber: Int, totalPages: Int) {
        val paint = Paint().apply {
            color = Color.rgb(120, 120, 120)
            textSize = 8.5f
            isAntiAlias = true
        }
        canvas.drawLine(30f, 810f, 565f, 810f, Paint().apply {
            color = Color.rgb(212, 175, 55)
            strokeWidth = 0.8f
        })
        canvas.drawText("Shree Navya Lekha Associates — Confidential Tax & Compliance Record", 30f, 825f, paint)
        canvas.drawText("Page $pageNumber of $totalPages", 510f, 825f, paint)
    }

    private fun saveAndGetPdfFile(context: Context, document: PdfDocument, fileName: String): File {
        val file = File(context.cacheDir, fileName)
        val fos = FileOutputStream(file)
        document.writeTo(fos)
        document.close()
        fos.flush()
        fos.close()
        return file
    }

    fun viewOrSharePdf(context: Context, file: File) {
        val uri = FileProvider.getUriForFile(context, "${context.packageName}.provider", file)
        val intent = Intent(Intent.ACTION_VIEW).apply {
            setDataAndType(uri, "application/pdf")
            addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
            flags = Intent.FLAG_ACTIVITY_NEW_TASK
        }
        try {
            context.startActivity(intent)
        } catch (e: Exception) {
            val shareIntent = Intent(Intent.ACTION_SEND).apply {
                type = "application/pdf"
                putExtra(Intent.EXTRA_STREAM, uri)
                addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
                flags = Intent.FLAG_ACTIVITY_NEW_TASK
            }
            context.startActivity(Intent.createChooser(shareIntent, "Open or Share PDF"))
        }
    }
}
