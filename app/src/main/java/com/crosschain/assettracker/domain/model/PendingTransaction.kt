package com.crosschain.assettracker.domain.model

import com.crosschain.assettracker.data.local.database.entity.PendingTransactionEntity

enum class PendingTransactionType {
    CCIP_REQUEST,
    ROUTER_ALLOWANCE_REQUEST
}

data class PendingTransaction(
    val requestId: String,
    val type: PendingTransactionType,
    val txHash: String?
)

fun PendingTransactionEntity.toPendingTransaction() = PendingTransaction(
    requestId = requestId,
    type = type,
    txHash = txHash
)

fun PendingTransaction.toPendingTransactionEntity() = PendingTransactionEntity(
    requestId = requestId,
    type = type,
    txHash = txHash
)