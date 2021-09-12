package ml.hazyalex.aam.model

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.Ignore
import kotlinx.serialization.KSerializer
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import kotlinx.serialization.SerializationException
import kotlinx.serialization.Serializer
import kotlinx.serialization.builtins.ListSerializer
import kotlinx.serialization.descriptors.*
import kotlinx.serialization.encoding.Decoder
import kotlinx.serialization.encoding.Encoder
import kotlinx.serialization.json.*
import java.security.InvalidParameterException
import java.text.DateFormat
import java.time.Month
import java.util.Calendar
import kotlin.collections.ArrayList

enum class Season {
    Winter, Spring, Summer, Fall;

    companion object {
        fun getCurrentSeason(): Season {
            val calendar = DateFormat.getDateInstance().calendar

            when (calendar.get(Calendar.MONTH)) {
                Month.DECEMBER.value, Month.JANUARY.value, Month.FEBRUARY.value -> {
                    return Winter
                }
                Month.MARCH.value, Month.MAY.value -> {
                    return Spring
                }
                Month.JUNE.value, Month.JULY.value, Month.AUGUST.value -> {
                    return Summer
                }
                Month.SEPTEMBER.value, Month.NOVEMBER.value -> {
                    return Fall
                }
            }

            throw InvalidParameterException("Invalid Month!")
        }
    }
}

@Serializable(with = AnimeSeasonSerializer::class)
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
object AnimeSeasonSerializer : KSerializer<AnimeSeason> {
    override val descriptor: SerialDescriptor = buildClassSerialDescriptor(AnimeSeasonSerializer::class.toString())

    override fun deserialize(decoder: Decoder): AnimeSeason {
        val input = decoder as? JsonDecoder
            ?: throw SerializationException("This class can be loaded only by Json")
        val tree = input.decodeJsonElement() as? JsonObject
            ?: throw SerializationException("Expected JsonObject")

        if (tree["season_name"] == null || tree["season_year"] == null || tree["season_name"]!! is JsonNull || tree["season_year"]!! is JsonNull)
            throw SerializationException("Invalid season.")

        val seasonName = tree["season_name"]!!.jsonPrimitive.content
        val seasonYear = tree["season_year"]!!.jsonPrimitive.int

        val animeJSON = tree.getValue("anime").jsonArray
        val animeList = input.json.decodeFromJsonElement(ListSerializer(Anime.serializer()), animeJSON)

        return AnimeSeason(seasonName, seasonYear).also { it.anime = animeList }
    }

    override fun serialize(encoder: Encoder, value: AnimeSeason) {
        error("Not implemented")
    }
}
