package com.crosschain.assettracker.data.repository

import com.crosschain.assettracker.constants.RebaseTokenConstants
import com.crosschain.assettracker.data.local.database.dao.RebaseTokenMetaDataDao
import com.crosschain.assettracker.data.local.database.entity.RebaseTokenMetadataEntity
import com.crosschain.assettracker.data.network.BlockchainService
import com.crosschain.assettracker.domain.model.BalanceInfo
import com.crosschain.assettracker.domain.model.Chain
import com.crosschain.assettracker.domain.RebaseTokenRepository
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.flowOn
import org.web3j.abi.TypeReference
import org.web3j.abi.datatypes.Address
import org.web3j.abi.datatypes.Utf8String
import org.web3j.abi.datatypes.generated.Uint256
import org.web3j.abi.datatypes.generated.Uint8
import timber.log.Timber
import java.math.BigInteger
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class RebaseTokenRepositoryImpl @Inject constructor(
    val service: BlockchainService,
    val rebaseTokenMetaDataDao: RebaseTokenMetaDataDao
) : RebaseTokenRepository {
    override fun getTokenBalance(chain: Chain, address: String): Flow<BalanceInfo> = flow {
        Timber.tag(TAG).d("getTokenBalance, chain = ${chain.chainId}")
        val localRecord =
            rebaseTokenMetaDataDao.getMetadata(chain.chainId, chain.rebaseTokenAddress)

        val symbol = localRecord?.symbol
            ?: service.sendEthCall<String>(
                chain.rpcUrl,
                null,
                chain.rebaseTokenAddress,
                RebaseTokenConstants.GET_SYMBOL_FUNCTION,
                listOf(),
                listOf(object : TypeReference<Utf8String>() {})
            )

        val decimals = localRecord?.decimals
            ?: service.sendEthCall<BigInteger>(
                chain.rpcUrl,
                null,
                chain.rebaseTokenAddress,
                RebaseTokenConstants.GET_DECIMAL_PLACES_FUNCTION,
                listOf(),
                listOf(object : TypeReference<Uint8>() {})
            ).toInt()

        val baseInterestRate = localRecord?.baseInterestRate
            ?: service.sendEthCall<BigInteger>(
                chain.rpcUrl,
                null,
                chain.rebaseTokenAddress,
                RebaseTokenConstants.GET_BASE_INTEREST_RATE_FUNCTION,
                listOf(),
                listOf(object : TypeReference<Uint256>() {})
            ).toBigDecimal().movePointLeft(decimals).toPlainString()

        if (localRecord == null) {
            rebaseTokenMetaDataDao.insertMetadata(
                RebaseTokenMetadataEntity(
                    chainId = chain.chainId,
                    tokenAddress = chain.rebaseTokenAddress,
                    symbol = symbol,
                    decimals = decimals,
                    baseInterestRate = baseInterestRate
                )
            )
        }

        service.observeEthTransaction(chain.rpcUrl).collect {
            if (address.isNotBlank()) {
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

                val bigDecimalBalance = balance.toBigDecimal().movePointLeft(decimals)
                val bigDecimalInterestRate = currentInterestRate.toBigDecimal().movePointLeft(decimals)

                emit(
                    BalanceInfo(
                        amount = bigDecimalBalance.toPlainString(),
                        tokenSymbol = symbol,
                        currentInterestRate = bigDecimalInterestRate.toPlainString(),
                        baseInterestRate = baseInterestRate,
                        chain = chain
                    )
                )
            }
        }
    }.flowOn(Dispatchers.IO)

    companion object {
        const val TAG = "RebaseTokenRepositoryImpl"
    }
}