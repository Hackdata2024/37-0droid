package com.devinfusion.hikisansih.fragment.phoneAuth.main

import NewsAdapter
import android.Manifest
import android.app.AlertDialog
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.location.Location
import android.location.LocationManager
import android.os.Bundle
import android.speech.RecognitionListener
import android.speech.RecognizerIntent
import android.speech.SpeechRecognizer
import android.speech.tts.TextToSpeech
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import com.airbnb.lottie.LottieAnimationView
import com.android.volley.Request
import com.android.volley.toolbox.JsonObjectRequest
import com.android.volley.toolbox.Volley
import com.devinfusion.hikisansih.R
import com.devinfusion.hikisansih.activities.DiseaseDetectionActivity
import com.devinfusion.hikisansih.activities.PlantDetectionActivity
import com.devinfusion.hikisansih.databinding.FragmentHomeBinding
import com.devinfusion.hikisansih.model.News
import com.devinfusion.hikisansih.model.WeatherRvModel
import com.devinfusion.hikisansih.model.location
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import com.google.android.material.dialog.MaterialDialogs
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener
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
    private val REQUEST_MICROPHONE_PERMISSION = 201
    private lateinit var api_key : String
    private lateinit var textToSpeech: TextToSpeech
    private lateinit var newsAdapter: NewsAdapter
    private val newsList = ArrayList<News>()
    private var weatherModel : WeatherRvModel? = null
    private var listeningDialog: AlertDialog? = null


    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        Paper.init(requireContext())
        _binding = FragmentHomeBinding.inflate(layoutInflater, container, false)
        api_key = "d14ce72bc6f2849598263e88bf643943"

        if (checkLocationPermission()) {
            getCurrentLocation()
        } else {
            requestLocationPermission()
        }

        textToSpeech = TextToSpeech(context, TextToSpeech.OnInitListener { status ->
            if (status == TextToSpeech.SUCCESS) {
                val result = textToSpeech.setLanguage(Locale("en", "IN"))

                if (result == TextToSpeech.LANG_MISSING_DATA ||
                    result == TextToSpeech.LANG_NOT_SUPPORTED
                ) {
                    Toast.makeText(context, "Indian English not supported", Toast.LENGTH_SHORT).show()
                }
            } else {
                Toast.makeText(context, "Something went wrong", Toast.LENGTH_SHORT).show()
            }
        })


        binding.litsenButton.setOnClickListener {
            if (ContextCompat.checkSelfPermission(
                    requireContext(),
                    Manifest.permission.RECORD_AUDIO
                ) == PackageManager.PERMISSION_GRANTED
            ) {
                // Show the listening dialog
                showListeningDialog()
                startSpeechRecognition()
            } else {
                ActivityCompat.requestPermissions(
                    requireActivity(),
                    arrayOf(Manifest.permission.RECORD_AUDIO),
                    REQUEST_MICROPHONE_PERMISSION
                )
            }
        }


        binding.recomendedCrops.setOnClickListener { startActivity(Intent(requireContext(),DiseaseDetectionActivity::class.java)) }
        binding.diseaseDetection.setOnClickListener { startActivity(Intent(requireContext(),PlantDetectionActivity::class.java)) }

//        val news = News("title","desc","image")
        val databaseReference = FirebaseDatabase.getInstance().getReference("News")
        newsAdapter = NewsAdapter(requireContext(),newsList)
        binding.homeRV.adapter = newsAdapter
        binding.homeRV.layoutManager = LinearLayoutManager(requireContext(),LinearLayoutManager.HORIZONTAL,false)
        databaseReference.addValueEventListener(object : ValueEventListener {
            override fun onDataChange(dataSnapshot: DataSnapshot) {
                newsList.clear()
                for (newsSnapshot in dataSnapshot.children) {
                    val news = newsSnapshot.getValue(News::class.java)
                    if (news != null) {
                        newsList.add(news)
                    }
                }
                newsAdapter.notifyDataSetChanged()
            }
            override fun onCancelled(databaseError: DatabaseError) {
                Toast.makeText(requireContext(), "${databaseError.message}", Toast.LENGTH_SHORT).show()
            }
        })

         weatherModel = Paper.book().read("weather")
        if (weatherModel != null) {
            binddata(weatherModel!!)
        } else {
            // Weather data is not in Paper, fetch it
            binding.weatherView.alpha = 0.7f
            binding.mainCardView.visibility = View.GONE

            if (checkLocationPermission()) {
                getCurrentLocation()
            } else {
                requestLocationPermission()
            }
        }

        return binding.root
    }
    private fun showListeningDialog() {
        val inflater = requireActivity().layoutInflater
        val dialogView = inflater.inflate(R.layout.dialog_listening, null)

        val builder = AlertDialog.Builder(requireContext())
        builder.setView(dialogView)
        builder.setCancelable(false)

        builder.setPositiveButton("Cancel") { dialog, _ ->
            dialog.dismiss()
        }

        listeningDialog = builder.create()
        listeningDialog?.show()

        val microphoneAnimation = dialogView.findViewById<LottieAnimationView>(R.id.microphoneAnimation)
        microphoneAnimation.playAnimation()
    }


    private fun dismissListeningDialog() {
        val microphoneAnimation = listeningDialog?.findViewById<LottieAnimationView>(R.id.microphoneAnimation)
        microphoneAnimation?.pauseAnimation()
        listeningDialog?.dismiss()
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
        if (requestCode == REQUEST_MICROPHONE_PERMISSION) {
            if (grantResults.isNotEmpty() && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                // Permission is granted, start speech recognition
                startSpeechRecognition()
            } else {
                // Permission is denied, inform the user
                Toast.makeText(
                    requireContext(),
                    "Microphone permission is required for speech recognition",
                    Toast.LENGTH_SHORT
                ).show()
            }
        }
        if (requestCode == REQUEST_LOCATION_PERMISSION) {
            if (grantResults.isNotEmpty() && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                getCurrentLocation()
            } else {
                requestLocationPermission()
            }
        }
    }
    private fun startSpeechRecognition() {
        val speechRecognizer = SpeechRecognizer.createSpeechRecognizer(requireContext())

        val recognitionListener = object : RecognitionListener {
            override fun onReadyForSpeech(params: Bundle?) {
            }

            override fun onBeginningOfSpeech() {
            }

            override fun onRmsChanged(rmsdB: Float) {
            }

            override fun onBufferReceived(buffer: ByteArray?) {
            }

            override fun onEndOfSpeech() {
            }

            override fun onError(error: Int) {
                Toast.makeText(requireContext(), "Something went wrong", Toast.LENGTH_SHORT).show()
            }

            override fun onResults(results: Bundle?) {
                val matches = results?.getStringArrayList(SpeechRecognizer.RESULTS_RECOGNITION)
                if (matches != null && matches.isNotEmpty()) {
                    val spokenText = matches[0].toLowerCase()

                    val diseaseDetectionKeywords = listOf("disease detection", "open disease detection","how to check for disease detection",)
                    val recommendedCropsKeywords = listOf("recommended crops", "open recommended crops", "how to check recommended crops")
                    val currentWeatherKeywords = listOf("current weather","what is todays weather","tell me weather","weather")

                    val regexPattern = "\\b(?:${(diseaseDetectionKeywords + recommendedCropsKeywords + currentWeatherKeywords).joinToString("|")})\\b"

                    val regex = Regex(regexPattern)
                    val matchedKeywords = regex.findAll(spokenText).map { it.value }.toList()

                    if (matchedKeywords.isNotEmpty()) {
                        when {
                            matchedKeywords.any { it in diseaseDetectionKeywords } -> {
                                val intent = Intent(requireContext(), PlantDetectionActivity::class.java)
                                startActivity(intent)
                            }
                            matchedKeywords.any { it in recommendedCropsKeywords } -> {
                                val intent = Intent(requireContext(), DiseaseDetectionActivity::class.java)
                                startActivity(intent)
                            }
                            matchedKeywords.any { it in currentWeatherKeywords } -> {
                                val sentenceToSpeak = "Current temp is ${weatherModel!!.temp} and humidity is ${weatherModel!!.humidity}"
                                textToSpeech.speak(sentenceToSpeak, TextToSpeech.QUEUE_FLUSH, null, null)
                            }
                        }
                    } else {
                        Toast.makeText(requireContext(), "Command not recognized", Toast.LENGTH_SHORT).show()
                    }
                }
                dismissListeningDialog()
            }



            override fun onPartialResults(partialResults: Bundle?) {
            }

            override fun onEvent(eventType: Int, params: Bundle?) {
            }
        }

        // Set the recognition listener
        speechRecognizer.setRecognitionListener(recognitionListener)

        // Start listening
        val speechIntent = Intent(RecognizerIntent.ACTION_RECOGNIZE_SPEECH)
        speechIntent.putExtra(RecognizerIntent.EXTRA_LANGUAGE_MODEL, RecognizerIntent.LANGUAGE_MODEL_FREE_FORM)
        speechIntent.putExtra(RecognizerIntent.EXTRA_LANGUAGE, Locale.getDefault())
        speechRecognizer.startListening(speechIntent)
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

                startFillingLocation(latitude, longitude)
            } else {
                Toast.makeText(requireContext(), "Something went wrong", Toast.LENGTH_SHORT).show()
            }
        } catch (e: SecurityException) {
            e.printStackTrace()
        }

    }

    private fun startFillingLocation(latitude: Double, longitude: Double) {
        val url = "https://api.openweathermap.org/data/2.5/weather?lat=$latitude&lon=$longitude&appid=$api_key"

        Log.d("'url",url)

        val requestQueue = Volley.newRequestQueue(requireContext())
        val jsonObjectRequest = JsonObjectRequest(
            Request.Method.GET, url, null,
            { response ->
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
