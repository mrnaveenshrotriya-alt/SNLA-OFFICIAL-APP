package com.example.service

import java.security.SecureRandom
import javax.crypto.Cipher
import javax.crypto.SecretKeyFactory
import javax.crypto.spec.GCMParameterSpec
import javax.crypto.spec.PBEKeySpec
import javax.crypto.spec.SecretKeySpec

object VaultCrypto {
    private const val ALGORITHM = "AES/GCM/NoPadding"
    private const val KEY_ALGORITHM = "AES"
    private const val GCM_TAG_LENGTH = 128
    private const val ITERATIONS = 10000
    private const val KEY_LENGTH = 256
    private val SALT = "ShreeNavyaLekhaTaxAssociatesSalt".toByteArray()

    private fun deriveKey(masterPin: String): SecretKeySpec {
        val factory = SecretKeyFactory.getInstance("PBKDF2WithHmacSHA256")
        val spec = PBEKeySpec(masterPin.toCharArray(), SALT, ITERATIONS, KEY_LENGTH)
        val secretKey = factory.generateSecret(spec)
        return SecretKeySpec(secretKey.encoded, KEY_ALGORITHM)
    }

    data class EncryptedResult(val cipherTextBase64: String, val ivBase64: String)

    fun encrypt(plainText: String, masterPin: String): EncryptedResult {
        val key = deriveKey(masterPin)
        val cipher = Cipher.getInstance(ALGORITHM)
        val iv = ByteArray(12)
        SecureRandom().nextBytes(iv)
        val gcmSpec = GCMParameterSpec(GCM_TAG_LENGTH, iv)
        cipher.init(Cipher.ENCRYPT_MODE, key, gcmSpec)
        val cipherText = cipher.doFinal(plainText.toByteArray(Charsets.UTF_8))
        return EncryptedResult(
            cipherTextBase64 = encodeBase64(cipherText),
            ivBase64 = encodeBase64(iv)
        )
    }

    fun decrypt(cipherTextBase64: String, ivBase64: String, masterPin: String): String {
        return try {
            val key = deriveKey(masterPin)
            val cipher = Cipher.getInstance(ALGORITHM)
            val iv = decodeBase64(ivBase64)
            val cipherText = decodeBase64(cipherTextBase64)
            val gcmSpec = GCMParameterSpec(GCM_TAG_LENGTH, iv)
            cipher.init(Cipher.DECRYPT_MODE, key, gcmSpec)
            val plainBytes = cipher.doFinal(cipherText)
            String(plainBytes, Charsets.UTF_8)
        } catch (e: Exception) {
            "Decryption Failed (Invalid PIN)"
        }
    }

    // Pure Kotlin Base64 for guaranteed compatibility across JVM unit tests and all Android APIs
    private const val BASE64_CHARS = "ABCDEFGHIJKLMNOPQRSTUVWXYZabcdefghijklmnopqrstuvwxyz0123456789+/"

    private fun encodeBase64(data: ByteArray): String {
        val out = StringBuilder()
        var i = 0
        while (i < data.size) {
            val b0 = data[i].toInt() and 0xFF
            val b1 = if (i + 1 < data.size) data[i + 1].toInt() and 0xFF else 0
            val b2 = if (i + 2 < data.size) data[i + 2].toInt() and 0xFF else 0

            val triple = (b0 shl 16) or (b1 shl 8) or b2
            out.append(BASE64_CHARS[(triple shr 18) and 0x3F])
            out.append(BASE64_CHARS[(triple shr 12) and 0x3F])
            out.append(if (i + 1 < data.size) BASE64_CHARS[(triple shr 6) and 0x3F] else '=')
            out.append(if (i + 2 < data.size) BASE64_CHARS[triple and 0x3F] else '=')
            i += 3
        }
        return out.toString()
    }

    private fun decodeBase64(s: String): ByteArray {
        val clean = s.filter { it in BASE64_CHARS }
        val out = mutableListOf<Byte>()
        var i = 0
        while (i < clean.length) {
            val c0 = BASE64_CHARS.indexOf(clean[i])
            val c1 = if (i + 1 < clean.length) BASE64_CHARS.indexOf(clean[i + 1]) else 0
            val c2 = if (i + 2 < clean.length) BASE64_CHARS.indexOf(clean[i + 2]) else 0
            val c3 = if (i + 3 < clean.length) BASE64_CHARS.indexOf(clean[i + 3]) else 0

            val triple = (c0 shl 18) or (c1 shl 12) or (c2 shl 6) or c3
            out.add(((triple shr 16) and 0xFF).toByte())
            if (i + 2 < clean.length) out.add(((triple shr 8) and 0xFF).toByte())
            if (i + 3 < clean.length) out.add((triple and 0xFF).toByte())
            i += 4
        }
        return out.toByteArray()
    }
}
