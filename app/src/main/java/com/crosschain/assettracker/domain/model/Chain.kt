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
        EthereumSepoliaConstants.CHAIN_ID,
        EthereumSepoliaConstants.RPC_URL,
        EthereumSepoliaConstants.REBASE_TOKEN_ADDRESS,
        EthereumSepoliaConstants.LINK_TOKEN_ADDRESS
    ),
    ARBITRUM(
        ArbitrumSepoliaConstants.CHAIN_ID,
        ArbitrumSepoliaConstants.RPC_URL,
        ArbitrumSepoliaConstants.REBASE_TOKEN_ADDRESS,
        ArbitrumSepoliaConstants.LINK_TOKEN_ADDRESS
    )
}