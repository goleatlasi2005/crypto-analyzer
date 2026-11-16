
package com.cryptoanalyzer

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import kotlinx.coroutines.launch

private val analysisBackground = Color(0xFF171326)
private val analysisCard = Color(0xFF29213D)
private val analysisPurple = Color(0xFFB68CFF)

@Composable
fun AnalysisScreen(
    symbol: String,
    repository: AnalysisRepository = remember { AnalysisRepository() }
) {
    val scope = rememberCoroutineScope()

    var timeframe by remember { mutableStateOf("1h") }
    var results by remember { mutableStateOf<List<ExchangeAnalysis>>(emptyList()) }
    var loading by remember { mutableStateOf(false) }
    var errorMessage by remember { mutableStateOf<String?>(null) }

    fun loadAnalysis() {
        scope.launch {
            loading = true
            errorMessage = null

            try {
                results = repository.analyze(symbol, timeframe)

                if (results.isEmpty()) {
                    errorMessage = "اطلاعات کندل از هیچ‌کدام از صرافی‌ها دریافت نشد."
                }
            } catch (e: Exception) {
                errorMessage = e.message ?: "خطا در تحلیل بازار"
            } finally {
                loading = false
            }
        }
    }

    LaunchedEffect(symbol, timeframe) {
        loadAnalysis()
    }

    MaterialTheme {
        Surface(
            modifier = Modifier.fillMaxWidth(),
            color = analysisBackground
        ) {
            LazyColumn(
                modifier = Modifier.padding(16.dp),
                verticalArrangement = Arrangement.spacedBy(14.dp)
            ) {
                item {
                    Text(
                        "تحلیل تکنیکال $symbol",
                        color = Color.White,
                        fontSize = 23.sp,
                        fontWeight = FontWeight.Bold
                    )
                }

                item {
                    Text(
                        "انتخاب تایم‌فریم",
                        color = analysisPurple,
                        fontWeight = FontWeight.Bold
                    )

                    Row(
                        horizontalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        listOf("5m", "15m", "1h").forEach { frame ->
                            FilterChip(
                                selected = timeframe == frame,
                                onClick = { timeframe = frame },
                                label = { Text(frame) }
                            )
                        }
                    }

                    Row(
                        horizontalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        listOf("4h", "1d", "1w").forEach { frame ->
                            FilterChip(
                                selected = timeframe == frame,
                                onClick = { timeframe = frame },
                                label = { Text(frame) }
                            )
                        }
                    }
                }

                item {
                    Button(
                        onClick = { loadAnalysis() },
                        enabled = !loading,
                        colors = ButtonDefaults.buttonColors(
                            containerColor = Color(0xFF8652D8)
                        )
                    ) {
                        Text("به‌روزرسانی تحلیل")
                    }
                }

                if (loading) {
                    item {
                        CircularProgressIndicator(color = analysisPurple)
                        Text(
                            "در حال دریافت کندل‌ها و تحلیل بازار...",
                            color = Color.LightGray
                        )
                    }
                }

                if (errorMessage != null) {
                    item {
                        Text(
                            errorMessage!!,
                            color = Color(0xFFF87171)
                        )
                    }
                }

                items(results.size) { index ->
                    val result = results[index]
                    val signal = result.signal

                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(16.dp),
                        colors = CardDefaults.cardColors(
                            containerColor = analysisCard
                        )
                    ) {
                        Column(
                            modifier = Modifier.padding(16.dp),
                            verticalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            Text(
                                result.exchange,
                                color = analysisPurple,
                                fontSize = 19.sp,
                                fontWeight = FontWeight.Bold
                            )

                            Text(
                                "تعداد کندل‌ها: ${result.candles.size}",
                                color = Color.LightGray
                            )

                            if (signal == null) {
                                Text(
                                    "داده کافی برای تحلیل وجود ندارد.",
                                    color = Color(0xFFFBBF24)
                                )
                            } else {
                                val signalText = when (signal.type) {
                                    SignalType.BUY -> "خرید"
                                    SignalType.SELL -> "فروش"
                                    SignalType.HOLD -> "نگهداری"
                                }

                                val signalColor = when (signal.type) {
                                    SignalType.BUY -> Color(0xFF4ADE80)
                                    SignalType.SELL -> Color(0xFFF87171)
                                    SignalType.HOLD -> Color(0xFFFBBF24)
                                }

                                Text(
                                    "سیگنال: $signalText",
                                    color = signalColor,
                                    fontSize = 22.sp,
                                    fontWeight = FontWeight.Bold
                                )

                                Text(
                                    "امتیاز تحلیلی: ${signal.score} از ۱۰۰",
                                    color = Color.White
                                )

                                Text(
                                    "قیمت ورود: ${signal.entryPrice}",
                                    color = Color.LightGray
                                )

                                Text(
                                    "حد ضرر: ${signal.stopLoss}",
                                    color = Color(0xFFF87171)
                                )

                                Text(
                                    "هدف اول: ${signal.takeProfit1}",
                                    color = Color(0xFF4ADE80)
                                )

                                Text(
                                    "هدف دوم: ${signal.takeProfit2}",
                                    color = Color(0xFF4ADE80)
                                )

                                Text(
                                    "دلایل تحلیل:",
                                    color = analysisPurple,
                                    fontWeight = FontWeight.Bold
                                )

                                signal.reasons.forEach { reason ->
                                    Text(
                                        "• $reason",
                                        color = Color.LightGray
                                    )
                                }
                            }
                        }
                    }
                }

                item {
                    Text(
                        "این سیگنال‌ها آزمایشی هستند. امتیاز تحلیلی احتمال موفقیت نیست و قبل از استفاده واقعی به بک‌تست نیاز دارد.",
                        color = Color.Gray,
                        fontSize = 12.sp
                    )
                }
            }
        }
    }
}
