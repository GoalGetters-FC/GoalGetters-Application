package com.ggetters.app.ui.shared.modals

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.ggetters.app.R
import com.ggetters.app.core.utils.Clogger
import com.ggetters.app.databinding.ModalBottomSheetJoinTeamBinding
import com.ggetters.app.ui.shared.models.Clickable
import com.google.android.material.bottomsheet.BottomSheetDialogFragment

class JoinTeamBottomSheet(
    private val onSubmit: (String, String) -> Unit
) : BottomSheetDialogFragment(), Clickable {
    companion object {
        const val TAG = "JoinTeamBottomSheet"
    }


    private lateinit var binds: ModalBottomSheetJoinTeamBinding


// --- Lifecycle


    override fun onCreate(savedInstanceState: Bundle?) {
        setStyle(STYLE_NORMAL, R.style.Widget_Local_ModalBottomSheet)
        super.onCreate(savedInstanceState)
    }


    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?
    ): View? = createBindings(inflater, container)


    override fun onViewCreated(
        view: View, savedInstanceState: Bundle?
    ) {
        super.onViewCreated(view, savedInstanceState)
        setupTouchListeners()
    }


// --- Event Handlers


    override fun setupTouchListeners() {
        binds.btContinue.setOnClickListener(this)
    }


    /**
     * TODO: Abstract logic to a function with input validation
     */
    override fun onClick(view: View?) = when (view?.id) {
        binds.btContinue.id -> {
            val teamCode = binds.etCodeTeam.text.toString().trim()
            val userCode = binds.etCodeUser.text.toString().trim()
            onSubmit(
                teamCode, userCode
            )

            dismiss()
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
        binds = ModalBottomSheetJoinTeamBinding.inflate(inflater, container, false)
        return binds.root
    }
}