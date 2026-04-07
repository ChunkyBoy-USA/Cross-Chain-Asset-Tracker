package com.crosschain.assettracker.data.local.database.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.crosschain.assettracker.data.local.database.entity.PendingRouterAllowanceEntity
import com.crosschain.assettracker.data.local.database.entity.PendingTransactionEntity
import com.crosschain.assettracker.data.local.database.entity.RouterAllowanceEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface PendingTransactionDao {
    @Query("SELECT * FROM pending_transaction WHERE requestId = :requestId LIMIT 1")
    suspend fun getPendingTransaction(requestId: String): PendingTransactionEntity?

    @Query("SELECT * FROM pending_transaction WHERE requestId = :requestId")
    fun getPendingTransactionFlow(requestId: String): Flow<PendingTransactionEntity?>

    @Query("SELECT * FROM pending_transaction")
    fun getPendingTransactionFlow(): Flow<PendingTransactionEntity?>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertPendingTransactionEntity(pendingTransactionEntity: PendingTransactionEntity)

    @Query("DELETE FROM pending_transaction WHERE requestId = :requestId")
    suspend fun deletePendingTransactionEntity(requestId: String)
    @Query("UPDATE pending_transaction SET txHash = :txHash WHERE requestId = :requestId")
    suspend fun insertPendingTransactionTxHash(txHash: String?, requestId: String)
}