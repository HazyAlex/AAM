package ml.hazyalex.aam.ui

import android.app.AlertDialog
import android.os.Bundle
import android.util.Log
import android.view.View
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.constraintlayout.widget.ConstraintLayout
import com.google.android.material.appbar.AppBarLayout
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
    private lateinit var customLists: MutableList<CustomList>

    // UI
    private val unknownField: String = "Unknown"
    private val unknownSynopsis: String = "No synopsis found."

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_anime_details)

        supportActionBar?.setDisplayShowTitleEnabled(false)

        CoroutineScope(Dispatchers.IO).launch {
            // An ID of '0' will never exist, we can safely use it as a null value.
            selectedAnimeID = intent.getLongExtra("ID", 0L)
            val selectedAnime = AnimeDB.getInstance(application).animeDAO().getAnime(selectedAnimeID)

            // Get the lists that don't have this anime inside, so we can show them in the UI.
            val lists = AnimeDB.getInstance(application)
                .customListDAO()
                .listsWithoutAnime(selectedAnimeID)

            customLists = MutableList(lists.size, init = {lists[it] })

            // If we don't have a record of the anime in the DB or if it hasn't been updated with the extra information, update it
            if (selectedAnime == null || !selectedAnime.updated) {
                getAnimeDetails(selectedAnime?.season_name, selectedAnime?.season_year)
                return@launch
            }

            // Otherwise show the cached version
            Log.d("AAM", "DETAILS_ANIME_ACTIVITY - SHOWING CACHED VERSION")
            addToUI(selectedAnime)
        }
    }

    fun addToList(view: View) {
        val titles = Array(customLists.size, init = { customLists[it].title })

        if (titles.isEmpty()) {
            AlertDialog.Builder(view.context)
                .setTitle("There are no custom lists available.")
                .setMessage("") // So we can have some padding below the title
                .create().show()
            return
        }

        AlertDialog.Builder(view.context)
            .setTitle("Add Anime to List")
            .setItems(titles) { _, position: Int ->
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

                    customLists.removeAt(position)

                    runOnUiThread {
                        Toast.makeText(view.context, "Added successfully!", Toast.LENGTH_LONG).show()
                    }
                }
            }.create().show()
    }

    private fun getAnimeDetails(season_name: String?, season_year: Int?) {
        API.getAnimeDetails(selectedAnimeID, object : Callback {
            override fun onFailure(call: Call, e: IOException) {
                val stackTrace = e.printStackTrace().toString()
                Log.e("AAM", "DETAILS_ANIME_ERROR_STACK:\n$stackTrace")

                runOnUiThread {
                    Toast.makeText(applicationContext, e.message, Toast.LENGTH_LONG).show()
                }
            }

            override fun onResponse(call: Call, response: Response) {
                val jsonResponse = response.body?.string()!!
                val anime = API.jsonParser.decodeFromString(Anime.serializer(), jsonResponse)

                // We have an updated version now that we have the full data
                anime.updated = true
                anime.season_year = season_year
                anime.season_name = season_name

                if (season_name != null && season_year != null) {
                    Log.d("AAM", "DETAILS_ANIME_ACTIVITY - UPDATING $selectedAnimeID")
                    AnimeDB.getInstance(applicationContext).animeDAO().update(anime)
                } else {
                    Log.d("AAM", "DETAILS_ANIME_ACTIVITY - CREATING $selectedAnimeID")
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

            setVisibleWith(R.id.anime_details_title, anime.title)
            setVisibleWith(R.id.anime_details_status, anime.status ?: "")
            setVisibleWith(R.id.anime_details_title_japanese, anime.title_japanese ?: "")
            setVisibleWith(R.id.anime_details_synopsis, anime.synopsis ?: unknownSynopsis)
            setVisibleWith(R.id.anime_details_episodes, anime.episodes?.toString() ?: unknownField)
            setVisibleWith(R.id.anime_details_source, anime.source ?: unknownField)
            setVisibleWith(R.id.anime_details_type, anime.type ?: unknownField)
            setVisibleWith(R.id.anime_details_rating, anime.rating ?: unknownField)
            setVisibleWith(R.id.anime_details_genres, anime.genres ?: unknownField)

            setLayoutVisible()
        }
    }


    private fun setVisibleWith(id: Int, text: String) {
        val textView = findViewById<TextView>(id) ?: error("not found.")
        textView.text = text
        textView.visibility = View.VISIBLE
    }

    private fun setLayoutVisible() {
        findViewById<ConstraintLayout>(R.id.anime_details_loading).visibility = View.INVISIBLE
        findViewById<LinearLayout>(R.id.anime_details_layout).visibility = View.VISIBLE
        findViewById<AppBarLayout>(R.id.anime_details_app_bar).visibility = View.VISIBLE
    }
}
