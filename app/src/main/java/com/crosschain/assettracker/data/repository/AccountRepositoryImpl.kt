package com.crosschain.assettracker.data.repository

import com.crosschain.assettracker.constants.AccountConstants
import com.crosschain.assettracker.data.local.LocalDataRepository
import com.crosschain.assettracker.domain.AccountRepository
import com.reown.appkit.client.AppKit
import timber.log.Timber
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class AccountRepositoryImpl @Inject constructor(
    val localDataRepository: LocalDataRepository
) : AccountRepository {
    override fun loadAccounts(): Boolean {
        AppKit.getAccount()?.let { account ->
            Timber.tag(TAG).d("Account address loaded: ${account.address}")
            localDataRepository.saveString(AccountConstants.ACCOUNT_ADDRESS_KEY, account.address)
            return true
        }
        return false
    }

    companion object {
        const val TAG = "AccountRepositoryImpl"
    }

}