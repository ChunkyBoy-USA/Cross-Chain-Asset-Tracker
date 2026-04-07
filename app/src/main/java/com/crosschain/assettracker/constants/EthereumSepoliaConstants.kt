package com.crosschain.assettracker.constants

import com.crosschain.assettracker.BuildConfig

object EthereumSepoliaConstants {
    const val CHAIN_NAME_SPACE = "eip155"
    const val CHAIN_REFERENCE = "11155111"
    const val CHAIN_ID = "$CHAIN_NAME_SPACE:$CHAIN_REFERENCE"
    const val RPC_URL = BuildConfig.ETHEREUM_SEPOLIA_RPC_URL
    const val REBASE_TOKEN_ADDRESS = "0x9c8276c5446574e12446eD893Ab5ae4561214979"
    const val LINK_TOKEN_ADDRESS = "0x779877A7B0D9E8603169DdbD7836e478b4624789"
    const val VAULT_ADDRESS = "0x9B309A8f1a314228eF225b21cAC3f27f3E0D3113"
    const val CCIP_ROUTER_ADDRESS = "0x0BF3dE8c5D3e8A2B34D2BEeB17ABfCeBaf363A59"
    const val CCIP_CHAIN_SELECTOR = "16015286601757825753"

    const val CCIP_OFF_RAMP_ADDRESS_FOR_ARB_SEPOLIA = "0xf18896ab20a09a29e64fdeba99fdb8ec328f43b1"
}