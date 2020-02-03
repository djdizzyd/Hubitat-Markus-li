/*
    TEMPERATURE HUMIDITY METHODS (helpers-temperature-humidity)

    Helper functions included in all drivers with Temperature and Humidity
*/
private getAdjustedTemp(value) {
    def decimalLimit
    if(tempRes == null || tempRes == '') {
        decimalLimit = 10
    } else {
        decimalLimit = 10**(tempRes as Integer) // 10 to the power of tempRes
    }
    value = Math.round((value as Double) * decimalLimit) / decimalLimit
	if (tempOffset) {
	   return value =  value + Math.round(tempOffset * decimalLimit) / decimalLimit
	} else {
       return value
    }
}

private getAdjustedHumidity(value) {
    value = Math.round((value as Double) * 100) / 100

	if (humidityOffset) {
	   return value =  value + Math.round(humidityOffset * 100) / 100
	} else {
       return value
    }
}

private getAdjustedPressure(value) {
    value = Math.round((value as Double) * 100) / 100

	if (pressureOffset) {
	   return value =  value + Math.round(pressureOffset * 100) / 100
	} else {
       return value
    }   
}

/*
    --END-- TEMPERATURE HUMIDITY METHODS (helpers-temperature-humidity)
*/