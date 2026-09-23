package br.com.rotacerta.colaborador.ui.trip

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.PathEffect
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.StrokeJoin
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.drawText
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.rememberTextMeasurer
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import br.com.rotacerta.colaborador.data.model.Trip
import br.com.rotacerta.colaborador.ui.theme.BrandAmber
import br.com.rotacerta.colaborador.ui.theme.BrandNavy
import br.com.rotacerta.colaborador.ui.theme.SuccessGreen
import kotlin.math.cos
import kotlin.math.max

/**
 * Mapa esquemático (sem SDK de mapas / sem chave de API): projeta as coordenadas
 * das paradas num Canvas. Para navegação real, o botão "Abrir no mapa" chama o app nativo.
 */
@Composable
fun RouteMap(trip: Trip, modifier: Modifier = Modifier) {
    val textMeasurer = rememberTextMeasurer()
    val grid = MaterialTheme.colorScheme.outlineVariant
    val land = MaterialTheme.colorScheme.surfaceVariant
    val onPrimary = Color.White

    val points = buildList {
        trip.stops.forEach { add(it.latitude to it.longitude) }
        trip.destination?.let { add(it.latitude to it.longitude) }
        trip.lastPosition?.let { add(it.latitude to it.longitude) }
    }

    Canvas(modifier.fillMaxWidth().height(220.dp)) {
        drawRect(land)
        val step = 28.dp.toPx()
        var x = 0f
        while (x < size.width) { drawLine(grid, Offset(x, 0f), Offset(x, size.height), 1f); x += step }
        var y = 0f
        while (y < size.height) { drawLine(grid, Offset(0f, y), Offset(size.width, y), 1f); y += step }
        if (points.isEmpty()) return@Canvas

        val project = projector(points, size, padding = 32.dp.toPx())

        // Trajeto: paradas em ordem → empresa
        val path = Path()
        val route = trip.stops.map { it.latitude to it.longitude } +
            listOfNotNull(trip.destination?.let { it.latitude to it.longitude })
        route.forEachIndexed { i, p ->
            val o = project(p)
            if (i == 0) path.moveTo(o.x, o.y) else path.lineTo(o.x, o.y)
        }
        drawPath(path, BrandNavy.copy(alpha = 0.25f), style = Stroke(12.dp.toPx(), cap = StrokeCap.Round, join = StrokeJoin.Round))
        drawPath(
            path,
            BrandNavy,
            style = Stroke(4.dp.toPx(), cap = StrokeCap.Round, join = StrokeJoin.Round, pathEffect = PathEffect.cornerPathEffect(12f)),
        )

        trip.stops.forEach { stop ->
            val o = project(stop.latitude to stop.longitude)
            val r = if (stop.isMyStop) 14.dp.toPx() else 11.dp.toPx()
            drawCircle(if (stop.isMyStop) BrandAmber else Color.White, r, o)
            drawCircle(BrandNavy, r, o, style = Stroke(3.dp.toPx()))
            val label = textMeasurer.measure(
                "${stop.sequence + 1}",
                TextStyle(fontSize = 11.sp, fontWeight = FontWeight.Bold, color = BrandNavy),
            )
            drawText(label, topLeft = Offset(o.x - label.size.width / 2f, o.y - label.size.height / 2f))
        }

        trip.destination?.let {
            val o = project(it.latitude to it.longitude)
            val s = 22.dp.toPx()
            drawRoundRect(BrandNavy, Offset(o.x - s / 2, o.y - s / 2), Size(s, s), androidx.compose.ui.geometry.CornerRadius(6f))
            val label = textMeasurer.measure("E", TextStyle(fontSize = 12.sp, fontWeight = FontWeight.Bold, color = onPrimary))
            drawText(label, topLeft = Offset(o.x - label.size.width / 2f, o.y - label.size.height / 2f))
        }

        trip.lastPosition?.let { drawVehicle(project(it.latitude to it.longitude)) }
    }
}

private fun DrawScope.drawVehicle(center: Offset) {
    drawCircle(SuccessGreen.copy(alpha = 0.25f), 20.dp.toPx(), center)
    drawCircle(Color.White, 10.dp.toPx(), center)
    drawCircle(SuccessGreen, 7.dp.toPx(), center)
}

private fun projector(points: List<Pair<Double, Double>>, size: Size, padding: Float): (Pair<Double, Double>) -> Offset {
    val minLat = points.minOf { it.first }
    val maxLat = points.maxOf { it.first }
    val minLng = points.minOf { it.second }
    val maxLng = points.maxOf { it.second }
    val lngScale = cos(Math.toRadians((minLat + maxLat) / 2))
    val spanX = max((maxLng - minLng) * lngScale, 1e-4)
    val spanY = max(maxLat - minLat, 1e-4)
    val w = size.width - padding * 2
    val h = size.height - padding * 2
    val scale = minOf(w / spanX, h / spanY)
    val offX = padding + (w - spanX * scale) / 2
    val offY = padding + (h - spanY * scale) / 2
    return { (lat, lng) ->
        Offset(
            (offX + (lng - minLng) * lngScale * scale).toFloat(),
            (offY + (maxLat - lat) * scale).toFloat(),
        )
    }
}
