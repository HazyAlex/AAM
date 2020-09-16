package ml.hazyalex.aam.database

import androidx.room.*
import ml.hazyalex.aam.model.*


@Dao
interface CustomListDAO {
    @Insert
    fun insert(list: CustomList)

    @Query("SELECT * FROM customlist")
    fun getAll(): List<CustomList>

    @Transaction
    @Query("SELECT EXISTS(SELECT 1 FROM customlistanimecross WHERE customListID = :customListID AND anime_id = :animeID)")
    fun exists(customListID: Int, animeID: Long): Boolean

    @Transaction
    @Query("SELECT * FROM customlist WHERE customListID = :customListID")
    fun getCustomListWithAnime(customListID: Int): CustomListWithAnime?

    @Insert
    fun insert(join: CustomListAnimeCross)
}


@Dao
interface AnimeDAO {
    @Insert
    fun insert(anime: Anime)

    @Update
    fun update(anime: Anime)

    @Query("SELECT * FROM anime WHERE anime_id = :id")
    fun getAnime(id: Long): Anime?
}


@Dao
interface SeasonDAO {
    @Insert
    fun insert(animeSeason: AnimeSeason)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    fun insertAnimeList(anime: List<Anime>)

    @Query("SELECT * FROM seasons WHERE season_name = :name AND season_year = :year")
    fun getSeason(name: String, year: Int): AnimeSeason

    @Query("SELECT * FROM anime WHERE season_name = :name AND season_year = :year")
    fun getAnimeList(name: String?, year: Int): List<Anime>

    /**
     * Saves the season and a list of anime from that season
     */
    fun insertSeasonWithAnime(animeSeason: AnimeSeason) {
        val anime: List<Anime> = animeSeason.anime
        for (i in anime.indices) {
            anime[i].season_name = animeSeason.name
            anime[i].season_year = animeSeason.year
        }

        insertAnimeList(anime)
        insert(animeSeason)
    }

    /**
     * Gets the Season and the list of Anime related to it
     *
     * @param name in ("Summer", "Spring", "Fall", "Winter")
     * @param year Specifies the year in which the Anime was released
     */
    fun getSeasonWithAnime(name: String, year: Int): AnimeSeason {
        val animeSeason: AnimeSeason = getSeason(name, year)
        animeSeason.anime = getAnimeList(animeSeason.name, animeSeason.year)
        return animeSeason
    }
}

