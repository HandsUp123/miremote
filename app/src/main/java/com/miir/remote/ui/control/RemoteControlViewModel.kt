package com.miir.remote.ui.control

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.miir.remote.data.entity.RemoteEntity
import com.miir.remote.data.repository.RemoteRepository
import com.miir.remote.ir.DeviceType
import com.miir.remote.ir.IrCodeDatabase
import com.miir.remote.ir.IrTransmitter
import com.miir.remote.ir.RemoteKey
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

class RemoteControlViewModel(
    private val repo: RemoteRepository,
    private val ir: IrTransmitter,
    private val codes: IrCodeDatabase,
    private val remoteId: Long
) : ViewModel() {

    private val _remote = MutableStateFlow<RemoteEntity?>(null)
    val remote: StateFlow<RemoteEntity?> = _remote.asStateFlow()

    val hasIrEmitter: Boolean = ir.hasIrEmitter

    init {
        viewModelScope.launch { _remote.value = repo.getRemote(remoteId) }
    }

    fun deviceType(): DeviceType? = _remote.value?.let { DeviceType.fromId(it.deviceType) }

    /** 当前遥控器命中的码集名（用于在控制屏显示来源）。 */
    fun currentCodeSetName(): String? =
        _remote.value?.modelId?.takeIf { it.isNotEmpty() }?.let { codes.codeSet(it)?.name }

    /** 当前码集协议是否被 App 实现（用于在控制屏提示不可发射）。 */
    fun isCodeSetSupported(): Boolean {
        val r = _remote.value ?: return false
        if (r.modelId.isEmpty()) return false
        return codes.codeSet(r.modelId)?.isSupported ?: false
    }

    fun transmit(key: RemoteKey) {
        val r = _remote.value ?: return
        if (r.modelId.isEmpty()) return
        codes.patternFor(r.modelId, key)?.let { (carrier, pattern) ->
            ir.transmit(carrier, pattern)
        }
    }

    fun rename(name: String) {
        viewModelScope.launch {
            repo.renameRemote(remoteId, name)
            _remote.value = _remote.value?.copy(name = name)
        }
    }

    fun delete(onDeleted: () -> Unit) {
        viewModelScope.launch {
            _remote.value?.let { repo.deleteRemote(it) }
            onDeleted()
        }
    }
}
