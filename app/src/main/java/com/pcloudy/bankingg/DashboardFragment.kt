package com.pcloudy.bankingg

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import androidx.navigation.fragment.findNavController
import androidx.navigation.navOptions
import androidx.recyclerview.widget.LinearLayoutManager
import com.google.android.material.snackbar.Snackbar
import com.pcloudy.bankingg.databinding.FragmentDashboardBinding
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class DashboardFragment : Fragment() {
    private var _binding: FragmentDashboardBinding? = null
    private val binding get() = _binding!!
    private val viewModel: MainViewModel by activityViewModels()
    private val transactionAdapter = TransactionAdapter()
    private var billPaymentCount = 0

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentDashboardBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        setupViews()
        setupTransactionsList()
        observeViewModel()
    }

    override fun onResume() {
        super.onResume()
        loadData()  // always fetch fresh balance from server when dashboard is visible
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }

    private fun setupViews() {
        binding.apply {
//            transferMoney.setOnClickListener {
//                findNavController().navigate(R.id.action_dashboard_to_transfer)
//            }
//            payBills.setOnClickListener {
//                findNavController().navigate(R.id.action_dashboard_to_billPayment)
//            }
//            cards.setOnClickListener {
//                findNavController().navigate(R.id.action_dashboard_to_cards)
//            }
//            mobileRecharge.setOnClickListener {
//                findNavController().navigate(R.id.action_dashboard_to_recharge)
//            }
            upiPayment.setOnClickListener {
                findNavController().navigate(R.id.action_dashboard_to_upi)
            }
            wallet.setOnClickListener {
                findNavController().navigate(R.id.action_dashboard_to_wallet)
            }
            transactions.setOnClickListener {
                findNavController().navigate(R.id.action_dashboard_to_transactions)
            }
            profile.setOnClickListener {
                findNavController().navigate(R.id.action_dashboard_to_profile)
            }
            more.setOnClickListener {
                findNavController().navigate(R.id.action_anr)
            }
            logoutButton.setOnClickListener { handleLogout() }
        }
    }

    private fun handleLogout() {
        lifecycleScope.launch {
            try {
                viewModel.simulateApiCall("/auth/logout") {
                    withContext(Dispatchers.Main) {
                        binding.root.animate()
                            .alpha(0f)
                            .setDuration(200)
                            .withEndAction {
                                viewModel.logout()
                                findNavController().navigate(
                                    R.id.loginFragment,
                                    null,
                                    navOptions {
                                        popUpTo(R.id.nav_graph) {
                                            inclusive = true
                                        }
                                        anim {
                                            enter = R.anim.slide_in_left
                                            exit = R.anim.slide_out_right
                                            popEnter = R.anim.fade_in
                                            popExit = R.anim.fade_out
                                        }
                                    }
                                )
                            }
                            .start()
                    }
                }
            } catch (e: Exception) {
                withContext(Dispatchers.Main) {
                    binding.root.alpha = 1f  // Reset alpha if error occurs
                    Snackbar.make(
                        binding.root,
                        "Logout failed: ${e.message}",
                        Snackbar.LENGTH_LONG
                    ).show()
                }
            }
        }
    }

    private fun observeViewModel() {
        viewModel.balanceResponse.removeObservers(viewLifecycleOwner)
        viewModel.notifications.removeObservers(viewLifecycleOwner)
        viewModel.isLoading.removeObservers(viewLifecycleOwner)
        viewModel.error.removeObservers(viewLifecycleOwner)

        // Observe balance from server
        viewModel.balanceResponse.observe(viewLifecycleOwner) { response ->
            response?.let { binding.balanceAmount.text = "₹${it.balance}" }
        }

        // Observe notifications — show alerts for money received and low balance
        viewModel.notifications.observe(viewLifecycleOwner) { notifications ->
            val moneyReceived = notifications?.firstOrNull { it.type == "MONEY_RECEIVED" }
            if (moneyReceived != null) {
                Snackbar.make(binding.root, moneyReceived.body ?: "Money received", Snackbar.LENGTH_LONG).show()
            }
            val lowBalance = notifications?.firstOrNull { it.type == "LOW_BALANCE" }
            if (lowBalance != null) {
                Snackbar.make(binding.root, lowBalance.body ?: "Low balance alert", Snackbar.LENGTH_LONG)
                    .setAction("Top Up") {
                        findNavController().navigate(R.id.action_dashboard_to_wallet)
                    }
                    .show()
            }
        }

        // Observe loading state
        viewModel.isLoading.observe(viewLifecycleOwner) { _ -> }

        // Observe error messages
        viewModel.error.observe(viewLifecycleOwner) { errorMessage ->
            errorMessage?.let {
                Snackbar.make(binding.root, it, Snackbar.LENGTH_LONG).show()
            }
        }

        viewLifecycleOwner.lifecycleScope.launch {
            viewLifecycleOwner.repeatOnLifecycle(Lifecycle.State.STARTED) {
                viewModel.transactionsPage.collect { page ->
                    page?.let { transactionAdapter.updateTransactions(it.transactions) }
                }
            }
        }
    }

    private fun setupTransactionsList() {
        binding.transactionsRecyclerView.apply {
            adapter = transactionAdapter
            layoutManager = LinearLayoutManager(context)
        }
    }

    private fun loadData() {
        lifecycleScope.launch {
            viewModel.fetchBalance()
            viewModel.loadTransactions(1)
            viewModel.fetchNotifications()
        }
    }

}




