package com.danil.maplayerstask.views

import android.os.Bundle
import android.util.TypedValue
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.RadioButton
import android.widget.RadioGroup
import androidx.annotation.ColorInt
import androidx.annotation.StringRes
import androidx.core.view.get
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import com.danil.maplayerstask.R
import com.danil.maplayerstask.viewmodels.MapLayersViewModel
import com.google.android.gms.maps.GoogleMap

class PadsFragment: Fragment() {
    companion object {
        const val titleId = R.string.title_pads
    }
    private val layersViewModel: MapLayersViewModel by activityViewModels()

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.fragment_pads, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        @ColorInt val colorOnPrimary: Int
        val colorOnPrimaryVal = TypedValue()
        val theme = requireContext().theme
        theme.resolveAttribute(R.attr.colorOnPrimary, colorOnPrimaryVal, true)
        colorOnPrimary = colorOnPrimaryVal.data

        val group: RadioGroup = view.findViewById(R.id.pads)
        group.removeAllViews()
        val idToType: MutableMap<Int, PadType> = mutableMapOf()
        for (type in PadType.members) {
            val btn = RadioButton(requireContext())
            btn.setText(type.nameRes)
            btn.setTextColor(colorOnPrimary)
            group.addView(btn)
            idToType[btn.id] = type
        }
        group.check(
            idToType.toList()
                .find { it.second.type == layersViewModel.padType.value?.type }?.first ?: idToType.keys.first()
        )

        group.setOnCheckedChangeListener { _, id ->
            val t = idToType[id] ?: return@setOnCheckedChangeListener
            layersViewModel.padType.value = t
        }
    }
}

sealed class PadType(@StringRes val nameRes: Int, val type: Int) {
    class Normal: PadType(R.string.pad_normal, GoogleMap.MAP_TYPE_NORMAL)
    class Terrain: PadType(R.string.pad_terrain, GoogleMap.MAP_TYPE_TERRAIN)
    class Hybrid: PadType(R.string.pad_hybrid, GoogleMap.MAP_TYPE_HYBRID)
    class Satellite: PadType(R.string.pad_satellite, GoogleMap.MAP_TYPE_SATELLITE)

    companion object {
        val members = listOf(Normal(), Terrain(), Hybrid(), Satellite())
    }
}