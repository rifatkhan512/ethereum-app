package com.example.myapplication

import android.os.Bundle
import android.util.Log
import androidx.appcompat.app.AppCompatActivity
import com.example.myapplication.databinding.ActivityMain3Binding
import okhttp3.HttpUrl
import okhttp3.HttpUrl.Companion.toHttpUrlOrNull
import okhttp3.OkHttpClient
import okhttp3.Request
import org.json.JSONObject
import java.math.BigDecimal
import kotlin.math.pow

class MainActivity3 : AppCompatActivity() {
    val APIKEY = "YMN8P3QZ9BA3RPYES9WHAT63AW3VYGIHCZ"
    val EXCHANGE_RATE= 117.39

    private lateinit var layout:ActivityMain3Binding
    data class EtherScanData(
        val ens: String,
        val ethBalance: BigDecimal,
        val usdBalance: String,
        val nonce: Long,
        val lastTxHash: String
    )
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        layout= ActivityMain3Binding.inflate(layoutInflater)
        setContentView(layout.root)
        val address= intent.getStringExtra("address")
        if (address != null) {
            Thread {
                val data = getEtherScanData(address, APIKEY)
                runOnUiThread {
                    layout.name.text = "Public name Tag: " + data.ens.toString()
                    layout.balance.text = "Balance (in ETH): " + data.ethBalance.toString()
                    layout.usd.text =  String.format("%.2f",data.usdBalance.toFloat())
                    layout.nonce.text = "Nonce: " + data.nonce.toString()
                    layout.lastTxHash.text = "Last Transaction Hash: " + data.lastTxHash.toString()


                }
            }.start()

        }
        layout.changeButton.setOnClickListener {
            if (layout.currency.text == "USD"){
                layout.usd.text= String.format("%.2f",layout.usd.text.toString().toFloat()*EXCHANGE_RATE)
                layout.currency.text= "BDT"
                layout.changeButton.text="Tap to change currency to USD"

            }
            else {
                layout.usd.text= String.format("%.2f",layout.usd.text.toString().toFloat()/EXCHANGE_RATE)
                layout.currency.text= "USD"
                layout.changeButton.text="Tap to change currency to BDT"
            }
        }
    }

    private fun buildUrl(
        module: String,
        action: String,
        address: String,
        apiKey: String
    ): String {
        val baseUrl = "https://api.etherscan.io/api"
        val urlBuilder = baseUrl.toHttpUrlOrNull()!!.newBuilder()
            .addQueryParameter("module", module)
            .addQueryParameter("action", action)
            .addQueryParameter("address", address)
            .addQueryParameter("tag", "latest") // Optional, defaults to "latest"
            .addQueryParameter("apikey", apiKey)
        return urlBuilder.build().toString()
    }

    private fun fetchDataFromEtherscan(url: String): String {
        val client = OkHttpClient()
        val request = Request.Builder()
            .url(url)
            .build()

        val response = client.newCall(request).execute()
        //Log.d("response", response.body?.toString()?:"No response from server")
        return response.body?.string() ?: ""
    }

    private fun getEtherScanData(address: String, apiKey: String): EtherScanData {
        val ensUrl = buildUrl(module = "contract", action = "getsourcecode", address = address, apiKey = apiKey)
        val ethBalanceUrl = buildUrl(module = "account", action = "balance", address = address, apiKey = apiKey)
        val nonceUrl = buildUrl(module = "proxy", action = "eth_getTransactionCount", address = address, apiKey = apiKey)

        val ensResponse = fetchDataFromEtherscan(ensUrl)
        val ethBalanceResponse = fetchDataFromEtherscan(ethBalanceUrl)
        val nonceResponse = fetchDataFromEtherscan(nonceUrl)

        val ens =
            (try {

            val name= JSONObject(ensResponse).getJSONObject("result").getString("ContractName")
            if (name!=""){->name} else "Not Found"
        } catch (e: Exception) {
            Log.e("error", "error getting name")
            "Not Found"
        }).toString()



        val ethBalance = try {
            //Log.d("json",JSONObject(ethBalanceResponse).toString())
            val weiBalance = JSONObject(ethBalanceResponse).getString("result").toBigDecimal()
            weiBalance.divide(BigDecimal.valueOf((10.0).pow(18))) // Convert wei to ETH
        } catch (e: Exception) {
            Log.e("error", "error getting balance")
            BigDecimal.ZERO
        }

        val nonce =
        try {
            Log.d("json",JSONObject(nonceResponse).toString())
            JSONObject(nonceResponse).getString("result").drop(2).toLong(16)
        } catch (e: Exception) {
            0L
        }

        // Implement logic to fetch USD balance using CoinGecko API or similar service
        val usdBalance = ((ethBalance.toFloat())* 3365.40).toString().format("%2f")


        // Implement logic to fetch last transaction hash (might require additional Etherscan call)
        val lastTxHash = ""

        return EtherScanData(ens, ethBalance, usdBalance, nonce, lastTxHash)
    }
}