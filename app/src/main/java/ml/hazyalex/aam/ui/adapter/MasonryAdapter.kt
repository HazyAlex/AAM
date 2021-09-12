package ml.hazyalex.aam.ui.adapter

import android.content.Context
import android.content.Intent
import android.graphics.Rect
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.fragment.app.FragmentActivity
import androidx.recyclerview.widget.RecyclerView
import ml.hazyalex.aam.R
import ml.hazyalex.aam.model.Anime
import ml.hazyalex.aam.model.Settings
import ml.hazyalex.aam.model.loadImageCentered
import ml.hazyalex.aam.ui.AnimeDetailsActivity

class MasonryAdapter(private val context: Context) : RecyclerView.Adapter<ItemView>() {
    private val ids: MutableList<Long> = ArrayList()
    private val titles: MutableList<String> = ArrayList()
    private val images: MutableList<String?> = ArrayList()


    fun addAnimeToView(activity: FragmentActivity?, anime: List<Anime>) {
        anime.forEach {
            val canSeeAdultContent = Settings.getInstance(context).showAdultContent

            val isAdult = it.adult != null && it.adult
            val hasAdultRating = it.rating != null && it.rating.contains("Rx")

            if (!canSeeAdultContent && (isAdult || hasAdultRating)) {
                return@forEach
            }

            titles.add(it.title)
            images.add(it.image_url)
            ids.add(it.idMyAnimeList)
        }
        refresh(activity)
    }

    fun clearUI(activity: FragmentActivity?) {
        titles.clear()
        images.clear()
        ids.clear()
        refresh(activity)
    }

    private fun refresh(activity: FragmentActivity?) {
        activity?.runOnUiThread {
            notifyDataSetChanged()
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ItemView {
        val layoutView = LayoutInflater.from(parent.context)
            .inflate(R.layout.item, parent, false)

        layoutView.setOnClickListener {
            val textView = it.findViewById(R.id.item_text) as TextView
            val animeID = ids[textView.tag as Int].or(0)

            val intent = Intent(context, AnimeDetailsActivity::class.java)
            intent.putExtra("ID", animeID)
            context.startActivity(intent)
        }

        return ItemView(layoutView)
    }

    override fun onBindViewHolder(holder: ItemView, position: Int) {
        loadImageCentered(images[position], holder.imageView, context)

        // Set the tag so we can later grab the ID
        holder.textView.tag = position
        holder.textView.text = titles[position]
    }

    override fun getItemCount(): Int {
        if (ids.size != titles.size || ids.size != images.size) {
            Log.d("MASONRY_ADAPTER", "Different sizes! Some information might be lost.")
        }

        return ids.size
    }
}


class SpacesItemDecoration(private val mSpace: Int = 16) : RecyclerView.ItemDecoration() {
    override fun getItemOffsets(
        outRect: Rect,
        view: View,
        parent: RecyclerView,
        state: RecyclerView.State,
    ) {
        outRect.left = mSpace
        outRect.right = mSpace
        outRect.bottom = mSpace

        val position: Int = parent.getChildAdapterPosition(view)

        // Add top margin only for the first items to avoid double space between items
        if (position == 0 || position == 1) {
            outRect.top = mSpace
        }

        if ((parent.adapter!!.itemCount - 1) == position) {
            outRect.bottom = 200 // TODO: How do you get the navbar size?
        }
    }
}
