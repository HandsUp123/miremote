package com.miir.remote.ir

import android.content.Context
import org.json.JSONObject

/**
 * 红外码库。在 App 启动时从 assets/ir_codes.json 一次性加载真实 IRDB 码值。
 *
 * 数据结构：品牌 → 多个机型 [CodeSet]（每个机型一组独立码表）。
 * 添加遥控器时可让用户在多组码表间切换试码，命中后保存 [CodeSet.id]。
 */
class IrCodeDatabase(context: Context) {

    private val brands: List<Brand>
    private val codeSetsByBrand: Map<String, List<CodeSet>>
    private val codeSetsById: Map<String, CodeSet>

    init {
        val json = context.assets.open("ir_codes.json").bufferedReader().use { it.readText() }
        val root = JSONObject(json)
        val brandList = mutableListOf<Brand>()
        val byBrand = mutableMapOf<String, MutableList<CodeSet>>()
        val byId = mutableMapOf<String, CodeSet>()

        val arr = root.getJSONArray("brands")
        for (i in 0 until arr.length()) {
            val b = arr.getJSONObject(i)
            val brand = Brand(
                id = b.getString("id"),
                name = b.getString("name"),
                deviceType = DeviceType.fromId(b.getString("deviceType").lowercase())
            )
            brandList.add(brand)
            val models = b.getJSONArray("models")
            val sets = mutableListOf<CodeSet>()
            for (j in 0 until models.length()) {
                val m = models.getJSONObject(j)
                val keysObj = m.getJSONObject("keys")
                val keys = mutableMapOf<String, Int>()
                val ki = keysObj.keys()
                while (ki.hasNext()) {
                    val k = ki.next()
                    keys[k] = keysObj.getInt(k)
                }
                val cs = CodeSet(
                    id = m.getString("id"),
                    brandId = brand.id,
                    name = m.getString("name"),
                    protocol = m.getString("protocol"),
                    device = m.getInt("device"),
                    subdevice = m.optInt("subdevice", 255),
                    keys = keys
                )
                sets.add(cs)
                byId[cs.id] = cs
            }
            byBrand[brand.id] = sets
        }
        brands = brandList
        codeSetsByBrand = byBrand
        codeSetsById = byId
    }

    /** 所有品牌。 */
    fun brands(): List<Brand> = brands

    /** 指定设备类型的所有品牌。 */
    fun brandsFor(type: DeviceType): List<Brand> = brands.filter { it.deviceType == type }

    /** 品牌下的所有码集（机型）。 */
    fun codeSetsFor(brandId: String): List<CodeSet> = codeSetsByBrand[brandId].orEmpty()

    /** 按 id 取码集。 */
    fun codeSet(id: String): CodeSet? = codeSetsById[id]

    /** 按 id 取品牌。 */
    fun brand(id: String): Brand? = brands.firstOrNull { it.id == id }

    /**
     * 取指定码集 + 按键的 (载波, 时序数组)。
     * @return 码集 / 按键 / 协议不存在均返回 null，调用方应做容错。
     */
    fun patternFor(codeSetId: String, key: RemoteKey): Pair<Int, IntArray>? {
        val cs = codeSetsById[codeSetId] ?: return null
        val function = cs.keys[key.id] ?: return null
        return ProtocolRegistry.encode(cs.protocol, cs.device, cs.subdevice, function)
    }
}
