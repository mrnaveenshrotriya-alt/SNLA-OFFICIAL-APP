package com.example.service

import android.content.Context
import android.content.Intent
import androidx.core.content.FileProvider
import com.example.data.model.ClientEntity
import com.example.data.model.GstReturnEntity
import com.example.data.model.ItrReturnEntity
import java.io.File
import java.io.FileWriter

object CsvExportHelper {

    fun exportClientsToCsv(context: Context, clients: List<ClientEntity>): File {
        val file = File(context.cacheDir, "Clients_Master_Export.csv")
        val writer = FileWriter(file)
        writer.append("Client ID,Business Name,Proprietor Name,Business Type,Constitution,Mobile,WhatsApp,Email,PAN,TAN,GSTIN,State,District,PIN,Status\n")
        for (c in clients) {
            writer.append("\"${c.clientId}\",")
            writer.append("\"${c.businessName.replace("\"", "\"\"")}\",")
            writer.append("\"${c.proprietorName.replace("\"", "\"\"")}\",")
            writer.append("\"${c.businessType}\",")
            writer.append("\"${c.constitution}\",")
            writer.append("\"${c.mobileNumber}\",")
            writer.append("\"${c.whatsAppNumber}\",")
            writer.append("\"${c.emailAddress}\",")
            writer.append("\"${c.pan}\",")
            writer.append("\"${c.tan}\",")
            writer.append("\"${c.primaryGstin}\",")
            writer.append("\"${c.state}\",")
            writer.append("\"${c.district}\",")
            writer.append("\"${c.pinCode}\",")
            writer.append("\"${c.status}\"\n")
        }
        writer.flush()
        writer.close()
        return file
    }

    fun exportGstReturnsToCsv(context: Context, returns: List<GstReturnEntity>): File {
        val file = File(context.cacheDir, "GST_Returns_Export.csv")
        val writer = FileWriter(file)
        writer.append("Client ID,Client Name,GSTIN,Return Type,Financial Year,Tax Period,Due Date,Status,Actual Filing Date,ARN,Turnover,Tax Liability,Payment Status\n")
        for (r in returns) {
            writer.append("\"${r.clientId}\",")
            writer.append("\"${r.clientName.replace("\"", "\"\"")}\",")
            writer.append("\"${r.gstin}\",")
            writer.append("\"${r.returnType}\",")
            writer.append("\"${r.financialYear}\",")
            writer.append("\"${r.taxPeriod}\",")
            writer.append("\"${r.originalDueDate}\",")
            writer.append("\"${r.filingStatus}\",")
            writer.append("\"${r.actualFilingDate}\",")
            writer.append("\"${r.arnNumber}\",")
            writer.append("${r.taxableTurnover},")
            writer.append("${r.taxLiability},")
            writer.append("\"${r.paymentStatus}\"\n")
        }
        writer.flush()
        writer.close()
        return file
    }

    fun shareCsvFile(context: Context, file: File, subject: String = "Tax Compliance Export") {
        val uri = FileProvider.getUriForFile(context, "${context.packageName}.provider", file)
        val shareIntent = Intent(Intent.ACTION_SEND).apply {
            type = "text/csv"
            putExtra(Intent.EXTRA_STREAM, uri)
            putExtra(Intent.EXTRA_SUBJECT, subject)
            addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
            flags = Intent.FLAG_ACTIVITY_NEW_TASK
        }
        context.startActivity(Intent.createChooser(shareIntent, "Share CSV Export"))
    }
}
