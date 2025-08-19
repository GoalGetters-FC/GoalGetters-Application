package com.ggetters.app.ui.central.views

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.fragment.app.viewModels
import com.ggetters.app.core.utils.Clogger
import com.ggetters.app.databinding.FragmentHomeTeamBinding
import com.ggetters.app.ui.central.viewmodels.HomeTeamViewModel
import com.ggetters.app.ui.central.viewmodels.HomeViewModel
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class HomeTeamFragment : Fragment() {
    companion object {
        private const val TAG = "HomeTeamFragment"
    }


// --- Fields


    private val activeModel: HomeTeamViewModel by viewModels()
    private val sharedModel: HomeViewModel by activityViewModels()


    private lateinit var binds: FragmentHomeTeamBinding


// --- Lifecycle


    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?
    ): View? = createBindings(inflater, container)


    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        Clogger.d(
            TAG, "Created a new instance of HomeTeamFragment"
        )
    }


// --- UI


    /**
     * Construct the view binding for this fragment.
     *
     * @return the root [View] of this fragment within the same context as every
     *         other invocation of the binding instance. This is crucial because
     *         otherwise they would exist in different contexts.
     */
    private fun createBindings(
        inflater: LayoutInflater, container: ViewGroup?
    ): View {
        binds = FragmentHomeTeamBinding.inflate(inflater, container, false)
        return binds.root
    }
}