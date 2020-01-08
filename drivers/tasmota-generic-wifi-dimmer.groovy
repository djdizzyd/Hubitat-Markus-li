#!include:getHeaderLicense()

#!include:getDefaultImports()

metadata {
	definition (name: "Tasmota - Generic Wifi Dimmer", namespace: "tasmota", author: "Markus Liljergren", vid: "generic-switch") {
        capability "Light"
        capability "Switch"
		capability "SwitchLevel"
        #!include:getDefaultMetadataCapabilities()
        
        attribute   "dimState", "number"
        #!include:getDefaultMetadataAttributes()

        //#!include:getMetadataCommandsForHandlingChildDevices()
        #!include:getDefaultMetadataCommands()
	}

	simulator {
	}
    
    preferences {
        #!include:getDefaultMetadataPreferences()
        //input(name: "numSwitches", type: "enum", title: "<b>Number of Switches</b>", description: "<i>Set the number of buttons on the switch (default 1)</i>", options: ["1", "2", "3", "4"], defaultValue: "1", displayDuringSetup: true, required: true)
        //input(name: "lowLevel", type: "string", title: "<b>Dimming Range (low)</b>", description: '<i>Used to calibrate the MINIMUM dimming level, see <a href="https://tasmota.github.io/docs/#/TuyaMCU?id=dimmers">here</a> for details.</i>', displayDuringSetup: true, required: false)
        //input(name: "highLevel", type: "string", title: "<b>Dimming Range (high)</b>", description: '<i>Used to calibrate the MINIMUM dimming level, see <a href="https://tasmota.github.io/docs/#/TuyaMCU?id=dimmers">here</a> for details.</i>', displayDuringSetup: true, required: false)
        #!include:getDefaultMetadataPreferencesForTasmota(False) # False = No TelePeriod setting
	}
}

#!include:getDeviceInfoFunction()

/* These functions are unique to each driver */
def installedAdditional() {
    // This runs from installed()
	logging("installedAdditional()",50)
}

def on() {
	logging("on()",50)
    def cmds = []
    cmds << getAction(getCommandString("Power", "1"))
    return cmds
}

def off() {
    logging("off()",50)
    def cmds = []
    cmds << getAction(getCommandString("Power", "0"))
    return cmds
}

def setLevel(l) {
    return(setLevel(l, 0))
}

def setLevel(l, duration) {
    logging("setLevel(l=$l, duration=$duration)",50)
    return(getAction(getCommandString("Dimmer", "$l")))
}

def parse(description) {
    #!include:getGenericTasmotaParseHeader()
            #!include:getTasmotaParserForBasicData()
            #!include:getTasmotaParserForWifi()
            if (result.containsKey("Dimmer")) {
                events << createEvent(name: "level", value: result.Dimmer)
            }
        #!include:getGenericTasmotaParseFooter()
}

def update_needed_settings()
{
    #!include:getUpdateNeededSettingsTasmotaHeader()

    #!include:getUpdateNeededSettingsTasmotaDynamicModuleCommand()

    #!include:getUpdateNeededSettingsTasmotaFooter()
}

#!include:getDefaultFunctions()

#!include:getLoggingFunction()

#!include:getHelperFunctions('default')

#!include:getHelperFunctions('tasmota')
