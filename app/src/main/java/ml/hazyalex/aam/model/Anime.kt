package ml.hazyalex.aam.model

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey
import kotlinx.serialization.*
import kotlinx.serialization.json.*

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
    override val descriptor: SerialDescriptor = SerialDescriptor(AnimeSerializer::class.toString()),
) : KSerializer<Anime> {
    override fun deserialize(decoder: Decoder): Anime {
        val input = decoder as? JsonInput
            ?: throw SerializationException("This class can be loaded only by Json")
        val tree = input.decodeJson() as? JsonObject
            ?: throw SerializationException("Expected JsonObject")

        return Anime(
            idMyAnimeList = tree["mal_id"]!!.long,
            title = tree["title"]!!.content,
            title_japanese = tree["title_japanese"]?.contentOrNull,
            image_url = tree["image_url"]?.contentOrNull,
            synopsis = tree["synopsis"]?.contentOrNull,
            type = tree["type"]?.contentOrNull,
            status = tree["status"]?.contentOrNull,
            episodes = tree["episodes"]?.intOrNull,
            genres = tree["genres"]?.jsonArray?.run {
                val str = StringBuilder()
                this.forEach { str.append(it.jsonObject["name"]?.contentOrNull).append(", ") }

                if (str.lastIndex > 2) {
                    // Delete the last ", "
                    str.deleteCharAt(str.lastIndex)
                    str.deleteCharAt(str.lastIndex)
                    str.toString()
                } else {
                    null
                }
            },
            adult = tree["r18"]?.booleanOrNull ?: false,
            kids = tree["kids"]?.booleanOrNull ?: false,
            source = tree["source"]?.contentOrNull,
            rating = tree["rating"]?.contentOrNull,
            score = tree["score"]?.floatOrNull
        )
    }

    override fun serialize(encoder: Encoder, value: Anime) {
        error("Not implemented")
    }
}
