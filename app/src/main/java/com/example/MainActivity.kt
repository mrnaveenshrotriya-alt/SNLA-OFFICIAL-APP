package com.example

import android.Manifest
import android.os.Build
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.BackHandler
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.result.contract.ActivityResultContracts
import androidx.activity.viewModels
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.Assignment
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import coil.compose.AsyncImage
import com.example.ui.components.CompanyTopBar
import com.example.ui.navigation.TaxNavigationItem
import com.example.ui.navigation.TaxRoute
import com.example.ui.navigation.TaxRouter
import com.example.ui.screens.*
import com.example.ui.theme.*
import com.example.ui.viewmodel.TaxViewModel
import kotlinx.coroutines.launch

class MainActivity : ComponentActivity() {

    private val taxViewModel: TaxViewModel by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        setContent {
            MyApplicationTheme {
                MainAppContainer(viewModel = taxViewModel)
            }
        }
    }
}

@Composable
fun MainAppContainer(viewModel: TaxViewModel) {
    val currentRoute by viewModel.currentRoute.collectAsStateWithLifecycle()
    val isHindi by viewModel.isHindi.collectAsStateWithLifecycle()
    val customLogoUri by viewModel.customLogoUri.collectAsStateWithLifecycle()
    val stats by viewModel.dashboardStats.collectAsStateWithLifecycle()
    val clients by viewModel.allClients.collectAsStateWithLifecycle()
    val gstReturns by viewModel.allGstReturns.collectAsStateWithLifecycle()
    val itrReturns by viewModel.allItrReturns.collectAsStateWithLifecycle()
    val tdsReturns by viewModel.allTdsReturns.collectAsStateWithLifecycle()
    val ewayBills by viewModel.allEWayBills.collectAsStateWithLifecycle()
    val dueDates by viewModel.allDueDates.collectAsStateWithLifecycle()
    val invoices by viewModel.allInvoices.collectAsStateWithLifecycle()
    val documents by viewModel.allDocuments.collectAsStateWithLifecycle()
    val vaultEntries by viewModel.allVaultEntries.collectAsStateWithLifecycle()
    val activeReminders by viewModel.activeReminders.collectAsStateWithLifecycle()
    val searchQuery by viewModel.searchQuery.collectAsStateWithLifecycle()
    val isVaultUnlocked by viewModel.isVaultUnlocked.collectAsStateWithLifecycle()

    val drawerState = rememberDrawerState(initialValue = DrawerValue.Closed)
    val coroutineScope = rememberCoroutineScope()

    // Request Notification permission for due date ringing alarms on Android 13+
    val notificationPermissionLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestPermission()
    ) {}

    LaunchedEffect(Unit) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            notificationPermissionLauncher.launch(Manifest.permission.POST_NOTIFICATIONS)
        }
    }

    // Centralized BackHandler responding to router backstack
    BackHandler(enabled = viewModel.router.canGoBack || drawerState.isOpen) {
        if (drawerState.isOpen) {
            coroutineScope.launch { drawerState.close() }
        } else {
            viewModel.popBackStack()
        }
    }

    ModalNavigationDrawer(
        drawerState = drawerState,
        drawerContent = {
            ModalDrawerSheet(
                modifier = Modifier.width(315.dp),
                drawerContainerColor = DeepMaroonDark,
                drawerContentColor = Color.White
            ) {
                // Firm Branding Drawer Header
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .background(DeepMaroon)
                        .padding(20.dp)
                ) {
                    Box(
                        modifier = Modifier
                            .size(54.dp)
                            .clip(CircleShape)
                            .border(2.dp, RoyalGold, CircleShape)
                            .background(Color.White)
                    ) {
                        if (customLogoUri != null) {
                            AsyncImage(
                                model = customLogoUri,
                                contentDescription = "Logo",
                                modifier = Modifier.fillMaxSize(),
                                contentScale = ContentScale.Crop
                            )
                        } else {
                            Image(
                                painter = painterResource(id = R.drawable.app_logo),
                                contentDescription = "Logo",
                                modifier = Modifier.fillMaxSize(),
                                contentScale = ContentScale.Crop
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(12.dp))

                    Text(
                        text = "SHREE NAVYA LEKHA",
                        color = RoyalGoldLight,
                        fontSize = 15.sp,
                        fontWeight = FontWeight.Bold,
                        letterSpacing = 0.5.sp
                    )
                    Text(
                        text = "ASSOCIATES • TAX CONSULTANCY",
                        color = Color.White.copy(alpha = 0.85f),
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Medium
                    )
                    Text(
                        text = "Jaipur, Rajasthan • 15 Compliance Modules",
                        color = RoyalGoldDark,
                        fontSize = 10.sp
                    )
                }

                HorizontalDivider(color = RoyalGoldDark.copy(alpha = 0.4f), thickness = 1.dp)

                // 15 Master Sections dynamically driven by Centralized Router
                LazyColumn(
                    modifier = Modifier
                        .fillMaxWidth()
                        .weight(1f)
                        .padding(vertical = 6.dp)
                ) {
                    items(TaxRouter.MASTER_SECTIONS, key = { it.sectionNumber }) { item ->
                        val isSelected = currentRoute == item.route
                        val badge = item.getBadgeCount(stats)
                        DrawerMasterItem(
                            item = item,
                            isSelected = isSelected,
                            isHindi = isHindi,
                            badgeCount = badge,
                            onClick = {
                                viewModel.navigateTo(item.route)
                                coroutineScope.launch { drawerState.close() }
                            }
                        )
                    }
                }
            }
        }
    ) {
        Scaffold(
            topBar = {
                if (currentRoute !is TaxRoute.ClientDetail) {
                    CompanyTopBar(
                        customLogoUri = customLogoUri,
                        isHindi = isHindi,
                        onToggleLanguage = { viewModel.toggleLanguage() },
                        onOpenSettings = { viewModel.navigateTo(TaxRoute.Settings) },
                        onOpenNavDrawer = {
                            coroutineScope.launch { drawerState.open() }
                        }
                    )
                }
            },
            bottomBar = {
                NavigationBar(
                    containerColor = DeepMaroonDark,
                    tonalElevation = 6.dp,
                    windowInsets = WindowInsets.navigationBars
                ) {
                    NavigationBarItem(
                        icon = { Icon(Icons.Default.Dashboard, contentDescription = "Dashboard") },
                        label = { Text("Home", fontSize = 10.sp) },
                        selected = currentRoute == TaxRoute.Dashboard,
                        colors = NavigationBarItemDefaults.colors(
                            selectedIconColor = DeepMaroon,
                            selectedTextColor = RoyalGoldLight,
                            indicatorColor = RoyalGoldLight,
                            unselectedIconColor = Color.White.copy(alpha = 0.7f),
                            unselectedTextColor = Color.White.copy(alpha = 0.7f)
                        ),
                        onClick = { viewModel.navigateTo(TaxRoute.Dashboard, clearTop = true) }
                    )
                    NavigationBarItem(
                        icon = { Icon(Icons.Default.People, contentDescription = "Clients") },
                        label = { Text("Clients", fontSize = 10.sp) },
                        selected = currentRoute == TaxRoute.Clients,
                        colors = NavigationBarItemDefaults.colors(
                            selectedIconColor = DeepMaroon,
                            selectedTextColor = RoyalGoldLight,
                            indicatorColor = RoyalGoldLight,
                            unselectedIconColor = Color.White.copy(alpha = 0.7f),
                            unselectedTextColor = Color.White.copy(alpha = 0.7f)
                        ),
                        onClick = { viewModel.navigateTo(TaxRoute.Clients) }
                    )
                    NavigationBarItem(
                        icon = { Icon(Icons.AutoMirrored.Filled.Assignment, contentDescription = "GST") },
                        label = { Text("GST", fontSize = 10.sp) },
                        selected = currentRoute == TaxRoute.GstManagement,
                        colors = NavigationBarItemDefaults.colors(
                            selectedIconColor = DeepMaroon,
                            selectedTextColor = RoyalGoldLight,
                            indicatorColor = RoyalGoldLight,
                            unselectedIconColor = Color.White.copy(alpha = 0.7f),
                            unselectedTextColor = Color.White.copy(alpha = 0.7f)
                        ),
                        onClick = { viewModel.navigateTo(TaxRoute.GstManagement) }
                    )
                    NavigationBarItem(
                        icon = { Icon(Icons.Default.Calculate, contentDescription = "ITR") },
                        label = { Text("ITR", fontSize = 10.sp) },
                        selected = currentRoute == TaxRoute.IncomeTax || currentRoute == TaxRoute.TaxAudit,
                        colors = NavigationBarItemDefaults.colors(
                            selectedIconColor = DeepMaroon,
                            selectedTextColor = RoyalGoldLight,
                            indicatorColor = RoyalGoldLight,
                            unselectedIconColor = Color.White.copy(alpha = 0.7f),
                            unselectedTextColor = Color.White.copy(alpha = 0.7f)
                        ),
                        onClick = { viewModel.navigateTo(TaxRoute.IncomeTax) }
                    )
                    NavigationBarItem(
                        icon = { Icon(Icons.Default.CalendarMonth, contentDescription = "Calendar") },
                        label = { Text("Due Dates", fontSize = 10.sp) },
                        selected = currentRoute == TaxRoute.DueDates,
                        colors = NavigationBarItemDefaults.colors(
                            selectedIconColor = DeepMaroon,
                            selectedTextColor = RoyalGoldLight,
                            indicatorColor = RoyalGoldLight,
                            unselectedIconColor = Color.White.copy(alpha = 0.7f),
                            unselectedTextColor = Color.White.copy(alpha = 0.7f)
                        ),
                        onClick = { viewModel.navigateTo(TaxRoute.DueDates) }
                    )
                }
            }
        ) { padding ->
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(padding)
            ) {
                when (val route = currentRoute) {
                    TaxRoute.Dashboard -> {
                        DashboardScreen(
                            stats = stats,
                            recentGstReturns = gstReturns,
                            dueDates = dueDates,
                            isHindi = isHindi,
                            onNavigate = { viewModel.navigateTo(it) },
                            onQuickAddClient = { viewModel.navigateTo(TaxRoute.Clients) },
                            onQuickAddReturn = { viewModel.navigateTo(TaxRoute.GstManagement) },
                            onQuickAddPayment = { viewModel.navigateTo(TaxRoute.FeesPayment) },
                            onTriggerAlarmTest = {
                                viewModel.testRingingAlarm("Marudhar Textiles", "GSTR-3B Urgent")
                            }
                        )
                    }
                    TaxRoute.Clients -> {
                        ClientsScreen(
                            clients = clients,
                            searchQuery = searchQuery,
                            isHindi = isHindi,
                            onSearchQueryChange = { viewModel.setSearchQuery(it) },
                            onSelectClient = { viewModel.openClientDetail(it) },
                            onAddClient = { viewModel.addClient(it) },
                            onDeleteClient = { viewModel.deleteClient(it) }
                        )
                    }
                    is TaxRoute.ClientDetail -> {
                        ClientDetailScreen(
                            client = route.client,
                            gstReturns = gstReturns,
                            itrReturns = itrReturns,
                            invoices = invoices,
                            documents = documents,
                            isHindi = isHindi,
                            onBack = { viewModel.popBackStack() },
                            onEditClient = { viewModel.updateClient(it) },
                            onDeleteClient = { viewModel.deleteClient(it) },
                            onAddGstReturn = { viewModel.addGstReturn(it) },
                            onMarkGstFiled = { id, arn -> viewModel.markGstAsFiled(id, arn) },
                            onAddInvoice = { viewModel.addInvoice(it) }
                        )
                    }
                    TaxRoute.GstManagement -> {
                        GstManagementScreen(
                            returns = gstReturns,
                            clients = clients,
                            isHindi = isHindi,
                            onAddReturn = { viewModel.addGstReturn(it) },
                            onMarkFiled = { id, arn -> viewModel.markGstAsFiled(id, arn) },
                            onAutoAssign = {}
                        )
                    }
                    TaxRoute.IncomeTax -> {
                        IncomeTaxScreen(
                            returns = itrReturns,
                            clients = clients,
                            isHindi = isHindi,
                            onAddItr = { viewModel.addItr(it) },
                            onUpdateItr = { viewModel.updateItr(it) }
                        )
                    }
                    TaxRoute.TaxAudit -> {
                        TaxAuditScreen(
                            auditCases = itrReturns,
                            clients = clients,
                            isHindi = isHindi,
                            onUpdateAuditCase = { viewModel.updateItr(it) },
                            onAddAuditCase = { viewModel.addItr(it) }
                        )
                    }
                    TaxRoute.TdsTcs -> {
                        TdsManagementScreen(
                            tdsReturns = tdsReturns,
                            clients = clients,
                            isHindi = isHindi,
                            onAddTds = { viewModel.addTds(it) },
                            onUpdateTds = { viewModel.updateTds(it) }
                        )
                    }
                    TaxRoute.EWayBill -> {
                        EWayBillScreen(
                            bills = ewayBills,
                            clients = clients,
                            isHindi = isHindi,
                            onAddBill = { viewModel.addEWayBill(it) }
                        )
                    }
                    TaxRoute.DueDates -> {
                        DueDateCalendarScreen(
                            dueDates = dueDates,
                            isHindi = isHindi,
                            onUpdateDueDate = { viewModel.updateDueDate(it) }
                        )
                    }
                    TaxRoute.Reminders -> {
                        RemindersAlarmScreen(
                            reminders = activeReminders,
                            isHindi = isHindi,
                            onTestAlarm = { client, ret -> viewModel.testRingingAlarm(client, ret) },
                            onSnooze = { viewModel.snoozeReminder(it) },
                            onMarkFiled = { viewModel.markReminderFiled(it) },
                            onAddReminder = { viewModel.addReminder(it) }
                        )
                    }
                    TaxRoute.FeesPayment -> {
                        FeesPaymentScreen(
                            invoices = invoices,
                            clients = clients,
                            isHindi = isHindi,
                            onAddInvoice = { viewModel.addInvoice(it) },
                            onUpdateInvoice = { viewModel.updateInvoice(it) }
                        )
                    }
                    TaxRoute.Documents -> {
                        DocumentsScreen(
                            documents = documents,
                            clients = clients,
                            isHindi = isHindi,
                            onAddDocument = { viewModel.addDocument(it) },
                            onDeleteDocument = { viewModel.deleteDocument(it) }
                        )
                    }
                    TaxRoute.Reports -> {
                        ReportsScreen(
                            clients = clients,
                            gstReturns = gstReturns,
                            isHindi = isHindi
                        )
                    }
                    TaxRoute.PasswordVault -> {
                        PasswordVaultScreen(
                            vaultEntries = vaultEntries,
                            clients = clients,
                            isVaultUnlocked = isVaultUnlocked,
                            isHindi = isHindi,
                            onUnlockVault = { viewModel.unlockVault(it) },
                            onLockVault = { viewModel.lockVault() },
                            onDecryptPassword = { viewModel.decryptVaultPassword(it) },
                            onAddEntry = { cId, cName, portal, user, pass, mob, email, notes ->
                                viewModel.addVaultEntry(cId, cName, portal, user, pass, mob, email, notes)
                            },
                            onDeleteEntry = { viewModel.deleteVaultEntry(it) }
                        )
                    }
                    TaxRoute.BackupSync -> {
                        BackupSyncScreen(
                            isHindi = isHindi
                        )
                    }
                    TaxRoute.Settings -> {
                        SettingsScreen(
                            customLogoUri = customLogoUri,
                            isHindi = isHindi,
                            onToggleLanguage = { viewModel.toggleLanguage() },
                            onLogoChanged = { viewModel.setCompanyLogoUri(it) }
                        )
                    }
                }
            }
        }
    }
}

@Composable
fun DrawerMasterItem(
    item: TaxNavigationItem,
    isSelected: Boolean,
    isHindi: Boolean,
    badgeCount: Int?,
    onClick: () -> Unit
) {
    NavigationDrawerItem(
        icon = {
            Icon(
                imageVector = item.icon,
                contentDescription = null,
                tint = if (isSelected) DeepMaroon else RoyalGoldLight,
                modifier = Modifier.size(20.dp)
            )
        },
        label = {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "${item.sectionNumber}. ${if (isHindi) item.titleHindi else item.titleEnglish}",
                    fontSize = 12.5.sp,
                    fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium,
                    modifier = Modifier.weight(1f)
                )

                if (badgeCount != null && badgeCount > 0) {
                    Surface(
                        shape = RoundedCornerShape(10.dp),
                        color = if (isSelected) DeepMaroon else RoyalGold,
                        contentColor = if (isSelected) RoyalGoldLight else DeepMaroonDark
                    ) {
                        Text(
                            text = badgeCount.toString(),
                            fontSize = 10.sp,
                            fontWeight = FontWeight.Bold,
                            modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                        )
                    }
                }
            }
        },
        selected = isSelected,
        onClick = onClick,
        colors = NavigationDrawerItemDefaults.colors(
            selectedContainerColor = RoyalGoldLight,
            selectedTextColor = DeepMaroonDark,
            unselectedTextColor = Color.White.copy(alpha = 0.9f)
        ),
        shape = RoundedCornerShape(8.dp),
        modifier = Modifier.padding(horizontal = 12.dp, vertical = 2.dp)
    )
}
