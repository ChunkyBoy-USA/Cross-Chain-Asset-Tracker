package com.crosschain.assettracker

import android.app.Application
import com.reown.android.Core
import com.reown.android.CoreClient
import com.reown.android.relay.ConnectionType
import com.reown.appkit.client.AppKit
import com.reown.appkit.client.Modal
import com.reown.appkit.presets.AppKitChainsPresets.ethToken
import com.reown.appkit.utils.EthUtils
import dagger.hilt.android.HiltAndroidApp
import timber.log.Timber

@HiltAndroidApp
class AssetTrackerApp : Application(), AppKit.ModalDelegate {
    override fun onCreate() {
        super.onCreate()

        if (BuildConfig.DEBUG) {
            Timber.plant(Timber.DebugTree())
        }

        val projectId = BuildConfig.REOWN_PROJECT_ID
        val connectionType = ConnectionType.AUTOMATIC
        val appMetaData = Core.Model.AppMetaData(
            name = "Meta Mask",
            description = "Wallet Description",
            url = "https://Cross-Chain-Asset-Tracker.com",
            icons = listOf("https://gblobscdn.gitbook.com/spaces%2F-LJJeCjcLrr53DcT1Ml7%2Favatar.png?alt=media"),
            redirect = "assert-tracker://request"
        )

        CoreClient.initialize(
            projectId = projectId,
            connectionType = connectionType,
            application = this,
            metaData = appMetaData,
            onError = { error ->
                Timber.tag(TAG).e(error.toString())
            }
        )

        val recommendedWalletsIds = listOf(
            "c57ca95b47569778a828d19178114f4db188b89b763c899ba0be274e97267d96", // Meta Mask
            "4622a2b2d6af1c9844944291e5e7351a6aa24cd7b23099efac1b2fd875da31a0" // Trust Wallet
        )

        AppKit.initialize(
            init = Modal.Params.Init(CoreClient, recommendedWalletsIds = recommendedWalletsIds),
            onSuccess = {
                Timber.tag(TAG).d("initialize onSuccess")
                AppKit.setDelegate(this)
            },
            onError = { error ->
                Timber.tag(TAG).e("error = %s", error.throwable.message)
            }
        )

        // Just choose Ethereum Chain to get wallet address
        // because addresses will be identical on all Ethereum compatible chains
        val chains = mapOf(
            "1" to Modal.Model.Chain(
                chainName = "Ethereum",
                chainNamespace = "eip155",
                chainReference = "1",
                requiredMethods = emptyList(),
                optionalMethods = emptyList(),
                events = EthUtils.ethEvents,
                token = ethToken
            )
        )

        AppKit.setChains(chains.values.toList())
    }

    override fun onConnectionStateChange(state: Modal.Model.ConnectionState) {
        Timber.tag(TAG).d("onConnectionStateChange")
    }

    override fun onError(error: Modal.Model.Error) {
        Timber.tag(TAG).d("onError %s", error.throwable.message)
    }

    override fun onProposalExpired(proposal: Modal.Model.ExpiredProposal) {
        Timber.tag(TAG).d("onProposalExpired")
    }

    override fun onRequestExpired(request: Modal.Model.ExpiredRequest) {
        Timber.tag(TAG).d("onRequestExpired")
    }

    override fun onSessionApproved(approvedSession: Modal.Model.ApprovedSession) {
        Timber.tag(TAG).d("onSessionApproved")
    }

    override fun onSessionDelete(deletedSession: Modal.Model.DeletedSession) {
        Timber.tag(TAG).d("onSessionDelete")
    }

    override fun onSessionEvent(sessionEvent: Modal.Model.SessionEvent) {
        Timber.tag(TAG).d("onSessionEvent")
    }

    override fun onSessionExtend(session: Modal.Model.Session) {
        Timber.tag(TAG).d("onSessionExtend")
    }

    override fun onSessionRejected(rejectedSession: Modal.Model.RejectedSession) {
        Timber.tag(TAG).d("onSessionRejected")
    }

    override fun onSessionRequestResponse(response: Modal.Model.SessionRequestResponse) {
        Timber.tag(TAG).d("onSessionRequestResponse")
    }

    override fun onSessionUpdate(updatedSession: Modal.Model.UpdatedSession) {
        Timber.tag(TAG).d("onSessionUpdate")
    }

    companion object {
        const val TAG = "AssetTrackerApp"
    }
}
