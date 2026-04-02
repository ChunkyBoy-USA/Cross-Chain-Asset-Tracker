package com.crosschain.assettracker.data.repository

import com.crosschain.assettracker.constants.ERC20Constants.APPROVE_FUNCTION
import com.crosschain.assettracker.constants.RouterClientConstants
import com.crosschain.assettracker.constants.RouterClientConstants.GENERIC_EXTRA_ARGS_V2_TAG
import com.crosschain.assettracker.constants.RouterClientConstants.GET_FEE_FUNCTION
import com.crosschain.assettracker.constants.WalletConnectConstants.METHOD_ETH_SEND_TRANSACTION
import com.crosschain.assettracker.data.local.database.dao.CcipSentRequestDao
import com.crosschain.assettracker.data.model.TokenAmount
import com.crosschain.assettracker.data.network.BlockchainService
import com.crosschain.assettracker.domain.model.CcipTransfer
import com.crosschain.assettracker.domain.CcipRepository
import com.crosschain.assettracker.domain.model.Chain
import com.crosschain.assettracker.domain.model.TransferStatus
import com.crosschain.assettracker.domain.model.toCcipSentRequestEntity
import com.crosschain.assettracker.domain.model.toCcipTransfer
import com.reown.appkit.client.AppKit
import com.reown.appkit.client.Modal
import com.reown.appkit.client.models.request.Request
import com.reown.appkit.client.models.request.SentRequestResult
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.flowOn
import kotlinx.coroutines.flow.map
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
    val service: BlockchainService,
    val ccipSentRequestDao: CcipSentRequestDao,
    val coroutineScope: CoroutineScope
) : CcipRepository {

    fun initSignClientDelegate() {
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
                            ccipSentRequestDao.updateCcipSentRequestTxHash(
                                jsonRpcResult.result,
                                response.topic
                            )
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

    override fun getPendingTransfer(): Flow<CcipTransfer?> =
        ccipSentRequestDao.getCcipSentRequests().map {
            if (it.isEmpty()) {
                null
            } else {
                it.first().toCcipTransfer()
            }
        }

    override fun trackTransfer(): Flow<CcipTransfer> = flow {

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
            val ccipFee = service.sendEthCall<BigInteger>(
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
            val ccipFeeWithBuffer = ccipFee.add(ccipFee.toBigDecimal().movePointLeft(1).toBigInteger()) // Give 10% buffer
            Timber.tag(TAG).d("ccipFee: ${ccipFee.toLong()}")
            Timber.tag(TAG).d("ccipFeeWithBuffer: ${ccipFeeWithBuffer.toLong()}")

            val approveCcipFee = service.sendEthCall<Boolean>(
                rpcUrl = sourceChain.rpcUrl,
                fromAddress = accountAddress,
                toAddress = sourceChain.linkTokenAddress,
                methodName = APPROVE_FUNCTION,
                inputParameters = listOf(Address(sourceChain.ccipRouterAddress), Uint256(ccipFeeWithBuffer)),
                outputParameters = listOf(object : TypeReference<Bool>() {})
            )
            if (!approveCcipFee) {
                Timber.tag(TAG).e("Fail to approve routerAddress to spend Link Token")
                trySend(false)
                close()
                return@callbackFlow
            }

            val approveAmountToSend = service.sendEthCall<Boolean>(
                rpcUrl = sourceChain.rpcUrl,
                fromAddress = accountAddress,
                toAddress = sourceChain.rebaseTokenAddress,
                methodName = APPROVE_FUNCTION,
                inputParameters = listOf(
                    Address(sourceChain.ccipRouterAddress),
                    Uint256(amountToSend)
                ),
                outputParameters = listOf(object : TypeReference<Bool>() {})
            )
            if (!approveAmountToSend) {
                Timber.tag(TAG).e("Fail to approve routerAddress to spend Rebase Token")
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
            val method = METHOD_ETH_SEND_TRANSACTION
            val txParams = "[{\"from\":\"$accountAddress\"," +
                        "\"to\":\"${sourceChain.ccipRouterAddress}\"," +
                        "\"data\":\"$encodedFunctionData\"," +
                        "\"gas\":\"${Numeric.toHexStringWithPrefix(BigInteger("500000"))}\"" + //TODO: optimize gas limit
                        ",\"value\":\"0x0\"}]"

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

            val request = Request(
                method = method,
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
                            sourceChain = sourceChain.name,
                            destinationChain = destinationChain.name
                        ).toCcipSentRequestEntity()
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

    override suspend fun getCcipMessageId(txHash: String, sourceChain: Chain): String? {
        val receipt = service.getEthTransactionReceipt(sourceChain.rpcUrl, txHash)
        if (receipt == null) {
            Timber.tag(TAG).d("getCcipMessageId() receipt is null")
            return null
        } else {
            val ccipLog = receipt.logs.find { it.topics.contains(CCIP_SEND_REQUESTED_TOPIC) }
            val messageId = "0x" + ccipLog?.data?.substring(834, 834 + 64) // TODO: Hard code for now
            Timber.tag(TAG).d("getCcipMessageId() messageId: $messageId")
            ccipSentRequestDao.insertCcipSentRequestMessageId(messageId, txHash, TransferStatus.WAITING_FOR_FINALITY)
            return messageId
        }
    }

    companion object {
        const val TAG = "CcipRepositoryImpl"
        const val CCIP_SEND_REQUESTED_TOPIC = "0xd0c3c799bf9e2639de44391e7f524d229b2b55f5b1ea94b2bf7da42f7243dddd"
    }
}