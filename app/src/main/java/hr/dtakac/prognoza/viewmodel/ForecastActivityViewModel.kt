package hr.dtakac.prognoza.viewmodel

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import hr.dtakac.prognoza.common.Event
import hr.dtakac.prognoza.extensions.shortenedName
import hr.dtakac.prognoza.repository.forecast.ForecastRepository
import hr.dtakac.prognoza.repository.place.PlaceRepository
import hr.dtakac.prognoza.repository.preferences.PreferencesRepository
import hr.dtakac.prognoza.uimodel.MeasurementUnit
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.launch

class ForecastActivityViewModel(
    coroutineScope: CoroutineScope?,
    private val placeRepository: PlaceRepository,
    private val forecastRepository: ForecastRepository,
    private val preferencesRepository: PreferencesRepository
) : CoroutineScopeViewModel(coroutineScope) {
    private var currentPlaceId: String? = null

    private val _placeName = MutableLiveData<String>()
    val placeName: LiveData<String> get() = _placeName

    private val _selectedUnits = MutableLiveData<Event<MeasurementUnit>>()
    val selectedUnits: LiveData<Event<MeasurementUnit>> get() = _selectedUnits

    fun getSelectedUnits() {
        coroutineScope.launch {
            _selectedUnits.value = Event(preferencesRepository.getSelectedUnit())
        }
    }

    fun getPlaceName() {
        coroutineScope.launch {
            if (isReloadNeeded()) {
                val selectedPlace = placeRepository.get(preferencesRepository.getSelectedPlaceId())
                    ?: placeRepository.getDefaultPlace()
                _placeName.value = selectedPlace.shortenedName
                currentPlaceId = selectedPlace.id
            }
        }
    }

    fun cleanUpDatabase() {
        coroutineScope.launch {
            forecastRepository.deleteExpiredData()
        }
    }

    fun changeUnits() {
        coroutineScope.launch {
            val newUnit = when (preferencesRepository.getSelectedUnit()) {
                MeasurementUnit.IMPERIAL -> MeasurementUnit.METRIC
                MeasurementUnit.METRIC -> MeasurementUnit.IMPERIAL
            }
            preferencesRepository.setSelectedUnit(newUnit)
            _selectedUnits.value = Event(newUnit)
        }
    }

    private suspend fun isReloadNeeded(): Boolean {
        return currentPlaceId != preferencesRepository.getSelectedPlaceId()
    }
}