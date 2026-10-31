package com.example.data.model

import androidx.room.Entity
import androidx.room.PrimaryKey
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

// 1. Client Master Entity
@Entity(tableName = "clients")
data class ClientEntity(
    @PrimaryKey val clientId: String, // e.g. SNL-2024-001
    val businessName: String, // Company / Firm / Trade Name
    val proprietorName: String, // Proprietor / Director / Partner
    val businessType: String, // Proprietorship, Partnership, Private Limited, LLP, HUF, Individual
    val constitution: String, // Manufacturer, Trader, Service Provider, Works Contractor
    val address: String,
    val state: String = "Rajasthan",
    val district: String = "Jaipur",
    val pinCode: String = "302001",
    val mobileNumber: String,
    val whatsAppNumber: String,
    val emailAddress: String,
    val alternateContact: String = "",
    val pan: String,
    val tan: String = "",
    val primaryGstin: String = "",
    val cinLlpin: String = "",
    val financialYear: String = "2024-25",
    val joiningDate: String = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).format(Date()),
    val status: String = "Active", // Active, Inactive, Suspended, Archived
    val notes: String = ""
)

// 2. Client Additional GSTINs
@Entity(tableName = "client_gstins")
data class ClientGstinEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val clientId: String,
    val gstin: String,
    val legalName: String,
    val tradeName: String,
    val registrationDate: String = "",
    val registrationStatus: String = "Active", // Active, Cancelled, Suspended
    val scheme: String = "Regular", // Regular / Composition
    val gstr1Frequency: String = "Monthly", // Monthly / Quarterly
    val gstr3bFrequency: String = "Monthly",
    val isQrmp: Boolean = false,
    val portalUserId: String = ""
)

// 3. GST Return Filing Entity
@Entity(tableName = "gst_returns")
data class GstReturnEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val clientId: String,
    val clientName: String,
    val gstin: String,
    val returnType: String, // GSTR-1, GSTR-1A, GSTR-3B, GSTR-4, CMP-08, GSTR-9, GSTR-9C
    val financialYear: String, // 2024-25
    val taxPeriod: String, // e.g. October 2024, Q2 (Jul-Sep)
    val isQuarterly: Boolean = false,
    val originalDueDate: String, // YYYY-MM-DD
    val revisedDueDate: String = "",
    val filingStatus: String = "Pending", // Pending, Data Awaited, Data Received, Under Preparation, Ready for Filing, Filed, Nil Filed, Overdue, Not Applicable
    val actualFilingDate: String = "",
    val arnNumber: String = "",
    val taxableTurnover: Double = 0.0,
    val taxLiability: Double = 0.0,
    val cgst: Double = 0.0,
    val sgst: Double = 0.0,
    val igst: Double = 0.0,
    val lateFee: Double = 0.0,
    val interest: Double = 0.0,
    val paymentStatus: String = "Unpaid", // Paid, Unpaid, Partial
    val challanDetails: String = "",
    val acknowledgementPdfUri: String = "",
    val remarks: String = ""
)

// 4. Income Tax Return Entity
@Entity(tableName = "itr_returns")
data class ItrReturnEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val clientId: String,
    val clientName: String,
    val pan: String,
    val financialYear: String, // 2023-24
    val assessmentYear: String, // 2024-25
    val itrForm: String, // ITR-1, ITR-2, ITR-3, ITR-4, ITR-5, ITR-6, ITR-7
    val returnNature: String = "Original", // Original (139(1)), Revised (139(5)), Belated (139(4)), Updated (139(8A))
    val filingDueDate: String,
    val extendedDueDate: String = "",
    val actualFilingDate: String = "",
    val filingStatus: String = "Pending", // Pending, Under Preparation, Ready for Filing, Filed, Overdue
    val acknowledgementNumber: String = "",
    val taxableIncome: Double = 0.0,
    val taxPayable: Double = 0.0,
    val refundAmount: Double = 0.0,
    val refundStatus: String = "Not Applicable", // Pending, Processed, Issued, Adjusted, Not Applicable
    val isTaxAuditApplicable: Boolean = false,
    val auditForm: String = "Form 3CB-3CD", // Form 3CA-3CD, Form 3CB-3CD
    val auditStatus: String = "Not Applicable", // Pending, In Progress, Uploaded, Approved
    val auditFilingDate: String = "",
    val remarks: String = ""
)

// 5. TDS / TCS Return Entity
@Entity(tableName = "tds_returns")
data class TdsReturnEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val clientId: String,
    val clientName: String,
    val tan: String,
    val pan: String,
    val financialYear: String,
    val quarter: String, // Q1, Q2, Q3, Q4
    val returnType: String, // Form 24Q, Form 26Q, Form 27Q, Form 27EQ
    val dueDate: String,
    val filingDate: String = "",
    val challanDetails: String = "",
    val cinBSR: String = "",
    val taxDeducted: Double = 0.0,
    val interest: Double = 0.0,
    val lateFilingFee: Double = 0.0,
    val filingStatus: String = "Pending", // Pending, Filed, Overdue
    val acknowledgementNumber: String = "",
    val certificateIssuedStatus: String = "Pending" // Pending, Issued Form 16/16A
)

// 6. E-Way Bill Entity
@Entity(tableName = "eway_bills")
data class EWayBillEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val clientId: String,
    val clientName: String,
    val gstin: String,
    val ewayBillNumber: String,
    val generationDate: String, // YYYY-MM-DD HH:mm
    val validityExpiryDate: String, // YYYY-MM-DD HH:mm
    val vehicleNumber: String,
    val transporterId: String = "",
    val documentNumber: String, // Invoice No
    val consignmentValue: Double = 0.0,
    val status: String = "Active", // Active, Expired, Cancelled, In Transit
    val remarks: String = ""
)

// 7. Statutory Due Date Entity
@Entity(tableName = "statutory_due_dates")
data class StatutoryDueDateEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val complianceCategory: String, // GST, Income Tax, Tax Audit, TDS/TCS, Advance Tax, ROC
    val title: String, // e.g. GSTR-3B (Monthly), ITR Non-Audit, Advance Tax Q3
    val periodDescription: String,
    val originalDueDate: String, // YYYY-MM-DD
    val extendedDueDate: String = "",
    val notificationRef: String = "Govt Notification",
    val applicability: String = "All Regular GST Taxpayers",
    val lastUpdatedDate: String = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).format(Date()),
    val isCustomOrModified: Boolean = false
)

// 8. Fee and Invoice Entity
@Entity(tableName = "client_invoices")
data class ClientInvoiceEntity(
    @PrimaryKey val invoiceNumber: String, // e.g. INV-2024-001
    val clientId: String,
    val clientName: String,
    val serviceType: String, // GST Filing, ITR Filing, Tax Audit, TDS Filing, Accounting, Consultation
    val invoiceDate: String,
    val paymentDueDate: String,
    val totalAmount: Double,
    val amountReceived: Double = 0.0,
    val pendingAmount: Double = 0.0,
    val paymentStatus: String = "Unpaid", // Paid, Partially Paid, Unpaid, Overdue
    val paymentMode: String = "UPI", // UPI, Bank Transfer (NEFT/RTGS), Cheque, Cash
    val transactionRef: String = "",
    val notes: String = ""
)

// 9. Document Vault Entity
@Entity(tableName = "client_documents")
data class ClientDocumentEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val clientId: String,
    val clientName: String,
    val documentCategory: String, // PAN Card, GST Certificate, ITR Acknowledgement, Tax Computation, Audit Report, Balance Sheet, P&L, Bank Statement, TDS Challan
    val title: String,
    val financialYear: String = "2024-25",
    val fileUri: String = "",
    val uploadDate: String = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).format(Date()),
    val notes: String = ""
)

// 10. Secure Encrypted Password Vault Entity
@Entity(tableName = "password_vault")
data class VaultCredentialEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val clientId: String,
    val clientName: String,
    val portalName: String, // GST Portal, Income Tax e-Filing, TRACES, E-Way Bill, MCA Portal, Icegate
    val loginId: String,
    val encryptedPassword: String, // AES-256 encrypted base64
    val iv: String, // Initialization vector
    val registeredMobile: String = "",
    val registeredEmail: String = "",
    val securityNotes: String = "",
    val lastUpdated: String = SimpleDateFormat("yyyy-MM-dd HH:mm", Locale.getDefault()).format(Date())
)

// 11. Reminder Alarm Configuration
@Entity(tableName = "tax_reminders")
data class TaxReminderEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val clientId: String,
    val clientName: String,
    val returnType: String,
    val taxPeriod: String,
    val dueDate: String, // YYYY-MM-DD
    val reminderType: String, // 15_DAYS, 7_DAYS, 3_DAYS, 1_DAY, ON_DUE_DATE, DAILY_AFTER
    val reminderTime: String = "09:00",
    val isAlarmRinging: Boolean = true,
    val isEnabled: Boolean = true,
    val isFiled: Boolean = false,
    val lastTriggeredDate: String = ""
)

// Communication Log
@Entity(tableName = "communication_logs")
data class CommunicationLogEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val clientId: String,
    val clientName: String,
    val channel: String, // WhatsApp, SMS, Email
    val recipient: String,
    val messageType: String, // Due Reminder, Payment Reminder, Document Request, Invoice
    val messageContent: String,
    val sentTimestamp: String = SimpleDateFormat("yyyy-MM-dd HH:mm", Locale.getDefault()).format(Date()),
    val status: String = "Sent"
)
