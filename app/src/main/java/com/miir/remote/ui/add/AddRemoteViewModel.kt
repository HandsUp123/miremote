package com.miir.remote.ui.add

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.miir.remote.data.entity.GroupEntity
import com.miir.remote.data.repository.RemoteRepository
import com.miir.remote.ir.DeviceType
import com.miir.remote.ir.IrCodeDatabase
import com.miir.remote.ir.IrTransmitter
import com.miir.remote.ir.RemoteKey
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

class AddRemoteViewModel(
    private val repo: RemoteRepository,
    private val ir: IrTransmitter
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

    private val _groupId = MutableStateFlow<Long?>(null)
    val groupId: StateFlow<Long?> = _groupId.asStateFlow()

    private val _remoteName = MutableStateFlow("")
    val remoteName: StateFlow<String> = _remoteName.asStateFlow()

    init {
        viewModelScope.launch {
            _groupId.value = repo.observeGroups().first().firstOrNull()?.id
        }
    }

    fun currentBrandName(): String? {
        val t = _type.value ?: return null
        val b = _brandId.value ?: return null
        return IrCodeDatabase.brandById(b)?.name
    }

    fun selectType(type: DeviceType) {
        _type.value = type
        _brandId.value = null
        _step.value = Step.BRAND
    }

    fun selectBrand(brandId: String) {
        _brandId.value = brandId
        _remoteName.value = IrCodeDatabase.brandById(brandId)?.name.orEmpty()
        _step.value = Step.TEST
    }

    fun transmit(key: RemoteKey) {
        val t = _type.value ?: return
        val b = _brandId.value ?: return
        IrCodeDatabase.patternFor(t, b, key)?.let { (carrier, pattern) ->
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
        _step.value = Step.SAVE
    }

    fun back(): Boolean {
        when (_step.value) {
            Step.TYPE -> return false
            Step.BRAND -> {
                _type.value = null
                _step.value = Step.TYPE
            }
            Step.TEST -> {
                _brandId.value = null
                _step.value = Step.BRAND
            }
            Step.SAVE -> _step.value = Step.TEST
        }
        return true
    }

    fun save(onDone: () -> Unit) {
        val t = _type.value ?: return
        val b = _brandId.value ?: return
        val name = _remoteName.value.ifBlank { t.displayName }
        viewModelScope.launch {
            val gid = _groupId.value ?: repo.observeGroups().first().firstOrNull()?.id
            if (gid == null) {
                // 没有分组则新建一个默认分组
                val newId = repo.addGroup("客厅")
                repo.addRemote(newId, name, t.id, b)
            } else {
                repo.addRemote(gid, name, t.id, b)
            }
            onDone()
        }
    }
}
