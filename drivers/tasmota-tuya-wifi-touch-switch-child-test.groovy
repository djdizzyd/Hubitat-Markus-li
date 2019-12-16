#!include:getHeaderLicense()

#!include:getDefaultImports()

// THIS IS NOT A REAL DRIVER AND IS ONLY USED FOR TESTING THE GENERATOR AND AUTO-PUBLISHING SYSTEM!

metadata {
    definition (name: "Tasmota - Tuya Wifi Touch Switch TEST (Child)", namespace: "tasmota", author: "Markus Liljergren") {
        capability "Switch"
        capability "Actuator"
    }
}

#!include:getDeviceInfoFunction()

/* These functions are unique tos each driver */
void on() { 
    logging("$device on",1)
    parent.childOn(device.deviceNetworkId)
}

void off() {
    logging("$device off",1)
    parent.childOff(device.deviceNetworkId)
}

#!include:getLoggingFunction()
