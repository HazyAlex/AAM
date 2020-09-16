package ml.hazyalex.aam.database

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import ml.hazyalex.aam.model.*


@Database(
    entities = [AnimeSeason::class, Anime::class, CustomList::class, CustomListAnimeCross::class],
    version = 1,
    exportSchema = false
)
abstract class AnimeDB : RoomDatabase() {
    abstract fun animeDAO(): AnimeDAO
    abstract fun seasonDAO(): SeasonDAO
    abstract fun customListDAO(): CustomListDAO

    companion object : SingletonHolder<AnimeDB, Context>({
        Room.databaseBuilder(
            it.applicationContext, AnimeDB::class.java, "aam.db"
        ).fallbackToDestructiveMigration().build()
    })
}
