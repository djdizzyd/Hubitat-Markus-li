#!include:getHeaderLicense()

#!include:getDefaultImports()

metadata {
	definition (name: "Tasmota - Generic Wifi Switch/Light", namespace: "tasmota", author: "Markus Liljergren", vid: "generic-switch") {
        capability "Actuator"
        capability "Light"
		capability "Switch"
        capability "PushableButton"
		capability "Sensor"

        #!include:getDefaultMetadataCapabilities()
        
        #!include:getDefaultMetadataAttributes()
        #!include:getDefaultMetadataCommands()
	}

	simulator {
	}
    
    preferences {
        #!include:getDefaultMetadataPreferences()
        #!include:getDefaultMetadataPreferencesForTasmota(False) # False = No TelePeriod setting, True is default
	}
}

#!include:getDeviceInfoFunction()

#!include:getGenericOnOffFunctions()

/* These functions are unique to each driver */
def parse(description) {
    #!include:getGenericTasmotaParseHeader()
            #!include:getTasmotaParserForBasicData()
            if (result.containsKey("Var1")) {
                theButton = result.Var1
                logging("Button: $result.Var1",99)
                try {
                    events << createEvent(name: "pushed", value: Integer.parseInt(theButton), isStateChange: true )
                } catch (e) { }
            }
            #!include:getTasmotaParserForWifi()
        #!include:getGenericTasmotaParseFooter()
}

def updateNeededSettings()
{
    #!include:getUpdateNeededSettingsTasmotaHeader()

    #!include:getUpdateNeededSettingsTasmotaDynamicModuleCommand()

    // Disabling these here, but leaving them if anyone needs them
    // If another driver has set SetOption81 to 1, the below might be needed, or you can use:
    // http://<device IP>/cm?user=admin&password=<your password>&cmnd=SetOption81%200
    // or without username and password:
    // http://<device IP>/cm?cmnd=SetOption81%200
    //cmds << getAction(getCommandString("SetOption81", "0")) // Set PCF8574 component behavior for all ports as inverted (default=0)
    //cmds << getAction(getCommandString("LedPower", "1"))  // 1 = turn LED ON and set LedState 8
    //mds << getAction(getCommandString("LedState", "8"))  // 8 = LED on when Wi-Fi and MQTT are connected.
    
    #!include:getUpdateNeededSettingsTelePeriod()
    
    #!include:getUpdateNeededSettingsTasmotaFooter()
}

#!include:getDefaultFunctions()

#!include:getLoggingFunction()

#!include:getHelperFunctions('default')

#!include:getHelperFunctions('tasmota')
