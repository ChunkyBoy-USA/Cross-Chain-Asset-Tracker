package com.crosschain.assettracker.data.model

import org.web3j.abi.datatypes.Address
import org.web3j.abi.datatypes.StaticStruct
import org.web3j.abi.datatypes.generated.Uint256

data class TokenAmount(val token: Address, val amount: Uint256) : StaticStruct(token, amount)