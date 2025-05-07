package com.pcloudy.bankingg

import android.graphics.Color
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.RecyclerView
import com.pcloudy.bankingg.databinding.ItemTransactionBinding
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

//class TransactionAdapter : RecyclerView.Adapter<TransactionAdapter.TransactionViewHolder>() {
//    private val transactions = mutableListOf<Transaction>()
//
//    fun getCurrentTransactions(): List<Transaction> = transactions
//
//    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): TransactionViewHolder {
//        val binding = ItemTransactionBinding.inflate(
//            LayoutInflater.from(parent.context),
//            parent,
//            false
//        )
//        return TransactionViewHolder(binding)
//    }
//
//    override fun onBindViewHolder(holder: TransactionViewHolder, position: Int) {
//        holder.bind(transactions[position])
//    }
//
//    override fun getItemCount() = transactions.size
//
//    inner class TransactionViewHolder(
//        private val binding: ItemTransactionBinding
//    ) : RecyclerView.ViewHolder(binding.root) {
//
//        fun bind(transaction: Transaction) {
//            binding.apply {
//                transactionDescription.text = transaction.description
//                transactionAmount.text = if (transaction.amount < 0)
//                    "-$${-transaction.amount}" else "$${transaction.amount}"
//                transactionDate.text = formatDate(transaction.date)
//                transactionStatus.text = transaction.status
//
//                // Set color based on transaction type
//                transactionAmount.setTextColor(
//                    if (transaction.amount < 0) {
//                        Color.RED
//                    } else {
//                        Color.GREEN
//                    }
//                )
//            }
//        }
//
//        private fun formatDate(timestamp: Long): String {
//            return SimpleDateFormat("MMM dd, yyyy", Locale.getDefault())
//                .format(Date(timestamp))
//        }
//    }
//
//
//
//    fun updateTransactions(newTransactions: List<Transaction>) {
//        transactions.clear()
//        transactions.addAll(newTransactions.take(3)) // Limit to 3 transactions
//        notifyDataSetChanged()
//    }
//
//
//
//}

class TransactionAdapter : RecyclerView.Adapter<TransactionAdapter.TransactionViewHolder>() {
    private val transactions = mutableListOf<Transaction>()

    fun getCurrentTransactions(): List<Transaction> = transactions

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): TransactionViewHolder {
        val binding = ItemTransactionBinding.inflate(
            LayoutInflater.from(parent.context),
            parent,
            false
        )
        return TransactionViewHolder(binding)
    }

    override fun onBindViewHolder(holder: TransactionViewHolder, position: Int) {
        holder.bind(transactions[position])
    }

    override fun getItemCount() = transactions.size

    // Nested ViewHolder class
    class TransactionViewHolder(
        private val binding: ItemTransactionBinding
    ) : RecyclerView.ViewHolder(binding.root) {

        fun bind(transaction: Transaction) {
            binding.apply {
                // Customize description based on transaction type
                transactionDescription.text = getTransactionDescription(transaction)

                // Format amount with appropriate sign and color
                transactionAmount.text = formatAmount(transaction.amount)
                transactionAmount.setTextColor(getAmountColor(transaction.amount))

                // Format date
                transactionDate.text = formatDate(transaction.date)

                // Set status
                transactionStatus.text = transaction.status
            }
        }

        private fun getTransactionDescription(transaction: Transaction): String {
            return when (transaction.type) {
                "recharge" -> "Mobile Recharge"
                "transfer" -> "Money Transfer"
                "bill" -> "Bill Payment"
                "deposit" -> "Salary Deposit"
                else -> transaction.description
            }
        }

        private fun formatAmount(amount: Double): String {
            return if (amount < 0) {
                "-$${String.format("%.2f", -amount)}"
            } else {
                "$${String.format("%.2f", amount)}"
            }
        }

        private fun getAmountColor(amount: Double): Int {
            return if (amount < 0) Color.RED else Color.GREEN
        }

        private fun formatDate(timestamp: Long): String {
            return SimpleDateFormat("MMM dd, yyyy", Locale.getDefault())
                .format(Date(timestamp))
        }
    }

    fun updateTransactions(newTransactions: List<Transaction>) {
        transactions.clear()
        // Take only the last 3 transactions
        transactions.addAll(newTransactions.takeLast(3))
        notifyDataSetChanged()
    }
}