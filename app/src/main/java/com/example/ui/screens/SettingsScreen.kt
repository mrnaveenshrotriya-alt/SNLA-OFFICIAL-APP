package com.example.ui.screens

import android.net.Uri
import android.widget.Toast
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.PickVisualMediaRequest
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.AsyncImage
import com.example.R
import com.example.ui.theme.*

@Composable
fun SettingsScreen(
    customLogoUri: String?,
    isHindi: Boolean,
    onToggleLanguage: () -> Unit,
    onLogoChanged: (String?) -> Unit
) {
    val context = LocalContext.current

    // Photo picker launcher for company logo
    val photoPickerLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.PickVisualMedia()
    ) { uri: Uri? ->
        if (uri != null) {
            onLogoChanged(uri.toString())
            Toast.makeText(context, "Company logo updated successfully!", Toast.LENGTH_SHORT).show()
        }
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(IvoryBackground)
            .testTag("settings_screen")
    ) {
        Surface(color = Color.White, tonalElevation = 2.dp, modifier = Modifier.fillMaxWidth()) {
            Column(modifier = Modifier.padding(14.dp)) {
                Text(
                    text = if (isHindi) "कंपनी सेटिंग्स एवं ब्रांडिंग (Firm Settings)" else "Firm Settings & Branding",
                    fontSize = 15.sp,
                    fontWeight = FontWeight.Bold,
                    color = DeepMaroon
                )
                Text(
                    text = "SHREE NAVYA LEKHA ASSOCIATES • Jaipur, Rajasthan",
                    fontSize = 11.sp,
                    color = Color.Gray
                )
            }
        }

        LazyColumn(
            modifier = Modifier.fillMaxSize(),
            contentPadding = PaddingValues(16.dp),
            verticalArrangement = Arrangement.spacedBy(14.dp)
        ) {
            // Company Logo Management Card
            item {
                Surface(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(12.dp),
                    color = Color.White,
                    border = androidx.compose.foundation.BorderStroke(1.dp, IvoryOutline),
                    tonalElevation = 2.dp
                ) {
                    Column(
                        modifier = Modifier.padding(16.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Text(
                            text = if (isHindi) "कंपनी का आधिकारिक लोगो (Official Logo)" else "Official Company Logo",
                            fontSize = 14.sp,
                            fontWeight = FontWeight.Bold,
                            color = DeepMaroon
                        )
                        Text(
                            text = "This logo appears on Login, Dashboard, PDF Reports & Invoices",
                            fontSize = 11.sp,
                            color = Color.Gray
                        )

                        Spacer(modifier = Modifier.height(14.dp))

                        // Logo Display Preview
                        Box(
                            modifier = Modifier
                                .size(90.dp)
                                .clip(CircleShape)
                                .border(2.5.dp, RoyalGold, CircleShape)
                                .background(Color.White)
                        ) {
                            if (customLogoUri != null) {
                                AsyncImage(
                                    model = Uri.parse(customLogoUri),
                                    contentDescription = "Custom Logo",
                                    modifier = Modifier.fillMaxSize(),
                                    contentScale = ContentScale.Crop
                                )
                            } else {
                                Image(
                                    painter = painterResource(id = R.drawable.app_logo),
                                    contentDescription = "Default Royal Logo",
                                    modifier = Modifier.fillMaxSize(),
                                    contentScale = ContentScale.Crop
                                )
                            }
                        }

                        Spacer(modifier = Modifier.height(14.dp))

                        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                            Button(
                                onClick = {
                                    photoPickerLauncher.launch(
                                        PickVisualMediaRequest(ActivityResultContracts.PickVisualMedia.ImageOnly)
                                    )
                                },
                                colors = ButtonDefaults.buttonColors(containerColor = DeepMaroon, contentColor = RoyalGoldLight),
                                shape = RoundedCornerShape(8.dp)
                            ) {
                                Icon(Icons.Default.CloudUpload, contentDescription = null, modifier = Modifier.size(16.dp))
                                Spacer(modifier = Modifier.width(6.dp))
                                Text(if (customLogoUri != null) "Change Logo" else "Upload Logo", fontSize = 12.sp)
                            }

                            if (customLogoUri != null) {
                                OutlinedButton(
                                    onClick = {
                                        onLogoChanged(null)
                                        Toast.makeText(context, "Reset to default firm logo", Toast.LENGTH_SHORT).show()
                                    },
                                    shape = RoundedCornerShape(8.dp),
                                    colors = ButtonDefaults.outlinedButtonColors(contentColor = StatusOverdueRed),
                                    border = androidx.compose.foundation.BorderStroke(1.dp, StatusOverdueRed)
                                ) {
                                    Text("Remove Logo", fontSize = 12.sp)
                                }
                            }
                        }
                    }
                }
            }

            // Language & Regional Settings
            item {
                Surface(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(12.dp),
                    color = Color.White,
                    border = androidx.compose.foundation.BorderStroke(1.dp, IvoryOutline),
                    tonalElevation = 2.dp
                ) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Text(
                            text = if (isHindi) "भाषा एवं क्षेत्रीय प्राथमिकताएं" else "Language & Localization",
                            fontSize = 14.sp,
                            fontWeight = FontWeight.Bold,
                            color = DeepMaroon
                        )

                        Spacer(modifier = Modifier.height(10.dp))

                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Column {
                                Text(
                                    text = if (isHindi) "वर्तमान भाषा: हिंदी (Hindi)" else "Current Language: English (EN)",
                                    fontSize = 13.sp,
                                    fontWeight = FontWeight.SemiBold,
                                    color = Color.DarkGray
                                )
                                Text(
                                    text = "India • Currency: INR (₹) • Timezone: Asia/Kolkata",
                                    fontSize = 11.sp,
                                    color = Color.Gray
                                )
                            }

                            Button(
                                onClick = onToggleLanguage,
                                colors = ButtonDefaults.buttonColors(containerColor = RoyalGoldDark, contentColor = Color.White),
                                shape = RoundedCornerShape(8.dp),
                                contentPadding = PaddingValues(horizontal = 12.dp, vertical = 4.dp)
                            ) {
                                Text(if (isHindi) "Switch to EN" else "हिंदी में बदलें", fontSize = 11.sp)
                            }
                        }
                    }
                }
            }

            // Firm Details Card
            item {
                Surface(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(12.dp),
                    color = Color.White,
                    border = androidx.compose.foundation.BorderStroke(1.dp, IvoryOutline),
                    tonalElevation = 2.dp
                ) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Text(
                            text = "Firm Information",
                            fontSize = 14.sp,
                            fontWeight = FontWeight.Bold,
                            color = DeepMaroon
                        )
                        Spacer(modifier = Modifier.height(8.dp))
                        Text(text = "Firm Name: Shree Navya Lekha Associates", fontSize = 12.sp, fontWeight = FontWeight.SemiBold, color = DeepMaroon)
                        Text(text = "Founder / Lead: Naveen Shrotriya", fontSize = 12.sp, color = Color.DarkGray)
                        Text(text = "Head Office: Jaipur, Rajasthan, India", fontSize = 12.sp, color = Color.DarkGray)
                        Text(text = "Services: GST Returns, Corporate ITR, Tax Audit, TDS/TCS, E-Way Bill", fontSize = 12.sp, color = Color.DarkGray)
                        Text(text = "Support: Mr.NaveenShrotriya@gmail.com • +91 9414056789", fontSize = 12.sp, color = RoyalGoldDark, fontWeight = FontWeight.Medium)
                    }
                }
            }
        }
    }
}
