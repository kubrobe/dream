package uz.dream.app.data

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase

@Database(
    entities = [
        InfoCard::class, Course::class, Lesson::class, University::class,
        StepDone::class, Enrollment::class, Interest::class,
        Message::class, Question::class, LocalAccount::class
    ],
    version = 1,
    exportSchema = false
)
abstract class AppDatabase : RoomDatabase() {
    abstract fun dao(): DreamDao

    companion object {
        @Volatile private var INSTANCE: AppDatabase? = null

        // Singleton: the whole app shares ONE database object
        fun get(context: Context): AppDatabase =
            INSTANCE ?: synchronized(this) {
                INSTANCE ?: Room.databaseBuilder(
                    context.applicationContext, AppDatabase::class.java, "dream.db"
                )
                    // during development: if you change an @Entity, increase "version"
                    // and the old database is deleted and re-created
                    .fallbackToDestructiveMigration(dropAllTables = true)
                    .build()
                    .also { INSTANCE = it }
            }
    }
}
