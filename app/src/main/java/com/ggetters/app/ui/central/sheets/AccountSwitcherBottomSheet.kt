package com.ggetters.app.ui.central.sheets

import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.android.material.bottomsheet.BottomSheetDialogFragment
import com.ggetters.app.R
import com.ggetters.app.ui.central.models.UserAccount
import com.ggetters.app.ui.management.views.TeamViewerActivity

class AccountSwitcherBottomSheet : BottomSheetDialogFragment() {
    
    private lateinit var accountAdapter: AccountAdapter
    private var onAccountSelected: ((UserAccount) -> Unit)? = null
    
    companion object {
        private const val ARG_ACCOUNTS = "accounts"
        
        fun newInstance(
            accounts: List<UserAccount>,
            onAccountSelected: (UserAccount) -> Unit
        ): AccountSwitcherBottomSheet {
            val fragment = AccountSwitcherBottomSheet()
            fragment.onAccountSelected = onAccountSelected
            
            val args = Bundle()
            args.putParcelableArray(ARG_ACCOUNTS, accounts.toTypedArray())
            fragment.arguments = args
            
            return fragment
        }
    }
    
    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.bottom_sheet_account_switcher, container, false)
    }
    
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        
        setupViews(view)

        val button = view.findViewById<TextView>(R.id.tvManageTeams)       
        button.setOnClickListener { 
            startActivity(Intent(requireContext(), TeamViewerActivity::class.java))
        }
        
        loadAccounts()
    }
    
    private fun setupViews(view: View) {
        val recyclerView = view.findViewById<RecyclerView>(R.id.accountsRecyclerView)
        
        accountAdapter = AccountAdapter { account ->
            onAccountSelected?.invoke(account)
            dismiss()
        }
        
        recyclerView.layoutManager = LinearLayoutManager(context)
        recyclerView.adapter = accountAdapter
    }
    
    private fun loadAccounts() {
        val accounts = arguments?.getParcelableArray(ARG_ACCOUNTS) as? Array<UserAccount> ?: emptyArray()
        accountAdapter.updateAccounts(accounts.toList())
    }
    
    private inner class AccountAdapter(
        private val onAccountClick: (UserAccount) -> Unit
    ) : RecyclerView.Adapter<AccountAdapter.AccountViewHolder>() {
        
        private var accounts: List<UserAccount> = emptyList()
        
        fun updateAccounts(newAccounts: List<UserAccount>) {
            accounts = newAccounts
            notifyDataSetChanged()
        }
        
        override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): AccountViewHolder {
            val view = LayoutInflater.from(parent.context)
                .inflate(R.layout.item_account, parent, false)
            return AccountViewHolder(view)
        }
        
        override fun onBindViewHolder(holder: AccountViewHolder, position: Int) {
            holder.bind(accounts[position])
        }
        
        override fun getItemCount(): Int = accounts.size
        
        inner class AccountViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
            private val avatarImage: ImageView = itemView.findViewById(R.id.accountAvatar)
            private val nameText: TextView = itemView.findViewById(R.id.accountName)
            private val emailText: TextView = itemView.findViewById(R.id.accountEmail)
            private val teamText: TextView = itemView.findViewById(R.id.accountTeam)
            private val roleText: TextView = itemView.findViewById(R.id.accountRole)
            private val activeIndicator: View = itemView.findViewById(R.id.activeIndicator)
            
            init {
                itemView.setOnClickListener {
                    val position = adapterPosition
                    if (position != RecyclerView.NO_POSITION) {
                        onAccountClick(accounts[position])
                    }
                }
            }
            
            fun bind(account: UserAccount) {
                nameText.text = account.name
                emailText.text = account.email
                teamText.text = account.teamName
                roleText.text = account.role
                
                // Show active indicator
                activeIndicator.visibility = if (account.isActive) View.VISIBLE else View.GONE
                
                // TODO: Load account avatar using Glide or similar
                // if (account.avatar != null) {
                //     Glide.with(itemView.context)
                //         .load(account.avatar)
                //         .placeholder(R.drawable.default_avatar)
                //         .into(avatarImage)
                // } else {
                //     avatarImage.setImageResource(R.drawable.default_avatar)
                // }
            }
        }
    }
} 