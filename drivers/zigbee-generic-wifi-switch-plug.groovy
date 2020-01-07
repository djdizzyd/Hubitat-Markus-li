#!include:getHeaderLicense()

#!include:getDefaultImports()

metadata {
	definition (name: "Zigbee - DO NOT USE Generic Wifi Switch/Plug", namespace: "markusl", author: "Markus Liljergren", vid: "generic-switch") {
        capability "Actuator"
        capability "Light"
		capability "Switch"
		capability "Sensor"

        #!include:getDefaultMetadataCapabilities()
        
        #!include:getDefaultMetadataAttributes()
        #!include:getDefaultMetadataCommands()
	}

	simulator {
	}
    
    preferences {
        #!include:getDefaultMetadataPreferences()
	}
}

#!include:getDeviceInfoFunction()

#!include:getGenericOnOffFunctions()

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
    #!include:getGenericZigbeeParseHeader()
            
    #!include:getGenericZigbeeParseFooter()
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

#!include:getDefaultFunctions()

#!include:getLoggingFunction()

#!include:getHelperFunctions('default')
