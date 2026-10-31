package com.example.ui.viewmodel

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.data.local.AppDatabase
import com.example.data.model.*
import com.example.data.repository.TaxRepository
import com.example.service.AlarmManagerHelper
import com.example.service.VaultCrypto
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.*

import com.example.ui.navigation.TaxRoute
import com.example.ui.navigation.TaxRouter

data class DashboardStats(
    val totalClients: Int = 0,
    val regularGstClients: Int = 0,
    val compositionClients: Int = 0,
    val gstr1Pending: Int = 0,
    val gstr3bPending: Int = 0,
    val itrPending: Int = 0,
    val taxAuditPending: Int = 0,
    val tdsPending: Int = 0,
    val totalPendingFees: Double = 0.0,
    val upcomingDueDatesCount: Int = 0
)

class TaxViewModel(application: Application) : AndroidViewModel(application) {

    val database: AppDatabase = AppDatabase.getDatabase(application, viewModelScope)
    val repository: TaxRepository = TaxRepository(database)

    // Centralized State-Based Router
    val router: TaxRouter = TaxRouter(TaxRoute.Dashboard)
    val currentRoute: StateFlow<TaxRoute> = router.currentRoute

    private val _selectedClient = MutableStateFlow<ClientEntity?>(null)
    val selectedClient: StateFlow<ClientEntity?> = _selectedClient.asStateFlow()

    // Multi-Language Toggle
    private val _isHindi = MutableStateFlow(false)
    val isHindi: StateFlow<Boolean> = _isHindi.asStateFlow()

    // Search
    private val _searchQuery = MutableStateFlow("")
    val searchQuery: StateFlow<String> = _searchQuery.asStateFlow()

    // Company Logo (URI string or null for default resource)
    private val _customLogoUri = MutableStateFlow<String?>(null)
    val customLogoUri: StateFlow<String?> = _customLogoUri.asStateFlow()

    // Vault Authentication State
    private val _isVaultUnlocked = MutableStateFlow(false)
    val isVaultUnlocked: StateFlow<Boolean> = _isVaultUnlocked.asStateFlow()
    private val _masterPin = MutableStateFlow("1234") // Default master PIN

    // Data Flows from Repository
    val allClients: StateFlow<List<ClientEntity>> = repository.allClients
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val allGstReturns: StateFlow<List<GstReturnEntity>> = repository.allGstReturns
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val allItrReturns: StateFlow<List<ItrReturnEntity>> = repository.allItrReturns
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val allTdsReturns: StateFlow<List<TdsReturnEntity>> = repository.allTdsReturns
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val allEWayBills: StateFlow<List<EWayBillEntity>> = repository.allEWayBills
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val allDueDates: StateFlow<List<StatutoryDueDateEntity>> = repository.allDueDates
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val allInvoices: StateFlow<List<ClientInvoiceEntity>> = repository.allInvoices
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val allDocuments: StateFlow<List<ClientDocumentEntity>> = repository.allDocuments
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val allVaultEntries: StateFlow<List<VaultCredentialEntity>> = repository.allVaultEntries
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val activeReminders: StateFlow<List<TaxReminderEntity>> = repository.activeReminders
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    // Dashboard aggregated stats
    val dashboardStats: StateFlow<DashboardStats> = combine(
        combine(allClients, allGstReturns, allItrReturns) { clients, gst, itrs ->
            Triple(clients, gst, itrs)
        },
        combine(allTdsReturns, allInvoices, allDueDates) { tds, invoices, dueDates ->
            Triple(tds, invoices, dueDates)
        }
    ) { (clients, gst, itrs), (tds, invoices, dueDates) ->
        val regular = clients.count { it.constitution.contains("Regular", ignoreCase = true) || !it.constitution.contains("Composition", ignoreCase = true) }
        val comp = clients.count { it.constitution.contains("Composition", ignoreCase = true) }
        val g1Pending = gst.count { it.returnType == "GSTR-1" && it.filingStatus != "Filed" && it.filingStatus != "Nil Filed" }
        val g3Pending = gst.count { (it.returnType == "GSTR-3B" || it.returnType == "CMP-08") && it.filingStatus != "Filed" && it.filingStatus != "Nil Filed" }
        val itrPend = itrs.count { it.filingStatus != "Filed" }
        val auditPend = itrs.count { it.isTaxAuditApplicable && it.auditStatus != "Approved" }
        val tdsPend = tds.count { it.filingStatus != "Filed" }
        val pendingFees = invoices.filter { it.paymentStatus != "Paid" }.sumOf { it.pendingAmount }

        DashboardStats(
            totalClients = clients.size,
            regularGstClients = regular,
            compositionClients = comp,
            gstr1Pending = g1Pending,
            gstr3bPending = g3Pending,
            itrPending = itrPend,
            taxAuditPending = auditPend,
            tdsPending = tdsPend,
            totalPendingFees = pendingFees,
            upcomingDueDatesCount = dueDates.size
        )
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), DashboardStats())

    // Navigation functions via centralized router
    fun navigateTo(route: TaxRoute, clearTop: Boolean = false) {
        router.navigateTo(route, clearTop)
    }

    fun popBackStack(): Boolean {
        return router.popBackStack()
    }

    fun openClientDetail(client: ClientEntity) {
        _selectedClient.value = client
        router.openClientDetail(client)
    }

    fun toggleLanguage() {
        _isHindi.value = !_isHindi.value
    }

    fun setSearchQuery(query: String) {
        _searchQuery.value = query
    }

    fun setCompanyLogoUri(uri: String?) {
        _customLogoUri.value = uri
    }

    // Vault Unlock
    fun unlockVault(pin: String): Boolean {
        if (pin == _masterPin.value) {
            _isVaultUnlocked.value = true
            return true
        }
        return false
    }

    fun lockVault() {
        _isVaultUnlocked.value = false
    }

    fun setMasterPin(newPin: String) {
        _masterPin.value = newPin
    }

    fun decryptVaultPassword(entry: VaultCredentialEntity): String {
        return VaultCrypto.decrypt(entry.encryptedPassword, entry.iv, _masterPin.value)
    }

    // CRUD Operations: Client
    fun addClient(client: ClientEntity) {
        viewModelScope.launch {
            repository.insertClient(client)
        }
    }

    fun updateClient(client: ClientEntity) {
        viewModelScope.launch {
            repository.updateClient(client)
            if (_selectedClient.value?.clientId == client.clientId) {
                _selectedClient.value = client
            }
        }
    }

    fun deleteClient(client: ClientEntity) {
        viewModelScope.launch {
            repository.deleteClient(client)
            if (_selectedClient.value?.clientId == client.clientId) {
                _selectedClient.value = null
                router.navigateTo(TaxRoute.Clients, clearTop = true)
            }
        }
    }

    // CRUD: GST Return
    fun addGstReturn(r: GstReturnEntity) {
        viewModelScope.launch {
            repository.insertGstReturn(r)
        }
    }

    fun updateGstReturn(r: GstReturnEntity) {
        viewModelScope.launch {
            repository.updateGstReturn(r)
        }
    }

    fun markGstAsFiled(id: Long, arn: String) {
        val today = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).format(Date())
        viewModelScope.launch {
            repository.markGstAsFiled(id, today, arn)
        }
    }

    // CRUD: ITR
    fun addItr(itr: ItrReturnEntity) {
        viewModelScope.launch {
            repository.insertItr(itr)
        }
    }

    fun updateItr(itr: ItrReturnEntity) {
        viewModelScope.launch {
            repository.updateItr(itr)
        }
    }

    // CRUD: TDS
    fun addTds(tds: TdsReturnEntity) {
        viewModelScope.launch {
            repository.insertTds(tds)
        }
    }

    fun updateTds(tds: TdsReturnEntity) {
        viewModelScope.launch {
            repository.updateTds(tds)
        }
    }

    // CRUD: E-Way Bill
    fun addEWayBill(bill: EWayBillEntity) {
        viewModelScope.launch {
            repository.insertEWayBill(bill)
        }
    }

    fun updateEWayBill(bill: EWayBillEntity) {
        viewModelScope.launch {
            repository.updateEWayBill(bill)
        }
    }

    // CRUD: Invoices
    fun addInvoice(invoice: ClientInvoiceEntity) {
        viewModelScope.launch {
            repository.insertInvoice(invoice)
        }
    }

    fun updateInvoice(invoice: ClientInvoiceEntity) {
        viewModelScope.launch {
            repository.updateInvoice(invoice)
        }
    }

    // CRUD: Vault Credential
    fun addVaultEntry(clientId: String, clientName: String, portal: String, loginId: String, plainPass: String, mobile: String, email: String, notes: String) {
        viewModelScope.launch {
            val enc = VaultCrypto.encrypt(plainPass, _masterPin.value)
            val entry = VaultCredentialEntity(
                clientId = clientId,
                clientName = clientName,
                portalName = portal,
                loginId = loginId,
                encryptedPassword = enc.cipherTextBase64,
                iv = enc.ivBase64,
                registeredMobile = mobile,
                registeredEmail = email,
                securityNotes = notes
            )
            repository.insertVaultEntry(entry)
        }
    }

    fun deleteVaultEntry(entry: VaultCredentialEntity) {
        viewModelScope.launch {
            repository.deleteVaultEntry(entry)
        }
    }

    // CRUD: Documents
    fun addDocument(doc: ClientDocumentEntity) {
        viewModelScope.launch {
            repository.insertDocument(doc)
        }
    }

    fun deleteDocument(doc: ClientDocumentEntity) {
        viewModelScope.launch {
            repository.deleteDocument(doc)
        }
    }

    // CRUD: Reminders & Alarm Trigger
    fun addReminder(reminder: TaxReminderEntity) {
        viewModelScope.launch {
            val id = repository.insertReminder(reminder)
            val scheduled = reminder.copy(id = id)
            AlarmManagerHelper.scheduleReminderAlarm(getApplication(), scheduled)
        }
    }

    fun markReminderFiled(id: Long) {
        viewModelScope.launch {
            repository.markReminderFiled(id)
            AlarmManagerHelper.cancelAlarm(getApplication(), id)
        }
    }

    fun testRingingAlarm(clientName: String, returnType: String) {
        AlarmManagerHelper.triggerTestAlarm(getApplication(), clientName, returnType)
    }

    fun snoozeReminder(reminderId: Long, minutes: Int = 10) {
        AlarmManagerHelper.snoozeAlarm(getApplication(), reminderId, minutes)
    }

    // Statutory Due Dates
    fun updateDueDate(dueDate: StatutoryDueDateEntity) {
        viewModelScope.launch {
            repository.updateDueDate(dueDate.copy(isCustomOrModified = true))
        }
    }
}
