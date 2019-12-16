#!include:getHeaderLicense()

#!include:getDefaultImports()

metadata {
	definition (name: "Tasmota - Sonoff PowR2", namespace: "tasmota", author: "Markus Liljergren", vid:"generic-switch") {
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
        #!include:getDefaultMetadataPreferencesForTasmota()
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
            #!include:getTasmotaParserForEnergyMonitor()
        #!include:getGenericTasmotaParseFooter()
}

def update_needed_settings()
{
    #!include:getUpdateNeededSettingsTasmotaHeader()

    cmds << getAction(getCommandString("SetOption81", "1"))
    cmds << getAction(getCommandString("LedPower", "1"))
    cmds << getAction(getCommandString("LedState", "8"))
    cmds << getAction(getCommandString("TelePeriod", (telePeriod == '' || telePeriod == null ? "300" : telePeriod)))
    
    #!include:getUpdateNeededSettingsTasmotaFooter()
}

#!include:getLoggingFunction()

#!include:getHelperFunctions('default')

#!include:getHelperFunctions('tasmota')
