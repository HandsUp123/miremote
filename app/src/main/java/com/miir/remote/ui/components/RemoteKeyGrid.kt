package com.miir.remote.ui.components

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.RowScope
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.miir.remote.ir.DeviceType
import com.miir.remote.ir.RemoteKey
import com.miir.remote.ir.RemoteLayouts

/**
 * 根据设备类型渲染按键网格。空位用 [Spacer] 占位以保持视觉对齐。
 * 按下任意按键回调 [onKey]（由调用方决定是否发射红外）。
 */
@Composable
fun RemoteKeyGrid(
    type: DeviceType,
    onKey: (RemoteKey) -> Unit
) {
    Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
        RemoteLayouts.layout(type).forEach { row ->
            Row(
                horizontalArrangement = Arrangement.spacedBy(12.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                row.forEach { key -> KeyCell(key, onKey) }
            }
        }
    }
}

@Composable
private fun RowScope.KeyCell(key: RemoteKey?, onKey: (RemoteKey) -> Unit) {
    if (key == null) {
        Spacer(Modifier.weight(1f))
    } else {
        RemoteButton(
            label = key.label,
            modifier = Modifier.weight(1f),
            highlighted = key == RemoteKey.POWER,
            onClick = { onKey(key) }
        )
    }
}
