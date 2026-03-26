package com.crosschain.assettracker.domain.model

enum class Chain(val rpcUrl: String, val chainId: Int) {
    ETHEREUM(
        rpcUrl = "https://eth-sepolia.g.alchemy.com/v2/6dWSSrHidtXsRg2VcHlfy",
        11155111
    ),
    ARBITRUM(
        rpcUrl = "https://arb-sepolia.g.alchemy.com/v2/6dWSSrHidtXsRg2VcHlfy",
        421614
    )
}