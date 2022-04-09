package com.danil.maplayerstask.views

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import androidx.drawerlayout.widget.DrawerLayout
import androidx.fragment.app.Fragment
import androidx.viewpager2.adapter.FragmentStateAdapter
import androidx.viewpager2.widget.ViewPager2
import com.danil.maplayerstask.R
import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.SupportMapFragment
import com.google.android.gms.maps.model.LatLng
import com.google.android.gms.maps.model.MarkerOptions
import com.google.android.material.tabs.TabLayout
import com.google.android.material.tabs.TabLayoutMediator


class MapWithControlsFragment: Fragment() {
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
        mapFragment.getMapAsync {  }

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