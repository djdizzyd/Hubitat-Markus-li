#!include:getHeaderLicense()

#!include:getDefaultImports()

metadata {
    definition (name: "Tasmota - RF/IR Motion Sensor (Child)", namespace: "tasmota", author: "Markus Liljergren") {
        capability "MotionSensor"

        #!include:getLearningModeAttributes()
        
        #!include:getLearningModeCommands()
    }

    preferences {
        #!include:getDefaultMetadataPreferences()
        input(name: "recoveryTime", type: "number", title: "<b>Recovery Time</b>", description: "<i>Set the number of seconds before returning to Inactive (default: 5)</i>", defaultValue: "5", displayDuringSetup: true, required: true)
        generateLearningPreferences()
    }
}

#!include:getDeviceInfoFunction()

#!include:getGenerateLearningPreferences(types='["Active", "Inactive"]', default_type='Active')

/* These functions are unique to each driver */
void active() {
    logging("active()", 1)
    sendEvent(name: "motion", value: "active", isStateChange: true)
    logging("Recovery time: ${recoveryTime ?: 5}", 100)
    runIn(recoveryTime ?: 5, inactive)
}

void inactive() {
    logging("inactive()", 1)
    sendEvent(name: "motion", value: "inactive", isStateChange: true)
}

// These are called when Action occurs, called from actionHandler()
def activeAction() {
    logging("activeAction()", 1)
    active()
}

def inactiveAction() {
    logging("inactiveAction()", 1)
    inactive()
}

#!include:getHelperFunctions('code-learning')

#!include:getCalculateB0()

#!include:getDefaultFunctions()

#!include:getHelperFunctions('default')

#!include:getLoggingFunction(specialDebugLevel=True)
