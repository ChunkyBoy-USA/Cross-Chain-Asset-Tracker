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

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertPendingRouterAllowance(pendingRouterAllowance: PendingRouterAllowanceEntity)

    @Query("SELECT * FROM pending_router_allowance WHERE requestId = :requestId LIMIT 1")
    fun getPendingRouterAllowance(requestId: String): Flow<PendingRouterAllowanceEntity>

    @Query("DELETE FROM pending_router_allowance WHERE requestId = :requestId")
    suspend fun deletePendingRouterAllowance(requestId: String)
}