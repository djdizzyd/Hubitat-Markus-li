 /**
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
 */

/* Default imports */
import groovy.json.JsonSlurper

metadata {
	definition (name: "Tasmota - Sonoff PowR2", namespace: "tasmota", author: "Markus Liljergren", vid:"generic-switch") {
        capability "Actuator"
		capability "Switch"
		capability "Sensor"

        
        // Default Capabilities for Energy Monitor
        capability "Voltage Measurement"
        capability "Power Meter"
        capability "Energy Meter"
        
        // Default Capabilities
        capability "Refresh"
        capability "Configuration"
        capability "HealthCheck"
        
        
        // Default Attributes for Energy Monitor
        attribute   "current", "string"
        attribute   "apparentPower", "string"
        attribute   "reactivePower", "string"
        attribute   "powerFactor", "string"
        attribute   "energyToday", "string"
        attribute   "energyYesterday", "string"
        attribute   "energyTotal", "string"
        
        // Default Attributes
        attribute   "needUpdate", "string"
        attribute   "uptime", "string"
        attribute   "ip", "string"
        attribute   "module", "string"
        
        // Default Commands
        command "reboot"
	}

	simulator {
	}
    
    preferences {
        
        // Default Preferences
        input(description: "For details and guidance, see the release thread in the <a href=\"https://community.hubitat.com/t/release-tasmota-7-x-firmware-with-hubitat-support/29368\"> Hubitat Forum</a>.", title: "Settings", displayDuringSetup: false, type: "paragraph", element: "paragraph")
        generate_preferences(configuration_model_debug())
        
        // Default Preferences for Tasmota
        input(name: "ipAddress", type: "string", title: "Device IP Address", description: "Set this as a default for the auto-discovery feature", displayDuringSetup: true, required: false)
        input(name: "port", type: "number", title: "Device Port", description: "Port (default: 80)", displayDuringSetup: true, required: false, defaultValue: 80)
        input(name: "override", type: "bool", title: "Override IP", description: "Override the automatically discovered IP address and disable auto-discovery.", displayDuringSetup: true, required: false)
        input(name: "disableModuleSelection", type: "bool", title: "Disable Module Selection", description: "Disable automatically setting the Module Type in Tasmota. Enable for using custom Module or Template settings.", displayDuringSetup: true, required: false)
        input(name: "telePeriod", type: "string", title: "Update Frequency", description: "Tasmota sensor value update interval, set this to any value between 10 and 3600 seconds. See the Tasmota docs concerning telePeriod for details. This is NOT a poll frequency. Button/switch changes are immediate and are NOT affected by this. This is for SENSORS only. (default = 300)", displayDuringSetup: true, required: false)
        generate_preferences(configuration_model_tasmota())
        input(name: "useIPAsID", type: "bool", title: "EXPERTS ONLY: IP as Network ID", description: "Not needed under normal circumstances. Setting this when not needed can break updates. This requires the IP to be static or set to not change in your DHCP server. It will force the use of IP as network ID. When in use, set Override IP to true and input the correct Device IP Address. See the release thread in the Hubitat forum for details and guidance.", displayDuringSetup: true, required: false)
	}
}

def getDeviceInfoByName(infoName) { 
    // DO NOT EDIT: This is generated from the metadata!
    // TODO: Figure out how to get this from Hubitat instead of generating this?
    deviceInfo = ['name': 'Tasmota - Sonoff PowR2', 'namespace': 'tasmota', 'author': 'Markus Liljergren', 'vid': 'generic-switch']
    return(deviceInfo[infoName])
}

/* These functions are unique to each driver */
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

def parse(description) {
    // parse() Generic header BEGINS here
    //log.debug "Parsing: ${description}"
    def events = []
    def descMap = parseDescriptionAsMap(description)
    def body
    //log.debug "descMap: ${descMap}"
    
    if (!state.mac || state.mac != descMap["mac"]) {
        logging("Mac address of device found ${descMap["mac"]}",1)
        state.mac = descMap["mac"]
    }
    
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
    
    if (descMap["body"] && descMap["body"] != "T04=") body = new String(descMap["body"].decodeBase64())
    
    if (body && body != "") {
        if(body.startsWith("{") || body.startsWith("[")) {
            logging("========== Parsing Report ==========",99)
            def slurper = new JsonSlurper()
            def result = slurper.parseText(body)
            
            logging("result: ${result}",0)
            // parse() Generic header ENDS here
            
            
            // Standard Basic Data parsing
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
            if (result.containsKey("IPAddress") && override == false) {
                logging("IPAddress: $result.IPAddress",99)
                events << createEvent(name: "ip", value: "$result.IPAddress")
            }
            if (result.containsKey("WebServerMode")) {
                logging("WebServerMode: $result.WebServerMode",99)
            }
            if (result.containsKey("Version")) {
                logging("Version: $result.Version",99)
            }
            if (result.containsKey("Module")) {
                logging("Module: $result.Module",99)
                events << createEvent(name: "module", value: "$result.Module")
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
                events << createEvent(name: 'uptime', value: result.Uptime, displayed: false)
            }
            
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
                }
                if (result.Wifi.containsKey("SSId")) {
                    logging("SSId: $result.Wifi.SSId",99)
                }
            }
            
            // Standard Energy Monitor Data parsing
            if (result.containsKey("StatusSNS")) {
                if (result.StatusSNS.containsKey("ENERGY")) {
                    if (result.StatusSNS.ENERGY.containsKey("Total")) {
                        logging("Total: $result.StatusSNS.ENERGY.Total kWh",99)
                        events << createEvent(name: "energyTotal", value: "$result.StatusSNS.ENERGY.Total kWh")
                    }
                    if (result.StatusSNS.ENERGY.containsKey("Today")) {
                        logging("Today: $result.StatusSNS.ENERGY.Today kWh",99)
                        events << createEvent(name: "energyToday", value: "$result.StatusSNS.ENERGY.Today kWh")
                    }
                    if (result.StatusSNS.ENERGY.containsKey("Yesterday")) {
                        logging("Yesterday: $result.StatusSNS.ENERGY.Yesterday kWh",99)
                        events << createEvent(name: "energyYesterday", value: "$result.StatusSNS.ENERGY.Yesterday kWh")
                    }
                    if (result.StatusSNS.ENERGY.containsKey("Current")) {
                        logging("Current: $result.StatusSNS.ENERGY.Current A",99)
                        events << createEvent(name: "current", value: "$result.StatusSNS.ENERGY.Current A")
                    }
                    if (result.StatusSNS.ENERGY.containsKey("ApparentPower")) {
                        logging("apparentPower: $result.StatusSNS.ENERGY.ApparentPower VA",99)
                        events << createEvent(name: "apparentPower", value: "$result.StatusSNS.ENERGY.ApparentPower VA")
                    }
                    if (result.StatusSNS.ENERGY.containsKey("ReactivePower")) {
                        logging("reactivePower: $result.StatusSNS.ENERGY.ReactivePower VAr",99)
                        events << createEvent(name: "reactivePower", value: "$result.StatusSNS.ENERGY.ReactivePower VAr")
                    }
                    if (result.StatusSNS.ENERGY.containsKey("Factor")) {
                        logging("powerFactor: $result.StatusSNS.ENERGY.Factor",99)
                        events << createEvent(name: "powerFactor", value: "$result.StatusSNS.ENERGY.Factor")
                    }
                    if (result.StatusSNS.ENERGY.containsKey("Voltage")) {
                        logging("Voltage: $result.StatusSNS.ENERGY.Voltage V",99)
                        events << createEvent(name: "voltage", value: "$result.StatusSNS.ENERGY.Voltage V")
                    }
                    if (result.StatusSNS.ENERGY.containsKey("Power")) {
                        logging("Power: $result.StatusSNS.ENERGY.Power W",99)
                        events << createEvent(name: "power", value: "$result.StatusSNS.ENERGY.Power W")
                    }
                }
            }
            if (result.containsKey("ENERGY")) {
                logging("Has ENERGY...", 1)
                if (result.ENERGY.containsKey("Total")) {
                    logging("Total: $result.ENERGY.Total kWh",99)
                    events << createEvent(name: "energyTotal", value: "$result.ENERGY.Total kWh")
                }
                if (result.ENERGY.containsKey("Today")) {
                    logging("Today: $result.ENERGY.Today kWh",99)
                    events << createEvent(name: "energyToday", value: "$result.ENERGY.Today kWh")
                }
                if (result.ENERGY.containsKey("Yesterday")) {
                    logging("Yesterday: $result.ENERGY.Yesterday kWh",99)
                    events << createEvent(name: "energyYesterday", value: "$result.ENERGY.Yesterday kWh")
                }
                if (result.ENERGY.containsKey("Current")) {
                    logging("Current: $result.ENERGY.Current A",99)
                    events << createEvent(name: "current", value: "$result.ENERGY.Current A")
                }
                if (result.ENERGY.containsKey("ApparentPower")) {
                    logging("apparentPower: $result.ENERGY.ApparentPower VA",99)
                    events << createEvent(name: "apparentPower", value: "$result.ENERGY.ApparentPower VA")
                }
                if (result.ENERGY.containsKey("ReactivePower")) {
                    logging("reactivePower: $result.ENERGY.ReactivePower VAr",99)
                    events << createEvent(name: "reactivePower", value: "$result.ENERGY.ReactivePower VAr")
                }
                if (result.ENERGY.containsKey("Factor")) {
                    logging("powerFactor: $result.ENERGY.Factor",99)
                    events << createEvent(name: "powerFactor", value: "$result.ENERGY.Factor")
                }
                if (result.ENERGY.containsKey("Voltage")) {
                    logging("Voltage: $result.ENERGY.Voltage V",99)
                    events << createEvent(name: "voltage", value: "$result.ENERGY.Voltage V")
                }
                if (result.ENERGY.containsKey("Power")) {
                    logging("Power: $result.ENERGY.Power W",99)
                    events << createEvent(name: "power", value: "$result.ENERGY.Power W")
                }
            }
            // StatusPTH:[PowerDelta:0, PowerLow:0, PowerHigh:0, VoltageLow:0, VoltageHigh:0, CurrentLow:0, CurrentHigh:0]
        // parse() Generic footer BEGINS here
        } else {
                //log.debug "Response is not JSON: $body"
            }
        }
        
        if (!device.currentValue("ip") || (device.currentValue("ip") != getDataValue("ip"))) events << createEvent(name: 'ip', value: getDataValue("ip"))
        
        return events
        // parse() Generic footer ENDS here
}

def update_needed_settings()
{
    // updateNeededSettings() Generic header BEGINS here
    def cmds = []
    def currentProperties = state.currentProperties ?: [:]
    
    state.settings = settings
    
    def configuration = new XmlSlurper().parseText(configuration_model_tasmota())
    def isUpdateNeeded = "NO"
    
    // updateNeededSettings() Generic header ENDS here

    
    // Tasmota Module selection command (autogenerated)
    cmds << getAction(getCommandString("Module", null))
    if(disableModuleSelection == false) {
        logging("Setting the Module soon...", 10)
        logging(device.currentValue('module'), 10)
        if(device.currentValue('module') != null && !device.currentValue('module').startsWith("[43")) {
            logging("This DOESN'T start with [43 ${device.currentValue('module')}",10)
            cmds << getAction(getCommandString("Module", "43"))
        } else {
            logging("This starts with [43 ${device.currentValue('module')}",10)
        }
    } else {
        logging("Setting the Module has been disabled!", 10)
    }

    cmds << getAction(getCommandString("SetOption81", "1")) // Set PCF8574 component behavior for all ports as inverted
    cmds << getAction(getCommandString("LedPower", "1"))  // 1 = turn LED ON and set LedState 8
    cmds << getAction(getCommandString("LedState", "8"))  // 8 = LED on when Wi-Fi and MQTT are connected.
    
    
    // updateNeededSettings() TelePeriod setting
    cmds << getAction(getCommandString("TelePeriod", (telePeriod == '' || telePeriod == null ? "300" : telePeriod)))
    
    
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
}

/* Logging function included in all drivers */
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


/* Helper functions included in all drivers */
def ping() {
    logging("ping()", 50)
    refresh()
}

def installed() {
	logging("installed()", 50)
	configure()
    try {
        // In case we have some more to run specific to this driver
        installedAdditional()
    } catch (MissingMethodException e) {
        // ignore
    }
}

def configure() {
    logging("configure()", 50)
    def cmds = []
    cmds = update_needed_settings()
    if (cmds != []) cmds
}

def generate_preferences(configuration_model)
{
    def configuration = new XmlSlurper().parseText(configuration_model)
   
    configuration.Value.each
    {
        if(it.@hidden != "true" && it.@disabled != "true"){
        switch(it.@type)
        {   
            case ["number"]:
                input "${it.@index}", "number",
                    title:"${it.@label}\n" + "${it.Help}",
                    range: "${it.@min}..${it.@max}",
                    defaultValue: "${it.@value}",
                    displayDuringSetup: "${it.@displayDuringSetup}"
            break
            case "list":
                def items = []
                it.Item.each { items << ["${it.@value}":"${it.@label}"] }
                input "${it.@index}", "enum",
                    title:"${it.@label}\n" + "${it.Help}",
                    defaultValue: "${it.@value}",
                    displayDuringSetup: "${it.@displayDuringSetup}",
                    options: items
            break
            case ["password"]:
                input "${it.@index}", "password",
                    title:"${it.@label}\n" + "${it.Help}",
                    displayDuringSetup: "${it.@displayDuringSetup}"
            break
            case "decimal":
               input "${it.@index}", "decimal",
                    title:"${it.@label}\n" + "${it.Help}",
                    range: "${it.@min}..${it.@max}",
                    defaultValue: "${it.@value}",
                    displayDuringSetup: "${it.@displayDuringSetup}"
            break
            case "boolean":
               input "${it.@index}", "boolean",
                    title:"${it.@label}\n" + "${it.Help}",
                    defaultValue: "${it.@value}",
                    displayDuringSetup: "${it.@displayDuringSetup}"
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
            sendEvent(name:"needUpdate", value:"NO", displayed:false, isStateChange: true)
        }
        else
        {
            sendEvent(name:"needUpdate", value:"YES", displayed:false, isStateChange: true)
        }
    }
    state.currentProperties = currentProperties
}

def configuration_model_debug()
{
'''
<configuration>
<Value type="list" index="logLevel" label="Debug Logging Level" value="0" setting_type="preference" fw="">
<Help>
</Help>
    <Item label="None" value="0" />
    <Item label="Insanely Verbose" value="-1" />
    <Item label="Very Verbose" value="1" />
    <Item label="Verbose" value="10" />
    <Item label="Reports+Status" value="50" />
    <Item label="Reports" value="99" />
</Value>
</configuration>
'''
}

/* Helper functions included in all Tasmota drivers */
def refresh() {
	log.debug "refresh()"
    def cmds = []
    cmds << getAction(getCommandString("Status", "0"))
    return cmds
}

def reboot() {
	log.debug "reboot()"
    getAction(getCommandString("Restart", "1"))
}

def updated()
{
    logging("updated()", 1)
    def cmds = [] 
    cmds = update_needed_settings()
    sendEvent(name: "checkInterval", value: 2 * 15 * 60 + 2 * 60, displayed: false, data: [protocol: "lan", hubHardwareId: device.hub.hardwareID])
    sendEvent(name:"needUpdate", value: device.currentValue("needUpdate"), displayed:false, isStateChange: true)
    logging(cmds,1)
    if (cmds != [] && cmds != null) cmds
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

def parseDescriptionAsMap(description) {
	description.split(",").inject([:]) { map, param ->
		def nameAndValue = param.split(":")
        
        if (nameAndValue.length == 2) map += [(nameAndValue[0].trim()):nameAndValue[1].trim()]
        else map += [(nameAndValue[0].trim()):""]
	}
}

private getAction(uri){ 
  updateDNI()
  
  def headers = getHeader()
  
  def hubAction = new hubitat.device.HubAction(
    method: "GET",
    path: uri,
    headers: headers
  )
  return hubAction    
}

private postAction(uri, data){ 
  updateDNI()

  def headers = getHeader()

  def hubAction = new hubitat.device.HubAction(
    method: "POST",
    path: uri,
    headers: headers,
    body: data
  )
  return hubAction    
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
<Value type="password" byteSize="1" index="password" label="Device Password" min="" max="" value="" setting_type="preference" fw="">
<Help>
</Help>
</Value>
</configuration>
'''
}
