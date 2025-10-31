package com.ggetters.app.ui.shared.components

import android.content.Context
import android.util.AttributeSet
import android.view.LayoutInflater
import android.widget.LinearLayout
import com.ggetters.app.R
import com.ggetters.app.databinding.WidgetHomeViewHeaderBinding

class HomeViewHeaderWidget
@JvmOverloads constructor(
    context: Context, attrs: AttributeSet? = null, defStyleAttr: Int = 0
) : LinearLayout(
    context, attrs, defStyleAttr
) {
    companion object {
        const val TAG = "HomeViewHeaderWidget"
    }


// --- Variables


    private val binds = WidgetHomeViewHeaderBinding.inflate(LayoutInflater.from(context), this)

    init {
        val a = context.obtainStyledAttributes(attrs, R.styleable.HomeViewHeaderWidget)
        try {
            binds.tvHead.text = a.getString(R.styleable.HomeViewHeaderWidget_widget_heading) ?: ""
            binds.tvText.text = a.getString(R.styleable.HomeViewHeaderWidget_widget_message) ?: ""
        } finally {
            a.recycle()
        }
    }


// --- Extension


    fun setHeadingText(value: String?) {
        binds.tvHead.text = value
    }


    fun setMessageText(value: String?) {
        binds.tvText.text = value
    }
}