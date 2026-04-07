package com.crosschain.assettracker.domain.model

import com.crosschain.assettracker.data.local.database.entity.PendingRouterAllowanceEntity

data class PendingRouterAllowance(
    val routerAddress: String,
    val walletAddress: String,
    val tokenAddress: String,
    val chainId: String,
    val rpcUrl: String,
    val pendingAllowance: String,
    val requestId: String,
    val txHash: String?,
    val isApproved: Boolean
)

fun PendingRouterAllowance.toPendingRouterAllowanceEntity() = PendingRouterAllowanceEntity(
    routerAddress = routerAddress,
    walletAddress = walletAddress,
    tokenAddress = tokenAddress,
    chainId = chainId,
    requestId = requestId,
    txHash = txHash,
    isApproved = isApproved,
    pendingAllowance = pendingAllowance,
    rpcUrl = rpcUrl
)

fun PendingRouterAllowanceEntity.toPendingRouterAllowance() = PendingRouterAllowance(
    routerAddress = routerAddress,
    walletAddress = walletAddress,
    tokenAddress = tokenAddress,
    chainId = chainId,
    requestId = requestId,
    txHash = txHash,
    isApproved = isApproved,
    pendingAllowance = pendingAllowance,
    rpcUrl = rpcUrl,
)