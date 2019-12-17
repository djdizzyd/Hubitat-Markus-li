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

driverVersion = "v0.9.0 for Tasmota 7.x (Hubitat version)"

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
"""

def getDefaultMetadataCapabilities():
    return """
// Default Capabilities
capability "Refresh"
capability "Configuration"
capability "HealthCheck"
"""

def getDefaultMetadataCapabilitiesForEnergyMonitor():
    return """
// Default Capabilities for Energy Monitor
capability "Voltage Measurement"
capability "Power Meter"
capability "Energy Meter"
"""

def getDefaultMetadataAttributes():
    return """
// Default Attributes
attribute   "needUpdate", "string"
//attribute   "uptime", "string"  // This floods the event log!
attribute   "ip", "string"
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
    logging(device.currentValue('templateData'), 10)
    if(usedDeviceTemplate != '') moduleNumberUsed = 0  // This activates the Template when set
    if(device.currentValue('templateData') != null && device.currentValue('templateData') != usedDeviceTemplate) {
        logging("The template is NOT set to '${usedDeviceTemplate}', it is set to '${device.currentValue('templateData')}'",10)
        urlencodedTemplate = URLEncoder.encode(usedDeviceTemplate).replace("+", "%20")
        // The NAME part of th Device Template can't exceed 14 characters! More than that and they will be truncated.
        // TODO: Parse and limit the size of NAME
        cmds << getAction(getCommandString("Template", "${urlencodedTemplate}"))
    } else {
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
    if(device.currentValue('module') != null && !device.currentValue('module').startsWith("[${moduleNumberUsed}:")) {
        logging("This DOESN'T start with [${moduleNumberUsed} ${device.currentValue('module')}",10)
        cmds << getAction(getCommandString("Module", "${moduleNumberUsed}"))
    } else {
        logging("This starts with [${moduleNumberUsed} ${device.currentValue('module')}",10)
    }
} else {
    logging("Setting the Module has been disabled!", 10)
}
"""

def getUpdateNeededSettingsTelePeriod():
    return """
// updateNeededSettings() TelePeriod setting
cmds << getAction(getCommandString("TelePeriod", (telePeriod == '' || telePeriod == null ? "300" : telePeriod)))
"""

def getUpdateNeededSettingsTasmotaFooter():
    return """
// updateNeededSettings() Generic footer BEGINS here
cmds << getAction(getCommandString("SetOption113", "1")) // Hubitat Enabled
cmds << getAction(getCommandString("HubitatHost", device.hub.getDataValue("localIP")))
cmds << getAction(getCommandString("HubitatPort", device.hub.getDataValue("localSrvPortTCP")))
cmds << getAction(getCommandString("FriendlyName1", URLEncoder.encode(device.displayName.take(32)))) // Set to a maximum of 32 characters

if(override == true) {
    cmds << sync(ipAddress)
}

//logging("Cmds: " +cmds,1)
sendEvent(name:"needUpdate", value: isUpdateNeeded, displayed:false, isStateChange: true)
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

def getDefaultFunctions():
    return '''/* Default functions go here */
private def getDriverVersion() {
    logging("getDriverVersion()", 50)
	def cmds = []
    sendEvent(name: "driverVersion", value: "''' + driverVersion + '''")
    return cmds
}
'''

def getLoggingFunction():
    return """/* Logging function included in all drivers */
private def logging(message, level) {
    if (logLevel != "0"){
        switch (logLevel) {
        case "-1": // Insanely verbose
            if (level >= 0 && level < 99)
                log.debug "$message"
        break
        case "1": // Very verbose
            if (level >= 1 && level < 99)
                log.debug "$message"
        break
        case "10": // A little less
            if (level >= 10 && level < 99)
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
        }
    }
}
"""
