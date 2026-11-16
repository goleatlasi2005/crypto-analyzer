package com.cryptoanalyzer

import kotlinx.coroutines.async
import kotlinx.coroutines.coroutineScope

data class TimeframeSignal(
    val timeframe: String,
    val signal: TradingSignal?
)

data class ExchangeMultiTimeframeAnalysis(
    val exchange: String,
    val symbol: String,
    val timeframeSignals: List<TimeframeSignal>,
    val finalType: SignalType,
    val averageScore: Int,
    val agreementPercent: Int,
    val referenceSignal: TradingSignal?
)

class MultiTimeframeAnalysisRepository(
    private val analysisRepository: AnalysisRepository = AnalysisRepository()
) {
    private val timeframes = listOf("15m", "1h", "4h", "1d")

    suspend fun analyze(
        symbol: String
    ): List<ExchangeMultiTimeframeAnalysis> = coroutineScope {

        val tasks = timeframes.map { timeframe ->
            async {
                timeframe to analysisRepository.analyze(symbol, timeframe)
            }
        }

        val resultsByTimeframe = tasks.map { it.await() }

        val exchanges = resultsByTimeframe
            .flatMap { (_, results) -> results.map { it.exchange } }
            .distinct()

        exchanges.mapNotNull { exchange ->

            val frameSignals = resultsByTimeframe.map { (timeframe, results) ->
                val result = results.firstOrNull { it.exchange == exchange }
                TimeframeSignal(timeframe, result?.signal)
            }

            val validSignals = frameSignals.mapNotNull { it.signal }

            if (validSignals.isEmpty()) {
                return@mapNotNull null
            }

            val averageRawScore = validSignals.map { it.score }.average()

            val normalizedScore = (averageRawScore / 90.0 * 100.0)
                .toInt()
                .coerceIn(-100, 100)

            val buyCount = validSignals.count {
                it.type == SignalType.BUY
            }

            val sellCount = validSignals.count {
                it.type == SignalType.SELL
            }

            val directionalCount = buyCount + sellCount

            val finalType = when {
                normalizedScore >= 45 -> SignalType.BUY
                normalizedScore <= -45 -> SignalType.SELL
                else -> SignalType.HOLD
            }

            val agreementPercent = if (directionalCount == 0) {
                0
            } else {
                (maxOf(buyCount, sellCount) * 100.0 / directionalCount)
                    .toInt()
            }

            val referenceSignal = when (finalType) {
                SignalType.BUY -> frameSignals
                    .firstOrNull {
                        it.timeframe == "1h" &&
                            it.signal?.type == SignalType.BUY
                    }?.signal
                    ?: frameSignals.firstOrNull {
                        it.signal?.type == SignalType.BUY
                    }?.signal

                SignalType.SELL -> frameSignals
                    .firstOrNull {
                        it.timeframe == "1h" &&
                            it.signal?.type == SignalType.SELL
                    }?.signal
                    ?: frameSignals.firstOrNull {
                        it.signal?.type == SignalType.SELL
                    }?.signal

                SignalType.HOLD -> null
            }

            ExchangeMultiTimeframeAnalysis(
                exchange = exchange,
                symbol = symbol,
                timeframeSignals = frameSignals,
                finalType = finalType,
                averageScore = normalizedScore,
                agreementPercent = agreementPercent,
                referenceSignal = referenceSignal
            )
        }
    }
}
