package hr.dtakac.prognoza.domain.usecase

import hr.dtakac.prognoza.domain.settings.SettingsRepository
import hr.dtakac.prognoza.entities.forecast.units.PressureUnit

class SetPressureUnit(
    private val settingsRepository: SettingsRepository
) {
    suspend operator fun invoke(pressureUnit: PressureUnit) =
        settingsRepository.setPressureUnit(pressureUnit)
}