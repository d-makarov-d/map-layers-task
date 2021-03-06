package com.danil.maplayerstask.views

import android.app.AlertDialog
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import androidx.drawerlayout.widget.DrawerLayout
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.lifecycle.Observer
import androidx.viewpager2.adapter.FragmentStateAdapter
import androidx.viewpager2.widget.ViewPager2
import com.danil.maplayerstask.R
import com.danil.maplayerstask.models.MapLayer
import com.danil.maplayerstask.viewmodels.LayerEvent
import com.danil.maplayerstask.viewmodels.MapLayersViewModel
import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.GoogleMap
import com.google.android.gms.maps.SupportMapFragment
import com.google.android.gms.maps.model.LatLng
import com.google.android.gms.maps.model.MarkerOptions
import com.google.android.material.tabs.TabLayout
import com.google.android.material.tabs.TabLayoutMediator
import java.lang.RuntimeException


class MapWithControlsFragment: Fragment() {
    private val layersViewModel: MapLayersViewModel by activityViewModels()
    private var map: GoogleMap? = null

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.fragment_map_with_controls, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        // init map
        val mapFragment =
            childFragmentManager.findFragmentById(R.id.map) as SupportMapFragment
        mapFragment.getMapAsync { map ->
            val type = layersViewModel.padType.value ?: PadType.members[0]
            map.mapType = type.type
            this.map = map

            // draw preselected layers
            layersViewModel.initialized.observe(viewLifecycleOwner) { ready ->
                if (!ready) return@observe

                val layers = layersViewModel.layers.value?.associateBy { it.id() } ?: return@observe
                val stateMap =  layersViewModel.layersState.value ?: return@observe
                for (state in stateMap.values) {
                    layers[state.id]?.elements()?.map { state.apply(map, it) }
                }
            }
        }

        // init drawer layout
        val dl: DrawerLayout = view.findViewById(R.id.main_drawer)
        val controlPane: View = view.findViewById(R.id.pane_right)

        val btnShow: ImageView = view.findViewById(R.id.btn_show_right_pane)
        btnShow.setOnClickListener {
            if (dl.isDrawerOpen(controlPane)) {
                dl.closeDrawer(controlPane)
            } else {
                dl.openDrawer(controlPane)
            }
        }

        // init viewpager in right pane
        val pager: ViewPager2 = view.findViewById(R.id.pager)
        pager.adapter = RightPanePagerAdapter(this)
        pager.isUserInputEnabled = false
        val tabLayout: TabLayout = view.findViewById(R.id.tab_layout)
        TabLayoutMediator(tabLayout, pager) {tab, pos ->
            tab.text = when(pos) {
                0 -> getString(LayersFragment.titleId)
                1 -> getString(PadsFragment.titleId)
                2 -> getString(MissionsFragment.titleId)
                else -> getString(LayersFragment.titleId)
            }
        }.attach()

        // Map type change processing
        layersViewModel.padType.observe(viewLifecycleOwner) { type ->
            val mMap = map ?: return@observe
            mMap.mapType = type.type
        }

        layersViewModel.layersState.observe(viewLifecycleOwner) { stateMap ->
            val layers = layersViewModel.layers.value?.associate { it.id() to it } ?: return@observe
            val mMap = map ?: return@observe
            for (state in stateMap.values) {
                layers[state.id]?.elements()?.map { state.apply(mMap, it) }
            }
        }

        layersViewModel.clearLayerEventListeners()
        layersViewModel.addOnLayerEventListener { event ->
            if (event.layer.elements().isEmpty()) return@addOnLayerEventListener

            when (event) {
                is LayerEvent.Aim -> {
                    val update =
                        CameraUpdateFactory.newLatLngBounds(event.layer.bounds(), 150)
                    map?.animateCamera(update, 1000, null)
                }
                is LayerEvent.List -> {
                    AlertDialog.Builder(requireContext())
                        .setItems(event.layer.elements().map { it.name() }.toTypedArray()) { _, i ->
                            val element = event.layer.elements()[i]
                            val update =
                                CameraUpdateFactory.newLatLngBounds(element.bounds(), 150)
                            map?.animateCamera(update)
                        }
                        .show()
                }
                is LayerEvent.Dash -> {
                    val mMap = map ?: return@addOnLayerEventListener
                    event.layer.elements().forEach { it.setDash(event.layer.dash, mMap) }
                }
            }
        }
    }

    private class RightPanePagerAdapter(f: Fragment): FragmentStateAdapter(f) {
        override fun getItemCount(): Int = 3

        override fun createFragment(position: Int): Fragment {
            return when(position) {
                0 -> LayersFragment()
                1 -> PadsFragment()
                2 -> MissionsFragment()
                else -> LayersFragment()
            }
        }

    }
}