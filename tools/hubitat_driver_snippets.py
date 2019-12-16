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
import groovy.json.JsonSlurper"""

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
attribute   "uptime", "string"
attribute   "ip", "string"
attribute   "module", "string"
attribute   "templateData", "string"
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
input(description: "For details and guidance, see the release thread in the <a href=\\\"https://community.hubitat.com/t/release-tasmota-7-x-firmware-with-hubitat-support/29368\\\"> Hubitat Forum</a>.", title: "Settings", displayDuringSetup: false, type: "paragraph", element: "paragraph")
generate_preferences(configuration_model_debug())
"""

def getDefaultMetadataPreferencesForTasmota(includeTelePeriod=True):
    return """
// Default Preferences for Tasmota
input(name: "ipAddress", type: "string", title: "Device IP Address", description: "Set this as a default for the auto-discovery feature", displayDuringSetup: true, required: false)
input(name: "port", type: "number", title: "Device Port", description: "Port (default: 80)", displayDuringSetup: true, required: false, defaultValue: 80)
input(name: "override", type: "bool", title: "Override IP", description: "Override the automatically discovered IP address and disable auto-discovery.", displayDuringSetup: true, required: false)
input(name: "disableModuleSelection", type: "bool", title: "Disable Automatically Setting Module and Template", description: "Disable automatically setting the Module Type and Template in Tasmota. Enable for using custom Module or Template settings directly on the device. With this disabled, you need to set these settings manually on the device.", displayDuringSetup: true, required: false)
""" + ("""input(name: "telePeriod", type: "string", title: "Update Frequency", description: "Tasmota sensor value update interval, set this to any value between 10 and 3600 seconds. See the Tasmota docs concerning telePeriod for details. This is NOT a poll frequency. Button/switch changes are immediate and are NOT affected by this. This is for SENSORS only. (default = 300)", displayDuringSetup: true, required: false)""" if includeTelePeriod else "") + """
generate_preferences(configuration_model_tasmota())
input(name: "useIPAsID", type: "bool", title: "EXPERTS ONLY: IP as Network ID", description: "Not needed under normal circumstances. Setting this when not needed can break updates. This requires the IP to be static or set to not change in your DHCP server. It will force the use of IP as network ID. When in use, set Override IP to true and input the correct Device IP Address. See the release thread in the Hubitat forum for details and guidance.", displayDuringSetup: true, required: false)
"""

def getUpdateNeededSettingsTasmotaHeader():
    return """// updateNeededSettings() Generic header BEGINS here
def cmds = []
def currentProperties = state.currentProperties ?: [:]

state.settings = settings

def configuration = new XmlSlurper().parseText(configuration_model_tasmota())
def isUpdateNeeded = "NO"

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

def getUpdateNeededSettingsTasmotaDynamicModuleCommand():
    return '''
// Tasmota Module and Template selection command (autogenerated)
cmds << getAction(getCommandString("Module", null))
cmds << getAction(getCommandString("Template", null))
if(disableModuleSelection == null) disableModuleSelection = false
if(disableModuleSelection == false && deviceTemplateInput != null && deviceTemplateInput != "") {
    logging("Setting the Template soon...", 10)
    logging(device.currentValue('templateData'), 10)
    moduleNumber = 0  // This activates the Template when set
    // TODO: Remove all SPACES from deviceTemplate
    if(device.currentValue('templateData') != null && device.currentValue('templateData') != deviceTemplateInput) {
        logging("The template is NOT set to '${deviceTemplateInput}', it is set to '${device.currentValue('templateData')}'",10)
        urlencodedTemplate = URLEncoder.encode(deviceTemplateInput)
        cmds << getAction(getCommandString("Template", "${urlencodedTemplate}"))
    } else {
        logging("The template is set to '${deviceTemplateInput}' already!",10)
    }
} else {
    logging("Can't set the Template soon...", 10)
    logging(device.currentValue('templateData'), 10)
    logging("deviceTemplateInput: '${deviceTemplateInput}'", 10)
    logging("disableModuleSelection: '${disableModuleSelection}'", 10)
}
if(disableModuleSelection == false && moduleNumber != null && moduleNumber >= 0) {
    logging("Setting the Module soon...", 10)
    logging("device.currentValue('module'): '${device.currentValue('module')}'", 10)
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
