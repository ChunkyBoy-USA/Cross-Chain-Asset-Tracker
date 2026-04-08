package com.crosschain.assettracker.domain

import com.crosschain.assettracker.data.model.ExecutionState
import com.crosschain.assettracker.domain.model.CcipTransfer
import com.crosschain.assettracker.domain.model.Chain
import com.crosschain.assettracker.domain.model.PendingRouterAllowance
import com.crosschain.assettracker.domain.model.PendingTransaction
import com.crosschain.assettracker.domain.model.RouterAllowance
import kotlinx.coroutines.flow.Flow
import java.math.BigInteger

interface CcipRepository {

    fun sendRebaseToken(
        accountAddress: String,
        sourceChain: Chain,
        destinationChain: Chain,
        amountToSend: BigInteger
    ): Flow<Boolean>

    suspend fun approveRouterToSpend(
        sourceChain: Chain,
        walletAddress: String,
        routerAddress: String,
        tokenAddress: String,
        amountToSpend: BigInteger
    ): Long?

    fun getCcipTransfer(): Flow<CcipTransfer?>

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

    fun getPendingRouterAllowances(): Flow<List<PendingRouterAllowance>>

    suspend fun insertPendingRouterAllowanceTxHash(requestId: String, txHash: String)
    suspend fun insertCcipTransferTxHash(requestId: String, txHash: String)

    fun getPendingTransactions(): Flow<List<PendingTransaction>>

    suspend fun deletePendingTransaction(requestId: String)

    suspend fun waitForPendingRouterAllowanceApproved(txHash: String, rpcUrl: String): Boolean

    suspend fun routerAllowanceApproved(
        routerAddress: String,
        tokenAddress: String,
        walletAddress: String,
        chainId: String,
        allowanceApproved: String
    )

    suspend fun deletePendingRouterAllowance(requestId: String)

    suspend fun retrieveCcipMessageIdAndSequenceNumber(txHash: String, sourceChain: Chain): Boolean

    fun waitForCcipTransfer(
        sourceChain: Chain,
        destinationChain: Chain,
        messageId: String,
        maxRetries: Int = 100
    ): Flow<ExecutionState>

    suspend fun getCcipFee(
        accountAddress: String,
        sourceChain: Chain,
        destinationChain: Chain,
        amountToSend: BigInteger
    ): BigInteger

    suspend fun deleteAllRouterAllowance()

    suspend fun deleteCcipTransfer(requestId: String)

}
