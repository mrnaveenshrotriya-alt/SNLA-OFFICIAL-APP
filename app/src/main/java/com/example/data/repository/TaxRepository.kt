package com.example.data.repository

import com.example.data.local.*
import com.example.data.model.*
import kotlinx.coroutines.flow.Flow
import java.text.SimpleDateFormat
import java.util.*

class TaxRepository(
    private val appDatabase: AppDatabase
) {
    private val clientDao = appDatabase.clientDao()
    private val gstReturnDao = appDatabase.gstReturnDao()
    private val itrReturnDao = appDatabase.itrReturnDao()
    private val tdsReturnDao = appDatabase.tdsReturnDao()
    private val ewayBillDao = appDatabase.ewayBillDao()
    private val statutoryDueDateDao = appDatabase.statutoryDueDateDao()
    private val clientInvoiceDao = appDatabase.clientInvoiceDao()
    private val clientDocumentDao = appDatabase.clientDocumentDao()
    private val vaultDao = appDatabase.vaultDao()
    private val reminderDao = appDatabase.reminderDao()
    private val communicationLogDao = appDatabase.communicationLogDao()

    // Clients
    val allClients: Flow<List<ClientEntity>> = clientDao.getAllClients()
    val clientCount: Flow<Int> = clientDao.getClientCount()

    fun searchClients(query: String): Flow<List<ClientEntity>> = clientDao.searchClients(query)
    suspend fun getClientById(id: String): ClientEntity? = clientDao.getClientById(id)
    suspend fun insertClient(client: ClientEntity) = clientDao.insertClient(client)
    suspend fun updateClient(client: ClientEntity) = clientDao.updateClient(client)
    suspend fun deleteClient(client: ClientEntity) = clientDao.deleteClient(client)
    suspend fun deleteClientById(id: String) = clientDao.deleteClientById(id)

    fun getGstinsForClient(clientId: String): Flow<List<ClientGstinEntity>> = clientDao.getGstinsForClient(clientId)
    suspend fun insertGstin(gstin: ClientGstinEntity) = clientDao.insertGstin(gstin)
    suspend fun deleteGstin(gstin: ClientGstinEntity) = clientDao.deleteGstin(gstin)

    // GST Returns
    val allGstReturns: Flow<List<GstReturnEntity>> = gstReturnDao.getAllGstReturns()
    val pendingGstCount: Flow<Int> = gstReturnDao.getPendingCount()
    val gstr1PendingCount: Flow<Int> = gstReturnDao.getGstr1PendingCount()
    val gstr3bPendingCount: Flow<Int> = gstReturnDao.getGstr3bPendingCount()

    fun getGstReturnsForClient(clientId: String): Flow<List<GstReturnEntity>> = gstReturnDao.getReturnsForClient(clientId)
    suspend fun insertGstReturn(gstReturn: GstReturnEntity): Long = gstReturnDao.insertReturn(gstReturn)
    suspend fun updateGstReturn(gstReturn: GstReturnEntity) = gstReturnDao.updateReturn(gstReturn)
    suspend fun deleteGstReturn(gstReturn: GstReturnEntity) = gstReturnDao.deleteReturn(gstReturn)
    suspend fun markGstAsFiled(id: Long, filingDate: String, arn: String) = gstReturnDao.markAsFiled(id, filingDate, arn)

    // Auto-generate applicable returns for a client based on scheme
    suspend fun autoAssignGstReturns(client: ClientEntity, scheme: String, isMonthly: Boolean, fy: String, period: String, dueDateGstr1: String, dueDateGstr3b: String) {
        if (scheme.equals("Composition", ignoreCase = true)) {
            // CMP-08 quarterly & GSTR-4 annual
            insertGstReturn(
                GstReturnEntity(
                    clientId = client.clientId,
                    clientName = client.businessName,
                    gstin = client.primaryGstin,
                    returnType = "CMP-08",
                    financialYear = fy,
                    taxPeriod = period,
                    isQuarterly = true,
                    originalDueDate = dueDateGstr3b,
                    filingStatus = "Pending"
                )
            )
        } else {
            // Regular: GSTR-1 and GSTR-3B
            insertGstReturn(
                GstReturnEntity(
                    clientId = client.clientId,
                    clientName = client.businessName,
                    gstin = client.primaryGstin,
                    returnType = "GSTR-1",
                    financialYear = fy,
                    taxPeriod = period,
                    isQuarterly = !isMonthly,
                    originalDueDate = dueDateGstr1,
                    filingStatus = "Pending"
                )
            )
            insertGstReturn(
                GstReturnEntity(
                    clientId = client.clientId,
                    clientName = client.businessName,
                    gstin = client.primaryGstin,
                    returnType = "GSTR-3B",
                    financialYear = fy,
                    taxPeriod = period,
                    isQuarterly = !isMonthly,
                    originalDueDate = dueDateGstr3b,
                    filingStatus = "Pending"
                )
            )
        }
    }

    // Income Tax
    val allItrReturns: Flow<List<ItrReturnEntity>> = itrReturnDao.getAllItrReturns()
    val pendingItrCount: Flow<Int> = itrReturnDao.getPendingItrCount()
    val pendingTaxAuditCount: Flow<Int> = itrReturnDao.getPendingTaxAuditCount()

    fun getItrsForClient(clientId: String): Flow<List<ItrReturnEntity>> = itrReturnDao.getItrsForClient(clientId)
    suspend fun insertItr(itr: ItrReturnEntity): Long = itrReturnDao.insertItr(itr)
    suspend fun updateItr(itr: ItrReturnEntity) = itrReturnDao.updateItr(itr)
    suspend fun deleteItr(itr: ItrReturnEntity) = itrReturnDao.deleteItr(itr)

    // TDS / TCS
    val allTdsReturns: Flow<List<TdsReturnEntity>> = tdsReturnDao.getAllTdsReturns()
    val pendingTdsCount: Flow<Int> = tdsReturnDao.getPendingTdsCount()

    fun getTdsForClient(clientId: String): Flow<List<TdsReturnEntity>> = tdsReturnDao.getTdsForClient(clientId)
    suspend fun insertTds(tds: TdsReturnEntity): Long = tdsReturnDao.insertTds(tds)
    suspend fun updateTds(tds: TdsReturnEntity) = tdsReturnDao.updateTds(tds)
    suspend fun deleteTds(tds: TdsReturnEntity) = tdsReturnDao.deleteTds(tds)

    // E-Way Bills
    val allEWayBills: Flow<List<EWayBillEntity>> = ewayBillDao.getAllEWayBills()
    fun getEWayBillsForClient(clientId: String): Flow<List<EWayBillEntity>> = ewayBillDao.getEWayBillsForClient(clientId)
    suspend fun insertEWayBill(bill: EWayBillEntity): Long = ewayBillDao.insertEWayBill(bill)
    suspend fun updateEWayBill(bill: EWayBillEntity) = ewayBillDao.updateEWayBill(bill)
    suspend fun deleteEWayBill(bill: EWayBillEntity) = ewayBillDao.deleteEWayBill(bill)

    // Statutory Due Dates
    val allDueDates: Flow<List<StatutoryDueDateEntity>> = statutoryDueDateDao.getAllDueDates()
    suspend fun insertDueDate(dueDate: StatutoryDueDateEntity): Long = statutoryDueDateDao.insertDueDate(dueDate)
    suspend fun updateDueDate(dueDate: StatutoryDueDateEntity) = statutoryDueDateDao.updateDueDate(dueDate)

    // Client Invoices & Ledger
    val allInvoices: Flow<List<ClientInvoiceEntity>> = clientInvoiceDao.getAllInvoices()
    val totalPendingAmount: Flow<Double?> = clientInvoiceDao.getTotalPendingAmount()
    fun getInvoicesForClient(clientId: String): Flow<List<ClientInvoiceEntity>> = clientInvoiceDao.getInvoicesForClient(clientId)
    suspend fun insertInvoice(invoice: ClientInvoiceEntity) = clientInvoiceDao.insertInvoice(invoice)
    suspend fun updateInvoice(invoice: ClientInvoiceEntity) = clientInvoiceDao.updateInvoice(invoice)
    suspend fun deleteInvoice(invoice: ClientInvoiceEntity) = clientInvoiceDao.deleteInvoice(invoice)

    // Documents
    val allDocuments: Flow<List<ClientDocumentEntity>> = clientDocumentDao.getAllDocuments()
    fun getDocumentsForClient(clientId: String): Flow<List<ClientDocumentEntity>> = clientDocumentDao.getDocumentsForClient(clientId)
    suspend fun insertDocument(doc: ClientDocumentEntity): Long = clientDocumentDao.insertDocument(doc)
    suspend fun deleteDocument(doc: ClientDocumentEntity) = clientDocumentDao.deleteDocument(doc)

    // Password Vault
    val allVaultEntries: Flow<List<VaultCredentialEntity>> = vaultDao.getAllVaultEntries()
    fun getVaultForClient(clientId: String): Flow<List<VaultCredentialEntity>> = vaultDao.getVaultForClient(clientId)
    suspend fun insertVaultEntry(entry: VaultCredentialEntity): Long = vaultDao.insertVaultEntry(entry)
    suspend fun updateVaultEntry(entry: VaultCredentialEntity) = vaultDao.updateVaultEntry(entry)
    suspend fun deleteVaultEntry(entry: VaultCredentialEntity) = vaultDao.deleteVaultEntry(entry)

    // Reminders
    val activeReminders: Flow<List<TaxReminderEntity>> = reminderDao.getActiveReminders()
    suspend fun insertReminder(reminder: TaxReminderEntity): Long = reminderDao.insertReminder(reminder)
    suspend fun updateReminder(reminder: TaxReminderEntity) = reminderDao.updateReminder(reminder)
    suspend fun deleteReminder(reminder: TaxReminderEntity) = reminderDao.deleteReminder(reminder)
    suspend fun markReminderFiled(id: Long) = reminderDao.markReminderFiled(id)

    // Communication Log
    val communicationLogs: Flow<List<CommunicationLogEntity>> = communicationLogDao.getAllLogs()
    suspend fun insertCommunicationLog(log: CommunicationLogEntity): Long = communicationLogDao.insertLog(log)
}
