package com.crosschain.assettracker.data.network

import com.crosschain.assettracker.constants.WalletConnectConstants.METHOD_ETH_SEND_TRANSACTION
import com.reown.appkit.client.AppKit
import com.reown.appkit.client.models.request.Request
import com.reown.appkit.client.models.request.SentRequestResult
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.FlowPreview
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.flow.conflate
import kotlinx.coroutines.flow.debounce
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.flowOn
import kotlinx.coroutines.flow.onStart
import kotlinx.coroutines.flow.sample
import kotlinx.coroutines.flow.take
import kotlinx.coroutines.launch
import kotlinx.coroutines.reactive.asFlow
import kotlinx.coroutines.withContext
import org.web3j.abi.FunctionEncoder
import org.web3j.abi.FunctionReturnDecoder
import org.web3j.abi.TypeReference
import org.web3j.abi.datatypes.Function
import org.web3j.protocol.Web3j
import org.web3j.abi.datatypes.Type
import org.web3j.protocol.core.DefaultBlockParameterName
import org.web3j.protocol.core.DefaultBlockParameterNumber
import org.web3j.protocol.core.methods.request.EthFilter
import org.web3j.protocol.core.methods.request.Transaction
import org.web3j.protocol.core.methods.response.EthBlock
import org.web3j.protocol.core.methods.response.Log
import org.web3j.protocol.core.methods.response.TransactionReceipt
import org.web3j.protocol.exceptions.TransactionException
import org.web3j.protocol.http.HttpService
import org.web3j.protocol.websocket.WebSocketService
import org.web3j.tx.response.PollingTransactionReceiptProcessor
import org.web3j.utils.Numeric
import timber.log.Timber
import java.math.BigInteger
import java.util.concurrent.TimeUnit
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class BlockchainService @Inject constructor() {
    suspend inline fun <reified T> sendEthCall(
        rpcUrl: String,
        fromAddress: String?,
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

    suspend fun waitForEthTransactionReceipt(rpcUrl: String, txHash: String): TransactionReceipt? = withContext(Dispatchers.IO) {
        val web3j = Web3j.build(HttpService(rpcUrl))
        val polling = PollingTransactionReceiptProcessor(web3j, 1000, 100)
        try {
            val receipt = polling.waitForTransactionReceipt(txHash)
            return@withContext receipt
        } catch (e: TransactionException) {
            Timber.tag(TAG).e(e)
            return@withContext null
        }
    }

    fun filterTopicsForEvent(
        rpcUrl: String,
        address: String,
        eventSignature: String,
        vararg topics: String
    ): Boolean {

        val web3j = Web3j.build(HttpService(rpcUrl))
        val latestBlock = web3j.ethBlockNumber().send().blockNumber

        val startBlock = latestBlock.subtract(BigInteger.valueOf(10))

        val filter = EthFilter(
            DefaultBlockParameterNumber(startBlock),
            DefaultBlockParameterName.LATEST,
            address
        ).apply {
            addSingleTopic(eventSignature)
            topics.forEach {
                addOptionalTopics(it)
            }
            addOptionalTopics(null)
        }

        val ethLog = web3j.ethGetLogs(filter).send()

        if (ethLog.hasError()) {
            Timber.tag(TAG).e("ethLog.hasError, ${ethLog.error}")
            return false
        }
        val logs = ethLog.logs
        if (logs.isNullOrEmpty()) {
            Timber.tag(TAG).e("ethLog.logs is null or empty, logs = $logs")
            return false
        }
        val data = (logs.first() as Log).data.removePrefix("0x")

        val stateHex = data.substring(64, 128)
        val state = Integer.parseInt(stateHex.trimStart('0').ifEmpty { "0" }, 16)
        return state == 2
    }

    @OptIn(FlowPreview::class)
    fun observeEthBlock(
        rpcUrl: String,
        period: Long = 10000
    ): Flow<EthBlock> {
        val wsService = WebSocketService(rpcUrl.replace("https", "wss"), true)
        wsService.connect()
        val web3j = Web3j.build(wsService)
        return web3j.blockFlowable(false)
            .asFlow()
            .sample(period)
            .onStart {
                emit(EthBlock())
            }.flowOn(Dispatchers.IO)
    }

    fun sendEthTransaction(
        coroutineScope: CoroutineScope,
        fromAddress: String,
        toAddress: String,
        methodName: String,
        inputParameters: List<Type<*>>,
        outputParameters: List<TypeReference<*>>,
        value: String = "0x0",
        gas: BigInteger = BigInteger("500000") //TODO: Fixed gas for now
    ): Flow<Long?> = callbackFlow {
        val activeSessionTopic = AppKit.getActiveSession()
        Timber.tag(TAG).d("ActiveSessionTopic : $activeSessionTopic")
        val account = AppKit.getAccount()
        Timber.tag(TAG).d("account : $account")

        if (activeSessionTopic?.topic.isNullOrBlank()) {
            Timber.tag(TAG).e("ActiveSessionTopic is null or empty string")
            trySend(null)
            close()
            return@callbackFlow
        }
        val function = Function(
            methodName,
            inputParameters,
            outputParameters
        )

        val txParams = "[{\"from\":\"$fromAddress\"," +
                "\"to\":\"${toAddress}\"," +
                "\"data\":\"${FunctionEncoder.encode(function)}\"," +
                "\"gas\":\"${Numeric.toHexStringWithPrefix(gas)}\"" +
                ",\"value\":\"$value\"}]"

        val request = Request(
            method = METHOD_ETH_SEND_TRANSACTION,
            params = txParams
        )

        AppKit.request(request, onSuccess = { sentRequestResult ->
            Timber.tag(TAG).d("Succeed to request, result: $sentRequestResult")
            coroutineScope.launch(Dispatchers.IO) {
                val requestId = (sentRequestResult as SentRequestResult.WalletConnect).requestId
                trySend(requestId)
                close()
            }
        }, onError = { error ->
            Timber.tag(TAG).e("Fail to request, error: $error")
            coroutineScope.launch {
                trySend(null)
                close()
            }
        })

        awaitClose {
            Timber.tag(TAG).d("sendEthTransaction() callback flow close")
        }
    }.flowOn(Dispatchers.IO)

    companion object {
        const val TAG = "BlockchainService"
    }
}