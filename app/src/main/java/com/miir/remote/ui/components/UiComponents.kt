package com.miir.remote.ui.components

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AcUnit
import androidx.compose.material.icons.filled.Cast
import androidx.compose.material.icons.filled.Disc
import androidx.compose.material.icons.filled.Lightbulb
import androidx.compose.material.icons.filled.LiveTv
import androidx.compose.material.icons.filled.Speaker
import androidx.compose.material.icons.filled.Toys
import androidx.compose.material.icons.filled.Tv
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.miir.remote.ir.DeviceType

/** 设备类型对应的图标。 */
fun deviceTypeIcon(type: DeviceType): ImageVector = when (type) {
    DeviceType.TV -> Icons.Filled.Tv
    DeviceType.SET_TOP_BOX -> Icons.Filled.LiveTv
    DeviceType.AC -> Icons.Filled.AcUnit
    DeviceType.FAN -> Icons.Filled.Toys
    DeviceType.PROJECTOR -> Icons.Filled.Cast
    DeviceType.AUDIO -> Icons.Filled.Speaker
    DeviceType.DVD -> Icons.Filled.Disc
    DeviceType.LIGHT -> Icons.Filled.Lightbulb
}

/**
 * 遥控器按键样式：圆角方块，按下时触发 onClick。
 * @param highlighted 是否高亮（电源等主要按键）
 */
@Composable
fun RemoteButton(
    label: String,
    modifier: Modifier = Modifier,
    highlighted: Boolean = false,
    enabled: Boolean = true,
    onClick: () -> Unit
) {
    val containerColor = if (highlighted) {
        MaterialTheme.colorScheme.primary
    } else {
        MaterialTheme.colorScheme.surfaceVariant
    }
    val contentColor = if (highlighted) {
        MaterialTheme.colorScheme.onPrimary
    } else {
        MaterialTheme.colorScheme.onSurfaceVariant
    }
    Surface(
        modifier = modifier,
        shape = RoundedCornerShape(16.dp),
        color = if (enabled) containerColor else containerColor.copy(alpha = 0.5f),
        contentColor = contentColor,
        tonalElevation = 2.dp,
        shadowElevation = 2.dp,
        enabled = enabled,
        onClick = onClick
    ) {
        Box(
            contentAlignment = Alignment.Center,
            modifier = Modifier.padding(vertical = 16.dp, horizontal = 8.dp)
        ) {
            Text(
                text = label,
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Medium,
                maxLines = 1
            )
        }
    }
}
