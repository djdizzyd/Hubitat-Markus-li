#!include:getHeaderLicense()

#!include:getDefaultImports()

metadata {
	definition (name: "Tasmota - Generic Temperature/Humidity/Pressure Device", namespace: "tasmota", author: "Markus Liljergren", vid: "generic-switch") {
        capability "Actuator"
        capability "Light"
		capability "Switch"
		capability "Sensor"

        #!include:getDefaultMetadataCapabilitiesForTHMonitor()
        #!include:getDefaultMetadataCapabilities()
        
        #!include:getDefaultMetadataAttributesForTHMonitor()
        #!include:getDefaultMetadataAttributes()
        #!include:getDefaultMetadataCommands()
	}

	simulator {
	}
    
    preferences {
        #!include:getDefaultMetadataPreferences()
        #!include:getDefaultMetadataPreferencesForTHMonitor()
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
            #!include:getTasmotaParserForTHMonitor()
        #!include:getGenericTasmotaParseFooter()
}

def updateNeededSettings()
{
    #!include:getUpdateNeededSettingsTasmotaHeader()

    // Unless the User sets a Module or Template, we won't touch the Tasmota settings in this driver
    #!include:getUpdateNeededSettingsTasmotaDynamicModuleCommand()  # Default is -1 for module and '' for the Template, ie no change to Tasmota

    //cmds << getAction(getCommandString("SetOption81", "1")) // Set PCF8574 component behavior for all ports as inverted (default=0)
    //cmds << getAction(getCommandString("LedPower", "1"))  // 1 = turn LED ON and set LedState 8
    //cmds << getAction(getCommandString("LedState", "8"))  // 8 = LED on when Wi-Fi and MQTT are connected.
    
    #!include:getUpdateNeededSettingsTelePeriod()
    #!include:getUpdateNeededSettingsTHMonitor()
    
    #!include:getUpdateNeededSettingsTasmotaFooter()
}

#!include:getDefaultFunctions()

#!include:getLoggingFunction()

#!include:getHelperFunctions('default')

#!include:getHelperFunctions('tasmota')

#!include:getHelperFunctions('temperature-humidity')
