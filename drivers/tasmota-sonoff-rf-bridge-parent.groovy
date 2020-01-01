#!include:getHeaderLicense()

#!include:getDefaultImports()

metadata {
	definition (name: "Tasmota - Sonoff RF Bridge (Parent)", namespace: "tasmota", author: "Markus Liljergren", vid: "generic-switch") {
        capability "Actuator"
		capability "Switch"
		capability "Sensor"

        #!include:getDefaultMetadataCapabilitiesForEnergyMonitor()
        #!include:getDefaultMetadataCapabilities()
        
        #!include:getDefaultMetadataAttributesForEnergyMonitor()
        attribute   "b0Code", "string"
        #!include:getDefaultMetadataAttributes()
        #!include:getMetadataCommandsForHandlingChildDevices()
        #!include:getDefaultMetadataCommands()
	}

	simulator {
	}
    
    preferences {
        input(name: "doNothing", description: "<i>This driver/device could be considered COMPLICATED, read about it in the <a href=\"https://community.hubitat.com/t/release-tasmota-7-x-firmware-with-hubitat-support/29368\"> Hubitat Forum</a>. Be sure to understand how this driver works, read everything, then ask if still not clear.</i>", title: "<b>Settings</b>", displayDuringSetup: false, type: "paragraph", element: "paragraph")
        #!include:getDefaultMetadataPreferences()

        // Device specific preferences
        input(name: "b1Code", type: "string", title: "<b>B1 code (received)</b>", description: "<i>Set this to a B1 code and save and the driver will calculate the B0 code.</i>", displayDuringSetup: true, required: false)
        input(name: "b0Code", type: "string", title: "<b>B0 code (command)</b>", description: "<i>Set this to a B0 code or input a B1 code and this will be calculated!</i>", displayDuringSetup: true, required: false)
        input(name: "rfRawMode", type: "bool", title: "<b>RF Raw Mode</b>", description: '<i>Set RF mode to RAW, only works with <a target="portisch" href="https://tasmota.github.io/docs/#/devices/Sonoff-RF-Bridge-433?id=rf-firmware">Portisch</a>. MAY be slower than Standard RF mode, but can handle more signals. Using the Portisch firmware is ALWAYS NOTICABLY faster than the original firmware, but RAW mode might be slower. With non-raw mode the risk is that you will miss events. DO NOT CHANGE THIS once you have paired child devices, they will stop working!</i>', displayDuringSetup: true, required: false)

        #!include:getDefaultMetadataPreferencesForParentDevicesWithUnlimitedChildren(numSwitches=1)
        #!include:getDefaultMetadataPreferencesForTasmota(True) # False = No TelePeriod setting, True is default
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
    cmds << getAction(getCommandString("Power0", "1"))
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
    
    cmds << getAction(getCommandString("Power0", "0"))
    Integer numSwitchesI = numSwitches.toInteger()
    
    for (i in 1..numSwitchesI) {
        cmds << getAction(getCommandString("Power$i", "0"))
    }
    //return delayBetween(cmds, 500)
    return cmds
}

def updateRFMode() {
    def cmds = []
    cmds << getAction(getCommandString("seriallog", "0"))
    if(rfRawMode == true) {
        logging("Switching to RAW RF mode...", 100)
        cmds << getAction(getCommandString("rfraw", "177"))
    } else {
        logging("Switching to Standard RF mode...", 100)
        cmds << getAction(getCommandString("rfraw", "0"))
    }
    return cmds
}

def getRawDataMain(rawData) {
    rawDataSplit = rawData.split()
    numBytes = Integer.parseInt(rawDataSplit[2], 16)
    // We only want this section, since it is what identifies each signal
    return rawDataSplit[ 3+numBytes..-2 ].join('')
}

def parse(description) {
    #!include:getGenericTasmotaParseHeader()
            #!include:getTasmotaParserForBasicData()
            if (result.containsKey("RestartReason")) {
                events << updateRFMode()
            }
            if (result.containsKey("RfReceived")) {
                logging("RfReceived: $result.RfReceived", 100)
                if(rfRawMode == true) {
                    logging("Switching to RAW RF mode...", 100)
                    events << getAction(getCommandString("rfraw", "177"))
                } else {
                    result.RfReceived.type = 'parsed_portisch'
                    events << sendParseEventToChildren(result.RfReceived)
                }
            }
            if (result.containsKey("RfRaw")) {
                logging("RfRaw: $result.RfRaw", 100)
                
                if (!(result.RfRaw instanceof String) && result.RfRaw.containsKey("Data")) {
                    rawData = result.RfRaw.Data.toString()
                    childData = [:]
                    if(rawData.substring(3,5) != 'B1' && rawData != "AAA055") {
                        // We have RAW data and it is NOT B1 data, fix it:
                        logging("Incorrect RAW mode, fixing it now...", 100) 
                        events << getAction(getCommandString("rfraw", "177"))
                    } 
                    if(rawData.substring(3,5) == 'B1') {
                        childData['type'] = 'raw_portisch'
                        childData['b1'] = rawData
                        // We always need to include 'Data' as well
                        childData['Data'] = getRawDataMain(rawData)
                    }
                    // Save CPU, only run this if needed:
                    if (logLevel == "100" && rawData.substring(3,5) == 'B1' ) {
                        b0 = calculateB0(rawData, 0)
                        childData['b0'] = b0
                        logging("Calculated B0: ${b0.replace(' ', '')}", 100)
                    }
                    logging("childData: '${childData}'", 100)
                    if (childData) {
                        events << sendParseEventToChildren(childData)
                    }
                }
                if(rfRawMode != true) {
                    logging("Switching to Standard RF mode...", 100)
                    events << getAction(getCommandString("rfraw", "0"))
                }
            }
            #!include:getTasmotaParserForWifi()
            #!include:getTasmotaParserForParentSwitch()
            #!include:getTasmotaParserForEnergyMonitor()
        #!include:getGenericTasmotaParseFooter()
}

#!include:getCalculateB0()

def update_needed_settings()
{
    #!include:getUpdateNeededSettingsTasmotaHeader()

    /*logging('Just saved...', 10)
    if((b0Code == null || b0Code == '') && b1Code != null && b1Code != '') {
        b0CodeTmp = calculateB0(b1Code, 0)
        b0Code = b0CodeTmp.replace(' ', '')
        sendEvent(name: "b0Code", value: b0CodeTmp)
        state.b0Code = b0Code
        logging('Calculated b0Code! ', 10)
    }*/
    
    // Don't send any other commands until AFTER setting the correct Module/Template
    #!include:getUpdateNeededSettingsTasmotaDynamicModuleCommand(0,'{"NAME":"Sonoff Bridge","GPIO":[17,148,255,149,255,255,0,0,255,56,255,0,0],"FLAG":0,"BASE":25}')

    //cmds << getAction(getCommandString("SetOption81", "1")) // Set PCF8574 component behavior for all ports as inverted (default=0)
    //cmds << getAction(getCommandString("LedPower", "1"))  // 1 = turn LED ON and set LedState 8
    //cmds << getAction(getCommandString("LedState", "8"))  // 8 = LED on when Wi-Fi and MQTT are connected.
    
    #!include:getUpdateNeededSettingsTelePeriod()

    // Don't send these types of commands until AFTER setting the correct Module/Template
    cmds << updateRFMode()

    #!include:getUpdateNeededSettingsTasmotaFooter()
}

#!include:getDefaultFunctions()

#!include:getLoggingFunction(specialDebugLevel=True)

#!include:getHelperFunctions('default')

#!include:getHelperFunctions('childDevices')

#!include:getHelperFunctions('tasmota')
