package com.ggetters.app.ui.dialogs

import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.EditText
import android.widget.LinearLayout
import android.widget.TextView
import android.widget.Toast
import com.google.android.material.bottomsheet.BottomSheetDialogFragment
import com.ggetters.app.R
import com.ggetters.app.ui.activities.HomeActivity

class TeamOptionsBottomSheet : BottomSheetDialogFragment() {
    private lateinit var rootView: View
    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?
    ): View? {
        rootView = inflater.inflate(R.layout.bottom_sheet_team_options, container, false)
        showOptions()
        return rootView
    }

    private fun showOptions() {
        val container = rootView.findViewById<LinearLayout>(R.id.teamOptionsContainer)
        container.removeAllViews()
        val title = TextView(requireContext()).apply {
            text = "Choose Your Next Steps"
            textSize = 20f
            setTextColor(resources.getColor(R.color.black, null))
            setPadding(0, 0, 0, 32)
        }
        val createButton = Button(requireContext()).apply {
            text = "Create a Team (Coach)"
            setBackgroundResource(R.drawable.button_border)
            setTextColor(resources.getColor(R.color.black, null))
            setPadding(0, 32, 0, 32)
            val params = LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.WRAP_CONTENT)
            params.setMargins(0, 0, 0, 16)
            layoutParams = params
            setOnClickListener { showCreateTeamForm() }
        }
        val joinButton = Button(requireContext()).apply {
            text = "Join a Team (Player)"
            setBackgroundResource(R.drawable.button_border)
            setTextColor(resources.getColor(R.color.black, null))
            setPadding(0, 32, 0, 32)
            val params = LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.WRAP_CONTENT)
            params.setMargins(0, 0, 0, 0)
            layoutParams = params
            setOnClickListener { showJoinTeamForm() }
        }
        container.addView(title)
        container.addView(createButton)
        container.addView(joinButton)
    }

    private fun showCreateTeamForm() {
        val container = rootView.findViewById<LinearLayout>(R.id.teamOptionsContainer)
        container.removeAllViews()
        val title = TextView(requireContext()).apply {
            text = "Create a Team"
            textSize = 18f
            setTextColor(resources.getColor(R.color.black, null))
            setPadding(0, 0, 0, 24)
        }
        val teamNameInput = EditText(requireContext()).apply {
            hint = "Team Name"
            setTextColor(resources.getColor(R.color.black, null))
            setBackgroundResource(R.drawable.input_background)
            textSize = 16f
            setPadding(32, 24, 32, 24)
            minHeight = 56
            val params = LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.WRAP_CONTENT)
            params.setMargins(0, 0, 0, 24)
            layoutParams = params
        }
        val createButton = Button(requireContext()).apply {
            text = "Create"
            setBackgroundResource(R.drawable.button_border)
            setTextColor(resources.getColor(R.color.black, null))
            setPadding(0, 32, 0, 32)
            val params = LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.WRAP_CONTENT)
            params.setMargins(0, 16, 0, 0)
            layoutParams = params
            setOnClickListener {
                // TODO: Backend - Create team with name
                val intent = Intent(requireContext(), HomeActivity::class.java)
                intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
                startActivity(intent)
                dismiss()
            }
        }
        container.addView(title)
        container.addView(teamNameInput)
        container.addView(createButton)
    }

    private fun showJoinTeamForm() {
        val container = rootView.findViewById<LinearLayout>(R.id.teamOptionsContainer)
        container.removeAllViews()
        val title = TextView(requireContext()).apply {
            text = "Join a Team"
            textSize = 18f
            setTextColor(resources.getColor(R.color.black, null))
            setPadding(0, 0, 0, 24)
        }
        val codeInput = EditText(requireContext()).apply {
            hint = "Team Code"
            setTextColor(resources.getColor(R.color.black, null))
            setBackgroundResource(R.drawable.input_background)
            textSize = 16f
            setPadding(32, 24, 32, 24)
            minHeight = 56
            val params = LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.WRAP_CONTENT)
            params.setMargins(0, 0, 0, 16)
            layoutParams = params
        }
        val memberInput = EditText(requireContext()).apply {
            hint = "Membership Number"
            setTextColor(resources.getColor(R.color.black, null))
            setBackgroundResource(R.drawable.input_background)
            textSize = 16f
            setPadding(32, 24, 32, 24)
            minHeight = 56
            val params = LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.WRAP_CONTENT)
            params.setMargins(0, 0, 0, 24)
            layoutParams = params
        }
        val joinButton = Button(requireContext()).apply {
            text = "Join"
            setBackgroundResource(R.drawable.button_border)
            setTextColor(resources.getColor(R.color.black, null))
            setPadding(0, 32, 0, 32)
            val params = LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.WRAP_CONTENT)
            params.setMargins(0, 16, 0, 0)
            layoutParams = params
            setOnClickListener {
                // TODO: Backend - Join team with code and membership number
                val intent = Intent(requireContext(), HomeActivity::class.java)
                intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
                startActivity(intent)
                dismiss()
            }
        }
        container.addView(title)
        container.addView(codeInput)
        container.addView(memberInput)
        container.addView(joinButton)
    }
} 