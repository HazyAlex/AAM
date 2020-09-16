package ml.hazyalex.aam.model

import android.content.Context
import android.graphics.drawable.Drawable
import android.util.Log
import android.widget.ImageView
import com.bumptech.glide.Glide
import com.bumptech.glide.RequestBuilder
import com.bumptech.glide.load.DecodeFormat
import ml.hazyalex.aam.R


fun loadImage(imageURL: String?, imageView: ImageView, context: Context) {
    val builder = Glide.with(context).load(imageURL)
        .placeholder(R.drawable.ic_launcher_foreground)

    loadImage(builder, imageView, context)
}

fun loadImageCentered(imageURL: String?, imageView: ImageView, context: Context) {
    val builder = Glide.with(context).load(imageURL)
        .placeholder(R.drawable.ic_launcher_foreground)
        .fitCenter()

    loadImage(builder, imageView, context)
}

private fun loadImage(builder: RequestBuilder<Drawable>, imageView: ImageView, context: Context) {
    val settings = Settings.getInstance(context)

    var builder = when (settings.imageQuality) {
        ImageQuality.PERFORMANCE -> {
            builder.format(DecodeFormat.PREFER_RGB_565)
        }
        ImageQuality.QUALITY -> {
            builder.format(DecodeFormat.PREFER_ARGB_8888)
        }
    }

    if (settings.meteredNetwork && !isWifiAvailable(context)) {
        Log.d("WiFi", "Using the cached version.")
        builder = builder.onlyRetrieveFromCache(true)
    }

    builder.into(imageView)
}