package com.crosschain.assettracker.domain.model

enum class Chain(val rpcUrl: String, val chainId: Int) {
    ETHEREUM(
        rpcUrl = "https://eth-sepolia.g.alchemy.com/v2/6dWSSrHidtXsRg2VcHlfy",
        1
    ),
    ARBITRUM(
        rpcUrl = "https://arb-sepolia.g.alchemy.com/v2/6dWSSrHidtXsRg2VcHlfy",
        42161
    )
}