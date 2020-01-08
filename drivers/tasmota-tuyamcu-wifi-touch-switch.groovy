#!include:getHeaderLicense()

#!include:getDefaultImports()

metadata {
	definition (name: "Tasmota - TuyaMCU Wifi Touch Switch", namespace: "tasmota", author: "Markus Liljergren", vid: "generic-switch") {
        capability "Actuator"
        capability "Light"
        capability "Switch"
		capability "Sensor"
        
        #!include:getDefaultMetadataCapabilities()
        
        //attribute   "checkInterval", "number"
        attribute   "tuyaMCU", "string"
        #!include:getDefaultMetadataAttributes()

        #!include:getMetadataCommandsForHandlingChildDevices()
        #!include:getDefaultMetadataCommands()
	}

	simulator {
	}
    
    preferences {
        #!include:getDefaultMetadataPreferences()
        #!include:getDefaultMetadataPreferencesForParentDevices()
        #!include:getDefaultMetadataPreferencesForTasmota(False) # False = No TelePeriod setting
	}
}

#!include:getDeviceInfoFunction()

/* These functions are unique to each driver */
def installedAdditional() {
    // This runs from installed()
	logging("installedAdditional()",50)
    createChildDevices()
}

def on() {
	logging("on()",50)
    //logging("device.namespace: ${getDeviceInfoByName('namespace')}, device.driverName: ${getDeviceInfoByName('name')}", 50)
    def cmds = []
    // Power0 doesn't work correctly for Tuya devices yet
    //cmds << getAction(getCommandString("Power0", "1"))
    Integer numSwitchesI = numSwitches.toInteger()
    
    for (i in 1..numSwitchesI) {
        cmds << getAction(getCommandString("Power$i", "1"))
    }
    //return delayBetween(cmds, 500)
    return cmds
}

def off() {
    logging("off()",50)
    def cmds = []
    // Power0 doesn't work correctly for Tuya devices yet
    //cmds << getAction(getCommandString("Power0", "0"))
    Integer numSwitchesI = numSwitches.toInteger()
    
    for (i in 1..numSwitchesI) {
        cmds << getAction(getCommandString("Power$i", "0"))
    }
    //return delayBetween(cmds, 500)
    return cmds
}

def parse(description) {
    #!include:getGenericTasmotaParseHeader()
            #!include:getTasmotaParserForBasicData()
            #!include:getTasmotaParserForParentSwitch()
            #!include:getTasmotaParserForWifi()
        #!include:getGenericTasmotaParseFooter()
}

def update_needed_settings()
{
    #!include:getUpdateNeededSettingsTasmotaHeader()

    #!include:getUpdateNeededSettingsTasmotaDynamicModuleCommand(54)

    // Update the TuyaMCU device with the correct number of switches
    cmds << getAction(getCommandString("TuyaMCU", null))
    if(device.currentValue('tuyaMCU') != null) {
        tuyaMCU = device.currentValue('tuyaMCU')
        logging("Got this tuyaMCU string ${tuyaMCU}",1)
        Integer numSwitchesI = numSwitches.toInteger()
    
        for (i in 1..numSwitchesI) {
            if(tuyaMCU.indexOf("1$i") == -1) {
                // Only send commands for missing buttons
                cmds << getAction(getCommandString("TuyaMCU", "1$i,$i"))
            } else {
                logging("Already have button $i",10)
            }
        }
        //Remove buttons we don't have
        if (numSwitchesI < 4) {
            n = numSwitchesI + 1
            for (i in n..4) {
                if(tuyaMCU.indexOf("1$i") != -1) {
                    // Only send commands for buttons we have
                    cmds << getAction(getCommandString("TuyaMCU", "1$i,0"))
                } else {
                    logging("Button $i already doesn't exist, just as expected...",10)
                }
            }
        }
    }
    
    //
    // https://github.com/arendst/Tasmota/wiki/commands
    //SetOption66
    //Set publishing TuyaReceived to MQTT  »6.7.0
    //0 = disable publishing TuyaReceived over MQTT (default)
    //1 = enable publishing TuyaReceived over MQTT
    //cmds << getAction(getCommandString("SetOption66", "1"))

    cmds << getAction(getCommandString("SetOption81", "0")) // Set PCF8574 component behavior for all ports as inverted (default=0)

    // Make sure we have our child devices
    recreateChildDevices()

    #!include:getUpdateNeededSettingsTasmotaFooter()
}

#!include:getDefaultFunctions()

#!include:getGetChildDriverNameMethod()

#!include:getLoggingFunction()

#!include:getHelperFunctions('default')

#!include:getHelperFunctions('childDevices')

#!include:getHelperFunctions('tasmota')
