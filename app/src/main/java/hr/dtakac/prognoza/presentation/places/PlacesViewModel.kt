package hr.dtakac.prognoza.presentation.places

import androidx.compose.runtime.State
import androidx.compose.runtime.mutableStateOf
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import hr.dtakac.prognoza.R
import hr.dtakac.prognoza.domain.usecase.*
import hr.dtakac.prognoza.entities.Place
import hr.dtakac.prognoza.presentation.ActionTimedLatch
import hr.dtakac.prognoza.presentation.TextResource
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class PlacesViewModel @Inject constructor(
    private val searchPlaces: SearchPlaces,
    private val getSavedPlaces: GetSavedPlaces,
    private val selectPlace: SelectPlace,
    private val getSelectedPlace: GetSelectedPlace
) : ViewModel() {
    private val loaderTimedLatch = ActionTimedLatch(viewModelScope)
    private var currentPlaces: List<Place> = listOf()

    private val _state = mutableStateOf(PlacesState())
    val state: State<PlacesState> get() = _state

    fun getSaved() {
        viewModelScope.launch {
            showLoader()
            when (val savedPlacesResult = getSavedPlaces()) {
                is GetSavedPlacesResult.Success -> {
                    val selectedPlace = getSelectedPlace()
                    val placesUi = savedPlacesResult.places.map {
                        mapToPlaceUi(it, isSelected = it == selectedPlace)
                    }
                    currentPlaces = savedPlacesResult.places
                    _state.value = _state.value.copy(
                        places = placesUi,
                        empty = null
                    )
                }
                is GetSavedPlacesResult.None -> {
                    _state.value = _state.value.copy(
                        empty = TextResource.fromStringId(R.string.no_saved_places)
                    )
                }
                is GetSavedPlacesResult.Error -> {
                    _state.value = _state.value.copy(
                        empty = TextResource.fromStringId(R.string.error_saved_places)
                    )
                }
            }
            hideLoader()
        }
    }

    fun search(query: String) {
        viewModelScope.launch {
            showLoader()
            when (val searchPlacesResult = searchPlaces(query)) {
                is SearchPlacesResult.Success -> {
                    val selectedPlace = getSelectedPlace()
                    val placesUi = searchPlacesResult.places.map {
                        mapToPlaceUi(it, isSelected = it == selectedPlace)
                    }
                    currentPlaces = searchPlacesResult.places
                    _state.value = _state.value.copy(
                        places = placesUi,
                        empty = null
                    )
                }
                is SearchPlacesResult.None -> {
                    _state.value = _state.value.copy(
                        empty = TextResource.fromStringId(R.string.no_places_found, query)
                    )
                }
                is SearchPlacesResult.Error -> {
                    _state.value = _state.value.copy(
                        empty = TextResource.fromStringId(R.string.error_search_places)
                    )
                }
            }
            hideLoader()
        }
    }

    fun select(index: Int) {
        viewModelScope.launch {
            showLoader()
            val selectedPlace = currentPlaces[index]
            selectPlace(selectedPlace)
            val placesUi = currentPlaces.map {
                mapToPlaceUi(it, isSelected = it == selectedPlace)
            }
            _state.value = _state.value.copy(
                places = placesUi,
                selectedPlace = selectedPlace
            )
            hideLoader()
        }
    }

    private fun showLoader() = loaderTimedLatch.start {
        _state.value = _state.value.copy(isLoading = true)
    }

    private fun hideLoader() = loaderTimedLatch.stop {
        _state.value = _state.value.copy(isLoading = false)
    }
}