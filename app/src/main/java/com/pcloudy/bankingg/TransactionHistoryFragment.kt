package com.pcloudy.bankingg

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import com.google.android.material.snackbar.Snackbar
import com.pcloudy.bankingg.databinding.FragmentTransactionHistoryBinding
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.launch

class TransactionHistoryFragment : Fragment() {
    private var _binding: FragmentTransactionHistoryBinding? = null
    private val binding get() = _binding!!
    private val viewModel: MainViewModel by activityViewModels()
    private val transactionAdapter = TransactionAdapter()
    private var currentPage = 1

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentTransactionHistoryBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        setupRecyclerView()
        setupViews()
        observeViewModel()
        loadTransactions()
    }

    private fun setupRecyclerView() {
        binding.transactionsRecyclerView.apply {
            adapter = transactionAdapter
            layoutManager = LinearLayoutManager(context)
        }
    }

    private fun setupViews() {
        binding.apply {
            loadMoreButton.setOnClickListener {
                currentPage++
                loadTransactions()
            }

            filterButton.setOnClickListener {
                // Implement filter dialog
                Snackbar.make(binding.root, "Filter functionality coming soon", Snackbar.LENGTH_SHORT).show()
            }
        }
    }

    private fun observeViewModel() {
        viewLifecycleOwner.lifecycleScope.launchWhenStarted {
            viewModel.transactionsPage.collect { page ->
                page?.let {
                    val failed = viewModel.failedTransactions.value ?: emptyList()
                    val enriched = (it.transactions + failed).sortedByDescending { txn -> txn.date }
                    if (currentPage == 1) {
                        transactionAdapter.updateTransactions(enriched)
                    } else {
                        transactionAdapter.addTransactions(enriched)
                    }
                    binding.loadMoreButton.visibility = if (it.pagination.hasNext) View.VISIBLE else View.GONE
                }
            }
        }

        viewModel.isLoading.observe(viewLifecycleOwner) { isLoading ->
            binding.progressBar.visibility = if (isLoading) View.VISIBLE else View.GONE
        }

        viewModel.error.observe(viewLifecycleOwner) { error ->
            error?.let {
                Snackbar.make(binding.root, it, Snackbar.LENGTH_LONG).show()
            }
        }
    }

    private fun loadTransactions() {
        lifecycleScope.launch {
            viewModel.loadTransactions(currentPage)
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}