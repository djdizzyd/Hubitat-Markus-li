
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
	definition (name: "Tasmota - ZNSN TuyaMCU Wifi Curtain Wall Panel", namespace: "tasmota", author: "Markus Liljergren", vid: "generic-switch", importURL: "https://raw.githubusercontent.com/markus-li/Hubitat/release/drivers/expanded/tasmota-znsn-tuyamcu-wifi-curtain-wall-panel-expanded.groovy") {
        capability "Light"
        capability "Switch"
        capability "WindowShade"
        
        // BEGIN:getDefaultMetadataCapabilities()
        
        // Default Capabilities
        capability "Refresh"
        capability "Configuration"
        
        // END:getDefaultMetadataCapabilities()
        
        attribute   "dimState", "number"
        attribute   "tuyaMCU", "string"
        attribute   "position", "number"
        attribute   "target", "number"
        
        // BEGIN:getDefaultMetadataAttributes()
        
        // Default Attributes
        
        // END:getDefaultMetadataAttributes()

        
        // BEGIN:getDefaultMetadataCommands()
        
        // Default Commands
        command "reboot"
        
        // END:getDefaultMetadataCommands()
        command "stop"
	}

	simulator {
	}
    
    preferences {
        
        // BEGIN:getDefaultMetadataPreferences()
        
        // Default Preferences
        generate_preferences(configuration_model_debug())
        
        // END:getDefaultMetadataPreferences()
        //input(name: "numSwitches", type: "enum", title: "<b>Number of Switches</b>", description: "<i>Set the number of buttons on the switch (default 1)</i>", options: ["1", "2", "3", "4"], defaultValue: "1", displayDuringSetup: true, required: true)
        //input(name: "lowLevel", type: "string", title: "<b>Dimming Range (low)</b>", description: '<i>Used to calibrate the MINIMUM dimming level, see <a href="https://tasmota.github.io/docs/#/TuyaMCU?id=dimmers">here</a> for details.</i>', displayDuringSetup: true, required: false)
        //input(name: "highLevel", type: "string", title: "<b>Dimming Range (high)</b>", description: '<i>Used to calibrate the MINIMUM dimming level, see <a href="https://tasmota.github.io/docs/#/TuyaMCU?id=dimmers">here</a> for details.</i>', displayDuringSetup: true, required: false)
        
        // BEGIN:getDefaultMetadataPreferencesForTasmota(False) # False = No TelePeriod setting
        
        // Default Preferences for Tasmota
        generate_preferences(configuration_model_tasmota())
        input(name: "ipAddress", type: "string", title: addTitleDiv("Device IP Address"), description: addDescriptionDiv("Set this as a default fallback for the auto-discovery feature."), displayDuringSetup: true, required: false)
        input(name: "port", type: "number", title: addTitleDiv("Device Port"), description: addDescriptionDiv("The http Port of the Device (default: 80)"), displayDuringSetup: true, required: false, defaultValue: 80)
        input(name: "override", type: "bool", title: addTitleDiv("Override IP"), description: addDescriptionDiv("Override the automatically discovered IP address and disable auto-discovery."), displayDuringSetup: true, required: false)
        
        input(name: "disableModuleSelection", type: "bool", title: addTitleDiv("Disable Automatically Setting Module and Template"), description: "ADVANCED: " + addDescriptionDiv("Disable automatically setting the Module Type and Template in Tasmota. Enable for using custom Module or Template settings directly on the device. With this disabled, you need to set these settings manually on the device."), displayDuringSetup: true, required: false)
        input(name: "moduleNumber", type: "number", title: addTitleDiv("Module Number"), description: "ADVANCED: " + addDescriptionDiv("Module Number used in Tasmota. If Device Template is set, this value is IGNORED. (default: -1 (use the default for the driver))"), displayDuringSetup: true, required: false, defaultValue: -1)
        input(name: "deviceTemplateInput", type: "string", title: addTitleDiv("Device Template"), description: "ADVANCED: " + addDescriptionDiv("Set this to a Device Template for Tasmota, leave it EMPTY to use the driver default. Set it to 0 to NOT use a Template. NAME can be maximum 14 characters! (Example: {\"NAME\":\"S120\",\"GPIO\":[0,0,0,0,0,21,0,0,0,52,90,0,0],\"FLAG\":0,\"BASE\":18})"), displayDuringSetup: true, required: false)
        input(name: "useIPAsID", type: "bool", title: addTitleDiv("IP as Network ID"), description: "ADVANCED: " + addDescriptionDiv("Not needed under normal circumstances. Setting this when not needed can break updates. This requires the IP to be static or set to not change in your DHCP server. It will force the use of IP as network ID. When in use, set Override IP to true and input the correct Device IP Address. See the release thread in the Hubitat forum for details and guidance."), displayDuringSetup: true, required: false)
        
        // END:getDefaultMetadataPreferencesForTasmota(False) # False = No TelePeriod setting
	}
}


// BEGIN:getDeviceInfoFunction()
public getDeviceInfoByName(infoName) { 
    // DO NOT EDIT: This is generated from the metadata!
    // TODO: Figure out how to get this from Hubitat instead of generating this?
    deviceInfo = ['name': 'Tasmota - ZNSN TuyaMCU Wifi Curtain Wall Panel', 'namespace': 'tasmota', 'author': 'Markus Liljergren', 'vid': 'generic-switch', 'importURL': 'https://raw.githubusercontent.com/markus-li/Hubitat/release/drivers/expanded/tasmota-znsn-tuyamcu-wifi-curtain-wall-panel-expanded.groovy']
    //logging("deviceInfo[${infoName}] = ${deviceInfo[infoName]}", 1)
    return(deviceInfo[infoName])
}
// END:getDeviceInfoFunction()


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
    
    // BEGIN:getGenericTasmotaParseHeader()
    // parse() Generic Tasmota-device header BEGINS here
    //log.debug "Parsing: ${description}"
    def events = []
    def descMap = parseDescriptionAsMap(description)
    def body
    //log.debug "descMap: ${descMap}"
    
    if (!state.mac || state.mac != descMap["mac"]) {
        logging("Mac address of device found ${descMap["mac"]}",1)
        state.mac = descMap["mac"]
    }
    
    prepareDNI()
    
    if (descMap["body"] && descMap["body"] != "T04=") body = new String(descMap["body"].decodeBase64())
    
    if (body && body != "") {
        if(body.startsWith("{") || body.startsWith("[")) {
            logging("========== Parsing Report ==========",99)
            def slurper = new JsonSlurper()
            def result = slurper.parseText(body)
    
            logging("result: ${result}",0)
            // parse() Generic header ENDS here
    
    // END:getGenericTasmotaParseHeader()
            
            // BEGIN:getTasmotaParserForBasicData()
            
            // Standard Basic Data parsing
            
            if (result.containsKey("StatusNET")) {
                logging("StatusNET: $result.StatusNET",99)
                result << result.StatusNET
            }
            if (result.containsKey("StatusFWR")) {
                logging("StatusFWR: $result.StatusFWR",99)
                result << result.StatusFWR
            }
            if (result.containsKey("StatusPRM")) {
                logging("StatusPRM: $result.StatusPRM",99)
                result << result.StatusPRM
            }
            if (result.containsKey("Status")) {
                logging("Status: $result.Status",99)
                if (result.Status.containsKey("Module")) {
                    // The check for Version is here to avoid using the wrong message
                    logging("Module: $result.Status.Module",50)
                    events << createEvent(name: "module", value: "$result.Status.Module")
                }
                result << result.Status
            }
            if (result.containsKey("StatusSTS")) {
                logging("StatusSTS: $result.StatusSTS",99)
                result << result.StatusSTS
            }
            if (result.containsKey("POWER")) {
                logging("POWER: $result.POWER",99)
                events << createEvent(name: "switch", value: result.POWER.toLowerCase())
            }
            if (result.containsKey("LoadAvg")) {
                logging("LoadAvg: $result.LoadAvg",99)
            }
            if (result.containsKey("Sleep")) {
                logging("Sleep: $result.Sleep",99)
            }
            if (result.containsKey("SleepMode")) {
                logging("SleepMode: $result.SleepMode",99)
            }
            if (result.containsKey("Vcc")) {
                logging("Vcc: $result.Vcc",99)
            }
            if (result.containsKey("Hostname")) {
                logging("Hostname: $result.Hostname",99)
            }
            if (result.containsKey("IPAddress") && (override == false || override == null)) {
                logging("IPAddress: $result.IPAddress",99)
                events << createEvent(name: "ip", value: "$result.IPAddress")
                //logging("ipLink: <a target=\"device\" href=\"http://$result.IPAddress\">$result.IPAddress</a>",10)
                events << createEvent(name: "ipLink", value: "<a target=\"device\" href=\"http://$result.IPAddress\">$result.IPAddress</a>")
            }
            if (result.containsKey("WebServerMode")) {
                logging("WebServerMode: $result.WebServerMode",99)
            }
            if (result.containsKey("Version")) {
                logging("Version: $result.Version",99)
                updateDataValue("firmware", result.Version)
            }
            // When it is a Template, it looks a bit different
            if (result.containsKey("NAME") && result.containsKey("GPIO") && result.containsKey("FLAG") && result.containsKey("BASE")) {
                n = result.toMapString()
                n = n.replaceAll(', ',',')
                n = n.replaceAll('\\[','{').replaceAll('\\]','}')
                n = n.replaceAll('NAME:', '"NAME":"').replaceAll(',GPIO:\\{', '","GPIO":\\[')
                n = n.replaceAll('\\},FLAG', '\\],"FLAG"').replaceAll('BASE', '"BASE"')
                // TODO: Learn how to do this the right way in Groovy
                logging("Template: $n",50)
                events << createEvent(name: "templateData", value: "${n}")
            }
            if (result.containsKey("RestartReason")) {
                logging("RestartReason: $result.RestartReason",99)
            }
            if (result.containsKey("TuyaMCU")) {
                logging("TuyaMCU: $result.TuyaMCU",99)
                events << createEvent(name: "tuyaMCU", value: "$result.TuyaMCU")
            }
            if (result.containsKey("SetOption81")) {
                logging("SetOption81: $result.SetOption81",99)
            }
            if (result.containsKey("SetOption113")) {
                logging("SetOption113 (Hubitat enabled): $result.SetOption113",99)
            }
            if (result.containsKey("Uptime")) {
                logging("Uptime: $result.Uptime",99)
                // Even with "displayed: false, archivable: false" these events still show up under events... There is no way of NOT having it that way...
                //events << createEvent(name: 'uptime', value: result.Uptime, displayed: false, archivable: false)
            
                state.uptime = result.Uptime
                updateDataValue('uptime', result.Uptime)
            }
            
            // END:getTasmotaParserForBasicData()
            
            // BEGIN:getTasmotaParserForWifi()
            
            // Standard Wifi Data parsing
            if (result.containsKey("Wifi")) {
                if (result.Wifi.containsKey("AP")) {
                    logging("AP: $result.Wifi.AP",99)
                }
                if (result.Wifi.containsKey("BSSId")) {
                    logging("BSSId: $result.Wifi.BSSId",99)
                }
                if (result.Wifi.containsKey("Channel")) {
                    logging("Channel: $result.Wifi.Channel",99)
                }
                if (result.Wifi.containsKey("RSSI")) {
                    logging("RSSI: $result.Wifi.RSSI",99)
                    quality = "${dBmToQuality(result.Wifi.RSSI)}%"
                    if(device.currentValue('wifiSignal') != quality) events << createEvent(name: "wifiSignal", value: quality)
                }
                if (result.Wifi.containsKey("SSId")) {
                    logging("SSId: $result.Wifi.SSId",99)
                }
            }
            
            // END:getTasmotaParserForWifi()
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
        
        // BEGIN:getGenericTasmotaParseFooter()
        // parse() Generic Tasmota-device footer BEGINS here
            } else {
                //log.debug "Response is not JSON: $body"
            }
        }
        
        if (!device.currentValue("ip") || (device.currentValue("ip") != getDataValue("ip"))) {
            curIP = getDataValue("ip")
            logging("Setting IP: $curIP", 1)
            events << createEvent(name: 'ip', value: curIP)
            events << createEvent(name: "ipLink", value: "<a target=\"device\" href=\"http://$curIP\">$curIP</a>")
        }
        
        return events
        // parse() Generic footer ENDS here
        // END:getGenericTasmotaParseFooter()
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

def update_needed_settings()
{
    
    // BEGIN:getUpdateNeededSettingsTasmotaHeader()
    // updateNeededSettings() Generic header BEGINS here
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
    
    // END:getUpdateNeededSettingsTasmotaHeader()

    
    // BEGIN:getUpdateNeededSettingsTasmotaDynamicModuleCommand(54)
    
    // Tasmota Module and Template selection command (autogenerated)
    cmds << getAction(getCommandString("Module", null))
    cmds << getAction(getCommandString("Template", null))
    if(disableModuleSelection == null) disableModuleSelection = false
    moduleNumberUsed = moduleNumber
    if(moduleNumber == null || moduleNumber == -1) moduleNumberUsed = 54
    useDefaultTemplate = false
    defaultDeviceTemplate = ''
    if(deviceTemplateInput != null && deviceTemplateInput == "0") {
        useDefaultTemplate = true
        defaultDeviceTemplate = ''
    }
    if(deviceTemplateInput == null || deviceTemplateInput == "") {
        // We should use the default of the driver
        useDefaultTemplate = true
        defaultDeviceTemplate = ''
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
        if(moduleNumberUsed != null && device.currentValue('module') != null && !(device.currentValue('module').startsWith("[${moduleNumberUsed}:") || device.currentValue('module') == '0')) {
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
    
    // END:getUpdateNeededSettingsTasmotaDynamicModuleCommand(54)
    
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
    
    
    // BEGIN:getUpdateNeededSettingsTasmotaFooter()
    
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
    
    // END:getUpdateNeededSettingsTasmotaFooter()
}


// BEGIN:getDefaultFunctions()
/* Default functions go here */
private def getDriverVersion() {
    //comment = "NOT GENERIC - read the instructions"
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
// Calls installed() -> [configure() -> [update_needed_settings(), updated() -> [updatedAdditional(), initialize() -> refresh() -> refreshAdditional()], installedAdditional()]
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

/*
    TASMOTA METHODS (helpers-tasmota)

    Helper functions included in all Tasmota drivers
*/

// Call order: installed() -> configure() -> updated() -> initialize() -> refresh()
def refresh() {
	logging("refresh()", 100)
    def cmds = []
    // Clear all old state variables
    state.clear()

    // Retrieve full status from Tasmota
    cmds << getAction(getCommandString("Status", "0"), callback="parseConfigureChildDevices")

    getDriverVersion()
    //logging("this.binding.variables = ${this.binding.variables}", 1)
    //logging("settings = ${settings}", 1)
    //logging("getDefinitionData() = ${getDefinitionData()}", 1)
    //logging("getPreferences() = ${getPreferences()}", 1)
    //logging("getSupportedCommands() = ${device.getSupportedCommands()}", 1)
    //logging("Seeing these commands: ${device.getSupportedCommands()}", 1)
    updateDataValue('namespace', getDeviceInfoByName('namespace'))
    /*metaConfig = setCommandsToHide(["on", "hiAgain2", "on"])
    metaConfig = setStateVariablesToHide(["uptime"], metaConfig=metaConfig)
    metaConfig = setCurrentStatesToHide(["needUpdate"], metaConfig=metaConfig)
    metaConfig = setDatasToHide(["namespace"], metaConfig=metaConfig)
    metaConfig = setPreferencesToHide(["port"], metaConfig=metaConfig)*/

    // This should be the first place we access metaConfig here, so clear and reset...
    metaConfig = clearThingsToHide()
    metaConfig = setCommandsToHide([], metaConfig=metaConfig)
    metaConfig = setStateVariablesToHide(['settings', 'colorMode', 'red', 'green', 'blue', 
        'mired', 'level', 'saturation', 'mode', 'hue'], metaConfig=metaConfig)
    
    metaConfig = setCurrentStatesToHide(['needUpdate'], metaConfig=metaConfig)
    //metaConfig = setDatasToHide(['preferences', 'namespace', 'appReturn', 'metaConfig'], metaConfig=metaConfig)
    metaConfig = setDatasToHide(['namespace', 'appReturn'], metaConfig=metaConfig)
    metaConfig = setPreferencesToHide([], metaConfig=metaConfig)
    try {
        // In case we have some more to run specific to this driver
        refreshAdditional(metaConfig)
    } catch (MissingMethodException e1) {
        // ignore
        try {
            // In case we have some more to run specific to this driver
            refreshAdditional()
        } catch (MissingMethodException e2) {
            // ignore
        }
    }
    return cmds
}

def reboot() {
	logging("reboot()", 10)
    getAction(getCommandString("Restart", "1"))
}

// Call order: installed() -> configure() -> updated() 
def updated()
{
    logging("updated()", 10)
    def cmds = [] 
    if(isDriver()) {
        cmds = update_needed_settings()
        //sendEvent(name: "checkInterval", value: 2 * 15 * 60 + 2 * 60, displayed: false, data: [protocol: "lan", hubHardwareId: device.hub.hardwareID])
        sendEvent(name:"needUpdate", value: device.currentValue("needUpdate"), displayed:false, isStateChange: false)
    }
    logging(cmds, 0)
    try {
        // Also run initialize(), if it exists...
        initialize()
        updatedAdditional()
    } catch (MissingMethodException e) {
        // ignore
    }
    if (cmds != [] && cmds != null) cmds
}

def prepareDNI() {
    if (useIPAsID) {
        hexIPAddress = setDeviceNetworkId(ipAddress, true)
        if(hexIPAddress != null && state.dni != hexIPAddress) {
            state.dni = hexIPAddress
            updateDNI()
        }
    }
    else if (state.mac != null && state.dni != state.mac) { 
        state.dni = setDeviceNetworkId(state.mac)
        updateDNI()
    }
}



def getCommandString(command, value) {
    def uri = "/cm?"
    if (password) {
        uri += "user=admin&password=${password}&"
    }
	if (value) {
		uri += "cmnd=${command}%20${value}"
	}
	else {
		uri += "cmnd=${command}"
	}
    return uri
}

def getMultiCommandString(commands) {
    def uri = "/cm?"
    if (password) {
        uri += "user=admin&password=${password}&"
    }
    uri += "cmnd=backlog%20"
    if(commands.size() > 30) {
        log.warn "Backlog only supports 30 commands, the last ${commands.size() - 30} will be ignored!"
    }
    commands.each {cmd->
        if(cmd.containsKey("value")) {
          uri += "${cmd['command']}%20${cmd['value']}%3B%20"
        } else {
          uri += "${cmd['command']}%3B%20"
        }
    }
    return uri
}

/*
    // Stress-testing runInstallCommands() 
    installCommands = []
    installCommands.add(["rule1", 'ON Var1#Value DO Var4 0; ENDON'])
    installCommands.add(["rule2", 'ON Var2#Value DO Var4 0; ENDON'])
    installCommands.add(["rule3", 'ON Var3#Value DO Var4 0; ENDON'])
    installCommands.add(["var1", "0"])
    installCommands.add(["var2", "0"])
    installCommands.add(["var3", "0"])
    (1..8).each {
        installCommands.add(["rule1", "+ ON Var1#Value DO Var4 $it; ENDON"])
        installCommands.add(["rule2", "+ ON Var2#Value DO Var4 $it; ENDON"])
        installCommands.add(["rule3", "+ ON Var3#Value DO Var4 $it; ENDON"])
        installCommands.add(["add1", "1"])
        installCommands.add(["add2", "1"])
        installCommands.add(["add3", "1"])
    }
    installCommands.add(["rule1", '0'])
    installCommands.add(["rule2", '0'])
    installCommands.add(["rule3", '0'])
    logging("refreshAdditional installCommands=$installCommands", 1)
    runInstallCommands(installCommands)
*/

def runInstallCommands(installCommands) {
    logging("runInstallCommands(installCommands=$installCommands)", 1)
    def cmds = []
    backlogs = []
    rule1 = []
    rule2 = []
    rule3 = []
    installCommands.each {cmd->
        if(cmd[0].toLowerCase() == "rule1") {
            rule1.add([command: cmd[0], value:urlEscape(cmd[1])])
        } else if(cmd[0].toLowerCase() == "rule2") {
            rule2.add([command: cmd[0], value:urlEscape(cmd[1])])
        } else if(cmd[0].toLowerCase() == "rule3") {
            rule3.add([command: cmd[0], value:urlEscape(cmd[1])])
        } else {
            backlogs.add([command: cmd[0], value:urlEscape(cmd[1])])
        }
    }

    // Backlog inter-command delay in milliseconds
    cmds << getAction(getCommandString("SetOption34", "20"))
    pauseExecution(100)
    // Maximum 30 commands per backlog call
    while(backlogs.size() > 0) {
        cmds << getAction(getMultiCommandString(backlogs.take(10)))
        backlogs = backlogs.drop(10)
        // If we run this too fast Tasmota can't keep up, 1000ms is enough when 20ms between commands...
        if(backlogs.size() > 0) pauseExecution(1000)
        // REALLY don't use pauseExecution often... NOT good for performance...
    }

    [rule1, rule2, rule3].each {
        //logging("rule: $it", 1)
        it.each {rule->
            // Rules can't run in backlog!
            cmds << getAction(getCommandString(rule["command"], rule["value"]))
            //logging("cmd=${rule["command"]}, value=${rule["value"]}", 1)
            pauseExecution(100)
            // REALLY don't use pauseExecution often... NOT good for performance...
        }
    }
    cmds << getAction(getCommandString("SetOption34", "200"))
}

def parseDescriptionAsMap(description) {
	description.split(",").inject([:]) { map, param ->
		def nameAndValue = param.split(":")
        
        if (nameAndValue.length == 2) map += [(nameAndValue[0].trim()):nameAndValue[1].trim()]
        else map += [(nameAndValue[0].trim()):""]
	}
}

private getAction(uri, callback="parse"){ 
    logging("Using getAction for '${uri}'...", 0)
    return httpGetAction(uri, callback=callback)
}

def parse(asyncResponse, data) {
    def events = []
    if(asyncResponse != null) {
        try{
            logging("parse(asyncResponse.getJson() 2= \"${asyncResponse.getJson()}\", data = \"${data}\")", 1)
            events << parseResult(asyncResponse.getJson())
        } catch(MissingMethodException e1) {
            log.error e1
        } catch(e1) {
            try{
                logging("parse(asyncResponse.data = \"${asyncResponse.data}\", data = \"${data}\") e1=$e1", 1)
            } catch(e2) {
                logging("parse(asyncResponse.data = null, data = \"${data}\") Is the device online? e2=$e2", 1)
            }
        }
    } else {
        logging("parse(asyncResponse.data = null, data = \"${data}\")", 1)
    }
    return events
}

def parseConfigureChildDevices(asyncResponse, data) {
    if(asyncResponse != null) {
        try{
            logging("parse(asyncResponse.getJson() 2= \"${asyncResponse.getJson()}\", data = \"${data}\")", 1)
            configureChildDevices(asyncResponse, data)
        } catch(MissingMethodException e1) {
            log.error e1
        } catch(e1) {
            try{
                logging("parse(asyncResponse.data = \"${asyncResponse.data}\", data = \"${data}\") e1=$e1", 1)
            } catch(e2) {
                logging("parse(asyncResponse.data = null, data = \"${data}\") Is the device online? e2=$e2", 1)
            }
        }
    } else {
        logging("parse(asyncResponse.data = null, data = \"${data}\")", 1)
    }
}

def containsKeyInSubMap(aMap, key) {
    hasKey = false
    aMap.find {
        try{
            hasKey = it.value.containsKey(key)
        } catch(e) {

        }
        hasKey == true
    }
    return hasKey
}

def numOfKeyInSubMap(aMap, key) {
    numKeys = 0
    aMap.each {
        try{
            if(it.value.containsKey(key)) numKeys += 1
        } catch(e) {
            // Do nothing
        }
    }
    return numKeys
}

def numOfKeysIsMap(aMap) {
    numKeys = 0
    aMap.each {
        if(it.value instanceof java.util.Map) numKeys += 1
    }
    return numKeys
}

TreeMap getKeysWithMapAndId(aMap) {
    def foundMaps = [:] as TreeMap
    aMap.each {
        if(it.value instanceof java.util.Map) {
            foundMaps[it.key] = it.value
        }
    }
    return foundMaps
}

def configureChildDevices(asyncResponse, data) {
    def statusMap = asyncResponse.getJson()
    logging("configureChildDevices() statusMap=$statusMap", 1)
    // Use statusMap to determine which Child Devices we should create

    // The built-in Generic Components are:
    //
    // Acceleration Sensor
    // Contact Sensor
    // Contact/Switch
    // CT
    // Dimmer
    // Metering Switch
    // Motion Sensor
    // RGB
    // RGBW
    // Smoke Detector
    // Switch
    // Temperature Sensor
    // Water Sensor

    // {"StatusSTS":{"Time":"2020-01-26T01:13:27","Uptime":"15T02:59:27","UptimeSec":1306767,
    // "Heap":26,"SleepMode":"Dynamic","Sleep":50,"LoadAvg":19,"MqttCount":0,"POWER1":"OFF",
    // "POWER2":"OFF","POWER3":"OFF","POWER4":"OFF","Wifi":{"AP":1,"SSId":"network",
    // "BSSId":"4A:11:11:12:CF:11","Channel":1,"RSSI":62,"LinkCount":37,"Downtime":"0T00:05:48"}}}

    // With a dimmer:
    // {"StatusSTS":{"Time":"2020-01-26T11:58:10","Uptime":"0T00:01:20","UptimeSec":80,"Heap":26,
    // "SleepMode":"Dynamic","Sleep":50,"LoadAvg":19,"MqttCount":0,"POWER":"OFF","Dimmer":0,
    // "Fade":"OFF","Speed":1,"LedTable":"ON","Wifi":{"AP":1,"SSId":"network",
    // "BSSId":"4A:11:11:12:D9:11","Channel":1,"RSSI":100,"LinkCount":1,"Downtime":"0T00:00:06"}}}

    // With an RGB+CW+WW light:
    // {"StatusSTS":{"Time":"2020-01-26T12:07:57","Uptime":"0T00:06:58","UptimeSec":418,"Heap":27,
    // "SleepMode":"Dynamic","Sleep":10,"LoadAvg":99,"MqttCount":0,"POWER":"ON","Dimmer":100,
    // "Color":"000000FF62","HSBColor":"0,0,0","Channel":[0,0,0,100,38],"CT":250,"Scheme":0,
    // "Fade":"ON","Speed":10,"LedTable":"ON","Wifi":{"AP":1,"SSId":"network",
    // "BSSId":"4A:11:11:12:D9:11","Channel":1,"RSSI":96,"LinkCount":1,"Downtime":"0T00:00:06"}}}
    

    // With an RGB+W light:
    // {"StatusSTS":{"Time":"2020-01-26T12:11:56","Uptime":"0T00:00:26","UptimeSec":26,"Heap":27,
    // "SleepMode":"Dynamic","Sleep":10,"LoadAvg":99,"MqttCount":0,"POWER":"ON","Dimmer":100,
    // "Color":"000000FF","HSBColor":"0,0,0","Channel":[0,0,0,100],"Scheme":0,"Fade":"ON",
    // "Speed":10,"LedTable":"ON","Wifi":{"AP":1,"SSId":"network","BSSId":"4A:11:11:12:D9:11",
    // "Channel":1,"RSSI":90,"LinkCount":1,"Downtime":"0T00:00:06"}}}

    // With an RGB light:
    // {"StatusSTS":{"Time":"2020-01-26T12:14:15","Uptime":"0T00:00:19","UptimeSec":19,"Heap":27,
    // "SleepMode":"Dynamic","Sleep":10,"LoadAvg":99,"MqttCount":0,"POWER":"ON","Dimmer":100,
    // "Color":"FFFFFF","HSBColor":"0,0,100","Channel":[100,100,100],"Scheme":0,"Fade":"ON",
    // "Speed":10,"LedTable":"ON","Wifi":{"AP":1,"SSId":"network","BSSId":"4A:11:11:12:D9:11",
    // "Channel":1,"RSSI":98,"LinkCount":1,"Downtime":"0T00:00:06"}}}

    // With CW+WW ("CT" is available):
    // {"StatusSTS":{"Time":"2020-01-26T12:16:48","Uptime":"0T00:00:17","UptimeSec":17,"Heap":28,
    // "SleepMode":"Dynamic","Sleep":10,"LoadAvg":99,"MqttCount":0,"POWER":"ON","Dimmer":100,
    // "Color":"FF62","HSBColor":"0,0,0","Channel":[100,38],"CT":250,"Fade":"ON","Speed":10,
    // "LedTable":"ON","Wifi":{"AP":1,"SSId":"network","BSSId":"4A:11:11:12:D9:11",
    // "Channel":1,"RSSI":94,"LinkCount":1,"Downtime":"0T00:00:06"}}}

    // With CW or WW (PWM1 configured on the correct pin), just the same as a normal dimmer...
    // {"StatusSTS":{"Time":"2020-01-26T12:19:51","Uptime":"0T00:01:15","UptimeSec":75,"Heap":27,
    // "SleepMode":"Dynamic","Sleep":10,"LoadAvg":99,"MqttCount":0,"POWER":"ON","Dimmer":71,
    // "Fade":"ON","Speed":10,"LedTable":"ON","Wifi":{"AP":1,"SSId":"network",
    // "BSSId":"4A:11:11:12:D9:11","Channel":1,"RSSI":88,"LinkCount":1,"Downtime":"0T00:00:25"}}}

    // Addressable RGB light (has the attribute "Width")
    // {"StatusSNS":{"Time":"2020-01-26T12:57:30","SR04":{"Distance":8.579}}}
    // {"StatusSTS":{"Time":"2020-01-26T12:57:30","Uptime":"0T00:02:14","UptimeSec":134,"Heap":21,
    // "SleepMode":"Dynamic","Sleep":10,"LoadAvg":113,"MqttCount":0,"POWER1":"ON","POWER2":"ON",
    // "Dimmer":100,"Color":"00FF00","HSBColor":"120,100,100","Channel":[0,100,0],"Scheme":13,
    // "Width":2,"Fade":"OFF","Speed":1,"LedTable":"ON","Wifi":{"AP":1,"SSId":"network",
    // "BSSId":"4A:11:11:12:D9:11","Channel":1,"RSSI":100,"Signal":-40,"LinkCount":1,
    // "Downtime":"0T00:00:09"}}}

    // {"StatusSNS":{"Time":"2020-01-26T01:24:16","BMP280":{"Temperature":23.710,"Pressure":1017.6},
    // "PressureUnit":"hPa","TempUnit":"C"}}

    // Multiple temperature sensors:
    // {"Time":"2020-01-26T17:45:30","DS18B20-1":{"Id":"000008BD38BF","Temperature":26.1},
    // "DS18B20-2":{"Id":"000008BD9714","Temperature":25.1},"DS18B20-3":{"Id":"000008C02C3A",
    // "Temperature":25.3},"TempUnit":"C"}
    
    // For DS18B20, us ID to distinguish them? Then you can't replace them...
    // For AM2301 the GPIO used is appended.
    // {"StatusSNS":{"Time":"2020-01-26T20:54:10","DS18B20-1":{"Id":"000008BD38BF","Temperature":25.8},
    // "DS18B20-2":{"Id":"000008BD9714","Temperature":24.7},"DS18B20-3":{"Id":"000008C02C3A","Temperature":24.9},
    // "AM2301-12":{"Temperature":25.1,"Humidity":66.4},"AM2301-14":{"Temperature":null,"Humidity":null},"TempUnit":"C"}}

    // D5 = GPIO14
    // D6 = GPIO12
    // D7 = GPIO13

    // Distance Sensor
    // {"StatusSNS":{"Time":"2020-01-26T13:52:19","SR04":{"Distance":11.667}}}

    // {"NAME":"ControlRGBWWCW","GPIO":[17,0,0,0,0,40,0,0,38,39,37,41,0],"FLAG":0,"BASE":18}

    // result: [Time:2020-01-30T11:30:43, DS18B20-1:[Id:000008BD38BF, Temperature:25.3], DS18B20-2:[Id:000008BD9714, Temperature:24.3], DS18B20-3:[Id:000008C02C3A, Temperature:24.4], AM2301-12:[Temperature:24.2, Humidity:68.1], AM2301-14:[Temperature:24.0, Humidity:68.1], TempUnit:C]
    // result: [Time:2020-01-30T11:31:12, DS18B20-1:[Id:000008BD38BF, Temperature:25.3], DS18B20-2:[Id:000008BD9714, Temperature:24.3], DS18B20-3:[Id:000008C02C3A, Temperature:24.4], AM2301-12:[Temperature:24.2, Humidity:68.0], AM2301-14:[Temperature:24.0, Humidity:68.1], TempUnit:C]
    // [hasEnergy:false, numTemperature:5, numHumidity:2, numPressure:0, numDistance:0, sensorMap:[AM2301-12:[Temperature:24.2, Humidity:68.1], AM2301-14:[Temperature:24.0, Humidity:68.1], DS18B20-1:[Id:000008BD38BF, Temperature:25.3], DS18B20-2:[Id:000008BD9714, Temperature:24.3], DS18B20-3:[Id:000008C02C3A, Temperature:24.4]], numSwitch:0, isDimmer:false, isAddressable:false, isRGB:false, hasCT:false]

    // SENSOR = {"Time":"2020-01-30T19:15:08","SR04":{"Distance":73.702}}

    // Switch or Metering Switch are the two most likely ones
    deviceInfo = [:]
    deviceInfo["hasEnergy"] = false
    deviceInfo["numTemperature"] = 0
    deviceInfo["numHumidity"] = 0
    deviceInfo["numPressure"] = 0
    deviceInfo["numDistance"] = 0
    deviceInfo["numSensorGroups"] = 0
    deviceInfo["sensorMap"] = [:] as TreeMap
    if(statusMap.containsKey("StatusSNS")) {
        sns = statusMap["StatusSNS"]
        deviceInfo["hasEnergy"] = sns.containsKey("ENERGY")
        deviceInfo["sensorMap"] = getKeysWithMapAndId(sns)
        deviceInfo["numSensorGroups"] = deviceInfo["sensorMap"].size()
        deviceInfo["numTemperature"] = numOfKeyInSubMap(sns, "Temperature")
        deviceInfo["numHumidity"] = numOfKeyInSubMap(sns, "Humidity")
        deviceInfo["numPressure"] = numOfKeyInSubMap(sns, "Pressure")
        deviceInfo["numDistance"] = numOfKeyInSubMap(sns, "Distance")
    }

    deviceInfo["numSwitch"] = 0
    deviceInfo["isDimmer"] = false
    deviceInfo["isAddressable"] = false
    deviceInfo["isRGB"] = false
    deviceInfo["hasCT"] = false
    if(statusMap["StatusSTS"] != null) {
        sts = statusMap["StatusSTS"]
        deviceInfo["isDimmer"] = sts.containsKey("Dimmer")
        deviceInfo["isAddressable"] = sts.containsKey("Width")
        if(sts.containsKey("Color")) deviceInfo["isRGB"] = sts["Color"].length() >= 6
        deviceInfo["hasCT"] = sts.containsKey("CT")

        if(sts["POWER"] != null) {
            // This only exist if there is ONLY one switch/bulb
            deviceInfo["numSwitch"] = 1
        } else {
            i = 1
            while(sts["POWER$i"] != null) {
                i += 1
            }
            deviceInfo["numSwitch"] = i - 1
        }
    }
    logging("Device info found: $deviceInfo", 100)
    // Create the devices, if needed

    // Switches
    if(deviceInfo["numSwitch"] > 0) {
        if(deviceInfo["numSwitch"] > 1 && (
            deviceInfo["isDimmer"] == true || deviceInfo["isAddressable"] == true || 
            deviceInfo["isRGB"] == true || deviceInfo["hasCT"] == true)) {
                log.warn "There's more than one switch and the device is either dimmable, addressable, RGB or has CT capability. This is not fully supported yet, please report which device and settings you're using to the developer."
            }
        
        driverName = ["Tasmota - Universal Switch (Child)", "Generic Component Switch"]
        if(deviceInfo["hasEnergy"] && (deviceInfo["isAddressable"] == false && deviceInfo["isRGB"] == false && deviceInfo["hasCT"] == false)) {
            if(deviceInfo["isDimmer"]) {
                // TODO: Make a Component Dimmer with Metering
                driverName = ["Tasmota - Universal Dimmer (Child)", "Generic Component Dimmer"]
            } else {
                driverName = ["Tasmota - Universal Switch (Child)", "Generic Component Metering Switch"]
            }
        } else {
            if(deviceInfo["hasEnergy"]) {
                log.warn "This device reports Metering Capability AND has RGB, Color Temperature or is Addressable. Metering values will be ignored... This is NOT supported and may result in errors, please report it to the developer."
            }
            if((deviceInfo["isDimmer"] == true || deviceInfo["isAddressable"] == true || 
                deviceInfo["isRGB"] == true || deviceInfo["hasCT"] == true)) {
                if(deviceInfo["isAddressable"] == false && deviceInfo["isRGB"] == false && deviceInfo["hasCT"] == false) {
                    driverName = ["Tasmota - Universal Dimmer (Child)", "Generic Component Dimmer"]
                } else if(deviceInfo["isAddressable"] == false && deviceInfo["isRGB"] == false && deviceInfo["hasCT"] == true) {
                    driverName = ["Tasmota - Universal CT/RGB/RGB+CW+WW (Child)", "Generic Component CT"]
                } else if(deviceInfo["isRGB"] == true && deviceInfo["hasCT"] == false) {
                    driverName = ["Tasmota - Universal CT/RGB/RGB+CW+WW (Child)", "Generic Component RGB"]
                } else {
                    driverName = ["Tasmota - Universal CT/RGB/RGB+CW+WW (Child)", "Generic Component RGBW"]
                }
            }
        }
        
        for(i in 1..deviceInfo["numSwitch"]) {
            namespace = "tasmota"
            childId = "POWER$i"
            childName = getChildDeviceNameRoot(keepType=true) + " ${getMinimizedDriverName(driverName[0])} ($childId)"
            childLabel = "${getMinimizedDriverName(device.getLabel())} ($childId)"
            logging("createChildDevice: POWER$i", 1)
            createChildDevice(namespace, driverName, childId, childName, childLabel)
            
            // Once the first switch is created we only support one type... At least for now...
            driverName = ["Tasmota - Universal Switch (Child)", "Generic Component Switch"]
        }
    }
    
    // Sensors
    deviceInfo["sensorMap"].each {
        namespace = "tasmota"
        driverName = ["Tasmota - Universal Multisensor (Child)"]
        childId = "${it.key}"
        childName = getChildDeviceNameRoot(keepType=true) + " ${getMinimizedDriverName(driverName[0])} ($childId)"
        childLabel = "${getMinimizedDriverName(device.getLabel())} ($childId)"
        createChildDevice(namespace, driverName, childId, childName, childLabel)
    }

    // Finally let the default parser have the data as well...
    parseResult(statusMap)
}

String getChildDeviceNameRoot(Boolean keepType=false) {
    childDeviceNameRoot = getDeviceInfoByName('name')
    if(childDeviceNameRoot.toLowerCase().endsWith(' (parent)')) {
        childDeviceNameRoot = childDeviceNameRoot.substring(0, childDeviceNameRoot.length()-9)
    } else if(childDeviceNameRoot.toLowerCase().endsWith(' parent')) {
        childDeviceNameRoot = childDeviceNameRoot.substring(0, childDeviceNameRoot.length()-7)
    }
    if(keepType == false && childDeviceNameRoot.toLowerCase().startsWith('tasmota - ')) {
        childDeviceNameRoot = childDeviceNameRoot.substring(10, childDeviceNameRoot.length())
    }
    return childDeviceNameRoot
}

String getMinimizedDriverName(String driverName) {
    logging("getMinimizedDriverName(driverName=$driverName)", 1)
    if(driverName.toLowerCase().endsWith(' (child)')) {
        driverName = driverName.substring(0, driverName.length()-8)
    } else if(driverName.toLowerCase().endsWith(' child')) {
        driverName = driverName.substring(0, driverName.length()-6)
    }
    if(driverName.toLowerCase().endsWith(' (parent)')) {
        driverName = driverName.substring(0, driverName.length()-9)
    } else if(driverName.toLowerCase().endsWith(' parent')) {
        driverName = driverName.substring(0, driverName.length()-7)
    }
    if(driverName.toLowerCase().startsWith('tasmota - ')) {
        driverName = driverName.substring(10, driverName.length())
    }
    if(driverName.toLowerCase().startsWith('universal ')) {
        driverName = driverName.substring(10, driverName.length())
    }
    driverName = driverName.replaceAll("Generic Component ", "")
    logging("getMinimizedDriverName(driverName=$driverName) end", 1)
    return driverName
}

def getChildDeviceByActionType(String actionType) {
    return childDevices.find{it.deviceNetworkId.endsWith("-$actionType")}
}

private void createChildDevice(String namespace, List driverName, String childId, String childName, String childLabel) {
    
    childDevice = childDevices.find{it.deviceNetworkId.endsWith("-$childId")}
    if(!childDevice && childId.toLowerCase().startsWith("power")) {
        logging("Looking for $childId, ending in ${childId.substring(5)}", 1)
        childDevice = childDevices.find{it.deviceNetworkId.endsWith("-${childId.substring(5)}")}
        if(childDevice) {
            logging("Setting new Network ID for $childId to '$device.id-$childId'", 1)
            childDevice.setDeviceNetworkId("$device.id-$childId")
        }
    }
    if (childDevice) {
        // The device exists, just update it
        childDevice.setName(childName)
        logging(childDevice.getData(), 10)
    } else {
        s = childName.size()
        for(i in 0..s) {
            if(driverName[i].toLowerCase().startsWith('generic component')) {
                currentNamespace = "hubitat"
            } else {
                currentNamespace = namespace
            }
            try {
                addChildDevice(currentNamespace, driverName[i], "$device.id-$childId", [name: childName, label: childLabel, isComponent: false])
                logging("Created child device '$childLabel' using driver '${driverName[i]}'...", 100)
            } catch (com.hubitat.app.exception.UnknownDeviceTypeException e) {
                if(i == s) {
                    log.error "'${driverName[i]}' driver can't be found! Did you forget to install the child driver?"
                } else {
                    log.warn "'${driverName[i]}' driver can't be found! Trying another driver: ${driverName[i+1]}..."
                }
            }
        }
    }
}

private httpGetAction(uri, callback="parse"){ 
  updateDNI()
  
  def headers = getHeader()
  logging("Using httpGetAction for 'http://${getHostAddress()}$uri'...", 0)
  def hubAction = null
  try {
    /*hubAction = new hubitat.device.HubAction(
        method: "GET",
        path: uri,
        headers: headers
    )*/
    hubAction = asynchttpGet(
        callback,
        [uri: "http://${getHostAddress()}$uri",
        headers: headers]
    )
  } catch (e) {
    log.error "Error in httpGetAction(uri): $e ('$uri')"
  }
  return hubAction    
}

private postAction(uri, data){ 
  updateDNI()

  def headers = getHeader()

  def hubAction = null
  try {
    hubAction = new hubitat.device.HubAction(
    method: "POST",
    path: uri,
    headers: headers,
    body: data
  )
  } catch (e) {
    log.error "Error in postAction(uri, data): $e ('$uri', '$data')"
  }
  return hubAction    
}

private onOffCmd(value, endpoint) {
    logging("onOffCmd, value: $value, endpoint: $endpoint", 1)
    def cmds = []
    cmds << getAction(getCommandString("Power$endpoint", "$value"))
    return cmds
}

private setDeviceNetworkId(macOrIP, isIP = false){
    def myDNI
    if (isIP == false) {
        myDNI = macOrIP
    } else {
        logging("About to convert ${macOrIP}...", 0)
        myDNI = convertIPtoHex(macOrIP)
    }
    logging("Device Network Id should be set to ${myDNI} from ${macOrIP}", 0)
    return myDNI
}

private updateDNI() { 
    if (state.dni != null && state.dni != "" && device.deviceNetworkId != state.dni) {
        logging("Device Network Id will be set to ${state.dni} from ${device.deviceNetworkId}", 0)
        device.deviceNetworkId = state.dni
    }
}

private getHostAddress() {
    if (port == null) {
        port = 80
    }
    if (override == true && ipAddress != null){
        return "${ipAddress}:${port}"
    }
    else if(getDeviceDataByName("ip") && getDeviceDataByName("port")){
        return "${getDeviceDataByName("ip")}:${getDeviceDataByName("port")}"
    }else{
	    return "${ip}:80"
    }
}

private String convertIPtoHex(ipAddress) {
    String hex = null
    if(ipAddress != null) {
        hex = ipAddress.tokenize( '.' ).collect {  String.format( '%02X', it.toInteger() ) }.join()
        logging("Get this IP in hex: ${hex}", 0)
    } else {
        hex = null
        if (useIPAsID) {
            logging('ERROR: To use IP as Network ID "Device IP Address" needs to be set and "Override IP" needs to be enabled! If this error persists, consult the release thread in the Hubitat Forum.')
        }
    }
    return hex
}

private String urlEscape(url) {
    return(URLEncoder.encode(url).replace("+", "%20"))
}

private String convertPortToHex(port) {
	String hexport = port.toString().format( '%04X', port.toInteger() )
    return hexport
}

private encodeCredentials(username, password){
	def userpassascii = "${username}:${password}"
    def userpass = "Basic " + userpassascii.bytes.encodeBase64().toString()
    return userpass
}

private getHeader(userpass = null){
    def headers = [:]
    headers.put("Host", getHostAddress())
    headers.put("Content-Type", "application/x-www-form-urlencoded")
    if (userpass != null)
       headers.put("Authorization", userpass)
    return headers
}

def sync(ip, port = null) {
    def existingIp = getDataValue("ip")
    def existingPort = getDataValue("port")
    logging("Running sync()", 1)
    if (ip && ip != existingIp) {
        updateDataValue("ip", ip)
        sendEvent(name: 'ip', value: ip)
        sendEvent(name: "ipLink", value: "<a target=\"device\" href=\"http://$ip\">$ip</a>")
        logging("IP set to ${ip}", 1)
    }
    if (port && port != existingPort) {
        updateDataValue("port", port)
        logging("Port set to ${port}", 1)
    }
}

def configuration_model_tasmota()
{
'''
<configuration>
<Value type="password" byteSize="1" index="password" label="Device Password" description="REQUIRED if set on the Device! Otherwise leave empty." min="" max="" value="" setting_type="preference" fw="">
<Help>
</Help>
</Value>
</configuration>
'''
}

/*
    --END-- TASMOTA METHODS (helpers-tasmota)
*/
