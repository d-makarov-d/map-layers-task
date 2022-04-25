package com.danil.maplayerstask.views

import android.os.Bundle
import android.text.Spannable
import android.text.SpannableString
import android.text.SpannableStringBuilder
import android.text.style.ForegroundColorSpan
import android.util.TypedValue
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.*
import androidx.annotation.ColorInt
import androidx.appcompat.widget.SearchView
import androidx.appcompat.widget.SwitchCompat
import androidx.core.content.ContextCompat
import androidx.core.widget.doOnTextChanged
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.recyclerview.widget.ItemTouchHelper
import androidx.recyclerview.widget.RecyclerView
import androidx.recyclerview.widget.SimpleItemAnimator
import com.danil.maplayerstask.R
import com.danil.maplayerstask.adapters.LayersArrayAdapter
import com.danil.maplayerstask.util.Switch3Pos
import com.danil.maplayerstask.viewmodels.MapLayersViewModel
import com.danil.maplayerstask.viewmodels.SwitchState

class LayersFragment: Fragment() {
    companion object {
        const val titleId = R.string.title_layers
    }
    private val itemTouchHelper: ItemTouchHelper
    private var isReordering = false
    private val layersViewModel: MapLayersViewModel by activityViewModels()
    init {
        val itemTouchCallback = object : ItemTouchHelper.SimpleCallback(
            ItemTouchHelper.UP or
            ItemTouchHelper.DOWN or
            ItemTouchHelper.START or
            ItemTouchHelper.END,
            0
        ) {
            override fun onMove(
                recyclerView: RecyclerView,
                viewHolder: RecyclerView.ViewHolder,
                target: RecyclerView.ViewHolder
            ): Boolean {
                val adapter = recyclerView.adapter as LayersArrayAdapter
                val from = viewHolder.adapterPosition
                val to = target.adapterPosition

                return adapter.moveItem(from, to)
            }

            override fun onSwiped(viewHolder: RecyclerView.ViewHolder, direction: Int) {}

            override fun isLongPressDragEnabled(): Boolean {
                return isReordering
            }
        }
        itemTouchHelper = ItemTouchHelper(itemTouchCallback)
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.fragment_layers, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        // Initialize layers list
        val list: RecyclerView = view.findViewById(R.id.layers_list)
        val adapter = LayersArrayAdapter(requireContext(), itemTouchHelper, layersViewModel)
        list.adapter = adapter
        itemTouchHelper.attachToRecyclerView(list)
        (list.itemAnimator as? SimpleItemAnimator)?.supportsChangeAnimations = false

        layersViewModel.layersFiltered.observe(viewLifecycleOwner) { layers ->
            adapter.updateSearchState(layersViewModel.filter != null)
            adapter.updateAll(layers)
        }
        layersViewModel.drawSwitchMode.observe(viewLifecycleOwner) {
            adapter.notifyItemRangeChanged(0, adapter.itemCount)
        }

        val drawState: SwitchCompat = view.findViewById(R.id.draw_state)
        val switchState = Switch3Pos(drawState)
        if (layersViewModel.drawSwitchMode.value != null)
            switchState.setState(layersViewModel.drawSwitchMode.value ?: SwitchState.StateUndefined)
        switchState.setOnStateChangeListener { state, byUser ->
            if (byUser) layersViewModel.updateDrawState(state)
        }

        layersViewModel.layersState.observe(viewLifecycleOwner) {  state ->
            val layersState = state ?: return@observe
            if (layersState.all { it.value.draw }) {
                switchState.setState(SwitchState.StateShowAll)
            } else if (layersState.all { !it.value.draw }) {
                if (!layersViewModel.savedState.all { !it.value.draw })
                    switchState.setState(SwitchState.StateShowNone)
            } else if (switchState.state() !is SwitchState.StateUndefined) {
                layersViewModel.savedState = layersState
                layersViewModel.updateDrawState(SwitchState.StateUndefined)
                switchState.setState(SwitchState.StateUndefined)
            }
        }

        val reorder: ToggleButton = view.findViewById(R.id.btn_reorder)
        reorder.setOnCheckedChangeListener { _, checked ->
            adapter.setReorder(checked)
            isReordering = checked
            drawState.visibility = if (checked) SeekBar.GONE else SwitchCompat.VISIBLE
        }

        // Searchbar
        val colorOnPrimaryVal = TypedValue()
        requireContext()
            .theme.resolveAttribute(R.attr.colorOnPrimary, colorOnPrimaryVal, true)
        @ColorInt val colorOnPrimary: Int = colorOnPrimaryVal.data
        val colorOnSurf = ContextCompat.getColor(requireContext(), R.color.on_surf)
        val search: TextView = view.findViewById(R.id.search)
        val hint = getString(R.string.hint_layers_query)
        val searchHint = SpannableString(hint)
        searchHint.setSpan(
            ForegroundColorSpan(colorOnSurf),
            0, hint.length, Spannable.SPAN_EXCLUSIVE_INCLUSIVE
        )
        val r = Regex("[0-9-/]+")
        val matches = r.findAll(searchHint)
        matches.forEach { searchHint.setSpan(
            ForegroundColorSpan(colorOnPrimary),
            it.range.first, it.range.last + 1, Spannable.SPAN_EXCLUSIVE_INCLUSIVE
        ) }
        search.hint = searchHint
        search.doOnTextChanged { text, _, _, _ ->
            layersViewModel.searchText.value = text.toString()
        }
        search.text = layersViewModel.searchText.value

        // Searchbar toggle key
        val searchBtn: ToggleButton = view.findViewById(R.id.btn_search)
        val searchLayout: LinearLayout = view.findViewById(R.id.search_bar)
        searchBtn.setOnCheckedChangeListener { _, checked ->
            searchLayout.visibility = if (checked) LinearLayout.VISIBLE else LinearLayout.GONE
            if (!checked) search.text = null
        }
        searchBtn.isChecked = layersViewModel.searchText.value?.isNotBlank() ?: false
        if (!searchBtn.isChecked)
            searchLayout.visibility = LinearLayout.GONE
    }
}