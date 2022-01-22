package com.example.cryptotraker

import android.annotation.SuppressLint
import android.content.Context
import androidx.recyclerview.widget.RecyclerView
import com.example.cryptotraker.CurrencyRVAdapter.CurrencyViewHolder
import android.view.ViewGroup
import android.view.LayoutInflater
import android.view.View
import android.widget.TextView
import java.text.DecimalFormat
import java.util.ArrayList


class CurrencyRVAdapter(
    private var currencyModals: ArrayList<CurrencyModal>,
    private val context: Context,
    private val listener : OnItemClickListener
) : RecyclerView.Adapter<CurrencyViewHolder>() {

    @SuppressLint("NotifyDataSetChanged")
    fun filterList(filterllist: ArrayList<CurrencyModal>) {
        currencyModals = filterllist
        notifyDataSetChanged()
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): CurrencyViewHolder {
        val view = LayoutInflater.from(context).inflate(R.layout.currency_rv_item, parent, false)
        return CurrencyViewHolder(view)
    }

    @SuppressLint("SetTextI18n")
    override fun onBindViewHolder(holder: CurrencyViewHolder, position: Int) {
        val currentItem = currencyModals[position]
        holder.nameTV.text = currentItem.name
        holder.rateTV.text = "$ " + df2.format(currentItem.price)
        holder.symbolTV.text = currentItem.symbol

    }

    override fun getItemCount(): Int {
        return currencyModals.size
    }

    inner class CurrencyViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView),
    View.OnClickListener{
         val symbolTV: TextView
         val rateTV: TextView
         val nameTV: TextView

        init {
            symbolTV = itemView.findViewById(R.id.idTVSymbol)
            rateTV = itemView.findViewById(R.id.idTVRate)
            nameTV = itemView.findViewById(R.id.idTVName)

            itemView.setOnClickListener(this)
        }

        override fun onClick(v: View?) {

            val position : Int = adapterPosition
            val value : String = currencyModals[position].name
            if (position != RecyclerView.NO_POSITION){
                listener.onItemClick(position,value)
            }
        }
    }
    interface OnItemClickListener{
        fun onItemClick(position: Int, value:String)
    }
    companion object {
        private val df2 = DecimalFormat("#.##")
    }
}