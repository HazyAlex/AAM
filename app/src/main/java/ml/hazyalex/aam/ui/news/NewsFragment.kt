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
import kotlinx.serialization.json.Json
import kotlinx.serialization.json.JsonConfiguration
import ml.hazyalex.aam.R
import ml.hazyalex.aam.database.AnimeDB
import ml.hazyalex.aam.model.API
import ml.hazyalex.aam.model.AnimeSeason
import ml.hazyalex.aam.model.Season
import ml.hazyalex.aam.ui.adapter.MasonryAdapter
import ml.hazyalex.aam.ui.adapter.SpacesItemDecoration
import okhttp3.Call
import okhttp3.Callback
import okhttp3.Response
import java.io.IOException
import java.time.Year


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

            animeAdapter.addAnimeToView(activity, animeList)
            return
        }

        // Otherwise we need to send a request!
        API.getAnimeCurrentSeason(object : Callback {
            override fun onFailure(call: Call, e: IOException) {
                activity?.runOnUiThread {
                    Toast.makeText(context, e.message?.capitalize(), Toast.LENGTH_LONG).show()
                }
            }

            override fun onResponse(call: Call, response: Response) {
                if (!response.isSuccessful) {
                    activity?.runOnUiThread {
                        Toast.makeText(context, "The service is down.", Toast.LENGTH_LONG).show()
                    }
                    return
                }

                val responseString = response.body?.string()!!
                val season = API.jsonParser.parse(AnimeSeason.serializer(), responseString)

                AnimeDB.getInstance(context!!).seasonDAO().insertSeasonWithAnime(season)
                animeAdapter.addAnimeToView(activity, season.anime)
            }
        })
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
            activity?.runOnUiThread {
                Toast.makeText(context, "Invalid year!", Toast.LENGTH_LONG).show()
            }
            return
        }

        val seasonSpinner = activity?.findViewById<Spinner>(R.id.news_season)!!
        if (!seasonSpinner.isSelected) return

        CoroutineScope(Dispatchers.IO).launch {
            getAnime(seasonSpinner.selectedItem.toString(), selectedYear)
        }
    }
}
