#!include:getHeaderLicense()

#!include:getDefaultImports()

metadata {
	definition (name: "Tasmota - Brilliant BL20925 Power Monitor Plug", namespace: "tasmota", author: "Markus Liljergren", vid: "generic-switch") {
        capability "Actuator"
		capability "Switch"
		capability "Sensor"

        #!include:getDefaultMetadataCapabilitiesForEnergyMonitor()
        #!include:getDefaultMetadataCapabilities()
        
        #!include:getDefaultMetadataAttributesForEnergyMonitor()
        #!include:getDefaultMetadataAttributes()
        #!include:getDefaultMetadataCommands()
	}

	simulator {
	}
    
    preferences {
        #!include:getDefaultMetadataPreferences()
        #!include:getDefaultMetadataPreferencesForTasmota(True) # False = No TelePeriod setting, True is default
	}
}

#!include:getDeviceInfoFunction()

#!include:getGenericOnOffFunctions()

/* These functions are unique to each driver */
def parse(description) {
    #!include:getGenericTasmotaParseHeader()
            #!include:getTasmotaParserForBasicData()
            #!include:getTasmotaParserForWifi()
            #!include:getTasmotaParserForEnergyMonitor()
        #!include:getGenericTasmotaParseFooter()
}

def update_needed_settings()
{
    #!include:getUpdateNeededSettingsTasmotaHeader()

    // https://blakadder.github.io/templates/brilliant_BL20925.html
    #!include:getUpdateNeededSettingsTasmotaDynamicModuleCommand(0, '{"NAME":"BL20925","GPIO":[0,0,0,17,133,132,0,0,131,158,21,0,0],"FLAG":0,"BASE":52}')

    cmds << getAction(getCommandString("SetOption81", "0")) // Set PCF8574 component behavior for all ports as inverted (default=0)
    //cmds << getAction(getCommandString("LedPower", "1"))  // 1 = turn LED ON and set LedState 8
    //cmds << getAction(getCommandString("LedState", "8"))  // 8 = LED on when Wi-Fi and MQTT are connected.
    
    #!include:getUpdateNeededSettingsTelePeriod()
    
    #!include:getUpdateNeededSettingsTasmotaFooter()
}

#!include:getDefaultFunctions()

#!include:getLoggingFunction()

#!include:getHelperFunctions('default')

#!include:getHelperFunctions('tasmota')
