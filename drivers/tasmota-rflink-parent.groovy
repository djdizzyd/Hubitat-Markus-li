#!include:getHeaderLicense()

#!include:getDefaultImports()

metadata {
	definition (name: "Tasmota - RFLink (Parent)", namespace: "tasmota", author: "Markus Liljergren", vid: "generic-switch") {
        capability "Actuator"
		capability "Switch"
		capability "Sensor"

        #!include:getDefaultMetadataCapabilitiesForEnergyMonitor()
        #!include:getDefaultMetadataCapabilities()
        
        #!include:getDefaultMetadataAttributesForEnergyMonitor()
        //attribute   "b0Code", "string"
        #!include:getDefaultMetadataAttributes()
        #!include:getMetadataCommandsForHandlingChildDevices()
        #!include:getDefaultMetadataCommands()
	}

	simulator {
	}
    
    preferences {
        #!include:getDefaultMetadataPreferences()
        //input(name: "b1Code", type: "string", title: "<b>B1 code (received)</b>", description: "<i>Set this to a B1 code and save and the driver will calculate the B0 code.</i>", displayDuringSetup: true, required: false)
        //input(name: "b0Code", type: "string", title: "<b>B0 code (command)</b>", description: "<i>Set this to a B0 code or input a B1 code and this will be calculated!</i>", displayDuringSetup: true, required: false)
        //input(name: "rfRawMode", type: "bool", title: "<b>RF Raw Mode</b>", description: '<i>Set RF mode to RAW, only works with <a target="portisch" href="https://tasmota.github.io/docs/#/devices/Sonoff-RF-Bridge-433?id=rf-firmware">Portisch</a>. MAY be slower than Standard RF mode, but can handle more signals.</i>', displayDuringSetup: true, required: false)
        #!include:getDefaultMetadataPreferencesForParentDevicesWithUnlimitedChildren(numSwitches=1)
        #!include:getDefaultMetadataPreferencesForTasmota(False) # False = No TelePeriod setting, True is default
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
    // If this is called from somewhere it could be repeatedly called, rate-limit this! See RF Bridge for an example
    def cmds = []
    logging("Initializing RFLink...", 100)
    cmds << getAction(getCommandString("Baudrate", "57600"))
    cmds << getAction(getCommandString("SerialSend1", "10;PING;"))
    return cmds
}

def refreshAdditional() {
	logging("refreshAdditional()", 10)
    def cmds = []
    cmds << getAction(getCommandString("SerialSend1", "10;PING;"))
    return cmds
}

def splitRFLinkData(rawRFLinkData) {
    parsedData = []
    rawRFLinkData.split('20;').each { cmd ->
        if(cmd != null && cmd != '') {
            c = cmd.split(';')
            r = c[2..(c.size()-1)].inject([:]) { map, token ->
                token.split('=').with {
                    try {
                        map[it[0].trim()] = it[1].trim() 
                    } catch(ArrayIndexOutOfBoundsException ex) {
                        // Do nothing, we don't want these values...
                    }
                } 
                map
            }
            
            d =['counter': c[0], 'etype': c[1]]
            d << r
            parsedData << d
        }
    }
    return parsedData
}

def makeRFLinkDataString(splitRFLinkData) {
    childData = ''
    splitRFLinkData.each { 
        if(it.key != 'counter') {
            childData = childData + "${it.key.toUpperCase()}=${it.value};"
        }
    }
    return(childData)
}

def parse(description) {
    #!include:getGenericTasmotaParseHeader()
            #!include:getTasmotaParserForBasicData()
            if (result.containsKey("RestartReason")) {
                events << updateRFMode()
            }
            // All commands received are separated by 20, then it's the counter (00 to FF, then it wraps around).
            // After that is the name of the encoding, after which key->data pairs come
            
            if (result.containsKey("SerialReceived")) {
                logging("SerialReceived: $result.SerialReceived", 100)
                splitRFLinkData = splitRFLinkData(result.SerialReceived)
                logging("Split RFLink Data: '${splitRFLinkData}'", 100)
                
                splitRFLinkData.each {
                    logging("it=${it}", 100)
                    if(it.containsKey('counter')) {
                        //if(it.containsKey('seen') && it['seen'] >= maxActionNumSeen) {
                        //    maxActionNumSeen = it['seen']
                        //    frequentData = it['data']
                        //}
                        it['Data'] = makeRFLinkDataString(it)
                        it['type'] = 'rflink'
                        logging("Split RFLink Data field: '${it['Data']}'", 100)
                        events << sendParseEventToChildren(it)
                    }
                }
            }
            #!include:getTasmotaParserForWifi()
            #!include:getTasmotaParserForParentSwitch()
            #!include:getTasmotaParserForEnergyMonitor()
        #!include:getGenericTasmotaParseFooter()
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
    #!include:getUpdateNeededSettingsTasmotaDynamicModuleCommand(0,'{"NAME":"RFLink Bridge","GPIO":[0,149,0,148,0,0,0,0,0,0,0,0,0],"FLAG":0,"BASE":18}')

    //cmds << getAction(getCommandString("SetOption81", "1")) // Set PCF8574 component behavior for all ports as inverted (default=0)
    //cmds << getAction(getCommandString("LedPower", "1"))  // 1 = turn LED ON and set LedState 8
    //cmds << getAction(getCommandString("LedState", "8"))  // 8 = LED on when Wi-Fi and MQTT are connected.
    
    #!include:getUpdateNeededSettingsTelePeriod(forcedTelePeriod=300)
    
    // Don't send these types of commands until AFTER setting the correct Module/Template
    cmds << updateRFMode()

    #!include:getUpdateNeededSettingsTasmotaFooter()
}

#!include:getDefaultFunctions()

#!include:getGetChildDriverNameMethod(childDriverName='Tasmota - RF/IR Switch/Toggle/Push')

#!include:getLoggingFunction(specialDebugLevel=True)

#!include:getHelperFunctions('default')

#!include:getHelperFunctions('childDevices')

#!include:getHelperFunctions('tasmota')
