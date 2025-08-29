package com.ggetters.app.ui.central.views

import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.fragment.app.viewModels
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import com.ggetters.app.core.utils.Clogger
import com.ggetters.app.databinding.FragmentSettingsBinding
import com.ggetters.app.ui.central.models.AppbarTheme
import com.ggetters.app.ui.central.models.HomeUiConfiguration
import com.ggetters.app.ui.central.viewmodels.HomeSettingsViewModel
import com.ggetters.app.ui.central.viewmodels.HomeViewModel
import com.ggetters.app.ui.shared.models.Clickable
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.launch

@AndroidEntryPoint
class HomeSettingsFragment : Fragment(), Clickable {
    companion object {
        private const val TAG = "HomeSettingsFragment"
    }


// --- Fields


    private val activeModel: HomeSettingsViewModel by viewModels()
    private val sharedModel: HomeViewModel by activityViewModels()


    private lateinit var binds: FragmentSettingsBinding


// --- Lifecycle


    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?
    ): View? = createBindings(inflater, container)


    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        setupTouchListeners()

        val email = activeModel.getAuthAccount()?.email
        binds.tvText.text = email ?: "Not signed in"

        viewLifecycleOwner.lifecycleScope.launch {
            viewLifecycleOwner.repeatOnLifecycle(Lifecycle.State.STARTED) {
                activeModel.fullName.collect { name ->
                    val fallback = email?.substringBefore('@')
                        ?.replace('.', ' ')
                        ?.replaceFirstChar { it.uppercase() }
                    binds.tvHead.text = name ?: fallback ?: email ?: "Account"
                }
            }
        }

        sharedModel.useViewConfiguration(
            HomeUiConfiguration(
                appBarColor = AppbarTheme.NIGHT,
                appBarTitle = "",
                appBarShown = true,
            )
        )
    }


// --- Event Handlers


    override fun setupTouchListeners() {
        binds.cvOptionAccount.setOnClickListener(this)
        binds.cvOptionSettings.setOnClickListener(this)
        binds.cvOptionNotifications.setOnClickListener(this)
        binds.cvOptionPrivacy.setOnClickListener(this)
        binds.cvOptionContact.setOnClickListener(this)
        binds.cvOptionFaq.setOnClickListener(this)
        binds.btLogout.setOnClickListener(this)
    }


    override fun onClick(view: View?) = when (view?.id) {
        binds.cvOptionAccount.id -> {}
        binds.cvOptionSettings.id -> {}
        binds.cvOptionNotifications.id -> {
            startActivity(Intent(requireContext(), NotificationsActivity::class.java))
        }

        binds.cvOptionPrivacy.id -> {}
        binds.cvOptionContact.id -> {}
        binds.cvOptionFaq.id -> {}
        binds.btLogout.id -> {
            activeModel.logout()
        }

        else -> {
            Clogger.w(
                TAG, "Unhandled on-click for: ${view?.id}"
            )
        }
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
        binds = FragmentSettingsBinding.inflate(inflater, container, false)
        return binds.root
    }
}