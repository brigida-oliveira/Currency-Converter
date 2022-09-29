package com.example.currencyconverter

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.widget.*
import com.example.currencyconverter.api.EndPoint
import com.example.currencyconverter.databinding.ActivityMainBinding
import com.example.currencyconverter.util.NetworkUtils
import com.google.gson.JsonObject
import retrofit2.Call
import retrofit2.Response

class MainActivity : AppCompatActivity() {

    private lateinit var binding: ActivityMainBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        getCurrencies()

        //Desabilita o botão se o usuário não tiver digitado nada
        binding.etValueFrom.addTextChangedListener(object: TextWatcher {
            override fun onTextChanged(s:CharSequence, start:Int, before:Int, count:Int) {
                binding.btnConvert.isEnabled = s.toString().trim { it <= ' ' }.isNotEmpty()
            }
            override fun beforeTextChanged(s:CharSequence, start:Int, count:Int,
                                           after:Int) {
                // TODO Auto-generated method stub
            }
            override fun afterTextChanged(s: Editable) {
                // TODO Auto-generated method stub
            }
        })

        binding.btnConvert.setOnClickListener { convertMoney() }
    }

    private fun convertMoney() {
        val retrofitClient = NetworkUtils.getRetrofitInstance("https://cdn.jsdelivr.net/")
        val endPoint = retrofitClient.create(EndPoint::class.java)

        val fromText = binding.spFrom.selectedItem.toString().substringBefore(":")
        val toText = binding.spTo.selectedItem.toString().substringBefore(":")

        binding.textTesteTo.text = toText.uppercase()

        endPoint.getCurrencyRate(fromText.lowercase(), toText.lowercase()).enqueue(object : retrofit2.Callback<JsonObject>{
            override fun onResponse(call: Call<JsonObject>, response: Response<JsonObject>) {
                val data = response.body()?.entrySet()?.find { it.key == binding.spTo.selectedItem.toString().substringBefore(":").lowercase() }
                val rate: Double = data?.value.toString().toDouble()
                val conversion = binding.etValueFrom.text.toString().toDouble() * rate

                binding.tvResult.text = String.format("%.2f", conversion)
                val rateText = String.format("%.2f", rate)
                binding.tvRate.text = baseContext.getString(R.string.currency_to_currency, fromText, rateText, toText)
            }

            override fun onFailure(call: Call<JsonObject>, t: Throwable) {
                Toast.makeText(this@MainActivity, "Não foi possível conectar, houve uma falha", Toast.LENGTH_LONG).show()
            }

        })

    }

    private fun getCurrencies() {
        val retrofitClient = NetworkUtils.getRetrofitInstance("https://cdn.jsdelivr.net/")
        val endPoint = retrofitClient.create(EndPoint::class.java)

        endPoint.getCurrencies().enqueue(object: retrofit2.Callback<JsonObject>{
            override fun onResponse(call: Call<JsonObject>, response: Response<JsonObject>) {
                val data = mutableListOf<String>()

                response.body()?.entrySet()?.iterator()?.forEach {
                    data.add("${it.key}: ${it.value}".replace("\"", "").uppercase())
                }

                val posBRL = data.indexOf("BRL: BRAZILIAN REAL")
                val posUSD = data.indexOf("USD: UNITED STATES DOLLAR")

                val adapter = ArrayAdapter(baseContext, android.R.layout.simple_spinner_dropdown_item, data)
                binding.spFrom.adapter = adapter
                binding.spTo.adapter = adapter

                binding.spFrom.setSelection(posUSD)
                binding.spTo.setSelection(posBRL)
            }

            override fun onFailure(call: Call<JsonObject>, t: Throwable) {
                Toast.makeText(this@MainActivity, "Não foi possível conectar, houve uma falha", Toast.LENGTH_LONG).show()
            }
        })
    }
}