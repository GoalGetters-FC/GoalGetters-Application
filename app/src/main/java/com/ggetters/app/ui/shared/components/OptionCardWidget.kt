package com.ggetters.app.ui.shared.components

import android.content.Context
import android.util.AttributeSet
import android.view.LayoutInflater
import com.ggetters.app.R
import com.ggetters.app.databinding.WidgetOptionCardBinding
import com.google.android.material.card.MaterialCardView

class OptionCardWidget
@JvmOverloads constructor(
    context: Context, attrs: AttributeSet? = null, defStyleAttr: Int = 0
) : MaterialCardView(
    context, attrs, defStyleAttr
) {
    companion object {
        const val TAG = "OptionCardWidget"
    }


// --- Variables


    private val binds = WidgetOptionCardBinding.inflate(LayoutInflater.from(context), this)

    init {
        cardElevation = 0f
        maxCardElevation = 0f
        val a = context.obtainStyledAttributes(attrs, R.styleable.OptionCardWidget)
        try {
            binds.tvText.text = a.getString(R.styleable.OptionCardWidget_widget_text) ?: ""
            binds.ivIcon.setImageResource(
                a.getResourceId(
                    R.styleable.OptionCardWidget_widget_icon, 0
                )
            )
        } finally {
            a.recycle()
        }
    }


// --- Extension


    fun setText(value: String?) {
        binds.tvText.text = value
    }


    fun setIcon(value: Int) {
        binds.ivIcon.setImageResource(value)
    }
}