package com.danil.maplayerstask.adapters

import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.*
import androidx.appcompat.widget.SwitchCompat
import com.danil.maplayerstask.R
import com.danil.maplayerstask.models.MapLayer
import java.text.SimpleDateFormat
import java.time.format.DateTimeFormatter
import java.util.*

class LayersArrayAdapter(
    context: Context
): ArrayAdapter<MapLayer>(context, R.layout.row_layer_controls) {
    private val inflater = LayoutInflater.from(context)
    private var layers: List<MapLayer> = listOf()

    private data class ViewHolder(
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
    )
    override fun getView(position: Int, convertView: View?, parent: ViewGroup): View {
        val rowView: View
        val holder: ViewHolder
        if (convertView == null) {
            val layout = inflater.inflate(R.layout.row_layer_controls, null)
            rowView = layout
            holder = ViewHolder(
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

        return rowView
    }

    override fun getItem(position: Int): MapLayer? {
        return layers.getOrNull(position)
    }

    override fun getCount(): Int {
        return layers.size
    }

    fun updateAll(layers: List<MapLayer>) {
        this.layers = layers
        notifyDataSetChanged()
    }
}