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
    private var layersMap: MutableMap<Long, MapLayer> = mutableMapOf()
    fun init(context: Context) {
        if (layers.value?.size == 0)
            updateLayers(Util.generateLayers(context))
    }
    fun getLayers(): LiveData<List<MapLayer>> = layers
    fun updateLayer(newLayer: MapLayer) {
        layersMap[newLayer.id()] = newLayer
        layers.value = layersMap.values.toList()
    }
    private fun updateLayers(layers: List<MapLayer>) {
        this.layers.value = layers
        layersMap = layers.associateBy { it.id() }.toMutableMap()
    }
}
