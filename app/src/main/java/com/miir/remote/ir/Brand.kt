package com.miir.remote.ir

/**
 * 品牌定义。仅承载展示信息，具体红外参数由 [CodeSet] 提供。
 */
data class Brand(
    val id: String,
    val name: String,
    val deviceType: DeviceType
)
