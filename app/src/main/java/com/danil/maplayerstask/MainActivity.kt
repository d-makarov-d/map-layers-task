package com.danil.maplayerstask

import android.content.Context
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.os.Parcel
import androidx.activity.viewModels
import com.danil.maplayerstask.models.LayerRepository
import com.danil.maplayerstask.viewmodels.MapLayersViewModel
import com.danil.maplayerstask.views.LayersFragment
import com.danil.maplayerstask.views.MapWithControlsFragment
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import java.io.FileNotFoundException

const val UI_STATE_FILE = "ui_state"
class MainActivity : AppCompatActivity() {
    private val layersViewModel: MapLayersViewModel by viewModels()
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        try {
            openFileInput(UI_STATE_FILE).use {
                val parcel = Parcel.obtain()
                val bytes = it.readBytes()
                parcel.unmarshall(bytes, 0, bytes.size)
                parcel.setDataPosition(0)
                val bundle = parcel.readBundle() ?: return@use
                layersViewModel.restoreFromBundle(bundle)
                parcel.recycle()
            }
        } catch (e: FileNotFoundException) {}

        LayerRepository.init(applicationContext)

        val ft = supportFragmentManager.beginTransaction()
        ft.replace(R.id.main_container, MapWithControlsFragment::class.java, Bundle())
        ft.commit()
    }

    override fun onStop() {
        val bundle = Bundle()
        val parcel = Parcel.obtain()
        layersViewModel.putToBundle(bundle)
        bundle.writeToParcel(parcel, 0)
        parcel.setDataPosition(0)
        openFileOutput(UI_STATE_FILE, Context.MODE_PRIVATE).use {
            it.write(parcel.marshall())
        }
        parcel.recycle()
        super.onStop()
    }
}