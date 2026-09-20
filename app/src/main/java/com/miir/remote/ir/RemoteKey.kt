package com.miir.remote.ir

/**
 * 遥控器按键。label 为界面显示文案，id 用于码库映射与持久化。
 */
enum class RemoteKey(val id: String, val label: String) {
    // 通用
    POWER("power", "电源"),
    MUTE("mute", "静音"),
    VOL_UP("vol_up", "音量+"),
    VOL_DOWN("vol_down", "音量-"),
    CH_UP("ch_up", "频道+"),
    CH_DOWN("ch_down", "频道-"),
    MENU("menu", "菜单"),
    SOURCE("source", "信号源"),
    EXIT("exit", "退出"),
    BACK("back", "返回"),
    HOME("home", "主页"),
    SETTINGS("settings", "设置"),
    GUIDE("guide", "节目单"),

    // 方向 / 确认
    UP("up", "上"),
    DOWN("down", "下"),
    LEFT("left", "左"),
    RIGHT("right", "右"),
    OK("ok", "确认"),

    // 数字
    NUM_0("0", "0"),
    NUM_1("1", "1"),
    NUM_2("2", "2"),
    NUM_3("3", "3"),
    NUM_4("4", "4"),
    NUM_5("5", "5"),
    NUM_6("6", "6"),
    NUM_7("7", "7"),
    NUM_8("8", "8"),
    NUM_9("9", "9"),
    PREV_CH("prev_ch", "上一个频道"),

    // 空调
    MODE("mode", "模式"),
    TEMP_UP("temp_up", "温度+"),
    TEMP_DOWN("temp_down", "温度-"),
    FAN_SPEED("fan_speed", "风速"),
    SWING("swing", "扫风"),
    TURBO("turbo", "强力"),
    SLEEP("sleep", "睡眠"),

    // 风扇
    SPEED("speed", "风速"),
    OSCILLATE("oscillate", "摇头"),
    TIMER("timer", "定时"),
    WIND_TYPE("wind_type", "风类"),

    // 媒体
    PLAY("play", "播放"),
    PAUSE("pause", "暂停"),
    NEXT("next", "下一曲"),
    PREV("prev", "上一曲"),
    STOP("stop", "停止"),
    EJECT("eject", "出仓"),

    // 灯具
    BRIGHT_UP("bright_up", "亮+"),
    BRIGHT_DOWN("bright_down", "暗-"),
    COLOR("color", "颜色"),
    BRIGHTNESS("brightness", "亮度");

    companion object {
        fun fromId(id: String): RemoteKey? = entries.firstOrNull { it.id == id }
    }
}
