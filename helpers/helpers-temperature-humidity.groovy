/* Helper functions included in all drivers with Temperature and Humidity */
private getAdjustedTemp(value) {
    value = Math.round((value as Double) * 100) / 100

	if (tempOffset) {
	   return value =  value + Math.round(tempOffset * 100) /100
	} else {
       return value
    }
    
}

private getAdjustedHumidity(value) {
    value = Math.round((value as Double) * 100) / 100

	if (humidityOffset) {
	   return value =  value + Math.round(humidityOffset * 100) /100
	} else {
       return value
    }
    
}