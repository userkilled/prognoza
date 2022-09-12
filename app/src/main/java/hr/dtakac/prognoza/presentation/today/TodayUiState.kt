package hr.dtakac.prognoza.presentation.today

import androidx.annotation.DrawableRes
import hr.dtakac.prognoza.presentation.TextResource

sealed interface TodayUiState {
    object Loading : TodayUiState

    data class Empty(
        val text: TextResource
    ) : TodayUiState

    data class Success(
        val placeName: TextResource,
        val time: TextResource,
        val temperature: TextResource,
        val feelsLike: TextResource,
        val currentDescription: TextResource,
        @DrawableRes
        val descriptionIcon: Int,
        val restOfDayDescription: TextResource,
        val hours: List<TodayHour>
    ) : TodayUiState
}

data class TodayHour(
    val time: TextResource,
    @DrawableRes
    val icon: Int,
    val temperature: TextResource,
    val precipitation: TextResource?,
    val wind: TextResource,
    val windIconRotation: Float
)