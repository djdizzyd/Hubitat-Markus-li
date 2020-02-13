#!include:getHeaderLicense()

#!include:getDefaultImports()

metadata {
    // Do NOT rename the child driver name unless you also change the corresponding code in the Parent!
    definition (name: "Tasmota - Universal Fan Control (Child)", namespace: "tasmota", author: "Markus Liljergren") {
        capability "FanControl"
        capability "Refresh"
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
        if (it.name in ["speed"]) {
            logging(it.descriptionText, 100)
            sendEvent(it)
            /*switch(it.value) {
                case "off":
                    setFanSpeedState("0")
                    // Buzzer 1
                    break
                case "low":
                    setFanSpeedState("33")
                    // Buzzer 1
                    break
                case "medium":
                    setFanSpeedState("66")
                    // Buzzer 2
                    break
                case "high":
                    setFanSpeedState("99")
                    // Buzzer 3
                    break
            }*/
        } else {
            log.warn "Got '$it.name' attribute data, but doesn't know what to do with it! This is probably a bug! Please report it..."
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

void setSpeed(String value) {
    parent?.componentSetSpeed(this.device, value)
    
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
