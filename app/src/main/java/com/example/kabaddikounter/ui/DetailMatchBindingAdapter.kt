package com.example.kabaddikounter.ui

import android.widget.Button
import androidx.databinding.BindingAdapter

/**
 * Custom DataBinding adapter so we can pass an Int color
 * directly to a Button's backgroundTint from the ViewModel.
 *
 * Usage in XML:
 *   app:backgroundTintColor="@{viewModel.subscribeButtonColor}"
 *
 * Note: the attribute name here is "backgroundTintColor" to
 * avoid clashing with the built-in "backgroundTint" attribute.
 */
object BindingAdapters {

    @JvmStatic
    @BindingAdapter("backgroundTintColor")
    fun setButtonBackgroundTintColor(button: Button, colorInt: Int?) {
        colorInt ?: return
        button.backgroundTintList =
            android.content.res.ColorStateList.valueOf(colorInt)
    }
}