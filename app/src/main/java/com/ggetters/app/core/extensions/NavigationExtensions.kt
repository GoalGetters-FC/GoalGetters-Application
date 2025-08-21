package com.ggetters.app.core.extensions

import androidx.fragment.app.Fragment
import com.ggetters.app.R

/**
 * Centralized helper for fragment navigation with directional animations.
 */
fun Fragment.navigateTo(
    destination: Fragment,
    isForward: Boolean = true,
    addToBackStack: Boolean = true,
    backStackName: String? = null
) {
    val transaction = parentFragmentManager.beginTransaction()
    transaction.setReorderingAllowed(true)

    if (isForward) {
        transaction.setCustomAnimations(
            R.anim.slide_in_right,
            R.anim.slide_out_left,
            R.anim.slide_in_left,
            R.anim.slide_out_right
        )
    } else {
        transaction.setCustomAnimations(
            R.anim.slide_in_left,
            R.anim.slide_out_right,
            R.anim.slide_in_right,
            R.anim.slide_out_left
        )
    }

    transaction.replace(R.id.fragmentContainer, destination)
    if (addToBackStack) {
        transaction.addToBackStack(backStackName)
    }
    transaction.commit()
}


