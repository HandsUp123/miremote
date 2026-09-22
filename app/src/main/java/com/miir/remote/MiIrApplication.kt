package com.miir.remote

import android.app.Application
import android.content.Context
import com.miir.remote.data.MiRemoteDatabase
import com.miir.remote.data.repository.RemoteRepository
import com.miir.remote.ir.IrCodeDatabase
import com.miir.remote.ir.IrTransmitter

class MiIrApplication : Application() {

    lateinit var container: AppContainer
        private set

    override fun onCreate() {
        super.onCreate()
        container = AppContainer(this)
    }
}

/**
 * 应用级依赖容器，供各页面 ViewModel 通过 [androidx.lifecycle.viewmodel.compose.viewModel]
 * 工厂获取。
 */
class AppContainer(context: Context) {
    val database = MiRemoteDatabase.get(context)
    val repository = RemoteRepository(database.groupDao(), database.remoteDao())
    val irTransmitter = IrTransmitter(context)
    val irCodeDatabase = IrCodeDatabase(context)
}
