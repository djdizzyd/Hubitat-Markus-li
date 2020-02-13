#!include:getHeaderLicense()

#!include:getDefaultImports()

metadata {
    // Do NOT rename the child driver name unless you also change the corresponding code in the Parent!
    definition (name: "Tasmota - Universal Dimmer (Child)", namespace: "tasmota", author: "Markus Liljergren") {
        capability "Actuator"
        capability "Switch"                       // Attributes: switch - ENUM ["on", "off"]
        capability "Light"                        // Attributes: switch - ENUM ["on", "off"]
		capability "SwitchLevel"                  // Attributes: level - NUMBER
        capability "ChangeLevel"
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
        if (it.name in ["switch", "level"]) {
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

void on() {
    parent?.componentOn(this.device)
}

void off() {
    parent?.componentOff(this.device)
}

void setLevel(BigDecimal level) {
    parent?.componentSetLevel(this.device, level)
}

void setLevel(BigDecimal level, BigDecimal duration) {
    parent?.componentSetLevel(this.device, level, duration)
}

void startLevelChange(String direction) {
    parent?.componentStartLevelChange(this.device, direction)
}

void stopLevelChange() {
    parent?.componentStopLevelChange(this.device)
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
