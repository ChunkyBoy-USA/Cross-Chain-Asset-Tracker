package com.crosschain.assettracker.data.repository

import com.crosschain.assettracker.constants.RebaseTokenConstants
import com.crosschain.assettracker.data.network.BlockchainService
import com.crosschain.assettracker.domain.model.BalanceInfo
import com.crosschain.assettracker.domain.model.Chain
import com.crosschain.assettracker.domain.BalanceRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import org.web3j.abi.TypeReference
import org.web3j.abi.datatypes.Address
import org.web3j.abi.datatypes.Utf8String
import org.web3j.abi.datatypes.generated.Uint256
import org.web3j.abi.datatypes.generated.Uint8
import java.math.BigInteger
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class BalanceRepositoryImpl @Inject constructor(
    val service: BlockchainService
): BalanceRepository {
    override fun getTokenBalance(chain: Chain, address: String): Flow<BalanceInfo> = flow {
        val balance = service.sendEthCall<BigInteger>(
            chain.rpcUrl,
            address,
            chain.rebaseTokenAddress,
            RebaseTokenConstants.GET_BALANCE_FUNCTION,
            listOf(Address(address)),
            listOf(object : TypeReference<Uint256>() {})
        )

        val currentInterestRate = service.sendEthCall<BigInteger>(
            chain.rpcUrl,
            address,
            chain.rebaseTokenAddress,
            RebaseTokenConstants.GET_USER_INTEREST_RATE_FUNCTION,
            listOf(Address(address)),
            listOf(object : TypeReference<Uint256>() {})
        )

        val symbol = service.sendEthCall<String>(
            chain.rpcUrl,
            address,
            chain.rebaseTokenAddress,
            RebaseTokenConstants.GET_SYMBOL_FUNCTION,
            listOf(),
            listOf(object : TypeReference<Utf8String>() {})
        )

        val decimalPlaces = service.sendEthCall<BigInteger>(
            chain.rpcUrl,
            address,
            chain.rebaseTokenAddress,
            RebaseTokenConstants.GET_DECIMAL_PLACES_FUNCTION,
            listOf(),
            listOf(object : TypeReference<Uint8>() {})
        )

        val bigDecimalBalance = balance.toBigDecimal().movePointLeft(decimalPlaces.toInt())
        bigDecimalBalance.stripTrailingZeros()

        val bigDecimalInterestRate = currentInterestRate.toBigDecimal().movePointLeft(decimalPlaces.toInt())
        bigDecimalInterestRate.stripTrailingZeros()

        emit(BalanceInfo(
            amount = bigDecimalBalance.toPlainString(),
            tokenSymbol = symbol,
            currentInterestRate = bigDecimalInterestRate.toPlainString(),
            chain = chain
        ))
    }
}