package com.example.cryptotraker

import android.annotation.SuppressLint
import androidx.appcompat.app.AppCompatActivity
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
import com.example.cryptotraker.databinding.ActivityMainBinding
import com.google.firebase.ktx.Firebase
import com.google.firebase.remoteconfig.FirebaseRemoteConfig
import com.google.firebase.remoteconfig.ktx.get
import com.google.firebase.remoteconfig.ktx.remoteConfig
import com.google.firebase.remoteconfig.ktx.remoteConfigSettings
import org.json.JSONArray
import org.json.JSONException
import org.json.JSONObject
import java.util.*
import kotlin.collections.ArrayList
import androidx.annotation.NonNull

import com.google.android.gms.tasks.OnCompleteListener





class MainActivity : AppCompatActivity(), CurrencyRVAdapter.OnItemClickListener {
    private val TAG = "MainActivity"
    private val API_KEY = "f0ca2743-1300-469b-892d-57bc85a15773"
    private lateinit var currencyModalArrayList: ArrayList<CurrencyModal>
    private lateinit var currencyRVAdapter: CurrencyRVAdapter
    private lateinit var binding: ActivityMainBinding
    private lateinit var remoteConfig: FirebaseRemoteConfig
    private lateinit var firebaseList : ArrayList<String>
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        val view = binding.root
        setContentView(view)
        remoteSetup()
        currencyModalArrayList = ArrayList()
        binding.idPBLoading.visibility = View.VISIBLE

        currencyRVAdapter = CurrencyRVAdapter(currencyModalArrayList, this, this)


        binding.idRVcurrency.layoutManager = LinearLayoutManager(this)

        binding.idRVcurrency.adapter = currencyRVAdapter
        currencyRVAdapter

       // data

        binding.idEdtCurrency.addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(s: CharSequence, start: Int, count: Int, after: Int) {}
            override fun onTextChanged(s: CharSequence, start: Int, before: Int, count: Int) {}
            override fun afterTextChanged(s: Editable) {
                filter(s.toString())
            }
        })
        var list = arrayListOf<String>("BTC","ETH")
//        remoteConfig = Firebase.remoteConfig
//        val configSettings = remoteConfigSettings {
//            minimumFetchIntervalInSeconds = 3600
//        }
//        remoteConfig.setConfigSettingsAsync(configSettings)
//        remoteConfig.fetchAndActivate()
//            .addOnCompleteListener(this, OnCompleteListener<Boolean?> { task ->
//                if (task.isSuccessful) {
//                    firebaseList = remoteConfig.getString("CRYPTO_LIST").split(",") as ArrayList<String>
//                    val updated = task.result
//                    Log.d(TAG, "Config params updated: $updated")
//                    Toast.makeText(
//                        this@MainActivity, "Fetch and activate succeeded",
//                        Toast.LENGTH_SHORT
//                    ).show()
//                } else {
//                    Toast.makeText(
//                        this@MainActivity, "Fetch failed",
//                        Toast.LENGTH_SHORT
//                    ).show()
//                }
//
//            })


        getFirebaseList(list)
    }


    private fun filter(filter: String) {
        val filteredlist = ArrayList<CurrencyModal>()

        for (item in currencyModalArrayList) {


            if (item.name.lowercase(Locale.getDefault()).contains(filter.lowercase(Locale.getDefault()))) {
                filteredlist.add(item)
            }
        }
        if (filteredlist.isEmpty()) {
            Toast.makeText(this, "No currency found..", Toast.LENGTH_SHORT).show()
        } else {
            currencyRVAdapter.filterList(filteredlist)
        }
    }

    val data: Unit
        get() {

            var url =
                "https://pro-api.coinmarketcap.com/v1/cryptocurrency/listings/latest?limit=100"

            val queue = Volley.newRequestQueue(this)

            val jsonObjectRequest: JsonObjectRequest =
                @SuppressLint("NotifyDataSetChanged")
                object : JsonObjectRequest(Method.GET, url, null, Response.Listener { response ->

                    binding.idPBLoading.visibility = View.GONE
                    try {

                        val dataArray = response.getJSONArray("data")

                        for (i in 0 until dataArray.length()) {
                            val dataObj = dataArray.getJSONObject(i)
                            val symbol = dataObj.getString("symbol")
                            val name = dataObj.getString("name")
                            val quote = dataObj.getJSONObject("quote")
                            val USD = quote.getJSONObject("USD")
                            val price = USD.getDouble("price")

                            currencyModalArrayList.add(CurrencyModal(name, symbol, price))
                            Log.i(TAG, "currencyModalArrayList: add $name $symbol $price")
                        }
                        currencyRVAdapter.notifyDataSetChanged()
                    } catch (e: JSONException) {
                        e.printStackTrace()
                        Toast.makeText(
                            this@MainActivity,
                            "Something went amiss. Please try again later",
                            Toast.LENGTH_SHORT
                        ).show()
                    }
                }, Response.ErrorListener {
                    Toast.makeText(
                        this@MainActivity,
                        "Something went amiss. Please try again later",
                        Toast.LENGTH_SHORT
                    ).show()
                }) {
                    override fun getHeaders(): Map<String, String> {
                        val headers = HashMap<String, String>()
                        headers["X-CMC_PRO_API_KEY"] = API_KEY
                        return headers
                    }
                }
            queue.add(jsonObjectRequest)
        }

    override fun onItemClick(position: Int, value: String) {
        Log.i(TAG, "onItemClick: ${"$position $value"}")
        val bundle = Bundle()
        bundle.putString("message", value)
        val fragInfo = CryptoPage()
        fragInfo.arguments = bundle
        supportFragmentManager.beginTransaction().replace(R.id.main_activity_layout, fragInfo)
            .commit()
    }

    private fun remoteSetup() {


    }

    fun getFirebaseList(list: ArrayList<String>) {
        for (i in list) {
            var url = "https://pro-api.coinmarketcap.com/v1/tools/price-conversion?amount=1&symbol=$i"
            val queue = Volley.newRequestQueue(this)
            val jsonObjectRequest: JsonObjectRequest =
                @SuppressLint("NotifyDataSetChanged")
                object : JsonObjectRequest(Method.GET, url, null, Response.Listener { response ->

                    binding.idPBLoading.visibility = View.GONE
                    try {

                        val dataArray = response.getJSONObject("data")
                        val symbol = dataArray.getString("symbol")
                        val name = dataArray.getString("name")
                        val quote = dataArray.getJSONObject("quote")
                        val USD = quote.getJSONObject("USD")
                        val price = USD.getDouble("price")

                        currencyModalArrayList.add(CurrencyModal(name, symbol, price))
                        Log.i(TAG, "currencyModalArrayList: add $name $symbol $price")

                        currencyRVAdapter.notifyDataSetChanged()
                    } catch (e: JSONException) {
                        e.printStackTrace()
                        Toast.makeText(
                            this@MainActivity,
                            "Something went amiss. Please try again later",
                            Toast.LENGTH_SHORT
                        ).show()
                    }
                }, Response.ErrorListener {
                    Toast.makeText(
                        this@MainActivity,
                        "Something went amiss. Please try again later",
                        Toast.LENGTH_SHORT
                    ).show()
                }) {
                    override fun getHeaders(): Map<String, String> {
                        val headers = HashMap<String, String>()
                        headers["X-CMC_PRO_API_KEY"] = API_KEY
                        return headers
                    }
                }
            queue.add(jsonObjectRequest)
        }
    }

}