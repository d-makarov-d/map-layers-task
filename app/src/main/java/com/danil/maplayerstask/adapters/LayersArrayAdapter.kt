package com.danil.maplayerstask.adapters

import android.content.Context
import android.graphics.Typeface
import android.util.TypedValue
import android.view.LayoutInflater
import android.view.MotionEvent
import android.view.View
import android.view.ViewGroup
import android.widget.*
import androidx.annotation.ColorInt
import androidx.appcompat.widget.SwitchCompat
import androidx.recyclerview.widget.AsyncListDiffer
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ItemTouchHelper
import androidx.recyclerview.widget.RecyclerView
import com.danil.maplayerstask.R
import com.danil.maplayerstask.models.LayerRepository
import com.danil.maplayerstask.models.MapLayer
import com.danil.maplayerstask.util.Util
import com.danil.maplayerstask.viewmodels.MapLayersViewModel
import kotlinx.coroutines.*
import java.text.SimpleDateFormat
import java.util.*

class LayersArrayAdapter(
    context: Context,
    private val itemTouchHelper: ItemTouchHelper,
    private val layersModel: MapLayersViewModel
): RecyclerView.Adapter<RecyclerView.ViewHolder>() {
    companion object {
        val diffCallback = object : DiffUtil.ItemCallback<RowElement>() {
            override fun areItemsTheSame(oldItem: RowElement, newItem: RowElement): Boolean {
                return oldItem.id() == newItem.id()
            }

            override fun areContentsTheSame(oldItem: RowElement, newItem: RowElement): Boolean {
                // Only externally changed properties must be compared here
                return oldItem.id() == newItem.id() &&
                        oldItem.active() == newItem.active() &&
                        oldItem.category() == newItem.category() &&
                        oldItem.name() == newItem.name() &&
                        oldItem.numElements() == newItem.numElements() &&
                        oldItem.sync() == newItem.sync() &&
                        oldItem.zoomRange() == newItem.zoomRange()
            }
        }
    }
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
    private val strSyncDate = context.getString(R.string.sync_date)
    private val strNElements = context.getString(R.string.num_elem)
    private val strZoom = context.getString(R.string.zoom)
    private val strOpacity = context.getString(R.string.opacity)
    // list of first element in each category id
    private var headElementInds: List<Int> = listOf()
    private var headElementNames: Map<Int, String?> = mapOf()
    private var reorder = false
    private var differ = AsyncListDiffer(this, diffCallback)
    private var recyclerView: RecyclerView? = null
    private var primaryOrdering: Map<Long, Int>? = null

    class ViewHolder(
        view: View,
        @ColorInt private val colorOnPrimary: Int,
        @ColorInt private val colorSecondary: Int
    ): RecyclerView.ViewHolder(view) {
        var current: RowElement? = null
        val mainRow: LinearLayout = view.findViewById(R.id.main_row)
        val icon: ImageView = view.findViewById(R.id.layer_icon)
        val title: TextView = view.findViewById(R.id.layer_title)
        val inactive: ImageView = view.findViewById(R.id.inactive)
        val drop: ImageButton = view.findViewById(R.id.btn_dropdown)
        val switch: SwitchCompat = view.findViewById(R.id.layer_switch)
        val reorder: ImageButton = view.findViewById(R.id.btn_reorder)
        val dropLayout: LinearLayout = view.findViewById(R.id.dropdown_layout)
        val opacity: TextView = view.findViewById(R.id.opacity)
        val sync: TextView = view.findViewById(R.id.sync)
        val seek: SeekBar = view.findViewById(R.id.seek)
        val nElements: TextView = view.findViewById(R.id.num_elem)
        val zoom: TextView = view.findViewById(R.id.zoom)
        val btnShowContours: ImageButton = view.findViewById(R.id.btn_show_contours)
        val btnList: ImageButton = view.findViewById(R.id.btn_list)
        val btnAim: ImageButton = view.findViewById(R.id.btn_aim)
        init {
            mainRow.setOnClickListener {
                current?.let { dropView() }
            }
        }

        fun dropped() = current?.dropdownOpen ?: false

        fun dropView() {
            val item = current ?: return
            if (item.active()) {
                item.dropdownOpen = !item.dropdownOpen
                title.typeface =
                    if (item.dropdownOpen) Typeface.DEFAULT_BOLD else Typeface.DEFAULT
                if (item.dropdownOpen) {
                    title.setTextColor(colorSecondary)
                    icon.setColorFilter(colorSecondary)
                    drop.animate()
                        .rotation(90f)
                        .setDuration(500)
                        .start()
                    dropLayout.animate()
                        .setDuration(500)
                        .alpha(1f)
                        .start()
                    dropLayout.visibility = LinearLayout.VISIBLE
                } else {
                    title.setTextColor(colorOnPrimary)
                    icon.setColorFilter(colorOnPrimary)
                    drop.animate()
                        .rotation(-90f)
                        .setDuration(500)
                        .start()
                    dropLayout.animate()
                        .setDuration(500)
                        .alpha(0f)
                        .start()
                    CoroutineScope(Dispatchers.IO).launch {
                        delay(500)
                        withContext(Dispatchers.Main) {
                            dropLayout.visibility = LinearLayout.GONE
                        }
                    }
                }
            }
        }
    }

    class HeaderHolder(view: View): RecyclerView.ViewHolder(view) {
        val category: TextView = view.findViewById(R.id.category_title)
    }

    fun updateAll(layers: List<MapLayer>) {
        // TODO optimize
        // group items by category
        val newLayers = layers.map { RowElement(
            it,
            differ.currentList.find { l -> l.id() == it.id() }?.dropdownOpen ?: false
        ) }
        val grouped: Map<String?, List<RowElement>> = newLayers.groupBy { it.category() }
        headElementInds = grouped.values.withIndex()
            .fold<IndexedValue<List<RowElement>>, List<Int>>(listOf()) { acc, v ->
                acc + listOf(acc.lastOrNull() ?: 0 + v.value.size + v.index)
            }.dropLast(1)
        headElementNames = (headElementInds zip grouped.keys.drop(1)).toMap()
        val flat = grouped.toList().fold(listOf<RowElement>()) { l, r -> l + r.second }
        if (
            primaryOrdering != null &&
            primaryOrdering!!.size == flat.size &&
            primaryOrdering!!.keys.containsAll(flat.map { it.id() })
        ) {
            differ.submitList(flat.sortedBy { primaryOrdering!![it.id()] } )
        } else {
            differ.submitList(flat)
        }
    }

    fun setReorder(reorder: Boolean) {
        val recycler = recyclerView ?: return
        this.reorder = reorder
        for (i in 0 until recycler.childCount) {
            val holder = recycler.getChildViewHolder(recycler.getChildAt(i))
            if (ViewHolder::class.isInstance(holder)) {
                val viewHolder = holder as ViewHolder
                if (viewHolder.dropped()) viewHolder.dropView()
            }
        }
        differ.currentList.forEach { it.dropdownOpen = false }
        notifyItemRangeChanged(0, itemCount)
    }

    fun moveItem(from: Int, to: Int): Boolean {
        if (headElementInds.contains(from) || headElementInds.contains(to)) return false
        if (differ.currentList[from].category() != differ.currentList[to].category()) return false
        val l = differ.currentList.toMutableList()
        Collections.swap(l, adjustedPos(from), adjustedPos(to))
        primaryOrdering = (l.map { it.id() } zip (0 until l.size)).toMap()
        AsyncListDiffer(this, diffCallback)
        differ.submitList(l) { notifyItemMoved(adjustedPos(to), adjustedPos(from)) }
        differ.currentList
        notifyItemMoved(from, to)
        return true
    }

    class RowElement(
        layer: MapLayer,
        var dropdownOpen: Boolean
    ): MapLayer(
        layer.id(), layer.name(), layer.category(), layer.sync(), layer.elements(),
        layer.minZoom(), layer.maxZoom(), layer.active(), layer.icon()
    )

    override fun getItemId(position: Int): Long {
        return if (headElementInds.contains(position)) {
            super.getItemId(position)
        } else {
            differ.currentList.getOrNull(adjustedPos(position))?.id() ?: super.getItemId(position)
        }
    }

    override fun onAttachedToRecyclerView(recyclerView: RecyclerView) {
        super.onAttachedToRecyclerView(recyclerView)
        this.recyclerView = recyclerView
    }

    override fun getItemViewType(position: Int): Int {
        return if (position in headElementInds) 1 else 0
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder {
        if (viewType == 0) {
            val view = LayoutInflater.from(parent.context)
                .inflate(R.layout.row_layer_controls, parent, false)
            return ViewHolder(view, colorOnPrimary, colorSecondary)
        } else {
            val view = LayoutInflater.from(parent.context)
                .inflate(R.layout.row_header, parent, false)
            return HeaderHolder(view)
        }
    }

    override fun onBindViewHolder(h: RecyclerView.ViewHolder, position: Int) {
        if (headElementInds.contains(position)) {
            val holder = h as HeaderHolder
            holder.category.text = headElementNames[position]
        } else {
            val holder = h as ViewHolder
            val item = differ.currentList.getOrNull(adjustedPos(position)) ?: return

            holder.current = item
            val opacity = layersModel.layersState.value?.get(item.id())?.opacity ?: 1f
            val draw = layersModel.layersState.value?.get(item.id())?.draw ?: false

            holder.icon.setImageDrawable(item.icon())
            if (item.dropdownOpen) {
                holder.icon.setColorFilter(colorSecondary)
                holder.title.typeface = Typeface.DEFAULT_BOLD
                holder.title.setTextColor(colorSecondary)
                holder.dropLayout.visibility = LinearLayout.VISIBLE
                holder.drop.rotation = 90f
            } else {
                holder.icon.setColorFilter(colorOnPrimary)
                holder.title.typeface = Typeface.DEFAULT
                holder.title.setTextColor(colorOnPrimary)
                holder.dropLayout.visibility = LinearLayout.GONE
                holder.drop.rotation = -90f
            }
            holder.title.text = item.name()
            holder.inactive.visibility = if (item.active()) ImageView.GONE else ImageView.VISIBLE
            holder.opacity.text = String.format(strOpacity, opacity * 100f)
            holder.sync.text = String.format(
                strSyncDate,
                SimpleDateFormat("dd.MM.yyyy", Locale.US).format(item.sync())
            )
            holder.seek.progress = (opacity * 100).toInt()
            holder.nElements.text = String.format(strNElements, item.numElements())
            holder.zoom.text = String.format(strZoom, item.minZoom(), item.maxZoom())

            holder.drop.setOnClickListener { holder.dropView() }
            holder.switch.isChecked = draw
            holder.switch.setOnCheckedChangeListener { _, checked ->
                val id = holder.current?.id() ?: return@setOnCheckedChangeListener
                layersModel.updateDraw(id, checked)
            }
            holder.seek.setOnSeekBarChangeListener(object: SeekBar.OnSeekBarChangeListener {
                override fun onProgressChanged(p0: SeekBar?, p1: Int, p2: Boolean) {
                    holder.opacity.text = String.format(strOpacity, p1.toFloat())
                    val seekBar = p0 ?: return
                    val id = holder.current?.id() ?: return
                    layersModel.updateOpacity(id, seekBar.progress.toFloat() / 100f)
                }
                override fun onStartTrackingTouch(p0: SeekBar?) {}
                override fun onStopTrackingTouch(p0: SeekBar?) {}
            })

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

            holder.reorder.setOnTouchListener { _, event ->
                if (event.actionMasked == MotionEvent.ACTION_DOWN) {
                    itemTouchHelper.startDrag(holder)
                }
                return@setOnTouchListener true
            }
        }
    }

    override fun getItemCount(): Int {
        return differ.currentList.size + headElementInds.size
    }

    private fun adjustedPos(position: Int): Int =
        position - headElementInds.filter { it < position }.size
}