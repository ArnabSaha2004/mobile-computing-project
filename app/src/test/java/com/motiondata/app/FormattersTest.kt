package com.motiondata.app

import com.motiondata.app.ui.utils.Formatters
import org.junit.Assert.assertEquals
import org.junit.Test

class FormattersTest {

    @Test
    fun testFormatDurationStopwatch() {
        assertEquals("00:00.00", Formatters.formatDurationStopwatch(0L))
        assertEquals("00:05.42", Formatters.formatDurationStopwatch(5420L))
        assertEquals("02:15.08", Formatters.formatDurationStopwatch(135080L))
    }

    @Test
    fun testFormatDurationHuman() {
        assertEquals("0s", Formatters.formatDurationHuman(500L))
        assertEquals("5s", Formatters.formatDurationHuman(5400L))
        assertEquals("2m 15s", Formatters.formatDurationHuman(135000L))
    }

    @Test
    fun testFormatFrequency() {
        assertEquals("50.0 Hz", Formatters.formatFrequency(50.0))
        assertEquals("98.4 Hz", Formatters.formatFrequency(98.42))
    }

    @Test
    fun testFormatNumber() {
        assertEquals("1,420", Formatters.formatNumber(1420L))
        assertEquals("0", Formatters.formatNumber(0L))
    }
}
