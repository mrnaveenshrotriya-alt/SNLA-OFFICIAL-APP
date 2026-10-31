package com.example.data.local

import androidx.room.*
import com.example.data.model.*
import kotlinx.coroutines.flow.Flow

@Dao
interface ClientDao {
    @Query("SELECT * FROM clients ORDER BY businessName ASC")
    fun getAllClients(): Flow<List<ClientEntity>>

    @Query("SELECT * FROM clients WHERE clientId = :clientId LIMIT 1")
    suspend fun getClientById(clientId: String): ClientEntity?

    @Query("SELECT * FROM clients WHERE businessName LIKE '%' || :query || '%' OR pan LIKE '%' || :query || '%' OR primaryGstin LIKE '%' || :query || '%' OR mobileNumber LIKE '%' || :query || '%'")
    fun searchClients(query: String): Flow<List<ClientEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertClient(client: ClientEntity)

    @Update
    suspend fun updateClient(client: ClientEntity)

    @Delete
    suspend fun deleteClient(client: ClientEntity)

    @Query("DELETE FROM clients WHERE clientId = :clientId")
    suspend fun deleteClientById(clientId: String)

    @Query("SELECT COUNT(*) FROM clients")
    fun getClientCount(): Flow<Int>

    // Additional GSTINs
    @Query("SELECT * FROM client_gstins WHERE clientId = :clientId")
    fun getGstinsForClient(clientId: String): Flow<List<ClientGstinEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertGstin(gstin: ClientGstinEntity)

    @Delete
    suspend fun deleteGstin(gstin: ClientGstinEntity)
}

@Dao
interface GstReturnDao {
    @Query("SELECT * FROM gst_returns ORDER BY originalDueDate ASC")
    fun getAllGstReturns(): Flow<List<GstReturnEntity>>

    @Query("SELECT * FROM gst_returns WHERE clientId = :clientId ORDER BY originalDueDate DESC")
    fun getReturnsForClient(clientId: String): Flow<List<GstReturnEntity>>

    @Query("SELECT * FROM gst_returns WHERE filingStatus = :status")
    fun getReturnsByStatus(status: String): Flow<List<GstReturnEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertReturn(gstReturn: GstReturnEntity): Long

    @Update
    suspend fun updateReturn(gstReturn: GstReturnEntity)

    @Delete
    suspend fun deleteReturn(gstReturn: GstReturnEntity)

    @Query("UPDATE gst_returns SET filingStatus = 'Filed', actualFilingDate = :filingDate, arnNumber = :arn WHERE id = :id")
    suspend fun markAsFiled(id: Long, filingDate: String, arn: String)

    @Query("SELECT COUNT(*) FROM gst_returns WHERE filingStatus != 'Filed' AND filingStatus != 'Nil Filed' AND filingStatus != 'Not Applicable'")
    fun getPendingCount(): Flow<Int>

    @Query("SELECT COUNT(*) FROM gst_returns WHERE returnType = 'GSTR-1' AND filingStatus != 'Filed' AND filingStatus != 'Nil Filed'")
    fun getGstr1PendingCount(): Flow<Int>

    @Query("SELECT COUNT(*) FROM gst_returns WHERE returnType = 'GSTR-3B' AND filingStatus != 'Filed' AND filingStatus != 'Nil Filed'")
    fun getGstr3bPendingCount(): Flow<Int>
}

@Dao
interface ItrReturnDao {
    @Query("SELECT * FROM itr_returns ORDER BY filingDueDate ASC")
    fun getAllItrReturns(): Flow<List<ItrReturnEntity>>

    @Query("SELECT * FROM itr_returns WHERE clientId = :clientId ORDER BY assessmentYear DESC")
    fun getItrsForClient(clientId: String): Flow<List<ItrReturnEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertItr(itr: ItrReturnEntity): Long

    @Update
    suspend fun updateItr(itr: ItrReturnEntity)

    @Delete
    suspend fun deleteItr(itr: ItrReturnEntity)

    @Query("SELECT COUNT(*) FROM itr_returns WHERE filingStatus != 'Filed'")
    fun getPendingItrCount(): Flow<Int>

    @Query("SELECT COUNT(*) FROM itr_returns WHERE isTaxAuditApplicable = 1 AND auditStatus != 'Approved'")
    fun getPendingTaxAuditCount(): Flow<Int>
}

@Dao
interface TdsReturnDao {
    @Query("SELECT * FROM tds_returns ORDER BY dueDate ASC")
    fun getAllTdsReturns(): Flow<List<TdsReturnEntity>>

    @Query("SELECT * FROM tds_returns WHERE clientId = :clientId ORDER BY dueDate DESC")
    fun getTdsForClient(clientId: String): Flow<List<TdsReturnEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertTds(tds: TdsReturnEntity): Long

    @Update
    suspend fun updateTds(tds: TdsReturnEntity)

    @Delete
    suspend fun deleteTds(tds: TdsReturnEntity)

    @Query("SELECT COUNT(*) FROM tds_returns WHERE filingStatus != 'Filed'")
    fun getPendingTdsCount(): Flow<Int>
}

@Dao
interface EWayBillDao {
    @Query("SELECT * FROM eway_bills ORDER BY validityExpiryDate ASC")
    fun getAllEWayBills(): Flow<List<EWayBillEntity>>

    @Query("SELECT * FROM eway_bills WHERE clientId = :clientId ORDER BY validityExpiryDate DESC")
    fun getEWayBillsForClient(clientId: String): Flow<List<EWayBillEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertEWayBill(bill: EWayBillEntity): Long

    @Update
    suspend fun updateEWayBill(bill: EWayBillEntity)

    @Delete
    suspend fun deleteEWayBill(bill: EWayBillEntity)
}

@Dao
interface StatutoryDueDateDao {
    @Query("SELECT * FROM statutory_due_dates ORDER BY originalDueDate ASC")
    fun getAllDueDates(): Flow<List<StatutoryDueDateEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertDueDate(dueDate: StatutoryDueDateEntity): Long

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAll(dueDates: List<StatutoryDueDateEntity>)

    @Update
    suspend fun updateDueDate(dueDate: StatutoryDueDateEntity)

    @Query("SELECT COUNT(*) FROM statutory_due_dates")
    suspend fun count(): Int
}

@Dao
interface ClientInvoiceDao {
    @Query("SELECT * FROM client_invoices ORDER BY invoiceDate DESC")
    fun getAllInvoices(): Flow<List<ClientInvoiceEntity>>

    @Query("SELECT * FROM client_invoices WHERE clientId = :clientId ORDER BY invoiceDate DESC")
    fun getInvoicesForClient(clientId: String): Flow<List<ClientInvoiceEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertInvoice(invoice: ClientInvoiceEntity)

    @Update
    suspend fun updateInvoice(invoice: ClientInvoiceEntity)

    @Delete
    suspend fun deleteInvoice(invoice: ClientInvoiceEntity)

    @Query("SELECT SUM(pendingAmount) FROM client_invoices WHERE paymentStatus != 'Paid'")
    fun getTotalPendingAmount(): Flow<Double?>
}

@Dao
interface ClientDocumentDao {
    @Query("SELECT * FROM client_documents ORDER BY uploadDate DESC")
    fun getAllDocuments(): Flow<List<ClientDocumentEntity>>

    @Query("SELECT * FROM client_documents WHERE clientId = :clientId ORDER BY uploadDate DESC")
    fun getDocumentsForClient(clientId: String): Flow<List<ClientDocumentEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertDocument(doc: ClientDocumentEntity): Long

    @Delete
    suspend fun deleteDocument(doc: ClientDocumentEntity)
}

@Dao
interface VaultDao {
    @Query("SELECT * FROM password_vault ORDER BY clientName ASC")
    fun getAllVaultEntries(): Flow<List<VaultCredentialEntity>>

    @Query("SELECT * FROM password_vault WHERE clientId = :clientId")
    fun getVaultForClient(clientId: String): Flow<List<VaultCredentialEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertVaultEntry(entry: VaultCredentialEntity): Long

    @Update
    suspend fun updateVaultEntry(entry: VaultCredentialEntity)

    @Delete
    suspend fun deleteVaultEntry(entry: VaultCredentialEntity)
}

@Dao
interface ReminderDao {
    @Query("SELECT * FROM tax_reminders WHERE isEnabled = 1 AND isFiled = 0 ORDER BY dueDate ASC")
    fun getActiveReminders(): Flow<List<TaxReminderEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertReminder(reminder: TaxReminderEntity): Long

    @Update
    suspend fun updateReminder(reminder: TaxReminderEntity)

    @Delete
    suspend fun deleteReminder(reminder: TaxReminderEntity)

    @Query("UPDATE tax_reminders SET isFiled = 1 WHERE id = :id")
    suspend fun markReminderFiled(id: Long)
}

@Dao
interface CommunicationLogDao {
    @Query("SELECT * FROM communication_logs ORDER BY sentTimestamp DESC")
    fun getAllLogs(): Flow<List<CommunicationLogEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertLog(log: CommunicationLogEntity): Long
}
