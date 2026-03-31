package com.crosschain.assettracker.data.local.database.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import com.crosschain.assettracker.data.local.database.entity.CcipSentRequestEntity
import com.crosschain.assettracker.data.local.database.entity.RebaseTokenMetadataEntity
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
}