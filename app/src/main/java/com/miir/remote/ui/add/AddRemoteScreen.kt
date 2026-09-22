package com.miir.remote.ui.add

import androidx.activity.compose.BackHandler
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
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.ArrowForward
import androidx.compose.material.icons.filled.Autorenew
import androidx.compose.material.icons.filled.Stop
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilledTonalButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.RadioButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.lifecycle.viewmodel.initializer
import androidx.lifecycle.viewmodel.viewModelFactory
import com.miir.remote.AppContainer
import com.miir.remote.ir.DeviceType
import com.miir.remote.ir.IrCodeDatabase
import com.miir.remote.ir.RemoteKey
import com.miir.remote.ui.components.RemoteKeyGrid
import com.miir.remote.ui.components.deviceTypeIcon

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AddRemoteScreen(
    onSaved: () -> Unit,
    onBack: () -> Unit,
    container: AppContainer
) {
    val vm: AddRemoteViewModel = viewModel(
        factory = viewModelFactory {
            initializer {
                AddRemoteViewModel(
                    container.repository,
                    container.irTransmitter,
                    container.irCodeDatabase
                )
            }
        }
    )
    val step by vm.step.collectAsState()
    val type by vm.type.collectAsState()
    val codeSets by vm.codeSets.collectAsState()
    val currentIndex by vm.currentIndex.collectAsState()
    val autoMatching by vm.autoMatching.collectAsState()
    val name by vm.remoteName.collectAsState()
    val groupId by vm.groupId.collectAsState()
    val groups by vm.groups.collectAsState()

    val title = when (step) {
        AddRemoteViewModel.Step.TYPE -> "选择设备类型"
        AddRemoteViewModel.Step.BRAND -> "选择品牌"
        AddRemoteViewModel.Step.TEST -> "试码匹配"
        AddRemoteViewModel.Step.SAVE -> "保存遥控器"
    }

    BackHandler(enabled = step != AddRemoteViewModel.Step.TYPE) {
        if (!vm.back()) onBack()
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(title) },
                navigationIcon = {
                    IconButton(onClick = {
                        if (!vm.back()) onBack()
                    }) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "返回")
                    }
                }
            )
        }
    ) { padding ->
        Box(modifier = Modifier.fillMaxSize().padding(padding)) {
            when (step) {
                AddRemoteViewModel.Step.TYPE -> TypeStep(onSelect = vm::selectType)

                AddRemoteViewModel.Step.BRAND -> type?.let {
                    BrandStep(
                        type = it,
                        codes = container.irCodeDatabase,
                        onSelect = vm::selectBrand
                    )
                }

                AddRemoteViewModel.Step.TEST -> type?.let {
                    TestStep(
                        type = it,
                        hasIr = vm.hasIrEmitter,
                        codeSets = codeSets,
                        currentIndex = currentIndex,
                        autoMatching = autoMatching,
                        onPrev = vm::prevCodeSet,
                        onNext = vm::nextCodeSet,
                        onToggleAutoMatch = vm::toggleAutoMatch,
                        onKey = vm::transmitKey,
                        onSave = vm::goNextToSave
                    )
                }

                AddRemoteViewModel.Step.SAVE -> SaveStep(
                    name = name,
                    onNameChange = vm::onNameChange,
                    groups = groups,
                    selectedGroupId = groupId,
                    onSelectGroup = vm::selectGroup,
                    onSave = { vm.save(onSaved) }
                )
            }
        }
    }
}

@OptIn(ExperimentalLayoutApi::class)
@Composable
private fun TypeStep(onSelect: (DeviceType) -> Unit) {
    FlowRow(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        horizontalArrangement = Arrangement.spacedBy(12.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        DeviceType.entries.forEach { type ->
            Card(
                modifier = Modifier.width(96.dp),
                shape = RoundedCornerShape(20.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
                onClick = { onSelect(type) }
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
                    Text(type.displayName, style = MaterialTheme.typography.labelLarge)
                }
            }
        }
    }
}

@Composable
private fun BrandStep(
    type: DeviceType,
    codes: IrCodeDatabase,
    onSelect: (String) -> Unit
) {
    LazyColumn(
        modifier = Modifier.fillMaxSize(),
        contentPadding = androidx.compose.foundation.layout.PaddingValues(
            start = 16.dp,
            end = 16.dp,
            top = 8.dp,
            bottom = 24.dp
        ),
        verticalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        items(codes.brandsFor(type), key = { it.id }) { brand ->
            val modelCount = codes.codeSetsFor(brand.id).size
            Card(
                modifier = Modifier.fillMaxWidth(),
                onClick = { onSelect(brand.id) }
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(
                        imageVector = deviceTypeIcon(type),
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.primary
                    )
                    Spacer(Modifier.width(16.dp))
                    Column(Modifier.weight(1f)) {
                        Text(brand.name, style = MaterialTheme.typography.bodyLarge)
                        Text(
                            text = "$modelCount 组码表",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun TestStep(
    type: DeviceType,
    hasIr: Boolean,
    codeSets: List<com.miir.remote.ir.CodeSet>,
    currentIndex: Int,
    autoMatching: Boolean,
    onPrev: () -> Unit,
    onNext: () -> Unit,
    onToggleAutoMatch: () -> Unit,
    onKey: (RemoteKey) -> Unit,
    onSave: () -> Unit
) {
    val cs = codeSets.getOrNull(currentIndex)
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp)
    ) {
        if (!hasIr) {
            Text(
                text = "当前设备没有红外发射器，按键将不会发射红外信号。",
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.error,
                modifier = Modifier.padding(bottom = 8.dp)
            )
        }

        // 当前码集信息 + 切换控件
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .padding(bottom = 12.dp),
            colors = CardDefaults.cardColors(
                containerColor = MaterialTheme.colorScheme.surfaceVariant
            )
        ) {
            Column(modifier = Modifier.padding(12.dp)) {
                Text(
                    text = "当前码表 ${currentIndex + 1}/${codeSets.size}",
                    style = MaterialTheme.typography.labelMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                Text(
                    text = cs?.name ?: "无可用码表",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold
                )
                if (cs != null && !cs.isSupported) {
                    Text(
                        text = "协议 ${cs.protocol} 暂未实现，需手动添加码值后才能发射",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.error,
                        modifier = Modifier.padding(top = 4.dp)
                    )
                } else {
                    Text(
                        text = "协议 ${cs?.protocol}",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        modifier = Modifier.padding(top = 4.dp)
                    )
                }
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(top = 8.dp),
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    FilledTonalButton(onClick = onPrev, enabled = codeSets.size > 1) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = null)
                        Spacer(Modifier.width(4.dp))
                        Text("上一组")
                    }
                    FilledTonalButton(onClick = onNext, enabled = codeSets.size > 1) {
                        Text("下一组")
                        Spacer(Modifier.width(4.dp))
                        Icon(Icons.AutoMirrored.Filled.ArrowForward, contentDescription = null)
                    }
                    FilledTonalButton(
                        onClick = onToggleAutoMatch,
                        enabled = codeSets.size > 1
                    ) {
                        Icon(
                            imageVector = if (autoMatching) Icons.Filled.Stop
                            else Icons.Filled.Autorenew,
                            contentDescription = null
                        )
                        Spacer(Modifier.width(4.dp))
                        Text(if (autoMatching) "停止" else "自动匹配")
                    }
                }
            }
        }

        Text(
            text = "将手机红外发射器对准设备，按电源键测试；设备有反应后点击「保存遥控器」。",
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            modifier = Modifier.padding(bottom = 12.dp)
        )

        RemoteKeyGrid(type = type, onKey = onKey)

        Spacer(Modifier.height(16.dp))
        Button(onClick = onSave, modifier = Modifier.fillMaxWidth()) {
            Text("保存遥控器", fontWeight = FontWeight.Bold)
        }
    }
}

@Composable
private fun SaveStep(
    name: String,
    onNameChange: (String) -> Unit,
    groups: List<com.miir.remote.data.entity.GroupEntity>,
    selectedGroupId: Long?,
    onSelectGroup: (Long) -> Unit,
    onSave: () -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        OutlinedTextField(
            value = name,
            onValueChange = onNameChange,
            label = { Text("遥控器名称") },
            singleLine = true,
            modifier = Modifier.fillMaxWidth()
        )
        Text("选择分组", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
        LazyColumn(
            verticalArrangement = Arrangement.spacedBy(4.dp),
            modifier = Modifier.weight(1f)
        ) {
            items(groups, key = { it.id }) { group ->
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 4.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    RadioButton(
                        selected = selectedGroupId == group.id,
                        onClick = { onSelectGroup(group.id) }
                    )
                    Text(group.name)
                }
            }
        }
        Button(onClick = onSave, modifier = Modifier.fillMaxWidth()) {
            Text("保存", fontWeight = FontWeight.Bold)
        }
    }
}
