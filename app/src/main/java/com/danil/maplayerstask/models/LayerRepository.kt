package com.danil.maplayerstask.models

import android.content.Context
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import com.danil.maplayerstask.util.Util

/**
 * Provides map layers
 */
object LayerRepository {
    private val layers: MutableLiveData<List<MapLayer>> = MutableLiveData(listOf())
    fun init(context: Context) {
        layers.value = Util.generateLayers(context)
    }
    fun getLayers(): LiveData<List<MapLayer>> = layers
}
