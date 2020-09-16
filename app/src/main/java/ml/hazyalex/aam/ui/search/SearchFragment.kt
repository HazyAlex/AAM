package ml.hazyalex.aam.ui.search

import android.app.AlertDialog
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.SearchView
import android.widget.Spinner
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.RecyclerView
import androidx.recyclerview.widget.StaggeredGridLayoutManager
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.serialization.builtins.list
import kotlinx.serialization.json.Json
import kotlinx.serialization.json.JsonConfiguration
import ml.hazyalex.aam.R
import ml.hazyalex.aam.model.API
import ml.hazyalex.aam.model.Anime
import ml.hazyalex.aam.model.Settings
import ml.hazyalex.aam.ui.adapter.MasonryAdapter
import ml.hazyalex.aam.ui.adapter.SpacesItemDecoration
import okhttp3.Call
import okhttp3.Callback
import okhttp3.Response
import java.io.IOException

class SearchFragment : Fragment() {
    private lateinit var animeAdapter: MasonryAdapter
    var filterGenre: Int? = null


    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?,
    ): View? {
        val root = inflater.inflate(R.layout.fragment_search, container, false)
        (activity as AppCompatActivity?)?.supportActionBar?.show()

        // Listen to search query's
        val searchView: SearchView = root.findViewById(R.id.searchview_anime)
        searchView.setOnQueryTextListener(TextListener(this))

        // Setup the advanced search options
        val btnAdvancedSettings = root.findViewById<View>(R.id.btn_advanced_settings)
        btnAdvancedSettings.setOnClickListener(AdvancedSettingsListener(this))

        // Get the UI ready to receive results
        animeAdapter = MasonryAdapter(requireContext())

        val animeView = root.findViewById(R.id.search_anime_view) as RecyclerView
        animeView.layoutManager = StaggeredGridLayoutManager(2, StaggeredGridLayoutManager.VERTICAL)
        animeView.addItemDecoration(SpacesItemDecoration())
        animeView.adapter = animeAdapter

        return root
    }

    override fun onResume() {
        super.onResume()

        // Prevent the SearchView getting focus when we return from the anime details
        requireActivity().findViewById<SearchView>(R.id.searchview_anime).clearFocus()
    }

    fun search(keyword: String) {
        // (q)      Query
        // (type)   Anime Types: tv,ova,movie,special,ona,music
        // (status) Anime status: airing,completed,complete,to_be_aired,tba,upcoming
        // (rated)  Rated: g,pg,pg13,r17,r,rx
        //   ...    genre, order_by, sort
        val parameters: HashMap<String, String> = HashMap(2)
        parameters["q"] = keyword
        parameters["rated"] = if (Settings.getInstance(requireContext()).showAdultContent) "rx" else "pg13"
        if (filterGenre != null) {
            parameters["genre"] = filterGenre.toString()
        }


        API.search(parameters, object : Callback {
            override fun onFailure(call: Call, e: IOException) {
                activity?.runOnUiThread {
                    Toast.makeText(context, e.message, Toast.LENGTH_LONG).show()
                }
            }

            override fun onResponse(call: Call, response: Response) {
                if (!response.isSuccessful) {
                    activity?.runOnUiThread {
                        Toast.makeText(context, response.message.capitalize(), Toast.LENGTH_LONG).show()
                    }
                    return
                }

                val jsonResponse = API.jsonParser.parseJson(response.body?.string()!!).jsonObject
                val results = API.jsonParser.fromJson(Anime.serializer().list, jsonResponse.getArray("results"))

                if (results.isEmpty()) {
                    activity?.runOnUiThread {
                        Toast.makeText(context, "No results found.", Toast.LENGTH_LONG).show()
                    }
                    return
                }

                animeAdapter.addAnimeToView(activity, results)
            }
        })
    }

    fun clearSearchResults() {
        animeAdapter.clearUI(activity)
    }

    fun setActionBarTitle(title: String) {
        activity?.runOnUiThread {
            (activity as AppCompatActivity?)?.supportActionBar?.title = title
        }
    }

    private class AdvancedSettingsListener(private val parent: SearchFragment) : View.OnClickListener {

        override fun onClick(fragmentView: View?) {
            val view = parent.layoutInflater.inflate(R.layout.dialog_advanced_search, null)
            val spinnerGenre: Spinner = view.findViewById(R.id.spinner_genres)
            spinnerGenre.setSelection(parent.filterGenre ?: 0)

            val alertDialog = AlertDialog.Builder(parent.context).setTitle("Advanced Search")
                .setView(view)
                .setNegativeButton("Cancel") { _, _ ->
                    // Nothing changes
                }
                .setNeutralButton("Clear", null)
                .setPositiveButton("Save") { _, _ ->
                    parent.filterGenre = spinnerGenre.selectedItemPosition
                }.create()


            alertDialog.show().also {
                // To prevent the dialog alert from closing on "Clear", we have to override it
                alertDialog.getButton(AlertDialog.BUTTON_NEUTRAL).setOnClickListener {
                    parent.filterGenre = null
                    spinnerGenre.setSelection(0)
                }
            }
        }
    }

    // Note: Returning false closes the keyboard, true keeps it open
    private class TextListener(private val parent: SearchFragment) : SearchView.OnQueryTextListener {

        override fun onQueryTextSubmit(query: String?): Boolean {
            if (query.isNullOrEmpty())
                return true

            if (query.length <= 3) {
                Toast.makeText(parent.context, "The query is too small!", Toast.LENGTH_LONG).show()
                return true
            }

            CoroutineScope(Dispatchers.Default).launch {
                parent.setActionBarTitle("Searching for: $query")
                parent.clearSearchResults()
                parent.search(query)
            }

            return false
        }

        override fun onQueryTextChange(p0: String?): Boolean {
            return true
        }
    }
}

