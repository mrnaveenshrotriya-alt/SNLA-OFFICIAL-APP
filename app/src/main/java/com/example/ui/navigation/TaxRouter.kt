package com.example.ui.navigation

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.Assignment
import androidx.compose.material.icons.automirrored.filled.FactCheck
import androidx.compose.material.icons.automirrored.filled.ReceiptLong
import androidx.compose.material.icons.filled.*
import androidx.compose.ui.graphics.vector.ImageVector
import com.example.data.model.ClientEntity
import com.example.ui.viewmodel.DashboardStats
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

sealed class TaxRoute(val routeId: String) {
    // 15 Master Sections
    object Dashboard : TaxRoute("dashboard")
    object Clients : TaxRoute("clients")
    object GstManagement : TaxRoute("gst_management")
    object IncomeTax : TaxRoute("income_tax")
    object TaxAudit : TaxRoute("tax_audit")
    object TdsTcs : TaxRoute("tds_tcs")
    object EWayBill : TaxRoute("eway_bill")
    object DueDates : TaxRoute("due_dates")
    object Reminders : TaxRoute("reminders")
    object FeesPayment : TaxRoute("fees_payment")
    object Documents : TaxRoute("documents")
    object Reports : TaxRoute("reports")
    object PasswordVault : TaxRoute("password_vault")
    object BackupSync : TaxRoute("backup_sync")
    object Settings : TaxRoute("settings")

    // Sub-Screen
    data class ClientDetail(val client: ClientEntity) : TaxRoute("client_detail_${client.clientId}")
}

data class TaxNavigationItem(
    val sectionNumber: Int,
    val route: TaxRoute,
    val titleEnglish: String,
    val titleHindi: String,
    val icon: ImageVector,
    val getBadgeCount: (DashboardStats) -> Int? = { null }
)

class TaxRouter(
    initialRoute: TaxRoute = TaxRoute.Dashboard
) {
    private val _backStack = MutableStateFlow(listOf(initialRoute))
    val backStack: StateFlow<List<TaxRoute>> = _backStack.asStateFlow()

    private val _currentRoute = MutableStateFlow(initialRoute)
    val currentRoute: StateFlow<TaxRoute> = _currentRoute.asStateFlow()

    val canGoBack: Boolean
        get() = _backStack.value.size > 1

    fun navigateTo(route: TaxRoute, clearTop: Boolean = false) {
        val currentStack = _backStack.value.toMutableList()
        if (clearTop) {
            currentStack.clear()
            currentStack.add(TaxRoute.Dashboard)
            if (route != TaxRoute.Dashboard) {
                currentStack.add(route)
            }
        } else {
            // If already at target route, don't duplicate
            if (currentStack.lastOrNull() == route) return
            // Avoid cycle if returning to dashboard
            if (route == TaxRoute.Dashboard) {
                currentStack.clear()
                currentStack.add(TaxRoute.Dashboard)
            } else {
                currentStack.add(route)
            }
        }
        _backStack.value = currentStack
        _currentRoute.value = route
    }

    fun popBackStack(): Boolean {
        val currentStack = _backStack.value.toMutableList()
        if (currentStack.size > 1) {
            currentStack.removeAt(currentStack.lastIndex)
            _backStack.value = currentStack
            _currentRoute.value = currentStack.last()
            return true
        }
        return false
    }

    fun openClientDetail(client: ClientEntity) {
        navigateTo(TaxRoute.ClientDetail(client))
    }

    companion object {
        val MASTER_SECTIONS: List<TaxNavigationItem> = listOf(
            TaxNavigationItem(
                sectionNumber = 1,
                route = TaxRoute.Dashboard,
                titleEnglish = "Dashboard",
                titleHindi = "डैशबोर्ड",
                icon = Icons.Default.Dashboard
            ),
            TaxNavigationItem(
                sectionNumber = 2,
                route = TaxRoute.Clients,
                titleEnglish = "Clients Master",
                titleHindi = "क्लाइंट्स मास्टर",
                icon = Icons.Default.People,
                getBadgeCount = { it.totalClients }
            ),
            TaxNavigationItem(
                sectionNumber = 3,
                route = TaxRoute.GstManagement,
                titleEnglish = "GST Management",
                titleHindi = "जीएसटी प्रबंधन",
                icon = Icons.AutoMirrored.Filled.Assignment,
                getBadgeCount = { if (it.gstr1Pending + it.gstr3bPending > 0) it.gstr1Pending + it.gstr3bPending else null }
            ),
            TaxNavigationItem(
                sectionNumber = 4,
                route = TaxRoute.IncomeTax,
                titleEnglish = "Income Tax (ITR)",
                titleHindi = "इनकम टैक्स (ITR)",
                icon = Icons.Default.Calculate,
                getBadgeCount = { if (it.itrPending > 0) it.itrPending else null }
            ),
            TaxNavigationItem(
                sectionNumber = 5,
                route = TaxRoute.TaxAudit,
                titleEnglish = "Tax Audit (3CA/3CD)",
                titleHindi = "टैक्स ऑडिट (3CD)",
                icon = Icons.Default.VerifiedUser,
                getBadgeCount = { if (it.taxAuditPending > 0) it.taxAuditPending else null }
            ),
            TaxNavigationItem(
                sectionNumber = 6,
                route = TaxRoute.TdsTcs,
                titleEnglish = "TDS / TCS Hub",
                titleHindi = "टीडीएस / टीसीएस",
                icon = Icons.AutoMirrored.Filled.FactCheck,
                getBadgeCount = { if (it.tdsPending > 0) it.tdsPending else null }
            ),
            TaxNavigationItem(
                sectionNumber = 7,
                route = TaxRoute.EWayBill,
                titleEnglish = "E-Way Bill Tracking",
                titleHindi = "ई-वे बिल ट्रैकिंग",
                icon = Icons.Default.LocalShipping
            ),
            TaxNavigationItem(
                sectionNumber = 8,
                route = TaxRoute.DueDates,
                titleEnglish = "Due Date Calendar",
                titleHindi = "नियत तिथि कैलेंडर",
                icon = Icons.Default.CalendarMonth,
                getBadgeCount = { it.upcomingDueDatesCount }
            ),
            TaxNavigationItem(
                sectionNumber = 9,
                route = TaxRoute.Reminders,
                titleEnglish = "Reminders & Alarms",
                titleHindi = "अलार्म एवं अनुस्मारक",
                icon = Icons.Default.NotificationsActive
            ),
            TaxNavigationItem(
                sectionNumber = 10,
                route = TaxRoute.FeesPayment,
                titleEnglish = "Fees & Invoicing",
                titleHindi = "फीस एवं इनवॉइसिंग",
                icon = Icons.Default.AccountBalanceWallet
            ),
            TaxNavigationItem(
                sectionNumber = 11,
                route = TaxRoute.Documents,
                titleEnglish = "Documents Vault",
                titleHindi = "दस्तावेज वॉल्ट",
                icon = Icons.Default.Folder
            ),
            TaxNavigationItem(
                sectionNumber = 12,
                route = TaxRoute.Reports,
                titleEnglish = "Reports & PDF Export",
                titleHindi = "रिपोर्ट्स एवं प्रिंट",
                icon = Icons.Default.PictureAsPdf
            ),
            TaxNavigationItem(
                sectionNumber = 13,
                route = TaxRoute.PasswordVault,
                titleEnglish = "Secure Password Vault",
                titleHindi = "पासवर्ड वॉल्ट",
                icon = Icons.Default.Key
            ),
            TaxNavigationItem(
                sectionNumber = 14,
                route = TaxRoute.BackupSync,
                titleEnglish = "Supabase & Cloud Sync",
                titleHindi = "क्लाउड सिंक एवं बैकअप",
                icon = Icons.Default.CloudSync
            ),
            TaxNavigationItem(
                sectionNumber = 15,
                route = TaxRoute.Settings,
                titleEnglish = "Settings & Firm Logo",
                titleHindi = "सेटिंग्स एवं लोगो",
                icon = Icons.Default.Settings
            )
        )
    }
}
