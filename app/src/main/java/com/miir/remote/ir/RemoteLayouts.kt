package com.miir.remote.ir

/**
 * 各设备类型的按键布局，按行组织。null 表示空格（占位），用于呈现
 * 类似小米遥控器「方向键居中、音量/频道两侧」的视觉效果。
 */
object RemoteLayouts {

    fun layout(type: DeviceType): List<List<RemoteKey?>> = when (type) {
        DeviceType.TV -> listOf(
            listOf(RemoteKey.POWER, null, RemoteKey.MUTE),
            listOf(RemoteKey.VOL_UP, RemoteKey.UP, RemoteKey.CH_UP),
            listOf(RemoteKey.LEFT, RemoteKey.OK, RemoteKey.RIGHT),
            listOf(RemoteKey.VOL_DOWN, RemoteKey.DOWN, RemoteKey.CH_DOWN),
            listOf(RemoteKey.BACK, RemoteKey.HOME, RemoteKey.MENU),
            listOf(RemoteKey.NUM_1, RemoteKey.NUM_2, RemoteKey.NUM_3),
            listOf(RemoteKey.NUM_4, RemoteKey.NUM_5, RemoteKey.NUM_6),
            listOf(RemoteKey.NUM_7, RemoteKey.NUM_8, RemoteKey.NUM_9),
            listOf(RemoteKey.PREV_CH, RemoteKey.NUM_0, RemoteKey.GUIDE)
        )

        DeviceType.SET_TOP_BOX -> listOf(
            listOf(RemoteKey.POWER, null, RemoteKey.MUTE),
            listOf(RemoteKey.VOL_UP, RemoteKey.UP, RemoteKey.CH_UP),
            listOf(RemoteKey.LEFT, RemoteKey.OK, RemoteKey.RIGHT),
            listOf(RemoteKey.VOL_DOWN, RemoteKey.DOWN, RemoteKey.CH_DOWN),
            listOf(RemoteKey.BACK, RemoteKey.HOME, RemoteKey.MENU),
            listOf(RemoteKey.NUM_1, RemoteKey.NUM_2, RemoteKey.NUM_3),
            listOf(RemoteKey.NUM_4, RemoteKey.NUM_5, RemoteKey.NUM_6),
            listOf(RemoteKey.NUM_7, RemoteKey.NUM_8, RemoteKey.NUM_9),
            listOf(RemoteKey.PREV_CH, RemoteKey.NUM_0, null)
        )

        DeviceType.AC -> listOf(
            listOf(RemoteKey.POWER, null, RemoteKey.MODE),
            listOf(null, RemoteKey.TEMP_UP, null),
            listOf(RemoteKey.SWING, RemoteKey.FAN_SPEED, RemoteKey.TURBO),
            listOf(null, RemoteKey.TEMP_DOWN, null),
            listOf(RemoteKey.SLEEP, null, null)
        )

        DeviceType.FAN -> listOf(
            listOf(RemoteKey.POWER, null, null),
            listOf(RemoteKey.SPEED, RemoteKey.OSCILLATE, RemoteKey.TIMER),
            listOf(RemoteKey.WIND_TYPE, null, null)
        )

        DeviceType.PROJECTOR -> listOf(
            listOf(RemoteKey.POWER, null, RemoteKey.SOURCE),
            listOf(null, RemoteKey.UP, null),
            listOf(RemoteKey.LEFT, RemoteKey.OK, RemoteKey.RIGHT),
            listOf(null, RemoteKey.DOWN, null),
            listOf(RemoteKey.BACK, RemoteKey.HOME, RemoteKey.MENU),
            listOf(RemoteKey.MUTE, null, RemoteKey.SETTINGS)
        )

        DeviceType.AUDIO -> listOf(
            listOf(RemoteKey.POWER, null, RemoteKey.MUTE),
            listOf(RemoteKey.VOL_UP, RemoteKey.OK, RemoteKey.VOL_DOWN),
            listOf(RemoteKey.PREV, RemoteKey.PLAY, RemoteKey.NEXT),
            listOf(RemoteKey.LEFT, RemoteKey.PAUSE, RemoteKey.RIGHT),
            listOf(null, RemoteKey.STOP, null),
            listOf(null, RemoteKey.MENU, null)
        )

        DeviceType.DVD -> listOf(
            listOf(RemoteKey.POWER, null, RemoteKey.EJECT),
            listOf(null, RemoteKey.UP, null),
            listOf(RemoteKey.LEFT, RemoteKey.OK, RemoteKey.RIGHT),
            listOf(null, RemoteKey.DOWN, null),
            listOf(RemoteKey.MENU, RemoteKey.BACK, null),
            listOf(RemoteKey.PREV, RemoteKey.PLAY, RemoteKey.NEXT),
            listOf(null, RemoteKey.PAUSE, null),
            listOf(null, RemoteKey.STOP, null)
        )

        DeviceType.LIGHT -> listOf(
            listOf(RemoteKey.POWER, null, null),
            listOf(RemoteKey.BRIGHT_UP, RemoteKey.BRIGHTNESS, RemoteKey.BRIGHT_DOWN),
            listOf(RemoteKey.COLOR, null, null)
        )
    }
}
