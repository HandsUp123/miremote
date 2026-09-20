package com.miir.remote.ir

/**
 * 红外码库。维护品牌列表与各设备类型按键到 NEC 命令值的映射。
 *
 * 说明：码值采用「品牌地址 + 顺序命令」的方式生成结构合法的 NEC 帧，
 * 可保证发射流程完整可用；如需精确匹配某台真实设备，可将对应品牌
 * 的 address / 命令值替换为实测码值。
 */
object IrCodeDatabase {

    val brands: List<Brand> = listOf(
        // 电视
        Brand("samsung", "三星", DeviceType.TV, 0xE0),
        Brand("lg", "LG", DeviceType.TV, 0x04),
        Brand("hisense", "海信", DeviceType.TV, 0x20),
        Brand("tcl", "TCL", DeviceType.TV, 0x00),
        Brand("mi_tv", "小米", DeviceType.TV, 0xFD),
        Brand("sony", "索尼", DeviceType.TV, 0x15),
        Brand("sharp", "夏普", DeviceType.TV, 0x05),
        Brand("skyworth", "创维", DeviceType.TV, 0x01),
        Brand("changhong", "长虹", DeviceType.TV, 0x12),
        Brand("panasonic", "松下", DeviceType.TV, 0x40),

        // 机顶盒
        Brand("gehua", "歌华有线", DeviceType.SET_TOP_BOX, 0x60),
        Brand("cable", "各地有线", DeviceType.SET_TOP_BOX, 0x61),
        Brand("satellite", "卫星机顶盒", DeviceType.SET_TOP_BOX, 0x62),
        Brand("zte", "中兴", DeviceType.SET_TOP_BOX, 0x63),
        Brand("huawei", "华为", DeviceType.SET_TOP_BOX, 0x64),
        Brand("skyworth_box", "创维盒子", DeviceType.SET_TOP_BOX, 0x65),
        Brand("fiberhome", "烽火", DeviceType.SET_TOP_BOX, 0x66),
        Brand("jiulian", "九联", DeviceType.SET_TOP_BOX, 0x67),

        // 空调
        Brand("gree", "格力", DeviceType.AC, 0x48),
        Brand("midea", "美的", DeviceType.AC, 0xB2),
        Brand("haier", "海尔", DeviceType.AC, 0x00),
        Brand("hisense_ac", "海信", DeviceType.AC, 0x10),
        Brand("aux", "奥克斯", DeviceType.AC, 0x20),
        Brand("chigo", "志高", DeviceType.AC, 0x30),
        Brand("changhong_ac", "长虹", DeviceType.AC, 0x40),
        Brand("tcl_ac", "TCL", DeviceType.AC, 0x50),

        // 风扇
        Brand("midea_fan", "美的", DeviceType.FAN, 0x80),
        Brand("gree_fan", "格力", DeviceType.FAN, 0x81),
        Brand("airmt", "艾美特", DeviceType.FAN, 0x82),
        Brand("xfire", "先锋", DeviceType.FAN, 0x83),
        Brand("xiaomi_fan", "小米", DeviceType.FAN, 0x84),

        // 投影仪
        Brand("xgimi", "极米", DeviceType.PROJECTOR, 0x90),
        Brand("jmgo", "坚果", DeviceType.PROJECTOR, 0x91),
        Brand("epson", "爱普生", DeviceType.PROJECTOR, 0x92),
        Brand("benq", "明基", DeviceType.PROJECTOR, 0x93),
        Brand("dangbei", "当贝", DeviceType.PROJECTOR, 0x94),
        Brand("xiaomi_proj", "小米", DeviceType.PROJECTOR, 0x95),

        // 音响
        Brand("sony_audio", "索尼", DeviceType.AUDIO, 0xA0),
        Brand("yamaha", "雅马哈", DeviceType.AUDIO, 0xA1),
        Brand("edifier", "漫步者", DeviceType.AUDIO, 0xA2),
        Brand("hivi", "惠威", DeviceType.AUDIO, 0xA3),
        Brand("xiaomi_audio", "小米", DeviceType.AUDIO, 0xA4),

        // 影碟机
        Brand("sony_dvd", "索尼", DeviceType.DVD, 0xB0),
        Brand("philips", "飞利浦", DeviceType.DVD, 0xB1),
        Brand("pioneer_dvd", "先锋", DeviceType.DVD, 0xB2),
        Brand("giec", "杰科", DeviceType.DVD, 0xB3),
        Brand("oppo", "OPPO", DeviceType.DVD, 0xB4),

        // 灯具
        Brand("hue", "飞利浦Hue", DeviceType.LIGHT, 0xC0),
        Brand("nvc", "雷士", DeviceType.LIGHT, 0xC1),
        Brand("opple", "欧普", DeviceType.LIGHT, 0xC2),
        Brand("yeelight", "米家台灯", DeviceType.LIGHT, 0xC3),
        Brand("bull", "公牛", DeviceType.LIGHT, 0xC4)
    )

    fun brandsFor(type: DeviceType): List<Brand> =
        brands.filter { it.deviceType == type }

    fun brandById(id: String): Brand? = brands.firstOrNull { it.id == id }

    private val tvCommands: Map<RemoteKey, Int> = mapOf(
        RemoteKey.POWER to 0x00, RemoteKey.MUTE to 0x01,
        RemoteKey.VOL_UP to 0x02, RemoteKey.VOL_DOWN to 0x03,
        RemoteKey.CH_UP to 0x04, RemoteKey.CH_DOWN to 0x05,
        RemoteKey.MENU to 0x06, RemoteKey.SOURCE to 0x07,
        RemoteKey.EXIT to 0x08, RemoteKey.BACK to 0x09,
        RemoteKey.HOME to 0x0A, RemoteKey.SETTINGS to 0x0B,
        RemoteKey.GUIDE to 0x0C,
        RemoteKey.UP to 0x0D, RemoteKey.DOWN to 0x0E,
        RemoteKey.LEFT to 0x0F, RemoteKey.RIGHT to 0x10, RemoteKey.OK to 0x11,
        RemoteKey.NUM_0 to 0x20, RemoteKey.NUM_1 to 0x21, RemoteKey.NUM_2 to 0x22,
        RemoteKey.NUM_3 to 0x23, RemoteKey.NUM_4 to 0x24, RemoteKey.NUM_5 to 0x25,
        RemoteKey.NUM_6 to 0x26, RemoteKey.NUM_7 to 0x27, RemoteKey.NUM_8 to 0x28,
        RemoteKey.NUM_9 to 0x29, RemoteKey.PREV_CH to 0x2A
    )

    private val stbCommands: Map<RemoteKey, Int> = mapOf(
        RemoteKey.POWER to 0x00, RemoteKey.MUTE to 0x01,
        RemoteKey.VOL_UP to 0x02, RemoteKey.VOL_DOWN to 0x03,
        RemoteKey.CH_UP to 0x04, RemoteKey.CH_DOWN to 0x05,
        RemoteKey.MENU to 0x06, RemoteKey.EXIT to 0x08,
        RemoteKey.BACK to 0x09, RemoteKey.HOME to 0x0A,
        RemoteKey.UP to 0x0D, RemoteKey.DOWN to 0x0E,
        RemoteKey.LEFT to 0x0F, RemoteKey.RIGHT to 0x10, RemoteKey.OK to 0x11,
        RemoteKey.NUM_0 to 0x20, RemoteKey.NUM_1 to 0x21, RemoteKey.NUM_2 to 0x22,
        RemoteKey.NUM_3 to 0x23, RemoteKey.NUM_4 to 0x24, RemoteKey.NUM_5 to 0x25,
        RemoteKey.NUM_6 to 0x26, RemoteKey.NUM_7 to 0x27, RemoteKey.NUM_8 to 0x28,
        RemoteKey.NUM_9 to 0x29, RemoteKey.PREV_CH to 0x2A
    )

    private val acCommands: Map<RemoteKey, Int> = mapOf(
        RemoteKey.POWER to 0x00, RemoteKey.MODE to 0x01,
        RemoteKey.TEMP_UP to 0x02, RemoteKey.TEMP_DOWN to 0x03,
        RemoteKey.FAN_SPEED to 0x04, RemoteKey.SWING to 0x05,
        RemoteKey.TURBO to 0x06, RemoteKey.SLEEP to 0x07
    )

    private val fanCommands: Map<RemoteKey, Int> = mapOf(
        RemoteKey.POWER to 0x00, RemoteKey.SPEED to 0x01,
        RemoteKey.OSCILLATE to 0x02, RemoteKey.TIMER to 0x03,
        RemoteKey.WIND_TYPE to 0x04
    )

    private val projectorCommands: Map<RemoteKey, Int> = mapOf(
        RemoteKey.POWER to 0x00, RemoteKey.SOURCE to 0x07,
        RemoteKey.MENU to 0x06, RemoteKey.EXIT to 0x08,
        RemoteKey.BACK to 0x09, RemoteKey.HOME to 0x0A,
        RemoteKey.SETTINGS to 0x0B, RemoteKey.MUTE to 0x01,
        RemoteKey.UP to 0x0D, RemoteKey.DOWN to 0x0E,
        RemoteKey.LEFT to 0x0F, RemoteKey.RIGHT to 0x10, RemoteKey.OK to 0x11
    )

    private val audioCommands: Map<RemoteKey, Int> = mapOf(
        RemoteKey.POWER to 0x00, RemoteKey.MUTE to 0x01,
        RemoteKey.VOL_UP to 0x02, RemoteKey.VOL_DOWN to 0x03,
        RemoteKey.MENU to 0x06, RemoteKey.PLAY to 0x10,
        RemoteKey.PAUSE to 0x11, RemoteKey.NEXT to 0x12,
        RemoteKey.PREV to 0x13, RemoteKey.STOP to 0x14,
        RemoteKey.UP to 0x0D, RemoteKey.DOWN to 0x0E,
        RemoteKey.LEFT to 0x0F, RemoteKey.RIGHT to 0x10, RemoteKey.OK to 0x11
    )

    private val dvdCommands: Map<RemoteKey, Int> = mapOf(
        RemoteKey.POWER to 0x00, RemoteKey.EJECT to 0x0E,
        RemoteKey.PLAY to 0x10, RemoteKey.PAUSE to 0x11,
        RemoteKey.NEXT to 0x12, RemoteKey.PREV to 0x13, RemoteKey.STOP to 0x14,
        RemoteKey.MENU to 0x06, RemoteKey.BACK to 0x09,
        RemoteKey.UP to 0x0D, RemoteKey.DOWN to 0x0E,
        RemoteKey.LEFT to 0x0F, RemoteKey.RIGHT to 0x10, RemoteKey.OK to 0x11
    )

    private val lightCommands: Map<RemoteKey, Int> = mapOf(
        RemoteKey.POWER to 0x00, RemoteKey.BRIGHT_UP to 0x01,
        RemoteKey.BRIGHT_DOWN to 0x02, RemoteKey.COLOR to 0x03,
        RemoteKey.BRIGHTNESS to 0x04
    )

    private fun commandsFor(type: DeviceType): Map<RemoteKey, Int> = when (type) {
        DeviceType.TV -> tvCommands
        DeviceType.SET_TOP_BOX -> stbCommands
        DeviceType.AC -> acCommands
        DeviceType.FAN -> fanCommands
        DeviceType.PROJECTOR -> projectorCommands
        DeviceType.AUDIO -> audioCommands
        DeviceType.DVD -> dvdCommands
        DeviceType.LIGHT -> lightCommands
    }

    fun commandFor(type: DeviceType, brandId: String, key: RemoteKey): Int? =
        commandsFor(type)[key]

    /**
     * @return (载波频率, NEC 时序数组) 或 null（品牌 / 按键不存在时）
     */
    fun patternFor(type: DeviceType, brandId: String, key: RemoteKey): Pair<Int, IntArray>? {
        val brand = brandById(brandId) ?: return null
        val command = commandsFor(type)[key] ?: return null
        return brand.carrier to NecProtocol.encode(brand.address, command)
    }
}
