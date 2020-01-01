#!include:getHeaderLicense()

#!include:getDefaultImports()

metadata {
    definition (name: "Tasmota - RF/IR Contact Sensor (Child)", namespace: "tasmota", author: "Markus Liljergren") {
        capability "ContactSensor"

        #!include:getLearningModeAttributes()
        
        #!include:getLearningModeCommands()
    }

    preferences {
        #!include:getDefaultMetadataPreferences()
        input(name: "recoveryTime", type: "number", title: "<b>Recovery Time</b>", description: "<i>Set the number of seconds before returning to Closed (default: -1=disabled)</i>", defaultValue: "-1", displayDuringSetup: true, required: true)
        generateLearningPreferences()
    }
}

#!include:getDeviceInfoFunction()

#!include:getGenerateLearningPreferences(types='["Open", "Closed"]', default_type='Open')

/* These functions are unique to each driver */
void open() {
    logging("open()", 1)
    sendEvent(name: "contact", value: "open", isStateChange: true)
    recoveryTime = recoveryTime ?: -1
    logging("Recovery time: ${recoveryTime}", 1)
    if(recoveryTime > 0) runIn(recoveryTime, closed)
}

void closed() {
    logging("closed()", 1)
    sendEvent(name: "contact", value: "closed", isStateChange: true)
}

// These are called when Action occurs, called from actionHandler()
def openAction() {
    logging("openAction()", 1)
    open()
}

def closedAction() {
    logging("closedAction()", 1)
    closed()
}

#!include:getHelperFunctions('code-learning')

#!include:getCalculateB0()

#!include:getDefaultFunctions()

#!include:getHelperFunctions('default')

#!include:getLoggingFunction(specialDebugLevel=True)
