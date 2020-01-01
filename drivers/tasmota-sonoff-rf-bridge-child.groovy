#!include:getHeaderLicense()

#!include:getDefaultImports()

metadata {
    definition (name: "Tasmota - Sonoff RF Bridge (Child)", namespace: "tasmota", author: "Markus Liljergren") {
        capability "Switch"
        capability "Actuator"
        capability "Momentary"

        #!include:getLearningModeAttributes()
        
        #!include:getLearningModeCommands()
    }

    preferences {
        #!include:getDefaultMetadataPreferences()
        generateLearningPreferences()
    }
}

#!include:getDeviceInfoFunction()

#!include:getGenerateLearningPreferences(types='["Toggle", "Push", "On", "Off"]', default_type='Toggle')

/* These functions are unique to each driver */


void on() {
    logging("on()", 1)
    sendEvent(name: "switch", value: "on", isStateChange: true)
}

void off() {
    logging("off()", 1)
    sendEvent(name: "switch", value: "off", isStateChange: true)
}

void push() {
    logging("$device pushed button", 1)
    sendEvent(name: "switch", value: "on", isStateChange: true)
    runIn(1, off)
}

// These are called when Action occurs, called from actionHandler()
def pushAction() {
    logging("pushAction()", 1)
    push()
}

def onAction() {
    logging("onAction()", 1)
    on()
}

def offAction() {
    logging("offAction()", 1)
    off()
}

def toggleAction() {
    logging("togglection()", 1)
    if(device.currentValue('switch', true) == 'on') {
        off()
    } else {
        on()
    }
}

#!include:getHelperFunctions('code-learning')

#!include:getCalculateB0()

#!include:getDefaultFunctions()

#!include:getHelperFunctions('default')

#!include:getLoggingFunction(specialDebugLevel=True)
