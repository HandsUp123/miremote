package com.miir.remote.ir

/**
 * 一组红外码表：某品牌某机型的所有按键 → IRDB 命令值映射。
 *
 * 一个品牌可包含多个 [CodeSet]（不同机型），用户在添加遥控器时可逐组试码，
 * 命中后将 [id] 持久化到 [com.miir.remote.data.entity.RemoteEntity.modelId]，
 * 控制屏据此取出对应码集发射红外。
 *
 * @param protocol IRDB 协议名（如 "NEC1"、"RC5"），由 [ProtocolRegistry] 解析
 * @param device   8 位设备地址
 * @param subdevice 8 位子地址；IRDB 约定 255 表示无子地址
 * @param keys     RemoteKey.id → 命令值
 */
data class CodeSet(
    val id: String,
    val brandId: String,
    val name: String,
    val protocol: String,
    val device: Int,
    val subdevice: Int,
    val keys: Map<String, Int>
) {
    /** 该码集是否包含某按键。 */
    fun has(key: RemoteKey): Boolean = keys.containsKey(key.id)

    /** 当前协议是否被 [ProtocolRegistry] 实现（可实际发射）。 */
    val isSupported: Boolean get() = ProtocolRegistry.isSupported(protocol)
}
