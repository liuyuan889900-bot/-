package com.example.data.db

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import com.example.data.dao.BookDao
import com.example.data.entity.BookmarkEntity
import com.example.data.entity.BookEntity
import com.example.data.entity.ChapterEntity
import com.example.data.entity.ReadingNoteEntity
import com.example.data.entity.ReadingProgressEntity
import com.example.data.entity.SearchHistoryEntity

@Database(
    entities = [
        BookEntity::class,
        ChapterEntity::class,
        BookmarkEntity::class,
        ReadingNoteEntity::class,
        SearchHistoryEntity::class,
        ReadingProgressEntity::class
    ],
    version = 4,
    exportSchema = false
)
abstract class AppDatabase : RoomDatabase() {
    abstract fun bookDao(): BookDao

    companion object {
        @Volatile
        private var INSTANCE: AppDatabase? = null

        fun getDatabase(context: Context): AppDatabase {
            return INSTANCE ?: synchronized(this) {
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    AppDatabase::class.java,
                    "ducongshu_database"
                )
                .fallbackToDestructiveMigration()
                .build()
                INSTANCE = instance
                instance
            }
        }
    }
}
