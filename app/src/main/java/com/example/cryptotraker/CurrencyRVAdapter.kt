package com.example.cryptotraker

import android.content.Context
import androidx.recyclerview.widget.RecyclerView
import com.example.cryptotraker.CurrencyRVAdapter.CurrencyViewholder
import android.view.ViewGroup
import android.view.LayoutInflater
import android.view.View
import android.widget.TextView
import java.text.DecimalFormat
import java.util.ArrayList

// on below line we are creating our adapter class
// in this class we are passing our array list
// and our View Holder class which we have created.
class CurrencyRVAdapter(
    private var currencyModals: ArrayList<CurrencyModal>,
    private val context: Context,
    private val listener : OnItemClickListener
) : RecyclerView.Adapter<CurrencyViewholder>() {
    // below is the method to filter our list.
    fun filterList(filterllist: ArrayList<CurrencyModal>) {
        // adding filtered list to our
        // array list and notifying data set changed
        currencyModals = filterllist
        notifyDataSetChanged()
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): CurrencyViewholder {
        // this method is use to inflate the layout file
        // which we have created for our recycler view.
        // on below line we are inflating our layout file.
        val view = LayoutInflater.from(context).inflate(R.layout.currency_rv_item, parent, false)
        return CurrencyViewholder(view)
    }

    override fun onBindViewHolder(holder: CurrencyViewholder, position: Int) {
        // on below line we are setting data to our item of
        // recycler view and all its views.
        val currentItem = currencyModals[position]
        holder.nameTV.text = currentItem.name
        holder.rateTV.text = "$ " + df2.format(currentItem.price)
        holder.symbolTV.text = currentItem.symbol

    }

    override fun getItemCount(): Int {
        // on below line we are returning
        // the size of our array list.
        return currencyModals.size
    }

    // on below line we are creating our view holder class
    // which will be used to initialize each view of our layout file.
    inner class CurrencyViewholder(itemView: View) : RecyclerView.ViewHolder(itemView),
    View.OnClickListener{
         val symbolTV: TextView
         val rateTV: TextView
         val nameTV: TextView

        init {
            // on below line we are initializing all
            // our text views along with  its ids.
            symbolTV = itemView.findViewById(R.id.idTVSymbol)
            rateTV = itemView.findViewById(R.id.idTVRate)
            nameTV = itemView.findViewById(R.id.idTVName)

            itemView.setOnClickListener(this)
        }

        override fun onClick(v: View?) {

            val position : Int = adapterPosition
            val value : String = currencyModals[position].symbol
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