package ml.hazyalex.aam.ui.search

import android.app.AlertDialog
import android.os.Bundle
import android.util.Log
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
import kotlinx.serialization.builtins.ListSerializer
import kotlinx.serialization.json.*
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
import java.util.Locale

class SearchFragment : Fragment() {
    private lateinit var animeAdapter: MasonryAdapter
    private lateinit var searchView: SearchView
    var filterGenre: Int? = null


    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?,
    ): View? {
        val root = inflater.inflate(R.layout.fragment_search, container, false)
        (activity as AppCompatActivity?)?.supportActionBar?.show()

        // Listen to search query's
        searchView = root.findViewById(R.id.searchview_anime)
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
        if (filterGenre != null) {
            parameters["genre"] = filterGenre.toString()
        }


        API.search(parameters, object : Callback {
            override fun onFailure(call: Call, e: IOException) {
                showError(e.message)
            }

            override fun onResponse(call: Call, response: Response) {
                if (!response.isSuccessful) {
                    showError(response.message.capitalize(Locale.ROOT))
                    return
                }

                val jsonResponse = API.jsonParser.parseToJsonElement(response.body?.string()!!).jsonObject

                val results = API.jsonParser.decodeFromJsonElement(
                    ListSerializer(Anime.serializer()),
                    jsonResponse.getValue("results").jsonArray
                )

                if (results.isEmpty()) {
                    showError("No results found.")
                    return
                }

                animeAdapter.add(activity, results)
            }
        })
    }

    fun clearSearchResults() {
        activity?.runOnUiThread {
            animeAdapter.clearUI(activity)
            searchView.clearFocus() // Prevent onQueryTextSubmit getting called twice
        }
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
