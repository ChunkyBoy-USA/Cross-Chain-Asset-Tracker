package com.crosschain.assettracker.domain.model

import java.math.BigDecimal

data class BalanceInfo(
    val amount: BigDecimal,
    val tokenSymbol: String,
    val rebaseRate: BigDecimal,
    val chain: Chain
)
