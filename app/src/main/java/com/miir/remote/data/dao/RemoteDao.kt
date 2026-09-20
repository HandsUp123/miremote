package com.miir.remote.data.dao

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.Query
import androidx.room.Update
import com.miir.remote.data.entity.RemoteEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface RemoteDao {

    @Query("SELECT * FROM remotes ORDER BY sortOrder ASC, createdAt ASC")
    fun observeAll(): Flow<List<RemoteEntity>>

    @Query("SELECT * FROM remotes WHERE groupId = :groupId ORDER BY sortOrder ASC, createdAt ASC")
    fun observeByGroup(groupId: Long): Flow<List<RemoteEntity>>

    @Query("SELECT * FROM remotes WHERE groupId = :groupId ORDER BY sortOrder ASC, createdAt ASC")
    suspend fun getByGroup(groupId: Long): List<RemoteEntity>

    @Query("SELECT * FROM remotes WHERE id = :id")
    suspend fun getById(id: Long): RemoteEntity?

    @Insert
    suspend fun insert(remote: RemoteEntity): Long

    @Update
    suspend fun update(remote: RemoteEntity)

    @Delete
    suspend fun delete(remote: RemoteEntity)

    @Query("UPDATE remotes SET groupId = :groupId, sortOrder = :sortOrder WHERE id = :id")
    suspend fun move(id: Long, groupId: Long, sortOrder: Int)

    @Query("UPDATE remotes SET name = :name WHERE id = :id")
    suspend fun rename(id: Long, name: String)
}
