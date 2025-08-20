package com.ggetters.app.core.extensions

import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentManager
import com.ggetters.app.R

/**
 * Centralized helper for directional fragment navigation with consistent animations.
 * - Forward: new screen comes from the right, current slides to the left
 * - Backward: new screen comes from the left, current slides to the right
 */
fun Fragment.navigateTo(
    destination: Fragment,
    containerId: Int = R.id.fragmentContainer,
    isForward: Boolean = true,
    addToBackStack: Boolean = true,
    backStackName: String? = null,
    fragmentManager: FragmentManager = parentFragmentManager
) {
    fragmentManager.beginTransaction().apply {
        setReorderingAllowed(true)
        if (isForward) {
            setCustomAnimations(
                R.anim.slide_in_right,
                R.anim.slide_out_left,
                R.anim.slide_in_left,
                R.anim.slide_out_right
            )
        } else {
            setCustomAnimations(
                R.anim.slide_in_left,
                R.anim.slide_out_right,
                R.anim.slide_in_right,
                R.anim.slide_out_left
            )
        }
        replace(containerId, destination)
        if (addToBackStack) addToBackStack(backStackName)
        commit()
    }
}


