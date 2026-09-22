package com.miir.remote.ui.add

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.miir.remote.data.entity.GroupEntity
import com.miir.remote.data.repository.RemoteRepository
import com.miir.remote.ir.CodeSet
import com.miir.remote.ir.DeviceType
import com.miir.remote.ir.IrCodeDatabase
import com.miir.remote.ir.IrTransmitter
import com.miir.remote.ir.RemoteKey
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

class AddRemoteViewModel(
    private val repo: RemoteRepository,
    private val ir: IrTransmitter,
    private val codes: IrCodeDatabase
) : ViewModel() {

    enum class Step { TYPE, BRAND, TEST, SAVE }

    val groups: StateFlow<List<GroupEntity>> = repo.observeGroups()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), emptyList())

    val hasIrEmitter: Boolean = ir.hasIrEmitter

    private val _step = MutableStateFlow(Step.TYPE)
    val step: StateFlow<Step> = _step.asStateFlow()

    private val _type = MutableStateFlow<DeviceType?>(null)
    val type: StateFlow<DeviceType?> = _type.asStateFlow()

    private val _brandId = MutableStateFlow<String?>(null)
    val brandId: StateFlow<String?> = _brandId.asStateFlow()

    /** 当前品牌下所有可选码集。 */
    private val _codeSets = MutableStateFlow<List<CodeSet>>(emptyList())
    val codeSets: StateFlow<List<CodeSet>> = _codeSets.asStateFlow()

    /** 当前选中的码集索引（在 _codeSets 中的位置）。 */
    private val _currentIndex = MutableStateFlow(0)
    val currentIndex: StateFlow<Int> = _currentIndex.asStateFlow()

    /** 自动匹配是否在运行中。 */
    private val _autoMatching = MutableStateFlow(false)
    val autoMatching: StateFlow<Boolean> = _autoMatching.asStateFlow()

    private val _groupId = MutableStateFlow<Long?>(null)
    val groupId: StateFlow<Long?> = _groupId.asStateFlow()

    private val _remoteName = MutableStateFlow("")
    val remoteName: StateFlow<String> = _remoteName.asStateFlow()

    private var autoMatchJob: Job? = null

    init {
        viewModelScope.launch {
            _groupId.value = repo.observeGroups().first().firstOrNull()?.id
        }
    }

    /** 当前码集（不可变视图）。 */
    fun currentCodeSet(): CodeSet? =
        _codeSets.value.getOrNull(_currentIndex.value)

    fun currentBrandName(): String? {
        val b = _brandId.value ?: return null
        return codes.brand(b)?.name
    }

    fun selectType(type: DeviceType) {
        _type.value = type
        _brandId.value = null
        _codeSets.value = emptyList()
        _currentIndex.value = 0
        _step.value = Step.BRAND
    }

    fun selectBrand(brandId: String) {
        _brandId.value = brandId
        val sets = codes.codeSetsFor(brandId)
        _codeSets.value = sets
        _currentIndex.value = 0
        _remoteName.value = codes.brand(brandId)?.name.orEmpty()
        _step.value = Step.TEST
    }

    /** 切换到上一组码集（环形）。 */
    fun prevCodeSet() {
        if (_codeSets.value.isEmpty()) return
        stopAutoMatch()
        val n = _codeSets.value.size
        _currentIndex.value = (_currentIndex.value - 1 + n) % n
    }

    /** 切换到下一组码集（环形）。 */
    fun nextCodeSet() {
        if (_codeSets.value.isEmpty()) return
        stopAutoMatch()
        val n = _codeSets.value.size
        _currentIndex.value = (_currentIndex.value + 1) % n
    }

    /**
     * 自动循环匹配：依次切换每组码集并发射电源键，每组间隔 2 秒，供用户观察设备反应。
     * 再次调用或调用 [stopAutoMatch] 即停止。
     */
    fun toggleAutoMatch() {
        if (_autoMatching.value) {
            stopAutoMatch()
        } else {
            startAutoMatch()
        }
    }

    private fun startAutoMatch() {
        if (_codeSets.value.isEmpty()) return
        _autoMatching.value = true
        autoMatchJob = viewModelScope.launch {
            val total = _codeSets.value.size
            var i = _currentIndex.value
            // 最多循环两轮，给用户足够观察时间
            repeat(total * 2) {
                if (!_autoMatching.value) return@launch
                _currentIndex.value = i % total
                transmitKey(RemoteKey.POWER)
                delay(2200)
                i++
            }
            _autoMatching.value = false
        }
    }

    fun stopAutoMatch() {
        autoMatchJob?.cancel()
        autoMatchJob = null
        _autoMatching.value = false
    }

    /** 用当前码集发射指定按键。 */
    fun transmitKey(key: RemoteKey) {
        val cs = currentCodeSet() ?: return
        codes.patternFor(cs.id, key)?.let { (carrier, pattern) ->
            ir.transmit(carrier, pattern)
        }
    }

    fun onNameChange(name: String) {
        _remoteName.value = name
    }

    fun selectGroup(id: Long) {
        _groupId.value = id
    }

    fun goNextToSave() {
        stopAutoMatch()
        _step.value = Step.SAVE
    }

    fun back(): Boolean {
        stopAutoMatch()
        when (_step.value) {
            Step.TYPE -> return false
            Step.BRAND -> {
                _type.value = null
                _step.value = Step.TYPE
            }
            Step.TEST -> {
                _brandId.value = null
                _codeSets.value = emptyList()
                _currentIndex.value = 0
                _step.value = Step.BRAND
            }
            Step.SAVE -> _step.value = Step.TEST
        }
        return true
    }

    fun save(onDone: () -> Unit) {
        val t = _type.value ?: return
        val b = _brandId.value ?: return
        val cs = currentCodeSet() ?: return
        val name = _remoteName.value.ifBlank { t.displayName }
        viewModelScope.launch {
            val gid = _groupId.value ?: repo.observeGroups().first().firstOrNull()?.id
            if (gid == null) {
                // 没有分组则新建一个默认分组
                val newId = repo.addGroup("客厅")
                repo.addRemote(newId, name, t.id, b, cs.id)
            } else {
                repo.addRemote(gid, name, t.id, b, cs.id)
            }
            onDone()
        }
    }
}
