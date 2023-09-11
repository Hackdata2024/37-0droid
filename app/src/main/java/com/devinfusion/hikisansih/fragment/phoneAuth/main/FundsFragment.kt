package com.devinfusion.hikisansih.fragment.phoneAuth.main

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.webkit.WebSettings
import android.webkit.WebView
import android.webkit.WebViewClient
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import com.devinfusion.hikisansih.R
import com.devinfusion.hikisansih.adapter.CropAdapter
import com.devinfusion.hikisansih.databinding.FragmentFundBinding
import com.devinfusion.hikisansih.model.CropDetails
import com.devinfusion.hikisansih.model.WeatherRvModel
import com.devinfusion.hikisansih.model.location
import io.paperdb.Paper


class FundsFragment : Fragment() {

    private var _binding : FragmentFundBinding? = null
    private val binding get() = _binding!!

    private val cropRecommendations = mapOf(
        "Cold" to listOf("Wheat", "Barley", "Potatoes", "Carrots", "Cabbage"),
        "Moderate" to listOf("Corn", "Soybeans", "Sunflowers", "Tomatoes", "Peppers"),
        "Warm" to listOf("Rice", "Cotton", "Sugarcane", "Maize", "Peanuts")
    )

    // Define crop details
    private val cropDetails = mapOf(
        "Wheat" to CropDetails("wheat","Cold", 120, 100, "Recommended", "Pre-soak in water for 24 hours",R.drawable.wheat),
        "Barley" to CropDetails("Barley","Cold", 90, 80, "Recommended", "None", R.drawable.barley),
        "Potatoes" to CropDetails("Potatoes","Cold", 150, 3000, "Not required", "Cut into seed pieces",R.drawable.potato),
        "Corn" to CropDetails("Corn","Moderate", 90, 20, "Recommended", "None",R.drawable.corn),
        "Soybeans" to CropDetails("Soybeans","Moderate", 120, 80, "Not required", "None",R.drawable.soyabean),
        "Sunflowers" to CropDetails("Sunflowers","Moderate", 100, 4, "Recommended", "None",R.drawable.sunflower),
        "Rice" to CropDetails("Rice","Warm", 150, 50, "Recommended", "Pre-soak in water for 24 hours",R.drawable.rice),
        "Cotton" to CropDetails("Cotton","Warm", 180, 10, "Not required", "None",R.drawable.cotton),
        "Sugarcane" to CropDetails("Sugarcane","Warm", 300, 20, "Not required", "None",R.drawable.sugarcane)
        // Add more crops and details as needed
    )

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        _binding = FragmentFundBinding.inflate(layoutInflater,container,false)
        Paper.init(requireContext())

        val loc : location? = Paper.book().read<location>("location")
        val weatherModel : WeatherRvModel? = Paper.book().read<WeatherRvModel>("weather")


        val webSettings: WebSettings = binding.webView.settings
        webSettings.javaScriptEnabled = true

        binding.webView.webViewClient = object : WebViewClient() {
            override fun onPageFinished(view: WebView?, url: String?) {
                super.onPageFinished(view, url)

                if (loc != null && loc.lat != null && loc.long != null) {
                    binding.webView.post {
                        binding.webView.evaluateJavascript(
                            "updateMapWithLocation(${loc.lat}, ${loc.long} , '${loc.city}');",
                            null
                        )
                    }
                }
            }
        }

        binding.webView.loadUrl("file:///android_asset/map.html")

        binding.cropRV.layoutManager = LinearLayoutManager(requireContext(),LinearLayoutManager.HORIZONTAL,false)
        val recommendedCrops = recommendCrops(weatherModel!!.temp)
        val cropDetailsList = mutableListOf<CropDetails>()
        for (cropName in recommendedCrops) {
            val cropDetails = cropDetails[cropName]
            if (cropDetails != null) {
                cropDetailsList.add(cropDetails)
            }
        }

        val adapter = CropAdapter(cropDetailsList)
        binding.cropRV.adapter = adapter
        adapter.notifyDataSetChanged()


        return binding.root
    }

    private fun recommendCrops(temperature: Int): List<String> {
        return when {
            temperature >= 0 && temperature <= 10 -> cropRecommendations["Cold"] ?: emptyList()
            temperature > 10 && temperature <= 25 -> cropRecommendations["Moderate"] ?: emptyList()
            temperature > 25 && temperature <= 40 -> cropRecommendations["Warm"] ?: emptyList()
            else -> emptyList()
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }

}