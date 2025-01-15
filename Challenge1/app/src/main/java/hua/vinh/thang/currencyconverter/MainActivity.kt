package hua.vinh.thang.currencyconverter

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.net.ConnectivityManager
import android.net.NetworkCapabilities
import android.net.Uri
import android.os.Bundle
import android.view.View
import android.widget.ArrayAdapter
import android.widget.Button
import android.widget.EditText
import android.widget.ImageView
import android.widget.ListView
import android.widget.SearchView
import android.widget.TextView
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.AppCompatButton
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.core.widget.addTextChangedListener
import com.android.volley.DefaultRetryPolicy
import com.android.volley.Request
import com.android.volley.Response
import com.android.volley.toolbox.StringRequest
import com.android.volley.toolbox.Volley
import com.google.android.material.bottomsheet.BottomSheetBehavior
import com.google.android.material.bottomsheet.BottomSheetDialog
import hua.vinh.thang.currencyconverter.model.SuportedCode
import org.json.JSONObject
import java.lang.Double
import java.text.DecimalFormat


class MainActivity : AppCompatActivity() {
    //declare api
    val ExchangeRatesAPI: String =
        "https://v6.exchangerate-api.com/v6/744179945678568f7f859b29/latest/"
    val getCurrencyCodeAPI: String =
        "https://v6.exchangerate-api.com/v6/744179945678568f7f859b29/codes"

    //declare controls
    var edtAmount: EditText? = null
    var edtConverted: EditText? = null
    var btnSwap: ImageView? = null
    var btnAmount: AppCompatButton? = null
    var btnConvertTo: AppCompatButton? = null
    var tvCompare: TextView? = null
    var tvLastUpdate: TextView? = null
    var tvNextUpdate: TextView? = null
    var searchView: SearchView? = null
    var lvCurrencyCode: ListView? = null

    //declare ArrayList, adapter, decimalFormat, default rate, network, flags
    var listCurrencyCode: ArrayList<SuportedCode>? = null
    var adapter: ArrayAdapter<SuportedCode>? = null
    val decimalFormat = DecimalFormat("#,###.########")
    var rate = 0.0
    private lateinit var networkReceiver: BroadcastReceiver
    private var isFirstCheck = true // flag check first time open app
    private var isFirstCall = true // flag check first time call getCurrencyCode()
    private var isFirstInput = true // flag check first time input


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_main)
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }

        //add controls and events
        addControls()
        addEvents()

        // Register to listen for network status changes
        networkReceiver = object : BroadcastReceiver() {
            override fun onReceive(context: Context, intent: Intent) {

                // Check if it's the first time opening the app, no need to notify about internet reconnected
                if (isFirstCheck) {

                    // If the first time has network continue check
                    // otherwise, notify about no network
                    if (isNetworkAvailable(context)) {

                        // Check if it's the first time calling the function getCurrencyCode()
                        if (isFirstCall) {
                            getCurrencyCode()
                            isFirstCall = false //set flag to false
                        }
                        getCurrencyRateAndConvert()
                        isFirstCheck = false
                    } else {
                        Toast.makeText(context, "No internet connectivity", Toast.LENGTH_SHORT)
                            .show()
                    }
                } else {
                    if (isNetworkAvailable(context)) {
                        Toast.makeText(context, "Internet reconnected!", Toast.LENGTH_SHORT).show()
                        if (isFirstCall) {
                            getCurrencyCode()
                            isFirstCall = false
                        }
                        getCurrencyRateAndConvert() // recall function when network available
                    } else {
                        Toast.makeText(context, "No internet connectivity", Toast.LENGTH_SHORT)
                            .show()
                    }
                }
            }
        }
        // Register a BroadcastReceiver to listen for events related to the device's network status
        registerReceiver(networkReceiver, IntentFilter(ConnectivityManager.CONNECTIVITY_ACTION))
    }

    private fun addControls() {
        edtAmount = findViewById(R.id.edtAmount)
        edtConverted = findViewById(R.id.edtConverted)
        btnSwap = findViewById(R.id.btnSwap)
        btnAmount = findViewById(R.id.btnAmount)
        btnConvertTo = findViewById(R.id.btnConvertTo)
        tvCompare = findViewById(R.id.tvCompare)
        tvLastUpdate = findViewById(R.id.tvLastUpdate)
        tvNextUpdate = findViewById(R.id.tvNextUpdate)
        listCurrencyCode = ArrayList<SuportedCode>()
        adapter = ArrayAdapter(this, android.R.layout.simple_list_item_1, listCurrencyCode!!)
    }

    private fun addEvents() {
        // btnAmount and btnConvertTo will open dialog to select currencies
        btnAmount?.setOnClickListener(View.OnClickListener {
            showListCurrencyCodeDialog(it)
        })
        btnConvertTo?.setOnClickListener(View.OnClickListener {
            showListCurrencyCodeDialog(it)
        })

        // btnSwap will swap the positions of the currencies
        btnSwap?.setOnClickListener(View.OnClickListener {
            swapCurrency()
        })

        //convert currency after input changed
        edtAmount?.addTextChangedListener(afterTextChanged = {
            //convert currency
            val amount = getAmount()
            val converted = convertCurrency(amount, rate)
            edtConverted?.setText(decimalFormat.format(converted).toString())
        })
    }

    private fun getCurrencyRateAndConvert() {
        //if network not available then return
        if (!isNetworkAvailable(this)) {
            Toast.makeText(this, "No internet connectivity", Toast.LENGTH_LONG).show()
            return
        }

        //if network available, get currency from btnAmount and btnConvertTo
        val currency1 = btnAmount!!.text.toString()
        val currency2 = btnConvertTo!!.text.toString()
        val requestQueue = Volley.newRequestQueue(this@MainActivity)
        val responeString = Response.Listener<String> { response ->
            try {
                //response return an object
                val jsonObject = JSONObject(response)

                //if result success then continue, else report error
                val result = jsonObject.getString("result")
                if (result == "success") {

                    //get object from conversion_rates
                    val conversion_rates = jsonObject.getJSONObject("conversion_rates")

                    //get rate of currency 2
                    rate = conversion_rates.getDouble(currency2)
                    if (isFirstInput) {
                        edtAmount?.setText("1") //init first value for amount EditText
                        isFirstInput = false
                    }

                    //convert currency
                    val amount = getAmount()
                    val converted = convertCurrency(amount, rate)
                    edtConverted?.setText(decimalFormat.format(converted).toString())

                    //string: 1 in currency 1 is how much in currency 2
                    val compareCurrency =
                        "1 $currency1 =  ${decimalFormat.format(rate)} $currency2"

                    //string last update and next update
                    val lastUpdate = "Last update: ${jsonObject.getString("time_last_update_utc")}"
                    val nextUpdate = "Next update: ${jsonObject.getString("time_next_update_utc")}"

                    //assign values into outputs
                    tvCompare?.setText(compareCurrency)
                    tvLastUpdate?.setText(lastUpdate)
                    tvNextUpdate?.setText(nextUpdate)

                } else if (result == "error") {
                    //when result response "error", display error on the screen
                    val error = jsonObject.getString("error-type")
                    Toast.makeText(this, error, Toast.LENGTH_SHORT).show()
                }
            } catch (e: Exception) {
            }
        }

        //listen error from response and report error
        val errorListener =
            Response.ErrorListener { error ->
                when (error) {
                    is com.android.volley.NoConnectionError -> {
                        Toast.makeText(
                            this@MainActivity,
                            "No internet connectivity",
                            Toast.LENGTH_LONG
                        ).show()
                    }

                    is com.android.volley.TimeoutError -> {
                        Toast.makeText(
                            this@MainActivity,
                            "Request timeout, please try again",
                            Toast.LENGTH_LONG
                        ).show()
                    }

                    else -> {
                        Toast.makeText(
                            this@MainActivity,
                            "Error: ${error.message}",
                            Toast.LENGTH_LONG
                        ).show()
                    }
                }
            }

        // call api ExchangeRatesAPI + (currency 1) using the GET method
        val builder = Uri.parse(ExchangeRatesAPI + currency1).buildUpon()
        val url = builder.build().toString()
        val request = StringRequest(Request.Method.GET, url, responeString, errorListener)
        request.setRetryPolicy(
            DefaultRetryPolicy(
                DefaultRetryPolicy.DEFAULT_TIMEOUT_MS,
                DefaultRetryPolicy.DEFAULT_MAX_RETRIES,
                DefaultRetryPolicy.DEFAULT_BACKOFF_MULT
            )
        )
        requestQueue.add(request)
    }

    private fun getAmount(): kotlin.Double{
        var amount = edtAmount?.text.toString().toDoubleOrNull() ?: 0.0
        return amount
    }
    private fun convertCurrency(amount: kotlin.Double, rate: kotlin.Double): kotlin.Double {
        //convert by formula amount * rate
        val converted = amount * rate

        return converted
    }

    //function get list currency code
    private fun getCurrencyCode() {
        if (!isNetworkAvailable(this)) {
            Toast.makeText(this, "No internet connectivity", Toast.LENGTH_LONG).show()
            return
        }
        val requestQueue = Volley.newRequestQueue(this@MainActivity)
        val responeString = Response.Listener<String> { response ->
            try {
                // get JSON from response
                val jsonObject = JSONObject(response)
                val result = jsonObject.getString("result")
                if (result == "success") {

                    //get array from supported_codes
                    val supportedCodesArray = jsonObject.getJSONArray("supported_codes")

                    // loop through element in supported_codes
                    for (i in 0 until supportedCodesArray.length()) {

                        //each element in supported_codes is sub array
                        val currencyArray = supportedCodesArray.getJSONArray(i)
                        val currencyCode = currencyArray.getString(0)  // get currency code from sub array
                        val moneyUnit = currencyArray.getString(1) // get currency name from sub array

                        //add supported code into listCurrencyCode
                        var supportedCode = SuportedCode(currencyCode, moneyUnit)
                        listCurrencyCode?.add(supportedCode)
                    }
                    adapter?.notifyDataSetChanged()
                } else if (result == "error") {

                    // handle when not receiving valid result from api
                    val error = jsonObject.getString("error-type")
                    Toast.makeText(this, error, Toast.LENGTH_SHORT).show()
                }
            } catch (e: Exception) {
            }
        }
        val errorListener =
            Response.ErrorListener { error ->
                when (error) {
                    is com.android.volley.NoConnectionError -> {
                        Toast.makeText(
                            this@MainActivity,
                            "No internet connectivity",
                            Toast.LENGTH_LONG
                        ).show()
                    }

                    is com.android.volley.TimeoutError -> {
                        Toast.makeText(
                            this@MainActivity,
                            "Request timeout, please try again",
                            Toast.LENGTH_LONG
                        ).show()
                    }

                    else -> {
                        Toast.makeText(
                            this@MainActivity,
                            "Error: ${error.message}",
                            Toast.LENGTH_LONG
                        ).show()
                    }
                }
            }
        val builder = Uri.parse(getCurrencyCodeAPI).buildUpon()
        val url = builder.build().toString()
        val request = StringRequest(Request.Method.GET, url, responeString, errorListener)
        request.setRetryPolicy(
            DefaultRetryPolicy(
                DefaultRetryPolicy.DEFAULT_TIMEOUT_MS,
                DefaultRetryPolicy.DEFAULT_MAX_RETRIES,
                DefaultRetryPolicy.DEFAULT_BACKOFF_MULT
            )
        )
        requestQueue.add(request)
    }

    private fun swapCurrency() {
        val temp = btnAmount?.text.toString()
        btnAmount?.text = btnConvertTo?.text.toString()
        btnConvertTo?.text = temp

        getCurrencyRateAndConvert()
    }

    private fun showListCurrencyCodeDialog(view: View) {
        //init dialog is a bottom sheet dialog
        val dialog = BottomSheetDialog(this)
        dialog.setContentView(R.layout.dialog_currency)
        lvCurrencyCode = dialog.findViewById(R.id.lvCurrencyCode)
        lvCurrencyCode?.adapter = adapter
        searchView = dialog.findViewById(R.id.searchView)

        // set behavior for dialog
        val bottomSheet =
            dialog.findViewById<View>(com.google.android.material.R.id.design_bottom_sheet)
        if (bottomSheet != null) {
            val behavior = BottomSheetBehavior.from(bottomSheet)
            behavior.isDraggable = false
        }

        //get value from button you clicked
        val currency = (view as Button).text

        // get position of currency in listCurrencyCode
        val currencyPosition =
            listCurrencyCode?.indexOfFirst { it.currencycode == currency }

        //set selection to it
        lvCurrencyCode?.setSelection(currencyPosition!!)

        // Listen for item selection events in the ListView
        lvCurrencyCode?.setOnItemClickListener { _, _, position, _ ->
            val selectedCurrency = adapter?.getItem(position)

            // Update the value for the button that was clicked
            if (view.id == R.id.btnAmount) {
                if (selectedCurrency?.currencycode == btnConvertTo?.text) {
                    swapCurrency()
                } else {
                    btnAmount?.text = selectedCurrency?.currencycode
                    getCurrencyRateAndConvert()
                }
            } else if (view.id == R.id.btnConvertTo) {
                if (selectedCurrency?.currencycode == btnAmount?.text) {
                    swapCurrency()
                } else {
                    btnConvertTo?.text = selectedCurrency?.currencycode
                    getCurrencyRateAndConvert()
                }
            }
            // Reset the list to its original state
            searchView?.setQuery("", false) // Clear the SearchView input
            adapter?.filter?.filter("")    // Remove filters and display the full list
            dialog.dismiss()
        }

        searchView?.setOnQueryTextListener(object : SearchView.OnQueryTextListener {
            override fun onQueryTextSubmit(query: String?): Boolean {
                return false
            }

            override fun onQueryTextChange(newText: String?): Boolean {
                // Check if the search keyword exists
                if (newText != null && newText.isNotEmpty()) {

                    // Filter the list based on the keyword
                    adapter?.filter?.filter(newText.toUpperCase())
                } else {
                    // If no keyword, display the full list again
                    adapter?.filter?.filter("")
                }
                return false
            }
        })
        dialog.show()
    }

    private fun isNetworkAvailable(context: Context): Boolean {
        val connectivityManager =
            context.getSystemService(Context.CONNECTIVITY_SERVICE) as ConnectivityManager
        val network = connectivityManager.activeNetwork
        val networkCapabilities = connectivityManager.getNetworkCapabilities(network)
        return networkCapabilities != null && networkCapabilities.hasCapability(NetworkCapabilities.NET_CAPABILITY_INTERNET)
    }

    override fun onDestroy() {
        super.onDestroy()
        unregisterReceiver(networkReceiver)
    }
}