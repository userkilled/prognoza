package hr.dtakac.prognoza.forecast.viewmodel

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import hr.dtakac.prognoza.base.CoroutineScopeViewModel
import hr.dtakac.prognoza.coroutines.DispatcherProvider
import hr.dtakac.prognoza.database.entity.ForecastMeta
import hr.dtakac.prognoza.forecast.uimodel.HourUiModel
import hr.dtakac.prognoza.forecast.uimodel.TodayUiModel
import hr.dtakac.prognoza.common.hasExpired
import hr.dtakac.prognoza.repository.forecast.ForecastRepository
import hr.dtakac.prognoza.repository.forecast.ForecastResult
import hr.dtakac.prognoza.repository.preferences.PreferencesRepository
import hr.dtakac.prognoza.common.toHourUiModels
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.time.ZonedDateTime

class TodayViewModel(
    coroutineScope: CoroutineScope?,
    private val forecastRepository: ForecastRepository,
    private val dispatcherProvider: DispatcherProvider,
    private val preferencesRepository: PreferencesRepository
) : CoroutineScopeViewModel(coroutineScope) {
    private var currentMeta: ForecastMeta? = null

    private val _todayForecast = MutableLiveData<TodayUiModel>()
    val todayForecast: LiveData<TodayUiModel> get() = _todayForecast

    private val _isLoading = MutableLiveData(false)
    val isLoading: LiveData<Boolean> get() = _isLoading

    fun getTodayForecast() {
        coroutineScope.launch {
            if (isReloadNeeded()) {
                getNewForecast()
            }
        }
    }

    private suspend fun getNewForecast() {
        _isLoading.value = true
        val selectedPlaceId = preferencesRepository.getSelectedPlaceId()
        when (val result = forecastRepository.getTodayForecastHours(selectedPlaceId)) {
            is ForecastResult.Success -> handleSuccess(result)
            is ForecastResult.Error -> handleError(result)
        }
        _isLoading.value = false
    }

    private suspend fun handleSuccess(result: ForecastResult.Success) {
        val uiModels = withContext(dispatcherProvider.default) { result.hours.toHourUiModels() }
        val forecastTodayUiModel = TodayUiModel.Success(
            currentHour = HourUiModel(
                time = ZonedDateTime.now(),
                temperature = uiModels[0].temperature,
                weatherIcon = uiModels[0].weatherIcon,
                precipitationAmount = uiModels[0].precipitationAmount,
                windSpeed = uiModels[0].windSpeed,
                windFromDirection = uiModels[0].windFromDirection
            ),
            otherHours = uiModels.subList(1, uiModels.size)
        )
        currentMeta = result.meta
        _todayForecast.value = forecastTodayUiModel
    }

    private fun handleError(error: ForecastResult.Error) {
        _todayForecast.value = TodayUiModel.Error(error.errorMessageResourceId)
    }

    private suspend fun isReloadNeeded(): Boolean {
        return _todayForecast.value == null
                || currentMeta?.hasExpired() != false
                || currentMeta?.placeId != preferencesRepository.getSelectedPlaceId()
    }
}