
// BEGIN:getHeaderLicense()
 /**
 *  Copyright 2020 Markus Liljergren
 *
 *  Licensed under the Apache License, Version 2.0 (the "License");
 *  you may not use this file except in compliance with the License.
 *  You may obtain a copy of the License at:
 *
 *  http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing, software
 *  distributed under the License is distributed on an "AS IS" BASIS,
 *  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *  See the License for the specific language governing permissions and
 *  limitations under the License.
 */
// END:getHeaderLicense()



// BEGIN:getDefaultImports()
/* Default Imports */
import groovy.json.JsonSlurper
import groovy.json.JsonOutput
import java.security.MessageDigest   // Used for MD5 calculations


// END:getDefaultImports()


metadata {
	definition (name: "Zigbee - DO NOT USE Generic Wifi Switch/Plug", namespace: "markusl", author: "Markus Liljergren", vid: "generic-switch", importURL: "https://raw.githubusercontent.com/markus-li/Hubitat/release/drivers/expanded/zigbee-generic-wifi-switch-plug-expanded.groovy") {
        capability "Actuator"
        capability "Light"
		capability "Switch"
		capability "Sensor"

        
        // BEGIN:getDefaultMetadataCapabilities()
        
        // Default Capabilities
        capability "Refresh"
        capability "Configuration"
        
        // END:getDefaultMetadataCapabilities()
        
        
        // BEGIN:getDefaultMetadataAttributes()
        
        // Default Attributes
        
        // END:getDefaultMetadataAttributes()
        
        // BEGIN:getDefaultMetadataCommands()
        
        // Default Commands
        command "reboot"
        
        // END:getDefaultMetadataCommands()
	}

	simulator {
	}
    
    preferences {
        
        // BEGIN:getDefaultMetadataPreferences()
        
        // Default Preferences
        generate_preferences(configuration_model_debug())
        
        // END:getDefaultMetadataPreferences()
	}
}


// BEGIN:getDeviceInfoFunction()
public getDeviceInfoByName(infoName) { 
    // DO NOT EDIT: This is generated from the metadata!
    // TODO: Figure out how to get this from Hubitat instead of generating this?
    deviceInfo = ['name': 'Zigbee - DO NOT USE Generic Wifi Switch/Plug', 'namespace': 'markusl', 'author': 'Markus Liljergren', 'vid': 'generic-switch', 'importURL': 'https://raw.githubusercontent.com/markus-li/Hubitat/release/drivers/expanded/zigbee-generic-wifi-switch-plug-expanded.groovy']
    //logging("deviceInfo[${infoName}] = ${deviceInfo[infoName]}", 1)
    return(deviceInfo[infoName])
}
// END:getDeviceInfoFunction()



// BEGIN:getGenericOnOffFunctions()

/* Generic On/Off functions used when only 1 switch/button exists */
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

// END:getGenericOnOffFunctions()


/* These functions are unique to each driver */

def refresh() {
    logging("refresh()", 10)
    // http://ftp1.digi.com/support/images/APP_NOTE_XBee_ZigBee_Device_Profile.pdf
    // https://docs.hubitat.com/index.php?title=Zigbee_Object
    // https://docs.smartthings.com/en/latest/ref-docs/zigbee-ref.html
    //zigbee.clusterLookup(0x0001)
    msgMap = [profileId:0, clusterId:"0x0001", sourceEndpoint:0, 
              destinationEndpoint:0, options:0, messageType:0, dni:"${device.endpointId}", 
              isClusterSpecific:false, isManufacturerSpecific:false, manufacturerId:0, 
              command:0, direction:0]
    
    logging("${device.deviceNetworkId}", 10)
    logging(zigbee.swapOctets("${device.deviceNetworkId}"), 10)
    zigbee.command(0x0001, 0, "${device.deviceNetworkId}")
    zigbee.command(0x0001, 0, zigbee.swapOctets("${device.deviceNetworkId}"))
    zigbee.command(0x0001, 0, msgMap)
    zigbee.command(0x0001, 0, '')
    zigbee.enrollResponse()
    /*return [
            "he 0x0001 0x${device.endpointId} 0x00 {}","delay 200",  //light state
            "he rattr 0x${device.deviceNetworkId} 0x${device.endpointId} 0x0004 0 {}","delay 200",  //light state
            "he rattr 0x${device.deviceNetworkId} 0x${device.endpointId} 0x0000 0 {}","delay 200",  //light state
            "he rattr 0x${device.deviceNetworkId} 0x${device.endpointId} 0x0086 0 {}","delay 200",  //light state
            "he rattr 0x${device.deviceNetworkId} 0x${device.endpointId} 0x0085 0 {}","delay 200",  //light state
            "he rattr 0x${device.deviceNetworkId} 0x${device.endpointId} 0x8600 0 {}","delay 200",  //light state
            "he rattr 0x${device.deviceNetworkId} 0x${device.endpointId} 0x8500 0 {}","delay 200",  //light state
            "he rattr 0x${device.deviceNetworkId} 0x${device.endpointId} 0x0006 0 {}","delay 200",  //light state
            "he rattr 0x${device.deviceNetworkId} 0x${device.endpointId} 0x0008 0 {}","delay 200",  //light level
            "he rattr 0x${device.deviceNetworkId} 0x${device.endpointId} 0x0300 0x0000 {}","delay 200", //hue
            "he rattr 0x${device.deviceNetworkId} 0x${device.endpointId} 0x0300 0x0001 {}","delay 200", //sat
            "he rattr 0x${device.deviceNetworkId} 0x${device.endpointId} 0x0300 0x0007 {}","delay 200",	//color temp
            "he rattr 0x${device.deviceNetworkId} 0x${device.endpointId} 0x0300 0x0008 {}"  		//color mode
    ]*/
    return [
        "he cmd 0x${device.deviceNetworkId} 0x${device.endpointId} 0x0006 0 {}"
    ]
}

def reboot() {
    logging('reboot() is NOT implemented for this device', 1)
    // Ignore
}

def parse(description) {
    
    // BEGIN:getGenericZigbeeParseHeader()
    // parse() Generic Zigbee-device header BEGINS here
    logging("Parsing: ${description}", 0)
    def events = []
    def msgMap = zigbee.parseDescriptionAsMap(description)
    logging("msgMap: ${msgMap}", 0)
    // parse() Generic header ENDS here
    // END:getGenericZigbeeParseHeader()
            
    
    // BEGIN:getGenericZigbeeParseFooter()
    // parse() Generic Zigbee-device footer BEGINS here
    
    return events
    // parse() Generic footer ENDS here
    // END:getGenericZigbeeParseFooter()
}

def updated()
{
    logging("updated()", 10)
    def cmds = [] 
    try {
        // Also run initialize(), if it exists...
        initialize()
    } catch (MissingMethodException e) {
        // ignore
    }
    if (cmds != [] && cmds != null) cmds
}

def updateNeededSettings()
{
    
}


// BEGIN:getDefaultFunctions()
/* Default functions go here */
private def getDriverVersion() {
    //comment = ""
    //if(comment != "") state.comment = comment
    version = "v0.9.5T"
    logging("getDriverVersion() = ${version}", 50)
    sendEvent(name: "driver", value: version)
    updateDataValue('driver', version)
    return version
}

// END:getDefaultFunctions()



// BEGIN:getLoggingFunction()
/* Logging function included in all drivers */
private def logging(message, level) {
    if (infoLogging == true) {
        logLevel = 100
    }
    if (debugLogging == true) {
        logLevel = 1
    }
    if (logLevel != "0"){
        switch (logLevel) {
        case "-1": // Insanely verbose
            if (level >= 0 && level < 100)
                log.debug "$message"
            else if (level == 100)
                log.info "$message"
        break
        case "1": // Very verbose
            if (level >= 1 && level < 99)
                log.debug "$message"
            else if (level == 100)
                log.info "$message"
        break
        case "10": // A little less
            if (level >= 10 && level < 99)
                log.debug "$message"
            else if (level == 100)
                log.info "$message"
        break
        case "50": // Rather chatty
            if (level >= 50 )
                log.debug "$message"
        break
        case "99": // Only parsing reports
            if (level >= 99 )
                log.debug "$message"
        break
        }
    }
}

// END:getLoggingFunction()


/*
    DEFAULT METHODS (helpers-default)

    This include should NOT be used, refactor to include the different 
    parts separately!
*/

/*
    ALL DEFAULT METHODS (helpers-all-default)

    Helper functions included in all drivers/apps
*/
/*
    ALL DEBUG METHODS (helpers-all-debug)

    Helper Debug functions included in all drivers/apps
*/
def configuration_model_debug()
{
    if(!isDeveloperHub()) {
        if(!isDriver()) {
            app.removeSetting("logLevel")
            app.updateSetting("logLevel", "0")
        }
        return '''
<configuration>
<Value type="bool" index="debugLogging" label="Enable debug logging" description="" value="true" submitOnChange="true" setting_type="preference" fw="">
<Help></Help>
</Value>
<Value type="bool" index="infoLogging" label="Enable descriptionText logging" description="" value="true" submitOnChange="true" setting_type="preference" fw="">
<Help></Help>
</Value>
</configuration>
'''
    } else {
        if(!isDriver()) {
            app.removeSetting("debugLogging")
            app.updateSetting("debugLogging", "false")
            app.removeSetting("infoLogging")
            app.updateSetting("infoLogging", "false")
        }
        return '''
<configuration>
<Value type="list" index="logLevel" label="Debug Log Level" description="Under normal operations, set this to None. Only needed for debugging. Auto-disabled after 30 minutes." value="-1" submitOnChange="true" setting_type="preference" fw="">
<Help>
</Help>
    <Item label="None" value="0" />
    <Item label="Insanely Verbose" value="-1" />
    <Item label="Very Verbose" value="1" />
    <Item label="Verbose" value="10" />
    <Item label="Reports+Status" value="50" />
    <Item label="Reports" value="99" />
    
    // BEGIN:getSpecialDebugEntry()
    
    // END:getSpecialDebugEntry()
</Value>
</configuration>
'''
    }
}

/*
    --END-- ALL DEBUG METHODS (helpers-all-debug)
*/

def isDriver() {
    try {
        // If this fails, this is not a driver...
        getDeviceDataByName('_unimportant')
        logging("This IS a driver!", 0)
        return true
    } catch (MissingMethodException e) {
        logging("This is NOT a driver!", 0)
        return false
    }
}

def deviceCommand(cmd) {
    def jsonSlurper = new JsonSlurper()
    cmd = jsonSlurper.parseText(cmd)
    logging("deviceCommand: ${cmd}", 0)
    r = this."${cmd['cmd']}"(*cmd['args'])
    logging("deviceCommand return: ${r}", 0)
    updateDataValue('appReturn', JsonOutput.toJson(r))
}

/*
	initialize

	Purpose: initialize the driver/app
	Note: also called from updated()
*/
// Call order: installed() -> configure() -> updated() -> initialize()
void initialize()
{
    logging("initialize()", 100)
	unschedule()
    // disable debug logs after 30 min, unless override is in place
	if (logLevel != "0" && logLevel != "100") {
        if(runReset != "DEBUG") {
            log.warn "Debug logging will be disabled in 30 minutes..."
        } else {
            log.warn "Debug logging will NOT BE AUTOMATICALLY DISABLED!"
        }
        runIn(1800, logsOff)
    }
    if(isDriver()) {
        if(!isDeveloperHub()) {
            device.removeSetting("logLevel")
            device.updateSetting("logLevel", "0")
        } else {
            device.removeSetting("debugLogging")
            device.updateSetting("debugLogging", "false")
            device.removeSetting("infoLogging")
            device.updateSetting("infoLogging", "false")
        }
    }
    try {
        // In case we have some more to run specific to this driver/app
        initializeAdditional()
    } catch (MissingMethodException e) {
        // ignore
    }
    refresh()
}

/*
	logsOff

	Purpose: automatically disable debug logging after 30 mins.
	Note: scheduled in Initialize()
*/
void logsOff(){
    if(runReset != "DEBUG") {
        log.warn "Debug logging disabled..."
        // Setting logLevel to "0" doesn't seem to work, it disables logs, but does not update the UI...
        //device.updateSetting("logLevel",[value:"0",type:"string"])
        //app.updateSetting("logLevel",[value:"0",type:"list"])
        // Not sure which ones are needed, so doing all... This works!
        if(isDriver()) {
            device.clearSetting("logLevel")
            device.removeSetting("logLevel")
            device.updateSetting("logLevel", "0")
            state.settings.remove("logLevel")
            device.clearSetting("debugLogging")
            device.removeSetting("debugLogging")
            device.updateSetting("debugLogging", "false")
            state.settings.remove("debugLogging")
            
        } else {
            //app.clearSetting("logLevel")
            // To be able to update the setting, it has to be removed first, clear does NOT work, at least for Apps
            app.removeSetting("logLevel")
            app.updateSetting("logLevel", "0")
            app.removeSetting("debugLogging")
            app.updateSetting("debugLogging", "false")
        }
    } else {
        log.warn "OVERRIDE: Disabling Debug logging will not execute with 'DEBUG' set..."
        if (logLevel != "0" && logLevel != "100") runIn(1800, logsOff)
    }
}

def generateMD5(String s){
    MessageDigest.getInstance("MD5").digest(s.bytes).encodeHex().toString()
}

def isDeveloperHub() {
    return generateMD5(location.hub.zigbeeId) == "125fceabd0413141e34bb859cd15e067"
    //return false
}

def getEnvironmentObject() {
    if(isDriver()) {
        return device
    } else {
        return app
    }
}

def dBmToQuality(dBm) {
    def quality = 0
    if(dBm > 0) dBm = dBm * -1
    if(dBm <= -100) {
        quality = 0
    } else if(dBm >= -50) {
        quality = 100
    } else {
        quality = 2 * (dBm + 100)
    }
    logging("DBM: $dBm (${quality}%)", 0)
    return quality
}

def extractInt( String input ) {
  return input.replaceAll("[^0-9]", "").toInteger()
}

private def getFilteredDeviceDriverName() {
    deviceDriverName = getDeviceInfoByName('name')
    if(deviceDriverName.toLowerCase().endsWith(' (parent)')) {
        deviceDriverName = deviceDriverName.substring(0, deviceDriverName.length()-9)
    }
    return deviceDriverName
}

private def getFilteredDeviceDisplayName() {
    device_display_name = device.displayName.replace(' (parent)', '').replace(' (Parent)', '')
    return device_display_name
}

def makeTextBold(s) {
    if(isDriver()) {
        return "<b>$s</b>"
    } else {
        return "$s"
    }
}

def makeTextItalic(s) {
    if(isDriver()) {
        return "<i>$s</i>"
    } else {
        return "$s"
    }
}

def generate_preferences(configuration_model)
{
    def configuration = new XmlSlurper().parseText(configuration_model)
   
    configuration.Value.each
    {
        if(it.@hidden != "true" && it.@disabled != "true"){
        switch(it.@type)
        {   
            case "number":
                input("${it.@index}", "number",
                    title:"${addTitleDiv(it.@label)}" + "${it.Help}",
                    description: makeTextItalic(it.@description),
                    range: "${it.@min}..${it.@max}",
                    defaultValue: "${it.@value}",
                    submitOnChange: it.@submitOnChange == "true",
                    displayDuringSetup: "${it.@displayDuringSetup}")
            break
            case "list":
                def items = []
                it.Item.each { items << ["${it.@value}":"${it.@label}"] }
                input("${it.@index}", "enum",
                    title:"${addTitleDiv(it.@label)}" + "${it.Help}",
                    description: makeTextItalic(it.@description),
                    defaultValue: "${it.@value}",
                    submitOnChange: it.@submitOnChange == "true",
                    displayDuringSetup: "${it.@displayDuringSetup}",
                    options: items)
            break
            case "password":
                input("${it.@index}", "password",
                    title:"${addTitleDiv(it.@label)}" + "${it.Help}",
                    description: makeTextItalic(it.@description),
                    submitOnChange: it.@submitOnChange == "true",
                    displayDuringSetup: "${it.@displayDuringSetup}")
            break
            case "decimal":
               input("${it.@index}", "decimal",
                    title:"${addTitleDiv(it.@label)}" + "${it.Help}",
                    description: makeTextItalic(it.@description),
                    range: "${it.@min}..${it.@max}",
                    defaultValue: "${it.@value}",
                    submitOnChange: it.@submitOnChange == "true",
                    displayDuringSetup: "${it.@displayDuringSetup}")
            break
            case "bool":
               input("${it.@index}", "bool",
                    title:"${addTitleDiv(it.@label)}" + "${it.Help}",
                    description: makeTextItalic(it.@description),
                    defaultValue: "${it.@value}",
                    submitOnChange: it.@submitOnChange == "true",
                    displayDuringSetup: "${it.@displayDuringSetup}")
            break
        }
        }
    }
}

/*
    --END-- ALL DEFAULT METHODS (helpers-all-default)
*/
/*
    DRIVER DEFAULT METHODS (helpers-driver-default)

    TODO: Write file description
*/

/*
    DRIVER METADATA METHODS (helpers-driver-metadata)

    These methods are to be used in (and/or with) the metadata section of drivers and
    is also what contains the CSS handling and styling.
*/

// These methods can be executed in both the NORMAL driver scope as well
// as the Metadata scope.
private getMetaConfig() {
    // This method can ALSO be executed in the Metadata Scope
    metaConfig = getDataValue('metaConfig')
    if(metaConfig == null) {
        metaConfig = [:]
    } else {
        metaConfig = parseJson(metaConfig)
    }
    return metaConfig
}

def isCSSDisabled(metaConfig=null) {
    if(metaConfig==null) metaConfig = getMetaConfig()
    disableCSS = false
    if(metaConfig.containsKey("disableCSS")) disableCSS = metaConfig["disableCSS"]
    return disableCSS
}

// These methods are used to set which elements to hide. 
// They have to be executed in the NORMAL driver scope.


private saveMetaConfig(metaConfig) {
    updateDataValue('metaConfig', JsonOutput.toJson(metaConfig))
}

private setSomethingToHide(String type, something, metaConfig=null) {
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

private clearTypeToHide(type, metaConfig=null) {
    if(metaConfig==null) metaConfig = getMetaConfig()
    something = something.unique()
    if(!metaConfig.containsKey("hide")) {
        metaConfig["hide"] = ["$type":[]]
    } else {
        metaConfig["hide"]["$type"] = []
    }
    saveMetaConfig(metaConfig)
    logging("clearTypeToHide() = ${metaConfig}", 1)
    return metaConfig
}

def clearThingsToHide(metaConfig=null) {
    metaConfig = setSomethingToHide("other", [], metaConfig=metaConfig)
    metaConfig["hide"] = [:]
    saveMetaConfig(metaConfig)
    logging("clearThingsToHide() = ${metaConfig}", 1)
    return metaConfig
}

def setDisableCSS(valueBool, metaConfig=null) {
    if(metaConfig==null) metaConfig = getMetaConfig()
    metaConfig["disableCSS"] = valueBool
    saveMetaConfig(metaConfig)
    logging("setDisableCSS(valueBool = $valueBool) = ${metaConfig}", 1)
    return metaConfig
}

def setStateCommentInCSS(stateComment, metaConfig=null) {
    if(metaConfig==null) metaConfig = getMetaConfig()
    metaConfig["stateComment"] = stateComment
    saveMetaConfig(metaConfig)
    logging("setStateCommentInCSS(stateComment = $stateComment) = ${metaConfig}", 1)
    return metaConfig
}

def setCommandsToHide(commands, metaConfig=null) {
    metaConfig = setSomethingToHide("command", commands, metaConfig=metaConfig)
    logging("setCommandsToHide(${commands})", 1)
    return metaConfig
}

def clearCommandsToHide(metaConfig=null) {
    metaConfig = clearTypeToHide("command", metaConfig=metaConfig)
    logging("clearCommandsToHide(metaConfig=${metaConfig})", 1)
    return metaConfig
}

def setStateVariablesToHide(stateVariables, metaConfig=null) {
    metaConfig = setSomethingToHide("stateVariable", stateVariables, metaConfig=metaConfig)
    logging("setStateVariablesToHide(${stateVariables})", 1)
    return metaConfig
}

def clearStateVariablesToHide(metaConfig=null) {
    metaConfig = clearTypeToHide("stateVariable", metaConfig=metaConfig)
    logging("clearStateVariablesToHide(metaConfig=${metaConfig})", 1)
    return metaConfig
}

def setCurrentStatesToHide(currentStates, metaConfig=null) {
    metaConfig = setSomethingToHide("currentState", currentStates, metaConfig=metaConfig)
    logging("setCurrentStatesToHide(${currentStates})", 1)
    return metaConfig
}

def clearCurrentStatesToHide(metaConfig=null) {
    metaConfig = clearTypeToHide("currentState", metaConfig=metaConfig)
    logging("clearCurrentStatesToHide(metaConfig=${metaConfig})", 1)
    return metaConfig
}

def setDatasToHide(datas, metaConfig=null) {
    metaConfig = setSomethingToHide("data", datas, metaConfig=metaConfig)
    logging("setDatasToHide(${datas})", 1)
    return metaConfig
}

def clearDatasToHide(metaConfig=null) {
    metaConfig = clearTypeToHide("data", metaConfig=metaConfig)
    logging("clearDatasToHide(metaConfig=${metaConfig})", 1)
    return metaConfig
}

def setPreferencesToHide(preferences, metaConfig=null) {
    metaConfig = setSomethingToHide("preference", preferences, metaConfig=metaConfig)
    logging("setPreferencesToHide(${preferences})", 1)
    return metaConfig
}

def clearPreferencesToHide(metaConfig=null) {
    metaConfig = clearTypeToHide("preference", metaConfig=metaConfig)
    logging("clearPreferencesToHide(metaConfig=${metaConfig})", 1)
    return metaConfig
}

// These methods are for executing inside the metadata section of a driver.
def metaDataExporter() {
    //log.debug "getEXECUTOR_TYPE = ${getEXECUTOR_TYPE()}"
    filteredPrefs = getPreferences()['sections']['input'].name[0]
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

def addTitleDiv(title) {
    return '<div class="preference-title">' + title + '</div>'
}

def addDescriptionDiv(description) {
    return '<div class="preference-description">' + description + '</div>'
}

def getDriverCSSWrapper() {
    metaConfig = getMetaConfig()
    disableCSS = isCSSDisabled(metaConfig=metaConfig)
    defaultCSS = '''
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
    r = "<style>"
    
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

def getCommandIndex(cmd) {
    commands = device.getSupportedCommands().unique()
    i = commands.findIndexOf{ "$it" == cmd}+1
    //log.debug "getCommandIndex: Seeing these commands: '${commands}', index=$i}"
    return i
}

def getCSSForCommandHiding(cmdToHide) {
    i = getCommandIndex(cmdToHide)
    r = ""
    if(i > 0) {
        r = "div.mdl-card__title div.mdl-grid div.mdl-grid .mdl-cell:nth-of-type($i){display: none;}"
    }
    return r
}

def getCSSForCommandsToHide(commands) {
    r = ""
    commands.each {
        r += getCSSForCommandHiding(it)
    }
    return r
}

def getCSSToChangeCommandTitle(cmd, newTitle) {
    i = getCommandIndex(cmd)
    r = ""
    if(i > 0) {
        r += "div.mdl-card__title div.mdl-grid div.mdl-grid .mdl-cell:nth-of-type($i) p {visibility: hidden;}"
        r += "div.mdl-card__title div.mdl-grid div.mdl-grid .mdl-cell:nth-of-type($i) p::after {content: '$newTitle';}"
    }
    return r
}

def getStateVariableIndex(stateVariable) {
    stateVariables = state.keySet()
    i = stateVariables.findIndexOf{ "$it" == stateVariable}+1
    //log.debug "getStateVariableIndex: Seeing these State Variables: '${stateVariables}', index=$i}"
    return i
}

def getCSSForStateVariableHiding(stateVariableToHide) {
    i = getStateVariableIndex(stateVariableToHide)
    r = ""
    if(i > 0) {
        r = "ul#statev li.property-value:nth-of-type($i){display: none;}"
    }
    return r
}

def getCSSForStateVariablesToHide(stateVariables) {
    r = ""
    stateVariables.each {
        r += getCSSForStateVariableHiding(it)
    }
    return r
}

def getCSSForCurrentStatesToHide(currentStates) {
    r = ""
    currentStates.each {
        r += "ul#cstate li#cstate-$it {display: none;}"
    }
    return r
}

def getDataIndex(data) {
    datas = device.getData().keySet()
    i = datas.findIndexOf{ "$it" == data}+1
    //log.debug "getDataIndex: Seeing these Data Keys: '${datas}', index=$i}"
    return i
}

def getCSSForDataHiding(dataToHide) {
    i = getDataIndex(dataToHide)
    r = ""
    if(i > 0) {
        r = "table.property-list tr li.property-value:nth-of-type($i) {display: none;}"
    }
    return r
}

def getCSSForDatasToHide(datas) {
    r = ""
    datas.each {
        r += getCSSForDataHiding(it)
    }
    return r
}

def getPreferenceIndex(preference, returnMax=false) {
    filteredPrefs = getPreferences()['sections']['input'].name[0]
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
    i = 0
    if(returnMax == true) {
        i = filteredPrefs.size()
    } else {
        i = filteredPrefs.findIndexOf{ "$it" == preference}+1
    }
    //log.debug "getPreferenceIndex: Seeing these Preferences: '${filteredPrefs}', index=$i"
    return i
}

def getCSSForPreferenceHiding(preferenceToHide, overrideIndex=0) {
    i = 0
    if(overrideIndex == 0) {
        i = getPreferenceIndex(preferenceToHide)
    } else {
        i = overrideIndex
    }
    r = ""
    if(i > 0) {
        r = "form[action*=\"preference\"] div.mdl-grid div.mdl-cell:nth-of-type($i) {display: none;} "
    }else if(i == -1) {
        r = "form[action*=\"preference\"] div.mdl-grid div.mdl-cell:nth-last-child(2) {display: none;} "
    }
    return r
}

def getCSSForPreferencesToHide(preferences) {
    r = ""
    preferences.each {
        r += getCSSForPreferenceHiding(it)
    }
    return r
}
def getCSSForHidingLastPreference() {
    return getCSSForPreferenceHiding(null, overrideIndex=-1)
}

/*
    --END-- DRIVER METADATA METHODS (helpers-driver-metadata)
*/

// Since refresh, with any number of arguments, is accepted as we always have it declared anyway, 
// we use it as a wrapper
// All our "normal" refresh functions take 0 arguments, we can declare one with 1 here...
def refresh(cmd) {
    deviceCommand(cmd)
}
// Call order: installed() -> configure() -> updated() -> initialize() -> refresh()
// Calls installed() -> [configure() -> [updateNeededSettings(), updated() -> [updatedAdditional(), initialize() -> refresh() -> refreshAdditional()], installedAdditional()]
def installed() {
	logging("installed()", 100)
    
	configure()
    try {
        // In case we have some more to run specific to this Driver
        installedAdditional()
    } catch (MissingMethodException e) {
        // ignore
    }
}

// Call order: installed() -> configure()
def configure() {
    logging("configure()", 100)
    def cmds = []
    if(isDriver()) {
        cmds = updateNeededSettings()
        try {
            // Run the getDriverVersion() command
            newCmds = getDriverVersion()
            if (newCmds != null && newCmds != []) cmds = cmds + newCmds
        } catch (MissingMethodException e) {
            // ignore
        }
    }
    if (cmds != []) cmds
}

def update_current_properties(cmd)
{
    def currentProperties = state.currentProperties ?: [:]
    currentProperties."${cmd.name}" = cmd.value

    if (state.settings?."${cmd.name}" != null)
    {
        if (state.settings."${cmd.name}".toString() == cmd.value)
        {
            sendEvent(name:"needUpdate", value:"NO", displayed:false, isStateChange: false)
        }
        else
        {
            sendEvent(name:"needUpdate", value:"YES", displayed:false, isStateChange: false)
        }
    }
    state.currentProperties = currentProperties
}

/*
    --END-- DRIVER DEFAULT METHODS (helpers-driver-default)
*/

/*
    --END-- DEFAULT METHODS (helpers-default)
*/
