package com.miir.remote.ir

/**
 * NEC 红外协议编码器。同时支持标准 NEC 与扩展 NEC，覆盖 IRDB 中的
 * NEC1 / NEC2 / NECx1 / NECx2 协议名（编码结构相同，NEC2 仅多一帧重复）。
 *
 * 标准 NEC 帧（38kHz 载波）：
 *   引导：9000us 高 + 4500us 低
 *   数据：32 位 = addr8 + addr_inv8 + cmd8 + cmd_inv8，低位在前
 *   每位：560us 高 + （0:560us 低 / 1:1690us 低）
 *   结束：560us 高
 *
 * 扩展 NEC：当 subdevice 不为 255（IRDB 约定的"无 subdevice"标记）时，
 * 第二字节使用 subdevice 原值而非 addr 取反，形成 16 位地址。
 */
object NecProtocol : IrProtocol {

    override val carrierHz = 38_000

    const val CARRIER = 38000  // 兼容旧引用

    private const val LEAD_ON = 9000
    private const val LEAD_OFF = 4500
    private const val BIT_ON = 560
    private const val ZERO_OFF = 560
    private const val ONE_OFF = 1690
    private const val STOP_ON = 560

    /**
     * @param device    8 位设备地址
     * @param subdevice 8 位子地址；IRDB 约定 255 表示无子地址（标准 NEC，发 ~device）
     * @param function  8 位命令
     */
    override fun encode(device: Int, subdevice: Int, function: Int): IntArray {
        val addr = device and 0xFF
        val cmd = function and 0xFF
        val secondByte = if (subdevice == 255 || subdevice < 0) {
            addr.inv() and 0xFF
        } else {
            subdevice and 0xFF
        }
        // 32 位数据：addr | secondByte<<8 | cmd<<16 | ~cmd<<24，低位在前
        val word = addr.toLong() or
            (secondByte.toLong() shl 8) or
            (cmd.toLong() shl 16) or
            ((cmd.inv() and 0xFF).toLong() shl 24)

        val pattern = ArrayList<Int>(68)
        pattern.add(LEAD_ON)
        pattern.add(LEAD_OFF)
        for (i in 0 until 32) {
            pattern.add(BIT_ON)
            pattern.add(if (((word shr i) and 1L) == 1L) ONE_OFF else ZERO_OFF)
        }
        pattern.add(STOP_ON)
        return pattern.toIntArray()
    }
}
