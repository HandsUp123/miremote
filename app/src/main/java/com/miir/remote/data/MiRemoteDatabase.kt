package com.miir.remote.data

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import com.miir.remote.data.dao.GroupDao
import com.miir.remote.data.dao.RemoteDao
import com.miir.remote.data.entity.GroupEntity
import com.miir.remote.data.entity.RemoteEntity

@Database(
    entities = [GroupEntity::class, RemoteEntity::class],
    version = 1,
    exportSchema = false
)
abstract class MiRemoteDatabase : RoomDatabase() {

    abstract fun groupDao(): GroupDao
    abstract fun remoteDao(): RemoteDao

    companion object {
        @Volatile
        private var INSTANCE: MiRemoteDatabase? = null

        fun get(context: Context): MiRemoteDatabase =
            INSTANCE ?: synchronized(this) {
                INSTANCE ?: Room.databaseBuilder(
                    context.applicationContext,
                    MiRemoteDatabase::class.java,
                    "mi_remote.db"
                ).fallbackToDestructiveMigration().build().also { INSTANCE = it }
            }
    }
}
