package com.danil.maplayerstask.util

import android.content.Context
import android.graphics.drawable.Drawable
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
            genEmptyLayer("Сигналы о лесоизменения, тестовая выборка с ув-ным шагом",
                "Общие слои", true, context),
            genEmptyLayer("Преграды для прохождения огня", null, true, context),
            genEmptyLayer("Сигналы о лесоизменения, тестовая выборка с ув-ным шагом",
                null, true, context),
            genEmptyLayer("Преграды для прохождения огня", "Общие слои",
                true, context),
            genEmptyLayer("Контуры гарей", "Общие слои", false, context),
            genEmptyLayer("Маска облачности от\n01.07.2021", "Общие слои",
                true, context,
                ContextCompat.getDrawable(context, R.drawable.outline_pentagon_24) ?: ShapeDrawable()),
            genEmptyLayer("Маска облачности от\n02.07.2021", null,
                true, context,
                ContextCompat.getDrawable(context, R.drawable.outline_pentagon_24) ?: ShapeDrawable()),
            genEmptyLayer("Маска облачности от\n02.07.2021", null,
                true, context,
                ContextCompat.getDrawable(context, R.drawable.outline_pentagon_24) ?: ShapeDrawable()),
            genEmptyLayer("Маска облачности от\n12.01.2021", "Общие слои",
                true, context,
                ContextCompat.getDrawable(context, R.drawable.outline_pentagon_24) ?: ShapeDrawable()),
            genEmptyLayer("Маска облачности от\n01.07.2021", "Общие слои",
                true, context,
                ContextCompat.getDrawable(context, R.drawable.outline_pentagon_24) ?: ShapeDrawable()),
            genEmptyLayer("Маска облачности от\n12.01.2021", "Общие слои",
                true, context,
                ContextCompat.getDrawable(context, R.drawable.outline_pentagon_24) ?: ShapeDrawable()),
            genEmptyLayer("Маска облачности от\n12.01.2021", "Общие слои",
                true, context,
                ContextCompat.getDrawable(context, R.drawable.outline_pentagon_24) ?: ShapeDrawable()),
        )
    }
}

private fun genEmptyLayer(
    name: String,
    category: String?,
    active: Boolean,
    context: Context,
    icon: Drawable =
        ContextCompat.getDrawable(context, R.drawable.outline_place_24) ?: ShapeDrawable()
): MapLayer = MapLayer(
    Random.nextLong(),
    name,
    category,
    Date(164e10.toLong() + Random.nextLong(1e10.toLong())),
    listOf(),
    Random.nextInt(0, 10),
    Random.nextInt(11, 20),
    active,
    icon
)
