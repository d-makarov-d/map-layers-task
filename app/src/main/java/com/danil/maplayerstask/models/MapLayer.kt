package com.danil.maplayerstask.models

import android.graphics.drawable.Drawable
import java.util.*

/**
 * Describes basic map layer
 */
open class MapLayer(
    private val id: Long,
    var name: String,
    private val category: String?,
    private val sync: Date,
    private val elements: List<MapElement>,
    private val minZoom: Int,
    private val maxZoom: Int,
    private val active: Boolean,
    private val draw: Boolean,
    private val icon: Drawable,
    private val opacity: Float = 1f
) {
    fun id(): Long = id
    fun name(): String = name
    fun category(): String? = category
    fun opacity(): Float = opacity
    fun sync(): Date = sync
    fun numElements(): Int = elements.size
    fun zoomRange(): Pair<Int, Int> = Pair(minZoom, maxZoom)
    fun elements(): List<MapElement> = elements
    fun active(): Boolean = active
    fun draw(): Boolean = draw
    fun icon(): Drawable = icon
    fun minZoom() = zoomRange().first
    fun maxZoom() = zoomRange().second
}