package com.example

import com.example.service.VaultCrypto
import com.example.service.WhatsAppHelper
import com.example.ui.navigation.TaxRoute
import com.example.ui.navigation.TaxRouter
import org.junit.Assert.*
import org.junit.Test

class ExampleUnitTest {
    @Test
    fun testVaultCryptoEncryptionAndDecryption() {
        val originalSecret = "GstPortal@Pass#2024"
        val masterPin = "1234"

        val encrypted = VaultCrypto.encrypt(originalSecret, masterPin)
        assertNotNull(encrypted.cipherTextBase64)
        assertNotNull(encrypted.ivBase64)
        assertNotEquals(originalSecret, encrypted.cipherTextBase64)

        val decrypted = VaultCrypto.decrypt(encrypted.cipherTextBase64, encrypted.ivBase64, masterPin)
        assertEquals(originalSecret, decrypted)

        val wrongPinDecrypted = VaultCrypto.decrypt(encrypted.cipherTextBase64, encrypted.ivBase64, "9999")
        assertEquals("Decryption Failed (Invalid PIN)", wrongPinDecrypted)
    }

    @Test
    fun testWhatsAppMessageBuilder() {
        val msgEn = WhatsAppHelper.buildMessage(
            type = WhatsAppHelper.MessageType.GST_DUE,
            clientName = "Marudhar Textiles",
            returnOrDocType = "GSTR-3B",
            period = "October 2024",
            dueDate = "2024-11-20",
            isHindi = false
        )
        assertTrue(msgEn.contains("Shree Navya Lekha Associates"))
        assertTrue(msgEn.contains("GSTR-3B"))

        val msgHi = WhatsAppHelper.buildMessage(
            type = WhatsAppHelper.MessageType.GST_DUE,
            clientName = "राजेश शर्मा",
            returnOrDocType = "GSTR-3B",
            period = "अक्टूबर 2024",
            dueDate = "20-11-2024",
            isHindi = true
        )
        assertTrue(msgHi.contains("श्री नव्या लेखा एसोसिएट्स"))
    }

    @Test
    fun testCentralizedStateBasedRouter() {
        // Verify all 15 master sections are present
        assertEquals(15, TaxRouter.MASTER_SECTIONS.size)
        assertEquals(1, TaxRouter.MASTER_SECTIONS[0].sectionNumber)
        assertEquals("Dashboard", TaxRouter.MASTER_SECTIONS[0].titleEnglish)
        assertEquals(15, TaxRouter.MASTER_SECTIONS[14].sectionNumber)
        assertEquals("Settings & Firm Logo", TaxRouter.MASTER_SECTIONS[14].titleEnglish)

        val router = TaxRouter()
        assertEquals(TaxRoute.Dashboard, router.currentRoute.value)
        assertFalse(router.canGoBack)

        // Navigate forward
        router.navigateTo(TaxRoute.Clients)
        assertEquals(TaxRoute.Clients, router.currentRoute.value)
        assertTrue(router.canGoBack)

        router.navigateTo(TaxRoute.GstManagement)
        assertEquals(TaxRoute.GstManagement, router.currentRoute.value)

        // Pop back
        val popped = router.popBackStack()
        assertTrue(popped)
        assertEquals(TaxRoute.Clients, router.currentRoute.value)

        val poppedToDashboard = router.popBackStack()
        assertTrue(poppedToDashboard)
        assertEquals(TaxRoute.Dashboard, router.currentRoute.value)
        assertFalse(router.canGoBack)
    }
}
