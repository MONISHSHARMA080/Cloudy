package com.example.ui.screens

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutVertically
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.IntrinsicSize
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.asPaddingValues
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.safeDrawing
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.AcUnit
import androidx.compose.material.icons.filled.Air
import androidx.compose.material.icons.filled.BlurOn
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Cloud
import androidx.compose.material.icons.filled.CloudQueue
import androidx.compose.material.icons.filled.LocationOn
import androidx.compose.material.icons.filled.Opacity
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.Thunderstorm
import androidx.compose.material.icons.filled.Umbrella
import androidx.compose.material.icons.filled.WaterDrop
import androidx.compose.material.icons.filled.WbSunny
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.VerticalDivider
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.platform.LocalSoftwareKeyboardController
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.text.withStyle
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.local.SavedLocation
import com.example.data.model.GeocodingResult
import com.example.data.model.WeatherResponse
import com.example.ui.viewmodel.WeatherUiState
import com.example.ui.viewmodel.WeatherViewModel
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

@Composable
fun WeatherScreen(
    viewModel: WeatherViewModel,
    modifier: Modifier = Modifier
) {
    val weatherUiState by viewModel.weatherUiState.collectAsState()
    val searchQuery by viewModel.searchQuery.collectAsState()
    val searchResults by viewModel.searchResults.collectAsState()
    val isSearching by viewModel.isSearching.collectAsState()
    val savedLocations by viewModel.savedLocations.collectAsState()
    val isRefreshing by viewModel.isRefreshing.collectAsState()

    val focusManager = LocalFocusManager.current
    val keyboardController = LocalSoftwareKeyboardController.current
    val searchFocusRequester = remember { FocusRequester() }
    var isSearchFocused by remember { mutableStateOf(false) }

    Scaffold(
        modifier = modifier.fillMaxSize(),
        containerColor = MaterialTheme.colorScheme.background,
        contentWindowInsets = WindowInsets.safeDrawing
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .padding(horizontal = 24.dp)
        ) {
            // Minimalist Header & Search Input
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 12.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                OutlinedTextField(
                    value = searchQuery,
                    onValueChange = { viewModel.search(it) },
                    placeholder = {
                        Text(
                            text = "SEARCH CITY...",
                            fontFamily = FontFamily.SansSerif,
                            fontSize = 12.sp,
                            letterSpacing = 1.5.sp,
                            fontWeight = FontWeight.Medium,
                            color = MaterialTheme.colorScheme.secondary
                        )
                    },
                    modifier = Modifier
                        .weight(1f)
                        .focusRequester(searchFocusRequester)
                        .testTag("search_input"),
                    leadingIcon = {
                        Icon(
                            imageVector = Icons.Default.Search,
                            contentDescription = "Search Icon",
                            tint = MaterialTheme.colorScheme.primary,
                            modifier = Modifier.size(14.dp) // Drastically smaller search icon
                        )
                    },
                    trailingIcon = {
                        if (searchQuery.isNotEmpty()) {
                            IconButton(
                                onClick = {
                                    viewModel.clearSearch()
                                    focusManager.clearFocus()
                                },
                                modifier = Modifier.testTag("clear_search")
                            ) {
                                Icon(
                                    imageVector = Icons.Default.Close,
                                    contentDescription = "Clear Search",
                                    tint = MaterialTheme.colorScheme.primary,
                                    modifier = Modifier.size(14.dp)
                                )
                            }
                        }
                    },
                    singleLine = true,
                    keyboardOptions = KeyboardOptions(imeAction = ImeAction.Search),
                    keyboardActions = KeyboardActions(
                        onSearch = {
                            viewModel.search(searchQuery)
                            keyboardController?.hide()
                        }
                    ),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedTextColor = MaterialTheme.colorScheme.onBackground,
                        unfocusedTextColor = MaterialTheme.colorScheme.onBackground,
                        focusedBorderColor = MaterialTheme.colorScheme.primary,
                        unfocusedBorderColor = MaterialTheme.colorScheme.outline.copy(alpha = 0.3f),
                        cursorColor = MaterialTheme.colorScheme.primary
                    ),
                    shape = RoundedCornerShape(0.dp) // Razor sharp brutalist corners
                )

                if (weatherUiState is WeatherUiState.Success && searchQuery.isEmpty()) {
                    IconButton(
                        onClick = { viewModel.refreshCurrentLocation() },
                        modifier = Modifier
                            .padding(start = 8.dp)
                            .testTag("refresh_button")
                    ) {
                        if (isRefreshing) {
                            CircularProgressIndicator(
                                modifier = Modifier.size(16.dp),
                                color = MaterialTheme.colorScheme.primary,
                                strokeWidth = 1.5.dp
                            )
                        } else {
                            Icon(
                                imageVector = Icons.Default.Refresh,
                                contentDescription = "Refresh Weather",
                                tint = MaterialTheme.colorScheme.primary,
                                modifier = Modifier.size(18.dp)
                            )
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(8.dp))

            // Search Results Overlay
            if (searchQuery.isNotEmpty()) {
                Box(modifier = Modifier.fillMaxWidth().weight(1f)) {
                    if (isSearching) {
                        Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                            CircularProgressIndicator(
                                color = MaterialTheme.colorScheme.primary,
                                strokeWidth = 2.dp,
                                modifier = Modifier.size(24.dp)
                            )
                        }
                    } else if (searchResults.isEmpty()) {
                        Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                            Text(
                                text = "NO LOCATIONS FOUND",
                                fontFamily = FontFamily.Monospace,
                                fontSize = 12.sp,
                                letterSpacing = 1.sp,
                                color = MaterialTheme.colorScheme.secondary
                            )
                        }
                    } else {
                        LazyColumn(
                            modifier = Modifier.fillMaxSize(),
                            verticalArrangement = Arrangement.spacedBy(1.dp)
                        ) {
                            items(searchResults) { result ->
                                SearchResultItem(
                                    result = result,
                                    onClick = {
                                        viewModel.selectLocation(
                                            SavedLocation(
                                                name = result.name,
                                                latitude = result.latitude,
                                                longitude = result.longitude,
                                                country = result.country,
                                                admin1 = result.admin1,
                                                timezone = result.timezone
                                            )
                                        )
                                        focusManager.clearFocus()
                                        keyboardController?.hide()
                                    }
                                )
                            }
                        }
                    }
                }
            } else {
                // Main Weather Info Screen
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .weight(1f)
                ) {
                    when (val state = weatherUiState) {
                        is WeatherUiState.Loading -> {
                            Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                                CircularProgressIndicator(
                                    color = MaterialTheme.colorScheme.primary,
                                    strokeWidth = 2.dp
                                )
                            }
                        }
                        is WeatherUiState.Success -> {
                            Box(
                                modifier = Modifier
                                    .fillMaxSize()
                                    .verticalScroll(rememberScrollState()),
                                contentAlignment = Alignment.Center
                            ) {
                                Column(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .padding(horizontal = 24.dp, vertical = 24.dp),
                                    horizontalAlignment = Alignment.CenterHorizontally,
                                    verticalArrangement = Arrangement.Center
                                ) {
                                    val current = state.weather.current
                                    if (current != null) {
                                        // 1. Large Center Temperature
                                        Text(
                                            text = "${current.temperature.toInt()}°",
                                            fontFamily = FontFamily.SansSerif,
                                            fontSize = 110.sp,
                                            fontWeight = FontWeight.Bold,
                                            lineHeight = 110.sp,
                                            color = MaterialTheme.colorScheme.onBackground,
                                            textAlign = TextAlign.Center,
                                            modifier = Modifier.testTag("weather_temp")
                                        )

                                        // 2. Condition Label
                                        val (_, conditionLabel) = getWeatherIconAndLabel(current.weatherCode)
                                        Text(
                                            text = conditionLabel.uppercase(Locale.getDefault()),
                                            fontFamily = FontFamily.Monospace,
                                            fontSize = 13.sp,
                                            fontWeight = FontWeight.Bold,
                                            letterSpacing = 3.sp,
                                            color = MaterialTheme.colorScheme.onBackground,
                                            textAlign = TextAlign.Center,
                                            modifier = Modifier.padding(top = 8.dp)
                                        )

                                        Spacer(modifier = Modifier.height(12.dp))

                                        // 3. Elegant Location & Date Line
                                        val locationAndDate = buildString {
                                            append(state.location.name.uppercase(Locale.getDefault()))
                                            state.location.country?.let {
                                                append(", ${it.uppercase(Locale.getDefault())}")
                                            }
                                            append("  •  ")
                                            append(getCurrentDateString().uppercase(Locale.getDefault()))
                                        }
                                        Text(
                                            text = locationAndDate,
                                            fontFamily = FontFamily.Monospace,
                                            fontSize = 10.sp,
                                            fontWeight = FontWeight.Bold,
                                            letterSpacing = 1.sp,
                                            color = MaterialTheme.colorScheme.secondary,
                                            textAlign = TextAlign.Center,
                                            modifier = Modifier
                                                .testTag("weather_city_name")
                                                .padding(horizontal = 16.dp)
                                        )

                                        Spacer(modifier = Modifier.height(36.dp))

                                        // 4. Centered Gemini Summary block (The poetic weather narrative)
                                        val onBg = MaterialTheme.colorScheme.onBackground
                                        val annotatedText = remember(state.summary, onBg) {
                                            parseMarkdownToAnnotatedString(state.summary, onBg)
                                        }
                                        Text(
                                            text = annotatedText,
                                            fontFamily = FontFamily.SansSerif,
                                            fontSize = 14.sp,
                                            lineHeight = 22.sp,
                                            textAlign = TextAlign.Center,
                                            fontWeight = FontWeight.Normal,
                                            color = MaterialTheme.colorScheme.onBackground,
                                            modifier = Modifier.padding(horizontal = 8.dp)
                                        )

                                        Spacer(modifier = Modifier.height(36.dp))

                                        // 5. Minimal Stats Line
                                        Text(
                                            text = "FEELS LIKE ${current.apparentTemperature?.toInt() ?: current.temperature.toInt()}°  •  WIND ${current.windSpeed?.toInt() ?: 0} KM/H  •  HUMIDITY ${current.humidity?.toInt() ?: 0}%",
                                            fontFamily = FontFamily.Monospace,
                                            fontSize = 10.sp,
                                            fontWeight = FontWeight.Bold,
                                            letterSpacing = 1.sp,
                                            color = MaterialTheme.colorScheme.secondary,
                                            textAlign = TextAlign.Center
                                        )

                                        Spacer(modifier = Modifier.height(44.dp))

                                        // 6. 7-Day Forecast Section
                                        DailyForecastSection(weather = state.weather)
                                    }

                                    Spacer(modifier = Modifier.height(40.dp))

                                    // 7. Saved Locations quick selector at the bottom
                                    if (savedLocations.size > 1) {
                                        Column(
                                            horizontalAlignment = Alignment.CenterHorizontally,
                                            modifier = Modifier.fillMaxWidth()
                                        ) {
                                            Text(
                                                text = "SAVED PLACES",
                                                fontFamily = FontFamily.Monospace,
                                                fontSize = 9.sp,
                                                fontWeight = FontWeight.Bold,
                                                letterSpacing = 1.5.sp,
                                                color = MaterialTheme.colorScheme.secondary.copy(alpha = 0.6f),
                                                modifier = Modifier.padding(bottom = 8.dp)
                                            )
                                            Row(
                                                modifier = Modifier
                                                    .fillMaxWidth()
                                                    .horizontalScroll(rememberScrollState()),
                                                horizontalArrangement = Arrangement.Center,
                                                verticalAlignment = Alignment.CenterVertically
                                            ) {
                                                savedLocations.forEachIndexed { index, savedLoc ->
                                                    val isSelected = savedLoc.id == state.location.id
                                                    Text(
                                                        text = savedLoc.name.uppercase(Locale.getDefault()),
                                                        fontFamily = FontFamily.SansSerif,
                                                        fontSize = 11.sp,
                                                        fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal,
                                                        color = if (isSelected) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.secondary,
                                                        modifier = Modifier
                                                            .clickable { viewModel.selectSavedLocation(savedLoc.id) }
                                                            .padding(horizontal = 8.dp, vertical = 4.dp)
                                                    )
                                                    if (index < savedLocations.size - 1) {
                                                        Text(
                                                            text = "·",
                                                            fontFamily = FontFamily.Monospace,
                                                            fontSize = 12.sp,
                                                            color = MaterialTheme.colorScheme.outline.copy(alpha = 0.3f),
                                                            modifier = Modifier.padding(horizontal = 4.dp)
                                                        )
                                                    }
                                                }
                                            }
                                        }
                                    }
                                }
                            }
                        }
                        is WeatherUiState.Error -> {
                            Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                                Column(
                                    horizontalAlignment = Alignment.CenterHorizontally,
                                    verticalArrangement = Arrangement.spacedBy(12.dp),
                                    modifier = Modifier.padding(24.dp)
                                ) {
                                    Text(
                                        text = state.message,
                                        fontFamily = FontFamily.SansSerif,
                                        fontSize = 14.sp,
                                        textAlign = TextAlign.Center,
                                        color = MaterialTheme.colorScheme.onBackground
                                    )
                                    Text(
                                        text = "TRY AGAIN",
                                        fontFamily = FontFamily.Monospace,
                                        fontSize = 12.sp,
                                        letterSpacing = 1.5.sp,
                                        fontWeight = FontWeight.Bold,
                                        modifier = Modifier
                                            .border(BorderStroke(1.dp, MaterialTheme.colorScheme.primary))
                                            .clickable {
                                                viewModel.refreshCurrentLocation()
                                            }
                                            .padding(horizontal = 16.dp, vertical = 8.dp)
                                    )
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun SearchResultItem(
    result: GeocodingResult,
    onClick: () -> Unit
) {
    val subtitle = listOfNotNull(result.admin1, result.country).joinToString(", ")
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick)
            .border(BorderStroke(0.5.dp, MaterialTheme.colorScheme.outline.copy(alpha = 0.15f)))
            .background(MaterialTheme.colorScheme.background)
            .padding(vertical = 14.dp, horizontal = 16.dp)
    ) {
        Text(
            text = result.name.uppercase(Locale.getDefault()),
            fontFamily = FontFamily.SansSerif,
            fontSize = 14.sp,
            fontWeight = FontWeight.Bold,
            color = MaterialTheme.colorScheme.onBackground,
            letterSpacing = 0.5.sp
        )
        if (subtitle.isNotEmpty()) {
            Spacer(modifier = Modifier.height(2.dp))
            Text(
                text = subtitle.uppercase(Locale.getDefault()),
                fontFamily = FontFamily.Monospace,
                fontSize = 10.sp,
                color = MaterialTheme.colorScheme.secondary,
                letterSpacing = 0.5.sp
            )
        }
    }
}

@Composable
fun CurrentWeatherSection(
    location: SavedLocation,
    weather: WeatherResponse
) {
    val current = weather.current ?: return
    val (weatherIcon, conditionName) = getWeatherIconAndLabel(current.weatherCode)

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(top = 16.dp)
    ) {
        // City Name
        Text(
            text = location.name.uppercase(Locale.getDefault()),
            fontFamily = FontFamily.SansSerif,
            fontSize = 24.sp,
            fontWeight = FontWeight.Light,
            letterSpacing = 4.sp,
            color = MaterialTheme.colorScheme.onBackground,
            modifier = Modifier.testTag("weather_city_name")
        )

        // Subtitle (Admin / Country & Local Time)
        val subtitleText = buildString {
            val details = listOfNotNull(location.admin1, location.country).joinToString(", ")
            if (details.isNotEmpty()) append("${details.uppercase(Locale.getDefault())}  |  ")
            append(getCurrentDateString())
        }

        Text(
            text = subtitleText,
            fontFamily = FontFamily.Monospace,
            fontSize = 10.sp,
            letterSpacing = 0.5.sp,
            color = MaterialTheme.colorScheme.secondary,
            modifier = Modifier.padding(top = 4.dp)
        )

        Spacer(modifier = Modifier.height(24.dp))

        // Large Temperature and Weather Icon side-by-side
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Column {
                Text(
                    text = "${current.temperature.toInt()}°",
                    fontFamily = FontFamily.SansSerif,
                    fontSize = 84.sp,
                    fontWeight = FontWeight.Bold,
                    lineHeight = 84.sp,
                    color = MaterialTheme.colorScheme.onBackground,
                    modifier = Modifier.testTag("weather_temp")
                )
                Text(
                    text = conditionName.uppercase(Locale.getDefault()),
                    fontFamily = FontFamily.Monospace,
                    fontSize = 12.sp,
                    fontWeight = FontWeight.Bold,
                    letterSpacing = 2.sp,
                    color = MaterialTheme.colorScheme.onBackground,
                    modifier = Modifier.padding(top = 2.dp)
                )
            }

            Icon(
                imageVector = weatherIcon,
                contentDescription = conditionName,
                tint = MaterialTheme.colorScheme.onBackground,
                modifier = Modifier
                    .size(72.dp)
                    .border(BorderStroke(1.dp, MaterialTheme.colorScheme.primary), RoundedCornerShape(0.dp))
                    .padding(16.dp)
            )
        }
    }
}

@Composable
fun GeminiSummarySection(summary: String) {
    val onBg = MaterialTheme.colorScheme.onBackground
    val annotatedText = remember(summary, onBg) {
        parseMarkdownToAnnotatedString(summary, onBg)
    }
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .border(BorderStroke(1.dp, MaterialTheme.colorScheme.primary.copy(alpha = 0.2f)))
            .padding(16.dp)
    ) {
        Text(
            text = "MINIMAL ADVICE",
            fontFamily = FontFamily.Monospace,
            fontSize = 9.sp,
            fontWeight = FontWeight.Bold,
            letterSpacing = 1.5.sp,
            color = MaterialTheme.colorScheme.secondary,
            modifier = Modifier.padding(bottom = 8.dp)
        )
        Text(
            text = annotatedText,
            fontFamily = FontFamily.SansSerif,
            fontSize = 14.sp,
            lineHeight = 20.sp,
            fontWeight = FontWeight.Normal,
            color = MaterialTheme.colorScheme.onBackground
        )
    }
}

@Composable
fun CurrentStatsSection(weather: WeatherResponse) {
    val current = weather.current ?: return
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .height(IntrinsicSize.Min)
            .border(BorderStroke(1.dp, MaterialTheme.colorScheme.primary))
            .background(MaterialTheme.colorScheme.background)
            .padding(vertical = 12.dp),
        horizontalArrangement = Arrangement.SpaceEvenly
    ) {
        StatItem(
            label = "FEELS LIKE",
            value = "${current.apparentTemperature?.toInt() ?: current.temperature.toInt()}°",
            icon = Icons.Default.CloudQueue,
            modifier = Modifier.weight(1f)
        )
        VerticalDivider(color = MaterialTheme.colorScheme.primary, modifier = Modifier.fillMaxHeight())
        StatItem(
            label = "WIND",
            value = "${current.windSpeed?.toInt() ?: 0} KM/H",
            icon = Icons.Default.Air,
            modifier = Modifier.weight(1f)
        )
        VerticalDivider(color = MaterialTheme.colorScheme.primary, modifier = Modifier.fillMaxHeight())
        StatItem(
            label = "HUMIDITY",
            value = "${current.humidity?.toInt() ?: 0}%",
            icon = Icons.Default.Opacity,
            modifier = Modifier.weight(1f)
        )
        VerticalDivider(color = MaterialTheme.colorScheme.primary, modifier = Modifier.fillMaxHeight())
        StatItem(
            label = "PRECIP",
            value = "${current.precipitation ?: 0.0} MM",
            icon = Icons.Default.WaterDrop,
            modifier = Modifier.weight(1f)
        )
    }
}

@Composable
fun StatItem(
    label: String,
    value: String,
    icon: ImageVector,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier,
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Icon(
            imageVector = icon,
            contentDescription = null,
            tint = MaterialTheme.colorScheme.secondary,
            modifier = Modifier.size(16.dp)
        )
        Spacer(modifier = Modifier.height(4.dp))
        Text(
            text = label,
            fontFamily = FontFamily.Monospace,
            fontSize = 8.sp,
            fontWeight = FontWeight.Medium,
            letterSpacing = 0.5.sp,
            color = MaterialTheme.colorScheme.secondary
        )
        Spacer(modifier = Modifier.height(2.dp))
        Text(
            text = value,
            fontFamily = FontFamily.SansSerif,
            fontSize = 12.sp,
            fontWeight = FontWeight.Bold,
            color = MaterialTheme.colorScheme.onBackground
        )
    }
}

@Composable
fun HourlyForecastSection(weather: WeatherResponse) {
    val hourly = weather.hourly ?: return
    Column(modifier = Modifier.fillMaxWidth()) {
        Text(
            text = "24-HOUR FORECAST",
            fontFamily = FontFamily.Monospace,
            fontSize = 11.sp,
            fontWeight = FontWeight.Bold,
            letterSpacing = 1.5.sp,
            color = MaterialTheme.colorScheme.secondary,
            modifier = Modifier.padding(bottom = 12.dp)
        )

        Row(
            modifier = Modifier
                .fillMaxWidth()
                .horizontalScroll(rememberScrollState()),
            horizontalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            // Take the next 24 elements from hourly forecast
            val itemsCount = minOf(hourly.time.size, 24)
            for (i in 0 until itemsCount) {
                val hourStr = formatHourString(hourly.time[i])
                val temp = hourly.temperatures[i].toInt()
                val code = hourly.weatherCodes[i]
                val icon = getWeatherIconAndLabel(code).first

                Column(
                    modifier = Modifier
                        .border(BorderStroke(0.5.dp, MaterialTheme.colorScheme.outline.copy(alpha = 0.2f)))
                        .background(MaterialTheme.colorScheme.background)
                        .padding(vertical = 12.dp, horizontal = 14.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Text(
                        text = hourStr,
                        fontFamily = FontFamily.Monospace,
                        fontSize = 10.sp,
                        color = MaterialTheme.colorScheme.secondary
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    Icon(
                        imageVector = icon,
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.onBackground,
                        modifier = Modifier.size(18.dp)
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(
                        text = "$temp°",
                        fontFamily = FontFamily.SansSerif,
                        fontSize = 13.sp,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onBackground
                    )
                }
            }
        }
    }
}

@Composable
fun DailyForecastSection(weather: WeatherResponse) {
    val daily = weather.daily ?: return
    Column(modifier = Modifier.fillMaxWidth()) {
        Text(
            text = "7-DAY FORECAST",
            fontFamily = FontFamily.Monospace,
            fontSize = 11.sp,
            fontWeight = FontWeight.Bold,
            letterSpacing = 1.5.sp,
            color = MaterialTheme.colorScheme.secondary,
            modifier = Modifier.padding(bottom = 12.dp)
        )

        Column(
            modifier = Modifier
                .fillMaxWidth()
                .border(BorderStroke(1.dp, MaterialTheme.colorScheme.primary))
        ) {
            val daysCount = minOf(daily.time.size, 7)
            for (i in 0 until daysCount) {
                val dayName = formatDayString(daily.time[i])
                val code = daily.weatherCodes[i]
                val (icon, label) = getWeatherIconAndLabel(code)
                val tempMin = daily.temperaturesMin[i].toInt()
                val tempMax = daily.temperaturesMax[i].toInt()

                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .background(MaterialTheme.colorScheme.background)
                        .padding(horizontal = 16.dp, vertical = 12.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = dayName.uppercase(Locale.getDefault()),
                        fontFamily = FontFamily.SansSerif,
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Medium,
                        letterSpacing = 1.sp,
                        color = MaterialTheme.colorScheme.onBackground,
                        modifier = Modifier.weight(1.5f)
                    )

                    Row(
                        modifier = Modifier.weight(2f),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.Start
                    ) {
                        Icon(
                            imageVector = icon,
                            contentDescription = label,
                            tint = MaterialTheme.colorScheme.secondary,
                            modifier = Modifier.size(16.dp)
                        )
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(
                            text = label.uppercase(Locale.getDefault()),
                            fontFamily = FontFamily.Monospace,
                            fontSize = 9.sp,
                            color = MaterialTheme.colorScheme.secondary,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis
                        )
                    }

                    Text(
                        text = "$tempMin° / $tempMax°",
                        fontFamily = FontFamily.Monospace,
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onBackground,
                        textAlign = TextAlign.End,
                        modifier = Modifier.weight(1f)
                    )
                }

                if (i < daysCount - 1) {
                    HorizontalDivider(color = MaterialTheme.colorScheme.outline.copy(alpha = 0.2f))
                }
            }
        }
    }
}

@Composable
fun SavedLocationsSection(
    savedList: List<SavedLocation>,
    currentLocationId: Int,
    onSelect: (Int) -> Unit,
    onDelete: (SavedLocation) -> Unit
) {
    if (savedList.size <= 1) return // No need to show if only 1 location is saved

    Column(modifier = Modifier.fillMaxWidth()) {
        Text(
            text = "SAVED PLACES",
            fontFamily = FontFamily.Monospace,
            fontSize = 11.sp,
            fontWeight = FontWeight.Bold,
            letterSpacing = 1.5.sp,
            color = MaterialTheme.colorScheme.secondary,
            modifier = Modifier.padding(bottom = 12.dp)
        )

        Row(
            modifier = Modifier
                .fillMaxWidth()
                .horizontalScroll(rememberScrollState()),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            savedList.forEach { location ->
                val isSelected = location.id == currentLocationId
                Row(
                    modifier = Modifier
                        .border(
                            BorderStroke(
                                if (isSelected) 1.5.dp else 0.5.dp,
                                if (isSelected) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.outline.copy(alpha = 0.3f)
                            )
                        )
                        .background(if (isSelected) MaterialTheme.colorScheme.primary.copy(alpha = 0.05f) else Color.Transparent)
                        .clickable { onSelect(location.id) }
                        .padding(horizontal = 12.dp, vertical = 8.dp)
                        .testTag("location_item_${location.id}"),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(
                        imageVector = Icons.Default.LocationOn,
                        contentDescription = null,
                        tint = if (isSelected) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.secondary,
                        modifier = Modifier.size(12.dp)
                    )
                    Spacer(modifier = Modifier.width(4.dp))
                    Text(
                        text = location.name.uppercase(Locale.getDefault()),
                        fontFamily = FontFamily.SansSerif,
                        fontSize = 11.sp,
                        fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal,
                        color = if (isSelected) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onBackground
                    )
                    Spacer(modifier = Modifier.width(6.dp))
                    Icon(
                        imageVector = Icons.Default.Close,
                        contentDescription = "Remove location",
                        tint = MaterialTheme.colorScheme.secondary,
                        modifier = Modifier
                            .size(12.dp)
                            .clickable { onDelete(location) }
                    )
                }
            }
        }
    }
}

// Helper formatting functions

fun formatHourString(dateStr: String): String {
    return try {
        val parser = SimpleDateFormat("yyyy-MM-dd'T'HH:mm", Locale.US)
        val formatter = SimpleDateFormat("HH:mm", Locale.US)
        val date = parser.parse(dateStr)
        date?.let { formatter.format(it) } ?: dateStr
    } catch (e: Exception) {
        if (dateStr.contains("T")) {
            dateStr.substringAfter("T")
        } else {
            dateStr
        }
    }
}

fun formatDayString(dateStr: String): String {
    return try {
        val parser = SimpleDateFormat("yyyy-MM-dd", Locale.US)
        val formatter = SimpleDateFormat("EEEE", Locale.US)
        val date = parser.parse(dateStr)
        date?.let { formatter.format(it) } ?: dateStr
    } catch (e: Exception) {
        dateStr
    }
}

fun getCurrentDateString(): String {
    val sdf = SimpleDateFormat("EEEE, d MMMM", Locale.US)
    return sdf.format(Date()).uppercase(Locale.US)
}

fun getWeatherIconAndLabel(code: Int): Pair<ImageVector, String> {
    return when (code) {
        0 -> Icons.Default.WbSunny to "Clear sky"
        1, 2, 3 -> Icons.Default.CloudQueue to "Mainly Clear"
        45, 48 -> Icons.Default.BlurOn to "Foggy"
        51, 53, 55 -> Icons.Default.WaterDrop to "Drizzle"
        56, 57 -> Icons.Default.AcUnit to "Freezing Drizzle"
        61, 63, 65 -> Icons.Default.Umbrella to "Rainy"
        66, 67 -> Icons.Default.AcUnit to "Freezing Rain"
        71, 73, 75 -> Icons.Default.AcUnit to "Snowing"
        77 -> Icons.Default.AcUnit to "Snow Grains"
        80, 81, 82 -> Icons.Default.Umbrella to "Showers"
        85, 86 -> Icons.Default.AcUnit to "Snow Showers"
        95 -> Icons.Default.Thunderstorm to "Thunderstorm"
        96, 99 -> Icons.Default.Thunderstorm to "Severe Storm"
        else -> Icons.Default.Cloud to "Cloudy"
    }
}

/**
 * A highly robust and simple parser that turns basic Markdown bold tokens like `**bold**`
 * into a styled AnnotatedString. Excellent for display outputs from LLMs.
 */
fun parseMarkdownToAnnotatedString(text: String, defaultColor: Color): androidx.compose.ui.text.AnnotatedString {
    val parts = text.split("**")
    return buildAnnotatedString {
        for (index in parts.indices) {
            if (index % 2 == 1) {
                withStyle(
                    style = SpanStyle(
                        fontWeight = FontWeight.Bold,
                        color = defaultColor
                    )
                ) {
                    append(parts[index])
                }
            } else {
                withStyle(
                    style = SpanStyle(
                        fontWeight = FontWeight.Normal,
                        color = defaultColor.copy(alpha = 0.85f)
                    )
                ) {
                    append(parts[index])
                }
            }
        }
    }
}
