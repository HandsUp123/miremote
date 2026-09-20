package com.miir.remote.data.dao

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.Query
import androidx.room.Update
import com.miir.remote.data.entity.GroupEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface GroupDao {

    @Query("SELECT * FROM remote_groups ORDER BY sortOrder ASC, createdAt ASC")
    fun observeAll(): Flow<List<GroupEntity>>

    @Query("SELECT * FROM remote_groups ORDER BY sortOrder ASC, createdAt ASC")
    suspend fun getAll(): List<GroupEntity>

    @Insert
    suspend fun insert(group: GroupEntity): Long

    @Update
    suspend fun update(group: GroupEntity)

    @Delete
    suspend fun delete(group: GroupEntity)

    @Query("UPDATE remote_groups SET name = :name WHERE id = :id")
    suspend fun rename(id: Long, name: String)
}
