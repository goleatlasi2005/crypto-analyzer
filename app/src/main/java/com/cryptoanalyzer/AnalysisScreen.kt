
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

private val analysisBackground = Color(0xFF171326)
private val analysisCard = Color(0xFF29213D)
private val analysisPurple = Color(0xFFB68CFF)

@Composable
fun AnalysisScreen(symbol: String) {

    val scope = rememberCoroutineScope()

    var results by remember {
        mutableStateOf<List<ExchangeMultiTimeframeAnalysis>>(emptyList())
    }

    var loading by remember { mutableStateOf(false) }
    var errorMessage by remember { mutableStateOf<String?>(null) }

    fun loadAnalysis() {
        scope.launch {
            loading = true
            errorMessage = null

            try {
                results = MultiTimeframeAnalysisRepository().analyze(symbol)

                if (results.isEmpty()) {
                    errorMessage = "اطلاعات کافی از صرافی‌ها دریافت نشد."
                }
            } catch (e: Exception) {
                errorMessage = e.message ?: "خطا در تحلیل بازار"
            } finally {
                loading = false
            }
        }
    }

    LaunchedEffect(symbol) {
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
                        "تحلیل ترکیبی $symbol",
                        color = Color.White,
                        fontSize = 23.sp,
                        fontWeight = FontWeight.Bold
                    )

                    Spacer(Modifier.height(6.dp))

                    Text(
                        "تحلیل هم‌زمان تایم‌فریم‌های 15m، 1h، 4h و 1d",
                        color = analysisPurple,
                        fontSize = 13.sp
                    )
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
                            "در حال تحلیل چندتایم‌فریمی بازار...",
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

                items(results) { result ->

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
                        shape = RoundedCornerShape(16.dp),
                        colors = CardDefaults.cardColors(
                            containerColor = analysisCard
                        )
                    ) {
                        Column(
                            modifier = Modifier.padding(16.dp),
                            verticalArrangement = Arrangement.spacedBy(10.dp)
                        ) {

                            Text(
                                result.exchange,
                                color = analysisPurple,
                                fontSize = 20.sp,
                                fontWeight = FontWeight.Bold
                            )

                            Text(
                                "سیگنال نهایی: $signalText",
                                color = signalColor,
                                fontSize = 23.sp,
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

                            Text(
                                "وضعیت تایم‌فریم‌ها",
                                color = analysisPurple,
                                fontWeight = FontWeight.Bold
                            )

                            result.timeframeSignals.forEach { frame ->

                                val frameSignal = frame.signal

                                val frameText = when (frameSignal?.type) {
                                    SignalType.BUY -> "خرید"
                                    SignalType.SELL -> "فروش"
                                    SignalType.HOLD -> "نگهداری"
                                    null -> "داده کافی نیست"
                                }

                                val frameColor = when (frameSignal?.type) {
                                    SignalType.BUY -> Color(0xFF4ADE80)
                                    SignalType.SELL -> Color(0xFFF87171)
                                    SignalType.HOLD -> Color(0xFFFBBF24)
                                    null -> Color.Gray
                                }

                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.SpaceBetween
                                ) {
                                    Text(
                                        frame.timeframe,
                                        color = Color.White,
                                        fontWeight = FontWeight.Bold
                                    )

                                    Text(
                                        frameText,
                                        color = frameColor
                                    )
                                }
                            }

                            val reference = result.referenceSignal

                            if (reference != null) {

                                Divider(color = Color.DarkGray)

                                Text(
                                    "سطوح پیشنهادی معامله",
                                    color = analysisPurple,
                                    fontWeight = FontWeight.Bold
                                )

                                Text(
                                    "قیمت ورود: ${reference.entryPrice}",
                                    color = Color.LightGray
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

                                Text(
                                    "این سطوح از سیگنال یک تایم‌فریم مرجع گرفته شده‌اند و تضمین نتیجه نیستند.",
                                    color = Color.Gray,
                                    fontSize = 11.sp
                                )
                            }

                        }
                    }
                }

                item {
                    Text(
                        "توجه: این تحلیل‌ها آزمایشی هستند. امتیاز ترکیبی و درصد توافق، احتمال موفقیت معامله نیستند. قبل از استفاده واقعی باید استراتژی با داده‌های تاریخی بک‌تست و ارزیابی شود.",
                        color = Color.Gray,
                        fontSize = 12.sp
                    )
                }
            }
        }
    }
}
