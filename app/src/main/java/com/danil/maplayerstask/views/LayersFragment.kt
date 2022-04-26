package com.danil.maplayerstask.views

import android.app.AlertDialog
import android.graphics.drawable.Drawable
import android.os.Bundle
import android.text.Spannable
import android.text.SpannableString
import android.text.style.ForegroundColorSpan
import android.util.TypedValue
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.*
import androidx.annotation.ColorInt
import androidx.appcompat.widget.SwitchCompat
import androidx.core.content.ContextCompat
import androidx.core.widget.doOnTextChanged
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.recyclerview.widget.ItemTouchHelper
import androidx.recyclerview.widget.RecyclerView
import androidx.recyclerview.widget.SimpleItemAnimator
import com.danil.maplayerstask.R
import com.danil.maplayerstask.adapters.DrawablesSpinnerAdapter
import com.danil.maplayerstask.adapters.LayersArrayAdapter
import com.danil.maplayerstask.models.LayerRepository
import com.danil.maplayerstask.models.MapLayer
import com.danil.maplayerstask.util.Switch3Pos
import com.danil.maplayerstask.viewmodels.MapLayersViewModel
import com.danil.maplayerstask.viewmodels.SwitchState
import java.util.*
import kotlin.random.Random

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
        val btnSearch: ToggleButton = view.findViewById(R.id.btn_search)
        val searchLayout: LinearLayout = view.findViewById(R.id.search_bar)
        btnSearch.setOnCheckedChangeListener { _, checked ->
            searchLayout.visibility = if (checked) LinearLayout.VISIBLE else LinearLayout.GONE
            if (!checked) search.text = null
        }
        btnSearch.isChecked = layersViewModel.searchText.value?.isNotBlank() ?: false
        if (!btnSearch.isChecked)
            searchLayout.visibility = LinearLayout.GONE

        // Delete toggle key
        val btnDelete: ToggleButton = view.findViewById(R.id.btn_delete)
        btnDelete.isChecked = layersViewModel.deleteMode
        btnDelete.setOnCheckedChangeListener { _, checked ->
            layersViewModel.deleteMode = checked
            adapter.notifyItemRangeChanged(0, adapter.itemCount)
        }

        // Add layer key
        val btnAddLayer: ImageButton = view.findViewById(R.id.btn_add)
        btnAddLayer.setOnClickListener {
            val root =
                LayoutInflater.from(requireContext()).inflate(R.layout.dialog_add_layer, null)

            val name: EditText = root.findViewById(R.id.name)
            val category: EditText = root.findViewById(R.id.category)
            val spinner: Spinner = root.findViewById(R.id.drawable)
            spinner.adapter = DrawablesSpinnerAdapter(requireContext())

            val dialog = AlertDialog.Builder(requireContext())
                .setView(root)
                .setPositiveButton(R.string.dialog_confirm, null)
                .setNegativeButton(R.string.dialog_cancel, null)
                .create()
            dialog.setOnShowListener { dlg ->
                val posBtn = (dlg as AlertDialog).getButton(AlertDialog.BUTTON_POSITIVE)
                posBtn.setOnClickListener {
                    if (name.text.toString().isBlank()) {
                        AlertDialog.Builder(requireContext())
                            .setPositiveButton(R.string.dialog_confirm, null)
                            .setTitle(R.string.title_error)
                            .setMessage(R.string.msg_set_name)
                            .show()
                    } else {
                        var c: String? = category.text.toString()
                        if (c.isNullOrBlank()) c = null
                        val layer = MapLayer(
                            Random.nextLong(),
                            name.text.toString(),
                            c,
                            Date(),
                            listOf(),
                            0,
                            1,
                            true,
                            spinner.selectedItem as Drawable
                        )
                        LayerRepository.add(layer)
                        dialog.dismiss()
                    }
                }
            }
            dialog.show()
        }
    }
}