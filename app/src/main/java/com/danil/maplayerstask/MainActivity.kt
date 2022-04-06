package com.danil.maplayerstask

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.view.View
import android.widget.ImageView
import androidx.drawerlayout.widget.DrawerLayout
import com.danil.maplayerstask.models.LayerRepository
import com.danil.maplayerstask.views.MapWithControlsFragment

class MainActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        LayerRepository.init(applicationContext)

        val ft = supportFragmentManager.beginTransaction()
        ft.replace(R.id.main_container, MapWithControlsFragment::class.java, Bundle())
        ft.commit()
    }
}