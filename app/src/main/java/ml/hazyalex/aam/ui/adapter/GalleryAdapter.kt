package ml.hazyalex.aam.ui.adapter

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
import ml.hazyalex.aam.model.CustomList
import ml.hazyalex.aam.model.availableColors
import ml.hazyalex.aam.ui.CustomListActivity


class GalleryAdapter(private val activity: FragmentActivity?) : RecyclerView.Adapter<ItemView>() {
    private val titles: MutableList<String> = ArrayList()
    private val colors: MutableList<String> = ArrayList()
    private val customListIDs: MutableList<Int> = ArrayList()


    fun add(list: CustomList) {
        colors.add(list.color)
        titles.add(list.title)
        customListIDs.add(list.customListID)
        refresh()
    }

    fun add(list: List<CustomList>) {
        list.forEach {
            colors.add(it.color)
            titles.add(it.title)
            customListIDs.add(it.customListID)
        }
        refresh()
    }

    private fun refresh() {
        activity?.runOnUiThread {
            notifyDataSetChanged()
        }
    }

    fun titleExists(title: String): Boolean {
        return titles.contains(title)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ItemView {
        val layoutView = LayoutInflater.from(parent.context)
            .inflate(R.layout.item, parent, false)

        layoutView.setOnClickListener {
            val textView = it.findViewById(R.id.item_text) as TextView
            val customListID = customListIDs[textView.tag as Int].or(0)

            val intent = Intent(activity?.applicationContext, CustomListActivity::class.java)
            intent.putExtra("ID", customListID)
            activity?.startActivity(intent)
        }

        return ItemView(layoutView)
    }

    override fun getItemCount(): Int {
        if (titles.size != customListIDs.size || titles.size != colors.size) {
            Log.d("GALLERY_ADAPTER", "Different list sizes! Information might be lost.")
        }

        return titles.size
    }

    override fun onBindViewHolder(holder: ItemView, position: Int) {
        val color = availableColors[colors[position]] ?: error("Invalid color.")
        val name = titles.elementAtOrElse(position) { error("Invalid name.") }

        holder.imageView.setImageResource(color)
        holder.textView.tag = position
        holder.textView.text = name
    }
}

class MarginItemDecoration(private val mSpace: Int = 16) : RecyclerView.ItemDecoration() {
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
        if (position == 0 || position == 1 || position == 2) {
            outRect.top = mSpace
        }

        if (parent.adapter!!.itemCount - 1 == position) {
            outRect.bottom = 200 // TODO: How do you get the navbar size?
        }
    }
}
