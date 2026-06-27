package com.emeth.kernel.capabilities

import com.emeth.kernel.intents.Intent
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertTrue
import org.junit.Test

class CapabilityRegistryTest {
    @Test
    fun classifiesHeadlessFileSearch() {
        val capability = CapabilityRegistry.forIntent(Intent.FIND_FILE)

        assertNotNull(capability)
        assertEquals(ExecutionMode.HEADLESS, capability!!.executionMode)
    }

    @Test
    fun classifiesWhatsAppSendAsConfirmationRequired() {
        val capability = CapabilityRegistry.forIntent(Intent.SEND_WHATSAPP)

        assertNotNull(capability)
        assertEquals(ExecutionMode.CONFIRMATION_REQUIRED, capability!!.executionMode)
    }

    @Test
    fun classifiesUnsupportedWhatsAppChatState() {
        val capability = CapabilityRegistry.forIntent(Intent.MUTE_WHATSAPP_CHAT)

        assertNotNull(capability)
        assertEquals(ExecutionMode.UNSUPPORTED, capability!!.executionMode)
    }

    @Test
    fun summaryNamesExecutionModes() {
        val summary = CapabilityRegistry.summary()

        assertTrue(summary.contains("headless"))
        assertTrue(summary.contains("confirmation-required"))
        assertTrue(summary.contains("unsupported"))
    }
}
