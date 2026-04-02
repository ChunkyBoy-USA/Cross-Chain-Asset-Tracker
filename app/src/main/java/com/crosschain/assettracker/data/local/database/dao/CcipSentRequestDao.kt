package com.crosschain.assettracker.data.local.database.dao

import android.R
import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.crosschain.assettracker.data.local.database.entity.CcipSentRequestEntity
import com.crosschain.assettracker.domain.model.TransferStatus
import kotlinx.coroutines.flow.Flow

@Dao
interface CcipSentRequestDao {
    @Query("SELECT * FROM ccip_sent_request WHERE sessionTopic = :sessionTopic AND requestId = :requestId LIMIT 1")
    fun getCcipSentRequest(sessionTopic: String, requestId: String): Flow<CcipSentRequestEntity>

    @Query("SELECT * FROM ccip_sent_request")
    fun getCcipSentRequests(): Flow<List<CcipSentRequestEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertCcipSentRequest(ccipSentRequest: CcipSentRequestEntity)

    @Query("UPDATE ccip_sent_request SET txHash = :txHash WHERE sessionTopic = :sessionTopic")
    suspend fun updateCcipSentRequestTxHash(txHash: String, sessionTopic: String)

    @Query("DELETE FROM ccip_sent_request")
    suspend fun clearAllCcipSentRequests()

    @Query("UPDATE ccip_sent_request SET ccipMessageId = :ccipMessageId, status = :newStatus WHERE txHash = :txHash")
    suspend fun insertCcipSentRequestMessageId(ccipMessageId: String?, txHash: String, newStatus: TransferStatus)
}