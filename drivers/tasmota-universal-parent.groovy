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
        metaConfig = setPreferencesToHide(['hideAdvanced', 'ipAddress', 'override', 'useIPAsID', 'telePeriod'], metaConfig=metaConfig)
    }
    if(hideExtended == null || hideExtended == true || hideAdvanced == null || hideAdvanced == true) {
        metaConfig = setPreferencesToHide(['disableModuleSelection', 'moduleNumber', 'deviceTemplateInput', , 'port', 'disableCSS'], metaConfig=metaConfig)
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

boolean parseResult(result) {
    boolean missingChild = false
    missingChild = parseResult(result, missingChild)
    return missingChild
}

boolean parseResult(result, missingChild) {

    updatePresence("present")
    boolean log99 = logging("parseResult: $result", 99)
    #!include:getTasmotaNewParserForBasicData()
    #!include:getTasmotaNewParserForParentSwitch()
    #!include:getTasmotaNewParserForFanMode()
    #!include:getTasmotaNewParserForEnergyMonitor()
    #!include:getTasmotaNewParserForDimmableDevice()
    #!include:getTasmotaNewParserForRGBWDevice()
    #!include:getTasmotaNewParserForSensors()
    #!include:getTasmotaNewParserForWifi()

    return missingChild
}

// Call order: installed() -> configure() -> initialize() -> updated() -> updateNeededSettings()
void updateNeededSettings() {
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
    logging("After getUpdateNeededSettingsTasmotaDynamicModuleCommand", 1)
    // TODO: Process device-type specific settings here...

    installCommands = deviceConfigMap?.installCommands
    if(installCommands == null || installCommands == '') installCommands = []
    logging("Got to just before runInstallCommands", 1)
    runInstallCommands(installCommands)

    //
    // https://tasmota.github.io/docs/#/Commands
    //SetOption66
    //Set publishing TuyaReceived to MQTT  »6.7.0
    //0 = disable publishing TuyaReceived over MQTT (default)
    //1 = enable publishing TuyaReceived over MQTT
    //getAction(getCommandString("SetOption66", "1"))

    //getAction(getCommandString("SetOption81", "0")) // Set PCF8574 component behavior for all ports as inverted (default=0)

    #!include:getUpdateNeededSettingsTasmotaFooter()
}

/** Calls TO Child devices */
boolean callChildParseByTypeId(String deviceTypeId, event, boolean missingChild) {
    event.each{
        if(it.containsKey("descriptionText") == false) {
            it["descriptionText"] = "'$it.name' set to '$it.value'"
        }
        it["isStateChange"] = false
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

/** Calls FROM Child devices */
void componentRefresh(cd) {
    String actionType = getDeviceActionType(cd.deviceNetworkId)
    logging("componentRefresh(cd=${cd.displayName} (${cd.deviceNetworkId})) actionType=$actionType", 1)
    refresh()
}

void componentOn(cd) {
    String actionType = getDeviceActionType(cd.deviceNetworkId)
    logging("componentOn(cd=${cd.displayName} (${cd.deviceNetworkId})) actionType=$actionType", 1)
    getAction(getCommandString("$actionType", "1"))
    //childParse(cd, [[name:"switch", value:"on", descriptionText:"${cd.displayName} was turned on"]])
}

void componentOff(cd) {
    String actionType = getDeviceActionType(cd.deviceNetworkId)
    logging("componentOff(cd=${cd.displayName} (${cd.deviceNetworkId})) actionType=$actionType", 1)
    getAction(getCommandString("$actionType", "0"))
    //childParse(cd, [[name:"switch", value:"off", descriptionText:"${cd.displayName} was turned off"]])
}

void componentSetLevel(cd, BigDecimal level) {
    String actionType = getDeviceActionType(cd.deviceNetworkId)
    logging("componentSetLevel(cd=${cd.displayName} (${cd.deviceNetworkId}), level=${level}) actionType=$actionType", 1)
    setLevel(level)
}

void componentSetLevel(cd, BigDecimal level, BigDecimal duration) {
    String actionType = getDeviceActionType(cd.deviceNetworkId)
    logging("componentSetLevel(cd=${cd.displayName} (${cd.deviceNetworkId}), level=${level}, duration=${duration}) actionType=$actionType", 1)
    setLevel(level, duration)
}

void componentStartLevelChange(cd, String direction) {
    String actionType = getDeviceActionType(cd.deviceNetworkId)
    logging("componentStartLevelChange(cd=${cd.displayName} (${cd.deviceNetworkId}), direction=${direction}) actionType=$actionType", 1)
    startLevelChange(direction)
}

void componentStopLevelChange(cd) {
    String actionType = getDeviceActionType(cd.deviceNetworkId)
    logging("componentStopLevelChange(cd=${cd.displayName} (${cd.deviceNetworkId})) actionType=$actionType", 1)
    stopLevelChange()
}

void componentSetColor(cd, Map colormap) {
    String actionType = getDeviceActionType(cd.deviceNetworkId)
    logging("componentSetColor(cd=${cd.displayName} (${cd.deviceNetworkId}), colormap=${colormap}) actionType=$actionType", 1)
    setColor(colormap)
}

void componentSetHue(cd, BigDecimal hue) {
    String actionType = getDeviceActionType(cd.deviceNetworkId)
    logging("componentSetHue(cd=${cd.displayName} (${cd.deviceNetworkId}), hue=${hue}) actionType=$actionType", 1)
    setHue(hue)
}

void componentWhite(cd) {
    String actionType = getDeviceActionType(cd.deviceNetworkId)
    logging("componentWhite(cd=${cd.displayName} (${cd.deviceNetworkId})) actionType=$actionType", 1)
    white()
}

void componentSetRGB(cd, r, g, b) {
    String actionType = getDeviceActionType(cd.deviceNetworkId)
    logging("componentSetRGB(cd=${cd.displayName} (${cd.deviceNetworkId}), r=${r}, g=${g}, b=${b}) actionType=$actionType", 1)
    setRGB(r, g, b)
}

void componentSetSaturation(cd, BigDecimal saturation) {
    String actionType = getDeviceActionType(cd.deviceNetworkId)
    logging("componentSetSaturation(cd=${cd.displayName} (${cd.deviceNetworkId}), saturation=${saturation}) actionType=$actionType", 1)
    setSaturation(saturation)
}

void componentSetColorTemperature(cd, BigDecimal colortemperature) {
    String actionType = getDeviceActionType(cd.deviceNetworkId)
    logging("componentSetColorTemperature(cd=${cd.displayName} (${cd.deviceNetworkId}), colortemperature=${colortemperature}) actionType=$actionType", 1)
    setColorTemperature(colortemperature)
}

void componentModeNext(cd, BigDecimal speed) {
    modeNext(speed)
}

void componentModePrevious(cd, BigDecimal speed) {
    modePrevious(speed)
}

void componentModeSingleColor(cd, BigDecimal speed) {
    modeSingleColor(speed)
}

void componentModeCycleUpColors(cd, BigDecimal speed) {
    modeCycleUpColors(speed)
}

void componentModeCycleDownColors(cd, BigDecimal speed) {
    modeCycleDownColors(speed)
}

void componentModeRandomColors(cd, BigDecimal speed) {
    modeRandomColors(speed)
}

void componentModeWakeUp(cd, BigDecimal wakeUpDuration, BigDecimal level) {
    modeWakeUp(wakeUpDuration, level)
}

void componentSetSpeed(cd, String fanspeed) {
    switch(fanspeed) {
        case "off":
            getAction(getCommandString("FanSpeed", "0"))
            break
        case "on":
        case "low":
            getAction(getCommandString("FanSpeed", "1"))
            break
        case "medium-low":
        case "medium":  
            getAction(getCommandString("FanSpeed", "2"))
            break
        case "medium-high":
        case "high":
            getAction(getCommandString("FanSpeed", "3"))
            break
    }  
}

/**
 * -----------------------------------------------------------------------------
 * Everything below here are LIBRARY includes and should NOT be edited manually!
 * -----------------------------------------------------------------------------
 * --- Nothings to edit here, move along! --------------------------------------
 * -----------------------------------------------------------------------------
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