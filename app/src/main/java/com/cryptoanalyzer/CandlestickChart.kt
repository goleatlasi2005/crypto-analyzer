
package com.cryptoanalyzer

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.unit.dp

@Composable
fun CandlestickChart(
    candles: List<Candle>,
    modifier: Modifier = Modifier
) {
    val visibleCandles = candles.takeLast(60)

    if (visibleCandles.isEmpty()) return

    val highest = visibleCandles.maxOf { it.high }
    val lowest = visibleCandles.minOf { it.low }
    val range = (highest - lowest).takeIf { it > 0.0 } ?: 1.0

    Card(
        modifier = modifier.fillMaxWidth(),
        shape = RoundedCornerShape(14.dp),
        colors = CardDefaults.cardColors(
            containerColor = Color(0xFF29213D)
        )
    ) {
        Column(
            modifier = Modifier.padding(12.dp)
        ) {
            Canvas(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(250.dp)
            ) {
                val chartWidth = size.width
                val chartHeight = size.height
                val spacing = chartWidth / visibleCandles.size
                val bodyWidth = spacing * 0.62f

                fun priceY(price: Double): Float {
                    return chartHeight -
                        ((price - lowest) / range * chartHeight).toFloat()
                }

                for (i in 0..4) {
                    val y = chartHeight * i / 4f

                    drawLine(
                        color = Color(0xFF514765),
                        start = androidx.compose.ui.geometry.Offset(0f, y),
                        end = androidx.compose.ui.geometry.Offset(chartWidth, y),
                        strokeWidth = 1.dp.toPx()
                    )
                }

                visibleCandles.forEachIndexed { index, candle ->
                    val centerX = spacing * (index + 0.5f)

                    val highY = priceY(candle.high)
                    val lowY = priceY(candle.low)
                    val openY = priceY(candle.open)
                    val closeY = priceY(candle.close)

                    val candleColor = if (candle.close >= candle.open) {
                        Color(0xFF4ADE80)
                    } else {
                        Color(0xFFF87171)
                    }

                    drawLine(
                        color = candleColor,
                        start = androidx.compose.ui.geometry.Offset(centerX, highY),
                        end = androidx.compose.ui.geometry.Offset(centerX, lowY),
                        strokeWidth = 1.5.dp.toPx(),
                        cap = StrokeCap.Round
                    )

                    val bodyTop = minOf(openY, closeY)
                    val bodyHeight = maxOf(
                        kotlin.math.abs(closeY - openY),
                        2.dp.toPx()
                    )

                    drawRect(
                        color = candleColor,
                        topLeft = androidx.compose.ui.geometry.Offset(
                            centerX - bodyWidth / 2f,
                            bodyTop
                        ),
                        size = androidx.compose.ui.geometry.Size(
                            bodyWidth,
                            bodyHeight
                        )
                    )
                }
            }
        }
    }
}
