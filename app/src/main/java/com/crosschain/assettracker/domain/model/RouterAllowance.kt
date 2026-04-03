package com.crosschain.assettracker.domain.model

import com.crosschain.assettracker.data.local.database.entity.RouterAllowanceEntity

data class RouterAllowance(
    val routerAddress: String,
    val walletAddress: String,
    val tokenAddress: String,
    val chainId: String,
    val allowance: String?
)

fun RouterAllowance.toRouterAllowanceEntity() = RouterAllowanceEntity(
        routerAddress = routerAddress,
        walletAddress = walletAddress,
        tokenAddress = tokenAddress,
        chainId = chainId,
        allowance = allowance
)

fun RouterAllowanceEntity.toRouterAllowance() = RouterAllowance(
    routerAddress = routerAddress,
    walletAddress = walletAddress,
    tokenAddress = tokenAddress,
    chainId = chainId,
    allowance = allowance
)