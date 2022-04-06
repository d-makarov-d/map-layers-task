package com.danil.maplayerstask.util

import android.content.Context
import android.graphics.drawable.ShapeDrawable
import androidx.core.content.ContextCompat
import com.danil.maplayerstask.R
import com.danil.maplayerstask.models.MapLayer
import java.util.*
import kotlin.random.Random

object Util {
    fun generateLayers(context: Context): List<MapLayer> {
        return listOf(
            genEmptyLayer("Слой делян", null, true, context),
        )
    }
}

private fun genEmptyLayer(
    name: String,
    category: String?,
    active: Boolean,
    context: Context
): MapLayer = MapLayer(
    Random.nextLong(),
    name,
    category,
    Date(164e10.toLong() + Random.nextLong(1e10.toLong())),
    listOf(),
    Random.nextInt(0, 10),
    Random.nextInt(11, 20),
    active,
    ContextCompat.getDrawable(context, R.drawable.outline_place_24) ?: ShapeDrawable()
)