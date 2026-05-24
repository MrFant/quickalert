package com.quickalert.app.data

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import kotlinx.coroutines.flow.Flow

@Dao
interface AlertRuleDao {

    @Query("SELECT * FROM alert_rules ORDER BY createdAt DESC")
    fun getAllRules(): Flow<List<AlertRule>>

    @Query("SELECT * FROM alert_rules WHERE enabled = 1")
    suspend fun getEnabledRules(): List<AlertRule>

    @Query("SELECT * FROM alert_rules WHERE id = :id")
    suspend fun getRuleById(id: Long): AlertRule?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(rule: AlertRule): Long

    @Update
    suspend fun update(rule: AlertRule)

    @Delete
    suspend fun delete(rule: AlertRule)

    @Query("DELETE FROM alert_rules WHERE id = :id")
    suspend fun deleteById(id: Long)

    @Query("UPDATE alert_rules SET enabled = :enabled WHERE id = :id")
    suspend fun setEnabled(id: Long, enabled: Boolean)
}
