package com.danil.maplayerstask.views

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.SeekBar
import android.widget.Switch
import android.widget.ToggleButton
import androidx.appcompat.widget.SwitchCompat
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.recyclerview.widget.ItemTouchHelper
import androidx.recyclerview.widget.RecyclerView
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

        layersViewModel.layers.observe(viewLifecycleOwner) { layers ->
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
    }
}