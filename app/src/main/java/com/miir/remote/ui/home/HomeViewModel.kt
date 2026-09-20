package com.miir.remote.ui.home

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.miir.remote.data.entity.GroupEntity
import com.miir.remote.data.entity.RemoteEntity
import com.miir.remote.data.repository.RemoteRepository
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

data class GroupWithRemotes(
    val group: GroupEntity,
    val remotes: List<RemoteEntity>
)

data class HomeUiState(
    val groups: List<GroupWithRemotes> = emptyList(),
    val loading: Boolean = true
)

class HomeViewModel(private val repo: RemoteRepository) : ViewModel() {

    val uiState: StateFlow<HomeUiState> = combine(
        repo.observeGroups(),
        repo.observeRemotes()
    ) { groups, remotes ->
        HomeUiState(
            groups = groups.map { g -> GroupWithRemotes(g, remotes.filter { it.groupId == g.id }) },
            loading = false
        )
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), HomeUiState())

    init {
        viewModelScope.launch {
            if (repo.observeGroups().first().isEmpty()) {
                repo.addGroup("客厅")
                repo.addGroup("卧室")
            }
        }
    }

    fun renameRemote(id: Long, name: String) {
        viewModelScope.launch { repo.renameRemote(id, name) }
    }

    fun deleteRemote(remote: RemoteEntity) {
        viewModelScope.launch { repo.deleteRemote(remote) }
    }

    fun moveRemote(id: Long, groupId: Long) {
        viewModelScope.launch { repo.moveRemote(id, groupId) }
    }
}
