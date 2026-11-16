
package com.cryptoanalyzer

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import kotlinx.coroutines.launch
import kotlinx.coroutines.async
import kotlinx.coroutines.coroutineScope
private val analysisBackground = Color(0xFF171326)
private val analysisCard = Color(0xFF29213D)
private val analysisPurple = Color(0xFFB68CFF)

@Composable
fun AnalysisScreen(symbol: String) {

    val scope = rememberCoroutineScope()
    val candleRepository = remember { CandleRepository() }

    var timeframe by remember { mutableStateOf("1h") }

    var chartResults by remember {
        mutableStateOf<List<Pair<String, List<Candle>>>(emptyList())
    }

    var multiResults by remember {
        mutableStateOf<List<Pair<String, List<Candle>>>>(emptyList())
    }

    var loading by remember { mutableStateOf(false) }
    var errorMessage by remember { mutableStateOf<String?>(null) }

    fun loadAnalysis() {
        scope.launch {
            loading = true
            errorMessage = null

            try {
                val chartData = kotlinx.coroutines.coroutineScope {
                    val binance = async {
                        runCatching {
                            candleRepository.getBinanceCandles(
                                symbol, timeframe, 100
                            ).candles
                        }.getOrNull()
                    }

                    val nobitex = async {
                        runCatching {
                            candleRepository.getNobitexCandles(
                                symbol, timeframe, 100
                            ).candles
                        }.getOrNull()
                    }

                    listOfNotNull(
                        binance.await()?.let { "Binance" to it },
                        nobitex.await()?.let { "Nobitex" to it }
                    )
                }

                chartResults = chartData

                if (chartData.isEmpty()) {
                    errorMessage = "اطلاعات نمودار از صرافی‌ها دریافت نشد."
                }

                multiResults =
                    MultiTimeframeAnalysisRepository().analyze(symbol)

            } catch (e: Exception) {
                errorMessage = e.message ?: "خطا در دریافت اطلاعات بازار"
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
                        "انتخاب تایم‌فریم نمودار",
                        color = analysisPurple,
                        fontWeight = FontWeight.Bold
                    )

                    Column(
                        verticalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        listOf(
                            listOf("5m", "15m", "1h"),
                            listOf("4h", "1d", "1w")
                        ).forEach { rowFrames ->
                            Row(
                                horizontalArrangement =
                                    Arrangement.spacedBy(6.dp)
                            ) {
                                rowFrames.forEach { frame ->
                                    FilterChip(
                                        selected = timeframe == frame,
                                        onClick = { timeframe = frame },
                                        label = { Text(frame) }
                                    )
                                }
                            }
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
                        Text("به‌روزرسانی نمودار و تحلیل")
                    }
                }

                if (loading) {
                    item {
                        CircularProgressIndicator(
                            color = analysisPurple
                        )

                        Text(
                            "در حال دریافت اطلاعات بازار...",
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

                items(chartResults) { chart ->

                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(16.dp),
                        colors = CardDefaults.cardColors(
                            containerColor = analysisCard
                        )
                    ) {
                        Column(
                            modifier = Modifier.padding(12.dp),
                            verticalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            Text(
                                "${chart.first} - نمودار $timeframe",
                                color = analysisPurple,
                                fontSize = 18.sp,
                                fontWeight = FontWeight.Bold
                            )

                            Text(
                                "تعداد کندل: ${chart.second.size}",
                                color = Color.LightGray
                            )

                            CandlestickChart(
                                candles = chart.second
                            )

                            chart.second.lastOrNull()?.let { last ->
                                Text(
                                    "آخرین قیمت: ${last.close}",
                                    color = Color.White
                                )
                            }
                        }
                    }
                }

                item {
                    Text(
                        "تحلیل ترکیبی چندتایم‌فریمی",
                        color = Color.White,
                        fontSize = 21.sp,
                        fontWeight = FontWeight.Bold
                    )
                }

                items(multiResults) { result ->

                    val signalColor = when (result.finalType) {
                        SignalType.BUY -> Color(0xFF4ADE80)
                        SignalType.SELL -> Color(0xFFF87171)
                        SignalType.HOLD -> Color(0xFFFBBF24)
                    }

                    val signalText = when (result.finalType) {
                        SignalType.BUY -> "خرید"
                        SignalType.SELL -> "فروش"
                        SignalType.HOLD -> "نگهداری"
                    }

                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        colors = CardDefaults.cardColors(
                            containerColor = analysisCard
                        )
                    ) {
                        Column(
                            modifier = Modifier.padding(16.dp),
                            verticalArrangement = Arrangement.spacedBy(9.dp)
                        ) {
                            Text(
                                result.exchange,
                                color = analysisPurple,
                                fontSize = 19.sp,
                                fontWeight = FontWeight.Bold
                            )

                            Text(
                                "سیگنال نهایی: $signalText",
                                color = signalColor,
                                fontSize = 22.sp,
                                fontWeight = FontWeight.Bold
                            )

                            Text(
                                "امتیاز ترکیبی: ${result.averageScore} از ۱۰۰",
                                color = Color.White
                            )

                            Text(
                                "توافق تایم‌فریم‌ها: ${result.agreementPercent}٪",
                                color = Color.White
                            )

                            Divider(color = Color.DarkGray)

                            result.timeframeSignals.forEach { frame ->
                                val frameText = when (frame.signal?.type) {
                                    SignalType.BUY -> "خرید"
                                    SignalType.SELL -> "فروش"
                                    SignalType.HOLD -> "نگهداری"
                                    null -> "داده کافی نیست"
                                }

                                Text(
                                    "${frame.timeframe}: $frameText",
                                    color = Color.LightGray
                                )
                            }

                            val reference = result.referenceSignal

                            if (reference != null) {
                                Divider(color = Color.DarkGray)

                                Text(
                                    "قیمت ورود: ${reference.entryPrice}",
                                    color = Color.White
                                )

                                Text(
                                    "حد ضرر: ${reference.stopLoss}",
                                    color = Color(0xFFF87171)
                                )

                                Text(
                                    "هدف اول: ${reference.takeProfit1}",
                                    color = Color(0xFF4ADE80)
                                )

                                Text(
                                    "هدف دوم: ${reference.takeProfit2}",
                                    color = Color(0xFF4ADE80)
                                )
                            }
                        }
                    }
                }

                item {
                    Text(
                        "نمودارها از داده‌های دریافتی صرافی‌ها رسم می‌شوند. تحلیل‌ها آزمایشی‌اند و امتیاز یا درصد توافق، احتمال موفقیت معامله نیست.",
                        color = Color.Gray,
                        fontSize = 12.sp
                    )
                }
            }
        }
    }
}
