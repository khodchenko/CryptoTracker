package com.example.cryptotraker

import android.annotation.SuppressLint
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.RecyclerView
import android.widget.EditText
import android.widget.ProgressBar
import android.os.Bundle
import androidx.recyclerview.widget.LinearLayoutManager
import android.text.TextWatcher
import android.text.Editable
import android.util.Log
import android.view.View
import android.widget.Toast
import com.android.volley.Response
import com.android.volley.toolbox.Volley
import com.android.volley.toolbox.JsonObjectRequest
import org.json.JSONException
import java.util.ArrayList
import java.util.HashMap



class MainActivity : AppCompatActivity(), CurrencyRVAdapter.OnItemClickListener{
    private val TAG = "MainActivity"
    private val API_KEY = "f0ca2743-1300-469b-892d-57bc85a15773"
    // creating variable for recycler view,
    // adapter, array list, progress bar
    private var currencyRV: RecyclerView? = null
    private var searchEdt: EditText? = null
    private lateinit var currencyModalArrayList: ArrayList<CurrencyModal>
    private var currencyRVAdapter: CurrencyRVAdapter? = null
    private var loadingPB: ProgressBar? = null
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        searchEdt = findViewById(R.id.idEdtCurrency)

        // initializing all our variables and array list.
        loadingPB = findViewById(R.id.idPBLoading)
        currencyRV = findViewById(R.id.idRVcurrency)
        currencyModalArrayList = ArrayList()

        // initializing our adapter class.
        currencyRVAdapter = CurrencyRVAdapter(currencyModalArrayList, this, this)

        // setting layout manager to recycler view.
        currencyRV!!.setLayoutManager(LinearLayoutManager(this))

        // setting adapter to recycler view.
        currencyRV!!.setAdapter(currencyRVAdapter)
        currencyRVAdapter
        // calling get data method to get data from API.
        data

        // on below line we are adding text watcher for our
        // edit text to check the data entered in edittext.
        searchEdt!!.addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(s: CharSequence, start: Int, count: Int, after: Int) {}
            override fun onTextChanged(s: CharSequence, start: Int, before: Int, count: Int) {}
            override fun afterTextChanged(s: Editable) {
                // on below line calling a 
                // method to filter our array list
                filter(s.toString())
            }
        })
    }

    private fun filter(filter: String) {
        // on below line we are creating a new array list
        // for storing our filtered data. 
        val filteredlist = ArrayList<CurrencyModal>()
        // running a for loop to search the data from our array list. 
        for (item in currencyModalArrayList!!) {
            // on below line we are getting the item which are 
            // filtered and adding it to filtered list.
            if (item.name.toLowerCase().contains(filter.toLowerCase())) {
                filteredlist.add(item)
            }
        }
        // on below line we are checking 
        // weather the list is empty or not. 
        if (filteredlist.isEmpty()) {
            // if list is empty we are displaying a toast message. 
            Toast.makeText(this, "No currency found..", Toast.LENGTH_SHORT).show()
        } else {
            // on below line we are calling a filter
            // list method to filter our list. 
            currencyRVAdapter!!.filterList(filteredlist)
        }
    }// in this method passing headers as

    // key along with value as API keys.
    // at last returning headers
    // calling a method to add our 
    // json object request to our queue.
// displaying error response when received any error.// handling json exception.// adding all data to our array list.
    // notifying adapter on data change.
// extracting data from json.// inside on response method extracting data
    // from response and passing it to array list
    // on below line we are making our progress
    // bar visibility to gone.
    // creating a variable for storing our string.
    val data: Unit
        // creating a variable for request queue.
        // making a json object request to fetch data from API.
        get() {
            // creating a variable for storing our string.
            val url = "https://pro-api.coinmarketcap.com/v1/cryptocurrency/listings/latest"
            // creating a variable for request queue.
            val queue = Volley.newRequestQueue(this)
            // making a json object request to fetch data from API.
            val jsonObjectRequest: JsonObjectRequest =
                @SuppressLint("NotifyDataSetChanged")
                object : JsonObjectRequest(Method.GET, url, null, Response.Listener { response ->
                    // inside on response method extracting data
                    // from response and passing it to array list
                    // on below line we are making our progress
                    // bar visibility to gone.
                    loadingPB!!.visibility = View.GONE
                    try {
                        // extracting data from json.
                        val dataArray = response.getJSONArray("data")
                        for (i in 0 until dataArray.length()) {
                            val dataObj = dataArray.getJSONObject(i)
                            val symbol = dataObj.getString("symbol")
                            val name = dataObj.getString("name")
                            val quote = dataObj.getJSONObject("quote")
                            val USD = quote.getJSONObject("USD")
                            val price = USD.getDouble("price")
                            // adding all data to our array list.
                            currencyModalArrayList!!.add(CurrencyModal(name, symbol, price))
                        }
                        // notifying adapter on data change.
                        currencyRVAdapter!!.notifyDataSetChanged()
                    } catch (e: JSONException) {
                        // handling json exception.
                        e.printStackTrace()
                        Toast.makeText(
                            this@MainActivity,
                            "Something went amiss. Please try again later",
                            Toast.LENGTH_SHORT
                        ).show()
                    }
                }, Response.ErrorListener { // displaying error response when received any error.
                    Toast.makeText(
                        this@MainActivity,
                        "Something went amiss. Please try again later",
                        Toast.LENGTH_SHORT
                    ).show()
                }) {
                    override fun getHeaders(): Map<String, String> {
                        // in this method passing headers as
                        // key along with value as API keys.
                        val headers = HashMap<String, String>()
                        headers["X-CMC_PRO_API_KEY"] = API_KEY
                        // at last returning headers
                        return headers
                    }
                }
            // calling a method to add our 
            // json object request to our queue.
            queue.add(jsonObjectRequest)
        }

    override fun onItemClick(position: Int, value: String) {
        Log.i(TAG, "onItemClick: ${"$position $value"}")
        val bundle = Bundle()
        bundle.putString("message", value)
        val fragInfo = CryptoPage()
        fragInfo.arguments = bundle
        supportFragmentManager.beginTransaction().replace(R.id.main_activity_layout, fragInfo).commit()
    }
}