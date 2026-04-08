package com.crosschain.assettracker.data.local.database.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.crosschain.assettracker.data.local.database.entity.PendingRouterAllowanceEntity
import com.crosschain.assettracker.data.local.database.entity.RouterAllowanceEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface RouterAllowanceDao {
    @Query("SELECT * FROM router_allowance WHERE routerAddress = :routerAddress AND tokenAddress = :tokenAddress AND walletAddress = :walletAddress AND chainId = :chainId LIMIT 1")
    suspend fun getRouterAllowance(routerAddress: String, tokenAddress: String, walletAddress: String, chainId: String): RouterAllowanceEntity?

    @Query("SELECT * FROM router_allowance WHERE routerAddress = :routerAddress AND tokenAddress = :tokenAddress AND walletAddress = :walletAddress AND chainId = :chainId LIMIT 1")
    fun getRouterAllowanceFlow(routerAddress: String, tokenAddress: String, walletAddress: String, chainId: String): Flow<RouterAllowanceEntity?>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertRouterAllowance(routerAllowance: RouterAllowanceEntity)

    @Query("UPDATE router_allowance SET allowance = CAST(\n" +
            "(CAST(COALESCE(allowance, '0') AS LONG) + CAST(COALESCE(:allowanceApproved, '0') AS LONG)) \n" +
            "AS TEXT)\n WHERE routerAddress = :routerAddress AND walletAddress = :walletAddress \n" +
            "AND tokenAddress = :tokenAddress AND chainId = :chainId")
    suspend fun routerAllowanceApproved(routerAddress: String, tokenAddress: String, walletAddress: String, chainId: String, allowanceApproved: String)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertPendingRouterAllowance(pendingRouterAllowance: PendingRouterAllowanceEntity)

    @Query("UPDATE pending_router_allowance SET txHash = :txHash WHERE requestId = :requestId")
    suspend fun insertPendingRouterAllowanceTxHash(txHash: String, requestId: String)

    @Query("SELECT * FROM pending_router_allowance")
    fun getPendingRouterAllowances(): Flow<List<PendingRouterAllowanceEntity>>

    @Query("DELETE FROM pending_router_allowance WHERE requestId = :requestId")
    suspend fun deletePendingRouterAllowance(requestId: String)

    @Query("DELETE FROM router_allowance")
    suspend fun deleteAllRouterAllowance()
}