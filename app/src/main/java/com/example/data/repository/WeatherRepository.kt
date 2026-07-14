package com.example.data.repository

import android.util.Log
import com.example.BuildConfig
import com.example.data.local.LocationDao
import com.example.data.local.SavedLocation
import com.example.data.model.GeocodingResult
import com.example.data.model.WeatherResponse
import com.example.data.network.GeminiCandidate
import com.example.data.network.GeminiClient
import com.example.data.network.GeminiContent
import com.example.data.network.GeminiGenerationConfig
import com.example.data.network.GeminiPart
import com.example.data.network.GeminiRequest
import com.example.data.network.OpenMeteoClient
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.withContext

class WeatherRepository(private val locationDao: LocationDao) {

    val savedLocations: Flow<List<SavedLocation>> = locationDao.getAllLocationsFlow()
    val currentLocation: Flow<SavedLocation?> = locationDao.getCurrentLocationFlow()

    suspend fun searchLocations(query: String): List<GeocodingResult> = withContext(Dispatchers.IO) {
        try {
            val response = OpenMeteoClient.service.searchLocations(query)
            response.results ?: emptyList()
        } catch (e: Exception) {
            Log.e("WeatherRepository", "Error searching locations", e)
            emptyList()
        }
    }

    suspend fun getWeatherForecast(lat: Double, lon: Double): WeatherResponse? = withContext(Dispatchers.IO) {
        try {
            OpenMeteoClient.service.getForecast(latitude = lat, longitude = lon)
        } catch (e: Exception) {
            Log.e("WeatherRepository", "Error fetching forecast", e)
            null
        }
    }

    suspend fun saveAndSelect(location: SavedLocation) = withContext(Dispatchers.IO) {
        locationDao.saveAndSelectLocation(location)
    }

    suspend fun selectLocation(locationId: Int) = withContext(Dispatchers.IO) {
        locationDao.selectLocation(locationId)
    }

    suspend fun deleteLocation(location: SavedLocation) = withContext(Dispatchers.IO) {
        locationDao.deleteLocation(location)
    }

    suspend fun getGeminiSummary(
        cityName: String,
        temp: Double,
        weatherCode: Int,
        humidity: Double,
        windSpeed: Double,
        apparentTemp: Double
    ): String = withContext(Dispatchers.IO) {
        val apiKey = try {
            BuildConfig.GEMINI_API_KEY
        } catch (e: Exception) {
            ""
        }

        if (apiKey.isEmpty() || apiKey == "MY_GEMINI_API_KEY" || apiKey == "placeholder") {
            return@withContext getLocalFallbackSummary(temp, weatherCode)
        }

        val conditionText = getWmoCodeDescription(weatherCode)
        val prompt = """
            Generate an ultra-minimalistic, poetic, and sophisticated 1-sentence weather summary and recommendation for $cityName.
            Current details:
            - Temperature: ${temp}°C (feels like ${apparentTemp}°C)
            - Condition: $conditionText
            - Humidity: ${humidity}%
            - Wind: ${windSpeed} km/h
            
            Speak directly. Limit to 20 words maximum. No emojis. Bold key terms (using markdown **) to maintain a high-contrast print style.
        """.trimIndent()

        val systemPrompt = "You are a highly sophisticated, minimalist weather assistant. You write concise print-style text for high-contrast typography screens. No fluff, no exclamation marks, pure sophistication."

        val request = GeminiRequest(
            contents = listOf(GeminiContent(parts = listOf(GeminiPart(text = prompt)))),
            systemInstruction = GeminiContent(parts = listOf(GeminiPart(text = systemPrompt))),
            generationConfig = GeminiGenerationConfig(
                temperature = 0.5f,
                maxOutputTokens = 60
            )
        )

        try {
            val response = GeminiClient.service.generateContent(apiKey, request)
            response.candidates?.firstOrNull()?.content?.parts?.firstOrNull()?.text?.trim()
                ?: getLocalFallbackSummary(temp, weatherCode)
        } catch (e: Exception) {
            Log.e("WeatherRepository", "Error getting Gemini summary, using local fallback", e)
            getLocalFallbackSummary(temp, weatherCode)
        }
    }

    private fun getLocalFallbackSummary(temp: Double, weatherCode: Int): String {
        val condition = getWmoCodeDescription(weatherCode)
        val tempFeel = when {
            temp < 10 -> "A crisp, **cold** day."
            temp < 20 -> "A **cool**, pleasant afternoon."
            else -> "A **warm**, sun-kissed day."
        }
        val recommendation = when {
            weatherCode in listOf(51, 53, 55, 61, 63, 65, 80, 81, 82) -> "An **umbrella** is recommended."
            weatherCode in listOf(71, 73, 75, 85, 86) -> "Bundle up; **snow** is falling."
            temp < 12 -> "Keep a **heavy coat** handy."
            temp < 18 -> "A **light jacket** will suffice."
            else -> "Perfect for a **brisk walk**."
        }
        return "$tempFeel $recommendation"
    }

    fun getWmoCodeDescription(code: Int): String {
        return when (code) {
            0 -> "Clear sky"
            1, 2, 3 -> "Mainly clear"
            45, 48 -> "Foggy conditions"
            51, 53, 55 -> "Drizzle"
            56, 57 -> "Freezing drizzle"
            61, 63, 65 -> "Rainy"
            66, 67 -> "Freezing rain"
            71, 73, 75 -> "Snow fall"
            77 -> "Snow grains"
            80, 81, 82 -> "Rain showers"
            85, 86 -> "Snow showers"
            95 -> "Thunderstorm"
            96, 99 -> "Thunderstorm with hail"
            else -> "Unknown weather"
        }
    }
}
