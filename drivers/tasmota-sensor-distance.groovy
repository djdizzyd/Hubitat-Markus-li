#!include:getHeaderLicense()

#!include:getDefaultImports()

metadata {
	definition (name: "Tasmota - Sensor (Distance)", namespace: "tasmota", author: "Markus Liljergren", vid: "generic-switch") {
        capability "MotionSensor"
		capability "PresenceSensor"
        capability "Sensor"
        

        #!include:getDefaultMetadataCapabilities()
        
        #!include:getDefaultMetadataAttributes()
        
        // Device Specific Attributes
        attribute   "distance", "string"

        #!include:getDefaultMetadataCommands()
	}

	simulator {
	}
    
    preferences {
        #!include:getDefaultMetadataPreferences()
        input(name: "recoveryTime", type: "number", title: "<b>Recovery Time</b>", description: "<i>Set the number of seconds before returning to Inactive (default: 5)</i>", defaultValue: "5", displayDuringSetup: true, required: true)
        input(name: "decimals", type: "number", title: "<b>Decimals</b>", description: "<i>Set the maximum number of decimals after the '.' (default: 3)</i>", defaultValue: "3", displayDuringSetup: false, required: false)
        #!include:getDefaultMetadataPreferencesForTasmota(True) # False = No TelePeriod setting, True is default
	}
}

#!include:getDeviceInfoFunction()

#!include:getGenericOnOffFunctions()

/* These functions are unique to each driver */
def parse(description) {
    #!include:getGenericTasmotaParseHeader()
            #!include:getTasmotaParserForBasicData()
            if (result.containsKey("StatusSNS")) {
                result << result.StatusSNS
            }
            if (result.containsKey("SR04")) {
                if (result.SR04.containsKey("Distance")) {
                    value = result.SR04.Distance
                    
                    if(decimals == null || decimals == '' || decimals == 0) {
                        decimalLimit = 1
                    } else {
                        decimalLimit = 10**(decimals as Integer) // 10 to the power of tempRes
                    }
                    value = Math.round((value as Double) * decimalLimit) / decimalLimit
                    old_value = device.currentValue('distance') as Float
                    logging("Distance: $result.SR04.Distance (rounded: $value, old_value: $old_value)", 99)
                    if(value > old_value + 0.5 || value < old_value -0.5 ) {
                        events << createEvent(name: "distance", value: "$value")  
                    }
                }
            }
            #!include:getTasmotaParserForWifi()
        #!include:getGenericTasmotaParseFooter()
}

def update_needed_settings()
{
    #!include:getUpdateNeededSettingsTasmotaHeader()

    #!include:getUpdateNeededSettingsTasmotaDynamicModuleCommand(0, '{"NAME":"Generic","GPIO":[0,0,0,0,0,0,0,0,74,73,0,0,0],"FLAG":0,"BASE":18}')

    //cmds << getAction(getCommandString("SetOption81", "1")) // Set PCF8574 component behavior for all ports as inverted
    //cmds << getAction(getCommandString("LedPower", "1"))  // 1 = turn LED ON and set LedState 8
    //cmds << getAction(getCommandString("LedState", "8"))  // 8 = LED on when Wi-Fi and MQTT are connected.
    
    #!include:getUpdateNeededSettingsTelePeriod()
    
    #!include:getUpdateNeededSettingsTasmotaFooter()
}

#!include:getDefaultFunctions()

#!include:getLoggingFunction()

#!include:getHelperFunctions('default')

#!include:getHelperFunctions('tasmota')
