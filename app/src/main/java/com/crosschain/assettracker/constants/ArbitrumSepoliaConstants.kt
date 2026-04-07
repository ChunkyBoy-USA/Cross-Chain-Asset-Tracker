package com.crosschain.assettracker.constants

import com.crosschain.assettracker.BuildConfig

object ArbitrumSepoliaConstants {
    const val CHAIN_NAME_SPACE = "eip155"
    const val CHAIN_REFERENCE = "421614"
    const val CHAIN_ID = "$CHAIN_NAME_SPACE:$CHAIN_REFERENCE"
    const val RPC_URL = BuildConfig.ARBITRUM_SEPOLIA_RPC_URL
    const val REBASE_TOKEN_ADDRESS = "0x03Eacf91aBF33470F22857B3C72Fe7e36aa87216"
    const val LINK_TOKEN_ADDRESS = "0xb1D4538B4571d411F07960EF2838Ce337FE1E80E"
    const val CCIP_ROUTER_ADDRESS = "0x2a9C5afB0d0e4BAb2BCdaE109EC4b0c4Be15a165"
    const val CCIP_CHAIN_SELECTOR = "3478487238524512106"

    const val CCIP_OFF_RAMP_ADDRESS_FOR_ETH_SEPOLIA = "0x1c71f141b4630ebe52d6af4894812960abe207eb"
}