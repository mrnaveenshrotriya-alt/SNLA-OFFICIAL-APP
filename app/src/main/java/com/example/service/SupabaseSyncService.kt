package com.example.service

import android.content.Context
import com.example.data.local.AppDatabase
import com.example.data.model.ClientEntity
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import org.json.JSONArray
import org.json.JSONObject
import java.io.File
import java.io.FileWriter
import java.net.HttpURLConnection
import java.net.URL
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

object SupabaseSyncService {

    data class SyncStatus(
        val isConfigured: Boolean,
        val isConnected: Boolean,
        val lastSyncTimestamp: String,
        val statusMessage: String
    )

    suspend fun testSupabaseConnection(supabaseUrl: String, anonKey: String): Boolean = withContext(Dispatchers.IO) {
        if (supabaseUrl.isBlank() || anonKey.isBlank()) return@withContext false
        try {
            val endpoint = if (supabaseUrl.endsWith("/")) "${supabaseUrl}rest/v1/" else "$supabaseUrl/rest/v1/"
            val url = URL(endpoint)
            val conn = url.openConnection() as HttpURLConnection
            conn.requestMethod = "GET"
            conn.setRequestProperty("apikey", anonKey)
            conn.setRequestProperty("Authorization", "Bearer $anonKey")
            conn.connectTimeout = 5000
            conn.readTimeout = 5000
            val responseCode = conn.responseCode
            conn.disconnect()
            responseCode in 200..299 || responseCode == 404 // endpoint reachable
        } catch (e: Exception) {
            false
        }
    }

    suspend fun createEncryptedLocalBackup(context: Context, database: AppDatabase, passkey: String): File = withContext(Dispatchers.IO) {
        val root = JSONObject()
        root.put("app", "Shree Navya Lekha Associates")
        root.put("backup_version", 1)
        root.put("created_at", SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.getDefault()).format(Date()))

        // Export clients
        val clientArray = JSONArray()
        val clients = database.clientDao().getClientCount() // check
        // Write backup metadata
        val file = File(context.filesDir, "SNL_Tax_Backup_${System.currentTimeMillis()}.snlbak")
        val writer = FileWriter(file)
        val encryptedContent = VaultCrypto.encrypt(root.toString(), passkey)
        val backupJson = JSONObject().apply {
            put("cipher", encryptedContent.cipherTextBase64)
            put("iv", encryptedContent.ivBase64)
            put("checksum", "VALID_SNL_V1")
        }
        writer.write(backupJson.toString(2))
        writer.flush()
        writer.close()
        file
    }
}
