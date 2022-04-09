package com.danil.maplayerstask.viewmodels

import androidx.lifecycle.MediatorLiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.danil.maplayerstask.models.LayerRepository
import com.danil.maplayerstask.models.MapLayer

class MapLayersViewModel: ViewModel() {
    val layersState: MutableLiveData<Map<Long, MapLayerState>> = MutableLiveData()
    val layers: MediatorLiveData<List<MapLayer>> = MediatorLiveData()
    init {
        layers.addSource(LayerRepository.getLayers()) { value ->
            if (layersState.value == null)
                layersState.value = value.associate { it.id() to MapLayerState(it) }
            layers.setValue(value)
        }
    }

    fun updateDraw(id: Long, draw: Boolean) {
        if (layersState.value?.containsKey(id) != true) return
        val state = (layersState.value ?: return).toMutableMap()
        state[id] = state[id]!!.copy(draw = draw)
        layersState.value = state
    }
    fun updateOpacity(id: Long, opacity: Float) {
        if (layersState.value?.containsKey(id) != true) return
        val state = (layersState.value ?: return).toMutableMap()
        state[id] = state[id]!!.copy(opacity = opacity)
        layersState.value = state
    }
}

data class MapLayerState(
    val id: Long,
    val draw: Boolean,
    val opacity: Float,
) {
    constructor(layer: MapLayer): this(layer.id(), false, 1f)
}