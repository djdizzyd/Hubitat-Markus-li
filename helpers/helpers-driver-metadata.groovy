/*
    DRIVER METADATA METHODS (helpers-driver-metadata)

    These methods are to be used in (and/or with) the metadata section of drivers and
    is also what contains the CSS handling and styling.
*/

// These methods can be executed in both the NORMAL driver scope as well
// as the Metadata scope.
private Map getMetaConfig() {
    // This method can ALSO be executed in the Metadata Scope
    def metaConfig = getDataValue('metaConfig')
    if(metaConfig == null) {
        metaConfig = [:]
    } else {
        metaConfig = parseJson(metaConfig)
    }
    return metaConfig
}

Boolean isCSSDisabled(Map metaConfig=null) {
    if(metaConfig==null) metaConfig = getMetaConfig()
    Boolean disableCSS = false
    if(metaConfig.containsKey("disableCSS")) disableCSS = metaConfig["disableCSS"]
    return disableCSS
}

// These methods are used to set which elements to hide. 
// They have to be executed in the NORMAL driver scope.
private void saveMetaConfig(Map metaConfig) {
    updateDataValue('metaConfig', JsonOutput.toJson(metaConfig))
}

private Map setSomethingToHide(String type, List something, Map metaConfig=null) {
    if(metaConfig==null) metaConfig = getMetaConfig()
    def oldData = []
    something = something.unique()
    if(!metaConfig.containsKey("hide")) {
        metaConfig["hide"] = [type:something]
    } else {
        //logging("setSomethingToHide 1 else: something: '$something', type:'$type' (${metaConfig["hide"]}) containsKey:${metaConfig["hide"].containsKey(type)}", 1)
        if(metaConfig["hide"].containsKey(type)) {
            //logging("setSomethingToHide 1 hasKey else: something: '$something', type:'$type' (${metaConfig["hide"]}) containsKey:${metaConfig["hide"].containsKey(type)}", 1)
            metaConfig["hide"][type].addAll(something)
        } else {
            //logging("setSomethingToHide 1 noKey else: something: '$something', type:'$type' (${metaConfig["hide"]}) containsKey:${metaConfig["hide"].containsKey(type)}", 1)
            metaConfig["hide"][type] = something
        }
        //metaConfig["hide"]["$type"] = oldData
        //logging("setSomethingToHide 2 else: something: '$something', type:'$type' (${metaConfig["hide"]}) containsKey:${metaConfig["hide"].containsKey(type)}", 1)
    }
    saveMetaConfig(metaConfig)
    logging("setSomethingToHide() = ${metaConfig}", 1)
    return metaConfig
}

private Map clearTypeToHide(String type, Map metaConfig=null) {
    if(metaConfig==null) metaConfig = getMetaConfig()
    if(!metaConfig.containsKey("hide")) {
        metaConfig["hide"] = [(type):[]]
    } else {
        metaConfig["hide"][(type)] = []
    }
    saveMetaConfig(metaConfig)
    logging("clearTypeToHide() = ${metaConfig}", 1)
    return metaConfig
}

Map clearThingsToHide(Map metaConfig=null) {
    metaConfig = setSomethingToHide("other", [], metaConfig=metaConfig)
    metaConfig["hide"] = [:]
    saveMetaConfig(metaConfig)
    logging("clearThingsToHide() = ${metaConfig}", 1)
    return metaConfig
}

Map setDisableCSS(Boolean value, Map metaConfig=null) {
    if(metaConfig==null) metaConfig = getMetaConfig()
    metaConfig["disableCSS"] = value
    saveMetaConfig(metaConfig)
    logging("setDisableCSS(value = $value) = ${metaConfig}", 1)
    return metaConfig
}

Map setStateCommentInCSS(String stateComment, Map metaConfig=null) {
    if(metaConfig==null) metaConfig = getMetaConfig()
    metaConfig["stateComment"] = stateComment
    saveMetaConfig(metaConfig)
    logging("setStateCommentInCSS(stateComment = $stateComment) = ${metaConfig}", 1)
    return metaConfig
}

Map setCommandsToHide(List commands, Map metaConfig=null) {
    metaConfig = setSomethingToHide("command", commands, metaConfig=metaConfig)
    logging("setCommandsToHide(${commands})", 1)
    return metaConfig
}

Map clearCommandsToHide(Map metaConfig=null) {
    metaConfig = clearTypeToHide("command", metaConfig=metaConfig)
    logging("clearCommandsToHide(metaConfig=${metaConfig})", 1)
    return metaConfig
}

Map setStateVariablesToHide(List stateVariables, Map metaConfig=null) {
    metaConfig = setSomethingToHide("stateVariable", stateVariables, metaConfig=metaConfig)
    logging("setStateVariablesToHide(${stateVariables})", 1)
    return metaConfig
}

Map clearStateVariablesToHide(Map metaConfig=null) {
    metaConfig = clearTypeToHide("stateVariable", metaConfig=metaConfig)
    logging("clearStateVariablesToHide(metaConfig=${metaConfig})", 1)
    return metaConfig
}

Map setCurrentStatesToHide(List currentStates, Map metaConfig=null) {
    metaConfig = setSomethingToHide("currentState", currentStates, metaConfig=metaConfig)
    logging("setCurrentStatesToHide(${currentStates})", 1)
    return metaConfig
}

Map clearCurrentStatesToHide(Map metaConfig=null) {
    metaConfig = clearTypeToHide("currentState", metaConfig=metaConfig)
    logging("clearCurrentStatesToHide(metaConfig=${metaConfig})", 1)
    return metaConfig
}

Map setDatasToHide(List datas, Map metaConfig=null) {
    metaConfig = setSomethingToHide("data", datas, metaConfig=metaConfig)
    logging("setDatasToHide(${datas})", 1)
    return metaConfig
}

Map clearDatasToHide(Map metaConfig=null) {
    metaConfig = clearTypeToHide("data", metaConfig=metaConfig)
    logging("clearDatasToHide(metaConfig=${metaConfig})", 1)
    return metaConfig
}

Map setPreferencesToHide(List preferences, Map metaConfig=null) {
    metaConfig = setSomethingToHide("preference", preferences, metaConfig=metaConfig)
    logging("setPreferencesToHide(${preferences})", 1)
    return metaConfig
}

Map clearPreferencesToHide(Map metaConfig=null) {
    metaConfig = clearTypeToHide("preference", metaConfig=metaConfig)
    logging("clearPreferencesToHide(metaConfig=${metaConfig})", 1)
    return metaConfig
}

// These methods are for executing inside the metadata section of a driver.
def metaDataExporter() {
    //log.debug "getEXECUTOR_TYPE = ${getEXECUTOR_TYPE()}"
    List filteredPrefs = getPreferences()['sections']['input'].name[0]
    //log.debug "filteredPrefs = ${filteredPrefs}"
    if(filteredPrefs != []) updateDataValue('preferences', "${filteredPrefs}".replaceAll("\\s",""))
}

// These methods are used to add CSS to the driver page
// This can be used for, among other things, to hide Commands
// They HAVE to be run in getDriverCSS() or getDriverCSSWrapper()!

/* Example usage:
r += getCSSForCommandsToHide(["off", "refresh"])
r += getCSSForStateVariablesToHide(["alertMessage", "mac", "dni", "oldLabel"])
r += getCSSForCurrentStatesToHide(["templateData", "tuyaMCU", "needUpdate"])
r += getCSSForDatasToHide(["preferences", "appReturn"])
r += getCSSToChangeCommandTitle("configure", "Run Configure2")
r += getCSSForPreferencesToHide(["numSwitches", "deviceTemplateInput"])
r += getCSSForPreferenceHiding('<none>', overrideIndex=getPreferenceIndex('<none>', returnMax=true) + 1)
r += getCSSForHidingLastPreference()
r += '''
form[action*="preference"]::before {
    color: green;
    content: "Hi, this is my content"
}
form[action*="preference"] div.mdl-grid div.mdl-cell:nth-of-type(2) {
    color: green;
}
form[action*="preference"] div[for^=preferences] {
    color: blue;
}
h3, h4, .property-label {
    font-weight: bold;
}
'''
*/

String getDriverCSSWrapper() {
    Map metaConfig = getMetaConfig()
    Boolean disableCSS = isCSSDisabled(metaConfig=metaConfig)
    String defaultCSS = '''
    /* This is part of the CSS for replacing a Command Title */
    div.mdl-card__title div.mdl-grid div.mdl-grid .mdl-cell p::after {
        visibility: visible;
        position: absolute;
        left: 50%;
        transform: translate(-50%, 0%);
        width: calc(100% - 20px);
        padding-left: 5px;
        padding-right: 5px;
        margin-top: 0px;
    }
    /* This is general CSS Styling for the Driver page */
    h3, h4, .property-label {
        font-weight: bold;
    }
    .preference-title {
        font-weight: bold;
    }
    .preference-description {
        font-style: italic;
    }
    '''
    String r = "<style>"
    
    if(disableCSS == false) {
        r += "$defaultCSS "
        try{
            // We always need to hide this element when we use CSS
            r += " ${getCSSForHidingLastPreference()} "
            
            if(disableCSS == false) {
                if(metaConfig.containsKey("hide")) {
                    if(metaConfig["hide"].containsKey("command")) {
                        r += getCSSForCommandsToHide(metaConfig["hide"]["command"])
                    }
                    if(metaConfig["hide"].containsKey("stateVariable")) {
                        r += getCSSForStateVariablesToHide(metaConfig["hide"]["stateVariable"])
                    }
                    if(metaConfig["hide"].containsKey("currentState")) {
                        r += getCSSForCurrentStatesToHide(metaConfig["hide"]["currentState"])
                    }
                    if(metaConfig["hide"].containsKey("data")) {
                        r += getCSSForDatasToHide(metaConfig["hide"]["data"])
                    }
                    if(metaConfig["hide"].containsKey("preference")) {
                        r += getCSSForPreferencesToHide(metaConfig["hide"]["preference"])
                    }
                }
                if(metaConfig.containsKey("stateComment")) {
                    r += "div#stateComment:after { content: \"${metaConfig["stateComment"]}\" }"
                }
                r += " ${getDriverCSS()} "
            }
        }catch(MissingMethodException e) {
            if(!e.toString().contains("getDriverCSS()")) {
                log.warn "getDriverCSS() Error: $e"
            }
        } catch(e) {
            log.warn "getDriverCSS() Error: $e"
        }
    }
    r += " </style>"
    return r
}

int getCommandIndex(String cmd) {
    List commands = device.getSupportedCommands().unique()
    int i = commands.findIndexOf{ "$it" == cmd}+1
    //log.debug "getCommandIndex: Seeing these commands: '${commands}', index=$i}"
    return i
}

String getCSSForCommandHiding(String cmdToHide) {
    int i = getCommandIndex(cmdToHide)
    String r = ""
    if(i > 0) {
        r = "div.mdl-card__title div.mdl-grid div.mdl-grid .mdl-cell:nth-of-type($i){display: none;}"
    }
    return r
}

String getCSSForCommandsToHide(List commands) {
    String r = ""
    commands.each {
        r += getCSSForCommandHiding(it)
    }
    return r
}

String getCSSToChangeCommandTitle(String cmd, String newTitle) {
    int i = getCommandIndex(cmd)
    String r = ""
    if(i > 0) {
        r += "div.mdl-card__title div.mdl-grid div.mdl-grid .mdl-cell:nth-of-type($i) p {visibility: hidden;}"
        r += "div.mdl-card__title div.mdl-grid div.mdl-grid .mdl-cell:nth-of-type($i) p::after {content: '$newTitle';}"
    }
    return r
}

int getStateVariableIndex(String stateVariable) {
    def stateVariables = state.keySet()
    int i = stateVariables.findIndexOf{ "$it" == stateVariable}+1
    //log.debug "getStateVariableIndex: Seeing these State Variables: '${stateVariables}', index=$i}"
    return i
}

String getCSSForStateVariableHiding(String stateVariableToHide) {
    int i = getStateVariableIndex(stateVariableToHide)
    String r = ""
    if(i > 0) {
        r = "ul#statev li.property-value:nth-of-type($i){display: none;}"
    }
    return r
}

String getCSSForStateVariablesToHide(List stateVariables) {
    String r = ""
    stateVariables.each {
        r += getCSSForStateVariableHiding(it)
    }
    return r
}

String getCSSForCurrentStatesToHide(List currentStates) {
    String r = ""
    currentStates.each {
        r += "ul#cstate li#cstate-$it {display: none;}"
    }
    return r
}

int getDataIndex(String data) {
    def datas = device.getData().keySet()
    int i = datas.findIndexOf{ "$it" == data}+1
    //log.debug "getDataIndex: Seeing these Data Keys: '${datas}', index=$i}"
    return i
}

String getCSSForDataHiding(String dataToHide) {
    int i = getDataIndex(dataToHide)
    String r = ""
    if(i > 0) {
        r = "table.property-list tr li.property-value:nth-of-type($i) {display: none;}"
    }
    return r
}

String  getCSSForDatasToHide(List datas) {
    String r = ""
    datas.each {
        r += getCSSForDataHiding(it)
    }
    return r
}

int getPreferenceIndex(String preference, Boolean returnMax=false) {
    def filteredPrefs = getPreferences()['sections']['input'].name[0]
    //log.debug "getPreferenceIndex: Seeing these Preferences first: '${filteredPrefs}'"
    if(filteredPrefs == [] || filteredPrefs == null) {
        d = getDataValue('preferences')
        //log.debug "getPreferenceIndex: getDataValue('preferences'): '${d}'"
        if(d != null && d.length() > 2) {
            try{
                filteredPrefs = d[1..d.length()-2].tokenize(',')
            } catch(e) {
                // Do nothing
            }
        }
        

    }
    int i = 0
    if(returnMax == true) {
        i = filteredPrefs.size()
    } else {
        i = filteredPrefs.findIndexOf{ "$it" == preference}+1
    }
    //log.debug "getPreferenceIndex: Seeing these Preferences: '${filteredPrefs}', index=$i"
    return i
}

String getCSSForPreferenceHiding(String preferenceToHide, int overrideIndex=0) {
    int i = 0
    if(overrideIndex == 0) {
        i = getPreferenceIndex(preferenceToHide)
    } else {
        i = overrideIndex
    }
    String r = ""
    if(i > 0) {
        r = "form[action*=\"preference\"] div.mdl-grid div.mdl-cell:nth-of-type($i) {display: none;} "
    }else if(i == -1) {
        r = "form[action*=\"preference\"] div.mdl-grid div.mdl-cell:nth-last-child(2) {display: none;} "
    }
    return r
}

String getCSSForPreferencesToHide(List preferences) {
    String r = ""
    preferences.each {
        r += getCSSForPreferenceHiding(it)
    }
    return r
}

String getCSSForHidingLastPreference() {
    return getCSSForPreferenceHiding(null, overrideIndex=-1)
}

/*
    --END-- DRIVER METADATA METHODS (helpers-driver-metadata)
*/