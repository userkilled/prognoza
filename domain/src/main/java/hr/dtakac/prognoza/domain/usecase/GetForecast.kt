package hr.dtakac.prognoza.domain.usecase

import hr.dtakac.prognoza.domain.forecast.ForecastProvider
import hr.dtakac.prognoza.domain.forecast.ForecastProviderResult
import hr.dtakac.prognoza.domain.forecast.ForecastSaver
import hr.dtakac.prognoza.domain.forecast.SavedForecastGetter
import hr.dtakac.prognoza.domain.settings.SettingsRepository
import hr.dtakac.prognoza.entities.Place
import hr.dtakac.prognoza.entities.forecast.units.LengthUnit
import hr.dtakac.prognoza.entities.forecast.units.SpeedUnit
import hr.dtakac.prognoza.entities.forecast.units.TemperatureUnit
import hr.dtakac.prognoza.entities.forecast.Forecast
import hr.dtakac.prognoza.entities.forecast.ForecastDatum

class GetForecast(
    private val getSelectedPlace: GetSelectedPlace,
    private val savedForecastGetter: SavedForecastGetter,
    private val forecastSaver: ForecastSaver,
    private val forecastProvider: ForecastProvider,
    private val settingsRepository: SettingsRepository
) {
    suspend operator fun invoke(): GetForecastResult {
        val selectedPlace = getSelectedPlace() ?: return GetForecastResult.Empty.NoSelectedPlace
        val latitude = selectedPlace.latitude
        val longitude = selectedPlace.longitude

        val freshForecast = forecastProvider.provide(latitude, longitude)
        if (freshForecast is ForecastProviderResult.Success) {
            forecastSaver.save(latitude, longitude, freshForecast.data)
        }

        return mapToResult(
            selectedPlace,
            savedForecastGetter.get(latitude, longitude)
        )
    }

    private suspend fun mapToResult(
        place: Place,
        data: List<ForecastDatum>
    ): GetForecastResult = if (data.isEmpty()) {
        GetForecastResult.Empty.Error
    } else GetForecastResult.Success(
        placeName = place.name,
        forecast = Forecast(data),
        temperatureUnit = settingsRepository.getTemperatureUnit(),
        windUnit = settingsRepository.getWindUnit(),
        precipitationUnit = settingsRepository.getPrecipitationUnit()
    )
}

sealed interface GetForecastResult {
    data class Success(
        val placeName: String,
        val forecast: Forecast,
        val temperatureUnit: TemperatureUnit,
        val windUnit: SpeedUnit,
        val precipitationUnit: LengthUnit
    ) : GetForecastResult

    sealed interface Empty : GetForecastResult {
        object Error : Empty
        object NoSelectedPlace : Empty
    }
}