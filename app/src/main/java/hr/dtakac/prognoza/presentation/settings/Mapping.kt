package hr.dtakac.prognoza.presentation.settings

import hr.dtakac.prognoza.R
import hr.dtakac.prognoza.entities.forecast.units.LengthUnit
import hr.dtakac.prognoza.entities.forecast.units.PressureUnit
import hr.dtakac.prognoza.entities.forecast.units.SpeedUnit
import hr.dtakac.prognoza.entities.forecast.units.TemperatureUnit
import hr.dtakac.prognoza.presentation.TextResource

fun mapToTemperatureUnitSetting(
    selectedTemperatureUnit: TemperatureUnit,
    availableTemperatureUnits: List<TemperatureUnit>
): UnitSetting = UnitSetting(
    name = TextResource.fromStringId(R.string.temperature_unit),
    value = TextResource.fromStringId(selectedTemperatureUnit.toSettingsLabel()),
    values = availableTemperatureUnits.map(TemperatureUnit::toSettingsLabel).map(TextResource::fromStringId)
)

fun mapToWindUnitSetting(
    selectedWindUnit: SpeedUnit,
    availableWindUnits: List<SpeedUnit>
): UnitSetting = UnitSetting(
    name = TextResource.fromStringId(R.string.wind_unit),
    value = TextResource.fromStringId(selectedWindUnit.toSettingsLabel()),
    values = availableWindUnits.map(SpeedUnit::toSettingsLabel).map(TextResource::fromStringId)
)

fun mapToPrecipitationUnitSetting(
    selectedPrecipitationUnit: LengthUnit,
    availablePrecipitationUnits: List<LengthUnit>
): UnitSetting = UnitSetting(
    name = TextResource.fromStringId(R.string.precipitation_unit),
    value = TextResource.fromStringId(selectedPrecipitationUnit.toSettingsLabel()),
    values = availablePrecipitationUnits.map(LengthUnit::toSettingsLabel).map(TextResource::fromStringId)
)

fun mapToPressureUnitSetting(
    selectedPressureUnit: PressureUnit,
    availablePressureUnits: List<PressureUnit>
): UnitSetting = UnitSetting(
    name = TextResource.fromStringId(R.string.pressure_unit),
    value = TextResource.fromStringId(selectedPressureUnit.toSettingsLabel()),
    values = availablePressureUnits.map(PressureUnit::toSettingsLabel).map(TextResource::fromStringId)
)