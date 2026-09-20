package com.miir.remote.ui.group

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.miir.remote.data.entity.GroupEntity
import com.miir.remote.data.repository.RemoteRepository
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

data class GroupItem(
    val group: GroupEntity,
    val remoteCount: Int
)

class GroupManageViewModel(private val repo: RemoteRepository) : ViewModel() {

    val items: StateFlow<List<GroupItem>> = combine(
        repo.observeGroups(),
        repo.observeRemotes()
    ) { groups, remotes ->
        groups.map { g -> GroupItem(g, remotes.count { it.groupId == g.id }) }
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), emptyList())

    fun addGroup(name: String) {
        viewModelScope.launch { repo.addGroup(name) }
    }

    fun renameGroup(id: Long, name: String) {
        viewModelScope.launch { repo.renameGroup(id, name) }
    }

    fun deleteGroup(group: GroupEntity) {
        viewModelScope.launch { repo.deleteGroup(group) }
    }
}
