package com.cryptoanalyzer

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            CryptoAnalyzerApp()
        }
    }
}

@Composable
fun CryptoAnalyzerApp() {
    var selectedTab by remember { mutableStateOf("BTC") }

    MaterialTheme {
        Surface(
            modifier = Modifier.fillMaxSize(),
            color = Color(0xFF101827)
        ) {
            LazyColumn(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(16.dp),
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
                        color = Color.LightGray,
                        fontSize = 14.sp
                    )
                }

                item {
                    Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                        listOf("BTC", "ETH").forEach { coin ->
                            Button(
                                onClick = { selectedTab = coin },
                                colors = ButtonDefaults.buttonColors(
                                    containerColor = if (selectedTab == coin)
                                        Color(0xFF2563EB) else Color(0xFF263449)
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
                                "$selectedTab / USDT",
                                color = Color.White,
                                fontSize = 20.sp,
                                fontWeight = FontWeight.Bold
                            )
                            Spacer(Modifier.height(12.dp))
                            Text(
                                "در انتظار دریافت اطلاعات بازار...",
                                color = Color.LightGray
                            )
                            Spacer(Modifier.height(8.dp))
                            Text(
                                "منبع داده: Binance",
                                color = Color(0xFF60A5FA)
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
                                fontSize = 20.sp,
                                fontWeight = FontWeight.Bold
                            )
                            Text(
                                "پس از دریافت داده‌های واقعی، سیگنال خرید، فروش یا نگهداری نمایش داده می‌شود.",
                                color = Color.LightGray
                            )
                        }
                    }
                }

                item {
                    Text(
                        "اندیکاتورها",
                        color = Color.White,
                        fontSize = 18.sp,
                        fontWeight = FontWeight.Bold
                    )
                    Text(
                        "RSI  •  MACD  •  EMA  •  SMA  •  Bollinger Bands  •  Ichimoku  •  Fibonacci  •  Volume",
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
