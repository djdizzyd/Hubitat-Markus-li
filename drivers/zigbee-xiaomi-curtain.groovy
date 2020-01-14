#!include:getHeaderLicense()

/* Inspired by a driver from shin4299 that can be found here:
   https://github.com/shin4299/XiaomiSJ/blob/master/devicetypes/shinjjang/xiaomi-curtain-b1.src/xiaomi-curtain-b1.groovy
*/

metadata {
	definition (name: "Zigbee - DO NOT USE Xiaomi Curtain", namespace: "markusl", author: "Markus Liljergren", vid: "generic-shade") {
        capability "Actuator"
        capability "Light"
		capability "Switch"
		capability "Sensor"
        capability "WindowShade"

        #!include:getDefaultMetadataCapabilities()
        
        #!include:getDefaultMetadataAttributes()
        #!include:getDefaultMetadataCommands()
        command "stop"

        // Fingerprint for Aqara Smart Curtain Motor (ZNCLDJ11LM)
		fingerprint profileId: "0104", inClusters: "0000,0004,0003,0005,000A,0102,000D,0013,0006,0001,0406", outClusters: "0019,000A,000D,0102,0013,0006,0001,0406", manufacturer: "LUMI", model: "lumi.curtain"
	}

	simulator {
	}
    
    preferences {
        #!include:getDefaultMetadataPreferences()
        input name: "mode", type: "bool", title: "Curtain Direction", description: "Reverse Mode ON", required: true, displayDuringSetup: true
	}
}

#!include:getDeviceInfoFunction()

/* These functions are unique to each driver */
private getCLUSTER_BASIC() { 0x0000 }
private getCLUSTER_POWER() { 0x0001 }
private getCLUSTER_WINDOW_COVERING() { 0x0102 }
private getCLUSTER_WINDOW_POSITION() { 0x000d }
private getBASIC_ATTR_POWER_SOURCE() { 0x0007 }
private getPOWER_ATTR_BATTERY_PERCENTAGE_REMAINING() { 0x0021 }
private getPOSITION_ATTR_VALUE() { 0x0055 }
private getCOMMAND_OPEN() { 0x00 }
private getCOMMAND_CLOSE() { 0x01 }
private getCOMMAND_PAUSE() { 0x02 }
private getENCODING_SIZE() { 0x39 }

def refresh() {
    logging("refresh()", 10)
    // http://ftp1.digi.com/support/images/APP_NOTE_XBee_ZigBee_Device_Profile.pdf
    // https://docs.hubitat.com/index.php?title=Zigbee_Object
    // https://docs.smartthings.com/en/latest/ref-docs/zigbee-ref.html
    //zigbee.clusterLookup(0x0001)
    /*msgMap = [profileId:0, clusterId:"0x0001", sourceEndpoint:0, 
              destinationEndpoint:0, options:0, messageType:0, dni:"${device.endpointId}", 
              isClusterSpecific:false, isManufacturerSpecific:false, manufacturerId:0, 
              command:0, direction:0]
    
    logging("${device.deviceNetworkId}", 10)
    logging(zigbee.swapOctets("${device.deviceNetworkId}"), 10)
    zigbee.command(0x0001, 0, "${device.deviceNetworkId}")
    zigbee.command(0x0001, 0, zigbee.swapOctets("${device.deviceNetworkId}"))
    zigbee.command(0x0001, 0, msgMap)
    zigbee.command(0x0001, 0, '')
    zigbee.enrollResponse()*/
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
    /*return [
        "he cmd 0x${device.deviceNetworkId} 0x${device.endpointId} 0x0006 0 {}"
    ]*/
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

def parse(description) {
    #!include:getGenericZigbeeParseHeader()
    if (msgMap["cluster"] == "000D" && msgMap["attrId"] == "0055") {
		if (msgMap["size"] == "16") {
			long theValue = Long.parseLong(msgMap["value"], 16)
			float floatValue = Float.intBitsToFloat(theValue.intValue());
			logging("long => ${theValue}, float => ${floatValue}", 1)
			curtainPosition = floatValue.intValue()
			events << positionEvent(curtainPosition)
		} else if (msgMap["size"] == "28" && msgMap["value"] == "00000000") {
			logging("done…", 1)
			sendHubCommand(zigbee.readAttribute(CLUSTER_WINDOW_POSITION, POSITION_ATTR_VALUE))                
		}
	} else if (msgMap["cluster"] == "0001" && msgMap["attrId"] == "0021") {
		def bat = msgMap["value"]
		long value = Long.parseLong(bat, 16)/2
		logging("Battery: ${value}%, ${bat}", 1)
		events << createEvent(name:"battery", value: value)
	} else if (msgMap["clusterId"] == "000A") {
		logging("Xiaomi Curtain Present Event", 1)
	} else {
		log.warn "Unhandled Event - description:${description}, parseMap:${msgMap}"
	}
    #!include:getGenericZigbeeParseFooter()
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
	eventStack << createEvent(name:"windowShade", value: windowShadeStatus as String)
	eventStack << createEvent(name:"position", value: curtainPosition)
	eventStack << createEvent(name:"switch", value: (windowShadeStatus == "closed" ? "off" : "on"))
	return events
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
	zigbee.command(CLUSTER_WINDOW_COVERING, COMMAND_PAUSE)
}

def setPosition(position) {
    if (position == null) {position = 0}
    position = position as int
    logging("setPosition(position: ${position})", 1)
    Integer  currentPosition = device.currentValue("position")
    if (position > currentPosition) {
        sendEvent(name: "windowShade", value: "opening")
    } else if (level < currentLevel) {
        sendEvent(name: "windowShade", value: "closing")
    }
	if(mode == true){
		position = (100 - position) as int
    }
	log.info "Set Position: ${position}%"
	//String hex = Integer.toHexString(Float.floatToIntBits(level)).toUpperCase()
	zigbee.writeAttribute(CLUSTER_WINDOW_POSITION, POSITION_ATTR_VALUE, ENCODING_SIZE, position)
}

#!include:getDefaultFunctions()

#!include:getLoggingFunction()

#!include:getHelperFunctions('default')
