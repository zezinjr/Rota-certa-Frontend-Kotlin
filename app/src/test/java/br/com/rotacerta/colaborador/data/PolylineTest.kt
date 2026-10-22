package br.com.rotacerta.colaborador.data

import org.junit.Assert.assertEquals
import org.junit.Test

class PolylineTest {
    @Test
    fun decodesTheGoogleReferenceExample() {
        // Exemplo da documentação do Google ("Encoded Polyline Algorithm Format").
        val points = decodePolyline("_p~iF~ps|U_ulLnnqC_mqNvxq`@")
        val expected = listOf(38.5 to -120.2, 40.7 to -120.95, 43.252 to -126.453)
        assertEquals(expected.size, points.size)
        expected.zip(points).forEach { (e, p) ->
            assertEquals(e.first, p.first, 1e-6)
            assertEquals(e.second, p.second, 1e-6)
        }
    }

    @Test
    fun emptyPolylineHasNoPoints() {
        assertEquals(emptyList<Pair<Double, Double>>(), decodePolyline(""))
    }
}
