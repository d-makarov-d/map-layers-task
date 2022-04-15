package com.danil.maplayerstask.models

import android.graphics.Color
import com.google.android.gms.maps.GoogleMap
import com.google.android.gms.maps.model.*

class PolygonElement(
    private val name: String,
    private val borders: List<LatLng>
): MapElement() {
    private var map: GoogleMap? = null
    // south-west corner
    private val sw = LatLng(
        borders.minOf { it.latitude },
        borders.minOf { it.longitude }
    )
    // north-east corner
    private val ne = LatLng(
        borders.maxOf { it.latitude },
        borders.maxOf { it.longitude }
    )
    private var polygon: Polygon? = null
    private var dash: Polyline? = null
    override fun name(): String = name
    override fun bounds(): LatLngBounds = LatLngBounds(sw, ne)
    override fun draw(map: GoogleMap) {
        if (this.map !== map) {
            polygon?.remove()
            val polyOptions = PolygonOptions()
                .fillColor(Color.RED)
                .strokeColor(Color.BLACK)
                .addAll(borders)
            polygon = map.addPolygon(polyOptions)
            this.map = map
        }
    }

    override fun setOpacity(opacity: Float) {
        val poly = polygon ?: return
        val c = poly.fillColor
        poly.fillColor =
            Color.argb((opacity * 255).toInt(), Color.red(c), Color.green(c), Color.blue(c))
    }

    override fun setVisible(visible: Boolean) {
        polygon?.isVisible = visible
    }

    override fun remove() {
        polygon?.remove()
    }

    override fun setDash(set: Boolean, map: GoogleMap) {
        dash?.remove()
        if (set) {
            val options = PolylineOptions()
                .color(Color.BLACK)
                .addAll(borders)
            dash = map.addPolyline(options)
            dash?.pattern = listOf(Dash(20F), Gap(20F))
        }
    }
}