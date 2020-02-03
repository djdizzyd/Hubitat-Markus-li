#!include:getHeaderLicense()

#!include:getDefaultImports()

metadata {
	definition (name: "Tasmota - ZNSN TuyaMCU Wifi Curtain Wall Panel", namespace: "tasmota", author: "Markus Liljergren", vid: "generic-switch") {
        capability "Light"
        capability "Switch"
        capability "WindowShade"
        #!include:getDefaultMetadataCapabilities()
        
        attribute   "dimState", "number"
        attribute   "tuyaMCU", "string"
        attribute   "position", "number"
        attribute   "target", "number"
        #!include:getDefaultMetadataAttributes()

        #!include:getDefaultMetadataCommands()
        command "stop"
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

def open() {
	logging("open()",50)
    def cmds = []
    cmds << getAction(getCommandString("TuyaSend4", "101,0"))
    return cmds
}

def stop() {
    logging("stop()",50)
    def cmds = []
    cmds << getAction(getCommandString("TuyaSend4", "101,1"))
    return cmds
}

def close() {
    logging("close()",50)
    def cmds = []
    cmds << getAction(getCommandString("TuyaSend4", "101,2"))
    return cmds
}

def setPosition(targetPosition) {
    position = device.latestValue("position", true)
    logging("targetPosition(targetPosition=${targetPosition}) current: ${position}",50)
    if(targetPosition > position + 10) {
        sendEvent(name: "target", value: targetPosition, isStateChange: true)
        close()
    } else if(targetPosition < position - 10) {
        sendEvent(name: "target", value: targetPosition, isStateChange: true)
        open()
    }
}

def parse(description) {
    #!include:getGenericTasmotaParseHeader()
            #!include:getTasmotaParserForBasicData()
            #!include:getTasmotaParserForWifi()
            if (result.containsKey("Dimmer")) {
                events << createEvent(name: "position", value: result.Dimmer)
                target = device.currentValue("target")
                
                if(target && target != -1 && target != null) {
                    logging("target: ${target}", 1)
                    cState = device.latestValue("windowShade", true)
                    if((cState == 'opening' && result.Dimmer < target + 5) ||
                       (cState == 'closing' && result.Dimmer > target - 5)) {
                        events << stop()
                    }
                }
            }
            if (result.containsKey("TuyaReceived")) {
                if (result.TuyaReceived.containsKey("Data")) {
                    tdata = result.TuyaReceived.Data
                    if(tdata == '55AA00070005020400010214') {
                        // Stop Event occured
                        position = device.latestValue("position", true)
                        events << createEvent(name: "target", value: -1, isStateChange: true)
                        margin = 11
                        if(position > margin && position < 100 - margin) {
                            logging('Curtain status: partially open', 50)
                            events << createEvent(name: "windowShade", value: "partially open", isStateChange: true)
                        } else if(position <= margin) {
                            logging('Curtain status: open', 50)
                            events << getAction(getCommandString("Dimmer", "0"))
                            events << createEvent(name: "windowShade", value: "open", isStateChange: true)
                        } else if(position >= 100 - margin) {
                            logging('Curtain status: closed', 50)
                            events << getAction(getCommandString("Dimmer", "100"))
                            events << createEvent(name: "windowShade", value: "closed", isStateChange: true)
                        }
                    } else if(tdata == '55AA00070005020400010012') {
                        // Open Event occured
                        logging('Curtain status: opening', 50)
                        events << createEvent(name: "windowShade", value: "opening", isStateChange: true)
                    } else if(tdata == '55AA00070005020400010113') {
                        // Close Event occured
                        logging('Curtain status: closing', 50)
                        events << createEvent(name: "windowShade", value: "closing", isStateChange: true)
                    }
                }
            }
        #!include:getGenericTasmotaParseFooter()
}

def updateRules() {
    logging("updateRules()",50)
    def cmds = []
    commands = [
        'Mem1': '100',  // Updated with the current Curtain location
        'Mem2': '11',   // Step for each increase
        'Mem3': '1',    // delay in 10th of a second (1 = 100ms)
        'Mem4': '9',    // Motor startup steps
        'Mem5': '1',    // Extra step when opening
        'Delay': '15',   // Set delay between Backlog commands
        'Rule1': 'ON Dimmer#State DO Mem1 %value%; ENDON',
        'Rule1': '+ ON TuyaReceived#Data=55AA00070005650400010277 DO Backlog Var1 %mem1%; Var2 Go; Var5 C; Add1 %mem2%; Sub1 %mem4%; Var4 %mem2%; Event Go; ENDON',
        'Rule1': '+ ON Event#Go DO Backlog Dimmer %var1%; Event %var5%%var1%; Event %var2%2; ENDON',
        'Rule1': '+ ON Event#Go2 DO Backlog Add1 %var4%; Delay %mem3%; Event %var1%; Event %var2%;  ENDON',
        'Rule1': '+ ON Event#O-7 DO Var2 sC; ENDON ON Event#O-8 DO Var2 sC; ENDON ON Event#O-9 DO Var2 sC; ENDON ON Event#O-10 DO Var2 sC; ENDON ON Event#O-11 DO Var2 sC; ENDON',
        'Rule1': '1',
        'Rule2': 'ON TuyaReceived#Data=55AA00070005650400010176 DO Backlog Var1 %mem1%; Var2 Go; Var5 O; Sub1 %mem2%; Add1 %mem4%; Var4 %mem2%; Add4 %mem5%; Mult4 -1; Event Go; ENDON',
        'Rule2': '+ ON Event#sC DO Backlog Var2 sC2; Event sC2; ENDON',
        'Rule2': '+ ON Event#sC2 DO Backlog Var2 sC2; TuyaSend4 101,1; ENDON',
        'Rule2': '+ ON TuyaReceived#Data=55AA00070005650400010075 DO Var2 sC3; ENDON',
        'Rule2': '+ ON Event#C107 DO Var2 sC; ENDON ON Event#C108 DO Var2 sC; ENDON ON Event#C109 DO Var2 sC; ENDON ON Event#C110 DO Var2 sC; END ON ON Event#C111 DO Var2 sC; ENDON',
        'Rule2': '1',
        'Rule3': 'ON Event#C100 DO Var2 sC; ENDON ON Event#C101 DO Var2 sC; ENDON ON Event#C102 DO Var2 sC; ENDON ON Event#C103 DO Var2 sC; ENDON ON Event#C104 DO Var2 sC; ENDON ON Event#C105 DO Var2 sC; ENDON ON Event#C106 DO Var2 sC; ENDON ON Event#O0 DO Var2 sC; ENDON ON Event#O-1 DO Var2 sC; ENDON ON Event#O-2 DO Var2 sC; ENDON ON Event#O-3 DO Var2 sC; ENDON ON Event#O-4 DO Var2 sC; ENDON ON Event#O-5 DO Var2 sC; ENDON ON Event#O-6 DO Var2 sC; ENDON ON Event#O-12 DO Var2 sC; ENDON',
        'Rule3': '1',
        ]
    for (command in commands) {
        cmds << getAction(getCommandString(command.key, command.value))
    }
    return cmds
}

def updateNeededSettings()
{
    #!include:getUpdateNeededSettingsTasmotaHeader()

    #!include:getUpdateNeededSettingsTasmotaDynamicModuleCommand(54)
    
    //
    // https://github.com/arendst/Tasmota/wiki/commands
    //SetOption66
    //Set publishing TuyaReceived to MQTT  »6.7.0
    //0 = disable publishing TuyaReceived over MQTT (default)
    //1 = enable publishing TuyaReceived over MQTT
    cmds << getAction(getCommandString("SetOption66", "1"))

    // Set all rules
    cmds << updateRules()

    //cmds << getAction(getCommandString("SetOption81", "0")) // Set PCF8574 component behavior for all ports as inverted (default=0)
    
    #!include:getUpdateNeededSettingsTasmotaFooter()
}

#!include:getDefaultFunctions()

#!include:getLoggingFunction()

#!include:getHelperFunctions('default')

#!include:getHelperFunctions('tasmota')
