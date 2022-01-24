package com.example.cryptotraker

import android.annotation.SuppressLint
import android.content.Context
import android.content.res.Configuration
import android.content.res.Resources
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import androidx.recyclerview.widget.LinearLayoutManager
import android.text.TextWatcher
import android.text.Editable
import android.util.Log
import android.view.View
import android.widget.Toast
import androidx.appcompat.app.ActionBarDrawerToggle
import androidx.appcompat.app.AppCompatDelegate
import androidx.core.view.GravityCompat
import androidx.drawerlayout.widget.DrawerLayout
import com.android.volley.Response
import com.android.volley.toolbox.Volley
import com.android.volley.toolbox.JsonObjectRequest
import com.example.cryptotraker.databinding.ActivityMainBinding
import com.google.android.material.navigation.NavigationView
import com.google.firebase.remoteconfig.FirebaseRemoteConfig
import org.json.JSONException
import kotlin.collections.ArrayList
import androidx.core.content.ContextCompat
import java.util.*
import kotlin.collections.HashMap
import android.os.Build
import com.google.firebase.ktx.Firebase
import com.google.firebase.remoteconfig.ktx.remoteConfig
import com.google.firebase.remoteconfig.ktx.remoteConfigSettings
import androidx.core.content.PackageManagerCompat.LOG_TAG



class MainActivity : AppCompatActivity(), CurrencyRVAdapter.OnItemClickListener {

    private val TAG = "MainActivity"
    private val API_KEY = "f0ca2743-1300-469b-892d-57bc85a15773"
    private lateinit var currencyModalArrayList: ArrayList<CurrencyModal>
    private lateinit var currencyRVAdapter: CurrencyRVAdapter
    private lateinit var binding: ActivityMainBinding
    private lateinit var remoteConfig: FirebaseRemoteConfig
    private lateinit var actionBarToggle: ActionBarDrawerToggle
    private lateinit var drawerLayout: DrawerLayout
    private lateinit var navView: NavigationView


    @SuppressLint("RestrictedApi")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        val view = binding.root
        setContentView(view)

        drawerLayout = binding.drawerLayout
        navView = binding.navView
        actionBarToggle = ActionBarDrawerToggle(this, drawerLayout, 0, 0)
        binding.drawerLayout.addDrawerListener(actionBarToggle)
        supportActionBar?.setDisplayHomeAsUpEnabled(true)
        actionBarToggle.syncState()
        val menu = navView.menu

        val sharedPreferencesTheme = getSharedPreferences("sharedPrefs", MODE_PRIVATE)
        val editorTheme = sharedPreferencesTheme.edit()

        val sharedPreferencesLocalization = getSharedPreferences("sharedPrefs", MODE_PRIVATE)
        val editorLocalization = sharedPreferencesLocalization.edit()


        val isEnglish = sharedPreferencesLocalization.getBoolean("isEnglish", true)
        if (isEnglish){
            updateResources(this,"en")
            menu.findItem(R.id.language_item).title = "РУССКИЙ"
        }else{
            updateResources(this,"ru")
            menu.findItem(R.id.language_item).title = "ENGLISH"
        }

        val isDarkModeOn = sharedPreferencesTheme.getBoolean("isDarkModeOn", false)
        if (isDarkModeOn) {
            AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_YES)
            menu.findItem(R.id.night_mode_item).title = getString(R.string.disable_dark_mode)
            menu.findItem(R.id.night_mode_item).icon = (ContextCompat.getDrawable(this, R.drawable.ic_day))
        } else {
            AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_NO)
            menu.findItem(R.id.night_mode_item).title = getString(R.string.enable_dark_mode)
            menu.findItem(R.id.night_mode_item).icon = (ContextCompat.getDrawable(this, R.drawable.ic_night))
        }

        navView.setNavigationItemSelectedListener { menuItem ->
            when (menuItem.itemId) {
                R.id.night_mode_item -> {

                    Log.i(TAG, "onCreate: Change Night mode")

                    if (isDarkModeOn) {
                        AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_NO)
                        editorTheme.putBoolean("isDarkModeOn", false)
                        editorTheme.apply()
                        menu.findItem(R.id.night_mode_item).title = getString(R.string.enable_dark_mode)
                        menu.findItem(R.id.night_mode_item).icon = (ContextCompat.getDrawable(this, R.drawable.ic_night))
                        recreate()
                        true
                    } else {
                        AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_YES)
                        editorTheme.putBoolean("isDarkModeOn", true)
                        editorTheme.apply()
                        menu.findItem(R.id.night_mode_item).title = getString(R.string.disable_dark_mode)
                        menu.findItem(R.id.night_mode_item).icon = (ContextCompat.getDrawable(this, R.drawable.ic_day))
                        recreate()
                        true
                    }
                }

                R.id.language_item -> {

                    if(isEnglish){
                        editorLocalization.putBoolean("isEnglish", false)
                        editorLocalization.apply()
                        updateResources(this,"ru")
                        menu.findItem(R.id.language_item).title = "РУССКИЙ"
                        recreate()
                        true
                    } else {
                        editorLocalization.putBoolean("isEnglish", true)
                        editorLocalization.apply()
                        updateResources(this,"en")
                        menu.findItem(R.id.language_item).title = "ENGLISH"
                        recreate()
                        true
                    }
                }
                else -> {
                    false
                }
            }
        }


        currencyModalArrayList = ArrayList()
        binding.idPBLoading.visibility = View.VISIBLE

        currencyRVAdapter = CurrencyRVAdapter(currencyModalArrayList, this, this)


        binding.idRVcurrency.layoutManager = LinearLayoutManager(this)

        binding.idRVcurrency.adapter = currencyRVAdapter
        currencyRVAdapter


        binding.idEdtCurrency.addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(s: CharSequence, start: Int, count: Int, after: Int) {}
            override fun onTextChanged(s: CharSequence, start: Int, before: Int, count: Int) {}
            override fun afterTextChanged(s: Editable) {
                filter(s.toString())
            }
        })

        remoteConfig = Firebase.remoteConfig
        val configSettings = remoteConfigSettings {
            minimumFetchIntervalInSeconds = 10
        }
        remoteConfig.setConfigSettingsAsync(configSettings)
        //remoteConfig.setDefaultsAsync(R.xml.remote_config_params)
        remoteConfig.fetch()
            .addOnSuccessListener {
                Log.d(LOG_TAG, "Remote Config fetch successful")
                remoteConfig.fetchAndActivate()
                val list = remoteConfig.getString("CRYPTO_LIST").split(",").toTypedArray()
                getFirebaseList(list)
            }
            .addOnFailureListener { e ->
                Log.e(LOG_TAG, "Remote Config fetch failed: " + e.message)

                data //loading basic list on failure
            }

    }

    override fun onItemClick(position: Int, value: String) {
        Log.i(TAG, "onItemClick: ${"$position $value"}")
        val bundle = Bundle()
        bundle.putString("message", value)
        val fragInfo = CryptoPage()
        fragInfo.arguments = bundle
        supportFragmentManager.beginTransaction().replace(R.id.drawer_layout, fragInfo, "fragment")
            .addToBackStack("fragment")
            .commit()
    }

    private fun filter(filter: String) {
        val filteredList = ArrayList<CurrencyModal>()

        for (item in currencyModalArrayList) {

            if (item.name.lowercase(Locale.getDefault())
                    .contains(filter.lowercase(Locale.getDefault()))
            ) {
                filteredList.add(item)
            }
        }
        if (filteredList.isEmpty()) {
           // toastMakeText(getString(R.string.no_currency_found))
        } else {
            currencyRVAdapter.filterList(filteredList)
        }
    }

    val data: Unit
        get() {

            val url =
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
                      //  toastMakeText(getString(R.string.error_loading_message))
                    }
                }, Response.ErrorListener {
                    toastMakeText(getString(R.string.error_loading_message))
                }) {
                    override fun getHeaders(): Map<String, String> {
                        val headers = HashMap<String, String>()
                        headers["X-CMC_PRO_API_KEY"] = API_KEY
                        return headers
                    }
                }
            queue.add(jsonObjectRequest)
        }

    private fun toastMakeText(text: String) {
        Toast.makeText(
            this@MainActivity,
            text,
            Toast.LENGTH_SHORT
        ).show()

    }

    private fun getFirebaseList(list: Array<String>) {
        for (i in list) {
            val url =
                "https://pro-api.coinmarketcap.com/v1/tools/price-conversion?amount=1&symbol=$i"
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
                       toastMakeText(getString(R.string.error_loading_message))
                    }
                }, Response.ErrorListener {
                    Log.e(TAG, "getFirebaseList: error occurred", )
                    //toastMakeText(getString(R.string.error_loading_message))
                    //todo fix error message
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

    override fun onSupportNavigateUp(): Boolean {
        drawerLayout.openDrawer(navView)
        return true
    }

    override fun onBackPressed() {
        if (this.drawerLayout.isDrawerOpen(GravityCompat.START)) {
            this.drawerLayout.closeDrawer(GravityCompat.START)
        } else {
            super.onBackPressed()
        }
    }

    private fun updateResources(context: Context, language: String) {
        val locale = Locale(language)
        Locale.setDefault(locale)
        val resources: Resources = context.resources
        val configuration: Configuration = resources.configuration

        //api level 17 android 4.2
        configuration.locale = locale
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN_MR1) {
            configuration.setLayoutDirection(locale)
        }
        resources.updateConfiguration(configuration, resources.displayMetrics)
    }


}