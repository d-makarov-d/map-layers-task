package com.danil.maplayerstask.views

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ListView
import androidx.fragment.app.Fragment
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

        val list: ListView = view.findViewById(R.id.layers_list)
        val adapter = LayersArrayAdapter(requireContext())
        list.adapter = adapter

        LayerRepository.getLayers().observe(viewLifecycleOwner) { layers ->
            adapter.updateAll(layers)
        }

        list.setOnItemClickListener { _, rowView, position, _ ->
            val holder = rowView.tag as LayersArrayAdapter.ViewHolder
            val item = adapter.getItem(position) ?: return@setOnItemClickListener

            adapter.dropView(item, holder)
        }
    }
}