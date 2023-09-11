package com.devinfusion.hikisansih.fragment.phoneAuth.main

import android.Manifest
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.location.Location
import android.location.LocationManager
import android.os.Bundle
import android.speech.tts.TextToSpeech
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import com.android.volley.Request
import com.android.volley.toolbox.JsonObjectRequest
import com.android.volley.toolbox.Volley
import com.devinfusion.hikisansih.R
import com.devinfusion.hikisansih.activities.PlantDetectionActivity
import com.devinfusion.hikisansih.databinding.FragmentHomeBinding
import com.devinfusion.hikisansih.model.WeatherRvModel
import com.devinfusion.hikisansih.model.location
import io.paperdb.Paper
import org.json.JSONObject
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import java.util.TimeZone

class HomeFragment : Fragment() {

    private var _binding: FragmentHomeBinding? = null
    private val binding get() = _binding!!
    private val REQUEST_LOCATION_PERMISSION = 101
    private lateinit var api_key : String
    private lateinit var textToSpeech: TextToSpeech


    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        Paper.init(requireContext())
        _binding = FragmentHomeBinding.inflate(layoutInflater, container, false)
        api_key = "d14ce72bc6f2849598263e88bf643943"

        binding.weatherView.alpha = 0.7f
        binding.loading.visibility = View.VISIBLE
        binding.mainCardView.visibility = View.GONE
        binding.loading.playAnimation()

        textToSpeech = TextToSpeech(requireContext(), TextToSpeech.OnInitListener { status ->
            if (status == TextToSpeech.SUCCESS) {
                val result = textToSpeech.setLanguage(Locale.US)

                if (result == TextToSpeech.LANG_MISSING_DATA ||
                    result == TextToSpeech.LANG_NOT_SUPPORTED
                ) {
                    // Handle language not supported error
                    Toast.makeText(requireContext(), "Language not supported", Toast.LENGTH_SHORT).show()
                }
            } else {
                // Handle TTS initialization failure
                Toast.makeText(requireContext(), "Something went wrong", Toast.LENGTH_SHORT).show()
            }
        })

        binding.litsenButton.setOnClickListener {
//            val sentenceToSpeak = binding.litsenText.text
//
//            // Speak the sentence
//            textToSpeech.speak(sentenceToSpeak, TextToSpeech.QUEUE_FLUSH, null, null)
            startActivity(Intent(requireContext(),PlantDetectionActivity::class.java))
        }

        if (checkLocationPermission()) {
            getCurrentLocation()
        } else {
            requestLocationPermission()
        }

        return binding.root
    }

    private fun checkLocationPermission(): Boolean {
        return ContextCompat.checkSelfPermission(
            requireContext(),
            Manifest.permission.ACCESS_FINE_LOCATION
        ) == PackageManager.PERMISSION_GRANTED
    }

    private fun requestLocationPermission() {
        ActivityCompat.requestPermissions(
            requireActivity(),
            arrayOf(Manifest.permission.ACCESS_FINE_LOCATION),
            REQUEST_LOCATION_PERMISSION
        )
    }

    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<String>,
        grantResults: IntArray
    ) {
        if (requestCode == REQUEST_LOCATION_PERMISSION) {
            if (grantResults.isNotEmpty() && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                getCurrentLocation()
            } else {
                requestLocationPermission()
            }
        }
    }

    private fun getCurrentLocation() {
        val locationManager = requireContext().getSystemService(Context.LOCATION_SERVICE) as LocationManager
        val networkProvider = LocationManager.NETWORK_PROVIDER

        try {
            val location: Location? = locationManager.getLastKnownLocation(networkProvider)
            if (location != null) {
                val latitude = location.latitude
                val longitude = location.longitude
                Log.d("Location", "Latitude: $latitude, Longitude: $longitude")

                Toast.makeText(requireContext(), "Latitude: $latitude, Longitude: $longitude", Toast.LENGTH_SHORT).show()
                startFillingLocation(latitude, longitude)
            } else {
                // Handle the case where the location is not available
                Toast.makeText(requireContext(), "Something went wrong", Toast.LENGTH_SHORT).show()
            }
        } catch (e: SecurityException) {
            e.printStackTrace()
        }

    }

    private fun startFillingLocation(latitude: Double, longitude: Double) {
        // Replace `YOUR_API_KEY` with your actual OpenWeatherMap API key
        val url = "https://api.openweathermap.org/data/2.5/weather?lat=$latitude&lon=$longitude&appid=$api_key"

        Log.d("'url",url)

        val requestQueue = Volley.newRequestQueue(requireContext())
        val jsonObjectRequest = JsonObjectRequest(
            Request.Method.GET, url, null,
            { response ->
                binding.loading.visibility = View.GONE
                binding.loading.cancelAnimation()
                binding.mainCardView.visibility = View.VISIBLE

                pushingValues(response,latitude,longitude)
                Log.d("API Response", response.toString())
            },
            { error ->
                Toast.makeText(
                    requireContext(),
                    "Error: ${error.message}",
                    Toast.LENGTH_SHORT
                ).show()
                Log.e("API Error", error.toString())
            }
        )
        requestQueue.add(jsonObjectRequest)
    }

    private fun pushingValues(response: JSONObject?, latitude: Double, longitude: Double) {
        val Cityname = response?.getString("name")
        val main = response?.getJSONObject("main")
        val temp = main?.getInt("temp")
        val temp_min = main?.getInt("temp_min")
        val temp_max = main?.getInt("temp_max")
        val temp_minV = temp_min as Int
        val temp_min_final = temp_minV-273
        val temp_maxV = temp_max as Int
        val temp_max_final = temp_maxV-273 + 1
        val sys = response.getJSONObject("sys")
        val sunrise = sys.getLong("sunrise")
        val sunset = sys.getLong("sunset")
        val humidity = main.getString("humidity")

        val wind = response.getJSONObject("wind")
        val windSpeed = wind.getString("speed")

        val weather = response.getJSONArray("weather").getJSONObject(0)
        val forecast = weather.getString("main")
        val forecastIcon = "http://openweathermap.org/img/wn/${weather.getString("icon")}.png"
        Log.d("forecastIcon", forecastIcon)

        val mainTemp : Int = temp!! - 273

        val date = response.getLong("dt")





        val weatherModel = WeatherRvModel(0,Cityname!!,windSpeed, humidity,mainTemp,temp_max_final.toString(),temp_min_final.toString(),
            forecastIcon,forecast, date,"current")

        val loc = location(latitude,longitude,Cityname)
        Paper.book().write("location",loc)
        Paper.book().write("weather",weatherModel)

        binddata(weatherModel)

    }

    private fun binddata(weatherModel: WeatherRvModel) {
        val final = longToDate(weatherModel.lastupdatedAt)

        binding.minMaxTemp.text = "${weatherModel.temp_max}° / ${weatherModel.temp_min}°"
        binding.location.text = weatherModel.name
        binding.windSpeed.text = weatherModel.speed
        binding.mainTemp.text = "${weatherModel.temp.toString()}°"
        binding.precipitation.text = weatherModel.humidity

        if (weatherModel.forecast == "Clouds"){
            binding.backgroundImageView.setImageResource(R.drawable.clear_background)
        }else if (weatherModel.forecast == "Rain"){
            binding.backgroundImageView.setImageResource(R.drawable.rain_background)
        }else if (weatherModel.forecast == "Thunderstorm"){
            binding.backgroundImageView.setImageResource(R.drawable.thunder_background)
        }else if (weatherModel.forecast == "Clear"){
            binding.backgroundImageView.setImageResource(R.drawable.clear_background)
        }else{
            binding.backgroundImageView.setImageResource(R.drawable.clear_background)
        }

        binding.litsenText.text = weatherModel.forecast





//        binding.minTemp.text = "min temp : ${weatherModel.temp_min.toString()}°C"
//        binding.maxTemp.text = "max temp : ${weatherModel.temp_min.toString()}°C"
    }

    private fun longToDate(time: Long): String {
        val format = "dd MMM yyyy HH:mm"
        val sdf = SimpleDateFormat(format, Locale.getDefault())
        sdf.timeZone = TimeZone.getDefault()
        return sdf.format(Date(time * 1000))
    }



    override fun onDestroyView() {
        super.onDestroyView()
        if (textToSpeech.isSpeaking) {
            textToSpeech.stop()
        }
        textToSpeech.shutdown()
        _binding = null
    }
}
