package com.miir.remote.ui.home

import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.combinedClickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.CreateNewFolder
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExtendedFloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.RadioButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.lifecycle.viewmodel.initializer
import androidx.lifecycle.viewmodel.viewModelFactory
import com.miir.remote.AppContainer
import com.miir.remote.data.entity.RemoteEntity
import com.miir.remote.ir.DeviceType
import com.miir.remote.ui.components.deviceTypeIcon

@Composable
fun HomeScreen(
    onAddRemote: () -> Unit,
    onManageGroups: () -> Unit,
    onOpenControl: (Long) -> Unit,
    container: AppContainer
) {
    val vm: HomeViewModel = viewModel(
        factory = viewModelFactory { initializer { HomeViewModel(container.repository) } }
    )
    val state by vm.uiState.collectAsState()

    var optionsRemote by remember { mutableStateOf<RemoteEntity?>(null) }
    var renameTarget by remember { mutableStateOf<RemoteEntity?>(null) }
    var renameText by remember { mutableStateOf("") }
    var moveTarget by remember { mutableStateOf<RemoteEntity?>(null) }
    var moveGroupId by remember { mutableStateOf<Long?>(null) }
    var deleteTarget by remember { mutableStateOf<RemoteEntity?>(null) }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("小米遥控", fontWeight = FontWeight.Bold) },
                actions = {
                    IconButton(onClick = onManageGroups) {
                        Icon(Icons.Filled.CreateNewFolder, contentDescription = "管理分组")
                    }
                    IconButton(onClick = onAddRemote) {
                        Icon(Icons.Filled.Add, contentDescription = "添加遥控器")
                    }
                }
            )
        },
        floatingActionButton = {
            ExtendedFloatingActionButton(
                onClick = onAddRemote,
                icon = { Icon(Icons.Filled.Add, contentDescription = null) },
                text = { Text("添加遥控") }
            )
        }
    ) { padding ->
        if (state.groups.isEmpty() && !state.loading) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(padding),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    "点击右下角添加你的第一个遥控器",
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        } else {
            LazyColumn(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(padding),
                contentPadding = androidx.compose.foundation.layout.PaddingValues(
                    start = 16.dp,
                    end = 16.dp,
                    top = 8.dp,
                    bottom = 96.dp
                ),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                items(state.groups, key = { it.group.id }) { gwr ->
                    GroupSection(
                        groupWithRemotes = gwr,
                        onOpenControl = onOpenControl,
                        onLongPress = { optionsRemote = it }
                    )
                }
            }
        }
    }

    // 长按弹出的操作菜单
    optionsRemote?.let { remote ->
        AlertDialog(
            onDismissRequest = { optionsRemote = null },
            confirmButton = {},
            title = { Text(remote.name) },
            text = {
                Column {
                    OptionRow("重命名") {
                        renameTarget = remote
                        renameText = remote.name
                        optionsRemote = null
                    }
                    OptionRow("移动到分组") {
                        moveTarget = remote
                        moveGroupId = remote.groupId
                        optionsRemote = null
                    }
                    OptionRow("删除") {
                        deleteTarget = remote
                        optionsRemote = null
                    }
                }
            }
        )
    }

    // 重命名
    renameTarget?.let { remote ->
        AlertDialog(
            onDismissRequest = { renameTarget = null },
            confirmButton = {
                TextButton(onClick = {
                    if (renameText.isNotBlank()) vm.renameRemote(remote.id, renameText.trim())
                    renameTarget = null
                }) { Text("确定") }
            },
            dismissButton = { TextButton(onClick = { renameTarget = null }) { Text("取消") } },
            title = { Text("重命名遥控器") },
            text = {
                OutlinedTextField(
                    value = renameText,
                    onValueChange = { renameText = it },
                    singleLine = true,
                    label = { Text("遥控器名称") }
                )
            }
        )
    }

    // 移动分组
    moveTarget?.let { remote ->
        AlertDialog(
            onDismissRequest = { moveTarget = null },
            confirmButton = {
                TextButton(onClick = {
                    moveGroupId?.let { gid -> vm.moveRemote(remote.id, gid) }
                    moveTarget = null
                }) { Text("确定") }
            },
            dismissButton = { TextButton(onClick = { moveTarget = null }) { Text("取消") } },
            title = { Text("移动到分组") },
            text = {
                Column {
                    state.groups.forEach { gwr ->
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(vertical = 4.dp)
                        ) {
                            RadioButton(
                                selected = moveGroupId == gwr.group.id,
                                onClick = { moveGroupId = gwr.group.id }
                            )
                            Text(gwr.group.name)
                        }
                    }
                }
            }
        )
    }

    // 删除确认
    deleteTarget?.let { remote ->
        AlertDialog(
            onDismissRequest = { deleteTarget = null },
            confirmButton = {
                TextButton(onClick = {
                    vm.deleteRemote(remote)
                    deleteTarget = null
                }) { Text("删除") }
            },
            dismissButton = { TextButton(onClick = { deleteTarget = null }) { Text("取消") } },
            title = { Text("删除遥控器") },
            text = { Text("确定删除「${remote.name}」？") }
        )
    }
}

@Composable
private fun OptionRow(text: String, onClick: () -> Unit) {
    Text(
        text = text,
        modifier = Modifier
            .fillMaxWidth()
            .combinedClickable(onClick = onClick)
            .padding(vertical = 12.dp),
        style = MaterialTheme.typography.bodyLarge
    )
}

@OptIn(ExperimentalLayoutApi::class)
@Composable
private fun GroupSection(
    groupWithRemotes: GroupWithRemotes,
    onOpenControl: (Long) -> Unit,
    onLongPress: (RemoteEntity) -> Unit
) {
    Column {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = 4.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = groupWithRemotes.group.name,
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold
            )
            Spacer(Modifier.width(8.dp))
            Text(
                text = "${groupWithRemotes.remotes.size} 个",
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
        if (groupWithRemotes.remotes.isEmpty()) {
            Text(
                text = "该分组还没有遥控器",
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                modifier = Modifier.padding(vertical = 8.dp)
            )
        } else {
            FlowRow(
                horizontalArrangement = Arrangement.spacedBy(12.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                groupWithRemotes.remotes.forEach { remote ->
                    RemoteCard(
                        remote = remote,
                        onClick = { onOpenControl(remote.id) },
                        onLongClick = { onLongPress(remote) }
                    )
                }
            }
        }
    }
}

@OptIn(ExperimentalFoundationApi::class)
@Composable
private fun RemoteCard(
    remote: RemoteEntity,
    onClick: () -> Unit,
    onLongClick: () -> Unit
) {
    val type = DeviceType.fromId(remote.deviceType)
    Card(
        modifier = Modifier
            .width(96.dp)
            .combinedClickable(onClick = onClick, onLongClick = onLongClick),
        shape = RoundedCornerShape(20.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Column(
            modifier = Modifier
                .padding(12.dp)
                .height(96.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            Icon(
                imageVector = deviceTypeIcon(type),
                contentDescription = type.displayName,
                tint = MaterialTheme.colorScheme.primary,
                modifier = Modifier.size(32.dp)
            )
            Spacer(Modifier.height(8.dp))
            Text(
                text = remote.name,
                style = MaterialTheme.typography.labelLarge,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
            Text(
                text = type.displayName,
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                maxLines = 1
            )
        }
    }
}
