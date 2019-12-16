#!include:getHeaderLicense()

#!include:getDefaultImports()

metadata {
	definition (name: "Tasmota - Generic Wifi Switch", namespace: "tasmota", author: "Markus Liljergren", vid:"generic-switch") {
        capability "Actuator"
		capability "Switch"
		capability "Sensor"

        #!include:getDefaultMetadataCapabilities()
        
        #!include:getDefaultMetadataAttributes()
        #!include:getDefaultMetadataCommands()
	}

	simulator {
	}
    
    preferences {
        #!include:getDefaultMetadataPreferences()
        input(name: "moduleNumber", type: "number", title: "Module Number", description: "Module Number used in Tasmota (default: 0)", displayDuringSetup: true, required: false, defaultValue: 0)
        input(name: "deviceTemplateInput", type: "string", title: "Device Template", description: "ADVANCED: Set this to a Device Template for Tasmota, leave it EMPTY to NOT set a template. (Example: {\"NAME\":\"S120\",\"GPIO\":[0,0,0,0,0,21,0,0,0,52,90,0,0],\"FLAG\":0,\"BASE\":18})", displayDuringSetup: true, required: false)
        #!include:getDefaultMetadataPreferencesForTasmota(False) # False = No TelePeriod setting, True is default
	}
}

#!include:getDeviceInfoFunction()

/* These functions are unique to each driver */
def on() {
	logging("on()", 50)
    def cmds = []
    cmds << getAction(getCommandString("Power", "On"))
    return cmds
}

def off() {
    logging("off()", 50)
	def cmds = []
    cmds << getAction(getCommandString("Power", "Off"))
    return cmds
}

def parse(description) {
    #!include:getGenericTasmotaParseHeader()
            #!include:getTasmotaParserForBasicData()
            #!include:getTasmotaParserForWifi()
        #!include:getGenericTasmotaParseFooter()
}

def update_needed_settings()
{
    #!include:getUpdateNeededSettingsTasmotaHeader()

    #!include:getUpdateNeededSettingsTasmotaDynamicModuleCommand()

    //Disabling these here, but leacing them if anyone needs them
    //cmds << getAction(getCommandString("SetOption81", "1")) // Set PCF8574 component behavior for all ports as inverted
    //cmds << getAction(getCommandString("LedPower", "1"))  // 1 = turn LED ON and set LedState 8
    //mds << getAction(getCommandString("LedState", "8"))  // 8 = LED on when Wi-Fi and MQTT are connected.
    
    #!include:getUpdateNeededSettingsTelePeriod()
    
    #!include:getUpdateNeededSettingsTasmotaFooter()
}

#!include:getLoggingFunction()

#!include:getHelperFunctions('default')

#!include:getHelperFunctions('tasmota')
