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

class MasonryAdapter(
    private val context: Context,
) : RecyclerView.Adapter<ItemView>() {
    var onLongPressListener: ((View?) -> Boolean)? = null

    private val ids: MutableList<Long> = ArrayList()
    private val titles: MutableList<String> = ArrayList()
    private val images: MutableList<String?> = ArrayList()


    fun add(activity: FragmentActivity?, anime: List<Anime>) {
        assert(ids.size == 0)
        assert(titles.size == 0)
        assert(images.size == 0)

        val canSeeAdultContent = Settings.getInstance(context).showAdultContent

        activity?.runOnUiThread {
            anime.forEach {
                val isAdult = it.adult != null && it.adult
                val hasAdultRating = it.rating != null && it.rating.contains("Rx")

                if (!canSeeAdultContent && (isAdult || hasAdultRating)) {
                    return@forEach
                }

                titles.add(it.title)
                images.add(it.image_url)
                ids.add(it.idMyAnimeList)
            }

            notifyItemRangeInserted(0, anime.size)
        }
    }

    fun remove(view: View, animeID: Long) {
        val index = ids.indexOf(animeID)

        ids.removeAt(index)
        titles.removeAt(index)
        images.removeAt(index)

        view.post {
            notifyItemRemoved(index)
        }
    }

    fun getID(index: Int): Long {
        return ids[index]
    }

    fun clearUI(activity: FragmentActivity?) {
        activity?.runOnUiThread {
            titles.clear()
            images.clear()
            ids.clear()

            notifyDataSetChanged()
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ItemView {
        val layoutView = LayoutInflater.from(parent.context)
            .inflate(R.layout.item, parent, false)

        layoutView.setOnClickListener {
            val textView = it.findViewById(R.id.item_text) as TextView
            val animeID = textView.tag as Long

            val intent = Intent(context, AnimeDetailsActivity::class.java)
            intent.putExtra("ID", animeID)
            context.startActivity(intent)
        }

        if (onLongPressListener != null) {
            layoutView.setOnLongClickListener(onLongPressListener)
        }

        return ItemView(layoutView)
    }

    override fun onBindViewHolder(holder: ItemView, position: Int) {
        loadImageCentered(images[position], holder.imageView, context)

        holder.textView.tag = ids[position]
        holder.textView.text = titles[position]
    }

    override fun getItemCount(): Int {
        if (ids.size != titles.size || ids.size != images.size) {
            Log.d("AAM", "MASONRY_ADAPTER - Different sizes! Some information might be lost.")
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
