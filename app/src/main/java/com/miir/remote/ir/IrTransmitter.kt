package com.miir.remote.ir

import android.content.Context
import android.hardware.ConsumerIrManager

/**
 * 对 [ConsumerIrManager] 的封装。小米带红外发射器的机型上可用。
 */
class IrTransmitter(context: Context) {

    private val irManager =
        context.applicationContext.getSystemService(Context.CONSUMER_IR_SERVICE) as? ConsumerIrManager

    /** 当前设备是否拥有可用的红外发射器。 */
    val hasIrEmitter: Boolean
        get() = irManager?.hasIrEmitter() == true

    /** 支持的载波频率范围（仅用于诊断展示）。 */
    val carrierFrequencies: Array<out ConsumerIrManager.CarrierFrequencyRange>?
        get() = irManager?.carrierFrequencies

    /**
     * 发射一帧红外信号。
     * @param carrierHz 载波频率（如 38000）
     * @param pattern 微秒时序数组，奇数位为高电平持续时间，偶数位为低电平持续时间
     */
    fun transmit(carrierHz: Int, pattern: IntArray) {
        val mgr = irManager ?: return
        if (!mgr.hasIrEmitter()) return
        try {
            mgr.transmit(carrierHz, pattern)
        } catch (_: Throwable) {
            // 部分厂商实现可能抛异常，忽略以保证 UI 不崩
        }
    }
}
