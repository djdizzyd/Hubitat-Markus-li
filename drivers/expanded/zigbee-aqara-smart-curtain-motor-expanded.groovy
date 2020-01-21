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

/* Inspired by a driver from shin4299 which can be found here:
   https://github.com/shin4299/XiaomiSJ/blob/master/devicetypes/shinjjang/xiaomi-curtain-b1.src/xiaomi-curtain-b1.groovy
*/

/* Default imports */
import groovy.json.JsonSlurper
import groovy.json.JsonOutput


metadata {
	definition (name: "Zigbee - Aqara Smart Curtain Motor", namespace: "markusl", author: "Markus Liljergren", vid: "generic-shade", importURL: "https://raw.githubusercontent.com/markus-li/Hubitat/release/drivers/expanded/zigbee-aqara-smart-curtain-motor-expanded.groovy") {
        capability "Actuator"
        capability "Light"
		capability "Switch"
		capability "Sensor"
        capability "WindowShade"
        capability "Battery"

        
        // Default Capabilities
        capability "Refresh"
        capability "Configuration"
        
        
        // Default Attributes
        attribute   "needUpdate", "string"
        //attribute   "uptime", "string"  // This floods the event log!
        attribute   "ip", "string"
        attribute   "ipLink", "string"
        attribute   "module", "string"
        attribute   "templateData", "string"
        attribute   "driverVersion", "string"
        attribute "lastCheckin", "String"
        attribute "battery2", "Number"
        attribute "batteryLastReplaced", "String"
        
        // Default Commands
        command "reboot"
        command "stop"
        command "altStop"
        command "altClose"
        command "altOpen"
        command "clearPosition"

        // Fingerprint for Aqara Smart Curtain Motor (ZNCLDJ11LM)
		fingerprint endpointId: "01", profileId: "0104", deviceId: "0202", inClusters: "0000, 0003, 0102, 000D, 0013, 0001", outClusters: "0003, 000A", manufacturer: "LUMI", model: "lumi.curtain.hagl04", deviceJoinName: "Xiaomi Curtain B1"
        fingerprint profileId: "0104", inClusters: "0000,0004,0003,0005,000A,0102,000D,0013,0006,0001,0406", outClusters: "0019,000A,000D,0102,0013,0006,0001,0406", manufacturer: "LUMI", model: "lumi.curtain"
        
	}

	simulator {
	}
    
    preferences {
        
        // Default Preferences
        input(name: "runReset", description: "<i>For details and guidance, see the release thread in the <a href=\"https://community.hubitat.com/t/release-tasmota-7-x-firmware-with-hubitat-support/29368\"> Hubitat Forum</a>. For settings marked as ADVANCED, make sure you understand what they do before activating them. If settings are not reflected on the device, press the Configure button in this driver. Also make sure all settings really are saved and correct.<br/>Type RESET and then press 'Save Preferences' to DELETE all Preferences and return to DEFAULTS.</i>", title: "<b>Settings</b>", displayDuringSetup: false, type: "paragraph", element: "paragraph")
        generate_preferences(configuration_model_debug())
        input name: "mode", type: "bool", title: "Curtain Direction", description: "Reverse Mode ON", required: true, displayDuringSetup: true
        input name: "onlySetPosition", type: "bool", title: "Use only Set Position", defaultValue: false, required: true, displayDuringSetup: true
        if(getDeviceDataByName('model') != "lumi.curtain") {
            //Battery Voltage Range
            input name: "voltsmin", type: "decimal", title: "Min Volts (0% battery = ___ volts). Default = 2.8 Volts", description: ""
            input name: "voltsmax", type: "decimal", title: "Max Volts (100% battery = ___ volts). Default = 3.05 Volts", description: ""
        }

	}
}

public getDeviceInfoByName(infoName) { 
    // DO NOT EDIT: This is generated from the metadata!
    // TODO: Figure out how to get this from Hubitat instead of generating this?
    deviceInfo = ['name': 'Zigbee - Aqara Smart Curtain Motor', 'namespace': 'markusl', 'author': 'Markus Liljergren', 'vid': 'generic-shade', 'importURL': 'https://raw.githubusercontent.com/markus-li/Hubitat/release/drivers/expanded/zigbee-aqara-smart-curtain-motor-expanded.groovy']
    //logging("deviceInfo[${infoName}] = ${deviceInfo[infoName]}", 1)
    return(deviceInfo[infoName])
}

/* These functions are unique to each driver */
private getCLUSTER_BASIC() { 0x0000 }
private getCLUSTER_POWER() { 0x0001 }
private getCLUSTER_WINDOW_COVERING() { 0x0102 }
private getCLUSTER_WINDOW_POSITION() { 0x000d }
private getCLUSTER_ON_OFF() { 0x0006 }
private getBASIC_ATTR_POWER_SOURCE() { 0x0007 }
private getPOWER_ATTR_BATTERY_PERCENTAGE_REMAINING() { 0x0021 }
private getPOSITION_ATTR_VALUE() { 0x0055 }
private getCOMMAND_OPEN() { 0x00 }
private getCOMMAND_CLOSE() { 0x01 }
private getCOMMAND_PAUSE() { 0x02 }
private getENCODING_SIZE() { 0x39 }

def refresh() {
    logging("refresh() model='${getDeviceDataByName('model')}'", 10)
    // http://ftp1.digi.com/support/images/APP_NOTE_XBee_ZigBee_Device_Profile.pdf
    // https://docs.hubitat.com/index.php?title=Zigbee_Object
    // https://docs.smartthings.com/en/latest/ref-docs/zigbee-ref.html
    // https://www.nxp.com/docs/en/user-guide/JN-UG-3115.pdf

    def cmds = []
    cmds += zigbee.readAttribute(CLUSTER_BASIC, BASIC_ATTR_POWER_SOURCE)
    cmds += zigbee.readAttribute(CLUSTER_POWER, POWER_ATTR_BATTERY_PERCENTAGE_REMAINING)
    cmds += zigbee.readAttribute(CLUSTER_WINDOW_POSITION, POSITION_ATTR_VALUE)
    return cmds
}

def reboot() {
    logging('reboot() is NOT implemented for this device', 1)
    // Ignore
}
// description:read attr - raw: 05470100000A07003001, dni: 0547, endpoint: 01, cluster: 0000, size: 0A, attrId: 0007, encoding: 30, command: 01, value: 01, parseMap:[raw:05470100000A07003001, dni:0547, endpoint:01, cluster:0000, size:0A, attrId:0007, encoding:30, command:01, value:01, clusterInt:0, attrInt:7]
// Closed curtain: read attr - raw: 054701000D1055003900000000, dni: 0547, endpoint: 01, cluster: 000D, size: 10, attrId: 0055, encoding: 39, command: 01, value: 00000000
// PArtially open: msgMap: [raw:054701000D1C5500390000C84200F02300000000, dni:0547, endpoint:01, cluster:000D, size:1C, attrId:0055, encoding:39, command:0A, value:42C80000, clusterInt:13, attrInt:85, additionalAttrs:[[value:00000000, encoding:23, attrId:F000, consumedBytes:7, attrInt:61440]]]
// 0104 000A 01 01 0040 00 0547 00 00 0000 00 00 0000, profileId:0104, clusterId:000A, clusterInt:10, sourceEndpoint:01, destinationEndpoint:01, options:0040, messageType:00, dni:0547, isClusterSpecific:false, isManufacturerSpecific:false, manufacturerId:0000, command:00, direction:00, data:[00, 00]]
// Fully open: 
def parse(description) {
    // parse() Generic Zigbee-device header BEGINS here
    logging("Parsing: ${description}", 0)
    def events = []
    def msgMap = zigbee.parseDescriptionAsMap(description)
    logging("msgMap: ${msgMap}", 0)
    // parse() Generic header ENDS here
    
    if (msgMap["profileId"] == "0104") {
        logging("Unhandled KNOWN event - description:${description} | parseMap:${msgMap}", 1)
        logging("RAW: ${msgMap["attrId"]}", 1)
        // catchall: 0104 000A 01 01 0040 00 63A1 00 00 0000 00 00 0000, parseMap:[raw:catchall: 0104 000A 01 01 0040 00 63A1 00 00 0000 00 00 0000, profileId:0104, clusterId:000A, clusterInt:10, sourceEndpoint:01, destinationEndpoint:01, options:0040, messageType:00, dni:63A1, isClusterSpecific:false, isManufacturerSpecific:false, manufacturerId:0000, command:00, direction:00, data:[00, 00]]
    } else if (msgMap["cluster"] == "0000" && msgMap["attrId"] == "0404") {
        logging("Unhandled KNOWN event - description:${description} | parseMap:${msgMap}", 1)
        //read attr - raw: 63A10100000804042000, dni: 63A1, endpoint: 01, cluster: 0000, size: 08, attrId: 0404, encoding: 20, command: 0A, value: 00, parseMap:[raw:63A10100000804042000, dni:63A1, endpoint:01, cluster:0000, size:08, attrId:0404, encoding:20, command:0A, value:00, clusterInt:0, attrInt:1028]
    } else if (msgMap["cluster"] == "0000" && msgMap["attrId"] == "0005") {
        logging("Unhandled KNOWN event (pressed button) - description:${description} | parseMap:${msgMap}", 1)
        // read attr - raw: 63A1010000200500420C6C756D692E6375727461696E, dni: 63A1, endpoint: 01, cluster: 0000, size: 20, attrId: 0005, encoding: 42, command: 0A, value: 0C6C756D692E6375727461696E, parseMap:[raw:63A1010000200500420C6C756D692E6375727461696E, dni:63A1, endpoint:01, cluster:0000, size:20, attrId:0005, encoding:42, command:0A, value:lumi.curtain, clusterInt:0, attrInt:5]
    } else if (msgMap["cluster"] == "0000" && msgMap["attrId"] == "0007") {
        logging("Unhandled KNOWN event (BASIC_ATTR_POWER_SOURCE) - description:${description} | parseMap:${msgMap}", 1)
        // Answer to zigbee.readAttribute(CLUSTER_BASIC, BASIC_ATTR_POWER_SOURCE)
        //read attr - raw: 63A10100000A07003001, dni: 63A1, endpoint: 01, cluster: 0000, size: 0A, attrId: 0007, encoding: 30, command: 01, value: 01, parseMap:[raw:63A10100000A07003001, dni:63A1, endpoint:01, cluster:0000, size:0A, attrId:0007, encoding:30, command:01, value:01, clusterInt:0, attrInt:7]
    } else if (msgMap["cluster"] == "0102" && msgMap["attrId"] == "0008") {
        logging("Position event (after pressing stop) - description:${description} | parseMap:${msgMap}", 1)
        long theValue = Long.parseLong(msgMap["value"], 16)
        curtainPosition = theValue.intValue()
        logging("GETTING POSITION from cluster 0102: int => ${curtainPosition}", 1)
        events << positionEvent(curtainPosition)
        //read attr - raw: 63A1010102080800204E, dni: 63A1, endpoint: 01, cluster: 0102, size: 08, attrId: 0008, encoding: 20, command: 0A, value: 4E
        //read attr - raw: 63A1010102080800203B, dni: 63A1, endpoint: 01, cluster: 0102, size: 08, attrId: 0008, encoding: 20, command: 0A, value: 3B | parseMap:[raw:63A1010102080800203B, dni:63A1, endpoint:01, cluster:0102, size:08, attrId:0008, encoding:20, command:0A, value:3B, clusterInt:258, attrInt:8]
    } else if (msgMap["cluster"] == "0000" && (msgMap["attrId"] == "FF01" || msgMap["attrId"] == "FF02")) {
        // This is probably the battery event, like in other Xiaomi devices... it can also be FF02
        logging("KNOWN event (probably battery) - description:${description} | parseMap:${msgMap}", 1)
        // TODO: Test this, I don't have the battery version...
        // 1C (file separator??) is missing in the beginning of the value after doing this encoding...
        if(getDeviceDataByName('model') != "lumi.curtain") {
            events << createEvent(parseBattery(msgMap["value"].getBytes().encodeHex().toString().toUpperCase()))
        }
        //read attr - raw: 63A10100004001FF421C03281E05210F00642000082120110727000000000000000009210002, dni: 63A1, endpoint: 01, cluster: 0000, size: 40, attrId: FF01, encoding: 42, command: 0A, value: 1C03281E05210F00642000082120110727000000000000000009210002, parseMap:[raw:63A10100004001FF421C03281E05210F00642000082120110727000000000000000009210002, dni:63A1, endpoint:01, cluster:0000, size:40, attrId:FF01, encoding:42, command:0A, value:(!d ! '	!, clusterInt:0, attrInt:65281]
    } else if (msgMap["cluster"] == "000D" && msgMap["attrId"] == "0055") {
        logging("cluster 000D", 1)
		if (msgMap["size"] == "16" || msgMap["size"] == "1C" || msgMap["size"] == "10") {
			long theValue = Long.parseLong(msgMap["value"], 16)
			float floatValue = Float.intBitsToFloat(theValue.intValue());
			logging("GETTING POSITION: long => ${theValue}, float => ${floatValue}", 1)
			curtainPosition = floatValue.intValue()
			events << positionEvent(curtainPosition)
		} else if (msgMap["size"] == "28" && msgMap["value"] == "00000000") {
			logging("done…", 1)
			sendHubCommand(zigbee.readAttribute(CLUSTER_WINDOW_POSITION, POSITION_ATTR_VALUE))                
		}
	} else if (msgMap["clusterId"] == "0001" && msgMap["attrId"] == "0021") {
        if(getDeviceDataByName('model') != "lumi.curtain") {
            def bat = msgMap["value"]
            long value = Long.parseLong(bat, 16)/2
            logging("Battery: ${value}%, ${bat}", 1)
            events << createEvent(name:"battery", value: value)
        }

	} else if (msgMap["clusterId"] == "000A") {
		logging("Xiaomi Curtain Present Event", 1)
	} else {
		log.warn "Unhandled Event - description:${description} | parseMap:${msgMap}"
	}
    logging("-----------------------", 1)
    // parse() Generic Zigbee-device footer BEGINS here
    
    return events
    // parse() Generic footer ENDS here
}

def positionEvent(curtainPosition) {
	def windowShadeStatus = ""
	if(mode == true) {
        curtainPosition = 100 - curtainPosition
	}
    if (curtainPosition == 100) {
        logging("Fully Open", 1)
        windowShadeStatus = "open"
    } else if (curtainPosition > 0) {
        logging(curtainPosition + '% Partially Open', 1)
        windowShadeStatus = "partially open"
    } else {
        logging("Closed", 1)
        windowShadeStatus = "closed"
    }
	def events = []
	events << createEvent(name:"windowShade", value: windowShadeStatus)
	events << createEvent(name:"position", value: curtainPosition)
	events << createEvent(name:"switch", value: (windowShadeStatus == "closed" ? "off" : "on"))
	return events
}

// Convert raw 4 digit integer voltage value into percentage based on minVolts/maxVolts range
private parseBattery(hexString) {
    // All credits go to veeceeoh for this battery parsing method!
    logging("Battery full string = ${hexString}", 1)
    // Moved this one byte to the left due to how the built-in parser work, needs testing!
	//def hexBattery = (hexString[8..9] + hexString[6..7])
    def hexBattery = (hexString[6..7] + hexString[4..5])
    logging("Battery parsed string = ${hexBattery}", 1)
	def rawValue = Integer.parseInt(hexBattery,16)
	def rawVolts = rawValue / 1000
	def minVolts = voltsmin ? voltsmin : 2.8
	def maxVolts = voltsmax ? voltsmax : 3.05
	def pct = (rawVolts - minVolts) / (maxVolts - minVolts)
	def roundedPct = Math.min(100, Math.round(pct * 100))
	logging("Battery report: $rawVolts Volts ($roundedPct%), calculating level based on min/max range of $minVolts to $maxVolts", 1)
	def descText = "Battery level is $roundedPct% ($rawVolts Volts)"
	return [
		name: 'battery2',
		value: roundedPct,
		unit: "%",
		descriptionText: descText
	]
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

def update_needed_settings()
{
    
}

def close() {
    logging("close()", 1)
	setPosition(0)    
}

def open() {
    logging("open()", 1)
	setPosition(100)    
}

def altOpen() {
    logging("altOpen()", 1)
	def cmd = []
	cmd += zigbee.command(CLUSTER_WINDOW_COVERING, COMMAND_OPEN)
    return cmd  
}

def altClose() {
    logging("altClose()", 1)
	def cmd = []
	cmd += zigbee.command(CLUSTER_WINDOW_COVERING, COMMAND_CLOSE)
    return cmd  
}

def on() {
    logging("on()", 1)
	open()
}


def off() {
    logging("off()", 1)
	close()
}


def stop() {
    logging("stop()", 1)
    def cmd = []
	cmd += zigbee.command(CLUSTER_WINDOW_COVERING, COMMAND_PAUSE)
    //cmd += zigbee.readAttribute(CLUSTER_WINDOW_POSITION, POSITION_ATTR_VALUE)
    return cmd
}

def altStop() {
    logging("altStop()", 1)
    def cmd = []
	cmd += zigbee.command(CLUSTER_ON_OFF, COMMAND_PAUSE)
    return cmd
}

def clearPosition() {
    logging("clearPosition()", 1)
    def cmd = []
	cmd += zigbee.writeAttribute(CLUSTER_BASIC, 0xFF27, 0x10, 0x00)
    logging("cmd=${cmd}", 1)
    return cmd
}

def setPosition(position) {
    if (position == null) {position = 0}
    position = position as int
    logging("setPosition(position: ${position})", 1)
    Integer  currentPosition = device.currentValue("position")
    if (position > currentPosition) {
        sendEvent(name: "windowShade", value: "opening")
    } else if (position < currentLevel) {
        sendEvent(name: "windowShade", value: "closing")
    }
    if(onlySetPosition == false && position == 100) {
        if(mode == true){
            logging("Command: Close", 1)
        } else {
            logging("Command: Open", 1)
        }
        logging("cluster: ${CLUSTER_ON_OFF}, command: ${COMMAND_OPEN}", 1)
        cmd = zigbee.command(CLUSTER_ON_OFF, COMMAND_CLOSE)
        logging("cmd=${cmd}", 1)
        return cmd
    } else if (onlySetPosition == false && position < 1) {
        if(mode == true){
            logging("Command: Open", 1)
        } else {
            logging("Command: Close", 1)
        }
        logging("cluster: ${CLUSTER_ON_OFF}, command: ${COMMAND_CLOSE}", 1)

        cmd = zigbee.command(CLUSTER_ON_OFF, COMMAND_OPEN)
        logging("cmd=${cmd}", 1)
        return cmd
    } else {
        if(mode == true){
            position = (100 - position) as int
        }
        logging("Set Position: ${position}%", 1)
        //logging("zigbee.writeAttribute(getCLUSTER_WINDOW_POSITION()=${CLUSTER_WINDOW_POSITION}, getPOSITION_ATTR_VALUE()=${POSITION_ATTR_VALUE}, getENCODING_SIZE()=${ENCODING_SIZE}, position=${Float.floatToIntBits(position)})", 1)
        return zigbee.writeAttribute(CLUSTER_WINDOW_POSITION, POSITION_ATTR_VALUE, ENCODING_SIZE, Float.floatToIntBits(position))
    }
}


/* Default functions go here */
private def getDriverVersion() {
    logging("getDriverVersion()", 50)
	def cmds = []
    comment = ""
    if(comment != "") state.comment = comment
    sendEvent(name: "driverVersion", value: "v0.9.0")
    return cmds
}


/* Logging function included in all drivers */
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
        }
    }
}


/* Helper functions included in all drivers/apps */
def isDriver() {
    try {
        // If this fails, this is not a driver...
        getDeviceDataByName('_unimportant')
        logging("This IS a driver!", 1)
        return true
    } catch (MissingMethodException e) {
        logging("This is NOT a driver!", 1)
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
                    title:"<b>${it.@label}</b>\n" + "${it.Help}",
                    description: "<i>${it.@description}</i>",
                    range: "${it.@min}..${it.@max}",
                    defaultValue: "${it.@value}",
                    displayDuringSetup: "${it.@displayDuringSetup}"
            break
            case "list":
                def items = []
                it.Item.each { items << ["${it.@value}":"${it.@label}"] }
                input "${it.@index}", "enum",
                    title:"<b>${it.@label}</b>\n" + "${it.Help}",
                    description: "<i>${it.@description}</i>",
                    defaultValue: "${it.@value}",
                    displayDuringSetup: "${it.@displayDuringSetup}",
                    options: items
            break
            case ["password"]:
                input "${it.@index}", "password",
                    title:"<b>${it.@label}</b>\n" + "${it.Help}",
                    description: "<i>${it.@description}</i>",
                    displayDuringSetup: "${it.@displayDuringSetup}"
            break
            case "decimal":
               input "${it.@index}", "decimal",
                    title:"<b>${it.@label}</b>\n" + "${it.Help}",
                    description: "<i>${it.@description}</i>",
                    range: "${it.@min}..${it.@max}",
                    defaultValue: "${it.@value}",
                    displayDuringSetup: "${it.@displayDuringSetup}"
            break
            case "boolean":
               input "${it.@index}", "boolean",
                    title:"<b>${it.@label}</b>\n" + "${it.Help}",
                    description: "<i>${it.@description}</i>",
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
        } else {
            //app.clearSetting("logLevel")
            // To be able to update the setting, it has to be removed first, clear does NOT work, at least for Apps
            app.removeSetting("logLevel")
            app.updateSetting("logLevel", "0")
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

def configuration_model_debug()
{
'''
<configuration>
<Value type="list" index="logLevel" label="Debug Log Level" description="Under normal operations, set this to None. Only needed for debugging. Auto-disabled after 30 minutes." value="-1" setting_type="preference" fw="">
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
