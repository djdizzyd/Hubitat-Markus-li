#!include:getHeaderLicense()

#!include:getDefaultImports()

metadata {
	definition (name: "Tasmota - Generic Power Monitor Plug", namespace: "tasmota", author: "Markus Liljergren", vid: "generic-switch") {
        capability "Actuator"
        capability "Light"
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

def updateNeededSettings()
{
    #!include:getUpdateNeededSettingsTasmotaHeader()

    #!include:getUpdateNeededSettingsTasmotaDynamicModuleCommand()

    //cmds << getAction(getCommandString("SetOption81", "1")) // Set PCF8574 component behavior for all ports as inverted (default=0)
    //cmds << getAction(getCommandString("LedPower", "1"))  // 1 = turn LED ON and set LedState 8
    //cmds << getAction(getCommandString("LedState", "8"))  // 8 = LED on when Wi-Fi and MQTT are connected.
    
    #!include:getUpdateNeededSettingsTelePeriod()
    
    #!include:getUpdateNeededSettingsTasmotaFooter()
}

#!include:getDefaultFunctions()

#!include:getLoggingFunction()

#!include:getHelperFunctions('default')

#!include:getHelperFunctions('tasmota')
