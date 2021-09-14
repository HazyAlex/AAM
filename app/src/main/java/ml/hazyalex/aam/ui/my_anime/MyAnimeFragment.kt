package ml.hazyalex.aam.ui.my_anime

import android.app.AlertDialog
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ArrayAdapter
import android.widget.EditText
import android.widget.Spinner
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.RecyclerView
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import ml.hazyalex.aam.R
import ml.hazyalex.aam.database.AnimeDB
import ml.hazyalex.aam.model.CustomList
import ml.hazyalex.aam.model.availableColors
import ml.hazyalex.aam.ui.adapter.GalleryAdapter
import ml.hazyalex.aam.ui.adapter.MarginItemDecoration


class MyAnimeFragment : Fragment() {
    private lateinit var mAdapter: GalleryAdapter


    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?,
    ): View? {
        val root = inflater.inflate(R.layout.fragment_my_anime, container, false)
        (activity as AppCompatActivity?)?.supportActionBar?.show()

        mAdapter = GalleryAdapter(activity)

        val mRecyclerView = root.findViewById(R.id.myanime_view) as RecyclerView
        mRecyclerView.layoutManager = GridLayoutManager(context, 3)
        mRecyclerView.setHasFixedSize(true)
        mRecyclerView.addItemDecoration(MarginItemDecoration())
        mRecyclerView.adapter = mAdapter

        // Get the custom lists and show them
        CoroutineScope(Dispatchers.IO).launch {
            val customLists = AnimeDB.getInstance(requireContext()).customListDAO().getAll()
            mAdapter.add(customLists)
        }

        // Floating action -> Create and save a new Custom List
        root.findViewById<View>(R.id.fab).setOnClickListener {
            val view = inflater.inflate(R.layout.dialog_new_list, null)

            val spinner: Spinner = view.findViewById(R.id.custom_list_colors)
            val title: EditText = view.findViewById(R.id.custom_list_name)

            spinner.adapter = ArrayAdapter(requireContext(),
                android.R.layout.simple_spinner_item, availableColors.keys.toTypedArray())

            AlertDialog.Builder(context).setTitle("Custom List")
                .setView(view)
                .setPositiveButton("OK") { _, _ ->
                    val selectedTitle = title.text.toString()
                    val selectedColor = spinner.selectedItem.toString()

                    if (mAdapter.titleExists(selectedTitle)) {
                        Toast.makeText(context, "The chosen name already exists!", Toast.LENGTH_LONG).show()
                        return@setPositiveButton
                    }

                    saveList(selectedTitle, selectedColor)
                }.create().show()
        }

        return root
    }

    private fun saveList(selectedTitle: String, selectedColor: String) {
        // Auto-generate ID, only used so we can store it in the DB
        val list = CustomList(title = selectedTitle, color = selectedColor)

        CoroutineScope(Dispatchers.IO).launch {
            AnimeDB.getInstance(requireContext()).customListDAO().insert(list)
        }

        mAdapter.add(list)
    }
}
