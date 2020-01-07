#  Copyright 2019 Markus Liljergren
#
#  Licensed under the Apache License, Version 2.0 (the "License");
#  you may not use this file except in compliance with the License.
#  You may obtain a copy of the License at
#  http://www.apache.org/licenses/LICENSE-2.0
#
#  Unless required by applicable law or agreed to in writing, software
#  distributed under the License is distributed on an "AS IS" BASIS,
#  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
#  See the License for the specific language governing permissions and
#  limitations under the License.

driverVersion = "v0.9.2 for Tasmota 7.x (Hubitat version)"

from hubitat_codebuilder import HubitatCodeBuilderError

"""
  Snippets used by hubitat-driver-helper-tool
"""

def getHeaderLicense():
    return """ /**
 *  Copyright 2019 Markus Liljergren
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
 */"""

def getDefaultImports():
    return """/* Default imports */
import groovy.json.JsonSlurper
import groovy.json.JsonOutput
"""

def getDefaultMetadataCapabilities():
    return """
// Default Capabilities
capability "Refresh"
capability "Configuration"
"""

def getDefaultMetadataCapabilitiesForEnergyMonitor():
    return """
// Default Capabilities for Energy Monitor
capability "Voltage Measurement"
capability "Power Meter"
capability "Energy Meter"
"""

def getDefaultMetadataCapabilitiesForTHMonitor():
    return """
// Default Capabilities for TH Monitor
capability "Sensor"
capability "Temperature Measurement"
capability "Relative Humidity Measurement"
capability "PressureMeasurement"
"""

def getDefaultMetadataAttributes():
    return """
// Default Attributes
attribute   "needUpdate", "string"
//attribute   "uptime", "string"  // This floods the event log!
attribute   "ip", "string"
attribute   "ipLink", "string"
attribute   "module", "string"
attribute   "templateData", "string"
attribute   "driverVersion", "string"
"""

def getDefaultMetadataAttributesForEnergyMonitor():
    return """
// Default Attributes for Energy Monitor
attribute   "current", "string"
attribute   "apparentPower", "string"
attribute   "reactivePower", "string"
attribute   "powerFactor", "string"
attribute   "energyToday", "string"
attribute   "energyYesterday", "string"
attribute   "energyTotal", "string"
attribute   "voltageStr", "string"
attribute   "powerStr", "string"
"""

def getDefaultMetadataAttributesForTHMonitor():
    return """
// Default Attributes for Temperature Humidity Monitor
attribute   "pressureWithUnit", "string"
"""

def getLearningModeAttributes():
    return """
// Attributes used for Learning Mode
attribute   "status", "string"
attribute   "actionSeen", "number"
attribute   "actionData", "json_object"
"""

def getDefaultMetadataCommands():
    return """
// Default Commands
command "reboot"
"""

def getMetadataCommandsForHandlingChildDevices():
    return """
// Commands for handling Child Devices
command "childOn"
command "childOff"
command "recreateChildDevices"
command "deleteChildren"
"""

def getMetadataCommandsForHandlingRGBWDevices():
    return """
// Commands for handling RGBW Devices
command "white"
command "red"
command "green"
command "blue"
"""

def getMetadataCommandsForHandlingTasmotaRGBWDevices():
    return """
// Commands for handling Tasmota RGBW Devices
command "modeNext"
command "modePrevious"
command "modeSingleColor"
command "modeWakeUp"
command "modeCycleUpColors"
command "modeCycleDownColors"
command "modeRandomColors"
"""

def getLearningModeCommands():
    return """
// Commands used for Learning Mode
command("actionStartLearning")
command("actionSave")
command("actionPauseUnpauseLearning")
"""

def getDefaultMetadataPreferences():
    #input(description: "Once you change values on this page, the corner of the 'configuration' icon will change to orange until all configuration parameters are updated.", title: "Settings", displayDuringSetup: false, type: "paragraph", element: "paragraph")
    return """
// Default Preferences
input(name: "runReset", description: "<i>For details and guidance, see the release thread in the <a href=\\\"https://community.hubitat.com/t/release-tasmota-7-x-firmware-with-hubitat-support/29368\\\"> Hubitat Forum</a>. For settings marked as ADVANCED, make sure you understand what they do before activating them. If settings are not reflected on the device, press the Configure button in this driver. Also make sure all settings really are saved and correct.<br/>Type RESET and then press 'Save Preferences' to DELETE all Preferences and return to DEFAULTS.</i>", title: "<b>Settings</b>", displayDuringSetup: false, type: "paragraph", element: "paragraph")
generate_preferences(configuration_model_debug())
"""

def getDefaultMetadataPreferencesForTasmota(includeTelePeriod=True):
    return """
// Default Preferences for Tasmota
input(name: "ipAddress", type: "string", title: "<b>Device IP Address</b>", description: "<i>Set this as a default fallback for the auto-discovery feature.</i>", displayDuringSetup: true, required: false)
input(name: "port", type: "number", title: "<b>Device Port</b>", description: "<i>The http Port of the Device (default: 80)</i>", displayDuringSetup: true, required: false, defaultValue: 80)
input(name: "override", type: "bool", title: "<b>Override IP</b>", description: "<i>Override the automatically discovered IP address and disable auto-discovery.</i>", displayDuringSetup: true, required: false)
""" + ("""input(name: "telePeriod", type: "string", title: "<b>Update Frequency</b>", description: "<i>Tasmota sensor value update interval, set this to any value between 10 and 3600 seconds. See the Tasmota docs concerning telePeriod for details. This is NOT a poll frequency. Button/switch changes are immediate and are NOT affected by this. This ONLY affects SENSORS and reporting of data such as UPTIME. (default = 300)</i>", displayDuringSetup: true, required: false)""" if includeTelePeriod else "") + """
generate_preferences(configuration_model_tasmota())
input(name: "disableModuleSelection", type: "bool", title: "<b>Disable Automatically Setting Module and Template</b>", description: "ADVANCED: <i>Disable automatically setting the Module Type and Template in Tasmota. Enable for using custom Module or Template settings directly on the device. With this disabled, you need to set these settings manually on the device.</i>", displayDuringSetup: true, required: false)
input(name: "moduleNumber", type: "number", title: "<b>Module Number</b>", description: "ADVANCED: <i>Module Number used in Tasmota. If Device Template is set, this value is IGNORED. (default: -1 (use the default for the driver))</i>", displayDuringSetup: true, required: false, defaultValue: -1)
input(name: "deviceTemplateInput", type: "string", title: "<b>Device Template</b>", description: "ADVANCED: <i>Set this to a Device Template for Tasmota, leave it EMPTY to use the driver default. Set it to 0 to NOT use a Template. NAME can be maximum 14 characters! (Example: {\\\"NAME\\\":\\\"S120\\\",\\\"GPIO\\\":[0,0,0,0,0,21,0,0,0,52,90,0,0],\\\"FLAG\\\":0,\\\"BASE\\\":18})</i>", displayDuringSetup: true, required: false)
input(name: "useIPAsID", type: "bool", title: "<b>IP as Network ID</b>", description: "ADVANCED: <i>Not needed under normal circumstances. Setting this when not needed can break updates. This requires the IP to be static or set to not change in your DHCP server. It will force the use of IP as network ID. When in use, set Override IP to true and input the correct Device IP Address. See the release thread in the Hubitat forum for details and guidance.</i>", displayDuringSetup: true, required: false)
"""

def getDefaultMetadataPreferencesForTHMonitor():
    return """
// Default Preferences for Temperature Humidity Monitor
input(name: "tempOffset", type: "decimal", title: "<b>Temperature Offset</b>", description: "<i>Adjust the temperature by this many degrees (in Celcius).</i>", displayDuringSetup: true, required: false, range: "*..*")
input(name: "humidityOffset", type: "decimal", title: "<b>Humidity Offset</b>", description: "<i>Adjust the humidity by this many percent.</i>", displayDuringSetup: true, required: false, range: "*..*")
input(name: "pressureOffset", type: "decimal", title: "<b>Pressure Offset</b>", description: "<i>Adjust the pressure value by this much.</i>", displayDuringSetup: true, required: false, range: "*..*")
input(name: "tempRes", type: "enum", title: "<b>Temperature Resolution</b>", description: "<i>Temperature sensor resolution (0..3 = maximum number of decimal places, default: 1)<br/>NOTE: If the 3rd decimal is a 0 (eg. 24.720) it will show without the last decimal (eg. 24.72).</i>", options: ["0", "1", "2", "3"], defaultValue: "1", displayDuringSetup: true, required: false)
"""

def getDefaultMetadataPreferencesForParentDevices(numSwitches=1):
    return '''
// Default Preferences for Parent Devices
input(name: "numSwitches", type: "enum", title: "<b>Number of Relays</b>", description: "<i>Set the number of buttons/relays on the device (default ''' + str(numSwitches) + ''')</i>", options: ["1", "2", "3", "4", "5", "6"], defaultValue: "''' + str(numSwitches) + '''", displayDuringSetup: true, required: true)
'''

def getDefaultMetadataPreferencesForParentDevicesWithUnlimitedChildren(numSwitches=1):
    return '''
// Default Preferences for Parent Devices
input(name: "numSwitches", type: "number", title: "<b>Number of Children</b>", description: "<i>Set the number of children (default ''' + str(numSwitches) + ''')</i>", defaultValue: "''' + str(numSwitches) + '''", displayDuringSetup: true, required: true)
'''

def getUpdateNeededSettingsTasmotaHeader():
    return """// updateNeededSettings() Generic header BEGINS here
def cmds = []
def currentProperties = state.currentProperties ?: [:]

state.settings = settings

def configuration = new XmlSlurper().parseText(configuration_model_tasmota())
def isUpdateNeeded = "NO"

if(runReset != null && runReset == 'RESET') {
    for ( e in state.settings ) {
        logging("Deleting '${e.key}' with value = ${e.value} from Settings", 50)
        // Not sure which ones are needed, so doing all...
        device.clearSetting("${e.key}")
        device.removeSetting("${e.key}")
        state.settings.remove("${e.key}")
    }
}

prepareDNI()

// updateNeededSettings() Generic header ENDS here
"""

def getUpdateNeededSettingsTasmotaModuleCommand(moduleNumber):
    return '''
// Tasmota Module selection command (autogenerated)
moduleNumber = '''+str(moduleNumber)+'''
cmds << getAction(getCommandString("Module", null))
cmds << getAction(getCommandString("Template", null))
if(disableModuleSelection == null) disableModuleSelection = false
if(disableModuleSelection == false) {
    logging("Setting the Module soon...", 10)
    logging(device.currentValue('module'), 10)
    if(device.currentValue('module') != null && !device.currentValue('module').startsWith("[${moduleNumber}:")) {
        logging("This DOESN'T start with [${moduleNumber} ${device.currentValue('module')}",10)
        cmds << getAction(getCommandString("Module", "${moduleNumber}"))
    } else {
        logging("This starts with [${moduleNumber} ${device.currentValue('module')}",10)
    }
} else {
    logging("Setting the Module has been disabled!", 10)
}
'''

def getUpdateNeededSettingsTasmotaDynamicModuleCommand(moduleNumber = -1, defaultDeviceTemplate = ''):
    return """
// Tasmota Module and Template selection command (autogenerated)
cmds << getAction(getCommandString("Module", null))
cmds << getAction(getCommandString("Template", null))
if(disableModuleSelection == null) disableModuleSelection = false
moduleNumberUsed = moduleNumber
if(moduleNumber == null || moduleNumber == -1) moduleNumberUsed = """+str(moduleNumber)+"""
useDefaultTemplate = false
defaultDeviceTemplate = ''
if(deviceTemplateInput != null && deviceTemplateInput == "0") {
    useDefaultTemplate = true
    defaultDeviceTemplate = ''
}
if(deviceTemplateInput == null || deviceTemplateInput == "") {
    // We should use the default of the driver
    useDefaultTemplate = true
    defaultDeviceTemplate = '""" + defaultDeviceTemplate + """'
}
if(deviceTemplateInput != null) deviceTemplateInput = deviceTemplateInput.replaceAll(' ','')
if(disableModuleSelection == false && ((deviceTemplateInput != null && deviceTemplateInput != "") || 
                                       (useDefaultTemplate && defaultDeviceTemplate != ""))) {
    if(useDefaultTemplate == false && deviceTemplateInput != null && deviceTemplateInput != "") {
        usedDeviceTemplate = deviceTemplateInput
    } else {
        usedDeviceTemplate = defaultDeviceTemplate
    }
    logging("Setting the Template soon...", 10)
    logging("templateData = ${device.currentValue('templateData')}", 10)
    if(usedDeviceTemplate != '') moduleNumberUsed = 0  // This activates the Template when set
    if(usedDeviceTemplate != null && device.currentValue('templateData') != null && device.currentValue('templateData') != usedDeviceTemplate) {
        logging("The template is NOT set to '${usedDeviceTemplate}', it is set to '${device.currentValue('templateData')}'",10)
        urlencodedTemplate = URLEncoder.encode(usedDeviceTemplate).replace("+", "%20")
        // The NAME part of th Device Template can't exceed 14 characters! More than that and they will be truncated.
        // TODO: Parse and limit the size of NAME
        cmds << getAction(getCommandString("Template", "${urlencodedTemplate}"))
    } else if (device.currentValue('module') == null){
        // Update our stored value!
        cmds << getAction(getCommandString("Template", null))
    }else if (usedDeviceTemplate != null) {
        logging("The template is set to '${usedDeviceTemplate}' already!",10)
    }
} else {
    logging("Can't set the Template...", 10)
    logging(device.currentValue('templateData'), 10)
    //logging("deviceTemplateInput: '${deviceTemplateInput}'", 10)
    //logging("disableModuleSelection: '${disableModuleSelection}'", 10)
}
if(disableModuleSelection == false && moduleNumberUsed != null && moduleNumberUsed >= 0) {
    logging("Setting the Module soon...", 10)
    logging("device.currentValue('module'): '${device.currentValue('module')}'", 10)
    if(moduleNumberUsed != null && device.currentValue('module') != null && !device.currentValue('module').startsWith("[${moduleNumberUsed}:")) {
        logging("This DOESN'T start with [${moduleNumberUsed} ${device.currentValue('module')}",10)
        cmds << getAction(getCommandString("Module", "${moduleNumberUsed}"))
    } else if (moduleNumberUsed != null && device.currentValue('module') != null){
        logging("This starts with [${moduleNumberUsed} ${device.currentValue('module')}",10)
    } else if (device.currentValue('module') == null){
        // Update our stored value!
        cmds << getAction(getCommandString("Module", null))
    } else {
        logging("Module is set to '${device.currentValue('module')}', and it's set to be null, report this to the creator of this driver!",10)
    }
} else {
    logging("Setting the Module has been disabled!", 10)
}
"""

def getUpdateNeededSettingsTelePeriod(forcedTelePeriod=None):
    if (forcedTelePeriod==None):
        return """
// updateNeededSettings() TelePeriod setting
cmds << getAction(getCommandString("TelePeriod", (telePeriod == '' || telePeriod == null ? "300" : telePeriod)))
"""
    else:
        return '''
// updateNeededSettings() TelePeriod setting
cmds << getAction(getCommandString("TelePeriod", "''' + str(forcedTelePeriod) + '''"))
'''

def getUpdateNeededSettingsTHMonitor():
    return """
// updateNeededSettings() Temperature/Humidity/Pressure setting
cmds << getAction(getCommandString("TempRes", (tempRes == '' || tempRes == null ? "1" : tempRes)))
"""

def getUpdateNeededSettingsTasmotaFooter():
    return """
// updateNeededSettings() Generic footer BEGINS here
cmds << getAction(getCommandString("SetOption113", "1")) // Hubitat Enabled
// Disabling Emulation so that we don't flood the logs with upnp traffic
//cmds << getAction(getCommandString("Emulation", "0")) // Emulation Disabled
cmds << getAction(getCommandString("HubitatHost", device.hub.getDataValue("localIP")))
logging("HubitatPort: ${device.hub.getDataValue("localSrvPortTCP")}", 1)
cmds << getAction(getCommandString("HubitatPort", device.hub.getDataValue("localSrvPortTCP")))
cmds << getAction(getCommandString("FriendlyName1", URLEncoder.encode(device.displayName.take(32)))) // Set to a maximum of 32 characters

if(override == true) {
    cmds << sync(ipAddress)
}

//logging("Cmds: " +cmds,1)
sendEvent(name:"needUpdate", value: isUpdateNeeded, displayed:false, isStateChange: false)
return cmds
// updateNeededSettings() Generic footer ENDS here
"""

#configuration.Value.each
#{     
#    if ("${it.@setting_type}" == "lan" && it.@disabled != "true"){
#        if (currentProperties."${it.@index}" == null)
#        {
#            if (it.@setonly == "true"){
#                logging("Setting ${it.@index} will be updated to ${it.@value}", 2)
#                cmds << getAction("/configSet?name=${it.@index}&value=${it.@value}")
#            } else {
#                isUpdateNeeded = "YES"
#                logging("Current value of setting ${it.@index} is unknown", 2)
#                cmds << getAction("/configGet?name=${it.@index}")
#            }
#        }
#        else if ((settings."${it.@index}" != null || it.@hidden == "true") && currentProperties."${it.@index}" != (settings."${it.@index}" != null? settings."${it.@index}".toString() : "${it.@value}"))
#        { 
#            isUpdateNeeded = "YES"
#            logging("Setting ${it.@index} will be updated to ${settings."${it.@index}"}", 2)
#            cmds << getAction("/configSet?name=${it.@index}&value=${settings."${it.@index}"}")
#        } 
#    }
#}

def getGenericOnOffFunctions():
    return """
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
"""

def getRGBWOnOffFunctions():
    return """
/* RGBW On/Off functions used when only 1 switch/button exists */
def on() {
	logging("on()", 50)
    def cmds = []
    h = null
    s = null
    b = 100
    if(state != null) {
        //h = state.containsKey("hue") ? state.hue : null
        //s = state.containsKey("saturation") ? state.saturation : null
        b = state.containsKey("level") ? state.level : 100
    }
    if(b < 20) b = 20
    cmds << setHSB(h, s, b)
    cmds << getAction(getCommandString("Power", "On"))
    return cmds
}

def off() {
    logging("off()", 50)
	def cmds = []
    cmds << getAction(getCommandString("Power", "Off"))
    return cmds
}
"""

def getDefaultFunctions(comment=""):
    return '''/* Default functions go here */
private def getDriverVersion() {
    logging("getDriverVersion()", 50)
	def cmds = []
    comment = "''' + comment + '''"
    if(comment != "") state.comment = comment
    sendEvent(name: "driverVersion", value: "''' + driverVersion + '''")
    return cmds
}
'''

def getLoggingFunction(specialDebugLevel=False):
    extraDebug = ""
    if(specialDebugLevel):
        extraDebug = """
        case "100": // Only special debug messages, eg IR and RF codes
            if (level == 100 )
                log.debug "$message"
        break
        """

    return """/* Logging function included in all drivers */
private def logging(message, level) {
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
        """ + extraDebug + """}
    }
}
"""

def getSpecialDebugEntry(label=None):
    if(label==None):
        return("")
    else:
        return '<Item label="' + label + '" value="100" />'

def getCreateChildDevicesCommand(childType='component'):
    #childType == 'not_component' should 
    start = "try {\n"
    end = """
        } catch (com.hubitat.app.exception.UnknownDeviceTypeException e) {
            log.error "'${getChildDriverName()}' driver can't be found! Did you forget to install the child driver?"
        }"""
    if(childType=='component'):
        #return('addChildDevice("${getDeviceInfoByName("namespace")}", "${getChildDriverName()}", "$device.id-$i", [name: "$device.name #$i", label: "$device.displayName $i", isComponent: true])')
        
        return(start + 'addChildDevice("${getDeviceInfoByName("namespace")}", "${getChildDriverName()}", "$device.id-$i", [name: "${getFilteredDeviceDriverName()} #$i", label: "${getFilteredDeviceDisplayName()} $i", isComponent: true])' + end)
    elif(childType=='not_component'):
        return(start + 'addChildDevice("${getDeviceInfoByName("namespace")}", "${getChildDriverName()}", "$device.id-$i", [name: "${getFilteredDeviceDriverName()} #$i", label: "${getFilteredDeviceDisplayName()} $i", isComponent: false])' + end)
    else:
        raise HubitatCodeBuilderError('Unknown childType specified in getcreateChildDevicesCommand(childType={})'.format(str(childType)))

def getGetChildDriverNameMethod(childDriverName='default'):
    if(childDriverName == 'default'):
        return """def getChildDriverName() {
    deviceDriverName = getDeviceInfoByName('name')
    if(deviceDriverName.toLowerCase().endsWith(' (parent)')) {
        deviceDriverName = deviceDriverName.substring(0, deviceDriverName.length()-9)
    }
    childDriverName = "${deviceDriverName} (Child)"
    logging("childDriverName = '$childDriverName'", 1)
    return(childDriverName)
}"""
    else:
        return """def getChildDriverName() {
    childDriverName = '""" + childDriverName + """ (Child)'
    logging("childDriverName = '$childDriverName'", 1)
    return(childDriverName)
}"""

def getCalculateB0():
    return """def calculateB0(inputStr, repeats) {
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
}"""

def getGenerateLearningPreferences(types='["Default", "Toggle", "Push", "On", "Off"]', default_type='Default'):
    return '''// Methods for displaying the correct Learning Preferences and returning the 
// current Action Name
def generateLearningPreferences() {
    input(name: "learningMode", type: "bool", title: "<b>Learning Mode</b>", description: '<i>Activate this to enter Learning Mode. DO NOT ACTIVATE THIS once you have learned the codes of a device, they will have to be re-learned!</i>', displayDuringSetup: false, required: false)
    if(learningMode) {
        input(name: "actionCurrentName", type: "enum", title: "<b>Action To Learn</b>", 
              description: "<i>Select which Action to save to in Learn Mode.</i>", 
              options: ''' + types + ''', defaultValue: "''' + default_type + '''", 
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
        logging("Doesn't have the action name defined... Using ''' + default_type + '''!", 1)
        actionName = "''' + default_type + '''"
    } else {
        actionName = actionCurrentName
    }
    return(actionName)
}'''