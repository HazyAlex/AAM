package ml.hazyalex.aam.model

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey
import kotlinx.serialization.*
import kotlinx.serialization.descriptors.SerialDescriptor
import kotlinx.serialization.descriptors.buildClassSerialDescriptor
import kotlinx.serialization.encoding.Decoder
import kotlinx.serialization.encoding.Encoder
import kotlinx.serialization.json.*
import java.util.Locale
import kotlin.Comparator

@Serializable(with = AnimeSerializer::class)
@Entity(tableName = "anime")
data class Anime(
    @SerialName("mal_id")
    @PrimaryKey @ColumnInfo(name = "anime_id")
    val idMyAnimeList: Long,

    @SerialName("title")
    val title: String,

    @SerialName("title_japanese")
    val title_japanese: String? = null,

    @SerialName("image_url")
    val image_url: String? = null,

    @SerialName("synopsis")
    val synopsis: String? = null,

    @SerialName("type")
    val type: String? = null,

    @SerialName("status")
    val status: String? = null,

    @SerialName("episodes")
    val episodes: Int? = null,

    @SerialName("genres")
    val genres: String? = null,

    @SerialName("r18")
    val adult: Boolean? = false,

    @SerialName("kids")
    val kids: Boolean? = false,

    @SerialName("source")
    val source: String? = null,

    @SerialName("rating")
    val rating: String? = null,

    @SerialName("score")
    val score: Float? = null,

    // Database - Not Serialized/Deserialized
    @Transient
    var updated: Boolean = false,

    @Transient
    var season_year: Int? = null,

    @Transient
    var season_name: String? = null,
)


@Serializer(forClass = Anime::class)
class AnimeSerializer(
    // Throwing errors if left to null, this should be done automatically by the library?
    override val descriptor: SerialDescriptor = buildClassSerialDescriptor(AnimeSerializer::class.toString()),
) : KSerializer<Anime> {
    override fun deserialize(decoder: Decoder): Anime {
        val input = decoder as? JsonDecoder
            ?: throw SerializationException("This class can be loaded only by Json")
        val tree = input.decodeJsonElement() as? JsonObject
            ?: throw SerializationException("Expected JsonObject")

        return Anime(
            idMyAnimeList = tree["mal_id"]!!.jsonPrimitive.long,
            title = tree["title"]!!.jsonPrimitive.content,
            title_japanese = tree["title_japanese"]?.jsonPrimitive?.contentOrNull,
            image_url = tree["image_url"]?.jsonPrimitive?.contentOrNull,
            synopsis = tree["synopsis"]?.jsonPrimitive?.contentOrNull,
            type = tree["type"]?.jsonPrimitive?.contentOrNull,
            status = tree["status"]?.jsonPrimitive?.contentOrNull,
            episodes = tree["episodes"]?.jsonPrimitive?.intOrNull,
            genres = tree["genres"]?.jsonArray?.run {
                val str = StringBuilder()
                this.forEach { str.append(it.jsonObject["name"]?.jsonPrimitive?.contentOrNull).append(", ") }

                if (str.lastIndex > 2) {
                    // Delete the last ", "
                    str.deleteCharAt(str.lastIndex)
                    str.deleteCharAt(str.lastIndex)
                    str.toString()
                } else {
                    null
                }
            },
            adult = tree["r18"]?.jsonPrimitive?.booleanOrNull ?: false,
            kids = tree["kids"]?.jsonPrimitive?.booleanOrNull ?: false,
            source = tree["source"]?.jsonPrimitive?.contentOrNull,
            rating = tree["rating"]?.jsonPrimitive?.contentOrNull,
            score = tree["score"]?.jsonPrimitive?.floatOrNull
        )
    }

    override fun serialize(encoder: Encoder, value: Anime) {
        error("Not implemented")
    }
}

class AnimeSort: Comparator<Anime> {
    private fun score(type: String): Int {
        // tv, ova, movie, special, ona, music
        return when (type.lowercase(Locale.ROOT)) {
            "tv" -> Int.MIN_VALUE + 1
            "special" -> Int.MIN_VALUE + 2
            "ova" -> Int.MIN_VALUE + 3
            "movie" -> Int.MIN_VALUE + 4
            "ona" -> Int.MIN_VALUE + 5
            "music" -> Int.MIN_VALUE + 6
            else -> Int.MIN_VALUE + 7
        }
    }

    // Order by Type -> Title
    override fun compare(c1: Anime?, c2: Anime?): Int {
        if (c1 == null || c2 == null) return 0

        if (c1.type == c2.type) return c1.title.compareTo(c2.title)

        if (c1.type == null) return -1
        if (c2.type == null) return 1
        return score(c1.type).compareTo(score(c2.type))
    }
}
