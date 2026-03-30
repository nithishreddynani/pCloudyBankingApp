package com.pcloudy.bankingg

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.lifecycle.lifecycleScope
import com.google.android.material.snackbar.Snackbar
import com.pcloudy.bankingg.databinding.FragmentWalletBinding
import kotlinx.coroutines.launch

class WalletFragment : Fragment() {
    private var _binding: FragmentWalletBinding? = null
    private val binding get() = _binding!!
    private val viewModel: MainViewModel by activityViewModels()

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentWalletBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        setupViews()
        observeViewModel()
    }

    override fun onResume() {
        super.onResume()
        loadBalance()  // always fetch fresh balances from server when wallet page is visible
    }

    private fun setupViews() {
        binding.apply {
            topupButton.setOnClickListener {
                val amount = topupAmountInput.text.toString().toDoubleOrNull()
                if (amount != null && amount > 0) {
                    performTopup(amount)
                } else {
                    Snackbar.make(binding.root, "Please enter a valid amount", Snackbar.LENGTH_SHORT).show()
                }
            }
        }
    }

    private fun observeViewModel() {
        // Wallet balance (from /wallet/balance)
        viewModel.walletBalance.observe(viewLifecycleOwner) { response ->
            response?.let { binding.balanceText.text = "₹${it.walletBalance}" }
        }

        // Account balance (source for top-up)
        viewModel.balanceResponse.observe(viewLifecycleOwner) { response ->
            response?.let { binding.accountBalanceText.text = "₹${it.balance}" }
        }

        viewModel.isLoading.observe(viewLifecycleOwner) { isLoading ->
            binding.progressBar.visibility = if (isLoading) View.VISIBLE else View.GONE
        }

        viewModel.error.observe(viewLifecycleOwner) { error ->
            error?.let { Snackbar.make(binding.root, it, Snackbar.LENGTH_LONG).show() }
        }
    }

    private fun loadBalance() {
        lifecycleScope.launch {
            viewModel.fetchWalletBalance()
            viewModel.fetchBalance()
        }
    }

    private fun performTopup(amount: Double) {
        lifecycleScope.launch {
            try {
                viewModel.topupWallet(amount)
                viewModel.fetchWalletBalance()
                viewModel.fetchBalance()
                binding.topupAmountInput.text?.clear()
                Snackbar.make(binding.root, "Top-up successful!", Snackbar.LENGTH_SHORT).show()
            } catch (e: Exception) {
                Snackbar.make(binding.root, e.message ?: "Top-up failed", Snackbar.LENGTH_LONG).show()
            }
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}