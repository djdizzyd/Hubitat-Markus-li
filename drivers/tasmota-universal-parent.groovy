#!include:getHeaderLicense()

#!include:getDefaultParentImports()

metadata {
	definition (name: "Tasmota - Universal Parent", namespace: "tasmota", author: "Markus Liljergren", vid: "generic-switch") {
        #!include:getDefaultMetadataCapabilities()
        capability "PresenceSensor"
        
        #!include:getDefaultParentMetadataAttributes()
        #!include:getDefaultMetadataAttributes()

        #!include:getMetadataCommandsForHandlingChildDevices()
        #!include:getDefaultMetadataCommands()
	}

	preferences {
        #!include:getDefaultParentMetadataPreferences()

        input(name: "deviceConfig", type: "enum", title: addTitleDiv("Device Configuration"), 
            description: addDescriptionDiv("Select a Device Configuration (default: Generic Device)<br/>'Generic Device' doesn't configure device Template and/or Module on Tasmota. Child devices and types are auto-detected as well as auto-created and does NOT depend on this setting."), 
            options: getDeviceConfigurationsAsListOption(), defaultValue: "01generic-device", 
            displayDuringSetup: true, required: false)

        #!include:getMetadataPreferencesForHiding()

        #!include:getDefaultMetadataPreferencesForTasmota(True) # False = No TelePeriod setting
        #!include:getDefaultMetadataPreferencesLast()
	}

    // The below line needs to exist in ALL drivers for custom CSS to work!
    #!include:getMetadataCustomizationMethods()
}

#!include:getDeviceInfoFunction()

#!include:getHelperFunctions('device-configurations')

/* These functions are unique to each driver */

// Called from installed()
def installedAdditional() {
    // This runs from installed()
	logging("installedAdditional()", 50)

    // Do NOT call updatedAdditional() form here!

    //createChildDevices()
}

// Called from updated()
def updatedAdditional() {
    logging("updatedAdditional()", 1)
    //Runs when saving settings
    setDisableCSS(disableCSS)
}

def getDriverCSS() {
    // Executed on page load, put CSS used by the driver here.
    
    // This does NOT execute in the NORMAL scope of the driver!

    r = ""
    // "Data" is available when this runs
    
    //r += getCSSForCommandsToHide(["deleteChildren"])
    //r += getCSSForCommandsToHide(["overSanta", "on", "off"])
    //r += getCSSForStateVariablesToHide(["settings", "mac"])
    //r += getCSSForStateVariablesToHide(["alertMessage", "mac", "dni", "oldLabel"])
    //r += getCSSForCurrentStatesToHide(["templateData", "tuyaMCU", "needUpdate"])
    //r += getCSSForDatasToHide(["metaConfig2", "preferences", "appReturn", "namespace"])
    //r += getCSSToChangeCommandTitle("configure", "Run Configure3")
    //r += getCSSForPreferencesToHide(["numSwitches", "deviceTemplateInput"])
    //r += getCSSForPreferenceHiding('<none>', overrideIndex=getPreferenceIndex('<none>', returnMax=true) + 1)
    //r += getCSSForHidingLastPreference()
    r += '''
    /*form[action*="preference"]::before {
        color: green;
        content: "Hi, this is my content"
    }
    form[action*="preference"] div[for^=preferences] {
        color: blue;
    }*/
    div#stateComment {
        display: inline;
    }
    /*div#stateComment:after {
        color: red;
        display: inline;
        visibility: visible;
        position: absolute;
        bottom: 150%;
        left: 400%;
        white-space: nowrap;
    }*/
    div#stateComment:after {
        color: #382e2b;
        visibility: visible;
        position: relative;
        white-space: nowrap;
        display: inline;
    }
    /*div#stateComment:after {
        color: #382e2b;
        display: inline;
        visibility: visible;
        position: fixed;
        left: 680px;
        white-space: nowrap;
        top: 95px;
    }*/
    /*
    div#stateComment:after {
        color: #5ea767;
        display: inline;
        visibility: visible;
        position: absolute;
        left: 120px;
        white-space: nowrap;
        bottom: -128px;
        height: 36px;
        vertical-align: middle;
    }*/
    div#stateCommentInside {
        display: none;
    }
    li[id*='stateCommentInside'] {
        /*visibility: hidden;*/
        /*position: absolute;*/
        display: list-item;
    }
    '''
    return r
}

def refreshAdditional(metaConfig) {
    
    //logging("this.binding.variables = ${this.binding.variables}", 1)
    //logging("settings = ${settings}", 1)
    //logging("getDefinitionData() = ${getDefinitionData()}", 1)
    //logging("getPreferences() = ${getPreferences()}", 1)
    //logging("getSupportedCommands() = ${device.getSupportedCommands()}", 1)
    //logging("Seeing these commands: ${device.getSupportedCommands()}", 1)
    
    metaConfig = setStateVariablesToHide(['mac'], metaConfig=metaConfig)
    logging("hideExtended=$hideExtended, hideAdvanced=$hideAdvanced", 1)
    if(hideExtended == null || hideExtended == true) {
        metaConfig = setPreferencesToHide(['hideAdvanced', 'ipAddress', 'override', 'telePeriod'], metaConfig=metaConfig)
    }
    if(hideExtended == null || hideExtended == true || hideAdvanced == null || hideAdvanced == true) {
        metaConfig = setPreferencesToHide(['disableModuleSelection', 'moduleNumber', 'deviceTemplateInput', 'useIPAsID', 'port', 'disableCSS'], metaConfig=metaConfig)
    }
    if(hideDangerousCommands == null || hideDangerousCommands == true) {
        metaConfig = setCommandsToHide(['deleteChildren'], metaConfig=metaConfig)
    }
    if(deviceConfig == null) deviceConfig = "01generic-device"
    deviceConfigMap = getDeviceConfiguration(deviceConfig)
    logging("deviceConfigMap=$deviceConfigMap", 1)
    try{
        if(deviceConfigMap.containsKey('comment') && 
           deviceConfigMap['comment'] != null &&
           deviceConfigMap['comment'].length() > 0) {
            logging("Settings state.comment...", 1)
            setStateCommentInCSS(deviceConfigMap['comment'], metaConfig=metaConfig) 
            //state.comment = "<div id=\"stateComment\"><div id=\"stateCommentInside\">${deviceConfigMap['comment']}</div></div>"
            //metaConfig = setStateVariablesToHide(['comment'], metaConfig=metaConfig)
            state.comment = "<div id=\"stateComment\"><div id=\"stateCommentInside\"></div></div>"
        } else {
            logging("Hiding state.comment...", 1)
            state.comment = "<div id=\"stateComment\"><div id=\"stateCommentInside\"></div></div>"
            metaConfig = setStateVariablesToHide(['comment'], metaConfig=metaConfig)
        }
    } catch(e2) {
        log.warn e2
        metaConfig = setStateVariablesToHide(['comment'], metaConfig=metaConfig)
    }

    

    /*metaConfig = setCommandsToHide(["on", "hiAgain2", "on"])
    metaConfig = setStateVariablesToHide(["uptime"], metaConfig=metaConfig)
    metaConfig = setCurrentStatesToHide(["needUpdate"], metaConfig=metaConfig)
    metaConfig = setDatasToHide(["namespace"], metaConfig=metaConfig)
    metaConfig = setPreferencesToHide(["port"], metaConfig=metaConfig)*/
    //metaConfig = clearThingsToHide()
    //setDisableCSS(false, metaConfig=metaConfig)
    /*metaConfig = setCommandsToHide([])
    metaConfig = setStateVariablesToHide([], metaConfig=metaConfig)
    metaConfig = setCurrentStatesToHide([], metaConfig=metaConfig)
    metaConfig = setDatasToHide([], metaConfig=metaConfig)
    metaConfig = setPreferencesToHide([], metaConfig=metaConfig)*/
}

/* The parse(description) function is included and auto-expanded from external files */
def parse(description) {
    #!include:getGenericTasmotaNewParseHeader()
        missingChild = parseResult(result, missingChild)
    #!include:getGenericTasmotaNewParseFooter()
}

def parseResult(result) {
    def missingChild = false
    missingChild = parseResult(result, missingChild)
    return missingChild
}

def parseResult(result, missingChild) {

    updatePresence("present")
    logging("parseResult: $result", 1)
    #!include:getTasmotaNewParserForBasicData()
    #!include:getTasmotaNewParserForParentSwitch()
    #!include:getTasmotaNewParserForEnergyMonitor()
    #!include:getTasmotaNewParserForDimmableDevice()
    #!include:getTasmotaNewParserForRGBWDevice()
    #!include:getTasmotaNewParserForSensors()
    #!include:getTasmotaNewParserForWifi()

    return missingChild
}

// Call order: installed() -> configure() -> initialize() -> updated() -> updateNeededSettings()
def updateNeededSettings() {
    #!include:getUpdateNeededSettingsTasmotaHeader()

    // Get the Device Configuration
    if(deviceConfig == null) deviceConfig = "01generic-device"
    def deviceConfigMap = getDeviceConfiguration(deviceConfig)
    
    def deviceTemplateInput = deviceConfigMap?.template
    def moduleNumber = deviceConfigMap?.module
    if(deviceTemplateInput == "") deviceTemplateInput = null
    if(moduleNumber == "") moduleNumber = null

    logging("updateNeededSettings: deviceConfigMap=$deviceConfigMap, deviceTemplateInput=$deviceTemplateInput, moduleNumber=$moduleNumber", 0)

    #!include:getUpdateNeededSettingsTasmotaDynamicModuleCommand()

    // TODO: Process device-type specific settings here...

    installCommands = deviceConfigMap?.installCommands
    if(installCommands == null || installCommands == '') installCommands = []
    runInstallCommands(installCommands)

    //
    // https://tasmota.github.io/docs/#/Commands
    //SetOption66
    //Set publishing TuyaReceived to MQTT  »6.7.0
    //0 = disable publishing TuyaReceived over MQTT (default)
    //1 = enable publishing TuyaReceived over MQTT
    //cmds << getAction(getCommandString("SetOption66", "1"))

    //cmds << getAction(getCommandString("SetOption81", "0")) // Set PCF8574 component behavior for all ports as inverted (default=0)

    #!include:getUpdateNeededSettingsTasmotaFooter()
}

// Calls TO Child devices
Boolean callChildParseByTypeId(String deviceTypeId, event, missingChild) {
    event.each{
        if(it.containsKey("descriptionText") == false) {
            it["descriptionText"] = "'$it.name' set to '$it.value'"
        }
    }
    try {
        cd = getChildDevice("$device.id-$deviceTypeId")
        if(cd != null) {
            cd.parse(event)
        } else {
            // We're missing a device...
            log.warn("childParse() can't FIND the device ${cd?.displayName}! (childId: ${"$device.id-$deviceTypeId"}) Did you delete something?")
            missingChild = true
        }
    } catch(e) {
        log.warn("childParse() can't send parse event to device ${cd?.displayName}! Error=$e")
        missingChild = true
    }
    return missingChild
}

void childParse(cd, event) {
    try {
        getChildDevice(cd.deviceNetworkId).parse(event)
    } catch(e) {
        log.warn("childParse() can't send parse event to device ${cd?.displayName}! Error=$e")
    }
}

String getDeviceActionType(String childDeviceNetworkId) {
    return childDeviceNetworkId.tokenize("-")[1]
}

// Calls FROM Child devices
void componentOn(cd) {
    actionType = getDeviceActionType(cd.deviceNetworkId)
    logging("componentOn(cd=${cd.displayName} (${cd.deviceNetworkId})) actionType=${getDeviceActionType(cd.deviceNetworkId)}", 1)
    getAction(getCommandString("$actionType", "1"))
    //childParse(cd, [[name:"switch", value:"on", descriptionText:"${cd.displayName} was turned on"]])
}

void componentOff(cd) {
    actionType = getDeviceActionType(cd.deviceNetworkId)
    logging("componentOff(cd=${cd.displayName} (${cd.deviceNetworkId})) actionType=${getDeviceActionType(cd.deviceNetworkId)}", 1)
    getAction(getCommandString("$actionType", "0"))
    //childParse(cd, [[name:"switch", value:"off", descriptionText:"${cd.displayName} was turned off"]])
}

void componentSetColor(cd, value) {
    actionType = getDeviceActionType(cd.deviceNetworkId)
    logging("componentSetColor(cd=${cd.displayName} (${cd.deviceNetworkId}), level=${value}) actionType=${getDeviceActionType(cd.deviceNetworkId)}", 1)
    setColor(value)
}

void componentSetHue(cd, h) {
    actionType = getDeviceActionType(cd.deviceNetworkId)
    logging("componentSetColor(cd=${cd.displayName} (${cd.deviceNetworkId}), level=${h}) actionType=${getDeviceActionType(cd.deviceNetworkId)}", 1)
    setHue(h)
}

void componentSetColorTemperature(cd, value) {
    actionType = getDeviceActionType(cd.deviceNetworkId)
    logging("componentSetColorTemperature(cd=${cd.displayName} (${cd.deviceNetworkId}), level=${value}) actionType=${getDeviceActionType(cd.deviceNetworkId)}", 1)
    setColorTemperature(value)
}

void componentSetLevel(cd, level) {
    actionType = getDeviceActionType(cd.deviceNetworkId)
    logging("componentSetLevel(cd=${cd.displayName} (${cd.deviceNetworkId}), level=${level}) actionType=${getDeviceActionType(cd.deviceNetworkId)}", 1)
    setLevel(level)
}

void componentSetLevel(cd, level, ramp) {
    actionType = getDeviceActionType(cd.deviceNetworkId)
    logging("componentSetLevel(cd=${cd.displayName} (${cd.deviceNetworkId}), level=${level}, ramp=${ramp}) actionType=${getDeviceActionType(cd.deviceNetworkId)}", 1)
    setLevel(level, ramp)
}

void componentSetSaturation(cd, s) {
    actionType = getDeviceActionType(cd.deviceNetworkId)
    logging("componentSetSaturation(cd=${cd.displayName} (${cd.deviceNetworkId}), s=${s}) actionType=${getDeviceActionType(cd.deviceNetworkId)}", 1)
    setSaturation(s)
}

void componentStartLevelChange(cd, direction) {
    actionType = getDeviceActionType(cd.deviceNetworkId)
    logging("componentStartLevelChange(cd=${cd.displayName} (${cd.deviceNetworkId}), direction=${direction}) actionType=${getDeviceActionType(cd.deviceNetworkId)}", 1)
    startLevelChange(direction)
}

void componentStopLevelChange(cd) {
    actionType = getDeviceActionType(cd.deviceNetworkId)
    logging("componentStopLevelChange(cd=${cd.displayName} (${cd.deviceNetworkId})) actionType=${getDeviceActionType(cd.deviceNetworkId)}", 1)
    stopLevelChange()
}

void componentRefresh(cd) {
    actionType = getDeviceActionType(cd.deviceNetworkId)
    logging("componentRefresh(cd=${cd.displayName} (${cd.deviceNetworkId})) actionType=${getDeviceActionType(cd.deviceNetworkId)}", 1)
    refresh()
}

/*
    -----------------------------------------------------------------------------
    Everything below here are LIBRARY includes and should NOT be edited manually!
    -----------------------------------------------------------------------------
    --- Nothings to edit here, move along! --------------------------------------
    -----------------------------------------------------------------------------
*/

#!include:getDefaultFunctions()

#!include:getGetChildDriverNameMethod()

#!include:getLoggingFunction(specialDebugLevel=True)

#!include:getHelperFunctions('all-default')

#!include:getHelperFunctions('driver-metadata')

#!include:getHelperFunctions('styling')

#!include:getHelperFunctions('driver-default')

#!include:getHelperFunctions('childDevices')

#!include:getHelperFunctions('temperature-humidity')

#!include:getHelperFunctions('tasmota')

#!include:getHelperFunctions('rgbw')

#!include:getHelperFunctions('tasmota-rgbw')