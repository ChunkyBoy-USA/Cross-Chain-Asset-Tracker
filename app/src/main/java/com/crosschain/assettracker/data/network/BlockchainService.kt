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
import org.web3j.protocol.http.HttpService
import timber.log.Timber
import javax.inject.Inject
import javax.inject.Singleton

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

    suspend fun sendEthTransaction(
        rpcUrl: String,
        fromAddress: String,
        toAddress: String,
    ) {
        val web3j = Web3j.build(HttpService(rpcUrl))

    }

    companion object {
        const val TAG = "BlockchainService"
    }
}