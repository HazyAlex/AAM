package ml.hazyalex.aam.ui

import android.app.AlertDialog
import android.os.Bundle
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.RecyclerView
import androidx.recyclerview.widget.StaggeredGridLayoutManager
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import ml.hazyalex.aam.R
import ml.hazyalex.aam.database.AnimeDB
import ml.hazyalex.aam.model.AnimeSort
import ml.hazyalex.aam.ui.adapter.MasonryAdapter
import ml.hazyalex.aam.ui.adapter.SpacesItemDecoration

class CustomListActivity : AppCompatActivity() {
    private lateinit var animeAdapter: MasonryAdapter


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_customlist)

        animeAdapter = MasonryAdapter(this)

        val animeView = findViewById<RecyclerView>(R.id.customlist_anime_view)
        animeView.layoutManager = StaggeredGridLayoutManager(2, StaggeredGridLayoutManager.VERTICAL)
        animeView.addItemDecoration(SpacesItemDecoration())
        animeView.adapter = animeAdapter

        val selectedCustomListID = intent.getLongExtra("ID", 0)
        if (selectedCustomListID == 0L) return

        animeAdapter.onLongPressListener = {
            AlertDialog.Builder(this)
                .setTitle("Do you want to remove this entry?")
                .setPositiveButton("OK") { _, _ ->
                    val textView = it?.findViewById(R.id.item_text) as TextView
                    val index = textView.tag as Int

                    val animeID = animeAdapter.getID(index)

                    CoroutineScope(Dispatchers.IO).launch {
                        val deletedRows = AnimeDB.getInstance(applicationContext)
                            .customListDAO()
                            .deleteAnime(selectedCustomListID, animeID)

                        if (deletedRows > 0) {
                            animeAdapter.remove(it, index)
                        }
                    }
                }.create().show()

            true // Return true if it was handled correctly
        }

        CoroutineScope(Dispatchers.IO).launch {
            val selectedCustomList = AnimeDB.getInstance(applicationContext)
                .customListDAO()
                .getCustomListWithAnime(selectedCustomListID)
                ?: return@launch

            val anime = selectedCustomList.anime.sortedWith(AnimeSort())
            animeAdapter.add(this@CustomListActivity, anime)
        }
    }
}
