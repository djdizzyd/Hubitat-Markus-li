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

        //command "clear"
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
        } else {
            log.warn "Got '$it.name' attribute data, but doesn't know what to do with it! Did you choose the right device type?"
        }
    }
}

void updated() {
    log.info "updated()"
    refresh()
}

void installed() {
    log.info "installed()"
    device.removeSetting("logLevel")
    device.updateSetting("logLevel", "100")
    refresh()
}

void refresh() {
    #!include:getChildComponentMetaConfigCommands()
    parent?.componentRefresh(this.device)
}

/**
 * -----------------------------------------------------------------------------
 * Everything below here are LIBRARY includes and should NOT be edited manually!
 * -----------------------------------------------------------------------------
 * --- Nothing to edit here, move along! ---------------------------------------
 * -----------------------------------------------------------------------------
 */

#!include:getHelperFunctions('all-default')

#!include:getHelperFunctions('driver-metadata')

#!include:getHelperFunctions('styling')

#!include:getLoggingFunction(specialDebugLevel=True)
