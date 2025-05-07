package com.pcloudy.bankingg

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView

class ATMAdapter : RecyclerView.Adapter<ATMAdapter.ATMViewHolder>() {
    private var atms = listOf<String>()

    fun updateAtms(newAtms: List<String>) {
        atms = newAtms
        notifyDataSetChanged()
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ATMViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_atm, parent, false)
        return ATMViewHolder(view)
    }

    override fun onBindViewHolder(holder: ATMViewHolder, position: Int) {
        holder.bind(atms[position])
    }

    override fun getItemCount() = atms.size

    class ATMViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        private val textAtmInfo: TextView = itemView.findViewById(R.id.textAtmInfo)

        fun bind(atmInfo: String) {
            textAtmInfo.text = atmInfo
        }
    }
}
