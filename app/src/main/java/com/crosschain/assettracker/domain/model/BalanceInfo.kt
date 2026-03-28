package com.crosschain.assettracker.domain.model


data class BalanceInfo(
    val amount: String,
    val tokenSymbol: String,
    val currentInterestRate: String,
    val baseInterestRate: String,
    val chain: Chain
)
