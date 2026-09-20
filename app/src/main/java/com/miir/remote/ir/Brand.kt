package com.miir.remote.ir

/**
 * 品牌定义。address 为该品牌在 NEC 协议下的设备地址，
 * carrier 为载波频率（绝大多数消费红外为 38kHz）。
 */
data class Brand(
    val id: String,
    val name: String,
    val deviceType: DeviceType,
    val address: Int,
    val carrier: Int = NecProtocol.CARRIER
)
