#!include:getHeaderLicense()

#!include:getDefaultImports()

metadata {
	definition (name: "Tasmota - DO NOT USE Sonoff RF Bridge (Parent)", namespace: "tasmota", author: "Markus Liljergren", vid: "generic-switch") {
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
        #!include:getDefaultMetadataPreferences()
        input(name: "b1Code", type: "string", title: "<b>B1 code (received)</b>", description: "<i>Set this to a B1 code and save and the driver will calculate the B0 code.</i>", displayDuringSetup: true, required: false)
        input(name: "b0Code", type: "string", title: "<b>B0 code (command)</b>", description: "<i>Set this to a B0 code or input a B1 code and this will be calculated!</i>", displayDuringSetup: true, required: false)
        input(name: "rfRawMode", type: "bool", title: "<b>RF Raw Mode</b>", description: '<i>Set RF mode to RAW, only works with <a target="portisch" href="https://tasmota.github.io/docs/#/devices/Sonoff-RF-Bridge-433?id=rf-firmware">Portisch</a>. MAY be slower than Standard RF mode, but can handle more signals.</i>', displayDuringSetup: true, required: false)
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
                }
            }
            if (result.containsKey("RfRaw")) {
                logging("RfRaw: $result.RfRaw", 100)
                if (!(result.RfRaw instanceof String) && result.RfRaw.containsKey("Data")) {
                    rawData = result.RfRaw.Data.toString()
                    
                    if(rawData.substring(3,5) != 'B1' && rawData != "AAA055") {
                        // We have RAW data and it is NOT B1 data, fix it:
                        logging("Incorrect RAW mode, fixing it now...", 100) 
                        events << getAction(getCommandString("rfraw", "177"))
                    }
                    // Save CPU:
                    if (logLevel == "100" && rawData.substring(3,5) == 'B1' ) {
                        logging("Calculated B0: ${calculateB0(rawData, 0).replace(' ', '')}", 100)
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

def calculateB0(inputStr, repeats) {
    // This calculates the B0 value from the B1 for use with the Sonoff RF Bridge
    logging('inputStr: ' + inputStr, 0)
    inputStr = inputStr.replace(' ', '')
    //logging('inputStr.substring(4,6): ' + inputStr.substring(4,6), 0)
    numBuckets = Integer.parseInt(inputStr.substring(4,6), 16)
    buckets = []

    logging('numBuckets: ' + numBuckets.toString(), 0)

    outAux = String.format(' %02X ', numBuckets.toInteger())
    outAux = outAux + String.format(' %02X ', repeats.toInteger())
    
    logging('outAux1: ' + outAux, 0)
    
    j = 0
    for(i in (0..numBuckets-1)){
        outAux = outAux + inputStr.substring(6+i*4,10+i*4) + " "
        j = i
    }
    logging('outAux2: ' + outAux, 0)
    outAux = outAux + inputStr.substring(10+j*4, inputStr.length()-2)
    logging('outAux3: ' + outAux, 0)

    dataStr = outAux.replace(' ', '')
    outAux = outAux + ' 55'
    length = (dataStr.length() / 2).toInteger()
    outAux = "AA B0 " + String.format(' %02X ', length.toInteger()) + outAux
    logging('outAux4: ' + outAux, 0)
    logging('outAux: ' + outAux.replace(' ', ''), 10)

    return(outAux)
}

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
