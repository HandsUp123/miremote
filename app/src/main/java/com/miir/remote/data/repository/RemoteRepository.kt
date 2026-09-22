package com.miir.remote.data.repository

import com.miir.remote.data.dao.GroupDao
import com.miir.remote.data.dao.RemoteDao
import com.miir.remote.data.entity.GroupEntity
import com.miir.remote.data.entity.RemoteEntity
import kotlinx.coroutines.flow.Flow

/**
 * 分组与遥控器的统一数据入口。
 */
class RemoteRepository(
    private val groupDao: GroupDao,
    private val remoteDao: RemoteDao
) {

    fun observeGroups(): Flow<List<GroupEntity>> = groupDao.observeAll()
    fun observeRemotes(): Flow<List<RemoteEntity>> = remoteDao.observeAll()
    fun observeRemotesByGroup(groupId: Long): Flow<List<RemoteEntity>> = remoteDao.observeByGroup(groupId)

    suspend fun getGroup(id: Long): GroupEntity? = groupDao.getAll().firstOrNull { it.id == id }
    suspend fun getGroups(): List<GroupEntity> = groupDao.getAll()
    suspend fun getRemote(id: Long): RemoteEntity? = remoteDao.getById(id)

    suspend fun addGroup(name: String): Long =
        groupDao.insert(GroupEntity(name = name, sortOrder = nextGroupOrder()))

    suspend fun renameGroup(id: Long, name: String) = groupDao.rename(id, name)

    suspend fun deleteGroup(group: GroupEntity) = groupDao.delete(group)

    suspend fun addRemote(
        groupId: Long,
        name: String,
        deviceType: String,
        brandId: String,
        modelId: String
    ): Long = remoteDao.insert(
        RemoteEntity(
            groupId = groupId,
            name = name,
            deviceType = deviceType,
            brandId = brandId,
            modelId = modelId,
            sortOrder = nextRemoteOrder(groupId)
        )
    )

    suspend fun renameRemote(id: Long, name: String) = remoteDao.rename(id, name)

    suspend fun deleteRemote(remote: RemoteEntity) = remoteDao.delete(remote)

    suspend fun moveRemote(id: Long, groupId: Long) =
        remoteDao.move(id, groupId, nextRemoteOrder(groupId))

    private suspend fun nextGroupOrder(): Int =
        (groupDao.getAll().maxOfOrNull { it.sortOrder } ?: -1) + 1

    private suspend fun nextRemoteOrder(groupId: Long): Int =
        (remoteDao.getByGroup(groupId).maxOfOrNull { it.sortOrder } ?: -1) + 1
}
