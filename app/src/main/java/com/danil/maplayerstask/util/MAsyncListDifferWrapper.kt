package com.danil.maplayerstask.util

import android.graphics.drawable.ShapeDrawable
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
    val currentList: List<RowElement>
        get() = differ.currentList

    fun submitList(list: Collection<MapLayer>) {
        // TODO optimize
        // Group items by category
        val newLayers = list.map {
            RowElement(
                it,
                differ.currentList.find { l -> l.id() == it.id() }?.dropdownOpen ?: false
            )
        }
        // Force uncategorized elements up
        val grouped: SortedMap<String?, List<RowElement>> = newLayers
            .groupBy { it.category() }
            .toSortedMap { key1, key2 ->
                if (key1 == null) -1 else if (key2 == null) 1 else key1.compareTo(key2)
            }
        // Save head elements indexes and names
        headElementInds = grouped.values.withIndex()
            .fold<IndexedValue<List<RowElement>>, List<Int>>(listOf()) { acc, v ->
                acc + listOf((acc.lastOrNull() ?: 0) + v.value.size + v.index)
            }.dropLast(1)
        headElementNames = (headElementInds zip grouped.keys.drop(1)).toMap()
        // Flatten grouped structure and insert dummy elements in place of headers
        val dummyEls = generateDummyElements(newLayers.map { it.id() }, headElementInds.size)
        val flat = grouped.toList().fold(listOf<RowElement>()) { l, r ->
            val d = dummyEls.removeFirstOrNull()
            if (d != null) l + r.second + d else l + r.second
        }
        // Apply user ordering
        if (
            primaryOrdering != null &&
            primaryOrdering!!.size == flat.size &&
            primaryOrdering!!.keys.containsAll(flat.map { it.id() })
        ) {
            differ.submitList(flat.sortedBy { primaryOrdering!![it.id()] } )
        } else {
            if (headElementInds.size <= 1)
                adapter.notifyItemRangeRemoved(0, differ.currentList.size)
            differ.submitList(flat)
            adapter.notifyItemRangeChanged(0, flat.size)
        }
    }

    fun getItem(position: Int): RowElement? {
        return if (headElementInds.contains(position)) null
        else differ.currentList.getOrNull(position)
    }

    fun getHeader(position: Int): String? {
        return headElementNames[position]
    }

    fun getItemCount(): Int {
        return differ.currentList.size
    }

    fun moveItem(from: Int, to: Int): Boolean {
        if (headElementInds.contains(from) || headElementInds.contains(to)) return false
        if (
            differ.currentList[from].category() !=
            differ.currentList[to].category()
        ) return false
        val l = differ.currentList.toMutableList()
        Collections.swap(l, from, to)
        primaryOrdering = (l.map { it.id() } zip (0 until l.size)).toMap()
        differ.submitList(l)
        return true
    }

    fun getItemId(position: Int): Long? {
        return if (headElementInds.contains(position)) {
            null
        } else {
            differ.currentList.getOrNull(position)?.id()
        }
    }

    fun isHeadElement(position: Int): Boolean = position in headElementInds

    private fun generateDummyElements(ids: List<Long>, n: Int): MutableList<DummyRowElement> {
        val maxId = ids.maxOrNull() ?: 0
        return (1 .. n).map { DummyRowElement(it + maxId) }.toMutableList()
    }
}

class DummyRowElement(id: Long):
    RowElement(
        MapLayer(
            id,
            "",
            null,
            Date(),
            listOf(),
            0,
            0,
            false,
            ShapeDrawable()
        ),
        false
    )
