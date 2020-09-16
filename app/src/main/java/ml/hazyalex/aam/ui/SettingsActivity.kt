package ml.hazyalex.aam.ui

import android.os.Bundle
import android.view.View
import android.widget.AdapterView
import android.widget.Spinner
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.SwitchCompat
import ml.hazyalex.aam.R
import ml.hazyalex.aam.model.ImageQuality
import ml.hazyalex.aam.model.Settings


class SettingsActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_settings)

        handleMeteredConnection()
        handleShowAdultContent()
        handleImageQuality()
    }

    private fun handleMeteredConnection() {
        val switchMeteredConnection = findViewById<SwitchCompat>(R.id.switch_metered_network)
        switchMeteredConnection.isChecked = Settings.getInstance(applicationContext).meteredNetwork

        switchMeteredConnection.setOnClickListener {
            Settings.getInstance(applicationContext).meteredNetwork = switchMeteredConnection.isChecked
        }
    }

    private fun handleShowAdultContent() {
        val switchAdultContent = findViewById<SwitchCompat>(R.id.switch_adult_content)
        switchAdultContent.isChecked = Settings.getInstance(applicationContext).showAdultContent

        switchAdultContent.setOnClickListener {
            Settings.getInstance(applicationContext).showAdultContent = switchAdultContent.isChecked
        }
    }

    private fun handleImageQuality() {
        val savedQualitySetting = Settings.getInstance(applicationContext).imageQuality.ordinal

        val spinnerImageQuality = findViewById<Spinner>(R.id.spinner_image_quality)
        spinnerImageQuality.setSelection(savedQualitySetting, false)
        spinnerImageQuality.onItemSelectedListener = ImageQualitySpinnerListener()
    }

    private class ImageQualitySpinnerListener : AdapterView.OnItemSelectedListener {
        override fun onItemSelected(parent: AdapterView<*>?, view: View?, pos: Int, id: Long) {
            Settings.getInstance(parent!!.context).imageQuality = ImageQuality.values()[pos]
        }

        override fun onNothingSelected(view: AdapterView<*>?) {}
    }
}
