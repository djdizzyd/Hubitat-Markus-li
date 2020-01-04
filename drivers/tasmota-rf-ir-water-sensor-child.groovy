#!include:getHeaderLicense()

#!include:getDefaultImports()

metadata {
    definition (name: "Tasmota - RF/IR Water Sensor (Child)", namespace: "tasmota", author: "Markus Liljergren") {
        capability "WaterSensor"
        capability "Sensor"

        #!include:getLearningModeAttributes()
        
        #!include:getLearningModeCommands()
        command "clear"
    }

    preferences {
        #!include:getDefaultMetadataPreferences()
        generateLearningPreferences()
    }
}

#!include:getDeviceInfoFunction()

#!include:getGenerateLearningPreferences(types='["Dry", "Wet"]', default_type='Wet')

/* These functions are unique to each driver */
void dry() {
    logging("dry()", 1)
    sendEvent(name: "water", value: "dry", isStateChange: true)
}

void wet() {
    logging("wet()", 1)
    sendEvent(name: "water", value: "wet", isStateChange: true)
}

void clear() {
    logging("clear()", 1)
    dry()
}

// These are called when Action occurs, called from actionHandler()
def dryAction() {
    logging("dryAction()", 1)
    dry()
}

def wetAction() {
    logging("wetAction()", 1)
    wet()
}

#!include:getHelperFunctions('code-learning')

#!include:getCalculateB0()

#!include:getDefaultFunctions()

#!include:getHelperFunctions('default')

#!include:getLoggingFunction(specialDebugLevel=True)
