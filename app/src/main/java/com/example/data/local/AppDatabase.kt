package com.example.data.local

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.sqlite.db.SupportSQLiteDatabase
import com.example.data.model.*
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

@Database(
    entities = [
        ClientEntity::class,
        ClientGstinEntity::class,
        GstReturnEntity::class,
        ItrReturnEntity::class,
        TdsReturnEntity::class,
        EWayBillEntity::class,
        StatutoryDueDateEntity::class,
        ClientInvoiceEntity::class,
        ClientDocumentEntity::class,
        VaultCredentialEntity::class,
        TaxReminderEntity::class,
        CommunicationLogEntity::class
    ],
    version = 1,
    exportSchema = false
)
abstract class AppDatabase : RoomDatabase() {

    abstract fun clientDao(): ClientDao
    abstract fun gstReturnDao(): GstReturnDao
    abstract fun itrReturnDao(): ItrReturnDao
    abstract fun tdsReturnDao(): TdsReturnDao
    abstract fun ewayBillDao(): EWayBillDao
    abstract fun statutoryDueDateDao(): StatutoryDueDateDao
    abstract fun clientInvoiceDao(): ClientInvoiceDao
    abstract fun clientDocumentDao(): ClientDocumentDao
    abstract fun vaultDao(): VaultDao
    abstract fun reminderDao(): ReminderDao
    abstract fun communicationLogDao(): CommunicationLogDao

    companion object {
        @Volatile
        private var INSTANCE: AppDatabase? = null

        fun getDatabase(context: Context, scope: CoroutineScope): AppDatabase {
            return INSTANCE ?: synchronized(this) {
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    AppDatabase::class.java,
                    "shree_navya_lekha_tax.db"
                )
                    .fallbackToDestructiveMigration()
                    .addCallback(AppDatabaseCallback(scope))
                    .build()
                INSTANCE = instance
                instance
            }
        }

        private class AppDatabaseCallback(
            private val scope: CoroutineScope
        ) : RoomDatabase.Callback() {
            override fun onCreate(db: SupportSQLiteDatabase) {
                super.onCreate(db)
                INSTANCE?.let { database ->
                    scope.launch(Dispatchers.IO) {
                        populateInitialComplianceData(database)
                    }
                }
            }
        }

        suspend fun populateInitialComplianceData(db: AppDatabase) {
            val dueDateDao = db.statutoryDueDateDao()
            val clientDao = db.clientDao()
            val gstDao = db.gstReturnDao()
            val itrDao = db.itrReturnDao()
            val invoiceDao = db.clientInvoiceDao()
            val reminderDao = db.reminderDao()

            // 1. Preload Statutory Tax Due Dates (Official Indian Compliance Calendar)
            val statutoryDates = listOf(
                StatutoryDueDateEntity(
                    complianceCategory = "GST",
                    title = "GSTR-1 (Monthly)",
                    periodDescription = "Monthly Return for October 2024",
                    originalDueDate = "2024-11-11",
                    applicability = "Taxpayers with aggregate turnover > 5 Cr or non-QRMP",
                    notificationRef = "Notification No. 83/2020 - Central Tax"
                ),
                StatutoryDueDateEntity(
                    complianceCategory = "GST",
                    title = "GSTR-3B (Monthly)",
                    periodDescription = "Monthly Summary Return for October 2024",
                    originalDueDate = "2024-11-20",
                    applicability = "All regular GST taxpayers",
                    notificationRef = "Rule 61(5) CGST Rules"
                ),
                StatutoryDueDateEntity(
                    complianceCategory = "GST",
                    title = "CMP-08 (Quarterly)",
                    periodDescription = "Quarter 3 (Oct - Dec 2024)",
                    originalDueDate = "2025-01-18",
                    applicability = "Composition Scheme Taxpayers",
                    notificationRef = "Notification No. 21/2019 - Central Tax"
                ),
                StatutoryDueDateEntity(
                    complianceCategory = "GST",
                    title = "GSTR-9 & GSTR-9C",
                    periodDescription = "Annual Return for FY 2023-24",
                    originalDueDate = "2024-12-31",
                    applicability = "Turnover > ₹2 Cr (GSTR-9), > ₹5 Cr (GSTR-9C)",
                    notificationRef = "Section 44 of CGST Act"
                ),
                StatutoryDueDateEntity(
                    complianceCategory = "Income Tax",
                    title = "ITR Filing (Tax Audit Cases)",
                    periodDescription = "Assessment Year 2024-25 (FY 2023-24)",
                    originalDueDate = "2024-11-15",
                    extendedDueDate = "2024-11-15",
                    applicability = "Corporate & Tax Audit Assessees",
                    notificationRef = "CBDT Circular No. 13/2024"
                ),
                StatutoryDueDateEntity(
                    complianceCategory = "Income Tax",
                    title = "Tax Audit Report (Form 3CA/3CD)",
                    periodDescription = "FY 2023-24 / AY 2024-25",
                    originalDueDate = "2024-10-07",
                    applicability = "Businesses turnover > 1 Cr / 10 Cr (Cash < 5%)",
                    notificationRef = "CBDT Circular Extension"
                ),
                StatutoryDueDateEntity(
                    complianceCategory = "TDS/TCS",
                    title = "TDS Return - Q2 (Jul - Sep)",
                    periodDescription = "Form 24Q, 26Q, 27Q for Q2 FY 2024-25",
                    originalDueDate = "2024-10-31",
                    applicability = "All Tax Deductors / TAN Holders",
                    notificationRef = "Rule 31A Income Tax Rules"
                ),
                StatutoryDueDateEntity(
                    complianceCategory = "Advance Tax",
                    title = "Advance Tax - 3rd Instalment (75%)",
                    periodDescription = "Financial Year 2024-25",
                    originalDueDate = "2024-12-15",
                    applicability = "All Assessees with tax liability > ₹10,000",
                    notificationRef = "Section 211 Income Tax Act"
                )
            )
            dueDateDao.insertAll(statutoryDates)

            // 2. Initial Clients
            val c1 = ClientEntity(
                clientId = "SNL-2024-001",
                businessName = "Marudhar Textiles Pvt Ltd",
                proprietorName = "Shri Rajesh Sharma",
                businessType = "Private Limited",
                constitution = "Manufacturer",
                address = "Plot No. 42, Sitapura Industrial Area, Tonk Road",
                state = "Rajasthan",
                district = "Jaipur",
                pinCode = "302022",
                mobileNumber = "9829012345",
                whatsAppNumber = "9829012345",
                emailAddress = "accounts@marudhartextiles.com",
                pan = "AABCM1234F",
                tan = "JPRM01234D",
                primaryGstin = "08AABCM1234F1Z5",
                cinLlpin = "U17120RJ2018PTC061234",
                financialYear = "2024-25",
                status = "Active"
            )
            clientDao.insertClient(c1)

            val c2 = ClientEntity(
                clientId = "SNL-2024-002",
                businessName = "Apeksha Gems & Jewellers",
                proprietorName = "Naveen Shrotriya",
                businessType = "Proprietorship",
                constitution = "Trader",
                address = "Johari Bazar, Pink City",
                state = "Rajasthan",
                district = "Jaipur",
                pinCode = "302003",
                mobileNumber = "9414056789",
                whatsAppNumber = "9414056789",
                emailAddress = "info@apekshagems.in",
                pan = "AABPS9876K",
                tan = "",
                primaryGstin = "08AABPS9876K1ZP",
                financialYear = "2024-25",
                status = "Active"
            )
            clientDao.insertClient(c2)

            val c3 = ClientEntity(
                clientId = "SNL-2024-003",
                businessName = "Khandelwal Sweets & Namkeen",
                proprietorName = "Manoj Khandelwal",
                businessType = "Proprietorship",
                constitution = "Trader (Composition)",
                address = "Shop 12, MI Road",
                state = "Rajasthan",
                district = "Jaipur",
                pinCode = "302001",
                mobileNumber = "9828112233",
                whatsAppNumber = "9828112233",
                emailAddress = "khandelwalsweets@gmail.com",
                pan = "AFGPK4321L",
                primaryGstin = "08AFGPK4321L1Z2",
                financialYear = "2024-25",
                status = "Active"
            )
            clientDao.insertClient(c3)

            // 3. Client GSTINs
            clientDao.insertGstin(
                ClientGstinEntity(
                    clientId = "SNL-2024-001",
                    gstin = "08AABCM1234F1Z5",
                    legalName = "Marudhar Textiles Private Limited",
                    tradeName = "Marudhar Textiles",
                    scheme = "Regular",
                    gstr1Frequency = "Monthly",
                    gstr3bFrequency = "Monthly",
                    portalUserId = "marudhar_gst"
                )
            )

            // 4. Initial Pending GST Returns
            gstDao.insertReturn(
                GstReturnEntity(
                    clientId = "SNL-2024-001",
                    clientName = "Marudhar Textiles Pvt Ltd",
                    gstin = "08AABCM1234F1Z5",
                    returnType = "GSTR-1",
                    financialYear = "2024-25",
                    taxPeriod = "October 2024",
                    originalDueDate = "2024-11-11",
                    filingStatus = "Ready for Filing",
                    taxableTurnover = 3850000.0,
                    taxLiability = 462000.0,
                    cgst = 115500.0,
                    sgst = 115500.0,
                    igst = 231000.0
                )
            )
            gstDao.insertReturn(
                GstReturnEntity(
                    clientId = "SNL-2024-001",
                    clientName = "Marudhar Textiles Pvt Ltd",
                    gstin = "08AABCM1234F1Z5",
                    returnType = "GSTR-3B",
                    financialYear = "2024-25",
                    taxPeriod = "October 2024",
                    originalDueDate = "2024-11-20",
                    filingStatus = "Pending",
                    taxableTurnover = 3850000.0,
                    taxLiability = 462000.0
                )
            )
            gstDao.insertReturn(
                GstReturnEntity(
                    clientId = "SNL-2024-002",
                    clientName = "Apeksha Gems & Jewellers",
                    gstin = "08AABPS9876K1ZP",
                    returnType = "GSTR-1",
                    financialYear = "2024-25",
                    taxPeriod = "September 2024",
                    originalDueDate = "2024-10-11",
                    filingStatus = "Filed",
                    actualFilingDate = "2024-10-10",
                    arnNumber = "AA0810240019284",
                    taxableTurnover = 2150000.0,
                    taxLiability = 64500.0
                )
            )

            // 5. Initial ITR Returns
            itrDao.insertItr(
                ItrReturnEntity(
                    clientId = "SNL-2024-001",
                    clientName = "Marudhar Textiles Pvt Ltd",
                    pan = "AABCM1234F",
                    financialYear = "2023-24",
                    assessmentYear = "2024-25",
                    itrForm = "ITR-6",
                    filingDueDate = "2024-11-15",
                    filingStatus = "Under Preparation",
                    taxableIncome = 4250000.0,
                    taxPayable = 1105000.0,
                    isTaxAuditApplicable = true,
                    auditForm = "Form 3CA-3CD",
                    auditStatus = "Uploaded",
                    auditFilingDate = "2024-10-06"
                )
            )
            itrDao.insertItr(
                ItrReturnEntity(
                    clientId = "SNL-2024-002",
                    clientName = "Apeksha Gems & Jewellers",
                    pan = "AABPS9876K",
                    financialYear = "2023-24",
                    assessmentYear = "2024-25",
                    itrForm = "ITR-3",
                    filingDueDate = "2024-07-31",
                    actualFilingDate = "2024-07-28",
                    filingStatus = "Filed",
                    acknowledgementNumber = "39201948201948",
                    taxableIncome = 1820000.0,
                    taxPayable = 358800.0,
                    refundAmount = 24500.0,
                    refundStatus = "Processed"
                )
            )

            // 6. Invoices
            invoiceDao.insertInvoice(
                ClientInvoiceEntity(
                    invoiceNumber = "INV-2024-089",
                    clientId = "SNL-2024-001",
                    clientName = "Marudhar Textiles Pvt Ltd",
                    serviceType = "Tax Audit & Corporate ITR Filing",
                    invoiceDate = "2024-10-15",
                    paymentDueDate = "2024-11-05",
                    totalAmount = 25000.0,
                    amountReceived = 10000.0,
                    pendingAmount = 15000.0,
                    paymentStatus = "Partially Paid",
                    paymentMode = "NEFT",
                    transactionRef = "SBIN004819203"
                )
            )
            invoiceDao.insertInvoice(
                ClientInvoiceEntity(
                    invoiceNumber = "INV-2024-090",
                    clientId = "SNL-2024-002",
                    clientName = "Apeksha Gems & Jewellers",
                    serviceType = "Quarterly GST & TDS Filing Retainer",
                    invoiceDate = "2024-10-20",
                    paymentDueDate = "2024-10-31",
                    totalAmount = 6500.0,
                    amountReceived = 0.0,
                    pendingAmount = 6500.0,
                    paymentStatus = "Unpaid",
                    paymentMode = "UPI"
                )
            )

            // 7. Reminders
            reminderDao.insertReminder(
                TaxReminderEntity(
                    clientId = "SNL-2024-001",
                    clientName = "Marudhar Textiles Pvt Ltd",
                    returnType = "GSTR-1",
                    taxPeriod = "October 2024",
                    dueDate = "2024-11-11",
                    reminderType = "7_DAYS",
                    reminderTime = "10:00",
                    isAlarmRinging = true
                )
            )
        }
    }
}
