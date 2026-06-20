package com.profilefinder.data.db

import androidx.room.*
import com.profilefinder.data.models.AnalysisResultEntity
import com.profilefinder.data.models.SearchHistoryEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface SearchHistoryDao {
    @Query("SELECT * FROM search_history ORDER BY timestamp DESC LIMIT 50")
    fun getAll(): Flow<List<SearchHistoryEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(entity: SearchHistoryEntity)

    @Query("DELETE FROM search_history WHERE id = :id")
    suspend fun delete(id: Long)

    @Query("DELETE FROM search_history")
    suspend fun clearAll()
}

@Dao
interface AnalysisResultDao {
    @Query("SELECT * FROM analysis_results ORDER BY timestamp DESC LIMIT 20")
    fun getAll(): Flow<List<AnalysisResultEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(entity: AnalysisResultEntity)

    @Query("DELETE FROM analysis_results WHERE id = :id")
    suspend fun delete(id: Long)
}

@Database(
    entities = [SearchHistoryEntity::class, AnalysisResultEntity::class],
    version = 1,
    exportSchema = false
)
abstract class ProfileFinderDatabase : RoomDatabase() {
    abstract fun searchHistoryDao(): SearchHistoryDao
    abstract fun analysisResultDao(): AnalysisResultDao
}
