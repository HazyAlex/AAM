package ml.hazyalex.aam.ui.adapter

import android.view.View
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import ml.hazyalex.aam.R

class ItemView(itemView: View) : RecyclerView.ViewHolder(itemView) {
    var imageView: ImageView = itemView.findViewById(R.id.item_image)
    var textView: TextView = itemView.findViewById(R.id.item_text)
}
