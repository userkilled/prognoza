package hr.dtakac.prognoza.viewmodel

import androidx.lifecycle.MutableLiveData
import hr.dtakac.prognoza.extensions.toHourUiModel
import hr.dtakac.prognoza.extensions.totalPrecipitationAmount
import hr.dtakac.prognoza.coroutines.DispatcherProvider
import hr.dtakac.prognoza.uimodel.forecast.TodayForecastUiModel
import hr.dtakac.prognoza.repomodel.ForecastResult
import hr.dtakac.prognoza.repomodel.Success
import hr.dtakac.prognoza.repository.forecast.*
import hr.dtakac.prognoza.repository.preferences.PreferencesRepository
import hr.dtakac.prognoza.uimodel.MeasurementUnit
import hr.dtakac.prognoza.uimodel.forecast.TodayForecastCurrentConditionsModel
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.async
import java.time.ZonedDateTime

class TodayFragmentViewModel(
    coroutineScope: CoroutineScope?,
    private val forecastRepository: ForecastRepository,
    private val dispatcherProvider: DispatcherProvider,
    preferencesRepository: PreferencesRepository
) : ForecastFragmentViewModel<TodayForecastUiModel>(coroutineScope, preferencesRepository) {
    override val _forecast = MutableLiveData<TodayForecastUiModel>()

    override suspend fun getNewForecast(): ForecastResult {
        val selectedPlaceId = preferencesRepository.getSelectedPlaceId()
        return forecastRepository.getTodayForecastHours(selectedPlaceId)
    }

    override suspend fun mapToForecastUiModel(success: Success, unit: MeasurementUnit): TodayForecastUiModel {
        val currentHourAsync = coroutineScope.async(dispatcherProvider.default) {
            success.hours[0].toHourUiModel(unit).copy(time = ZonedDateTime.now())
        }
        val otherHoursAsync = coroutineScope.async(dispatcherProvider.default) {
            success.hours.map { it.toHourUiModel(unit) }
        }
        val precipitationForecastAsync = coroutineScope.async(dispatcherProvider.default) {
            val total = success.hours.subList(0, 2).totalPrecipitationAmount()
            if (total <= 0f) null else total
        }
        return TodayForecastUiModel(
            currentConditionsModel = TodayForecastCurrentConditionsModel(
                currentHour = currentHourAsync.await(),
                precipitationForecast = precipitationForecastAsync.await(),
                unit = unit
            ),
            otherHours = otherHoursAsync.await(),
        )
    }
}