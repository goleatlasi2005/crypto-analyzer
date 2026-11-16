
package com.cryptoanalyzer

enum class SignalType {
    BUY,
    SELL,
    HOLD
}

data class TradingSignal(
    val type: SignalType,
    val score: Int,
    val entryPrice: Double,
    val stopLoss: Double,
    val takeProfit1: Double,
    val takeProfit2: Double,
    val reasons: List<String>
)

object SignalEngine {

    fun analyze(candles: List<Candle>): TradingSignal? {

        if (candles.size < 60) return null

        val closes = candles.map { it.close }

        val rsi = TechnicalIndicators.rsi(closes)
        val ema20 = TechnicalIndicators.ema(closes, 20)
        val ema50 = TechnicalIndicators.ema(closes, 50)
        val macd = TechnicalIndicators.macd(closes)
        val atr = TechnicalIndicators.atr(candles)

        val last = candles.lastIndex

        val currentPrice = closes[last]
        val currentRsi = rsi[last] ?: return null
        val shortEma = ema20[last] ?: return null
        val longEma = ema50[last] ?: return null
        val currentMacd = macd[last] ?: return null
        val currentAtr = atr[last] ?: return null

        if (currentAtr <= 0.0) return null

        var score = 0
        val reasons = mutableListOf<String>()

        if (currentRsi in 50.0..70.0) {
            score += 20
            reasons.add("RSI نشان‌دهنده مومنتوم صعودی است")
        } else if (currentRsi in 30.0..50.0) {
            score -= 20
            reasons.add("RSI نشان‌دهنده مومنتوم نزولی است")
        }

        if (shortEma > longEma) {
            score += 20
            reasons.add("EMA20 بالاتر از EMA50 است")
        } else {
            score -= 20
            reasons.add("EMA20 پایین‌تر از EMA50 است")
        }

        if (currentPrice > shortEma) {
            score += 15
            reasons.add("قیمت بالاتر از EMA20 است")
        } else {
            score -= 15
            reasons.add("قیمت پایین‌تر از EMA20 است")
        }

        if (
            currentMacd.macd > currentMacd.signal
        ) {
            score += 25
            reasons.add("MACD سیگنال صعودی دارد")
        } else {
            score -= 25
            reasons.add("MACD سیگنال نزولی دارد")
        }

        if (currentMacd.histogram > 0) {
            score += 10
            reasons.add("هیستوگرام MACD مثبت است")
        } else {
            score -= 10
            reasons.add("هیستوگرام MACD منفی است")
        }

        val type = when {
            score >= 40 -> SignalType.BUY
            score <= -40 -> SignalType.SELL
            else -> SignalType.HOLD
        }

        val stopLoss = when (type) {
            SignalType.BUY -> currentPrice - currentAtr * 1.5
            SignalType.SELL -> currentPrice + currentAtr * 1.5
            SignalType.HOLD -> currentPrice - currentAtr * 1.5
        }

        val takeProfit1 = when (type) {
            SignalType.BUY -> currentPrice + currentAtr * 2.0
            SignalType.SELL -> currentPrice - currentAtr * 2.0
            SignalType.HOLD -> currentPrice + currentAtr * 2.0
        }

        val takeProfit2 = when (type) {
            SignalType.BUY -> currentPrice + currentAtr * 3.0
            SignalType.SELL -> currentPrice - currentAtr * 3.0
            SignalType.HOLD -> currentPrice + currentAtr * 3.0
        }

        return TradingSignal(
            type = type,
            score = score,
            entryPrice = currentPrice,
            stopLoss = stopLoss,
            takeProfit1 = takeProfit1,
            takeProfit2 = takeProfit2,
            reasons = reasons
        )
    }
}
