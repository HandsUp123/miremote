package com.miir.remote.ir

/**
 * 红外协议编码器统一接口。每个协议将 IRDB 风格的 (device, subdevice, function)
 * 编码为可发射的时序数组。时序数组交替为 高电平/低电平 持续时间（微秒），
 * 第 0 项为高电平，符合 [android.hardware.ConsumerIrManager.transmit] 要求。
 */
interface IrProtocol {
    /** 载波频率（Hz）。 */
    val carrierHz: Int

    /** 将协议参数编码为时序数组。 */
    fun encode(device: Int, subdevice: Int, function: Int): IntArray
}

/**
 * Philips RC5 协议（36kHz 载波，14 位 Manchester 编码）。
 *
 * 帧结构（MSB 在前）：
 *   start1(1) + start2(1) + toggle(0) + addr(5) + cmd(6)
 *
 * Manchester：bit=1 → (高, 低)；bit=0 → (低, 高)。半位周期 889us。
 * 注意：toggle 位固定为 0（每次按同一键不发 toggle），适合 App 单次发射场景。
 */
object Rc5Protocol : IrProtocol {

    override val carrierHz = 36_000
    private const val T = 889  // 半位周期 us

    override fun encode(device: Int, subdevice: Int, function: Int): IntArray {
        val addr = device and 0x1F
        val cmd = function and 0x3F
        // 14 位 MSB 在前：S1(1) S2(1) TG(0) A4 A3 A2 A1 A0 C5 C4 C3 C2 C1 C0
        val bits = intArrayOf(
            1, 1, 0,
            (addr shr 4) and 1, (addr shr 3) and 1, (addr shr 2) and 1,
            (addr shr 1) and 1, addr and 1,
            (cmd shr 5) and 1, (cmd shr 4) and 1, (cmd shr 3) and 1,
            (cmd shr 2) and 1, (cmd shr 1) and 1, cmd and 1
        )
        // Manchester：bit=1 → (mark, space)；bit=0 → (space, mark)
        val segments = ArrayList<Pair<Boolean, Int>>(bits.size * 2)
        for (b in bits) {
            segments.add((b == 1) to T)
            segments.add((b == 0) to T)
        }
        return mergeAlternating(segments)
    }
}

/**
 * Philips RC6 Mode 0 协议（36kHz 载波，Manchester 变体）。
 *
 * 帧结构（MSB 在前）：
 *   引导(2666 高 + 889 低) + start(1) + mode(000) + trailer(0,双宽) + addr(8) + cmd(8)
 *
 * 普通 bit 半位 444us；trailer bit 半位 889us。
 * bit=1 → (高, 低)；bit=0 → (低, 高)。
 */
object Rc6Protocol : IrProtocol {

    override val carrierHz = 36_000
    private const val HDR_MARK = 2666
    private const val HDR_SPACE = 889
    private const val T = 444           // 普通位半位
    private const val TRAILER = 889     // trailer 位半位（双宽）

    override fun encode(device: Int, subdevice: Int, function: Int): IntArray {
        val addr = device and 0xFF
        val cmd = function and 0xFF
        // 21 位 MSB 在前：start(1) + mode(000) + trailer(0) + addr8 + cmd8
        val bits = intArrayOf(
            1,                              // start
            0, 0, 0,                        // mode
            0                               // trailer (always 0 in Mode 0)
        ) + IntArray(8) { (addr shr (7 - it)) and 1 } +
            IntArray(8) { (cmd shr (7 - it)) and 1 }

        val segments = ArrayList<Pair<Boolean, Int>>()
        // 引导
        segments.add(true to HDR_MARK)
        segments.add(false to HDR_SPACE)
        // start 位（普通）
        appendManchester(segments, bits[0], T)
        // mode 3 位（普通）
        appendManchester(segments, bits[1], T)
        appendManchester(segments, bits[2], T)
        appendManchester(segments, bits[3], T)
        // trailer 位（双宽）
        appendManchester(segments, bits[4], TRAILER)
        // addr 8 位 + cmd 8 位（普通）
        for (i in 5 until bits.size) {
            appendManchester(segments, bits[i], T)
        }
        return mergeAlternating(segments)
    }

    private fun appendManchester(
        out: ArrayList<Pair<Boolean, Int>>,
        bit: Int,
        half: Int
    ) {
        // bit=1 → (mark, space)；bit=0 → (space, mark)
        out.add((bit == 1) to half)
        out.add((bit == 0) to half)
    }
}

/**
 * Sony SIRC 协议（40kHz 载波，PWM 编码）。
 *
 * 引导：2400us 高 + 600us 低
 * 位：1 = 1200us 高 + 600us 低；0 = 600us 高 + 600us 低
 * 帧低位在前：cmd(7) + addr(5/8/13)
 *
 * Sony12 = 7+5；Sony15 = 7+8；Sony20 = 7+5+8。
 */
object SonySircProtocol {

    const val CARRIER = 40_000
    private const val LEAD_MARK = 2400
    private const val BIT_SPACE = 600
    private const val ONE_MARK = 1200
    private const val ZERO_MARK = 600

    fun encode(totalBits: Int, addr: Int, cmd: Int): IntArray {
        val cmdBits = (0 until 7).map { (cmd shr it) and 1 }      // cmd 低位在前
        val addrBits = (0 until totalBits - 7).map { (addr shr it) and 1 }
        val allBits = cmdBits + addrBits                          // 发射顺序

        val pattern = ArrayList<Int>(allBits.size * 2 + 1)
        pattern.add(LEAD_MARK)
        pattern.add(BIT_SPACE)
        for ((i, b) in allBits.withIndex()) {
            pattern.add(if (b == 1) ONE_MARK else ZERO_MARK)
            if (i < allBits.size - 1) pattern.add(BIT_SPACE)
        }
        return pattern.toIntArray()
    }
}

object SonyProtocol12 : IrProtocol {
    override val carrierHz = SonySircProtocol.CARRIER
    override fun encode(device: Int, subdevice: Int, function: Int) =
        SonySircProtocol.encode(12, device, function)
}

object SonyProtocol15 : IrProtocol {
    override val carrierHz = SonySircProtocol.CARRIER
    override fun encode(device: Int, subdevice: Int, function: Int) =
        SonySircProtocol.encode(15, device, function)
}

object SonyProtocol20 : IrProtocol {
    override val carrierHz = SonySircProtocol.CARRIER
    override fun encode(device: Int, subdevice: Int, function: Int) =
        SonySircProtocol.encode(20, device, function)
}

/**
 * Panasonic 协议（37kHz 载波）。
 *
 * 引导：3456us 高 + 1728us 低
 * 位：1 = 502us 高 + 1244us 低；0 = 502us 高 + 400us 低
 * 帧：32 位 = addr1(8) + addr2(8) + cmd(8) + ~cmd(8)，低位在前
 *
 * IRDB 中 device=addr1，subdevice=addr2。
 */
object PanasonicProtocol : IrProtocol {

    override val carrierHz = 37_000
    private const val LEAD_MARK = 3456
    private const val LEAD_SPACE = 1728
    private const val BIT_MARK = 502
    private const val ONE_SPACE = 1244
    private const val ZERO_SPACE = 400

    override fun encode(device: Int, subdevice: Int, function: Int): IntArray {
        val addr1 = device and 0xFF
        val addr2 = subdevice and 0xFF
        val cmd = function and 0xFF
        val cmdInv = cmd.inv() and 0xFF
        val bytes = intArrayOf(addr1, addr2, cmd, cmdInv)

        val pattern = ArrayList<Int>(68)
        pattern.add(LEAD_MARK)
        pattern.add(LEAD_SPACE)
        for (b in bytes) {
            for (i in 0 until 8) {
                val bit = (b shr i) and 1   // 低位在前
                pattern.add(BIT_MARK)
                pattern.add(if (bit == 1) ONE_SPACE else ZERO_SPACE)
            }
        }
        pattern.add(BIT_MARK)
        return pattern.toIntArray()
    }
}

/**
 * Sharp 协议（38kHz 载波，与 NEC 同引导但位编码相反）。
 *
 * 引导：9000us 高 + 4500us 低
 * 位：1 = 1680us 高 + 560us 低；0 = 560us 高 + 1680us 低
 * 帧：13 位 = addr(5) + cmd(8)，低位在前
 *
 * 注：完整 Sharp 协议需在 40ms 后重发一次取反帧，此处只发单帧。
 */
object SharpProtocol : IrProtocol {

    override val carrierHz = 38_000
    private const val LEAD_MARK = 9000
    private const val LEAD_SPACE = 4500
    private const val ONE_MARK = 1680
    private const val BIT_MARK = 560
    private const val BIT_SPACE = 560
    private const val ZERO_SPACE = 1680
    private const val STOP_MARK = 560

    override fun encode(device: Int, subdevice: Int, function: Int): IntArray {
        val addr = device and 0x1F       // 5 位地址
        val cmd = function and 0xFF
        val bits = (0 until 5).map { (addr shr it) and 1 } +
            (0 until 8).map { (cmd shr it) and 1 }

        val pattern = ArrayList<Int>(28)
        pattern.add(LEAD_MARK)
        pattern.add(LEAD_SPACE)
        for (b in bits) {
            if (b == 1) {
                pattern.add(ONE_MARK)
                pattern.add(BIT_SPACE)
            } else {
                pattern.add(BIT_MARK)
                pattern.add(ZERO_SPACE)
            }
        }
        pattern.add(STOP_MARK)
        return pattern.toIntArray()
    }
}

/**
 * RCA 协议（38kHz 载波）。
 *
 * 引导：4000us 高 + 4000us 低
 * 位：1 = 550us 高 + 1900us 低；0 = 550us 高 + 950us 低
 * 帧：16 位 = addr(4) + cmd(8) + ~addr(4)，低位在前
 *
 * IRDB "RCA-38" 协议名映射到此处。
 */
object RcaProtocol : IrProtocol {

    override val carrierHz = 38_000
    private const val LEAD_MARK = 4000
    private const val LEAD_SPACE = 4000
    private const val BIT_MARK = 550
    private const val ONE_SPACE = 1900
    private const val ZERO_SPACE = 950
    private const val STOP_MARK = 550

    override fun encode(device: Int, subdevice: Int, function: Int): IntArray {
        val addr = device and 0x0F       // 4 位地址
        val cmd = function and 0xFF
        val addrInv = addr.inv() and 0x0F
        val bits = (0 until 4).map { (addr shr it) and 1 } +
            (0 until 8).map { (cmd shr it) and 1 } +
            (0 until 4).map { (addrInv shr it) and 1 }

        val pattern = ArrayList<Int>(34)
        pattern.add(LEAD_MARK)
        pattern.add(LEAD_SPACE)
        for (b in bits) {
            pattern.add(BIT_MARK)
            pattern.add(if (b == 1) ONE_SPACE else ZERO_SPACE)
        }
        pattern.add(STOP_MARK)
        return pattern.toIntArray()
    }
}

/**
 * 协议注册表：将 IRDB 协议名映射到具体编码器。
 *
 * 返回 null 的协议（如 Samsung20 / F12 等冷门协议）暂未实现，调用方应做容错。
 */
object ProtocolRegistry {

    private val protocols: Map<String, IrProtocol> = mapOf(
        "NEC1" to NecProtocol,
        "NEC2" to NecProtocol,
        "NECx1" to NecProtocol,
        "NECx2" to NecProtocol,
        "RC5" to Rc5Protocol,
        "RC6" to Rc6Protocol,
        "Sony12" to SonyProtocol12,
        "Sony15" to SonyProtocol15,
        "Sony20" to SonyProtocol20,
        "Panasonic" to PanasonicProtocol,
        "Sharp" to SharpProtocol,
        "RCA-38" to RcaProtocol,
        "RCA" to RcaProtocol
    )

    /** 协议是否已实现（用于 UI 提示用户哪些机型可发射）。 */
    fun isSupported(protocolName: String): Boolean = protocols.containsKey(protocolName)

    /**
     * 编码为可直接传给 [android.hardware.ConsumerIrManager.transmit] 的 (载波, 时序数组)。
     * @return 不支持的协议返回 null。
     */
    fun encode(
        protocolName: String,
        device: Int,
        subdevice: Int,
        function: Int
    ): Pair<Int, IntArray>? {
        val proto = protocols[protocolName] ?: return null
        return proto.carrierHz to proto.encode(device, subdevice, function)
    }
}

/**
 * 将交替的 (是否高电平, 持续时间) 序列合并为 ConsumerIrManager 所需的时序数组：
 * 相邻同极性段合并为一段，且第 0 段为高电平。
 */
private fun mergeAlternating(segments: List<Pair<Boolean, Int>>): IntArray {
    if (segments.isEmpty()) return IntArray(0)
    val merged = ArrayList<Int>(segments.size)
    var currentIsMark = segments[0].first
    var currentDur = 0
    for ((isMark, dur) in segments) {
        if (isMark == currentIsMark) {
            currentDur += dur
        } else {
            merged.add(currentDur)
            currentIsMark = isMark
            currentDur = dur
        }
    }
    merged.add(currentDur)
    return merged.toIntArray()
}
