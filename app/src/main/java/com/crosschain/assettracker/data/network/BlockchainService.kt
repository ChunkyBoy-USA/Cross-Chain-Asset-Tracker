package com.crosschain.assettracker.data.network

import org.web3j.abi.FunctionEncoder
import org.web3j.abi.FunctionReturnDecoder
import org.web3j.abi.TypeReference
import org.web3j.abi.datatypes.Function
import org.web3j.protocol.Web3j
import org.web3j.abi.datatypes.Type
import org.web3j.protocol.core.DefaultBlockParameterName
import org.web3j.protocol.core.methods.request.Transaction
import org.web3j.protocol.http.HttpService
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class BlockchainService @Inject constructor() {

    inline fun <reified T> sendEthCall(
        rpcUrl: String,
        fromAddress: String,
        toAddress: String,
        methodName: String,
        inputParameters: List<Type<*>>,
        outputParameters: List<TypeReference<*>>
    ): T? {
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

        val results = FunctionReturnDecoder.decode(response.value, function.outputParameters)
        return if (results.isNotEmpty()) {
            results[0].value as T
        } else {
           null
        }
    }
}