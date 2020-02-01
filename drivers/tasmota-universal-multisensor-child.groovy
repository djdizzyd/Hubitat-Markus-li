#!include:getHeaderLicense()

#!include:getDefaultImports()

metadata {
    // Do NOT rename the child driver name unless you also change the corresponding code in the Parent!
    definition (name: "Tasmota - Universal Multisensor (Child)", namespace: "tasmota", author: "Markus Liljergren") {
        capability "Sensor"
        capability "TemperatureMeasurement"       // Attributes: temperature - NUMBER
        capability "RelativeHumidityMeasurement"  // Attributes: humidity - NUMBER
        capability "PressureMeasurement"          // Attributes: pressure - NUMBER
        capability "IlluminanceMeasurement"       // Attributes: illuminance - NUMBER
        capability "MotionSensor"                 // Attributes: motion - ENUM ["inactive", "active"]
        capability "WaterSensor"                  // Attributes: water - ENUM ["wet", "dry"]
        capability "Refresh"

        // Non-standard sensor attributes
        attribute  "distance", "string"
        attribute  "pressureWithUnit", "string"

        command "clear"
    }

    preferences {
        #!include:getDefaultMetadataPreferences()

    }

    // The below line needs to exist in ALL drivers for custom CSS to work!
    #!include:getMetadataCustomizationMethods()
}

#!include:getDeviceInfoFunction()

/* These functions are unique to each driver */
void parse(List<Map> description) {
    description.each {
        if (it.name in ["temperature", "humidity", "pressure", "pressureWithUnit",
            "illuminance", "motion", "water", "distance"]) {
            logging(it.descriptionText, 100)
            sendEvent(it)
        }
    }
}

void updated() {
    log.info "updated()"
}

void installed() {
    log.info "installed()"
    device.removeSetting("logLevel")
    device.updateSetting("logLevel", "100")
    refresh()
}

/*void dry() {
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
}*/

void refresh() {
    parent?.componentRefresh(this.device)
}

/*
    -----------------------------------------------------------------------------
    Everything below here are LIBRARY includes and should NOT be edited manually!
    -----------------------------------------------------------------------------
    --- Nothings to edit here, move along! --------------------------------------
    -----------------------------------------------------------------------------
*/

#!include:getHelperFunctions('all-default')

#!include:getHelperFunctions('driver-metadata')

#!include:getLoggingFunction(specialDebugLevel=True)
