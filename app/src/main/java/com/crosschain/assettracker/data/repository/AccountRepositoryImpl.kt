package com.crosschain.assettracker.data.repository

import com.crosschain.assettracker.constants.AccountConstants
import com.crosschain.assettracker.data.local.EncryptedDataRepository
import com.crosschain.assettracker.domain.AccountRepository
import com.reown.appkit.client.AppKit
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.flowOn
import timber.log.Timber
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class AccountRepositoryImpl @Inject constructor(
    val encryptedDataRepository: EncryptedDataRepository
) : AccountRepository {
    override fun loadAccountFromAppKit() = flow {
        val account = AppKit.getAccount()
        if (account == null) {
            Timber.tag(TAG).d("Account loaded is null")
            emit(false)
        } else {
            Timber.tag(TAG).d("Account loaded: $account")
            encryptedDataRepository.saveString(AccountConstants.ACCOUNT_ADDRESS_PREF_KEY, account.address)
            emit(true)
        }
    }.flowOn(Dispatchers.IO)

    override fun getCurrentAccountAddress(): String? {
        return encryptedDataRepository.getString(AccountConstants.ACCOUNT_ADDRESS_PREF_KEY)
    }

    companion object {
        const val TAG = "AccountRepositoryImpl"
    }

}