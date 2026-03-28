package com.crosschain.assettracker.domain.model

import com.crosschain.assettracker.constants.ArbitrumSepoliaConstants
import com.crosschain.assettracker.constants.EthereumSepoliaConstants

enum class Chain(
    val chainId: String,
    val rpcUrl: String,
    val rebaseTokenAddress: String,
    val linkTokenAddress: String
) {
    ETHEREUM(
        EthereumSepoliaConstants.chainId,
        EthereumSepoliaConstants.rpcUrl,
        EthereumSepoliaConstants.rebaseTokenAddress,
        EthereumSepoliaConstants.linkTokenAddress
    ),
    ARBITRUM(
        ArbitrumSepoliaConstants.chainId,
        ArbitrumSepoliaConstants.rpcUrl,
        ArbitrumSepoliaConstants.rebaseTokenAddress,
        ArbitrumSepoliaConstants.linkTokenAddress
    )
}