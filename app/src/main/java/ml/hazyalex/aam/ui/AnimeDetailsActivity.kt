package ml.hazyalex.aam.ui

import android.app.AlertDialog
import android.os.Bundle
import android.util.Log
import android.view.View
import android.widget.ImageView
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import kotlinx.android.synthetic.main.activity_anime_details.*
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import ml.hazyalex.aam.R
import ml.hazyalex.aam.database.AnimeDB
import ml.hazyalex.aam.model.*
import okhttp3.Call
import okhttp3.Callback
import okhttp3.Response
import java.io.IOException

class AnimeDetailsActivity : AppCompatActivity() {
    private var selectedAnimeID: Long = 0
    private lateinit var customLists: List<CustomList>
    private lateinit var customListTitles: Array<String>

    // UI
    private val unknownField: String = "Unknown"
    private val unknownSynopsis: String = "No synopsis found."

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_anime_details)

        setSupportActionBar(toolbar)
        supportActionBar?.setDisplayShowTitleEnabled(false)

        CoroutineScope(Dispatchers.IO).launch {
            selectedAnimeID = intent.getLongExtra("ID", 0)
            val selectedAnime = AnimeDB.getInstance(application).animeDAO().getAnime(selectedAnimeID)

            // If we don't have a record of the anime or if it's not updated, update it
            if (selectedAnime == null || !selectedAnime.updated) {
                getAnimeDetails(selectedAnime?.season_name, selectedAnime?.season_year)
                return@launch
            }

            // Otherwise show the cached version
            Log.d("DETAILS_ANIME_ACTIVITY", "SHOWING CACHED VERSION")
            addToUI(selectedAnime)
        }

        CoroutineScope(Dispatchers.Default).launch {
            customLists = AnimeDB.getInstance(applicationContext).customListDAO().getAll()
            customListTitles = Array(customLists.size, init = { customLists[it].title })
        }
    }

    private fun getAnimeDetails(season_name: String?, season_year: Int?) {
        API.getAnimeDetails(selectedAnimeID, object : Callback {
            override fun onFailure(call: Call, e: IOException) {
                Log.e("DETAILS_ANIME_ERROR_STACK", e.printStackTrace().toString())
                Log.e("DETAILS_ANIME_ERROR_MESSAGE", e.message.toString())

                runOnUiThread {
                    Toast.makeText(applicationContext, e.message, Toast.LENGTH_LONG).show()
                }
            }

            override fun onResponse(call: Call, response: Response) {
                val jsonResponse = response.body?.string()!!
                val anime = API.jsonParser.parse(Anime.serializer(), jsonResponse)

                // We have an updated version now that we have the full data
                anime.updated = true
                anime.season_year = season_year
                anime.season_name = season_name

                if (season_name != null && season_year != null) {
                    Log.d("DETAILS_ANIME_ACTIVITY", "UPDATING $selectedAnimeID")
                    AnimeDB.getInstance(applicationContext).animeDAO().update(anime)
                } else {
                    Log.d("DETAILS_ANIME_ACTIVITY", "CREATING $selectedAnimeID")
                    AnimeDB.getInstance(applicationContext).animeDAO().insert(anime)
                }

                addToUI(anime)
            }
        })
    }

    private fun addToUI(anime: Anime) {
        runOnUiThread {
            val header = findViewById<ImageView>(R.id.header)
            loadImage(anime.image_url, header, applicationContext)

            findViewById<TextView>(R.id.anime_details_title).text = anime.title
            findViewById<TextView>(R.id.anime_details_status).text = anime.status ?: ""
            findViewById<TextView>(R.id.anime_details_title_japanese).text = anime.title_japanese ?: ""
            findViewById<TextView>(R.id.anime_details_synopsis).text = anime.synopsis ?: unknownSynopsis
            findViewById<TextView>(R.id.anime_details_episodes).text = anime.episodes?.toString() ?: unknownField
            findViewById<TextView>(R.id.anime_details_source).text = anime.source ?: unknownField
            findViewById<TextView>(R.id.anime_details_type).text = anime.type ?: unknownField
            findViewById<TextView>(R.id.anime_details_rating).text = anime.rating ?: unknownField
            findViewById<TextView>(R.id.anime_details_genres).text = anime.genres ?: unknownField
        }
    }

    fun addToList(view: View) {
        if (customListTitles.isEmpty()) {
            AlertDialog.Builder(view.context).setTitle("There are no custom lists available.")
                .setMessage("") // So we can have some padding below the title
                .create().show()
            return
        }

        val dialog = AlertDialog.Builder(view.context)
            .setTitle("Add Anime to List")
            .setItems(customListTitles) { _, position: Int ->
                CoroutineScope(Dispatchers.Default).launch {
                    val selectedListID = customLists[position].customListID

                    // Check if it's not already there
                    val exists = AnimeDB.getInstance(view.context).customListDAO()
                        .exists(selectedListID, selectedAnimeID)

                    if (exists) {
                        runOnUiThread {
                            Toast.makeText(view.context, "Already in the list!", Toast.LENGTH_LONG).show()
                        }
                        return@launch
                    }

                    // Add it to the cross-reference table in the DB
                    AnimeDB.getInstance(view.context).customListDAO().insert(
                        CustomListAnimeCross(selectedListID, selectedAnimeID)
                    )
                    runOnUiThread {
                        Toast.makeText(view.context, "Success!", Toast.LENGTH_LONG).show()
                    }
                }
            }

        dialog.create()
        dialog.show()
    }
}
