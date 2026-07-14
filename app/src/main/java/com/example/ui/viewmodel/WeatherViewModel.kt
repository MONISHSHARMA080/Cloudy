package com.example.ui.viewmodel

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.example.data.local.SavedLocation
import com.example.data.local.WeatherDatabase
import com.example.data.model.GeocodingResult
import com.example.data.model.WeatherResponse
import com.example.data.repository.WeatherRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

sealed interface WeatherUiState {
    object Loading : WeatherUiState
    data class Success(
        val location: SavedLocation,
        val weather: WeatherResponse,
        val summary: String
    ) : WeatherUiState
    data class Error(val message: String) : WeatherUiState
}

class WeatherViewModel(
    private val repository: WeatherRepository,
    application: Application
) : AndroidViewModel(application) {

    private val _weatherUiState = MutableStateFlow<WeatherUiState>(WeatherUiState.Loading)
    val weatherUiState: StateFlow<WeatherUiState> = _weatherUiState.asStateFlow()

    private val _searchQuery = MutableStateFlow("")
    val searchQuery: StateFlow<String> = _searchQuery.asStateFlow()

    private val _searchResults = MutableStateFlow<List<GeocodingResult>>(emptyList())
    val searchResults: StateFlow<List<GeocodingResult>> = _searchResults.asStateFlow()

    private val _isSearching = MutableStateFlow(false)
    val isSearching: StateFlow<Boolean> = _isSearching.asStateFlow()

    private val _isRefreshing = MutableStateFlow(false)
    val isRefreshing: StateFlow<Boolean> = _isRefreshing.asStateFlow()

    val savedLocations: StateFlow<List<SavedLocation>> = repository.savedLocations
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = emptyList()
        )

    init {
        // Observe current selected location and load weather automatically
        viewModelScope.launch {
            repository.currentLocation.collectLatest { location ->
                if (location != null) {
                    loadWeatherForLocation(location)
                } else {
                    // Check if database is completely empty; if so, pre-populate defaults
                    val currentSaved = savedLocations.value
                    if (currentSaved.isEmpty()) {
                        prepopulateDefaults()
                    } else {
                        // Otherwise select the first available
                        repository.selectLocation(currentSaved.first().id)
                    }
                }
            }
        }
    }

    private suspend fun prepopulateDefaults() {
        // Prepopulate standard cities
        val defaults = listOf(
            SavedLocation(name = "London", latitude = 51.5074, longitude = -0.1278, country = "United Kingdom"),
            SavedLocation(name = "New York", latitude = 40.7128, longitude = -74.0060, country = "United States"),
            SavedLocation(name = "Tokyo", latitude = 35.6762, longitude = 139.6503, country = "Japan")
        )
        defaults.forEach { repository.saveAndSelect(it) }
    }

    fun loadWeatherForLocation(location: SavedLocation) {
        viewModelScope.launch {
            _weatherUiState.value = WeatherUiState.Loading
            val weather = repository.getWeatherForecast(location.latitude, location.longitude)
            if (weather != null) {
                // Fetch Gemini summary
                val current = weather.current
                val summary = if (current != null) {
                    repository.getGeminiSummary(
                        cityName = location.name,
                        temp = current.temperature,
                        weatherCode = current.weatherCode,
                        humidity = current.humidity ?: 0.0,
                        windSpeed = current.windSpeed ?: 0.0,
                        apparentTemp = current.apparentTemperature ?: current.temperature
                    )
                } else {
                    "Weather details unavailable."
                }
                _weatherUiState.value = WeatherUiState.Success(location, weather, summary)
            } else {
                _weatherUiState.value = WeatherUiState.Error("Failed to fetch weather forecast. Please check your connection.")
            }
        }
    }

    fun search(query: String) {
        _searchQuery.value = query
        if (query.trim().length < 2) {
            _searchResults.value = emptyList()
            return
        }
        viewModelScope.launch {
            _isSearching.value = true
            val results = repository.searchLocations(query)
            _searchResults.value = results
            _isSearching.value = false
        }
    }

    fun selectLocation(location: SavedLocation) {
        viewModelScope.launch {
            repository.saveAndSelect(location)
            clearSearch()
        }
    }

    fun selectSavedLocation(id: Int) {
        viewModelScope.launch {
            repository.selectLocation(id)
        }
    }

    fun deleteLocation(location: SavedLocation) {
        viewModelScope.launch {
            repository.deleteLocation(location)
        }
    }

    fun refreshCurrentLocation() {
        val state = _weatherUiState.value
        if (state is WeatherUiState.Success) {
            viewModelScope.launch {
                _isRefreshing.value = true
                loadWeatherForLocation(state.location)
                _isRefreshing.value = false
            }
        }
    }

    fun clearSearch() {
        _searchQuery.value = ""
        _searchResults.value = emptyList()
    }

    class Factory(private val application: Application) : ViewModelProvider.Factory {
        @Suppress("UNCHECKED_CAST")
        override fun <T : ViewModel> create(modelClass: Class<T>): T {
            val db = WeatherDatabase.getDatabase(application)
            val repository = WeatherRepository(db.locationDao())
            return WeatherViewModel(repository, application) as T
        }
    }
}
