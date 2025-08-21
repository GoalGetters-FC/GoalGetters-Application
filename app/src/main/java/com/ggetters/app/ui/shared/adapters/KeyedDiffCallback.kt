package com.ggetters.app.ui.shared.adapters

import android.annotation.SuppressLint
import androidx.recyclerview.widget.DiffUtil
import com.ggetters.app.core.utils.Clogger
import com.ggetters.app.data.model.supers.KeyedEntity

class KeyedDiffCallback<T : KeyedEntity> : DiffUtil.ItemCallback<T>() {
    companion object {
        private const val TAG = "KeyedDiffCallback"
    }


// --- Functions


    override fun areItemsTheSame(
        oldItem: T, newItem: T
    ): Boolean {
        val result = (oldItem.id == newItem.id)
        return result
    }


    @SuppressLint("DiffUtilEquals")
    override fun areContentsTheSame(
        oldItem: T, newItem: T
    ): Boolean {
        if (isEqualsUsingReference(oldItem)) Clogger.w(
            TAG, "${oldItem::class.simpleName} does not override equals(); performance reduced."
        )

        return (oldItem == newItem)
    }


// --- Internals


    /**
     * Checks whether the compared objects use reference equality.
     *
     * @return `True` if the compared objects are not data classes which compare
     *         hashes efficiently or a class which overrides `equals`. Reference
     *         equality is inefficient and will produce false inequalities.
     */
    private fun <T : KeyedEntity> isEqualsUsingReference(item: T): Boolean {
        val usedEqualsMethod = item::class.java.getMethod("equals", Any::class.java)
        return usedEqualsMethod.declaringClass == Any::class.java
    }
}