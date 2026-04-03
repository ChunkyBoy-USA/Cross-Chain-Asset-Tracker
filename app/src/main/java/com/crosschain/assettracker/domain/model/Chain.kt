package com.crosschain.assettracker.domain.model

import com.crosschain.assettracker.constants.ArbitrumSepoliaConstants
import com.crosschain.assettracker.constants.EthereumSepoliaConstants
import java.math.BigInteger

enum class Chain(
    val chainNamespace: String,
    val chainReference: String,
    val chainId: String,
    val rpcUrl: String,
    val rebaseTokenAddress: String,
    val linkTokenAddress: String,
    val ccipRouterAddress: String,
    val ccipRegistryModuleOwnerCustom: String,
    val ccipTokenAdminRegistry: String,
    val ccipRnmProxyAddress: String,
    val ccipChainSelector: BigInteger
) {
    ETHEREUM(
        EthereumSepoliaConstants.CHAIN_NAME_SPACE,
        EthereumSepoliaConstants.CHAIN_REFERENCE,
        EthereumSepoliaConstants.CHAIN_ID,
        EthereumSepoliaConstants.RPC_URL,
        EthereumSepoliaConstants.REBASE_TOKEN_ADDRESS,
        EthereumSepoliaConstants.LINK_TOKEN_ADDRESS,
        EthereumSepoliaConstants.CCIP_ROUTER_ADDRESS,
        EthereumSepoliaConstants.CCIP_REGISTRY_MODULE_OWNER_CUSTOM,
        EthereumSepoliaConstants.CCIP_TOKEN_ADMIN_REGISTRY,
        EthereumSepoliaConstants.CCIP_RNM_PROXY_ADDRESS,
        BigInteger(EthereumSepoliaConstants.CCIP_CHAIN_SELECTOR)
    ),
    ARBITRUM(
        ArbitrumSepoliaConstants.CHAIN_NAME_SPACE,
        ArbitrumSepoliaConstants.CHAIN_REFERENCE,
        ArbitrumSepoliaConstants.CHAIN_ID,
        ArbitrumSepoliaConstants.RPC_URL,
        ArbitrumSepoliaConstants.REBASE_TOKEN_ADDRESS,
        ArbitrumSepoliaConstants.LINK_TOKEN_ADDRESS,
        ArbitrumSepoliaConstants.CCIP_ROUTER_ADDRESS,
        ArbitrumSepoliaConstants.CCIP_REGISTRY_MODULE_OWNER_CUSTOM,
        ArbitrumSepoliaConstants.CCIP_TOKEN_ADMIN_REGISTRY,
        ArbitrumSepoliaConstants.CCIP_RNM_PROXY_ADDRESS,
        BigInteger(ArbitrumSepoliaConstants.CCIP_CHAIN_SELECTOR)
    )
}

fun String.nameToChain(): Chain {
    return when(this) {
        Chain.ETHEREUM.name -> {
            Chain.ETHEREUM
        }
        Chain.ARBITRUM.name -> {
            Chain.ARBITRUM
        }
        else -> {
            throw IllegalArgumentException("Invalid chain name: $this")
        }
    }
}