#!include:getHeaderLicense()

#!include:getDefaultImports()

metadata {
    // Do NOT rename the child driver name unless you also change the corresponding code in the Parent!
    definition (name: "Tasmota - Universal Multi Sensor (Child)", namespace: "tasmota", author: "Markus Liljergren") {
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
        input(name: "hideMeasurementAdjustments", type: "bool", title: addTitleDiv("Hide Measurement Adjustment Preferences"), description: "", defaultValue: false, displayDuringSetup: false, required: false)
        #!include:getDefaultMetadataPreferencesForTHMonitor()
        
    }

    // The below line needs to exist in ALL drivers for custom CSS to work!
    #!include:getMetadataCustomizationMethods()
}

#!include:getDeviceInfoFunction()

/* These functions are unique to each driver */
void parse(List<Map> description) {
    description.each {
        if(it.name in ["illuminance", "motion", "water", "distance"]) {
            logging(it.descriptionText, 100)
            sendEvent(it)
        } else if(it.name == "temperature") {
            // Offset the temperature based on preference
            c = String.valueOf((char)(Integer.parseInt("00B0", 16))); // Creates a degree character
            if (tempUnitConversion == "2") {
                it.unit = "${c}F"
            } else if (tempUnitConversion == "3") {
                it.unit  = "${c}C"
            }
            it.value = getAdjustedTemp(new BigDecimal(it.value))
            logging(it.descriptionText, 100)
            sendEvent(it)
        } else if(it.name == "humidity") {
            // Offset the humidity based on preference
            it.value = getAdjustedHumidity(new BigDecimal(it.value))
            logging(it.descriptionText, 100)
            sendEvent(it)
        } else if(it.name == "pressure") {
            // Offset the pressure based on preference and adjust it to the correct unit
            it.value = convertPressure(new BigDecimal(it.value))
            if(pressureUnitConversion != null) {
                it.unit = pressureUnitConversion
            } else {
                it.unit = "kPa"
            }
            logging(it.descriptionText, 100)
            sendEvent(it)
            sendEvent(name: "pressureWithUnit", value: "$it.value $it.unit", isStateChange: false)
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
    if(hideMeasurementAdjustments == true) {
        metaConfig = setPreferencesToHide(["tempOffset", "tempRes", "tempUnitConversion",
                                           "humidityOffset", "pressureOffset", "pressureUnitConversion"], metaConfig=metaConfig)
    }
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

#!include:getHelperFunctions('sensor-data')

#!include:getLoggingFunction(specialDebugLevel=True)
