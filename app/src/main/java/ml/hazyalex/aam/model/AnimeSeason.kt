package ml.hazyalex.aam.model

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.Ignore
import kotlinx.serialization.*
import kotlinx.serialization.json.JsonInput
import kotlinx.serialization.json.JsonObject
import kotlinx.serialization.json.content
import kotlinx.serialization.json.int


@Serializable(with = SeasonSerializer::class)
@Entity(tableName = "seasons", primaryKeys = ["season_name", "season_year"])
data class AnimeSeason(
    @SerialName("season_name")
    @ColumnInfo(name = "season_name")
    var name: String,

    @SerialName("season_year")
    @ColumnInfo(name = "season_year")
    var year: Int,
) {
    @Transient
    @Ignore
    var anime: List<Anime> = ArrayList()
}


@Serializer(forClass = AnimeSeason::class)
class SeasonSerializer(
    override val descriptor: SerialDescriptor,
) : KSerializer<AnimeSeason> {
    override fun deserialize(decoder: Decoder): AnimeSeason {
        val input = decoder as? JsonInput
            ?: throw SerializationException("This class can be loaded only by Json")
        val tree = input.decodeJson() as? JsonObject
            ?: throw SerializationException("Expected JsonObject")

        val seasonName = tree["season_name"]!!.content
        val seasonYear = tree["season_year"]!!.int
        val animeJSON = tree.getArray("anime")


        // This fails on the library code as of 08/2020, I think it's because I'm using a custom serializer.
        //val animeList = input.json.fromJson(Anime.serializer().list, tree.getArray("anime"))

        val animeList = ArrayList<Anime>(animeJSON.size)
        animeJSON.forEach {
            val currentAnime = input.json.fromJson(Anime.serializer(), it)
            animeList.add(currentAnime)
        }

        return AnimeSeason(seasonName, seasonYear).also { it.anime = animeList }
    }

    override fun serialize(encoder: Encoder, value: AnimeSeason) {
        error("Not implemented")
    }
}
