package com.danil.maplayerstask.adapters

import android.content.Context
import android.content.res.ColorStateList
import android.graphics.Typeface
import android.util.TypedValue
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.animation.AnimationUtils
import android.widget.*
import androidx.annotation.ColorInt
import androidx.appcompat.widget.SwitchCompat
import androidx.core.content.ContextCompat
import androidx.core.widget.ImageViewCompat
import com.danil.maplayerstask.R
import com.danil.maplayerstask.models.LayerRepository
import com.danil.maplayerstask.models.MapLayer
import kotlinx.coroutines.*
import java.text.SimpleDateFormat
import java.util.*

class LayersArrayAdapter(
    context: Context
): ArrayAdapter<LayersArrayAdapter.RowElement>(context, R.layout.row_layer_controls) {
    @ColorInt private val colorSecondary: Int
    @ColorInt private val colorOnPrimary: Int
    init {
        // get colors from theme
        val colorSecondaryVal = TypedValue()
        val colorOnPrimaryVal = TypedValue()
        val theme = context.theme
        theme.resolveAttribute(R.attr.colorSecondary, colorSecondaryVal, true)
        theme.resolveAttribute(R.attr.colorOnPrimary, colorOnPrimaryVal, true)
        colorSecondary = colorSecondaryVal.data
        colorOnPrimary = colorOnPrimaryVal.data
    }
    private val inflater = LayoutInflater.from(context)
    private var layers: List<RowElement> = listOf()
    // list of first element in each category id
    private var headElementIds: List<Long> = listOf()
    private var reorder = false

    data class ViewHolder(
        val mainRow: LinearLayout,
        val icon: ImageView,
        val title: TextView,
        val inactive: ImageView,
        val drop: ImageButton,
        val switch: SwitchCompat,
        val reorder: ImageButton,
        val dropLayout: LinearLayout,
        val opacity: TextView,
        val sync: TextView,
        val seek: SeekBar,
        val nElements: TextView,
        val zoom: TextView,
        val btnShowContours: ImageButton,
        val btnList: ImageButton,
        val btnAim: ImageButton,
        val categoryLayout: LinearLayout,
        val categoryTitle: TextView
    )
    override fun getView(position: Int, convertView: View?, parent: ViewGroup): View {
        val rowView: View
        val holder: ViewHolder
        if (convertView == null) {
            val layout = inflater.inflate(R.layout.row_layer_controls, null)
            rowView = layout
            holder = ViewHolder(
                layout.findViewById(R.id.main_row),
                layout.findViewById(R.id.layer_icon),
                layout.findViewById(R.id.layer_title),
                layout.findViewById(R.id.inactive),
                layout.findViewById(R.id.btn_dropdown),
                layout.findViewById(R.id.layer_switch),
                layout.findViewById(R.id.btn_reorder),
                layout.findViewById(R.id.dropdown_layout),
                layout.findViewById(R.id.opacity),
                layout.findViewById(R.id.sync),
                layout.findViewById(R.id.seek),
                layout.findViewById(R.id.num_elem),
                layout.findViewById(R.id.zoom),
                layout.findViewById(R.id.btn_show_contours),
                layout.findViewById(R.id.btn_list),
                layout.findViewById(R.id.btn_aim),
                layout.findViewById(R.id.category_layout),
                layout.findViewById(R.id.category_title)
            )
            rowView.tag = holder
        } else {
            rowView = convertView
            holder = rowView.tag as ViewHolder
        }

        val item = getItem(position) ?: return rowView

        holder.icon.setImageDrawable(item.icon())
        holder.title.text = item.name()
        holder.inactive.visibility = if (item.active()) ImageView.GONE else ImageView.VISIBLE
        holder.opacity.text = context.getString(R.string.opacity, item.opacity() * 100f)
        holder.sync.text = context.getString(
            R.string.sync_date,
            SimpleDateFormat("dd.MM.yyyy", Locale.US).format(item.sync())
        )
        holder.seek.progress = (item.opacity() * 100).toInt()
        holder.nElements.text = context.getString(R.string.num_elem, item.numElements())
        holder.zoom.text = context.getString(R.string.zoom, item.minZoom(), item.maxZoom())

        holder.drop.setOnClickListener { dropView(item, holder) }
        holder.switch.setOnCheckedChangeListener { _, checked ->
            val updatedLayer = MapLayer(
                item.id(), item.name(), item.category(), item.sync(), item.elements(),
                item.minZoom(), item.maxZoom(), item.active(), checked, item.icon()
            )
            LayerRepository.updateLayer(updatedLayer)
        }
        holder.seek.setOnSeekBarChangeListener(object: SeekBar.OnSeekBarChangeListener {
            override fun onProgressChanged(p0: SeekBar?, p1: Int, p2: Boolean) {
                holder.opacity.text =
                    context.getString(R.string.opacity, p1.toFloat())
            }
            override fun onStartTrackingTouch(p0: SeekBar?) {}
            override fun onStopTrackingTouch(p0: SeekBar?) {
                val seekBar = p0 ?: return
                val updatedLayer = MapLayer(
                    item.id(), item.name(), item.category(), item.sync(), item.elements(),
                    item.minZoom(), item.maxZoom(), item.active(), item.draw(), item.icon(),
                    seekBar.progress.toFloat() / 100f
                )
                LayerRepository.updateLayer(updatedLayer)
            }
        })

        if (headElementIds.contains(item.id()) && item.category() != null) {
            holder.categoryLayout.visibility = LinearLayout.VISIBLE
            holder.categoryTitle.text = item.category()
        } else {
            holder.categoryLayout.visibility = LinearLayout.GONE
        }

        if (item.active()) {
            holder.mainRow.alpha = 1f
            holder.drop.isClickable = true
            holder.switch.isClickable = true
        } else {
            holder.mainRow.alpha = .5f
            holder.drop.isClickable = false
            holder.switch.isClickable = false
        }

        if (reorder) {
            holder.reorder.visibility = ImageButton.VISIBLE
            holder.switch.visibility = SwitchCompat.GONE
        } else {
            holder.reorder.visibility = ImageButton.GONE
            holder.switch.visibility = SwitchCompat.VISIBLE
        }

        return rowView
    }

    override fun getItem(position: Int): RowElement? {
        return layers.getOrNull(position)
    }

    override fun getCount(): Int {
        return layers.size
    }

    fun updateAll(layers: List<MapLayer>) {
        // TODO optimize
        this.layers = layers.map { RowElement(
            it,
            this.layers.find { l -> l.id() == it.id() }?.dropdownOpen ?: false
        ) }

        // group items by category
        val grouped: Map<String?, List<RowElement>> = this.layers.groupBy { it.category() }
        headElementIds = grouped.values.map { it[0].id() }
        this.layers = grouped.toList().fold(listOf()) { l, r -> l + r.second }

        notifyDataSetChanged()
    }

    fun dropView(item: RowElement, holder: ViewHolder) {
        if (item.active()) {
            item.dropdownOpen = !item.dropdownOpen
            holder.title.typeface =
                if (item.dropdownOpen) Typeface.DEFAULT_BOLD else Typeface.DEFAULT
            if (item.dropdownOpen) {
                holder.title.setTextColor(colorSecondary)
                holder.icon.setColorFilter(colorSecondary)
                holder.drop.animate()
                    .rotation(90f)
                    .setDuration(500)
                    .start()
                holder.dropLayout.animate()
                    .setDuration(500)
                    .alpha(1f)
                    .start()
                holder.dropLayout.visibility = LinearLayout.VISIBLE
            } else {
                holder.title.setTextColor(colorOnPrimary)
                holder.icon.setColorFilter(colorOnPrimary)
                holder.drop.animate()
                    .rotation(-90f)
                    .setDuration(500)
                    .start()
                holder.dropLayout.animate()
                    .setDuration(500)
                    .alpha(0f)
                    .start()
                CoroutineScope(Dispatchers.IO).launch {
                    delay(500)
                    withContext(Dispatchers.Main) {
                        holder.dropLayout.visibility = LinearLayout.GONE
                    }
                }
            }
        }
    }

    fun setReorder(reorder: Boolean) {
        this.reorder = reorder
        notifyDataSetChanged()
    }

    class RowElement(
        layer: MapLayer,
        var dropdownOpen: Boolean
    ): MapLayer(
        layer.id(), layer.name(), layer.category(), layer.sync(), layer.elements(),
        layer.minZoom(), layer.maxZoom(), layer.active(), layer.draw(), layer.icon(),
        layer.opacity()
    )
}