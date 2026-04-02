package com.crosschain.assettracker.data.network

import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import org.web3j.abi.FunctionEncoder
import org.web3j.abi.FunctionReturnDecoder
import org.web3j.abi.TypeReference
import org.web3j.abi.datatypes.Function
import org.web3j.protocol.Web3j
import org.web3j.abi.datatypes.Type
import org.web3j.protocol.core.DefaultBlockParameterName
import org.web3j.protocol.core.methods.request.Transaction
import org.web3j.protocol.core.methods.response.TransactionReceipt
import org.web3j.protocol.http.HttpService
import timber.log.Timber
import javax.inject.Inject
import javax.inject.Singleton
import kotlin.jvm.optionals.getOrNull

@Singleton
class BlockchainService @Inject constructor() {
    suspend inline fun <reified T> sendEthCall(
        rpcUrl: String,
        fromAddress: String,
        toAddress: String,
        methodName: String,
        inputParameters: List<Type<*>>,
        outputParameters: List<TypeReference<*>>
    ): T = withContext(Dispatchers.IO) {
        try {
            Timber.tag(TAG).d("sendEthCall: rpcUrl: $rpcUrl, fromAddress: $fromAddress, toAddress: $toAddress, methodName: $methodName, inputParameters: $inputParameters")
            val web3j = Web3j.build(HttpService(rpcUrl))
            val function = Function(
                methodName,
                inputParameters,
                outputParameters
            )

            val encodedFunction = FunctionEncoder.encode(function)

            val response = web3j.ethCall(
                Transaction.createEthCallTransaction(fromAddress, toAddress, encodedFunction),
                DefaultBlockParameterName.LATEST
            ).send()

            if (response.hasError()) {
                Timber.tag(TAG).d("response hasError, message: ${response.error.message}")
                throw Exception(response.error.message)
            }
            val results = FunctionReturnDecoder.decode(response.value, function.outputParameters)
            Timber.tag(TAG).d("sendEthCall results: $results")
            if (results.isNotEmpty()) {
                val value = results[0].value
                if (value is T) {
                    return@withContext value
                }else {
                    throw Exception("ethCall result is not Type ${T::class.java.simpleName}")
                }
            } else {
                throw Exception("ethCall result is Empty")
            }
        } catch (e: Exception) {
            throw e
        }
    }

    suspend fun getEthTransactionReceipt(rpcUrl: String, txHash: String): TransactionReceipt? = withContext(Dispatchers.IO) {
        val web3j = Web3j.build(HttpService(rpcUrl))
        val response = web3j.ethGetTransactionReceipt(txHash).send()
        val receipt = response.transactionReceipt
        if (receipt != null && receipt.isPresent) {
            return@withContext receipt.getOrNull()
        } else {
            Timber.tag(TAG).d("No transaction receipt found for txHash: $txHash, rpcUrl: $rpcUrl")
            return@withContext null
        }
    }

    companion object {
        const val TAG = "BlockchainService"
    }
}