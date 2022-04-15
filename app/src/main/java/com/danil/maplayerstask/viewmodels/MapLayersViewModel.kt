package com.danil.maplayerstask.viewmodels

import android.os.Bundle
import androidx.lifecycle.LiveData
import androidx.lifecycle.MediatorLiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.danil.maplayerstask.models.LayerRepository
import com.danil.maplayerstask.models.MapElement
import com.danil.maplayerstask.models.MapLayer
import com.danil.maplayerstask.views.PadType
import com.google.android.gms.maps.GoogleMap

class MapLayersViewModel: ViewModel() {
    val layersState: MutableLiveData<Map<Long, MapLayerState>> = MutableLiveData()
    val layers: MediatorLiveData<List<MapLayer>> = MediatorLiveData()
    val drawSwitchMode: MutableLiveData<SwitchState?> = MutableLiveData(null)
    var savedState: Map<Long, MapLayerState> = mapOf()
    val padType: MutableLiveData<PadType> = MutableLiveData(PadType.Normal())
    private val listeners: MutableList<LayerEventListener> = mutableListOf()
    val initialized: MediatorLiveData<Boolean> = MediatorLiveData()
    init {
        layers.addSource(LayerRepository.getLayers()) { value ->
            if (layersState.value == null)
                layersState.value = value.associate { it.id() to MapLayerState(it) }
            else
                layersState.value = value.associate {
                    it.id() to (layersState.value?.get(it.id()) ?: MapLayerState(it))
                }
            layers.setValue(value)
        }
        initialized.addSource(layers) { v ->
            initialized.setValue(v != null && layersState.value != null)
        }
        initialized.addSource(layersState) { v ->
            initialized.setValue(v != null && layers.value != null)
        }
    }

    fun interface LayerEventListener {
        fun onEvent(event: LayerEvent)
    }

    fun addOnLayerEventListener(listener: LayerEventListener) {
        listeners.add(listener)
    }

    fun removeOnLayerEventListener(listener: LayerEventListener) {
        listeners.remove(listener)
    }

    fun clearLayerEventListeners() {
        listeners.clear()
    }

    fun handleLayerEvent(event: LayerEvent) {
        listeners.forEach { it.onEvent(event) }
    }

    fun updateDraw(id: Long, draw: Boolean) {
        if (layersState.value?.containsKey(id) != true) return
        val state = (layersState.value ?: return).toMutableMap()
        state[id] = state[id]!!.copy(draw = draw)
        layersState.value = state
        if (drawSwitchMode.value == SwitchState.StateUndefined) savedState = layersState.value!!
    }
    fun updateOpacity(id: Long, opacity: Float) {
        if (layersState.value?.containsKey(id) != true) return
        val state = (layersState.value ?: return).toMutableMap()
        state[id] = state[id]!!.copy(opacity = opacity)
        layersState.value = state
    }
    fun updateDrawState(state: Int) {
        if (state < 0 || state > 2) return
        val newState = when (state) {
            0 -> SwitchState.StateShowNone
            1 -> SwitchState.StateUndefined
            2 -> SwitchState.StateShowAll
            else -> SwitchState.StateShowNone
        }

        val value = layersState.value?.toMap() ?: return
        when (newState) {
            SwitchState.StateShowNone -> {
                layersState.value =
                    value.values.associate { it.id to MapLayerState(it.id, false, it.opacity) }
            }
            SwitchState.StateShowAll -> {
                layersState.value =
                    value.values.associate { it.id to MapLayerState(it.id, true, it.opacity) }
            }
            SwitchState.StateUndefined -> {
                layersState.value = savedState
            }
        }
        drawSwitchMode.value = newState
    }

    fun putToBundle(bundle: Bundle) {
        bundle.putLongArray("state_keys", savedState.keys.toLongArray())
        bundle.putBooleanArray("state_draw", savedState.values.map { it.draw }.toBooleanArray())
        bundle.putFloatArray("state_opacity", savedState.values.map { it.opacity }.toFloatArray())
    }

    fun restoreFromBundle(bundle: Bundle) {
        val keys = bundle.getLongArray("state_keys") ?: return
        val draw = bundle.getBooleanArray("state_draw") ?: return
        val opacity = bundle.getFloatArray("state_opacity") ?: return
        val state: MutableMap<Long, MapLayerState> = mutableMapOf()
        for (i in keys.indices) {
            state[keys[i]] = MapLayerState(keys[i], draw[i], opacity[i])
        }
        savedState = state
        layersState.value = savedState
    }
}

data class MapLayerState(
    val id: Long,
    val draw: Boolean,
    val opacity: Float,
) {
    constructor(layer: MapLayer): this(layer.id(), false, 1f)
    fun apply(map: GoogleMap, element: MapElement) {
        if (draw) {
            element.draw(map)
            element.setVisible(true)
            element.setOpacity(opacity)
        } else {
            element.remove()
        }
    }
}

sealed class SwitchState(val state: Int){
    object StateShowNone: SwitchState(0)
    object StateUndefined: SwitchState(1)
    object StateShowAll: SwitchState(2)
}

sealed class LayerEvent(val layer: MapLayer) {
    class Aim(layer: MapLayer): LayerEvent(layer)
    class List(layer: MapLayer): LayerEvent(layer)
}
