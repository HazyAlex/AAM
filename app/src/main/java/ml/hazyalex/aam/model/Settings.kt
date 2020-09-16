package ml.hazyalex.aam.model

import android.content.Context
import android.net.ConnectivityManager
import android.net.Network
import android.net.NetworkCapabilities


enum class ImageQuality {
    PERFORMANCE, QUALITY;

    override fun toString(): String {
        return when (this) {
            PERFORMANCE -> {
                "Decent (Performance)"
            }
            QUALITY -> {
                "Best (Quality)"
            }
        }
    }
}

fun isWifiAvailable(context: Context): Boolean {
    val connMgr = context.getSystemService(Context.CONNECTIVITY_SERVICE) as ConnectivityManager?
        ?: return false

    val network: Network = connMgr.activeNetwork ?: return false
    val capabilities = connMgr.getNetworkCapabilities(network)
    return capabilities != null && capabilities.hasTransport(NetworkCapabilities.TRANSPORT_WIFI)
}


class Settings private constructor(context: Context) {
    private val sharedPreferences = context.getSharedPreferences("aam", Context.MODE_PRIVATE)

    var meteredNetwork = sharedPreferences.getBoolean("meteredNetwork", false)
        set(value) {
            field = value

            val editor = sharedPreferences.edit()
            editor.putBoolean("meteredNetwork", field)
            editor.apply()
        }

    var showAdultContent: Boolean = sharedPreferences.getBoolean("showAdultContent", false)
        set(value) {
            field = value

            val editor = sharedPreferences.edit()
            editor.putBoolean("showAdultContent", field)
            editor.apply()
        }

    var imageQuality: ImageQuality = ImageQuality.values()[sharedPreferences.getInt("imageQuality", 0)]
        set(value) {
            field = value

            val editor = sharedPreferences.edit()
            editor.putInt("imageQuality", field.ordinal)
            editor.apply()
        }

    companion object : SingletonHolder<Settings, Context>(::Settings)
}