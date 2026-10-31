package com.example.ui.components

import android.net.Uri
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.AsyncImage
import com.example.R
import com.example.ui.theme.*

@Composable
fun CompanyTopBar(
    customLogoUri: String?,
    isHindi: Boolean,
    onToggleLanguage: () -> Unit,
    onOpenSettings: () -> Unit,
    onOpenNavDrawer: () -> Unit
) {
    Surface(
        modifier = Modifier
            .fillMaxWidth()
            .testTag("company_top_bar"),
        color = DeepMaroon,
        tonalElevation = 4.dp
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .background(
                    Brush.verticalGradient(
                        colors = listOf(DeepMaroon, DeepMaroonDark)
                    )
                )
                .statusBarsPadding()
                .padding(horizontal = 12.dp, vertical = 8.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier.weight(1f)
                ) {
                    IconButton(
                        onClick = onOpenNavDrawer,
                        modifier = Modifier.testTag("nav_drawer_button")
                    ) {
                        Icon(
                            imageVector = Icons.Default.Menu,
                            contentDescription = "Open Navigation",
                            tint = RoyalGoldLight
                        )
                    }

                    Spacer(modifier = Modifier.width(6.dp))

                    // Company Logo (Uploaded or Default)
                    Box(
                        modifier = Modifier
                            .size(38.dp)
                            .clip(CircleShape)
                            .border(1.5.dp, RoyalGold, CircleShape)
                            .background(Color.White)
                    ) {
                        if (customLogoUri != null) {
                            AsyncImage(
                                model = Uri.parse(customLogoUri),
                                contentDescription = "Company Logo",
                                modifier = Modifier.fillMaxSize(),
                                contentScale = ContentScale.Crop
                            )
                        } else {
                            Image(
                                painter = painterResource(id = R.drawable.app_logo),
                                contentDescription = "Company Logo",
                                modifier = Modifier.fillMaxSize(),
                                contentScale = ContentScale.Crop
                            )
                        }
                    }

                    Spacer(modifier = Modifier.width(10.dp))

                    Column {
                        Text(
                            text = "SHREE NAVYA LEKHA",
                            color = RoyalGoldLight,
                            fontSize = 13.sp,
                            fontWeight = FontWeight.Bold,
                            letterSpacing = 0.5.sp
                        )
                        Text(
                            text = if (isHindi) "एसोसिएट्स • टैक्स एवं जीएसटी कंसल्टेंसी" else "ASSOCIATES • Tax & GST Compliance",
                            color = Color.White.copy(alpha = 0.85f),
                            fontSize = 10.sp,
                            fontWeight = FontWeight.Medium
                        )
                    }
                }

                Row(verticalAlignment = Alignment.CenterVertically) {
                    // Language Switcher Badge
                    Surface(
                        modifier = Modifier
                            .clip(RoundedCornerShape(16.dp))
                            .clickable { onToggleLanguage() }
                            .testTag("language_toggle_button"),
                        color = RoyalGold.copy(alpha = 0.2f),
                        border = androidx.compose.foundation.BorderStroke(1.dp, RoyalGold)
                    ) {
                        Text(
                            text = if (isHindi) "हिंदी" else "EN",
                            color = RoyalGoldLight,
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Bold,
                            modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                        )
                    }

                    IconButton(
                        onClick = onOpenSettings,
                        modifier = Modifier.testTag("settings_top_button")
                    ) {
                        Icon(
                            imageVector = Icons.Default.Settings,
                            contentDescription = "Settings",
                            tint = RoyalGoldLight
                        )
                    }
                }
            }
        }
    }
}

@Composable
fun StatusBadge(status: String) {
    val (bgColor, textColor) = when (status) {
        "Filed", "Nil Filed", "Active", "Paid", "Approved" ->
            Pair(StatusFiledGreenContainer, StatusFiledGreen)
        "Overdue", "Expired", "Suspended" ->
            Pair(StatusOverdueRedContainer, StatusOverdueRed)
        "Pending", "Data Awaited", "Unpaid" ->
            Pair(StatusPendingAmberContainer, StatusPendingAmber)
        "Under Preparation", "Ready for Filing", "In Transit", "Partially Paid" ->
            Pair(StatusProgressBlueContainer, StatusProgressBlue)
        else -> Pair(IvorySurfaceVariant, Color.DarkGray)
    }

    Surface(
        shape = RoundedCornerShape(12.dp),
        color = bgColor,
        modifier = Modifier.padding(vertical = 2.dp)
    ) {
        Text(
            text = status,
            color = textColor,
            fontSize = 11.sp,
            fontWeight = FontWeight.SemiBold,
            modifier = Modifier.padding(horizontal = 8.dp, vertical = 3.dp)
        )
    }
}
