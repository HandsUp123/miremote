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
    private val remoteId: Long
) : ViewModel() {

    private val _remote = MutableStateFlow<RemoteEntity?>(null)
    val remote: StateFlow<RemoteEntity?> = _remote.asStateFlow()

    val hasIrEmitter: Boolean = ir.hasIrEmitter

    init {
        viewModelScope.launch { _remote.value = repo.getRemote(remoteId) }
    }

    fun deviceType(): DeviceType? = _remote.value?.let { DeviceType.fromId(it.deviceType) }

    fun transmit(key: RemoteKey) {
        val r = _remote.value ?: return
        val type = DeviceType.fromId(r.deviceType)
        IrCodeDatabase.patternFor(type, r.brandId, key)?.let { (carrier, pattern) ->
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
