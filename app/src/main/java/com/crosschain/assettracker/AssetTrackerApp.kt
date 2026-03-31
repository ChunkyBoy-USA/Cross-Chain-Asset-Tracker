package com.crosschain.assettracker

import android.app.Application
import com.crosschain.assettracker.data.repository.CcipRepositoryImpl
import com.crosschain.assettracker.domain.model.Chain
import com.reown.android.Core
import com.reown.android.CoreClient
import com.reown.android.relay.ConnectionType
import com.reown.appkit.client.AppKit
import com.reown.appkit.client.Modal
import com.reown.appkit.presets.AppKitChainsPresets.ethToken
import com.reown.appkit.utils.EthUtils
import com.reown.appkit.utils.EthUtils.ethOptionalMethods
import com.reown.appkit.utils.EthUtils.ethRequiredMethods
import dagger.hilt.android.HiltAndroidApp
import timber.log.Timber
import javax.inject.Inject

@HiltAndroidApp
class AssetTrackerApp : Application() {
    @Inject
    lateinit var ccipRepository: CcipRepositoryImpl

    override fun onCreate() {
        super.onCreate()

        if (BuildConfig.DEBUG) {
            Timber.plant(Timber.DebugTree())
        }

        val projectId = BuildConfig.REOWN_PROJECT_ID
        val connectionType = ConnectionType.AUTOMATIC
        val appMetaData = Core.Model.AppMetaData(
            name = "Meta Mask",
            description = "My first Android dApp",
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
            },
            onError = { error ->
                Timber.tag(TAG).e("error = %s", error.throwable.message)
            }
        )

        // Just choose Ethereum Sepolia to get wallet address
        // because addresses will be identical on all EVM compatible chains
        val chains = listOf(
            Modal.Model.Chain(
                chainName = "Ethereum Sepolia",
                chainNamespace = Chain.ETHEREUM.chainNamespace,
                chainReference = Chain.ETHEREUM.chainReference,
                requiredMethods = ethRequiredMethods,
                optionalMethods = ethOptionalMethods,
                events = EthUtils.ethEvents,
                token = ethToken,
                rpcUrl = Chain.ETHEREUM.rpcUrl
            )
        )

        AppKit.setChains(chains)
        ccipRepository.initSignClientDelegate()
    }

    companion object {
        const val TAG = "AssetTrackerApp"
    }
}
