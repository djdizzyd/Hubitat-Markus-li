#!include:getHeaderLicense()

/* Acknowledgements:
 * Inspired by work done by Eric Maycock (erocm123) and damondins.
 */


#!include:getDefaultImports()

metadata {
	definition (name: "Tasmota - Unbranded RGB Controller with IR", namespace: "tasmota", author: "Markus Liljergren", vid: "generic-switch") {
        capability "Actuator"
		capability "Light"
        capability "Switch"
		capability "ColorControl"
        capability "ColorTemperature"
        capability "SwitchLevel"
        #!include:getDefaultMetadataCapabilities()
        
        #!include:getDefaultMetadataAttributes()

        #!include:getDefaultMetadataCommands()
        #!include:getMetadataCommandsForHandlingRGBWDevices()
        #!include:getMetadataCommandsForHandlingTasmotaRGBWDevices()
	}

	simulator {
	}
    
    preferences {
        #!include:getDefaultMetadataPreferences()
        #!include:getDefaultMetadataPreferencesForTasmota(True) # False = No TelePeriod setting, True is default
	}
}

#!include:getDeviceInfoFunction()

#!include:getRGBWOnOffFunctions()

/* These functions are unique to each driver */
def parse(description) {
    #!include:getGenericTasmotaParseHeader()
            #!include:getTasmotaParserForBasicData()
            #!include:getTasmotaParserForWifi()
            #!include:getTasmotaParserForRGBWIRRemote()
            #!include:getTasmotaParserForRGBWDevice()
        #!include:getGenericTasmotaParseFooter()
}

def update_needed_settings() {
    #!include:getUpdateNeededSettingsTasmotaHeader()

    #!include:getUpdateNeededSettingsTasmotaDynamicModuleCommand(0, '{"NAME":"RGB Controller","GPIO":[0,0,0,0,0,38,0,0,39,51,0,37,0],"FLAG":15,"BASE":18}')

    // Disabling these here, but leaving them if anyone needs them
    // If another driver has set SetOption81 to 1, the below might be needed, or you can use:
    // http://<device IP>/cm?user=admin&password=<your password>&cmnd=SetOption81%200
    // or without username and password:
    // http://<device IP>/cm?cmnd=SetOption81%200
    //cmds << getAction(getCommandString("SetOption81", "0")) // Set PCF8574 component behavior for all ports as inverted (default=0)
    //cmds << getAction(getCommandString("LedPower", "1"))  // 1 = turn LED ON and set LedState 8
    //cmds << getAction(getCommandString("LedState", "8"))  // 8 = LED on when Wi-Fi and MQTT are connected.
    
    cmds << getAction(getCommandString("WebLog", "2")) // To avoid errors in the Hubitat logs, make sure this is 2

    #!include:getUpdateNeededSettingsTelePeriod()
    
    #!include:getUpdateNeededSettingsTasmotaFooter()
}

#!include:getDefaultFunctions()

#!include:getLoggingFunction()

#!include:getHelperFunctions('default')

#!include:getHelperFunctions('tasmota')

#!include:getHelperFunctions('rgbw')

#!include:getHelperFunctions('tasmota-rgbw')
