#!include:getHeaderLicense()

#!include:getDefaultImports()

metadata {
    definition (name: "Tasmota - RF/IR Smoke Detector (Child)", namespace: "tasmota", author: "Markus Liljergren") {
        capability "SmokeDetector"

        #!include:getLearningModeAttributes()
        
        #!include:getLearningModeCommands()
        command "clear"
        command "tested"
    }

    preferences {
        #!include:getDefaultMetadataPreferences()
        generateLearningPreferences()
    }
}

#!include:getDeviceInfoFunction()

#!include:getGenerateLearningPreferences(types='["Clear", "Tested", "Detected"]', default_type='Detected')

/* These functions are unique to each driver */
void detected() {
    logging("detected()", 1)
    sendEvent(name: "smoke", value: "detected", isStateChange: true)
}

void clear() {
    logging("clear()", 1)
    sendEvent(name: "smoke", value: "clear", isStateChange: true)
}

void tested() {
    logging("tested()", 1)
    sendEvent(name: "smoke", value: "tested", isStateChange: true)
}

// These are called when Action occurs, called from actionHandler()
def detectedAction() {
    logging("detectedAction()", 1)
    detected()
}

def clearAction() {
    logging("clearAction()", 1)
    clear()
}

def testedAction() {
    logging("testedAction()", 1)
    tested()
}

#!include:getHelperFunctions('code-learning')

#!include:getCalculateB0()

#!include:getDefaultFunctions()

#!include:getHelperFunctions('default')

#!include:getLoggingFunction(specialDebugLevel=True)
