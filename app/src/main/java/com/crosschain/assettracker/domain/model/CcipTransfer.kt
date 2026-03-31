package com.crosschain.assettracker.domain.model

import com.crosschain.assettracker.data.local.database.entity.CcipSentRequestEntity

enum class TransferStatus {
    INITIATED,
    WAITING_FOR_FINALITY,
    SUCCESS,
    FAILED
}

data class CcipTransfer(
    val requestId: Long,
    val sessionTopic: String,
    val method: String,
    val params: String,
    val chainId: String,
    val txHash: String?,
    val ccipMessageId: String?,
    val status: TransferStatus,
    val sourceChain: String,
    val destinationChain: String
)

fun CcipSentRequestEntity.toCcipTransfer() = CcipTransfer(
    requestId = requestId,
    sessionTopic = sessionTopic,
    method = method,
    params = params,
    chainId = chainId,
    txHash = txHash,
    ccipMessageId = ccipMessageId,
    status = status,
    sourceChain = sourceChain,
    destinationChain = destinationChain
)

fun CcipTransfer.toCcipSentRequestEntity() = CcipSentRequestEntity(
    requestId = requestId,
    sessionTopic = sessionTopic,
    method = method,
    params = params,
    chainId = chainId,
    txHash = txHash,
    ccipMessageId = ccipMessageId,
    status = status,
    sourceChain = sourceChain,
    destinationChain = destinationChain
)

fun CcipTransfer.statusToProgress(): Float = when (status) {
    TransferStatus.INITIATED -> 0f
    TransferStatus.WAITING_FOR_FINALITY -> 0.5f
    TransferStatus.SUCCESS -> 1f
    TransferStatus.FAILED -> 1f
}
