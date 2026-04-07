package com.crosschain.assettracker.ui.mvi.main

import com.crosschain.assettracker.domain.model.Chain
import java.math.BigInteger

sealed class MainIntent {
    data class LoadBalance(val chain: Chain) : MainIntent()
    data class DeleteTransfer(val requestId: String) : MainIntent()
    data class TransferTokens(val amount: BigInteger, val sourceChain: Chain, val destinationChain: Chain) : MainIntent()

    data object LoadAccountFromWallet : MainIntent()
    data object LoadAccountFromCache : MainIntent()
}
