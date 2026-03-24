package com.crosschain.assettracker

import android.app.Application
import com.reown.android.Core
import com.reown.android.CoreClient
import com.reown.android.relay.ConnectionType
import com.reown.walletkit.client.Wallet
import com.reown.walletkit.client.WalletKit
import dagger.hilt.android.HiltAndroidApp
import timber.log.Timber

@HiltAndroidApp
class AssetTrackerApp : Application() {
    override fun onCreate() {
        super.onCreate()

        val projectId = "359dbd68680e3f40b7e45fd1f03a5200"
        val connectionType = ConnectionType.AUTOMATIC
        val telemetryEnabled: Boolean = true
        val appMetaData = Core.Model.AppMetaData(
            name = "Meta Mask",
            description = "Wallet Description",
            url = "Wallet URL",
            icons = listOf("Wallet Icon URL"),
            redirect = "assert-tracker://request" // Custom Redirect URI
        )

        CoreClient.initialize(
            projectId = projectId,
            connectionType = connectionType,
            application = this, metaData = appMetaData,
            telemetryEnabled = telemetryEnabled,
            onError = { error ->
                Timber.tag(TAG).e(error.toString())
            }
        )

        val initParams = Wallet.Params.Init(core = CoreClient)

        WalletKit.initialize(initParams) { error ->
            // Error will be thrown if there's an issue during initialization
            Timber.tag(TAG).e(error.toString())
        }
    }

    companion object {
        const val TAG = "AssetTrackerApp"
    }
}
