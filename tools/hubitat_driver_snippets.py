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

from datetime import date

driverVersion = "v1.0.MMDDTa"

def getDriverVersion(driverVersionSpecial=None):
    if(driverVersionSpecial != None):
        driver_version_current = driverVersionSpecial
    else:
        driver_version_current = driverVersion
    if(driver_version_current.find("MMDD") != -1):
        driver_version_current = driver_version_current.replace("MMDD", date.today().strftime("%m%d"))
    return driver_version_current

from hubitat_codebuilder import HubitatCodeBuilderError

"""
  Snippets used by hubitat-driver-helper-tool
"""

def getHeaderLicense():
    return """/**
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
 */"""

def getDefaultImports():
    return """/** Default Imports */
import groovy.json.JsonSlurper
import groovy.json.JsonOutput
// Used for MD5 calculations
import java.security.MessageDigest
//import groovy.transform.TypeChecked
//import groovy.transform.TypeCheckingMode
"""

def getDefaultParentImports():
    return getDefaultImports() + """/* Default Parent Imports */
"""

def getUpdateNeededSettingsTasmotaHeader():
    return """// updateNeededSettings() Generic header BEGINS here
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
        state?.settings?.remove("${e.key}")
    }
}

prepareDNI()

// updateNeededSettings() Generic header ENDS here
"""

def getUpdateNeededSettingsTasmotaModuleCommand(moduleNumber):
    return '''
// Tasmota Module selection command (autogenerated)
moduleNumber = '''+str(moduleNumber)+'''   // Defined earlier
getAction(getCommandString("Module", null))
getAction(getCommandString("Template", null))
if(disableModuleSelection == null) disableModuleSelection = false
if(disableModuleSelection == false) {
    logging("Setting the Module soon...", 10)
    logging(device.currentValue('module'), 10)
    if(device.currentValue('module') != null && !device.currentValue('module').startsWith("[${moduleNumber}:")) {
        logging("This DOESN'T start with [${moduleNumber} ${device.currentValue('module')}",10)
        getAction(getCommandString("Module", "${moduleNumber}"))
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
getAction(getCommandString("Module", null))
getAction(getCommandString("Template", null))
if(disableModuleSelection == null) disableModuleSelection = false
def moduleNumberUsed = moduleNumber
if(moduleNumber == null || moduleNumber == -1) moduleNumberUsed = """+str(moduleNumber)+"""
boolean useDefaultTemplate = false
def defaultDeviceTemplate = ''
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
    def usedDeviceTemplate = defaultDeviceTemplate
    if(useDefaultTemplate == false && deviceTemplateInput != null && deviceTemplateInput != "") {
        usedDeviceTemplate = deviceTemplateInput
    }
    logging("Setting the Template soon...", 10)
    logging("templateData = ${device.currentValue('templateData')}", 10)
    if(usedDeviceTemplate != '') moduleNumberUsed = 0  // This activates the Template when set
    if(usedDeviceTemplate != null && device.currentValue('templateData') != null && device.currentValue('templateData') != usedDeviceTemplate) {
        logging("The template is NOT set to '${usedDeviceTemplate}', it is set to '${device.currentValue('templateData')}'",10)
        // The NAME part of th Device Template can't exceed 14 characters! More than that and they will be truncated.
        // TODO: Parse and limit the size of NAME???
        getAction(getCommandString("Template", usedDeviceTemplate))
    } else if (device.currentValue('module') == null){
        // Update our stored value!
        getAction(getCommandString("Template", null))
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
    if(moduleNumberUsed != null && device.currentValue('module') != null && !(device.currentValue('module').startsWith("[${moduleNumberUsed}:") || device.currentValue('module') == '0')) {
        logging("This DOESN'T start with [${moduleNumberUsed} ${device.currentValue('module')}",10)
        getAction(getCommandString("Module", "${moduleNumberUsed}"))
    } else if (moduleNumberUsed != null && device.currentValue('module') != null){
        logging("This starts with [${moduleNumberUsed} ${device.currentValue('module')}",10)
    } else if (device.currentValue('module') == null){
        // Update our stored value!
        getAction(getCommandString("Module", null))
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
getAction(getCommandString("TelePeriod", (telePeriod == '' || telePeriod == null ? "300" : telePeriod)))
"""
    else:
        return '''
// updateNeededSettings() TelePeriod setting
getAction(getCommandString("TelePeriod", "''' + str(forcedTelePeriod) + '''"))
'''

def getUpdateNeededSettingsTHMonitor():
    return """
// updateNeededSettings() Temperature/Humidity/Pressure setting
getAction(getCommandString("TempRes", (tempRes == '' || tempRes == null ? "1" : tempRes)))
"""

def getUpdateNeededSettingsTasmotaFooter():
    return """
getAction(getCommandString("TelePeriod", "${getTelePeriodValue()}"))
// updateNeededSettings() Generic footer BEGINS here
getAction(getCommandString("SetOption113", "1")) // Hubitat Enabled
// Disabling Emulation so that we don't flood the logs with upnp traffic
//getAction(getCommandString("Emulation", "0")) // Emulation Disabled
getAction(getCommandString("HubitatHost", device.hub.getDataValue("localIP")))
logging("HubitatPort: ${device.hub.getDataValue("localSrvPortTCP")}", 1)
getAction(getCommandString("HubitatPort", device.hub.getDataValue("localSrvPortTCP")))
getAction(getCommandString("FriendlyName1", device.displayName.take(32))) // Set to a maximum of 32 characters

if(override == true) {
    sync(ipAddress)
}

//logging("Cmds: " +cmds,1)
sendEvent(name:"needUpdate", value: isUpdateNeeded, displayed:false, isStateChange: false)
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
    def h = null
    def s = null
    def b = 100
    if(state != null) {
        //h = state.containsKey("hue") ? state.hue : null
        //s = state.containsKey("saturation") ? state.saturation : null
        b = state.containsKey("level") ? state.level : 100
    }
    if(b < 20) b = 20
    if(state.colorMode == "CT") {
        state.level = b
        cmds << setColorTemperature(colorTemperature ? colorTemperature : 3000)
        cmds << setLevel(state.level, 0)
    } else {
        cmds << setHSB(h, s, b)
    }
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

def getDefaultFunctions(comment="", driverVersionSpecial=None):
    driverVersionActual = getDriverVersion(driverVersionSpecial)
    return '''/* Default Driver Methods go here */
private String getDriverVersion() {
    //comment = "''' + comment + '''"
    //if(comment != "") state.comment = comment
    String version = "''' + driverVersionActual + '''"
    logging("getDriverVersion() = ${version}", 50)
    sendEvent(name: "driver", value: version)
    updateDataValue('driver', version)
    return version
}
'''

def getDefaultAppMethods(driverVersionSpecial=None):
    driverVersionActual = getDriverVersion(driverVersionSpecial)
    return '''/* Default App Methods go here */
private String getAppVersion() {
    String version = "''' + driverVersionActual + '''"
    logging("getAppVersion() = ${version}", 50)
    return version
}
'''

def getLoggingFunction(specialDebugLevel=False):
    extraDebug = ""
    if(specialDebugLevel):
        extraDebug = """
        case "100": // Only special debug messages, eg IR and RF codes
            if (level == 100 ) {
                log.info "$message"
                didLogging = true
            }
        break
        """

    return """/* Logging function included in all drivers */
private boolean logging(message, level) {
    boolean didLogging = false
    if (infoLogging == true) {
        logLevel = 100
    }
    if (debugLogging == true) {
        logLevel = 1
    }
    if (logLevel != "0"){
        switch (logLevel) {
        case "-1": // Insanely verbose
            if (level >= 0 && level < 100) {
                log.debug "$message"
                didLogging = true
            } else if (level == 100) {
                log.info "$message"
                didLogging = true
            }
        break
        case "1": // Very verbose
            if (level >= 1 && level < 99) {
                log.debug "$message"
                didLogging = true
            } else if (level == 100) {
                log.info "$message"
                didLogging = true
            }
        break
        case "10": // A little less
            if (level >= 10 && level < 99) {
                log.debug "$message"
                didLogging = true
            } else if (level == 100) {
                log.info "$message"
                didLogging = true
            }
        break
        case "50": // Rather chatty
            if (level >= 50 ) {
                log.debug "$message"
                didLogging = true
            }
        break
        case "99": // Only parsing reports
            if (level >= 99 ) {
                log.debug "$message"
                didLogging = true
            }
        break
        """ + extraDebug + """}
    }
    return didLogging
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
        return """String getChildDriverName() {
    String deviceDriverName = getDeviceInfoByName('name')
    if(deviceDriverName.toLowerCase().endsWith(' (parent)')) {
        deviceDriverName = deviceDriverName.substring(0, deviceDriverName.length()-9)
    }
    String childDriverName = "${deviceDriverName} (Child)"
    logging("childDriverName = '$childDriverName'", 1)
    return(childDriverName)
}"""
    else:
        return """String getChildDriverName() {
    String childDriverName = '""" + childDriverName + """ (Child)'
    logging("childDriverName = '$childDriverName'", 1)
    return(childDriverName)
}"""

def getCalculateB0():
    return """String calculateB0(String inputStr, repeats) {
    // This calculates the B0 value from the B1 for use with the Sonoff RF Bridge
    logging('inputStr: ' + inputStr, 0)
    inputStr = inputStr.replace(' ', '')
    //logging('inputStr.substring(4,6): ' + inputStr.substring(4,6), 0)
    Integer numBuckets = Integer.parseInt(inputStr.substring(4,6), 16)
    List buckets = []

    logging('numBuckets: ' + numBuckets.toString(), 0)

    String outAux = String.format(' %02X ', numBuckets.toInteger())
    outAux = outAux + String.format(' %02X ', repeats.toInteger())
    
    logging('outAux1: ' + outAux, 0)
    
    Integer j = 0
    for(i in (0..numBuckets-1)){
        outAux = outAux + inputStr.substring(6+i*4,10+i*4) + " "
        j = i
    }
    logging('outAux2: ' + outAux, 0)
    outAux = outAux + inputStr.substring(10+j*4, inputStr.length()-2)
    logging('outAux3: ' + outAux, 0)

    String dataStr = outAux.replace(' ', '')
    outAux = outAux + ' 55'
    Integer length = (dataStr.length() / 2).toInteger()
    outAux = "AA B0 " + String.format(' %02X ', length.toInteger()) + outAux
    logging('outAux4: ' + outAux, 0)
    logging('outAux: ' + outAux.replace(' ', ''), 10)

    return(outAux)
}"""

def getGenerateLearningPreferences(types='["Default", "Toggle", "Push", "On", "Off"]', default_type='Default'):
    return '''// Methods for displaying the correct Learning Preferences and returning the 
// current Action Name
def generateLearningPreferences() {
    input(name: "learningMode", type: "bool", title: addTitleDiv("Learning Mode"), description: '<i>Activate this to enter Learning Mode. DO NOT ACTIVATE THIS once you have learned the codes of a device, they will have to be re-learned!</i>', displayDuringSetup: false, required: false)
    if(learningMode) {
        input(name: "actionCurrentName", type: "enum", title: addTitleDiv("Action To Learn"), 
              description: addDescriptionDiv("Select which Action to save to in Learn Mode."), 
              options: ''' + types + ''', defaultValue: "''' + default_type + '''", 
              displayDuringSetup: false, required: false)
        input(name: "learningModeAdvanced", type: "bool", title: addTitleDiv("Advanced Learning Mode"), 
              description: '<i>Activate this to enable setting Advanced settings. Normally this is NOT needed, be careful!</i>', 
              defaultValue: false, displayDuringSetup: false, required: false)
        if(learningModeAdvanced) {
            input(name: "actionCodeSetManual", type: "string", title: addTitleDiv("Set Action Code Manually"), 
              description: '<i>WARNING! For ADVANCED users only!</i>', 
              displayDuringSetup: false, required: false)
            input(name: "actionResetAll", type: "bool", title: addTitleDiv("RESET all Saved Actions"), 
              description: '<i>WARNING! This will DELETE all saved/learned Actions!</i>', 
              defaultValue: false, displayDuringSetup: false, required: false)
        }
    }
}

String getCurrentActionName() {
    String actionName
    if(!binding.hasVariable('actionCurrentName') || 
      (binding.hasVariable('actionCurrentName') && actionCurrentName == null)) {
        logging("Doesn't have the action name defined... Using ''' + default_type + '''!", 1)
        actionName = "''' + default_type + '''"
    } else {
        actionName = actionCurrentName
    }
    return(actionName)
}'''

def getChildComponentMetaConfigCommands():
    return """
// metaConfig is what contains all fields to hide and other configuration
// processed in the "metadata" context of the driver.
def metaConfig = clearThingsToHide()
metaConfig = setDatasToHide(['metaConfig', 'isComponent', 'preferences', 'label', 'name'], metaConfig=metaConfig)
"""