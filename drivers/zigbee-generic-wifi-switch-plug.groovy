#!include:getHeaderLicense()

#!include:getDefaultImports()

metadata {
	definition (name: "Zigbee - Generic Wifi Switch/Plug", namespace: "markusl", author: "Markus Liljergren", vid: "generic-switch") {
        capability "Actuator"
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
    // https://docs.hubitat.com/index.php?title=Zigbee_Object
    return  [
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
    ]
}

def reboot() {
    logging('reboot()', 1)
    // Ignore
}

def parse(description) {
    #!include:getGenericZigbeeParseHeader()
            
    #!include:getGenericZigbeeParseFooter()
}

def update_needed_settings()
{
    
}

#!include:getDefaultFunctions()

#!include:getLoggingFunction()

#!include:getHelperFunctions('default')
