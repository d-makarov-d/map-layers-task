package com.danil.maplayerstask.views

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ListView
import android.widget.ToggleButton
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.danil.maplayerstask.R
import com.danil.maplayerstask.adapters.LayersArrayAdapter
import com.danil.maplayerstask.models.LayerRepository

class LayersFragment: Fragment() {
    companion object {
        const val titleId = R.string.title_layers
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
        val adapter = LayersArrayAdapter(requireContext())
        list.adapter = adapter

        LayerRepository.getLayers().observe(viewLifecycleOwner) { layers ->
            adapter.updateAll(layers)
        }

        val reorder: ToggleButton = view.findViewById(R.id.btn_reorder)
        reorder.setOnCheckedChangeListener { _, checked -> adapter.setReorder(checked)  }
    }
}