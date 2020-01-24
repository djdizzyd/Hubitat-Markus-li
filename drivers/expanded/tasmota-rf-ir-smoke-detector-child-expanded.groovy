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

/* Default imports */
import groovy.json.JsonSlurper
import groovy.json.JsonOutput
import java.security.MessageDigest


metadata {
    definition (name: "Tasmota - RF/IR Smoke Detector (Child)", namespace: "tasmota", author: "Markus Liljergren", importURL: "https://raw.githubusercontent.com/markus-li/Hubitat/release/drivers/expanded/tasmota-rf-ir-smoke-detector-child-expanded.groovy") {
        capability "SmokeDetector"
        capability "Sensor"

        
        // Attributes used for Learning Mode
        attribute   "status", "string"
        attribute   "actionSeen", "number"
        attribute   "actionData", "json_object"
        
        
        // Commands used for Learning Mode
        command("actionStartLearning")
        command("actionSave")
        command("actionPauseUnpauseLearning")
        command "clear"
        command "tested"
    }

    preferences {
        
        // Default Preferences
        input(name: "runReset", description: "<i>For details and guidance, see the release thread in the <a href=\"https://community.hubitat.com/t/release-tasmota-7-x-firmware-with-hubitat-support/29368\"> Hubitat Forum</a>. For settings marked as ADVANCED, make sure you understand what they do before activating them. If settings are not reflected on the device, press the Configure button in this driver. Also make sure all settings really are saved and correct.<br/>Type RESET and then press 'Save Preferences' to DELETE all Preferences and return to DEFAULTS.</i>", title: "<b>Settings</b>", displayDuringSetup: false, type: "paragraph", element: "paragraph")
        generate_preferences(configuration_model_debug())
        generateLearningPreferences()
    }
}

public getDeviceInfoByName(infoName) { 
    // DO NOT EDIT: This is generated from the metadata!
    // TODO: Figure out how to get this from Hubitat instead of generating this?
    deviceInfo = ['name': 'Tasmota - RF/IR Smoke Detector (Child)', 'namespace': 'tasmota', 'author': 'Markus Liljergren', 'importURL': 'https://raw.githubusercontent.com/markus-li/Hubitat/release/drivers/expanded/tasmota-rf-ir-smoke-detector-child-expanded.groovy']
    //logging("deviceInfo[${infoName}] = ${deviceInfo[infoName]}", 1)
    return(deviceInfo[infoName])
}

// Methods for displaying the correct Learning Preferences and returning the 
// current Action Name
def generateLearningPreferences() {
    input(name: "learningMode", type: "bool", title: "<b>Learning Mode</b>", description: '<i>Activate this to enter Learning Mode. DO NOT ACTIVATE THIS once you have learned the codes of a device, they will have to be re-learned!</i>', displayDuringSetup: false, required: false)
    if(learningMode) {
        input(name: "actionCurrentName", type: "enum", title: "<b>Action To Learn</b>", 
              description: "<i>Select which Action to save to in Learn Mode.</i>", 
              options: ["Clear", "Tested", "Detected"], defaultValue: "Detected", 
              displayDuringSetup: false, required: false)
        input(name: "learningModeAdvanced", type: "bool", title: "<b>Advanced Learning Mode</b>", 
              description: '<i>Activate this to enable setting Advanced settings. Normally this is NOT needed, be careful!</i>', 
              defaultValue: false, displayDuringSetup: false, required: false)
        if(learningModeAdvanced) {
            input(name: "actionCodeSetManual", type: "string", title: "<b>Set Action Code Manually</b>", 
              description: '<i>WARNING! For ADVANCED users only!</i>', 
              displayDuringSetup: false, required: false)
            input(name: "actionResetAll", type: "bool", title: "<b>RESET all Saved Actions</b>", 
              description: '<i>WARNING! This will DELETE all saved/learned Actions!</i>', 
              defaultValue: false, displayDuringSetup: false, required: false)
        }
    }
}

def getCurrentActionName() {
    if(!binding.hasVariable('actionCurrentName') || 
      (binding.hasVariable('actionCurrentName') && actionCurrentName == null)) {
        logging("Doesn't have the action name defined... Using Detected!", 1)
        actionName = "Detected"
    } else {
        actionName = actionCurrentName
    }
    return(actionName)
}

/* These functions are unique to each driver */
void detected() {
    logging("detected()", 1)
    sendEvent(name: "smoke", value: "detected", isStateChange: true)
}

void clear() {
    logging("clear()", 1)
    sendEvent(name: "smoke", value: "clear", isStateChange: true)
}

void tested() {
    logging("tested()", 1)
    sendEvent(name: "smoke", value: "tested", isStateChange: true)
}

// These are called when Action occurs, called from actionHandler()
def detectedAction() {
    logging("detectedAction()", 1)
    detected()
}

def clearAction() {
    logging("clearAction()", 1)
    clear()
}

def testedAction() {
    logging("testedAction()", 1)
    tested()
}

/* Helper functions for Code Learning */
def actionStartLearning() {
    return(actionStartLearning(true))
}

def actionStartLearning(resetActionData) {
    def cmds = []
    if(learningMode) {
        actionName = getCurrentActionName()
        cmds << sendEvent(name: "status", value: "Learning Mode: Learning Action '${actionName}'.")
        logging("actionStartLearning", 1)
        cmds << sendEvent(name: "actionSeen", value: 0)
        if(resetActionData) cmds << sendEvent(name: "actionData", value: JsonOutput.toJson(null))
        actionSeen = device.currentValue('actionSeen', true)
        actionData = device.currentValue('actionData', true)
        logging("actionStartLearning actionData=${actionData}", 1)
    } else {
        log.warn "Learning Mode not active, can't start Learning!"
    }
    return cmds
}

def actionPauseUnpauseLearning() {
    def cmds = []
    // This will pause/unpause Learning, good for Contact sensors for example...
    status = device.currentValue('status', true)
    if(status == "Learning Mode: Paused") {
        actionName = getCurrentActionName()
        cmds << sendEvent(name: "status", value: "Learning Mode: Learning Action '${actionName}'.")
    } else {
        cmds << sendEvent(name: "status", value: "Learning Mode: Paused")
    }
    return cmds
}

def actionSave() {
    def cmds = []
    logging("actionSave()", 1)
    if(learningMode) {
        actionName = getCurrentActionName()
        def slurper = new JsonSlurper()
        actionData = device.currentValue('actionData', true)
        if(actionData != null) actionData = slurper.parseText(actionData)

        if(actionData && actionData != "Saved") {
            frequentData = null
            maxActionNumSeen = 0
            actionData.each {
                logging("it=${it}", 1)
                if(it.containsKey('data')) {
                    if(it.containsKey('seen') && it['seen'] >= maxActionNumSeen) {
                        maxActionNumSeen = it['seen']
                        frequentData = it['data']
                    }
                }
            }
            if(frequentData != null) {
                logging("actionSave() saving this data: '${frequentData}'", 100)
                //cmds << device.clearSetting('actionCodeDefault')
                //cmds << device.removeSetting('actionCodeDefault')
                //cmds << device.updateSetting('actionCodeDefault', frequentData)
                state.actions = state.actions ?: [:]
                state.actions[actionName] = frequentData
                cmds << sendEvent(name: "actionSeen", value: 0)
                cmds << sendEvent(name: "actionData", value: JsonOutput.toJson('Saved'))
                cmds << sendEvent(name: "status", value: "Learning Mode: Saved Action Code for Action '${actionName}'. Refresh the page to see it in State Variables.")
            } else {
                log.warn "No Action codes found in actionData!"
                cmds << sendEvent(name: "status", value: "Learning Mode: FAILED to save Action Code for Action '${actionName}'. See the log.")
            }
        } else {
            log.warn "No Action codes found!"
            if(actionData && actionData == "Saved") {
                // Do nothing...
            } else {
                cmds << sendEvent(name: "status", value: "Learning Mode: FAILED to save Action Code for Action '${actionName}'. See the log.")
            }
        }
    } else {
        log.warn "Learning Mode not active, can't save Action Code!"
    }
    return cmds
}


def actionLearn(data) {
    def cmds = []
    status = device.currentValue('status', true)
    if(status == "Learning Mode: Paused") return cmds
    actionName = getCurrentActionName()
    // Can't do this inside a mutex lock, but just don't press so quickly and it will be ok...
    def slurper = new JsonSlurper()
    actionSeen = device.currentValue('actionSeen', true)
    actionData = device.currentValue('actionData', true)
    if(actionData != null) actionData = slurper.parseText(actionData)

    if(actionSeen == null || actionSeen == 'null') {
        actionSeen = 0
    }
    if(actionData == null || actionData == 'null' || 
       actionData == 'Saved' || actionData == '"Saved"' ||
       actionData == 'N/A' || actionData == '"N/A"') {
        actionData = []
    }
    logging("actionSeen=${actionSeen}", 1)
    logging("actionData=${actionData}", 1)
    // All is same for all types, no need to have special cases...
    //if(data.type == 'parsed_portisch') {
    found = false
    actionData.each {
        logging("it=${it}", 1)
        if(it.containsKey('data') && data['Data'] == it['data']) {
            found = true
            it['seen'] = it['seen'] + 1
        }
        if(it.containsKey('seen') && actionSeen < it['seen']) actionSeen = it['seen']
    }
    if (!found) {
        actionData.add([seen: 1, data: data.Data])
        if (actionSeen < 1) actionSeen = 1
    }
    
    cmds << sendEvent(name: "status", value: "Learning Mode: Learning Action '${actionName}'. The most frequent Action seen ${actionSeen} time(s)!")
    cmds << sendEvent(name: "actionSeen", value: actionSeen)
    cmds << sendEvent(name: "actionData", value: JsonOutput.toJson(actionData))
    
    if (state.containsKey("events")) {
        state.remove("events")
    }
    return cmds
}

def actionHandler(data) {
    def cmds = []
    logging("actionHandler(data='${data}')", 1)
    actionName = getCurrentActionName()
    if(data && data.containsKey('Data') && state.actions) {
        // && data['Data'] == state.actions[actionName]
        currentData = data['Data']
        state.actions.each {
            if (it.value == currentData) {
                logging('Button pushed: ${it.value)', 1)
                "${it.key[0].toLowerCase() + it.key.substring(1)}Action"()
            }
        }
    }
    return cmds
}


def parseParentData(parentData) {
    def cmds = []
    //logging("parseParentData(parentData=${parentData})", 100)
    if (parentData.containsKey("type")) {
        if(parentData.type == 'parsed_portisch' || 
           parentData.type == 'raw_portisch' || 
           parentData.type == 'rflink') {
            logging("${parentData.type}=${parentData}", 100)
            if(learningMode) {
                cmds << actionLearn(parentData)
            } else {
                cmds << actionHandler(parentData)
            }
        } else {
            log.error("Unknown Format=${parentData}")
        }
    } else {
        log.error("Unknown parentData=${parentData}")
    }
    return cmds
}

void updated() {
    logging('Inside updated()...', 1)
    if(!learningMode) {
        sendEvent(name: "actionSeen", value: 0)
        sendEvent(name: "actionData", value: JsonOutput.toJson('N/A'))
        sendEvent(name: "status", value: "Action Mode")
    } else {
        //sendEvent(name: "actionSeen", value: 0)
        //sendEvent(name: "actionData", value: JsonOutput.toJson(null))
        //sendEvent(name: "status", value: "Learning Mode")
        actionStartLearning(false)  // Do NOT reset actionData
        if(learningModeAdvanced) {
            if(actionResetAll) {
                log.warn "ALL saved Actions have been DELETED!"
                state.actions = [:]
                device.clearSetting('actionResetAll')
                device.removeSetting('actionResetAll')
            }
            if(actionCodeSetManual && actionCodeSetManual != "") {
                actionName = getCurrentActionName()
                state.actions = state.actions ?: [:]
                state.actions[actionName] = actionCodeSetManual
                sendEvent(name: "actionSeen", value: 0)
                sendEvent(name: "actionData", value: JsonOutput.toJson('Saved'))
                sendEvent(name: "status", value: "Learning Mode: Saved Action Code for Action '${actionName}'. Refresh the page to see it in State Variables.")
                device.clearSetting('actionCodeSetManual')
                device.removeSetting('actionCodeSetManual')
            }
        }
    }
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

/* Default functions go here */
private def getDriverVersion() {
    comment = ""
    if(comment != "") state.comment = comment
    version = "v0.9.5T"
    logging("getDriverVersion() = ${version}", 50)
    sendEvent(name: "driver", value: version)
    updateDataValue('driver', version)
    return version
}


/* Helper functions included in all drivers/apps */
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

// Since refresh, with any number of arguments, is accepted as we always have it declared anyway, 
// we use it as a wrapper
// All our "normal" refresh functions take 0 arguments, we can declare one with 1 here...
def refresh(cmd) {
    deviceCommand(cmd)
}

def installed() {
	logging("installed()", 50)
    
	if(isDriver()) configure()
    try {
        // In case we have some more to run specific to this driver
        installedAdditional()
    } catch (MissingMethodException e) {
        // ignore
    }
}

/*
	initialize

	Purpose: initialize the driver/app
	Note: also called from updated() in most drivers/apps
*/
void initialize()
{
    logging("initialize()", 50)
	unschedule()
    // disable debug logs after 30 min, unless override is in place
	if (logLevel != "0") {
        if(runReset != "DEBUG") {
            log.warn "Debug logging will be disabled in 30 minutes..."
        } else {
            log.warn "Debug logging will NOT BE AUTOMATICALLY DISABLED!"
        }
        runIn(1800, logsOff)
    }
    try {
        // In case we have some more to run specific to this driver/app
        initializeAdditional()
    } catch (MissingMethodException e) {
        // ignore
    }
}

def configure() {
    logging("configure()", 50)
    def cmds = []
    if(isDriver()) {
        cmds = update_needed_settings()
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
                    title:"${makeTextBold(it.@label)}\n" + "${it.Help}",
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
                    title:"${makeTextBold(it.@label)}\n" + "${it.Help}",
                    description: makeTextItalic(it.@description),
                    defaultValue: "${it.@value}",
                    submitOnChange: it.@submitOnChange == "true",
                    displayDuringSetup: "${it.@displayDuringSetup}",
                    options: items)
            break
            case "password":
                input("${it.@index}", "password",
                    title:"${makeTextBold(it.@label)}\n" + "${it.Help}",
                    description: makeTextItalic(it.@description),
                    submitOnChange: it.@submitOnChange == "true",
                    displayDuringSetup: "${it.@displayDuringSetup}")
            break
            case "decimal":
               input("${it.@index}", "decimal",
                    title:"${makeTextBold(it.@label)}\n" + "${it.Help}",
                    description: makeTextItalic(it.@description),
                    range: "${it.@min}..${it.@max}",
                    defaultValue: "${it.@value}",
                    submitOnChange: it.@submitOnChange == "true",
                    displayDuringSetup: "${it.@displayDuringSetup}")
            break
            case "bool":
               input("${it.@index}", "bool",
                    title:"${makeTextBold(it.@label)}\n" + "${it.Help}",
                    description: makeTextItalic(it.@description),
                    defaultValue: "${it.@value}",
                    submitOnChange: it.@submitOnChange == "true",
                    displayDuringSetup: "${it.@displayDuringSetup}")
            break
        }
        }
    }
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
        if (logLevel != "0") runIn(1800, logsOff)
    }
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
    <Item label="Code Learning" value="100" />
</Value>
</configuration>
'''
    }
}

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
            if (level >= 0 && level <= 100)
                log.debug "$message"
        break
        case "1": // Very verbose
            if (level >= 1 && level < 99 || level == 100)
                log.debug "$message"
        break
        case "10": // A little less
            if (level >= 10 && level < 99 || level == 100)
                log.debug "$message"
        break
        case "50": // Rather chatty
            if (level >= 50 )
                log.debug "$message"
        break
        case "99": // Only parsing reports
            if (level >= 99 )
                log.debug "$message"
        break
        
        case "100": // Only special debug messages, eg IR and RF codes
            if (level == 100 )
                log.info "$message"
        break
        }
    }
}

