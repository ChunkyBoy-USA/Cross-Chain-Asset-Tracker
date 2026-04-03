package com.crosschain.assettracker.domain

import com.crosschain.assettracker.domain.model.CcipTransfer
import com.crosschain.assettracker.domain.model.Chain
import com.crosschain.assettracker.domain.model.RouterAllowance
import kotlinx.coroutines.flow.Flow
import java.math.BigInteger

interface CcipRepository {
    fun trackTransfer(): Flow<CcipTransfer>

    fun sendRebaseToken(
        accountAddress: String,
        sourceChain: Chain,
        destinationChain: Chain,
        amountToSend: BigInteger
    ): Flow<Boolean>

    suspend fun approveCcipFee(
        sourceChainId: String,
        walletAddress: String,
        routerAddress: String,
        erc20Address: String,
        ccipFee: BigInteger
    ): Long?

    suspend fun approveTokenToSend(
        sourceChainId: String,
        walletAddress: String,
        routerAddress: String,
        erc20Address: String,
        amountToSend: BigInteger
    ): Long?

    fun getPendingTransfer(): Flow<CcipTransfer?>

    suspend fun getRouterAllowance(
        routerAddress: String,
        tokenAddress: String,
        walletAddress: String,
        chainId: String
    ): RouterAllowance?

    fun getRouterAllowanceFlow(
        routerAddress: String,
        tokenAddress: String,
        walletAddress: String,
        chainId: String
    ): Flow<RouterAllowance?>

    suspend fun deletePendingRouterAllowance(requestId: String)

    suspend fun getCcipMessageId(txHash: String, sourceChain: Chain): String?

    suspend fun getOffRampAddress(
        txHash: String,
        sourceChain: Chain,
        destinationChain: Chain
    ): String?

    suspend fun getCcipFee(
        accountAddress: String,
        sourceChain: Chain,
        destinationChain: Chain,
        amountToSend: BigInteger
    ): BigInteger

//    fun monitorCcipStatus(messageId: String, destinationChain: Chain, sourceChain: Chain): Flow<String?>
}
