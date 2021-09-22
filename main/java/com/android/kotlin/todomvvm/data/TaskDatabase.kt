package com.android.kotlin.todomvvm.data

import androidx.room.Database
import androidx.room.RoomDatabase
import androidx.sqlite.db.SupportSQLiteDatabase
import com.android.kotlin.todomvvm.di.ApplicationScope
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.launch
import javax.inject.Inject
import javax.inject.Provider

@Database(entities = [Task::class], version = 1)
abstract class TaskDatabase : RoomDatabase() {

    abstract fun taskDao(): TaskDao

    class CallBack @Inject constructor(
        private val database: Provider<TaskDatabase>,
        @ApplicationScope private val applicationScope: CoroutineScope
    ) : RoomDatabase.Callback() {
        override fun onCreate(db: SupportSQLiteDatabase) {
            super.onCreate(db)
            val dao = database.get().taskDao()

            applicationScope.launch {
                dao.insert(Task("Task One"))
                dao.insert(Task("Task Two"))
                dao.insert(Task("Task Three", important = true))
                dao.insert(Task("Task Four"))
                dao.insert(Task("Task Five", completed = true))
                dao.insert(Task("Task Six"))
                dao.insert(Task("Task Seven"))
                dao.insert(Task("Task Eight", completed = true))
            }

        }
    }

}