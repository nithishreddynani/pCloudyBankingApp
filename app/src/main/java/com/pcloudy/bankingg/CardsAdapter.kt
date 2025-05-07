package com.pcloudy.bankingg

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.TextView
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView

class CardsAdapter(
    private val onCardClick: (BankCard) -> Unit,
    private val onDeleteCard: (BankCard) -> Unit // Ensure this matches the lambda in the fragment
) : ListAdapter<BankCard, CardsAdapter.CardViewHolder>(CardDiffCallback()) {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): CardViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_card, parent, false)
        return CardViewHolder(view, onCardClick, onDeleteCard)
    }

    override fun onBindViewHolder(holder: CardViewHolder, position: Int) {
        holder.bind(getItem(position))
    }

    class CardViewHolder(
        itemView: View,
        private val onCardClick: (BankCard) -> Unit,
        private val onDeleteCard: (BankCard) -> Unit
    ) : RecyclerView.ViewHolder(itemView) {
        fun bind(card: BankCard) {
            itemView.apply {
                findViewById<TextView>(R.id.cardNumber).text =
                    "**** **** **** ${card.cardNumber.takeLast(4)}"
                findViewById<TextView>(R.id.cardHolder).text = card.cardHolderName
                findViewById<TextView>(R.id.cardType).text = card.cardType.name

                setOnClickListener { onCardClick(card) }

                findViewById<Button>(R.id.blockCardButton).setOnClickListener {
                    onDeleteCard(card)
                }
            }
        }
    }

    class CardDiffCallback : DiffUtil.ItemCallback<BankCard>() {
        override fun areItemsTheSame(oldItem: BankCard, newItem: BankCard): Boolean =
            oldItem.id == newItem.id

        override fun areContentsTheSame(oldItem: BankCard, newItem: BankCard): Boolean =
            oldItem == newItem
    }
}