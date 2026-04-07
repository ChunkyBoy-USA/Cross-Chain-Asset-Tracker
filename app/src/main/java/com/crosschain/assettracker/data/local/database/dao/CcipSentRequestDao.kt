package com.crosschain.assettracker.data.local.database.dao

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

    @Query("UPDATE ccip_sent_request SET txHash = :txHash WHERE requestId = :requestId")
    suspend fun insertCcipTransferTxHash(txHash: String, requestId: String)

    @Query("DELETE FROM ccip_sent_request")
    suspend fun clearAllCcipSentRequests()

    @Query("DELETE FROM ccip_sent_request WHERE requestId = :requestId")
    suspend fun deleteCcipSentRequest(requestId: String)

    @Query("UPDATE ccip_sent_request SET ccipMessageId = :ccipMessageId, status = :newStatus, sequenceNumber = :sequenceNumber WHERE txHash = :txHash")
    suspend fun insertCcipSentRequestMessageIdAndSequenceNumber(sequenceNumber: String?, ccipMessageId: String?, txHash: String, newStatus: TransferStatus)

    @Query("UPDATE ccip_sent_request SET offRampAddress = :offRampAddress WHERE txHash = :txHash")
    suspend fun insertCcipOffRampAddress(txHash: String, offRampAddress: String?)
}