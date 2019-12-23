#!include:getHeaderLicense()

#!include:getDefaultImports()

metadata {
    definition (name: "Tasmota - Generic Power Monitor Plug (Child)", namespace: "tasmota", author: "Markus Liljergren") {
        capability "Switch"
        capability "Actuator"
    }
}

#!include:getDeviceInfoFunction()

/* These functions are unique to each driver */
void on() { 
    logging("$device on",1)
    parent.childOn(device.deviceNetworkId)
}

void off() {
    logging("$device off",1)
    parent.childOff(device.deviceNetworkId)
}

#!include:getDefaultFunctions()

#!include:getLoggingFunction()
