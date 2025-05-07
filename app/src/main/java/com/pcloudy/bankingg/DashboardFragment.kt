package com.pcloudy.bankingg

import android.annotation.SuppressLint
import android.graphics.Color
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.RadioButton
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import androidx.navigation.fragment.findNavController
import androidx.navigation.navOptions
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import com.google.android.material.snackbar.Snackbar
import com.google.android.material.textfield.TextInputEditText
import com.pcloudy.bankingg.databinding.FragmentDashboardBinding
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.util.UUID

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
        loadDummyData()
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }

    private fun setupViews() {
        binding.apply {
            transferMoney.setOnClickListener {
                findNavController().navigate(R.id.action_dashboard_to_transfer)
            }
            payBills.setOnClickListener {
                findNavController().navigate(R.id.action_dashboard_to_billPayment)
            }
            cards.setOnClickListener {
                findNavController().navigate(R.id.action_dashboard_to_cards)
            }
            mobileRecharge.setOnClickListener {
                findNavController().navigate(R.id.action_dashboard_to_recharge)
            }
            upiPayment.setOnClickListener {
                findNavController().navigate(R.id.action_dashboard_to_upi)
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
        // Observe balance
        viewModel.balance.observe(viewLifecycleOwner) { balance ->
            binding.balanceAmount.text = "$${balance}"
        }

        // Observe loading state
        viewModel.isLoading.observe(viewLifecycleOwner) { isLoading ->
//            binding.progressBar.visibility = if (isLoading) View.VISIBLE else View.GONE
//            // Disable buttons while loading
//            binding.apply {
//                buttonTransfer.isEnabled = !isLoading
//                buttonLogout.isEnabled = !isLoading
//            }
        }

        // Observe error messages
        viewModel.error.observe(viewLifecycleOwner) { errorMessage ->
            errorMessage?.let {
                Snackbar.make(binding.root, it, Snackbar.LENGTH_LONG).show()
            }
        }

        viewLifecycleOwner.lifecycleScope.launch {
            viewLifecycleOwner.repeatOnLifecycle(Lifecycle.State.STARTED) {
                viewModel.transactions.collect { transactions ->
                    transactionAdapter.updateTransactions(transactions)
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

    private fun loadDummyData() {
        val dummyTransactions = listOf(
            Transaction(
                "1",
                -50.0,
                "bill",
                "Electricity Bill Payment",
                System.currentTimeMillis() - 86400000, // 1 day ago
                "Completed"
            ),
            Transaction(
                "2",
                -100.0,
                "transfer",
                "Transfer to John",
                System.currentTimeMillis() - 172800000, // 2 days ago
                "Completed"
            ),
            Transaction(
                "3",
                500.0,
                "deposit",
                "Salary Deposit",
                System.currentTimeMillis() - 259200000, // 3 days ago
                "Completed"
            )
        )
        transactionAdapter.updateTransactions(dummyTransactions)

        // Update spending summary
//        binding.apply {
//            monthlySpending.text = "This month: $650.00"
//            spendingTrend.text = "15% less than last month"
//        }
    }

}




