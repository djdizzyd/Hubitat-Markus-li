/**
 * TEMPERATURE HUMIDITY METHODS (helpers-temperature-humidity)
 *
 * Helper functions included in all drivers with Temperature and Humidity
 */
private BigDecimal getAdjustedTemp(BigDecimal value) {
    //Double decimalLimit = 10
    Integer res = 1
    if(tempRes != null && tempRes != '') {
        res = Integer.parseInt(tempRes)
    }
    if (tempUnitConversion == "2") {
        value = celsiusToFahrenheit(value)
    } else if (tempUnitConversion == "3") {
        value = fahrenheitToCelsius(value)
    }
	if (tempOffset) {
	   return (value + new BigDecimal(tempOffset)).setScale(res, BigDecimal.ROUND_HALF_UP)
	} else {
       return value.setScale(res, BigDecimal.ROUND_HALF_UP)
    }
}

private BigDecimal getAdjustedHumidity(BigDecimal value) {
    if (humidityOffset) {
	   return (value + new BigDecimal(humidityOffset)).setScale(1, BigDecimal.ROUND_HALF_UP)
	} else {
       return value.setScale(1, BigDecimal.ROUND_HALF_UP)
    }
}

private BigDecimal getAdjustedPressure(BigDecimal value, Integer decimals=2) {
    if (pressureOffset) {
	   return (value + new BigDecimal(pressureOffset)).setScale(decimals, BigDecimal.ROUND_HALF_UP)
	} else {
       return value.setScale(decimals, BigDecimal.ROUND_HALF_UP)
    }   
}

private BigDecimal convertPressure(BigDecimal pressureInkPa) {
    BigDecimal pressure = pressureInkPa
    switch(pressureUnitConversion) {
        case null:
        case "kPa":
			pressure = getAdjustedPressure(pressure / 10)
			break
		case "inHg":
			pressure = getAdjustedPressure(pressure * 0.0295299)
			break
		case "mmHg":
            pressure = getAdjustedPressure(pressure * 0.75006157)
			break
        case "atm":
			pressure = getAdjustedPressure(pressure / 1013.25, 5)
			break
        default:
            // Tasmota provides the pressure in mbar by default
            pressure = getAdjustedPressure(pressure, 1)
            break
    }
    return pressure
}

/**
 *   --END-- TEMPERATURE HUMIDITY METHODS (helpers-temperature-humidity)
 */