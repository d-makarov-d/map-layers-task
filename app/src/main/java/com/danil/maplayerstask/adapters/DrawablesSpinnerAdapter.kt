package com.danil.maplayerstask.adapters

import android.content.Context
import android.graphics.drawable.Drawable
import android.graphics.drawable.ShapeDrawable
import android.util.TypedValue
import android.view.View
import android.view.ViewGroup
import android.widget.ArrayAdapter
import android.widget.ImageView
import androidx.annotation.ColorInt
import androidx.appcompat.content.res.AppCompatResources
import com.danil.maplayerstask.R

class DrawablesSpinnerAdapter(
    context: Context
): ArrayAdapter<Drawable>(context, 0) {
    @ColorInt private val colorSecondary: Int
    init {
        val colorSecondaryVal = TypedValue()
        val theme = context.theme
        theme.resolveAttribute(R.attr.colorSecondary, colorSecondaryVal, true)
        colorSecondary = colorSecondaryVal.data
    }

    private val drawables: List<Drawable> = listOf(
        AppCompatResources.getDrawable(context, R.drawable.ic_waypoint) ?: ShapeDrawable(),
        AppCompatResources.getDrawable(context, R.drawable.ic_geometry_collection) ?: ShapeDrawable(),
        AppCompatResources.getDrawable(context, R.drawable.ic_line) ?: ShapeDrawable(),
        AppCompatResources.getDrawable(context, R.drawable.ic_polygon_hatched_2) ?: ShapeDrawable(),
        AppCompatResources.getDrawable(context, R.drawable.ic_polygon) ?: ShapeDrawable()
    )

    override fun getCount(): Int = drawables.size

    override fun getDropDownView(position: Int, convertView: View?, parent: ViewGroup): View {
        return getCustomView(position, convertView, parent)
    }

    override fun getView(position: Int, convertView: View?, parent: ViewGroup): View {
        return getCustomView(position, convertView, parent)
    }

    private fun getCustomView(position: Int, convertView: View?, parent: ViewGroup): View {
        val image: ImageView = if (convertView == null) {
            ImageView(context)
        } else {
            convertView as ImageView
        }

        image.setImageDrawable(getItem(position))
        image.setColorFilter(colorSecondary)

        return image
    }

    override fun getItem(position: Int): Drawable {
        return drawables[position]
    }
}