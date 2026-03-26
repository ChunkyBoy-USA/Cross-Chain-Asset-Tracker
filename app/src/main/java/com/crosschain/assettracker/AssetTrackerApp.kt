package com.crosschain.assettracker

import android.app.Application
import com.reown.android.Core
import com.reown.android.CoreClient
import com.reown.android.relay.ConnectionType
import com.reown.appkit.client.AppKit
import com.reown.appkit.client.Modal
import com.reown.appkit.presets.AppKitChainsPresets
import dagger.hilt.android.HiltAndroidApp
import timber.log.Timber

@HiltAndroidApp
class AssetTrackerApp : Application() {
    override fun onCreate() {
        super.onCreate()

        val projectId = "0e054c704861974befea7f3d29ab6612"
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


        AppKit.initialize(
            init = Modal.Params.Init(CoreClient),
            onSuccess = {
                Timber.tag(TAG).d("initialize onSuccess")
            },
            onError = { error ->
                Timber.tag(TAG).e("error = %s", error.throwable.message)
            }
        )

        AppKit.setChains(AppKitChainsPresets.ethChains.values.toList())
    }

    companion object {
        const val TAG = "AssetTrackerApp"
    }
}
