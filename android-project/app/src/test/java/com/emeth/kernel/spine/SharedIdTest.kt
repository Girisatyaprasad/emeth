package com.emeth.kernel.spine

import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class SharedIdTest {
    @Test
    fun stableIdsUnifyTheSameEntityAcrossNodes() {
        val first = SharedId.stableId("contact", "satya")
        val second = SharedId.stableId("contact", "satya")
        assertEquals(first, second)
        assertTrue(first.startsWith("air:contact:"))
    }

    @Test
    fun eventIdsRemainUnique() {
        assertNotEquals(
            SharedId.newId("behavior"),
            SharedId.newId("behavior")
        )
    }
}
