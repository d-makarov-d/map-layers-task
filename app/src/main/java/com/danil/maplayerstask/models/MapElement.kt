package com.danil.maplayerstask.models

import com.google.android.gms.maps.GoogleMap
import com.google.android.gms.maps.model.LatLngBounds

/**
 * Describes basic map layer element
 */
abstract class MapElement {
    abstract fun name(): String
    abstract fun bounds(): LatLngBounds
    abstract fun draw(map: GoogleMap)
    abstract fun setOpacity(opacity: Float)
    abstract fun setVisible(visible: Boolean)
    abstract fun setDash(set: Boolean, map: GoogleMap)
    abstract fun remove()
}