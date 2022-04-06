package com.danil.maplayerstask.views

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import com.danil.maplayerstask.R

class PadsFragment: Fragment() {
    companion object {
        const val titleId = R.string.title_pads
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.fragment_pads, container, false)
    }
}