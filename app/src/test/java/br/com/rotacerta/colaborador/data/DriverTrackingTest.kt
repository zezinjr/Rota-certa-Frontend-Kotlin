package br.com.rotacerta.colaborador.data

import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class DriverTrackingTest {
    @Test
    fun firstFixIsAlwaysSent() = assertTrue(shouldSend(lastSentAtMillis = null, nowMillis = 1_000))

    @Test
    fun fixesCloserThanTheIntervalAreSkipped() = assertFalse(shouldSend(lastSentAtMillis = 10_000, nowMillis = 12_000))

    @Test
    fun fixAfterTheIntervalIsSent() {
        // Tolerância de 0,5 s: o GPS às vezes entrega a leitura um pouco antes dos 5 s.
        assertTrue(shouldSend(lastSentAtMillis = 10_000, nowMillis = 14_600))
        assertTrue(shouldSend(lastSentAtMillis = 10_000, nowMillis = 15_000))
    }
}
