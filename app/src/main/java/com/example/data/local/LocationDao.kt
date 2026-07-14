package com.example.data.local

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Transaction
import kotlinx.coroutines.flow.Flow

@Dao
interface LocationDao {
    @Query("SELECT * FROM saved_locations ORDER BY timestamp DESC")
    fun getAllLocationsFlow(): Flow<List<SavedLocation>>

    @Query("SELECT * FROM saved_locations WHERE isCurrent = 1 LIMIT 1")
    fun getCurrentLocationFlow(): Flow<SavedLocation?>

    @Query("SELECT * FROM saved_locations WHERE isCurrent = 1 LIMIT 1")
    suspend fun getCurrentLocationDirect(): SavedLocation?

    @Query("SELECT * FROM saved_locations WHERE name = :name AND ABS(latitude - :lat) < 0.01 AND ABS(longitude - :lon) < 0.01 LIMIT 1")
    suspend fun findLocation(name: String, lat: Double, lon: Double): SavedLocation?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertLocation(location: SavedLocation): Long

    @Delete
    suspend fun deleteLocation(location: SavedLocation)

    @Query("UPDATE saved_locations SET isCurrent = 0")
    suspend fun clearCurrentStatus()

    @Query("UPDATE saved_locations SET isCurrent = 1, timestamp = :timestamp WHERE id = :id")
    suspend fun setCurrentStatusDirect(id: Int, timestamp: Long)

    @Transaction
    suspend fun selectLocation(locationId: Int) {
        clearCurrentStatus()
        setCurrentStatusDirect(locationId, System.currentTimeMillis())
    }

    @Transaction
    suspend fun saveAndSelectLocation(location: SavedLocation) {
        clearCurrentStatus()
        val existing = findLocation(location.name, location.latitude, location.longitude)
        if (existing != null) {
            setCurrentStatusDirect(existing.id, System.currentTimeMillis())
        } else {
            insertLocation(location.copy(isCurrent = true, timestamp = System.currentTimeMillis()))
        }
    }
}
