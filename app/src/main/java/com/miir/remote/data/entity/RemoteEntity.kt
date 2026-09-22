package com.miir.remote.data.entity

import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey

/**
 * 单个遥控器，归属于某个分组。deviceType / brandId 对应 [com.miir.remote.ir] 中的码库，
 * modelId 为命中的 [com.miir.remote.ir.CodeSet.id]，控制屏据此取出码集发射红外。
 */
@Entity(
    tableName = "remotes",
    foreignKeys = [
        ForeignKey(
            entity = GroupEntity::class,
            parentColumns = ["id"],
            childColumns = ["groupId"],
            onDelete = ForeignKey.CASCADE
        )
    ],
    indices = [Index("groupId")]
)
data class RemoteEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val groupId: Long,
    val name: String,
    val deviceType: String,
    val brandId: String,
    val modelId: String = "",
    val sortOrder: Int = 0,
    val createdAt: Long = System.currentTimeMillis()
)
