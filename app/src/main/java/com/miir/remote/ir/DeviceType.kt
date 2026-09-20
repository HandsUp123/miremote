package com.miir.remote.ir

/**
 * 支持的设备类型。id 持久化到数据库，displayName 用于界面。
 */
enum class DeviceType(val displayName: String, val id: String) {
    TV("电视", "tv"),
    SET_TOP_BOX("机顶盒", "stb"),
    AC("空调", "ac"),
    FAN("风扇", "fan"),
    PROJECTOR("投影仪", "projector"),
    AUDIO("音响", "audio"),
    DVD("影碟机", "dvd"),
    LIGHT("灯具", "light");

    companion object {
        fun fromId(id: String): DeviceType =
            entries.firstOrNull { it.id == id } ?: TV
    }
}
