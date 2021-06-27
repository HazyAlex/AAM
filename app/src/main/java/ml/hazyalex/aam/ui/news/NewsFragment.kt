package ml.hazyalex.aam.ui.news

import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.*
import androidx.appcompat.app.AppCompatActivity
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.RecyclerView
import androidx.recyclerview.widget.StaggeredGridLayoutManager
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.serialization.SerializationException
import ml.hazyalex.aam.R
import ml.hazyalex.aam.database.AnimeDB
import ml.hazyalex.aam.model.*
import ml.hazyalex.aam.ui.adapter.MasonryAdapter
import ml.hazyalex.aam.ui.adapter.SpacesItemDecoration
import okhttp3.Call
import okhttp3.Callback
import okhttp3.Response
import java.io.IOException
import java.time.Year
import java.util.Locale


class NewsFragment : Fragment() {
    private lateinit var animeAdapter: MasonryAdapter


    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?,
    ): View? {
        val view = inflater.inflate(R.layout.fragment_news, container, false)
        (activity as AppCompatActivity?)?.supportActionBar?.hide()

        // Listen for search events
        val searchButton = view.findViewById<Button>(R.id.news_search)
        searchButton.setOnClickListener { onSearchClicked(it) }

        // Setup the list adapter and padding
        animeAdapter = MasonryAdapter(requireContext())

        val animeView = view.findViewById<RecyclerView>(R.id.news_anime_view)
        animeView.layoutManager = StaggeredGridLayoutManager(2, StaggeredGridLayoutManager.VERTICAL)
        animeView.adapter = animeAdapter
        animeView.addItemDecoration(SpacesItemDecoration())


        val currentYear = Year.now().value
        val currentSeason = Season.getCurrentSeason()

        val yearTextView = view.findViewById<TextView>(R.id.news_year)
        yearTextView.text = currentYear.toString()

        val seasonSpinner = view.findViewById<Spinner>(R.id.news_season)
        seasonSpinner.setSelection(currentSeason.ordinal)

        // Get the anime (cached or requested)
        CoroutineScope(Dispatchers.IO).launch { getAnime(currentSeason.name, currentYear) }

        return view
    }

    private fun getAnime(seasonToSearch: String, yearToSearch: Int) {
        animeAdapter.clearUI(activity)

        val animeList = AnimeDB.getInstance(requireContext())
            .seasonDAO().getAnimeList(seasonToSearch, yearToSearch)

        if (animeList.isNotEmpty()) {
            // If it's cached show the cached version
            Log.d("NEWS_FRAGMENT", "SHOWING CACHED VERSION")

            val anime = animeList.sortedWith(AnimeSort())

            animeAdapter.addAnimeToView(activity, anime)
            return
        }

        // Otherwise we need to send a request!
        API.getAnimeCurrentSeason(object : Callback {
            override fun onFailure(call: Call, e: IOException) {
                showError(e.message?.capitalize(Locale.ROOT))
            }

            override fun onResponse(call: Call, response: Response) {
                if (!response.isSuccessful) {
                    showError(response.message)
                    return
                }

                val season: AnimeSeason
                try {
                    season = API.jsonParser.decodeFromString(AnimeSeason.serializer(), response.body?.string()!!)
                } catch (e: SerializationException) {
                    showError(e.message)
                    return
                }

                AnimeDB.getInstance(context!!).seasonDAO().insertSeasonWithAnime(season)
                val anime = season.anime.sortedWith(AnimeSort())

                animeAdapter.addAnimeToView(activity, anime)
            }
        }, yearToSearch, seasonToSearch)
    }

    private fun onSearchClicked(view: View?) {
        val selectedYear: Int

        try {
            val yearEditText: EditText = activity?.findViewById(R.id.news_year)!!
            selectedYear = Integer.parseInt(yearEditText.text.toString())

            if (selectedYear < 1910) {
                throw NumberFormatException()
            }

        } catch (e: NumberFormatException) { // Can fail on parseInt or on the condition
            showError("Invalid year!")
            return
        }

        val seasonSpinner = activity?.findViewById<Spinner>(R.id.news_season)!!
        if (seasonSpinner.selectedItem == null) return

        CoroutineScope(Dispatchers.IO).launch {
            getAnime(seasonSpinner.selectedItem.toString(), selectedYear)
        }
    }

    private fun showError(message: String?) {
        if (message == null) {
            activity?.runOnUiThread {
                Toast.makeText(context, "Error!", Toast.LENGTH_LONG).show()
            }
            return
        }
        activity?.runOnUiThread {
            Toast.makeText(context, "Error: $message", Toast.LENGTH_LONG).show()
        }
    }
}
