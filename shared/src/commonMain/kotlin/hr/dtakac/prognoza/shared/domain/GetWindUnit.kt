package hr.dtakac.prognoza.shared.domain

import hr.dtakac.prognoza.shared.domain.data.SettingsRepository
import hr.dtakac.prognoza.shared.entity.SpeedUnit

class GetWindUnit(
    private val settingsRepository: SettingsRepository
) {
    suspend operator fun invoke(): SpeedUnit = settingsRepository.getWindUnit()
}