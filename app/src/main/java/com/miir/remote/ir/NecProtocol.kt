package com.miir.remote.ir

/**
 * NEC 红外协议编码器。
 *
 * 标准 NEC 帧结构（38kHz 载波）：
 *   引导：9000us 高 + 4500us 低
 *   数据：32 位（地址8 + 地址取反8 + 命令8 + 命令取反8），低位在前
 *   每位：560us 高 + （0:560us 低 / 1:1690us 低）
 *   结束：560us 高
 *
 * 注意：实际设备码值因品牌 / 型号而异，此处生成的是结构合法、可由
 * [android.hardware.ConsumerIrManager] 发射的 NEC 帧。如需匹配真实设备，
 * 可在 [IrCodeDatabase] 中替换为对应品牌的真实码值。
 */
object NecProtocol {

    const val CARRIER = 38000

    private const val LEAD_ON = 9000
    private const val LEAD_OFF = 4500
    private const val BIT_ON = 560
    private const val ZERO_OFF = 560
    private const val ONE_OFF = 1690
    private const val STOP_ON = 560

    /**
     * @param address 8 位地址（0..0xFF）
     * @param command 8 位命令（0..0xFF）
     * @return 可直接传给 [android.hardware.ConsumerIrManager.transmit] 的时序数组（微秒）
     */
    fun encode(address: Int, command: Int): IntArray {
        val addr = address and 0xFF
        val cmd = command and 0xFF
        // 32 位数据：addr | ~addr | cmd | ~cmd
        val word = addr.toLong() or
            ((addr.inv() and 0xFF).toLong() shl 8) or
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
