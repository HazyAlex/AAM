package ml.hazyalex.aam.model

import androidx.room.*
import ml.hazyalex.aam.R


@Entity
data class CustomList(
    @PrimaryKey(autoGenerate = true)
    var customListID: Long = 0,

    val title: String,
    val color: String,
) {
    companion object {
        // The colors that are available to the user when they are creating a custom list.
        val availableColors: Map<String, Int> = hashMapOf(
            "Black" to R.color.Black, "White" to R.color.White,
            "Red" to R.color.Red, "Green" to R.color.Green, "Blue" to R.color.Blue
        )
    }
}


@Entity(primaryKeys = ["customListID", "anime_id"])
data class CustomListAnimeCross(
    val customListID: Long,

    @ColumnInfo(name = "anime_id")
    val animeID: Long,
)


data class CustomListWithAnime(
    @Embedded
    val customList: CustomList,

    @Relation(
        parentColumn = "customListID",
        entityColumn = "anime_id",
        associateBy = Junction(CustomListAnimeCross::class)
    )
    val anime: List<Anime>,
)
