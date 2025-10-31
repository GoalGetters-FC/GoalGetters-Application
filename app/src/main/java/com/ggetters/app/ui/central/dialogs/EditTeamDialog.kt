package com.ggetters.app.ui.central.dialogs

import android.app.Dialog
import android.content.Context
import android.os.Bundle
import android.widget.ArrayAdapter
import android.widget.AutoCompleteTextView
import androidx.appcompat.app.AlertDialog
import androidx.fragment.app.DialogFragment
import com.ggetters.app.R
import com.ggetters.app.data.model.Team
import com.ggetters.app.data.model.TeamComposition
import com.ggetters.app.data.model.TeamDenomination
import com.ggetters.app.core.utils.Clogger
import com.google.android.material.textfield.TextInputEditText
import com.google.android.material.snackbar.Snackbar

class EditTeamDialog : DialogFragment() {

    companion object {
        const val TAG = "EditTeamDialog"
        
        fun newInstance(
            teamId: String?,
            teamName: String?,
            teamCode: String?,
            teamAlias: String?,
            teamDescription: String?,
            teamComposition: String?,
            teamDenomination: String?,
            yearFormed: String?,
            contactCell: String?,
            contactMail: String?,
            clubAddress: String?
        ): EditTeamDialog {
            val dialog = EditTeamDialog()
            val args = Bundle().apply {
                putString("team_id", teamId)
                putString("team_name", teamName)
                putString("team_code", teamCode)
                putString("team_alias", teamAlias)
                putString("team_description", teamDescription)
                putString("team_composition", teamComposition)
                putString("team_denomination", teamDenomination)
                putString("year_formed", yearFormed)
                putString("contact_cell", contactCell)
                putString("contact_mail", contactMail)
                putString("club_address", clubAddress)
            }
            dialog.arguments = args
            return dialog
        }
    }

    interface EditTeamDialogListener {
        fun onTeamUpdated(updatedTeam: Team)
    }

    private var listener: EditTeamDialogListener? = null

    private lateinit var nameInput: TextInputEditText
    private lateinit var codeInput: TextInputEditText
    private lateinit var aliasInput: TextInputEditText
    private lateinit var descriptionInput: TextInputEditText
    private lateinit var compositionDropdown: AutoCompleteTextView
    private lateinit var denominationDropdown: AutoCompleteTextView
    private lateinit var yearFormedInput: TextInputEditText
    private lateinit var contactCellInput: TextInputEditText
    private lateinit var contactEmailInput: TextInputEditText
    private lateinit var clubAddressInput: TextInputEditText

    override fun onAttach(context: Context) {
        super.onAttach(context)
        try {
            listener = context as EditTeamDialogListener
        } catch (e: ClassCastException) {
            // Parent activity doesn't implement the interface
            // We'll handle this gracefully
            Clogger.w(TAG, "Parent activity doesn't implement EditTeamDialogListener")
        }
    }

    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {
        val view = layoutInflater.inflate(R.layout.dialog_edit_team_comprehensive, null)
        
        setupViews(view)
        setupDropdowns()
        loadTeamData()
        setupClickListeners(view)

        val dialog = AlertDialog.Builder(requireContext())
            .setView(view)
            .create()

        // Set dialog properties for modern appearance
        dialog.window?.let { window ->
            window.setBackgroundDrawableResource(android.R.color.transparent)
            window.attributes?.windowAnimations = R.style.DialogAnimation
        }

        return dialog
    }

    private fun setupViews(view: android.view.View) {
        nameInput = view.findViewById(R.id.editTeamName)
        codeInput = view.findViewById(R.id.editTeamCode)
        aliasInput = view.findViewById(R.id.editTeamAlias)
        descriptionInput = view.findViewById(R.id.editTeamDescription)
        compositionDropdown = view.findViewById(R.id.editComposition)
        denominationDropdown = view.findViewById(R.id.editDenomination)
        yearFormedInput = view.findViewById(R.id.editYearFormed)
        contactCellInput = view.findViewById(R.id.editContactCell)
        contactEmailInput = view.findViewById(R.id.editContactEmail)
        clubAddressInput = view.findViewById(R.id.editClubAddress)

        // Team code is generated, not user-editable
        codeInput.isEnabled = false
        codeInput.isFocusable = false
        codeInput.isClickable = false
    }

    private fun setupDropdowns() {
        // Setup composition dropdown
        val compositionOptions = TeamComposition.values().map { composition ->
            when (composition) {
                TeamComposition.ANYONE -> "Anyone"
                TeamComposition.UNISEX_MALE -> "Unisex (Male)"
                TeamComposition.UNISEX_FEMALE -> "Unisex (Female)"
            }
        }.toTypedArray()

        val compositionAdapter = ArrayAdapter(requireContext(), android.R.layout.simple_dropdown_item_1line, compositionOptions)
        compositionDropdown.setAdapter(compositionAdapter)

        // Setup denomination dropdown
        val denominationOptions = TeamDenomination.values().map { denomination ->
            when (denomination) {
                TeamDenomination.U10 -> "U10"
                TeamDenomination.U11 -> "U11"
                TeamDenomination.U12 -> "U12"
                TeamDenomination.U13 -> "U13"
                TeamDenomination.U14 -> "U14"
                TeamDenomination.U15 -> "U15"
                TeamDenomination.U16 -> "U16"
                TeamDenomination.U17 -> "U17"
                TeamDenomination.U18 -> "U18"
                TeamDenomination.OPEN -> "Open"
            }
        }.toTypedArray()

        val denominationAdapter = ArrayAdapter(requireContext(), android.R.layout.simple_dropdown_item_1line, denominationOptions)
        denominationDropdown.setAdapter(denominationAdapter)
    }

    private fun loadTeamData() {
        arguments?.let { args ->
            nameInput.setText(args.getString("team_name", ""))
            codeInput.setText(args.getString("team_code", ""))
            aliasInput.setText(args.getString("team_alias", ""))
            descriptionInput.setText(args.getString("team_description", ""))
            yearFormedInput.setText(args.getString("year_formed", ""))
            contactCellInput.setText(args.getString("contact_cell", ""))
            contactEmailInput.setText(args.getString("contact_mail", ""))
            clubAddressInput.setText(args.getString("club_address", ""))

            // Set dropdown values
            val composition = args.getString("team_composition", "UNISEX_MALE")
            compositionDropdown.setText(getCompositionDisplayName(composition), false)

            val denomination = args.getString("team_denomination", "OPEN")
            denominationDropdown.setText(getDenominationDisplayName(denomination), false)
        }

        Clogger.d(TAG, "Team data loaded into dialog")
    }

    private fun setupClickListeners(view: android.view.View) {
        view.findViewById<com.google.android.material.button.MaterialButton>(R.id.btnCancel).setOnClickListener {
            dismiss()
        }

        view.findViewById<com.google.android.material.button.MaterialButton>(R.id.btnSave).setOnClickListener {
            saveTeamChanges()
        }
    }

    private fun saveTeamChanges() {
        // TODO: Backend - Implement team data validation
        // TODO: Backend - Save team changes to backend
        // TODO: Backend - Add team change notifications to team members
        // TODO: Backend - Implement team change audit logging

        val teamName = nameInput.text.toString().trim()
        // Preserve existing generated code from args (read-only)
        val teamCode = arguments?.getString("team_code", "")?.trim()?.uppercase() ?: ""
        val teamAlias = aliasInput.text.toString().trim()
        val teamDescription = descriptionInput.text.toString().trim()
        val teamYearFormed = yearFormedInput.text.toString().trim()
        val teamContactCell = contactCellInput.text.toString().trim()
        val teamContactMail = contactEmailInput.text.toString().trim()
        val teamClubAddress = clubAddressInput.text.toString().trim()

        // Validate required fields
        if (teamName.isBlank()) {
            nameInput.error = "Team name is required"
            nameInput.requestFocus()
            return
        }

        // If no code yet, allow save; code will be generated elsewhere (e.g., when inviting members)

        // Validate email format if provided
        if (teamContactMail.isNotBlank() && !android.util.Patterns.EMAIL_ADDRESS.matcher(teamContactMail).matches()) {
            contactEmailInput.error = "Please enter a valid email address"
            contactEmailInput.requestFocus()
            return
        }

        // Get selected dropdown values
        val selectedComposition = getCompositionFromDisplayName(compositionDropdown.text.toString())
        val selectedDenomination = getDenominationFromDisplayName(denominationDropdown.text.toString())

        // Create updated team object
        val updatedTeam = Team(
            id = arguments?.getString("team_id") ?: "",
            name = teamName,
            alias = teamAlias.ifBlank { null },
            description = teamDescription.ifBlank { null },
            code = teamCode,
            composition = selectedComposition,
            denomination = selectedDenomination,
            yearFormed = teamYearFormed.ifBlank { null },
            contactCell = teamContactCell.ifBlank { null },
            contactMail = teamContactMail.ifBlank { null },
            clubAddress = teamClubAddress.ifBlank { null }
        )

        Clogger.d(TAG, "Saving team: $updatedTeam")

        // TODO: Call backend to save team data
        // teamRepository.updateTeam(updatedTeam)

        // Notify listener
        listener?.onTeamUpdated(updatedTeam)

        // Show success message and dismiss
        activity?.let { activity ->
            Snackbar.make(activity.findViewById(android.R.id.content), "Team updated successfully", Snackbar.LENGTH_LONG).show()
        }
        dismiss()
    }

    private fun getCompositionDisplayName(composition: String): String {
        return try {
            when (TeamComposition.valueOf(composition)) {
                TeamComposition.ANYONE -> "Anyone"
                TeamComposition.UNISEX_MALE -> "Unisex (Male)"
                TeamComposition.UNISEX_FEMALE -> "Unisex (Female)"
            }
        } catch (e: Exception) {
            "Unisex (Male)" // Default
        }
    }

    private fun getDenominationDisplayName(denomination: String): String {
        return try {
            when (TeamDenomination.valueOf(denomination)) {
                TeamDenomination.U10 -> "U10"
                TeamDenomination.U11 -> "U11"
                TeamDenomination.U12 -> "U12"
                TeamDenomination.U13 -> "U13"
                TeamDenomination.U14 -> "U14"
                TeamDenomination.U15 -> "U15"
                TeamDenomination.U16 -> "U16"
                TeamDenomination.U17 -> "U17"
                TeamDenomination.U18 -> "U18"
                TeamDenomination.OPEN -> "Open"
            }
        } catch (e: Exception) {
            "Open" // Default
        }
    }

    private fun getCompositionFromDisplayName(displayName: String): TeamComposition {
        return when (displayName) {
            "Anyone" -> TeamComposition.ANYONE
            "Unisex (Male)" -> TeamComposition.UNISEX_MALE
            "Unisex (Female)" -> TeamComposition.UNISEX_FEMALE
            else -> TeamComposition.UNISEX_MALE
        }
    }

    private fun getDenominationFromDisplayName(displayName: String): TeamDenomination {
        return when (displayName) {
            "U10" -> TeamDenomination.U10
            "U11" -> TeamDenomination.U11
            "U12" -> TeamDenomination.U12
            "U13" -> TeamDenomination.U13
            "U14" -> TeamDenomination.U14
            "U15" -> TeamDenomination.U15
            "U16" -> TeamDenomination.U16
            "U17" -> TeamDenomination.U17
            "U18" -> TeamDenomination.U18
            "Open" -> TeamDenomination.OPEN
            else -> TeamDenomination.OPEN
        }
    }
}
