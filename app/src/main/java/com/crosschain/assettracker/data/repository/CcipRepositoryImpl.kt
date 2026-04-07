package com.crosschain.assettracker.data.repository

import com.crosschain.assettracker.constants.ERC20Constants.APPROVE_FUNCTION
import com.crosschain.assettracker.constants.RouterClientConstants
import com.crosschain.assettracker.constants.RouterClientConstants.GENERIC_EXTRA_ARGS_V2_TAG
import com.crosschain.assettracker.constants.RouterClientConstants.GET_FEE_FUNCTION
import com.crosschain.assettracker.constants.WalletConnectConstants.METHOD_ETH_SEND_TRANSACTION
import com.crosschain.assettracker.data.local.database.dao.CcipSentRequestDao
import com.crosschain.assettracker.data.local.database.dao.PendingTransactionDao
import com.crosschain.assettracker.data.local.database.dao.RouterAllowanceDao
import com.crosschain.assettracker.data.local.database.entity.PendingRouterAllowanceEntity
import com.crosschain.assettracker.data.local.database.entity.PendingTransactionEntity
import com.crosschain.assettracker.data.local.database.entity.RouterAllowanceEntity
import com.crosschain.assettracker.data.model.ExecutionState
import com.crosschain.assettracker.data.model.TokenAmount
import com.crosschain.assettracker.data.network.BlockchainService
import com.crosschain.assettracker.data.network.CcipApiService
import com.crosschain.assettracker.domain.model.CcipTransfer
import com.crosschain.assettracker.domain.CcipRepository
import com.crosschain.assettracker.domain.model.Chain
import com.crosschain.assettracker.domain.model.PendingRouterAllowance
import com.crosschain.assettracker.domain.model.PendingTransaction
import com.crosschain.assettracker.domain.model.PendingTransactionType
import com.crosschain.assettracker.domain.model.RouterAllowance
import com.crosschain.assettracker.domain.model.TransferStatus
import com.crosschain.assettracker.domain.model.toCcipSentRequestEntity
import com.crosschain.assettracker.domain.model.toCcipTransfer
import com.crosschain.assettracker.domain.model.toPendingRouterAllowance
import com.crosschain.assettracker.domain.model.toPendingTransaction
import com.crosschain.assettracker.domain.model.toRouterAllowance
import com.reown.appkit.client.AppKit
import com.reown.appkit.client.Modal
import com.reown.appkit.client.models.request.Request
import com.reown.appkit.client.models.request.SentRequestResult
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.flowOn
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.merge
import kotlinx.coroutines.launch
import org.web3j.abi.FunctionEncoder
import org.web3j.abi.TypeEncoder
import org.web3j.abi.TypeReference
import org.web3j.abi.datatypes.Address
import org.web3j.abi.datatypes.Bool
import org.web3j.abi.datatypes.DynamicArray
import org.web3j.abi.datatypes.DynamicBytes
import org.web3j.abi.datatypes.DynamicStruct
import org.web3j.abi.datatypes.Function
import org.web3j.abi.datatypes.generated.Bytes32
import org.web3j.abi.datatypes.generated.Uint256
import org.web3j.abi.datatypes.generated.Uint64
import org.web3j.utils.Numeric
import timber.log.Timber
import java.math.BigInteger
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class CcipRepositoryImpl @Inject constructor(
    val blockChainService: BlockchainService,
    val ccipSentRequestDao: CcipSentRequestDao,
    val routerAllowanceDao: RouterAllowanceDao,
    val pendingTransactionDao: PendingTransactionDao,
    val coroutineScope: CoroutineScope,
    val ccipApiService: CcipApiService,
) : CcipRepository {

    init {
        initSignClientDelegate()
    }

    fun initSignClientDelegate() {
        // TODO: Need to refactor this callback
        AppKit.setDelegate(object : AppKit.ModalDelegate {
            override fun onConnectionStateChange(state: Modal.Model.ConnectionState) {
                Timber.tag(TAG).d("onConnectionStateChange, state: $state")
            }

            override fun onError(error: Modal.Model.Error) {
                Timber.tag(TAG).d("onError, error: $error")
            }

            override fun onProposalExpired(proposal: Modal.Model.ExpiredProposal) {
                Timber.tag(TAG).d("onProposalExpired, proposal: $proposal")
            }

            override fun onRequestExpired(request: Modal.Model.ExpiredRequest) {
                Timber.tag(TAG).d("onRequestExpired, request: $request")
            }

            override fun onSessionApproved(approvedSession: Modal.Model.ApprovedSession) {
                Timber.tag(TAG).d("onSessionApproved, approvedSession: $approvedSession")
            }

            override fun onSessionDelete(deletedSession: Modal.Model.DeletedSession) {
                Timber.tag(TAG).d("onSessionDelete, onSessionDelete: $deletedSession")
            }

            override fun onSessionEvent(sessionEvent: Modal.Model.SessionEvent) {
                Timber.tag(TAG).d("onSessionEvent, sessionEvent: $sessionEvent")
            }

            override fun onSessionExtend(session: Modal.Model.Session) {
                Timber.tag(TAG).d("onSessionExtend, session: $session")
            }

            override fun onSessionRejected(rejectedSession: Modal.Model.RejectedSession) {
                Timber.tag(TAG).d("onSessionRejected, rejectedSession: $rejectedSession")
            }

            override fun onSessionRequestResponse(response: Modal.Model.SessionRequestResponse) {
                Timber.tag(TAG).d("onSessionRequestResponse, response: $response")
                when (val jsonRpcResult = response.result) {
                    is Modal.Model.JsonRpcResponse.JsonRpcResult -> {
                        coroutineScope.launch(Dispatchers.IO) {
                            pendingTransactionDao.insertPendingTransactionTxHash(jsonRpcResult.result, jsonRpcResult.id.toString())
                        }
                    }

                    is Modal.Model.JsonRpcResponse.JsonRpcError -> {
                        coroutineScope.launch(Dispatchers.IO) {
                            ccipSentRequestDao.clearAllCcipSentRequests()
                        }
                    }
                }
            }

            override fun onSessionUpdate(updatedSession: Modal.Model.UpdatedSession) {
                Timber.tag(TAG).d("onSessionUpdate, updatedSession: $updatedSession")
            }

            override fun onSessionAuthenticateResponse(sessionAuthenticateResponse: Modal.Model.SessionAuthenticateResponse) {
                Timber.tag(TAG)
                    .d("onSessionAuthenticateResponse, sessionAuthenticateResponse: $sessionAuthenticateResponse")
                super.onSessionAuthenticateResponse(sessionAuthenticateResponse)
            }

            override fun onSIWEAuthenticationResponse(response: Modal.Model.SIWEAuthenticateResponse) {
                Timber.tag(TAG).d("onSIWEAuthenticationResponse, response: $response")
                super.onSIWEAuthenticationResponse(response)
            }

        })
    }

    override fun getCcipTransfer(): Flow<CcipTransfer?> =
        merge(ccipSentRequestDao.getCcipSentRequests().map {
            if (it.isEmpty()) {
                null
            } else {
                it.first().toCcipTransfer()
            }
        })

    override suspend fun getRouterAllowance(
        routerAddress: String,
        tokenAddress: String,
        walletAddress: String,
        chainId: String
    ): RouterAllowance? {
        val routerAllowance = routerAllowanceDao.getRouterAllowance(
            routerAddress,
            tokenAddress,
            walletAddress,
            chainId
        )?.toRouterAllowance()

        Timber.tag(TAG).d("routerAllowance: $routerAllowance")
        return routerAllowance
    }


    override fun getRouterAllowanceFlow(
        routerAddress: String,
        tokenAddress: String,
        walletAddress: String,
        chainId: String
    ): Flow<RouterAllowance?> =
        routerAllowanceDao.getRouterAllowanceFlow(
            routerAddress,
            tokenAddress,
            walletAddress,
            chainId
        ).map {
            it?.toRouterAllowance()
        }

    override fun getPendingRouterAllowanceFlow(): Flow<PendingRouterAllowance?> =
        routerAllowanceDao.getPendingRouterAllowance().map {
             it?.toPendingRouterAllowance()
        }

    override fun getPendingTransaction(): Flow<PendingTransaction?> =
        pendingTransactionDao.getPendingTransactionFlow().map {
            it?.toPendingTransaction()
        }

    override suspend fun insertPendingRouterAllowanceTxHash(requestId: String, txHash: String) {
        routerAllowanceDao.insertPendingRouterAllowanceTxHash(txHash, requestId)
    }

    override suspend fun deletePendingRouterAllowance(requestId: String) {
        routerAllowanceDao.deletePendingRouterAllowance(requestId)
    }

    override suspend fun deletePendingTransaction(requestId: String) {
        pendingTransactionDao.deletePendingTransactionEntity(requestId)
    }

    override suspend fun approveRouterToSpend(
        sourceChain: Chain,
        walletAddress: String,
        routerAddress: String,
        tokenAddress: String,
        amountToSpend: BigInteger
    ): Long? {
        val requestId = blockChainService.sendEthTransaction(
            coroutineScope,
            walletAddress,
            tokenAddress,
            APPROVE_FUNCTION,
            listOf(Address(routerAddress), Uint256(amountToSpend)),
            listOf(object : TypeReference<Bool>() {})
        ).first()

        if (requestId != null) {
            routerAllowanceDao.insertPendingRouterAllowance(
                PendingRouterAllowanceEntity(
                    routerAddress,
                    walletAddress,
                    tokenAddress,
                    sourceChain.chainId,
                    sourceChain.rpcUrl,
                    amountToSpend.toString(),
                    requestId.toString(),
                    null,
                    false
                )
            )
            pendingTransactionDao.insertPendingTransactionEntity(
                PendingTransactionEntity(
                    requestId.toString(),
                    PendingTransactionType.ROUTER_ALLOWANCE_REQUEST,
                    null
                )
            )
            Timber.tag(TAG).d("approveTokenToSend() requestId = $requestId, tokenAddress = $tokenAddress, amountToSpend = $amountToSpend")
        }
        return requestId
    }

    override suspend fun getCcipFee(
        accountAddress: String,
        sourceChain: Chain,
        destinationChain: Chain,
        amountToSend: BigInteger
    ): BigInteger {
        val tokenAmounts = TokenAmount(
            Address(sourceChain.rebaseTokenAddress),
            Uint256(amountToSend)
        )
        val gasLimit = Uint256(500_000) // TODO: Set a fixed gas limit for now
        val allowOutOfOrder = Bool(true)
        val encodedArgs =
            GENERIC_EXTRA_ARGS_V2_TAG + TypeEncoder.encode(gasLimit) + TypeEncoder.encode(
                allowOutOfOrder
            )
        val extraArgs = DynamicBytes(Numeric.hexStringToByteArray(encodedArgs))
        val encodedReceiver = TypeEncoder.encode(Address(accountAddress))
        val receiverBytes = DynamicBytes(Numeric.hexStringToByteArray(encodedReceiver))
        val message = DynamicStruct(
            receiverBytes,
            DynamicBytes("Send Rebase Token from ${sourceChain.name} to ${destinationChain.name}".toByteArray()),
            DynamicArray(TokenAmount::class.java, listOf(tokenAmounts)),
            Address(sourceChain.linkTokenAddress),
            extraArgs
        )

        val ccipFee = blockChainService.sendEthCall<BigInteger>(
            rpcUrl = sourceChain.rpcUrl,
            fromAddress = accountAddress,
            toAddress = sourceChain.ccipRouterAddress,
            methodName = GET_FEE_FUNCTION,
            inputParameters = listOf(
                Uint64(destinationChain.ccipChainSelector),
                message
            ),
            outputParameters = listOf(object : TypeReference<Uint256>() {})
        )
        Timber.tag(TAG).d("ccipFee: $ccipFee")
        return ccipFee
    }

    override fun sendRebaseToken(
        accountAddress: String,
        sourceChain: Chain,
        destinationChain: Chain,
        amountToSend: BigInteger
    ): Flow<Boolean> = callbackFlow {

        try {
            // Solidity codes as reference
//        Client.EVM2AnyMessage memory message = Client.EVM2AnyMessage({
//            receiver: abi.encode(receiverAddress),
//            data: abi.encode("Test Cross Chain Message"),
//            tokenAmounts: tokenAmounts,
//            feeToken: linkTokenAddress,
//            extraArgs: Client._argsToBytes(Client.GenericExtraArgsV2({ gasLimit: 1000_000, allowOutOfOrderExecution: true }))
//        });

            val tokenAmounts = TokenAmount(
                Address(sourceChain.rebaseTokenAddress),
                Uint256(amountToSend)
            )
            val gasLimit = Uint256(500_000) // TODO: Set a fixed gas limit for now
            val allowOutOfOrder = Bool(true)
            val encodedArgs =
                GENERIC_EXTRA_ARGS_V2_TAG + TypeEncoder.encode(gasLimit) + TypeEncoder.encode(
                    allowOutOfOrder
                )
            val extraArgs = DynamicBytes(Numeric.hexStringToByteArray(encodedArgs))
            val encodedReceiver = TypeEncoder.encode(Address(accountAddress))
            val receiverBytes = DynamicBytes(Numeric.hexStringToByteArray(encodedReceiver))
            val message = DynamicStruct(
                receiverBytes,
                DynamicBytes("Send Rebase Token from ${sourceChain.name} to ${destinationChain.name}".toByteArray()),
                DynamicArray(TokenAmount::class.java, listOf(tokenAmounts)),
                Address(sourceChain.linkTokenAddress),
                extraArgs
            )
            Timber.tag(TAG).d("message: $message")

            // Foundry CLI as reference
//        cast call  0x0BF3dE8c5D3e8A2B34D2BEeB17ABfCeBaf363A59 --rpc-url https://eth-sepolia.g.alchemy.co
//        m/v2/ "getFee(uint64,(bytes,bytes,(address,uint256)[],address,bytes))" "3478487238524512106" "(0xd4bD9a13058A2Ff4D
//        a5Cf699e51432aDA67aA84B,$(cast --format-bytes32-string "test message"),[(0x9c8276c5446574e12446eD893Ab5ae4561214979,1000000)],0x779877A
//        7B0D9E8603169DdbD7836e478b4624789,0x181dcf10$(cast abi-encode "f(uint256,bool)" 1000000 true | cut -c 3-))" -vvvv

            val activeSessionTopic = AppKit.getActiveSession()
            Timber.tag(TAG).d("ActiveSessionTopic : $activeSessionTopic")
            val account = AppKit.getAccount()
            Timber.tag(TAG).d("account : $account")

            if (activeSessionTopic?.topic.isNullOrBlank()) {
                Timber.tag(TAG).e("ActiveSessionTopic is null or empty string")
                trySend(false)
                close()
                return@callbackFlow
            }

            val function = Function(
                RouterClientConstants.CCIP_SEND_FUNCTION,
                listOf(Uint64(destinationChain.ccipChainSelector), message),
                listOf(object : TypeReference<Bytes32>() {})
            )
            val encodedFunctionData = FunctionEncoder.encode(function)
            val txParams = "[{\"from\":\"$accountAddress\"," +
                    "\"to\":\"${sourceChain.ccipRouterAddress}\"," +
                    "\"data\":\"$encodedFunctionData\"," +
                    "\"gas\":\"${Numeric.toHexStringWithPrefix(BigInteger("500000"))}\"" + //TODO: optimize gas limit
                    ",\"value\":\"0x0\"}]"

            val request = Request(
                method = METHOD_ETH_SEND_TRANSACTION,
                params = txParams
            )

            AppKit.request(request, onSuccess = { sentRequestResult ->
                Timber.tag(TAG).d("Succeed to request, result: $sentRequestResult")
                coroutineScope.launch(Dispatchers.IO) {
                    ccipSentRequestDao.insertCcipSentRequest(
                        CcipTransfer(
                            requestId = (sentRequestResult as SentRequestResult.WalletConnect).requestId,
                            sessionTopic = sentRequestResult.sessionTopic,
                            method = sentRequestResult.method,
                            params = sentRequestResult.params,
                            chainId = sentRequestResult.chainId,
                            status = TransferStatus.INITIATED,
                            txHash = null,
                            ccipMessageId = null,
                            sourceChainName = sourceChain.name,
                            destinationChainName = destinationChain.name,
                            offRampAddress = destinationChain.ccipOffRampAddress
                        ).toCcipSentRequestEntity()
                    )
                    pendingTransactionDao.insertPendingTransactionEntity(
                        PendingTransactionEntity(
                            sentRequestResult.requestId.toString(),
                            PendingTransactionType.CCIP_REQUEST,
                            null
                        )
                    )
                    trySend(true)
                    close()
                }
            }, onError = { error ->
                Timber.tag(TAG).e("Fail to request, error: $error")
                coroutineScope.launch {
                    trySend(false)
                    close()
                }
            })
        } catch (e: Exception) {
            Timber.tag(TAG).e(e, "Error in sendRebaseToken")
            trySend(false)
            close()
        }

        awaitClose {
            Timber.tag(TAG).d("sendRebaseToken() callback flow close")
        }
    }.flowOn(Dispatchers.IO)

    override suspend fun retrieveCcipMessageIdAndSequenceNumber(txHash: String, sourceChain: Chain): Boolean {
        val receipt = blockChainService.waitForEthTransactionReceipt(sourceChain.rpcUrl, txHash)
        if (receipt == null) {
            Timber.tag(TAG).d("getCcipMessageId() receipt is null")
            return false
        } else {
            Timber.tag(TAG).d("receipt.logs =  ${receipt.logs}")
            val ccipLog = receipt.logs.find { it.topics.contains(CCIP_SEND_REQUESTED_TOPIC) }
            val data = ccipLog?.data
            if (data == null) {
                Timber.tag(TAG).d("getCcipMessageId() receipt logs data is null")
                return false
            }
            val messageId =
                "0x" + data.substring(834, 834 + 64) // TODO: Hard code for now
            Timber.tag(TAG).d("getCcipMessageId() messageId: $messageId")
            val messageSequenceNumber = data.substring(data.length - 64).trimStart('0') // TODO: Hard code for now
            Timber.tag(TAG).d("getCcipMessageId() messageSequenceNumber: $messageSequenceNumber")
            ccipSentRequestDao.insertCcipSentRequestMessageIdAndSequenceNumber(
                messageSequenceNumber,
                messageId,
                txHash,
                TransferStatus.WAITING_FOR_FINALITY
            )
            return true
        }
    }

    override suspend fun waitForPendingRouterAllowanceApproved(txHash: String, rpcUrl: String): Boolean {
        val isStatusOK = blockChainService.waitForEthTransactionReceipt(rpcUrl, txHash)?.isStatusOK
        if (isStatusOK == null || !isStatusOK) {
            Timber.tag(TAG).d("waitForPendingRouterAllowanceApproved() fail, txHash: $txHash, rpcUrl: $rpcUrl")
            return false
        } else {
            Timber.tag(TAG).d("waitForPendingRouterAllowanceApproved() succeed, txHash: $txHash, rpcUrl: $rpcUrl")
            return true
        }
    }

    override suspend fun routerAllowanceApproved(
        routerAddress: String,
        tokenAddress: String,
        walletAddress: String,
        chainId: String,
        allowanceApproved: String
    ) {
        val existing = routerAllowanceDao.getRouterAllowance(
            routerAddress,
            tokenAddress,
            walletAddress,
            chainId
        )
        if (existing == null) {
            routerAllowanceDao.insertRouterAllowance(
                RouterAllowanceEntity(
                    routerAddress,
                    walletAddress,
                    tokenAddress,
                    chainId,
                    allowanceApproved
                )
            )
        } else {
            routerAllowanceDao.routerAllowanceApproved(
                routerAddress,
                tokenAddress,
                walletAddress,
                chainId,
                allowanceApproved
            )
        }
    }

    override suspend fun waitForCcipTransfer(
        sourceChain: Chain,
        destinationChain: Chain,
        messageId: String,
        maxRetries: Int
    ): ExecutionState {
        repeat(maxRetries) { attempt ->
            // TODO: Use web3j way to monitor CCIP transfer state
            try {
                val response = ccipApiService.getCcipTransferDetails(messageId)
                if (response.isSuccessful) {
                    val statusString = response.body()?.status
                    if (statusString == ExecutionState.SUCCESS) {
                        Timber.tag(TAG).d("getCcipTransferDetails() status = SUCCESS")
                        return ExecutionState.SUCCESS
                    } else if (statusString == ExecutionState.FAILURE) {
                        Timber.tag(TAG).d("getCcipTransferDetails() status = FAILURE")
                        return ExecutionState.FAILURE
                    } else {
                        Timber.tag(TAG).d("waitForCcipTransfer() waiting...")
                    }
                } else {
                    Timber.tag(TAG).d("getCcipTransferDetails() failed, response: $response")
                    return ExecutionState.FAILURE
                }
            } catch (e: Exception) {
                Timber.tag(TAG).e(e)
                Timber.tag(TAG).d("waitForCcipTransfer() Attempt $attempt: Message not yet committed or reverted.")
            }
            delay(10_000)
        }
        return ExecutionState.FAILURE
    }

    override suspend fun insertCcipTransferTxHash(requestId: String, txHash: String) {
        ccipSentRequestDao.insertCcipTransferTxHash(txHash, requestId)
    }

    override suspend fun deleteAllRouterAllowance() {
        routerAllowanceDao.deleteAllRouterAllowance()
    }

    override suspend fun deleteCcipTransfer(requestId: String) {
        ccipSentRequestDao.deleteCcipSentRequest(requestId)
    }

    companion object {
        const val TAG = "CcipRepositoryImpl"
        const val CCIP_SEND_REQUESTED_TOPIC =
            "0xd0c3c799bf9e2639de44391e7f524d229b2b55f5b1ea94b2bf7da42f7243dddd"
    }
}