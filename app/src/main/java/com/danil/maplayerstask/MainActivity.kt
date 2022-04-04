package com.danil.maplayerstask

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.view.View
import android.widget.ImageView
import androidx.drawerlayout.widget.DrawerLayout

class MainActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        val dl: DrawerLayout = findViewById(R.id.main_drawer)
        val controlPane: View = findViewById(R.id.pane_right)

        val btnShow: ImageView = findViewById(R.id.btn_show_right_pane)
        btnShow.setOnClickListener {
            if (dl.isDrawerOpen(controlPane)) {
                dl.closeDrawer(controlPane)
            } else {
                dl.openDrawer(controlPane)
            }
        }
    }
}