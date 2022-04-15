package com.danil.maplayerstask.util

import android.content.res.ColorStateList
import android.graphics.Color
import androidx.appcompat.widget.SwitchCompat
import com.danil.maplayerstask.R
import com.danil.maplayerstask.viewmodels.SwitchState

class Switch3Pos(
    private val switch: SwitchCompat,
    private var state: SwitchState = SwitchState.StateUndefined
) {
    fun interface OnStateChangedListener {
        fun onChange(state: SwitchState, byUser: Boolean)
    }
    private var stateChangeListener: OnStateChangedListener? = null

    init {
        displayState(state)
        switch.setOnClickListener {
            state = when (state) {
                is SwitchState.StateShowNone -> {
                    SwitchState.StateUndefined
                }
                is SwitchState.StateUndefined -> {
                    SwitchState.StateShowAll
                }
                is SwitchState.StateShowAll -> {
                    SwitchState.StateShowNone
                }
            }
            displayState(state)
            stateChangeListener?.onChange(state, true)
        }
    }

    private fun displayState(state: SwitchState) {
        when(state) {
            is SwitchState.StateShowNone -> {
                switch.isChecked = false
                switch.thumbTintList = null
            }
            is SwitchState.StateUndefined -> {
                switch.isChecked = false
                switch.thumbTintList = ColorStateList.valueOf(Color.argb(0, 0, 0, 0))
            }
            is SwitchState.StateShowAll -> {
                switch.isChecked = true
                switch.thumbTintList = null
            }
        }
    }

    fun setOnStateChangeListener(listener: OnStateChangedListener) {
        stateChangeListener = listener
    }

    fun setState(state: SwitchState) {
        this.state = state
        displayState(state)
        stateChangeListener?.onChange(state, false)
    }

    fun state(): SwitchState = state
}