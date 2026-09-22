package com.miir.remote.ui.control

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
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
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.lifecycle.viewmodel.initializer
import androidx.lifecycle.viewmodel.viewModelFactory
import com.miir.remote.AppContainer
import com.miir.remote.ir.DeviceType
import com.miir.remote.ui.components.RemoteKeyGrid

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun RemoteControlScreen(
    remoteId: Long,
    onBack: () -> Unit,
    onDeleted: () -> Unit,
    container: AppContainer
) {
    val vm: RemoteControlViewModel = viewModel(
        factory = viewModelFactory {
            initializer {
                RemoteControlViewModel(
                    container.repository,
                    container.irTransmitter,
                    container.irCodeDatabase,
                    remoteId
                )
            }
        }
    )
    val remote by vm.remote.collectAsState()
    val codeSetName = remember(remote) { vm.currentCodeSetName() }
    val codeSetSupported = remember(remote) { vm.isCodeSetSupported() }

    var showRename by remember { mutableStateOf(false) }
    var renameText by remember { mutableStateOf("") }
    var showDelete by remember { mutableStateOf(false) }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(remote?.name ?: "遥控器", fontWeight = FontWeight.Bold) },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "返回")
                    }
                },
                actions = {
                    IconButton(onClick = {
                        renameText = remote?.name.orEmpty()
                        showRename = true
                    }) {
                        Icon(Icons.Filled.Edit, contentDescription = "重命名")
                    }
                    IconButton(onClick = { showDelete = true }) {
                        Icon(Icons.Filled.Delete, contentDescription = "删除")
                    }
                }
            )
        }
    ) { padding ->
        val r = remote
        if (r == null) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(padding),
                contentAlignment = Alignment.Center
            ) { Text("加载中…") }
        } else {
            val type = DeviceType.fromId(r.deviceType)
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(padding)
                    .padding(16.dp)
                    .verticalScroll(rememberScrollState())
            ) {
                if (!vm.hasIrEmitter) {
                    Text(
                        text = "当前设备没有红外发射器，按键不会发射信号。",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.error,
                        modifier = Modifier.padding(bottom = 12.dp)
                    )
                }
                if (!codeSetSupported) {
                    Text(
                        text = "该遥控器码表协议暂未实现，按键不会发射。可在码库中替换为已支持的协议码值。",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.error,
                        modifier = Modifier.padding(bottom = 12.dp)
                    )
                }
                codeSetName?.let {
                    Text(
                        text = "码表：$it",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        modifier = Modifier.padding(bottom = 12.dp)
                    )
                }
                RemoteKeyGrid(type = type, onKey = vm::transmit)
                Spacer(Modifier.height(24.dp))
            }
        }
    }

    if (showRename) {
        AlertDialog(
            onDismissRequest = { showRename = false },
            confirmButton = {
                TextButton(onClick = {
                    if (renameText.isNotBlank()) vm.rename(renameText.trim())
                    showRename = false
                }) { Text("确定") }
            },
            dismissButton = { TextButton(onClick = { showRename = false }) { Text("取消") } },
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

    if (showDelete) {
        AlertDialog(
            onDismissRequest = { showDelete = false },
            confirmButton = {
                TextButton(onClick = {
                    showDelete = false
                    vm.delete(onDeleted)
                }) { Text("删除") }
            },
            dismissButton = { TextButton(onClick = { showDelete = false }) { Text("取消") } },
            title = { Text("删除遥控器") },
            text = { Text("确定删除「${remote?.name}」？") }
        )
    }
}
