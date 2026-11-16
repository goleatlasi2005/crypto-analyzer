
package com.cryptoanalyzer

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
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
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.withContext
import okhttp3.OkHttpClient
import okhttp3.Request
import org.json.JSONObject
import java.text.DecimalFormat
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent { CryptoAnalyzerApp() }
    }
}

private data class MarketTicker(
    val price: Double,
    val change: Double,
    val high: Double,
    val low: Double,
    val volume: Double
)

private val client = OkHttpClient()

private suspend fun fetchTicker(symbol: String): MarketTicker =
    withContext(Dispatchers.IO) {
        val request = Request.Builder()
            .url("https://api.binance.com/api/v3/ticker/24hr?symbol=$symbol")
            .build()

        client.newCall(request).execute().use { response ->
            if (!response.isSuccessful) {
                error("خطای Binance: ${response.code}")
            }

            val json = JSONObject(
                response.body?.string() ?: error("پاسخی دریافت نشد")
            )

            MarketTicker(
                json.getString("lastPrice").toDouble(),
                json.getString("priceChangePercent").toDouble(),
                json.getString("highPrice").toDouble(),
                json.getString("lowPrice").toDouble(),
                json.getString("volume").toDouble()
            )
        }
    }

private fun formatNumber(value: Double): String =
    DecimalFormat("#,##0.00").format(value)

@Composable
fun CryptoAnalyzerApp() {
    var selectedCoin by remember { mutableStateOf("BTC") }
    var ticker by remember { mutableStateOf<MarketTicker?>(null) }
    var loading by remember { mutableStateOf(true) }
    var errorMessage by remember { mutableStateOf<String?>(null) }
    var updatedAt by remember { mutableStateOf("") }

    LaunchedEffect(selectedCoin) {
        ticker = null
        loading = true
        errorMessage = null

        while (true) {
            try {
                ticker = fetchTicker("${selectedCoin}USDT")
                errorMessage = null
                updatedAt = SimpleDateFormat(
                    "HH:mm:ss", Locale.getDefault()
                ).format(Date())
            } catch (e: Exception) {
                errorMessage = e.message ?: "خطا در دریافت اطلاعات"
            } finally {
                loading = false
            }

            delay(15_000)
        }
    }

    MaterialTheme {
        Surface(
            modifier = Modifier.fillMaxSize(),
            color = Color(0xFF101827)
        ) {
            LazyColumn(
                modifier = Modifier.padding(16.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                item {
                    Text(
                        "Crypto Analyzer",
                        color = Color.White,
                        fontSize = 26.sp,
                        fontWeight = FontWeight.Bold
                    )
                    Text(
                        "تحلیل هوشمند بازار ارز دیجیتال",
                        color = Color.LightGray
                    )
                }

                item {
                    Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                        listOf("BTC", "ETH").forEach { coin ->
                            Button(
                                onClick = { selectedCoin = coin },
                                colors = ButtonDefaults.buttonColors(
                                    containerColor =
                                        if (selectedCoin == coin)
                                            Color(0xFF2563EB)
                                        else Color(0xFF263449)
                                )
                            ) {
                                Text(coin)
                            }
                        }
                    }
                }

                item {
                    Card(
                        colors = CardDefaults.cardColors(
                            containerColor = Color(0xFF1E293B)
                        ),
                        shape = RoundedCornerShape(16.dp)
                    ) {
                        Column(Modifier.padding(20.dp)) {
                            Text(
                                "$selectedCoin / USDT",
                                color = Color.White,
                                fontSize = 20.sp,
                                fontWeight = FontWeight.Bold
                            )

                            Spacer(Modifier.height(12.dp))

                            val data = ticker

                            if (data != null) {
                                Text(
                                    "$${formatNumber(data.price)}",
                                    color = Color.White,
                                    fontSize = 30.sp,
                                    fontWeight = FontWeight.Bold
                                )

                                Text(
                                    "تغییر ۲۴ ساعته: ${
                                        String.format(
                                            Locale.US,
                                            "%+.2f%%",
                                            data.change
                                        )
                                    }",
                                    color = if (data.change >= 0)
                                        Color(0xFF4ADE80)
                                    else Color(0xFFF87171),
                                    fontSize = 16.sp
                                )

                                Spacer(Modifier.height(12.dp))

                                Text(
                                    "بیشترین قیمت: $${formatNumber(data.high)}",
                                    color = Color.LightGray
                                )
                                Text(
                                    "کمترین قیمت: $${formatNumber(data.low)}",
                                    color = Color.LightGray
                                )
                                Text(
                                    "حجم معاملات: ${formatNumber(data.volume)} $selectedCoin",
                                    color = Color.LightGray
                                )
                                Text(
                                    "آخرین به‌روزرسانی: $updatedAt",
                                    color = Color(0xFF60A5FA),
                                    fontSize = 12.sp
                                )
                            } else if (loading) {
                                Text(
                                    "در حال دریافت قیمت از Binance...",
                                    color = Color.LightGray
                                )
                            }

                            if (errorMessage != null) {
                                Text(
                                    errorMessage!!,
                                    color = Color(0xFFF87171)
                                )
                                Text(
                                    "اتصال اینترنت و دسترسی به Binance را بررسی کنید.",
                                    color = Color.LightGray
                                )
                            }

                            Spacer(Modifier.height(8.dp))

                            Text(
                                "منبع داده: Binance | به‌روزرسانی هر ۱۵ ثانیه",
                                color = Color(0xFF60A5FA),
                                fontSize = 12.sp
                            )
                        }
                    }
                }

                item {
                    Text(
                        "تایم‌فریم‌ها",
                        color = Color.White,
                        fontSize = 18.sp,
                        fontWeight = FontWeight.Bold
                    )
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        listOf("5m", "15m", "1h", "4h", "1d", "1w").forEach {
                            Text(it, color = Color.White, fontSize = 13.sp)
                        }
                    }
                }

                item {
                    Text(
                        "سیگنال تحلیل",
                        color = Color.White,
                        fontSize = 18.sp,
                        fontWeight = FontWeight.Bold
                    )
                    Card(
                        colors = CardDefaults.cardColors(
                            containerColor = Color(0xFF1E293B)
                        )
                    ) {
                        Column(Modifier.padding(18.dp)) {
                            Text(
                                "در انتظار تحلیل",
                                color = Color(0xFFFBBF24),
                                fontSize = 20.sp
                            )
                            Text(
                                "سیگنال‌ها پس از پیاده‌سازی اندیکاتورها نمایش داده می‌شوند.",
                                color = Color.LightGray
                            )
                        }
                    }
                }

                item {
                    Text(
                        "RSI • MACD • EMA • SMA • Bollinger Bands • Ichimoku • Fibonacci • Volume",
                        color = Color.LightGray,
                        fontSize = 14.sp
                    )
                }

                item {
                    Text(
                        "این برنامه فقط تحلیل ارائه می‌دهد و هیچ معامله‌ای انجام نمی‌دهد.",
                        color = Color.Gray,
                        fontSize = 12.sp
                    )
                }
            }
        }
    }
}
