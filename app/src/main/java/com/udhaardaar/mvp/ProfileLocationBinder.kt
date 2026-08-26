package com.udhaardaar.mvp

import android.widget.EditText

/** Binds validated PIN lookups to profile city/state fields. */
object ProfileLocationBinder {
    fun bind(pinField: EditText, cityField: EditText, stateField: EditText) {
        pinField.setOnFocusChangeListener { _, hasFocus ->
            if (!hasFocus) resolve(pinField, cityField, stateField)
        }
        pinField.setOnEditorActionListener { _, _, _ ->
            resolve(pinField, cityField, stateField)
            false
        }
    }

    fun resolve(pinField: EditText, cityField: EditText, stateField: EditText): Boolean {
        val location = PinLocationResolver.lookup(pinField.text.toString()) ?: run {
            if (pinField.text.length == 6) pinField.error = "Enter a valid PIN"
            return false
        }
        cityField.setText(location.city)
        stateField.setText(location.state)
        return true
    }
}
