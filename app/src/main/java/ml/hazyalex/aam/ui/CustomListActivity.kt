package ml.hazyalex.aam.ui

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.RecyclerView
import androidx.recyclerview.widget.StaggeredGridLayoutManager
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import ml.hazyalex.aam.R
import ml.hazyalex.aam.database.AnimeDB
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

        val selectedCustomListID = intent.getIntExtra("ID", 0)
        if (selectedCustomListID == 0) return

        CoroutineScope(Dispatchers.IO).launch {
            val selectedCustomList = AnimeDB.getInstance(applicationContext).customListDAO()
                .getCustomListWithAnime(selectedCustomListID)
                ?: return@launch

            animeAdapter.addAnimeToView(this@CustomListActivity, selectedCustomList.anime)
        }
    }
}
