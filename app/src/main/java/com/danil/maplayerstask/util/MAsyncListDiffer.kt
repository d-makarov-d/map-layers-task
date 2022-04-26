package com.danil.maplayerstask.util

import androidx.recyclerview.widget.AsyncListDiffer
import androidx.recyclerview.widget.DiffUtil
import com.danil.maplayerstask.adapters.LayersArrayAdapter
import com.danil.maplayerstask.adapters.LayersArrayAdapter.RowElement
import com.danil.maplayerstask.models.MapLayer
import java.util.*

class MAsyncListDifferWrapper(
    private val adapter: LayersArrayAdapter
) {
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

    private val differ = AsyncListDiffer(adapter, diffCallback)
    // list of first element in each category id
    private var headElementInds: List<Int> = listOf()
    private var headElementNames: Map<Int, String?> = mapOf()
    private var primaryOrdering: Map<Long, Int>? = null

    fun submitList(list: Collection<MapLayer>) {
        // TODO optimize
        // group items by category
        val newLayers = list.map {
            RowElement(
                it,
                differ.currentList.find { l -> l.id() == it.id() }?.dropdownOpen ?: false
            )
        }
        val grouped: SortedMap<String?, List<RowElement>> = newLayers
            .groupBy { it.category() }
            .toSortedMap { key1, key2 ->
                if (key1 == null) -1 else if (key2 == null) 1 else key1.compareTo(key2)
            }
        headElementInds = grouped.values.withIndex()
            .fold<IndexedValue<List<RowElement>>, List<Int>>(listOf()) { acc, v ->
                acc + listOf((acc.lastOrNull() ?: 0) + v.value.size + v.index)
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
            if (flat.size <= 1)
                adapter.notifyItemRangeRemoved(0, differ.currentList.size)
            differ.submitList(flat)
        }
    }

    fun getItem(position: Int): RowElement {
        TODO()
    }
}