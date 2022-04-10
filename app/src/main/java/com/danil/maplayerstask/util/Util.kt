package com.danil.maplayerstask.util

import android.content.Context
import android.graphics.drawable.Drawable
import android.graphics.drawable.ShapeDrawable
import androidx.core.content.ContextCompat
import com.danil.maplayerstask.R
import com.danil.maplayerstask.models.MapLayer
import com.danil.maplayerstask.models.PolygonElement
import com.google.android.gms.maps.model.LatLng
import java.util.*
import kotlin.random.Random

object Util {
    var random = Random(0)
    fun generateLayers(context: Context): List<MapLayer> {
        random = Random(0)
        return listOf(
            genEmptyLayer("Слой делян", null, true, context),
            getCitiesLayer(context),
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
    Util.random.nextLong(),
    name,
    category,
    Date(164e10.toLong() + Random.nextLong(1e10.toLong())),
    listOf(),
    Random.nextInt(0, 10),
    Random.nextInt(11, 20),
    active,
    icon
)

private fun getCitiesLayer(context: Context): MapLayer {
    val mskCoords = listOf(
        LatLng(55.581694, 37.573164),
        LatLng(55.662825, 37.430482),
        LatLng(55.758334, 37.370111),
        LatLng(55.871933, 37.412790),
        LatLng(55.908651, 37.594448),
        LatLng(55.856536, 37.781131),
        LatLng(55.776451, 37.842845),
        LatLng(55.659416, 37.839168),
        LatLng(55.572083, 37.664862)
    )
    val spbCoords = listOf(
        LatLng(59.833621, 30.282320),
        LatLng(59.878384, 30.293482),
        LatLng(59.909172, 30.206469),
        LatLng(60.009843, 30.235756),
        LatLng(60.060555, 30.140921),
        LatLng(60.091232, 30.369569),
        LatLng(59.989478, 30.487127),
        LatLng(59.968392, 30.550136),
        LatLng(59.864322, 30.525782),
        LatLng(59.813582, 30.350838)
    )
    val mgdCoords = listOf(
        LatLng(59.532109, 150.894048),
        LatLng(59.531950, 150.814960),
        LatLng(59.537291, 150.772507),
        LatLng(59.563836, 150.770110),
        LatLng(59.579173, 150.795331),
        LatLng(59.586386, 150.777824),
        LatLng(59.598407, 150.780492),
        LatLng(59.590213, 150.792991),
        LatLng(59.581042, 150.814873),
        LatLng(59.599939, 150.835454),
        LatLng(59.594304, 150.856726),
        LatLng(59.569606, 150.846322),
        LatLng(59.565364, 150.912803)
    )
    val msk = PolygonElement("Moscow", mskCoords)
    val spb = PolygonElement("SPB", spbCoords)
    val mgd = PolygonElement("Magadan", mgdCoords)
    return MapLayer(
        Util.random.nextLong(),
        "Cities layer",
        "Drawable Layers",
        Date(164e10.toLong() + Random.nextLong(1e10.toLong())),
        listOf(msk, spb, mgd),
        0, 1,
        true,
        ContextCompat.getDrawable(context, R.drawable.outline_place_24) ?: ShapeDrawable()
    )
}
