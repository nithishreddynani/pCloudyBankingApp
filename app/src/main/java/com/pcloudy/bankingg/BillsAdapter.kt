package com.pcloudy.bankingg

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.TextView
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import java.text.SimpleDateFormat
import java.util.Locale

class BillsAdapter(
    private val onBillClick: (Bill) -> Unit,
    private val onPayBillClick: (Bill) -> Unit
) : ListAdapter<Bill, BillsAdapter.BillViewHolder>(BillDiffCallback()) {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): BillViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_bill, parent, false)
        return BillViewHolder(view, onBillClick, onPayBillClick)
    }

    override fun onBindViewHolder(holder: BillViewHolder, position: Int) {
        holder.bind(getItem(position))
    }

    class BillViewHolder(
        itemView: View,
        private val onBillClick: (Bill) -> Unit,
        private val onPayBillClick: (Bill) -> Unit
    ) : RecyclerView.ViewHolder(itemView) {
        private val billerNameTextView: TextView = itemView.findViewById(R.id.billerNameTextView)
        private val billAmountTextView: TextView = itemView.findViewById(R.id.billAmountTextView)
        private val dueDateTextView: TextView = itemView.findViewById(R.id.dueDateTextView)
        private val payButton: Button = itemView.findViewById(R.id.payButton)

        fun bind(bill: Bill) {
            billerNameTextView.text = bill.billerName
            billAmountTextView.text = "₹${String.format("%.2f", bill.amount)}"
            dueDateTextView.text = SimpleDateFormat("dd MMM yyyy", Locale.getDefault())
                .format(bill.dueDate)

            payButton.isEnabled = !bill.isPaid
            payButton.setOnClickListener { onPayBillClick(bill) }

            itemView.setOnClickListener { onBillClick(bill) }
        }
    }

    class BillDiffCallback : DiffUtil.ItemCallback<Bill>() {
        override fun areItemsTheSame(oldItem: Bill, newItem: Bill): Boolean =
            oldItem.id == newItem.id

        override fun areContentsTheSame(oldItem: Bill, newItem: Bill): Boolean =
            oldItem == newItem
    }
}