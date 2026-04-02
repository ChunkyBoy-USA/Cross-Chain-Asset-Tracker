package com.crosschain.assettracker.domain

import com.crosschain.assettracker.domain.model.CcipTransfer
import com.crosschain.assettracker.domain.model.Chain
import kotlinx.coroutines.flow.Flow
import java.math.BigInteger

interface CcipRepository {
    fun trackTransfer() : Flow<CcipTransfer>

    fun sendRebaseToken(
        accountAddress: String,
        sourceChain: Chain,
        destinationChain: Chain,
        amountToSend: BigInteger
    ) : Flow<Boolean>

    fun getPendingTransfer() : Flow<CcipTransfer?>

    suspend fun getCcipMessageId(txHash: String, sourceChain: Chain) : String?
}
