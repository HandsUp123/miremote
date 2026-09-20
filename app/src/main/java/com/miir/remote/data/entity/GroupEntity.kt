package com.miir.remote.data.entity

import androidx.room.Entity
import androidx.room.PrimaryKey

/**
 * 遥控分组（例如：客厅、卧室、办公室）。
 */
@Entity(tableName = "remote_groups")
data class GroupEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val name: String,
    val sortOrder: Int = 0,
    val createdAt: Long = System.currentTimeMillis()
)
