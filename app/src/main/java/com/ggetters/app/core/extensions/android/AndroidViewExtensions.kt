package com.ggetters.app.core.extensions.android

import android.view.View
import androidx.databinding.BindingAdapter

@BindingAdapter("visibleIfElevated")
fun View.setVisibleIfElevated(isElevated: Boolean) {
    visibility = if (isElevated) View.VISIBLE else View.GONE
}